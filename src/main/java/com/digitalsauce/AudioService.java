package com.digitalsauce;

import com.microsoft.cognitiveservices.speech.AudioDataStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AudioService {
    private static final Logger log = Logger.getLogger(AudioService.class.getName());
    private static final int BUFFER_SIZE = 1024; // Aligned with MixerService
    private final MixerService mixerService;

    public AudioService(MixerService mixerService) {
        this.mixerService = mixerService;
    }

    public void playStreamingAudio(AudioDataStream audioDataStream, CharacterTTSPlugin.TtsSource source) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = (int) audioDataStream.readData(buffer)) > 0) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] audioBytes = baos.toByteArray();
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            AudioInputStream stream = new AudioInputStream(
                    new ByteArrayInputStream(audioBytes),
                    format,
                    audioBytes.length / format.getFrameSize()
            );
            if (source == CharacterTTSPlugin.TtsSource.DIALOGUE) {
                mixerService.addDialogueAudio(stream);
            } else {
                mixerService.addFloatingAudio(stream);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error streaming audio: ", e);
        }
    }

    public void stopAudio() {
        mixerService.stop();
    }
}