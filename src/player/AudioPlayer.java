package player;

import effects.Chorus;
import effects.Vibrato;
import equalizer.Equalizer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class AudioPlayer {
    private final File currentMusicFile;
    private AudioInputStream audioStream;
    private SourceDataLine sourceDataLine;
    public static final int BUFF_SIZE = 20000;
    private final byte[] bufferBytes = new byte[BUFF_SIZE];
    private short[] bufferShort = new short[BUFF_SIZE / 2];
    private boolean pauseStatus;
    private boolean stopStatus;
    private double gain;
    private final Equalizer equalizer;
    private final Chorus chorus;
    private boolean isChorus;
    private final Vibrato vibrato;
    private boolean isVibrato;

    public AudioPlayer(File musicFile) {
        this.currentMusicFile = musicFile;
        this.equalizer = new Equalizer();
        this.gain = 1.0;
        this.isVibrato = false;
        this.vibrato = new Vibrato();
        this.isChorus = false;
        this.chorus = new Chorus();
    }


    public void play() {
        try {
            this.audioStream = AudioSystem.getAudioInputStream(currentMusicFile);
            AudioFormat audioFormat = audioStream.getFormat();
            this.sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
            this.sourceDataLine.open(audioFormat);
            this.sourceDataLine.start();
            this.pauseStatus = false;
            this.stopStatus = false;

            while ((this.audioStream.read(this.bufferBytes) != -1)) {
                this.ByteArrayToShortArray();

                if (this.pauseStatus) this.pause();

                if (this.stopStatus) break;

                if (this.isVibrato) {
                    this.vibrato(this.bufferShort);
                }

                if (this.isChorus) {
                    this.chorus(this.bufferShort);
                }

                equalizer.setInputSignal(this.bufferShort);
                this.equalizer.equalization();
                this.bufferShort = equalizer.getOutputSignal();

                this.ShortArrayToByteArray();
                this.sourceDataLine.write(this.bufferBytes, 0, this.bufferBytes.length);
            }
            this.sourceDataLine.drain();
            this.sourceDataLine.close();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException | ExecutionException |
                 InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void pause() {
        if (this.pauseStatus) {
            while (true) {
                try {
                    if (!this.pauseStatus || this.stopStatus) break;
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void setPauseStatus(boolean pauseStatus) {
        this.pauseStatus = pauseStatus;
    }

    public void setStopStatus(boolean stopStatus) {
        this.stopStatus = stopStatus;
    }

    public boolean getStopStatus() {
        return this.stopStatus;
    }

    public void close() {
        if (this.audioStream != null) try {
            this.audioStream.close();
        } catch (IOException e) {
        }
        if (this.sourceDataLine != null) this.sourceDataLine.close();
    }

    private void ByteArrayToShortArray() {
        for (int i = 0, j = 0; i < this.bufferBytes.length; i += 2, j++) {
            this.bufferShort[j] = (short) ((ByteBuffer.wrap(this.bufferBytes, i, 2).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort() / 2) * this.gain);
        }
    }

    private void ShortArrayToByteArray() {
        for (int i = 0, j = 0; i < this.bufferShort.length && j < this.bufferBytes.length; i++, j += 2) {
            this.bufferBytes[j] = (byte) (this.bufferShort[i]);
            this.bufferBytes[j + 1] = (byte) (this.bufferShort[i] >>> 8);
        }
    }

    public void setGain(double gain) {
        this.gain = gain;
    }

    public Equalizer getEqualizer() {
        return this.equalizer;
    }

    private void vibrato(short[] inputSamples) {
        this.vibrato.setInputSampleStream(inputSamples);
        this.vibrato.createEffect();
    }

    public boolean vibratoIsActive() {
        return this.isVibrato;
    }

    public void setVibrato(boolean b) {
        this.isVibrato = b;
    }

    private void chorus(short[] inputSamples) throws ExecutionException, InterruptedException {
        chorus.setInputSampleStream(inputSamples);
        chorus.createEffect();
    }

    public boolean ChorusIsActive() {
        return this.isChorus;
    }

    public void setChorus(boolean b) {
        this.isChorus = b;
    }

}
