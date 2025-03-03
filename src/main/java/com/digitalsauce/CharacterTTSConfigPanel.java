package com.digitalsauce;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.config.ConfigManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CharacterTTSConfigPanel extends PluginPanel {
    private final CharacterTTSPlugin plugin;
    private final CharacterTTSConfig config;
    private final ConfigManager configManager;

    private final JTextField azureApiKeyField;
    private final JTextField azureRegionField;
    private final JTextField playerTestTextField;
    private final JButton playerSendButton;
    private final JTextField npcMaleTestTextField;
    private final JButton npcMaleSendButton;
    private final JTextField npcFemaleTestTextField;
    private final JButton npcFemaleSendButton;
    private final JCheckBox blockFloatingTextCheckbox;
    private final JComboBox<String> voiceDropdown;
    private final JComboBox<String> npcMaleVoiceDropdown;
    private final JComboBox<String> npcFemaleVoiceDropdown;
    private JSlider pitchSlider = null;
    private JSlider volumeSlider = null;
    private final JLabel pitchValueLabel;
    private final JLabel volumeValueLabel;
    private JSlider npcMalePitchSlider = null;
    private JSlider npcMaleVolumeSlider = null;
    private final JLabel npcMalePitchValueLabel;
    private final JLabel npcMaleVolumeValueLabel;
    private JSlider npcFemalePitchSlider = null;
    private JSlider npcFemaleVolumeSlider = null;
    private final JLabel npcFemalePitchValueLabel;
    private final JLabel npcFemaleVolumeValueLabel;
    private final JTextField npcNameField;
    private final JTextField npcIdField;
    private final JComboBox<String> genderOverrideDropdown;
    private final JButton saveConfigButton;

    public CharacterTTSConfigPanel(CharacterTTSConfig config, CharacterTTSPlugin plugin, ConfigManager configManager) {
        this.config = config;
        this.plugin = plugin;
        this.configManager = configManager;
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        azureApiKeyField = new JTextField(config.azureApiKey(), 20);
        azureRegionField = new JTextField(config.azureRegion(), 20);
        String[] defaultVoices = {"Loading voices..."};
        voiceDropdown = new JComboBox<>(defaultVoices);
        npcMaleVoiceDropdown = new JComboBox<>(defaultVoices);
        npcFemaleVoiceDropdown = new JComboBox<>(defaultVoices);

        playerTestTextField = new JTextField(20);
        playerSendButton = new JButton("Send Player Text");
        playerSendButton.addActionListener(e -> {
            String text = playerTestTextField.getText().trim();
            if (!text.isEmpty()) {
                plugin.playCustomText(text, (String) voiceDropdown.getSelectedItem(), formatPitch(pitchSlider.getValue()),
                        formatVolume(volumeSlider.getValue()), CharacterTTSPlugin.TtsSource.TEST, -1);
            }
        });
        npcMaleTestTextField = new JTextField(20);
        npcMaleSendButton = new JButton("Send Male NPC Text");
        npcMaleSendButton.addActionListener(e -> {
            String text = npcMaleTestTextField.getText().trim();
            if (!text.isEmpty()) {
                plugin.playCustomText(text, (String) npcMaleVoiceDropdown.getSelectedItem(), formatPitch(npcMalePitchSlider.getValue()),
                        formatVolume(npcMaleVolumeSlider.getValue()), CharacterTTSPlugin.TtsSource.TEST, -1);
            }
        });
        npcFemaleTestTextField = new JTextField(20);
        npcFemaleSendButton = new JButton("Send Female NPC Text");
        npcFemaleSendButton.addActionListener(e -> {
            String text = npcFemaleTestTextField.getText().trim();
            if (!text.isEmpty()) {
                plugin.playCustomText(text, (String) npcFemaleVoiceDropdown.getSelectedItem(), formatPitch(npcFemalePitchSlider.getValue()),
                        formatVolume(npcFemaleVolumeSlider.getValue()), CharacterTTSPlugin.TtsSource.TEST, -1);
            }
        });

        pitchValueLabel = new JLabel();
        volumeValueLabel = new JLabel();
        npcMalePitchValueLabel = new JLabel();
        npcMaleVolumeValueLabel = new JLabel();
        npcFemalePitchValueLabel = new JLabel();
        npcFemaleVolumeValueLabel = new JLabel();

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel apiConfigPanel = new JPanel();
        apiConfigPanel.setLayout(new BoxLayout(apiConfigPanel, BoxLayout.Y_AXIS));
        apiConfigPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        apiConfigPanel.setBorder(new TitledBorder("API Configuration"));
        apiConfigPanel.add(new JLabel("API Key:"));
        apiConfigPanel.add(azureApiKeyField);
        apiConfigPanel.add(Box.createVerticalStrut(5));
        apiConfigPanel.add(new JLabel("Azure Region:"));
        apiConfigPanel.add(azureRegionField);
        JButton refreshVoicesButton = new JButton("Refresh Voices");
        refreshVoicesButton.addActionListener(e -> updateVoiceDropdown());
        apiConfigPanel.add(refreshVoicesButton);
        mainContent.add(apiConfigPanel);
        mainContent.add(Box.createVerticalStrut(10));

        JPanel testTTSPanel = new JPanel();
        testTTSPanel.setLayout(new BoxLayout(testTTSPanel, BoxLayout.Y_AXIS));
        testTTSPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        testTTSPanel.setBorder(new TitledBorder("Test TTS"));

        JPanel playerTestPanel = new JPanel();
        playerTestPanel.setLayout(new BoxLayout(playerTestPanel, BoxLayout.Y_AXIS));
        playerTestPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        playerTestPanel.setBorder(BorderFactory.createTitledBorder("Test Player Text"));
        playerTestPanel.add(playerTestTextField);
        playerTestPanel.add(playerSendButton);
        testTTSPanel.add(playerTestPanel);
        testTTSPanel.add(Box.createVerticalStrut(5));

        JPanel npcMaleTestPanel = new JPanel();
        npcMaleTestPanel.setLayout(new BoxLayout(npcMaleTestPanel, BoxLayout.Y_AXIS));
        npcMaleTestPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        npcMaleTestPanel.setBorder(BorderFactory.createTitledBorder("Test Male NPC Text"));
        npcMaleTestPanel.add(npcMaleTestTextField);
        npcMaleTestPanel.add(npcMaleSendButton);
        testTTSPanel.add(npcMaleTestPanel);
        testTTSPanel.add(Box.createVerticalStrut(5));

        JPanel npcFemaleTestPanel = new JPanel();
        npcFemaleTestPanel.setLayout(new BoxLayout(npcFemaleTestPanel, BoxLayout.Y_AXIS));
        npcFemaleTestPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        npcFemaleTestPanel.setBorder(BorderFactory.createTitledBorder("Test Female NPC Text"));
        npcFemaleTestPanel.add(npcFemaleTestTextField);
        npcFemaleTestPanel.add(npcFemaleSendButton);
        testTTSPanel.add(npcFemaleTestPanel);
        mainContent.add(testTTSPanel);
        mainContent.add(Box.createVerticalStrut(10));

        JPanel blockerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        blockerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        blockerPanel.setBorder(new TitledBorder("Floating Text Options"));
        blockFloatingTextCheckbox = new JCheckBox("Block floating text while in dialogue", config.blockFloatingTextWhileDialogue());
        blockFloatingTextCheckbox.setBackground(ColorScheme.DARK_GRAY_COLOR);
        blockFloatingTextCheckbox.addActionListener(e -> configManager.setConfiguration("charactertts", "blockFloatingTextWhileDialogue",
                blockFloatingTextCheckbox.isSelected()));
        blockerPanel.add(blockFloatingTextCheckbox);
        mainContent.add(blockerPanel);
        mainContent.add(Box.createVerticalStrut(10));

        JPanel voiceConfigPanel = new JPanel();
        voiceConfigPanel.setLayout(new BoxLayout(voiceConfigPanel, BoxLayout.Y_AXIS));
        voiceConfigPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        voiceConfigPanel.setBorder(new TitledBorder("Voice Configuration"));

        JPanel voiceDropdownPanel = new JPanel();
        voiceDropdownPanel.setLayout(new BoxLayout(voiceDropdownPanel, BoxLayout.Y_AXIS));
        voiceDropdownPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        voiceDropdownPanel.add(new JLabel("Player Voice:"));
        voiceDropdownPanel.add(voiceDropdown);
        voiceDropdownPanel.add(Box.createVerticalStrut(5));
        voiceDropdownPanel.add(new JLabel("NPC Male Voice:"));
        voiceDropdownPanel.add(npcMaleVoiceDropdown);
        voiceDropdownPanel.add(Box.createVerticalStrut(5));
        voiceDropdownPanel.add(new JLabel("NPC Female Voice:"));
        voiceDropdownPanel.add(npcFemaleVoiceDropdown);
        voiceConfigPanel.add(voiceDropdownPanel);
        voiceConfigPanel.add(Box.createVerticalStrut(10));

        pitchSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 0);
        pitchSlider.setMajorTickSpacing(25);
        pitchSlider.setMinorTickSpacing(5);
        pitchSlider.setPaintTicks(true);
        voiceConfigPanel.add(createSliderPanel("Player Pitch:", pitchSlider, pitchValueLabel, this::formatPitch));
        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        volumeSlider.setMajorTickSpacing(50);
        volumeSlider.setMinorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        voiceConfigPanel.add(createSliderPanel("Player Volume:", volumeSlider, volumeValueLabel, this::formatVolume));
        npcMalePitchSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 0);
        npcMalePitchSlider.setMajorTickSpacing(25);
        npcMalePitchSlider.setMinorTickSpacing(5);
        npcMalePitchSlider.setPaintTicks(true);
        voiceConfigPanel.add(createSliderPanel("NPC Male Pitch:", npcMalePitchSlider, npcMalePitchValueLabel, this::formatPitch));
        npcMaleVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        npcMaleVolumeSlider.setMajorTickSpacing(50);
        npcMaleVolumeSlider.setMinorTickSpacing(25);
        npcMaleVolumeSlider.setPaintTicks(true);
        voiceConfigPanel.add(createSliderPanel("NPC Male Volume:", npcMaleVolumeSlider, npcMaleVolumeValueLabel, this::formatVolume));
        npcFemalePitchSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 0);
        npcFemalePitchSlider.setMajorTickSpacing(25);
        npcFemalePitchSlider.setMinorTickSpacing(5);
        npcFemalePitchSlider.setPaintTicks(true);
        voiceConfigPanel.add(createSliderPanel("NPC Female Pitch:", npcFemalePitchSlider, npcFemalePitchValueLabel, this::formatPitch));
        npcFemaleVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        npcFemaleVolumeSlider.setMajorTickSpacing(50);
        npcFemaleVolumeSlider.setMinorTickSpacing(25);
        npcFemaleVolumeSlider.setPaintTicks(true);
        voiceConfigPanel.add(createSliderPanel("NPC Female Volume:", npcFemaleVolumeSlider, npcFemaleVolumeValueLabel, this::formatVolume));
        mainContent.add(voiceConfigPanel);
        mainContent.add(Box.createVerticalStrut(10));

        JPanel genderOverridePanel = new JPanel();
        genderOverridePanel.setLayout(new BoxLayout(genderOverridePanel, BoxLayout.Y_AXIS));
        genderOverridePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        genderOverridePanel.setBorder(new TitledBorder("Gender Override"));
        npcNameField = new JTextField(20);
        npcIdField = new JTextField(5);
        genderOverrideDropdown = new JComboBox<>(new String[]{"male", "female", "unknown"});
        JButton overrideButton = new JButton("Override Gender");
        overrideButton.addActionListener(e -> {
            String npcName = npcNameField.getText().trim();
            String npcIdStr = npcIdField.getText().trim();
            String gender = (String) genderOverrideDropdown.getSelectedItem();
            if (!npcName.isEmpty()) {
                GenderService genderService = new GenderService();
                int npcId = npcIdStr.isEmpty() ? -1 : Integer.parseInt(npcIdStr);
                genderService.overrideGender(npcName, npcId, gender);
                log.info("Gender overridden for {} (ID: {}) to {}", npcName, npcId, gender);
            }
        });
        genderOverridePanel.add(new JLabel("NPC Name:"));
        genderOverridePanel.add(npcNameField);
        genderOverridePanel.add(new JLabel("NPC ID (optional):"));
        genderOverridePanel.add(npcIdField);
        genderOverridePanel.add(new JLabel("Gender:"));
        genderOverridePanel.add(genderOverrideDropdown);
        genderOverridePanel.add(overrideButton);
        mainContent.add(genderOverridePanel);
        mainContent.add(Box.createVerticalStrut(10));

        saveConfigButton = new JButton("Save All");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        buttonPanel.add(saveConfigButton);
        buttonPanel.setPreferredSize(new Dimension(0, 80));
        saveConfigButton.addActionListener(e -> {
            configManager.setConfiguration("charactertts", "azureApiKey", azureApiKeyField.getText());
            configManager.setConfiguration("charactertts", "azureRegion", azureRegionField.getText());
            configManager.setConfiguration("charactertts", "pitch", formatPitch(pitchSlider.getValue()));
            configManager.setConfiguration("charactertts", "volume", formatVolume(volumeSlider.getValue()));
            configManager.setConfiguration("charactertts", "azureVoiceName", (String) voiceDropdown.getSelectedItem());
            configManager.setConfiguration("charactertts", "npcMaleVoiceName", (String) npcMaleVoiceDropdown.getSelectedItem());
            configManager.setConfiguration("charactertts", "npcFemaleVoiceName", (String) npcFemaleVoiceDropdown.getSelectedItem());
            configManager.setConfiguration("charactertts", "npcMalePitch", formatPitch(npcMalePitchSlider.getValue()));
            configManager.setConfiguration("charactertts", "npcMaleVolume", formatVolume(npcMaleVolumeSlider.getValue()));
            configManager.setConfiguration("charactertts", "npcFemalePitch", formatPitch(npcFemalePitchSlider.getValue()));
            configManager.setConfiguration("charactertts", "npcFemaleVolume", formatVolume(npcFemaleVolumeSlider.getValue()));
            configManager.setConfiguration("charactertts", "blockFloatingTextWhileDialogue", blockFloatingTextCheckbox.isSelected());
            log.info("Configuration saved.");
        });

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Component view = scrollPane.getViewport().getView();
                if (view != null) {
                    Dimension newSize = view.getPreferredSize();
                    newSize.width = scrollPane.getViewport().getWidth();
                    view.setPreferredSize(newSize);
                    view.revalidate();
                }
            }
        });

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(scrollPane, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(outerPanel, BorderLayout.CENTER);

        if (!azureApiKeyField.getText().isEmpty() && !azureRegionField.getText().isEmpty()) {
            updateVoiceDropdown();
        }

        DocumentListener refreshListener = new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { updateVoiceDropdown(); }
            @Override public void removeUpdate(DocumentEvent e) { updateVoiceDropdown(); }
            @Override public void insertUpdate(DocumentEvent e) { updateVoiceDropdown(); }
        };
        azureApiKeyField.getDocument().addDocumentListener(refreshListener);
        azureRegionField.getDocument().addDocumentListener(refreshListener);
    }

    private String formatPitch(int sliderValue) {
        if (sliderValue == 0) return "default";
        return sliderValue > 0 ? "+" + sliderValue + "%" : sliderValue + "%";
    }

    private String formatVolume(int sliderValue) {
        if (sliderValue == 100) return "default";
        if (sliderValue == 0) return "silent";
        return sliderValue < 100 ? "-" + (100 - sliderValue) + "%" : "+" + (sliderValue - 100) + "%";
    }

    private JPanel createSliderPanel(String labelText, JSlider slider, JLabel valueLabel, Function<Integer, String> formatter) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        panel.add(new JLabel(labelText));
        slider.setPaintLabels(false);
        slider.addChangeListener(e -> valueLabel.setText(formatter.apply(slider.getValue())));
        panel.add(slider);
        valueLabel.setText(formatter.apply(slider.getValue()));
        panel.add(valueLabel);
        return panel;
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
                        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(voices.toArray(new String[0]));
                        voiceDropdown.setModel(model);
                        voiceDropdown.setSelectedItem(config.azureVoiceName());
                        npcMaleVoiceDropdown.setModel(new DefaultComboBoxModel<>(voices.toArray(new String[0])));
                        npcMaleVoiceDropdown.setSelectedItem(config.npcMaleVoiceName());
                        npcFemaleVoiceDropdown.setModel(new DefaultComboBoxModel<>(voices.toArray(new String[0])));
                        npcFemaleVoiceDropdown.setSelectedItem(config.npcFemaleVoiceName());
                    } else {
                        log.warn("No voices fetched from Azure");
                        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(new String[]{"No voices found"});
                        voiceDropdown.setModel(model);
                        npcMaleVoiceDropdown.setModel(model);
                        npcFemaleVoiceDropdown.setModel(model);
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
            String url = "https://" + config.azureRegion() + ".tts.speech.microsoft.com/cognitiveservices/voices/list";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Ocp-Apim-Subscription-Key", config.azureApiKey())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray voicesArray = new JSONArray(response.body());
            for (int i = 0; i < voicesArray.length(); i++) {
                voiceNames.add(voicesArray.getJSONObject(i).getString("ShortName"));
            }
        } catch (Exception e) {
            log.error("Error fetching voices from Azure: ", e);
        }
        return voiceNames;
    }
}