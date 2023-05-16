package player;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioPlayer {
    private SourceDataLine sourceDataLine;
    public static final int BUFF_SIZE = 16000;
    //Переменная, отвечающая за размер буфера. При малых значениях [200-1000] появляются трески/щелчки
    private final byte[] bufferBytes  = new byte[BUFF_SIZE];
    private final short[] bufferShort = new short[BUFF_SIZE / 2];
    private AudioInputStream audioStream;
    private boolean pauseStatus;
    private final File currentMusicFile;
    private boolean stopStatus;

    public AudioPlayer(File musicFile) {
        this.currentMusicFile = musicFile;
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
                this.ByteArrayToSamplesArray();
                if (this.pauseStatus)
                    this.pause();
                if (this.stopStatus)
                    break;
                this.SampleArrayByteArray();
                this.sourceDataLine.write(this.bufferBytes, 0, this.bufferBytes.length);
            }
            this.sourceDataLine.drain();
            this.sourceDataLine.close();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
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
        if (this.audioStream != null)
            try {
                this.audioStream.close();
            } catch (IOException e) {
            }
        if (this.sourceDataLine != null)
            this.sourceDataLine.close();
    }


    private void ByteArrayToSamplesArray() {
        //Формула (32000/BUFF_SIZE - 1) нужна для выявления стыков между отсчетами при уменьшении размера буффера
        //Последние байты отсчета не обрабатываются и не воспроизводятся
        for (int i = 0, j = 0; i < this.bufferBytes.length - (32000/BUFF_SIZE - 1); i += 2, j++) {
            this.bufferShort[j] = (short) ((ByteBuffer.wrap(this.bufferBytes, i, 2).order(
                    java.nio.ByteOrder.LITTLE_ENDIAN).getShort() / 2));
        }
    }

    private void SampleArrayByteArray() {
        //Формула (32000/BUFF_SIZE - 1) нужна для выявления стыков между отсчетами при уменьшении размера буффера
        //Последние байты отсчета не обрабатываются и не воспроизводятся
        for (int i = 0, j = 0; i < this.bufferShort.length && j < (this.bufferBytes.length - (32000/BUFF_SIZE - 1)); i++, j += 2) {
            this.bufferBytes[j] = (byte) (this.bufferShort[i]);
            this.bufferBytes[j + 1] = (byte) (this.bufferShort[i] >>> 8);
        }
    }
}
