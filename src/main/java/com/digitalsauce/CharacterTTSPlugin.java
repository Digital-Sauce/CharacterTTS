package com.digitalsauce;

import com.google.inject.Provides;
import javax.inject.Inject;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import com.microsoft.cognitiveservices.speech.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@PluginDescriptor(
        name = "Character TTS"
)
public class CharacterTTSPlugin extends Plugin {

    public enum TtsSource {
        DIALOGUE,
        FLOATING,
        TEST
    }

    @Inject
    private Client client;

    @Inject
    private CharacterTTSConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ConfigManager configManager;

    private NavigationButton navButton;

    private final Map<Integer, String> overheadCache = new HashMap<>();
    private final Map<String, Long> lastPlayedMap = new HashMap<>();

    private boolean dialogueActive = false;
    private boolean greetingPlayed = false;

    private final MixerService mixerService;
    private final AudioService audioService;

    private static final ExecutorService ttsExecutor = Executors.newFixedThreadPool(4);

    {
        try {
            mixerService = new MixerService();
            audioService = new AudioService(mixerService);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void startUp() throws Exception {
        CharacterTTSConfigPanel panel = new CharacterTTSConfigPanel(config, this, configManager);
        BufferedImage icon = loadIcon();
        navButton = NavigationButton.builder()
                .tooltip("Character TTS")
                .icon(icon)
                .priority(10)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
        log.info("Character TTS plugin started.");
    }

    @Override
    protected void shutDown() throws Exception {
        audioService.stopAudio();
        ttsExecutor.shutdown();
        if (navButton != null) {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
        }
        greetingPlayed = false;
        log.info("Character TTS plugin stopped.");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN && !greetingPlayed) {
            playCustomText(config.greeting(), config.azureVoiceName(), config.pitch(), config.volume(), TtsSource.DIALOGUE, -1);
            greetingPlayed = true;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equalsIgnoreCase("Talk-to") && client.getLocalPlayer().getInteracting() instanceof NPC) {
            NPC npc = (NPC) client.getLocalPlayer().getInteracting();
            GenderService genderService = new GenderService();
            String gender = genderService.determineGender(npc.getId(), npc.getName());
            log.info("Pre-cached gender for NPC {} (ID {}): {}", npc.getName(), npc.getId(), gender);
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        dialogueActive = true;
        try {
            mixerService.stopDialogueStream();
        } catch (Exception e) {
            log.error("Error stopping dialogue stream: ", e);
        }
        if (event.getGroupId() == WidgetID.DIALOG_NPC_GROUP_ID) {
            Timer timer = new Timer(200, e -> {
                Widget dialogueWidget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
                Widget npcNameWidget = client.getWidget(WidgetInfo.DIALOG_NPC_NAME);
                String dialogue = extractDialogue(dialogueWidget);
                String npcName = npcNameWidget != null ? npcNameWidget.getText() : "";
                if (!dialogue.isEmpty()) {
                    if (!npcName.isEmpty() && dialogue.startsWith(npcName)) {
                        dialogue = dialogue.substring(npcName.length()).trim();
                        if (dialogue.startsWith(":")) {
                            dialogue = dialogue.substring(1).trim();
                        }
                    }
                    int npcId = getNpcId();
                    GenderService genderService = new GenderService();
                    String gender = genderService.determineGender(npcId, npcName);
                    String voice = "male".equals(gender) ? config.npcMaleVoiceName() : "female".equals(gender) ? config.npcFemaleVoiceName() : config.azureVoiceName();
                    String pitch = "male".equals(gender) ? config.npcMalePitch() : "female".equals(gender) ? config.npcFemalePitch() : config.pitch();
                    String volume = "male".equals(gender) ? config.npcMaleVolume() : "female".equals(gender) ? config.npcFemaleVolume() : config.volume();
                    playCustomText(dialogue, voice, pitch, volume, TtsSource.DIALOGUE, npcId);
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else if (event.getGroupId() == WidgetID.DIALOG_PLAYER_GROUP_ID) {
            Timer timer = new Timer(200, e -> {
                Widget dialogueWidget = client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT);
                String dialogue = extractDialogue(dialogueWidget);
                if (!dialogue.isEmpty()) {
                    playCustomText(dialogue, config.azureVoiceName(), config.pitch(), config.volume(), TtsSource.DIALOGUE, client.getLocalPlayer().getId());
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.ITEM_EXAMINE ||
                event.getType() == ChatMessageType.NPC_EXAMINE ||
                event.getType() == ChatMessageType.OBJECT_EXAMINE) {
            String examineText = event.getMessage();
            if (!examineText.isEmpty()) {
                playCustomText(examineText, config.azureVoiceName(), config.pitch(), config.volume(),
                        TtsSource.FLOATING, client.getLocalPlayer().getId());
                log.info("Playing examine text: " + examineText);
            }
        }
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) {
        Actor actor = event.getActor();
        String overhead = event.getOverheadText();
        if (overhead == null || overhead.trim().isEmpty() ||
                (dialogueActive && config.blockFloatingTextWhileDialogue())) return;


        if (actor instanceof Player) {
            Player player = (Player) actor;
            int playerId = player.getId();
            if (player.equals(client.getLocalPlayer())) {
                // Player's overhead as internal dialogue
                playCustomText(overhead, config.azureVoiceName(), config.pitch(), config.volume(),
                        TtsSource.FLOATING, playerId);
                log.info("Playing player floating text: " + overhead);
            } else {
                // Other players with distance attenuation
                WorldPoint targetLoc = player.getWorldLocation();
                String attenuation = getProximityAttenuation(player, config.volume());
                playCustomText(overhead, config.azureVoiceName(), config.pitch(), attenuation,
                        TtsSource.FLOATING, playerId);
                log.info("Playing other player floating text: " + overhead);
            }
        } else if (actor instanceof NPC) {
            NPC npc = (NPC) actor;
            if (npc.getId() == getNpcId()) return; // Skip active NPC (dialogue)
            WorldPoint npcLoc = npc.getWorldLocation();
            GenderService genderService = new GenderService();
            String gender = genderService.determineGender(npc.getId(), npc.getName());
            String voice = "male".equals(gender) ? config.npcMaleVoiceName() : "female".equals(gender) ? config.npcFemaleVoiceName() : config.azureVoiceName();
            String pitch = "male".equals(gender) ? config.npcMalePitch() : "female".equals(gender) ? config.npcFemalePitch() : config.pitch();
            String baseVolume = "male".equals(gender) ? config.npcMaleVolume() : "female".equals(gender) ? config.npcFemaleVolume() : config.volume();
            String attenuation = getProximityAttenuation(npc, baseVolume);
            playCustomText(overhead, voice, pitch, attenuation, TtsSource.FLOATING, npc.getId());
            log.info("Playing NPC floating text: " + overhead);
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        Widget dialogueWidgetPlayer = client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT);
        Widget dialogueWidgetNPC = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
        Widget dialogueWidgetOption = client.getWidget(WidgetInfo.DIALOG_OPTION);

        dialogueActive = (dialogueWidgetPlayer != null && dialogueWidgetPlayer.getBounds().height > 0) ||
                (dialogueWidgetNPC != null && dialogueWidgetNPC.getBounds().height > 0) ||
                (dialogueWidgetOption != null && dialogueWidgetOption.getBounds().height > 0);
        if (!dialogueActive) {
            try {
                mixerService.stopDialogueStream();
            } catch (Exception e) {
                log.error("Error stopping dialogue stream: ", e);
            }
        }
    }

    private String getProximityAttenuation(Actor target, String baseVolumeStr) {
        WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();
        WorldPoint targetLoc = target.getWorldLocation();
        int distance = playerLoc.distanceTo(targetLoc);
        double ratio = distance >= config.maxDistance() ? config.minVolumeRatio() :
                config.minVolumeRatio() + (1.0 - ((double) distance / config.maxDistance())) * (config.maxVolumeRatio() - config.minVolumeRatio());
        try {
            double baseVolume = parseVolume(baseVolumeStr);
            double effectiveVolume = baseVolume * ratio;
            double attenuation = effectiveVolume - 100.0;
            return (int) attenuation + "%";
        } catch (Exception e) {
            log.error("Error computing proximity attenuation: {}", e.getMessage());
            return baseVolumeStr;
        }
    }

    private double parseVolume(String volumeStr) {
        if (volumeStr.equalsIgnoreCase("default")) return 100.0;
        if (volumeStr.equalsIgnoreCase("silent")) return 0.0;
        try {
            if (volumeStr.startsWith("-")) {
                return 100.0 - Double.parseDouble(volumeStr.substring(1).replace("%", "").trim());
            } else if (volumeStr.startsWith("+")) {
                return 100.0 + Double.parseDouble(volumeStr.substring(1).replace("%", "").trim());
            } else {
                return Double.parseDouble(volumeStr.replace("%", "").trim());
            }
        } catch (Exception e) {
            log.error("Error parsing volume string '{}': {}", volumeStr, e.getMessage());
            return 100.0;
        }
    }

    private String extractDialogue(Widget dialogueWidget) {
        if (dialogueWidget == null) return "";
        String dialogue = dialogueWidget.getText();
        if (dialogue == null || dialogue.trim().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Widget child : dialogueWidget.getDynamicChildren()) {
                String childText = child.getText();
                if (childText != null && !childText.trim().isEmpty()) {
                    sb.append(childText).append(" ");
                }
            }
            dialogue = sb.toString().trim();
        }
        return dialogue;
    }

    public void playCustomText(String text, String voice, String pitch, String volume, TtsSource source, int sourceId) {
        final String safeText = text.replaceAll("(?i)<br\\s*/?>", " ").trim();
        String cacheKey = sourceId + ":" + safeText;
        long now = System.currentTimeMillis();
        if (lastPlayedMap.containsKey(cacheKey) && (now - lastPlayedMap.get(cacheKey)) < config.playbackCooldown()) {
            return;
        }
        lastPlayedMap.put(cacheKey, now);

        ttsExecutor.submit(() -> {
            try {
                SpeechConfig speechConfig = SpeechConfig.fromSubscription(config.azureApiKey(), config.azureRegion());
                speechConfig.setProperty(PropertyId.SpeechServiceConnection_EndSilenceTimeoutMs, "0");
                speechConfig.setProperty(PropertyId.SpeechServiceResponse_RequestSentenceBoundary, "true");
                speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Raw16Khz16BitMonoPcm);
                speechConfig.setSpeechSynthesisVoiceName(voice);

                String ssml = "<speak version=\"1.0\" xml:lang=\"en-US\">" +
                        "<voice name=\"" + voice + "\">" +
                        "<prosody pitch=\"" + pitch + "\" volume=\"" + volume + "\">" +
                        safeText +
                        "</prosody></voice></speak>";

                AudioOutputStream outputStream = AudioOutputStream.createPullStream();
                AudioConfig audioConfig = AudioConfig.fromStreamOutput(outputStream);
                SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, audioConfig);

                Future<SpeechSynthesisResult> resultFuture = synthesizer.StartSpeakingSsmlAsync(ssml);
                SpeechSynthesisResult result = resultFuture.get();

                if (result.getReason() == ResultReason.SynthesizingAudioStarted || result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                    AudioDataStream audioDataStream = AudioDataStream.fromResult(result);
                    audioService.playStreamingAudio(audioDataStream, source);
                } else {
                    log.error("Speech synthesis failed with reason: {}", result.getReason());
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "TTS failed: " + result.getReason(), null);
                }
                synthesizer.close();
            } catch (Exception e) {
                log.error("Error in TTS execution: ", e);
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "TTS failed: " + e.getMessage(), null);
            }
        });
    }

    private int getNpcId() {
        Actor interacting = client.getLocalPlayer() != null ? client.getLocalPlayer().getInteracting() : null;
        return interacting instanceof NPC ? ((NPC) interacting).getId() : -1;
    }

    @Provides
    CharacterTTSConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CharacterTTSConfig.class);
    }

    private BufferedImage loadIcon() {
        try {
            return ImageIO.read(getClass().getResourceAsStream("/charactertts_icon.png"));
        } catch (IOException | NullPointerException e) {
            log.error("Error loading icon: ", e);
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }
}