package com.digitalsauce;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.config.ConfigManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

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

    private JTextField npcMaleTestTextField;
    private JButton npcMaleSendButton;

    private JTextField npcFemaleTestTextField;
    private JButton npcFemaleSendButton;

    // Components for configuration (Player settings)
    private final JComboBox<String> voiceDropdown; // For player voice

    // Components for configuration (NPC Male settings)
    private final JComboBox<String> npcMaleVoiceDropdown; // For NPC male voice

    // Components for configuration (NPC Female settings)
    private final JComboBox<String> npcFemaleVoiceDropdown; // For NPC female voice

    private JSlider pitchSlider;      // For player pitch
    private JSlider volumeSlider;     // For player volume
    private final JLabel pitchValueLabel;
    private final JLabel volumeValueLabel;

    private JSlider npcMalePitchSlider;   // For NPC male pitch
    private JSlider npcMaleVolumeSlider;  // For NPC male volume
    private final JLabel npcMalePitchValueLabel;
    private final JLabel npcMaleVolumeValueLabel;

    private JSlider npcFemalePitchSlider;   // For NPC female pitch
    private JSlider npcFemaleVolumeSlider;  // For NPC female volume
    private final JLabel npcFemalePitchValueLabel;
    private final JLabel npcFemaleVolumeValueLabel;

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

        // Initialize voice dropdowns with a default value
        String[] defaultVoices = {"Loading voices..."};
        voiceDropdown = new JComboBox<>(defaultVoices);
        npcMaleVoiceDropdown = new JComboBox<>(defaultVoices);
        npcFemaleVoiceDropdown = new JComboBox<>(defaultVoices);

        // Initialize test text fields and buttons
        playerTestTextField = new JTextField(20);
        playerSendButton = new JButton("Send Player Text");

        npcMaleTestTextField = new JTextField(20);
        npcMaleSendButton = new JButton("Send Male NPC Text");

        npcFemaleTestTextField = new JTextField(20);
        npcFemaleSendButton = new JButton("Send Female NPC Text");

        // Pre-initialize labels for sliders
        pitchValueLabel = new JLabel();
        volumeValueLabel = new JLabel();
        npcMalePitchValueLabel = new JLabel();
        npcMaleVolumeValueLabel = new JLabel();
        npcFemalePitchValueLabel = new JLabel();
        npcFemaleVolumeValueLabel = new JLabel();

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

        apiGbc.gridx = 0;
        apiGbc.gridy = 0;
        apiGbc.weightx = 0;
        apiConfigPanel.add(new JLabel("API Key:"), apiGbc);
        apiGbc.gridx = 1;
        apiGbc.weightx = 1.0;
        apiConfigPanel.add(azureApiKeyField, apiGbc);

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
        playerTestTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        playerTestPanel.add(playerTestTextField, BorderLayout.CENTER);
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

        // NPC Male Test TTS Panel
        JPanel npcMaleTestPanel = new JPanel(new BorderLayout());
        npcMaleTestPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        npcMaleTestPanel.setBorder(BorderFactory.createTitledBorder("Test Male NPC Text"));
        npcMaleTestTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        npcMaleTestPanel.add(npcMaleTestTextField, BorderLayout.CENTER);
        npcMaleSendButton.addActionListener(e -> {
            String text = npcMaleTestTextField.getText();
            String selectedVoice = (String) npcMaleVoiceDropdown.getSelectedItem();
            String pitchStr = formatPitch(npcMalePitchSlider.getValue());
            String volumeStr = formatVolume(npcMaleVolumeSlider.getValue());
            if (text != null && !text.trim().isEmpty())
            {
                plugin.playCustomText(text, selectedVoice, pitchStr, volumeStr);
            }
        });
        npcMaleTestPanel.add(npcMaleSendButton, BorderLayout.SOUTH);
        testTTSPanel.add(npcMaleTestPanel);

        // NPC Female Test TTS Panel
        JPanel npcFemaleTestPanel = new JPanel(new BorderLayout());
        npcFemaleTestPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        npcFemaleTestPanel.setBorder(BorderFactory.createTitledBorder("Test Female NPC Text"));
        npcFemaleTestTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        npcFemaleTestPanel.add(npcFemaleTestTextField, BorderLayout.CENTER);
        npcFemaleSendButton.addActionListener(e -> {
            String text = npcFemaleTestTextField.getText();
            String selectedVoice = (String) npcFemaleVoiceDropdown.getSelectedItem();
            String pitchStr = formatPitch(npcFemalePitchSlider.getValue());
            String volumeStr = formatVolume(npcFemaleVolumeSlider.getValue());
            if (text != null && !text.trim().isEmpty())
            {
                plugin.playCustomText(text, selectedVoice, pitchStr, volumeStr);
            }
        });
        npcFemaleTestPanel.add(npcFemaleSendButton, BorderLayout.SOUTH);
        testTTSPanel.add(npcFemaleTestPanel);

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

        // NPC Male Voice Dropdown
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 1;
        voiceGbc.weightx = 0;
        voiceGbc.gridwidth = 1;
        voiceConfigPanel.add(new JLabel("NPC Male Voice:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.gridy = 1;
        voiceGbc.weightx = 1.0;
        voiceGbc.gridwidth = 2;
        voiceConfigPanel.add(npcMaleVoiceDropdown, voiceGbc);

        // NPC Female Voice Dropdown
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 2;
        voiceGbc.weightx = 0;
        voiceGbc.gridwidth = 1;
        voiceConfigPanel.add(new JLabel("NPC Female Voice:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.gridy = 2;
        voiceGbc.weightx = 1.0;
        voiceGbc.gridwidth = 2;
        voiceConfigPanel.add(npcFemaleVoiceDropdown, voiceGbc);
        voiceGbc.gridwidth = 1; // Reset gridwidth

        // Player Pitch Slider
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 3;
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
        voiceGbc.gridy = 4;
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

        // NPC Male Pitch Slider
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 5;
        voiceGbc.weightx = 0;
        voiceConfigPanel.add(new JLabel("NPC Male Pitch:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.weightx = 1.0;
        npcMalePitchSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 0);
        npcMalePitchSlider.setMajorTickSpacing(25);
        npcMalePitchSlider.setMinorTickSpacing(5);
        npcMalePitchSlider.setPaintTicks(true);
        npcMalePitchSlider.setPaintLabels(false);
        voiceConfigPanel.add(npcMalePitchSlider, voiceGbc);
        voiceGbc.gridx = 2;
        voiceGbc.weightx = 0;
        npcMalePitchValueLabel.setText(formatPitch(npcMalePitchSlider.getValue()));
        voiceConfigPanel.add(npcMalePitchValueLabel, voiceGbc);
        npcMalePitchSlider.addChangeListener(e -> npcMalePitchValueLabel.setText(formatPitch(npcMalePitchSlider.getValue())));

        // NPC Male Volume Slider
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 6;
        voiceGbc.weightx = 0;
        voiceConfigPanel.add(new JLabel("NPC Male Volume:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.weightx = 1.0;
        npcMaleVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        npcMaleVolumeSlider.setMajorTickSpacing(50);
        npcMaleVolumeSlider.setMinorTickSpacing(25);
        npcMaleVolumeSlider.setPaintTicks(true);
        npcMaleVolumeSlider.setPaintLabels(false);
        voiceConfigPanel.add(npcMaleVolumeSlider, voiceGbc);
        voiceGbc.gridx = 2;
        voiceGbc.weightx = 0;
        npcMaleVolumeValueLabel.setText(formatVolume(npcMaleVolumeSlider.getValue()));
        voiceConfigPanel.add(npcMaleVolumeValueLabel, voiceGbc);
        npcMaleVolumeSlider.addChangeListener(e -> npcMaleVolumeValueLabel.setText(formatVolume(npcMaleVolumeSlider.getValue())));

        // NPC Female Pitch Slider
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 7;
        voiceGbc.weightx = 0;
        voiceConfigPanel.add(new JLabel("NPC Female Pitch:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.weightx = 1.0;
        npcFemalePitchSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 0);
        npcFemalePitchSlider.setMajorTickSpacing(25);
        npcFemalePitchSlider.setMinorTickSpacing(5);
        npcFemalePitchSlider.setPaintTicks(true);
        npcFemalePitchSlider.setPaintLabels(false);
        voiceConfigPanel.add(npcFemalePitchSlider, voiceGbc);
        voiceGbc.gridx = 2;
        voiceGbc.weightx = 0;
        npcFemalePitchValueLabel.setText(formatPitch(npcFemalePitchSlider.getValue()));
        voiceConfigPanel.add(npcFemalePitchValueLabel, voiceGbc);
        npcFemalePitchSlider.addChangeListener(e -> npcFemalePitchValueLabel.setText(formatPitch(npcFemalePitchSlider.getValue())));

        // NPC Female Volume Slider
        voiceGbc.gridx = 0;
        voiceGbc.gridy = 8;
        voiceGbc.weightx = 0;
        voiceConfigPanel.add(new JLabel("NPC Female Volume:"), voiceGbc);
        voiceGbc.gridx = 1;
        voiceGbc.weightx = 1.0;
        npcFemaleVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        npcFemaleVolumeSlider.setMajorTickSpacing(50);
        npcFemaleVolumeSlider.setMinorTickSpacing(25);
        npcFemaleVolumeSlider.setPaintTicks(true);
        npcFemaleVolumeSlider.setPaintLabels(false);
        voiceConfigPanel.add(npcFemaleVolumeSlider, voiceGbc);
        voiceGbc.gridx = 2;
        voiceGbc.weightx = 0;
        npcFemaleVolumeValueLabel.setText(formatVolume(npcFemaleVolumeSlider.getValue()));
        voiceConfigPanel.add(npcFemaleVolumeValueLabel, voiceGbc);
        npcFemaleVolumeSlider.addChangeListener(e -> npcFemaleVolumeValueLabel.setText(formatVolume(npcFemaleVolumeSlider.getValue())));

        // Save All Button
        voiceGbc.gridx = 1;
        voiceGbc.gridy = 9;
        voiceGbc.weightx = 0;
        voiceGbc.gridwidth = 2;
        saveConfigButton = new JButton("Save All");
        saveConfigButton.addActionListener(e -> {
            String newApiKey = azureApiKeyField.getText();
            String newRegion = azureRegionField.getText();
            String playerPitch = formatPitch(pitchSlider.getValue());
            String playerVolume = formatVolume(volumeSlider.getValue());
            String playerVoice = (String) voiceDropdown.getSelectedItem();
            String npcMaleVoice = (String) npcMaleVoiceDropdown.getSelectedItem();
            String npcFemaleVoice = (String) npcFemaleVoiceDropdown.getSelectedItem();
            String npcMalePitch = formatPitch(npcMalePitchSlider.getValue());
            String npcMaleVolume = formatVolume(npcMaleVolumeSlider.getValue());
            String npcFemalePitch = formatPitch(npcFemalePitchSlider.getValue());
            String npcFemaleVolume = formatVolume(npcFemaleVolumeSlider.getValue());
            configManager.setConfiguration("charactertts", "azureApiKey", newApiKey);
            configManager.setConfiguration("charactertts", "azureRegion", newRegion);
            configManager.setConfiguration("charactertts", "pitch", playerPitch);
            configManager.setConfiguration("charactertts", "volume", playerVolume);
            configManager.setConfiguration("charactertts", "azureVoiceName", playerVoice);
            configManager.setConfiguration("charactertts", "npcMaleVoiceName", npcMaleVoice);
            configManager.setConfiguration("charactertts", "npcFemaleVoiceName", npcFemaleVoice);
            configManager.setConfiguration("charactertts", "npcMalePitch", npcMalePitch);
            configManager.setConfiguration("charactertts", "npcMaleVolume", npcMaleVolume);
            configManager.setConfiguration("charactertts", "npcFemalePitch", npcFemalePitch);
            configManager.setConfiguration("charactertts", "npcFemaleVolume", npcFemaleVolume);

            // When both API key and region are provided, attempt to populate the voices list again.
            if (!newApiKey.isEmpty() && !newRegion.isEmpty())
            {
                updateVoiceDropdown();
            }
        });
        voiceConfigPanel.add(saveConfigButton, voiceGbc);

        mainPanel.add(voiceConfigPanel);
        add(mainPanel, BorderLayout.NORTH);
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
     * Converts the volume slider value (0–200) into a volume string.
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
        else
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

                        // Update NPC male voice dropdown
                        DefaultComboBoxModel<String> model2 = new DefaultComboBoxModel<>(voices.toArray(new String[0]));
                        npcMaleVoiceDropdown.setModel(model2);
                        npcMaleVoiceDropdown.setSelectedItem(config.npcMaleVoiceName());

                        // Update NPC female voice dropdown
                        DefaultComboBoxModel<String> model3 = new DefaultComboBoxModel<>(voices.toArray(new String[0]));
                        npcFemaleVoiceDropdown.setModel(model3);
                        npcFemaleVoiceDropdown.setSelectedItem(config.npcFemaleVoiceName());
                    }
                    else {
                        voiceDropdown.setModel(new DefaultComboBoxModel<>(new String[]{"No voices found"}));
                        npcMaleVoiceDropdown.setModel(new DefaultComboBoxModel<>(new String[]{"No voices found"}));
                        npcFemaleVoiceDropdown.setModel(new DefaultComboBoxModel<>(new String[]{"No voices found"}));
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
