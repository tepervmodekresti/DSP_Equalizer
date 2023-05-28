package effects;

public class Vibrato extends Effect {
    private double degrees = 0;

    public Vibrato() {
    }

    public void setInputSampleStream(short[] inputAudioStream) {
        this.inputAudioStream = inputAudioStream;
    }

    @Override
    public synchronized short[] createEffect() {
        for (int i = 0; i < this.inputAudioStream.length; i++) {
            this.inputAudioStream[i] *= Math.sin(this.degrees);
        }
        this.degrees += 45;
        return this.inputAudioStream;
    }
}