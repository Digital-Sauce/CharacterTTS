package com.digitalsauce;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.config.ConfigManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import lombok.extern.slf4j.Slf4j;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

@Slf4j
public class CharacterTTSConfigPanel extends PluginPanel
{
    private final CharacterTTSPlugin plugin;
    private final CharacterTTSConfig config;
    private final ConfigManager configManager;

    // API Configuration fields
    private final JTextField azureApiKeyField;
    private final JTextField azureRegionField;

    // Components for testing TTS
    private JTextField playerTestTextField;
    private JButton playerSendButton;
    private JTextField npcTestTextField;
    private JButton npcSendButton;

    // Components for configuration (Player settings)
    private final JComboBox<String> voiceDropdown; // For player voice

    // Components for configuration (NPC settings)
    private final JComboBox<String> npcVoiceDropdown; // For NPC voice

    private JSlider pitchSlider;      // For player pitch
    private JSlider volumeSlider;     // For player volume
    private final JLabel pitchValueLabel;
    private final JLabel volumeValueLabel;

    private JSlider npcPitchSlider;   // For NPC pitch
    private JSlider npcVolumeSlider;  // For NPC volume
    private final JLabel npcPitchValueLabel;
    private final JLabel npcVolumeValueLabel;

    private final JButton saveConfigButton;

    public CharacterTTSConfigPanel(CharacterTTSConfig config, CharacterTTSPlugin plugin, ConfigManager configManager)
    {
        this.config = config;
        this.plugin = plugin;
        this.configManager = configManager;
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Initialize API configuration fields with current config values
        azureApiKeyField = new JTextField(config.azureApiKey(), 20);
        azureRegionField = new JTextField(config.azureRegion(), 20);

        // Initialize both voice dropdowns with a default value
        String[] defaultVoices = {"Loading voices..."};
        voiceDropdown = new JComboBox<>(defaultVoices);
        npcVoiceDropdown = new JComboBox<>(defaultVoices);

        // Initialize test text fields and buttons for player and NPC
        playerTestTextField = new JTextField(20);
        playerSendButton = new JButton("Send Player Text");
        npcTestTextField = new JTextField(20);
        npcSendButton = new JButton("Send NPC Text");

        // Pre-initialize labels (they will be assigned after slider creation)
        pitchValueLabel = new JLabel();
        volumeValueLabel = new JLabel();
        npcPitchValueLabel = new JLabel();
        npcVolumeValueLabel = new JLabel();

        // Create main panel with vertical BoxLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // --- API Configuration Section ---
        JPanel apiConfigPanel = new JPanel(new GridBagLayout());
        apiConfigPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        apiConfigPanel.setBorder(new TitledBorder("API Configuration"));
        GridBagConstraints apiGbc = new GridBagConstraints();
        apiGbc.insets = new Insets(5, 5, 5, 5);
        apiGbc.fill = GridBagConstraints.HORIZONTAL;

        // API Key Label and Field
        apiGbc.gridx = 0;
        apiGbc.gridy = 0;
        apiGbc.weightx = 0;
        apiConfigPanel.add(new JLabel("API Key:"), apiGbc);
        apiGbc.gridx = 1;
        apiGbc.weightx = 1.0;
        apiConfigPanel.add(azureApiKeyField, apiGbc);

        // Azure Region Label and Field
        apiGbc.gridx = 0;
        apiGbc.gridy = 1;
        apiGbc.weightx = 0;
        apiConfigPanel.add(new JLabel("Azure Region:"), apiGbc);
        apiGbc.gridx = 1;
        apiGbc.weightx = 1.0;
        apiConfigPanel.add(azureRegionField, apiGbc);

        mainPanel.add(apiConfigPanel);

        // --- Test TTS Section ---
        JPanel testTTSPanel = new JPanel();
        testTTSPanel.setLayout(new BoxLayout(testTTSPanel, BoxLayout.Y_AXIS));
        testTTSPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        testTTSPanel.setBorder(new TitledBorder("Test TTS"));

        // Player Test TTS Panel
        JPanel playerTestPanel = new JPanel(new BorderLayout());
        playerTestPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        playerTestPanel.setBorder(BorderFactory.createTitledBorder("Test Player Text"));
        playerTestTextField = new JTextField(30);
        playerTestTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        playerTestPanel.add(playerTestTextField, BorderLayout.CENTER);
        playerSendButton = new JButton("Send Player Text");
        playerSendButton.addActionListener(e -> {
            String text = playerTestTextField.getText();
            String selectedVoice = (String) voiceDropdown.getSelectedItem();
            String pitchStr = formatPitch(pitchSlider.getValue());
            String volumeStr = formatVolume(volumeSlider.getValue());
            if (text != null && !text.trim().isEmpty())
            {
                plugin.playCustomText(text, selectedVoice, pitchStr, volumeStr);
            }
        });
        playerTestPanel.add(playerSendButton, BorderLayout.SOUTH);
        testTTSPanel.add(playerTestPanel);

        // NPC Test TTS Panel
        JPanel npcTestPanel = new JPanel(new BorderLayout());
        npcTestPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        npcTestPanel.setBorder(BorderFactory.createTitledBorder("Test NPC Text"));
        npcTestTextField = new JTextField(30);
        npcTestTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        npcTestPanel.add(npcTestTextField, BorderLayout.CENTER);
        npcSendButton = new JButton("Send NPC Text");
        npcSendButton.addActionListener(e -> {
            String text = npcTestTextField.getText();
            String selectedVoice = (String) npcVoiceDropdown.getSelectedItem();
            String pitchStr = formatPitch(npcPitchSlider.getValue());
            String volumeStr = formatVolume(npcVolumeSlider.getValue());
            if (text != null && !text.trim().isEmpty())
            {
                plugin.playCustomText(text, selectedVoice, pitchStr, volumeStr);
            }
        });
        npcTestPanel.add(npcSendButton, BorderLayout.SOUTH);
        testTTSPanel.add(npcTestPanel);

        mainPanel.add(testTTSPanel);

        // --- Voice Configuration Section ---
        JPanel voiceConfigPanel = new JPanel(new GridBagLayout());
        voiceConfigPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        voiceConfigPanel.setBorder(new TitledBorder("Voice Configuration"));
        GridBagConstraints voiceGbc = new GridBagConstraints();
        voiceGbc.insets = new Insets(5, 5, 5, 5);
        voiceGbc.fill = GridBagConstraints.HORIZONTAL;

        // Player Voice Dropdown
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 0;
        voiceGbc.weightx = 0;
        voiceConfigPanel.add(new JLabel("Player Voice:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.gridy = 0;
        voiceGbc.weightx = 1.0;
        voiceGbc.gridwidth = 2;
        voiceConfigPanel.add(voiceDropdown, voiceGbc);

        // NPC Voice Dropdown
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 1;
        voiceGbc.weightx = 0;
        voiceGbc.gridwidth = 1;
        voiceConfigPanel.add(new JLabel("NPC Voice:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.gridy = 1;
        voiceGbc.weightx = 1.0;
        voiceGbc.gridwidth = 2;
        voiceConfigPanel.add(npcVoiceDropdown, voiceGbc);
        voiceGbc.gridwidth = 1; // Reset gridwidth

        // Player Pitch Slider
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 2;
        voiceGbc.weightx = 0;
        voiceConfigPanel.add(new JLabel("Player Pitch:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.weightx = 1.0;
        pitchSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 0);
        pitchSlider.setMajorTickSpacing(25);
        pitchSlider.setMinorTickSpacing(5);
        pitchSlider.setPaintTicks(true);
        pitchSlider.setPaintLabels(false);
        voiceConfigPanel.add(pitchSlider, voiceGbc);
        voiceGbc.gridx = 2;
        voiceGbc.weightx = 0;
        pitchValueLabel.setText(formatPitch(pitchSlider.getValue()));
        voiceConfigPanel.add(pitchValueLabel, voiceGbc);
        pitchSlider.addChangeListener(e -> pitchValueLabel.setText(formatPitch(pitchSlider.getValue())));

        // Player Volume Slider
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 3;
        voiceGbc.weightx = 0;
        voiceConfigPanel.add(new JLabel("Player Volume:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.weightx = 1.0;
        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        volumeSlider.setMajorTickSpacing(50);
        volumeSlider.setMinorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(false);
        voiceConfigPanel.add(volumeSlider, voiceGbc);
        voiceGbc.gridx = 2;
        voiceGbc.weightx = 0;
        volumeValueLabel.setText(formatVolume(volumeSlider.getValue()));
        voiceConfigPanel.add(volumeValueLabel, voiceGbc);
        volumeSlider.addChangeListener(e -> volumeValueLabel.setText(formatVolume(volumeSlider.getValue())));

        // NPC Pitch Slider
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 4;
        voiceGbc.weightx = 0;
        voiceConfigPanel.add(new JLabel("NPC Pitch:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.weightx = 1.0;
        npcPitchSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 0);
        npcPitchSlider.setMajorTickSpacing(25);
        npcPitchSlider.setMinorTickSpacing(5);
        npcPitchSlider.setPaintTicks(true);
        npcPitchSlider.setPaintLabels(false);
        voiceConfigPanel.add(npcPitchSlider, voiceGbc);
        voiceGbc.gridx = 2;
        voiceGbc.weightx = 0;
        npcPitchValueLabel.setText(formatPitch(npcPitchSlider.getValue()));
        voiceConfigPanel.add(npcPitchValueLabel, voiceGbc);
        npcPitchSlider.addChangeListener(e -> npcPitchValueLabel.setText(formatPitch(npcPitchSlider.getValue())));

        // NPC Volume Slider
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 5;
        voiceGbc.weightx = 0;
        voiceConfigPanel.add(new JLabel("NPC Volume:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.weightx = 1.0;
        npcVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        npcVolumeSlider.setMajorTickSpacing(50);
        npcVolumeSlider.setMinorTickSpacing(25);
        npcVolumeSlider.setPaintTicks(true);
        npcVolumeSlider.setPaintLabels(false);
        voiceConfigPanel.add(npcVolumeSlider, voiceGbc);
        voiceGbc.gridx = 2;
        voiceGbc.weightx = 0;
        npcVolumeValueLabel.setText(formatVolume(npcVolumeSlider.getValue()));
        voiceConfigPanel.add(npcVolumeValueLabel, voiceGbc);
        npcVolumeSlider.addChangeListener(e -> npcVolumeValueLabel.setText(formatVolume(npcVolumeSlider.getValue())));

        // Save All Button
        voiceGbc.gridx = 1;
        voiceGbc.gridy = 6;
        voiceGbc.weightx = 0;
        voiceGbc.gridwidth = 2;
        saveConfigButton = new JButton("Save All");
        saveConfigButton.addActionListener(e -> {
            String newApiKey = azureApiKeyField.getText();
            String newRegion = azureRegionField.getText();
            String playerPitch = formatPitch(pitchSlider.getValue());
            String playerVolume = formatVolume(volumeSlider.getValue());
            String playerVoice = (String) voiceDropdown.getSelectedItem();
            String npcVoice = (String) npcVoiceDropdown.getSelectedItem();
            String npcPitch = formatPitch(npcPitchSlider.getValue());
            String npcVolume = formatVolume(npcVolumeSlider.getValue());
            configManager.setConfiguration("charactertts", "azureApiKey", newApiKey);
            configManager.setConfiguration("charactertts", "azureRegion", newRegion);
            configManager.setConfiguration("charactertts", "pitch", playerPitch);
            configManager.setConfiguration("charactertts", "volume", playerVolume);
            configManager.setConfiguration("charactertts", "azureVoiceName", playerVoice);
            configManager.setConfiguration("charactertts", "npcVoiceName", npcVoice);
            configManager.setConfiguration("charactertts", "npcPitch", npcPitch);
            configManager.setConfiguration("charactertts", "npcVolume", npcVolume);

            // When both API key and region are provided, attempt to populate the voices list again.
            if (!newApiKey.isEmpty() && !newRegion.isEmpty())
            {
                updateVoiceDropdown();
            }
        });
        voiceConfigPanel.add(saveConfigButton, voiceGbc);

        mainPanel.add(voiceConfigPanel);

        add(mainPanel, BorderLayout.NORTH);
        // Optionally, you may call updateVoiceDropdown() here if API credentials are already present.
    }

    private String formatPitch(int sliderValue)
    {
        if (sliderValue == 0)
        {
            return "default";
        }
        else if (sliderValue > 0)
        {
            return "+" + sliderValue + "%";
        }
        else
        {
            return sliderValue + "%";
        }
    }

    /**
     * Converts the volume slider value (0–200) into a volume string:
     * - 100 returns "default"
     * - 0 returns "silent"
     * - Values below 100 become negative percentages (e.g. 50 -> "-50%")
     * - Values above 100 become positive percentages (e.g. 150 -> "+50%")
     */
    private String formatVolume(int sliderValue)
    {
        if (sliderValue == 100)
        {
            return "default";
        }
        else if (sliderValue == 0)
        {
            return "silent";
        }
        else if (sliderValue < 100)
        {
            int diff = 100 - sliderValue;
            return "-" + diff + "%";
        }
        else // sliderValue > 100
        {
            int diff = sliderValue - 100;
            return "+" + diff + "%";
        }
    }

    private void updateVoiceDropdown() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return fetchVoices();
            }
            @Override
            protected void done() {
                try {
                    List<String> voices = get();
                    if (voices != null && !voices.isEmpty()) {
                        // Update player voice dropdown
                        DefaultComboBoxModel<String> model1 = new DefaultComboBoxModel<>(voices.toArray(new String[0]));
                        voiceDropdown.setModel(model1);
                        voiceDropdown.setSelectedItem(config.azureVoiceName());

                        // Update NPC voice dropdown
                        DefaultComboBoxModel<String> model2 = new DefaultComboBoxModel<>(voices.toArray(new String[0]));
                        npcVoiceDropdown.setModel(model2);
                        npcVoiceDropdown.setSelectedItem(config.npcVoiceName());
                    }
                    else {
                        voiceDropdown.setModel(new DefaultComboBoxModel<>(new String[]{"No voices found"}));
                        npcVoiceDropdown.setModel(new DefaultComboBoxModel<>(new String[]{"No voices found"}));
                    }
                } catch (Exception e) {
                    log.error("Error updating voices dropdown: ", e);
                }
            }
        }.execute();
    }

    private List<String> fetchVoices() {
        List<String> voiceNames = new ArrayList<>();
        try {
            String region = config.azureRegion();
            String subscriptionKey = config.azureApiKey();
            String url = "https://" + region + ".tts.speech.microsoft.com/cognitiveservices/voices/list";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Ocp-Apim-Subscription-Key", subscriptionKey)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray voicesArray = new JSONArray(response.body());
            for (int i = 0; i < voicesArray.length(); i++) {
                JSONObject voiceObj = voicesArray.getJSONObject(i);
                String voiceName = voiceObj.getString("ShortName");
                voiceNames.add(voiceName);
            }
        } catch (Exception e) {
            log.error("Error fetching voices from Azure: ", e);
        }
        return voiceNames;
    }
}
