package com.digitalsauce;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixerService {
    private static final Logger log = LoggerFactory.getLogger(MixerService.class);
    private final AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
    private final List<AudioInputStream> floatingStreams = new LinkedList<>();
    private AudioInputStream dialogueStream = null;
    private final Object lock = new Object();
    private SourceDataLine line;
    private Thread mixerThread;
    private volatile boolean running = false;

    public MixerService() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        running = true;
        mixerThread = new Thread(this::mixLoop, "AudioMixerThread");
        mixerThread.start();
        log.info("MixerService started with format: {}", format);
    }

    public void addFloatingAudio(AudioInputStream stream) {
        synchronized (lock) {
            floatingStreams.add(stream);
            log.info("Floating audio added. Total floating streams: {}", floatingStreams.size());
        }
    }

    public void addDialogueAudio(AudioInputStream stream) {
        synchronized (lock) {
            if (dialogueStream != null) {
                try {
                    dialogueStream.close();
                } catch (IOException e) {
                    log.error("Error closing previous dialogue stream: ", e);
                }
            }
            dialogueStream = stream;
            log.info("Dialogue stream set.");
        }
    }

    public void stopDialogueStream() {
        synchronized (lock) {
            if (dialogueStream != null) {
                try {
                    dialogueStream.close();
                    log.info("Dialogue stream stopped.");
                } catch (IOException e) {
                    log.error("Error stopping dialogue stream: ", e);
                }
                dialogueStream = null;
            }
        }
    }

    private void mixLoop() {
        int bufferSize = 1024;
        byte[] mixBuffer = new byte[bufferSize];
        byte[] tempBuffer = new byte[bufferSize];
        int samplesPerBuffer = bufferSize / 2;
        double bufferDurationSeconds = samplesPerBuffer / format.getSampleRate();
        long bufferDurationMillis = (long) (bufferDurationSeconds * 1000);

        while (running) {
            int activeStreams = 0;
            short[] mixSamples = new short[samplesPerBuffer];

            synchronized (lock) {
                if (dialogueStream != null) {
                    try {
                        int bytesRead = dialogueStream.read(tempBuffer, 0, bufferSize);
                        if (bytesRead == -1) {
                            dialogueStream.close();
                            dialogueStream = null;
                        } else {
                            if (bytesRead < bufferSize) {
                                for (int i = bytesRead; i < bufferSize; i++) {
                                    tempBuffer[i] = 0;
                                }
                            }
                            activeStreams++;
                            for (int i = 0; i < samplesPerBuffer; i++) {
                                int low = tempBuffer[2 * i] & 0xff;
                                int high = tempBuffer[2 * i + 1];
                                mixSamples[i] += (short) ((high << 8) | low);
                            }
                        }
                    } catch (IOException ex) {
                        log.error("Error reading dialogue stream: ", ex);
                        dialogueStream = null;
                    }
                }

                Iterator<AudioInputStream> it = floatingStreams.iterator();
                while (it.hasNext()) {
                    AudioInputStream stream = it.next();
                    try {
                        int bytesRead = stream.read(tempBuffer, 0, bufferSize);
                        if (bytesRead == -1) {
                            stream.close();
                            it.remove();
                            continue;
                        }
                        if (bytesRead < bufferSize) {
                            for (int i = bytesRead; i < bufferSize; i++) {
                                tempBuffer[i] = 0;
                            }
                        }
                        activeStreams++;
                        for (int i = 0; i < samplesPerBuffer; i++) {
                            int low = tempBuffer[2 * i] & 0xff;
                            int high = tempBuffer[2 * i + 1];
                            mixSamples[i] += (short) ((high << 8) | low);
                        }
                    } catch (IOException ex) {
                        log.error("Error reading floating stream: ", ex);
                        try {
                            stream.close();
                        } catch (IOException e) {
                            log.error("Error closing floating stream: ", e);
                        }
                        it.remove();
                    }
                }
            }

            if (activeStreams > 1) {
                for (int i = 0; i < samplesPerBuffer; i++) {
                    mixSamples[i] = (short) (mixSamples[i] / activeStreams);
                }
            }

            for (int i = 0; i < samplesPerBuffer; i++) {
                mixBuffer[2 * i] = (byte) (mixSamples[i] & 0xff);
                mixBuffer[2 * i + 1] = (byte) (mixSamples[i] >> 8 & 0xff);
            }

            line.write(mixBuffer, 0, mixBuffer.length);

            try {
                Thread.sleep(bufferDurationMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        line.drain();
        line.stop();
        line.close();
        log.info("MixerService stopped.");
    }

    public void stop() {
        running = false;
        try {
            mixerThread.join();
            log.info("Mixer thread joined. MixerService fully stopped.");
        } catch (InterruptedException ex) {
            log.error("Mixer thread interrupted: ", ex);
            Thread.currentThread().interrupt();
        }
    }
}