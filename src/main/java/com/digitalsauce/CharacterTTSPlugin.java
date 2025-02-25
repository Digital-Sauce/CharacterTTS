package com.digitalsauce;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetID;
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
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Timer;
import java.awt.event.ActionListener;

@Slf4j
@PluginDescriptor(
        name = "Character TTS"
)
public class CharacterTTSPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private CharacterTTSConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ConfigManager configManager;

    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception
    {
        CharacterTTSConfigPanel panel = new CharacterTTSConfigPanel(config, this, configManager);
        BufferedImage icon = loadIcon();

        navButton = NavigationButton.builder()
                .tooltip("Character TTS")
                .icon(icon)
                .priority(10)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() throws Exception
    {
        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            playCustomText(config.greeting(), config.azureVoiceName());
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        if (event.getGroupId() == WidgetID.DIALOG_NPC_GROUP_ID)
        {
            Timer timer = new Timer(200, e -> {
                Widget dialogueWidget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
                String dialogue = extractDialogue(dialogueWidget);
                if (!dialogue.isEmpty())
                {
                    log.info("NPC dialog loaded: \"" + dialogue + "\"");
                    playCustomText(dialogue, config.npcVoiceName(), config.npcPitch(), config.npcVolume());
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
        else if (event.getGroupId() == WidgetID.DIALOG_PLAYER_GROUP_ID)
        {
            Timer timer = new Timer(200, e -> {
                Widget dialogueWidget = client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT);
                String dialogue = extractDialogue(dialogueWidget);
                if (!dialogue.isEmpty())
                {
                    log.info("Player dialog loaded: \"" + dialogue + "\"");
                    playCustomText(dialogue, config.azureVoiceName(), config.pitch(), config.volume());
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private String extractDialogue(Widget dialogueWidget)
    {
        String dialogue = "";
        if (dialogueWidget != null)
        {
            dialogue = dialogueWidget.getText();
            if (dialogue == null || dialogue.trim().isEmpty())
            {
                for (Widget child : dialogueWidget.getDynamicChildren())
                {
                    String childText = child.getText();
                    if (childText != null && !childText.trim().isEmpty())
                    {
                        dialogue += childText + " ";
                    }
                }
                dialogue = dialogue.trim();
            }
        }
        return dialogue;
    }

    public void playCustomText(String text, String voice)
    {
        playCustomText(text, voice, config.pitch(), config.volume());
    }

    public void playCustomText(String text, String voice, String pitch, String volume)
    {
        new Thread(() -> {
            try {
                SpeechConfig speechConfig = SpeechConfig.fromSubscription(config.azureApiKey(), config.azureRegion());
                speechConfig.setSpeechSynthesisVoiceName(voice);

                SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig);
                speakTextWithAzure(text, voice, pitch, volume, synthesizer);

                synthesizer.close();
            }
            catch(Exception e)
            {
                log.error("Error playing custom text", e);
            }
        }).start();
    }

    private void speakTextWithAzure(String text, String voice, String pitch, String volume, SpeechSynthesizer synth)
    {
        try {
            text = text.replaceAll("(?i)<br\\s*/?>", " ");

            String ssml = "<speak version=\"1.0\" xml:lang=\"en-US\">" +
                    "<voice name=\"" + voice + "\">" +
                    "<prosody pitch=\"" + pitch + "\" volume=\"" + volume + "\">" +
                    text +
                    "</prosody>" +
                    "</voice>" +
                    "</speak>";

            log.info("Generated SSML: " + ssml);

            SpeechSynthesisResult result = synth.SpeakSsml(ssml);

            if(result.getReason() == ResultReason.Canceled)
            {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(result);
                log.error("Speech synthesis canceled: " + cancellation.getErrorDetails());
            }
        }
        catch(Exception e)
        {
            log.error("Error during speech synthesis", e);
        }
    }

    private BufferedImage loadIcon()
    {
        try
        {
            return ImageIO.read(getClass().getResourceAsStream("/charactertts_icon.png"));
        }
        catch (IOException | NullPointerException e)
        {
            log.error("Error loading icon", e);
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }

    @Provides
    CharacterTTSConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(CharacterTTSConfig.class);
    }
}
