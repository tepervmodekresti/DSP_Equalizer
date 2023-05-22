package equalizer;

public class Filter {
    protected short[] inputSignal;
    protected short[] outputSignal;

    public Filter(){
    }
    public short[] filtering(final short[] inputSignal) {
        this.inputSignal = inputSignal;
        this.outputSignal = new short[inputSignal.length];
        this.convolution();
        return this.outputSignal;
    }

    private void convolution() {
        //Функция свертки, выполняющая фильтрацию сигнала
        double tmp;
        int k;
        for(int i = 0; i <  this.inputSignal.length; i++) {
            tmp = 0;
            for(int j = 0; j < FilterInfo.COUNT_OF_COFFS; j++) {
                k = i - j;
                if(k >= 0)
                    tmp += FilterInfo.COFFS_NUM[j] * this.inputSignal[k];
            }
            this.outputSignal[i] += (short)(tmp);
        }
    }

}
