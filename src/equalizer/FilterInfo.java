package equalizer;

public final class FilterInfo {
    public final static short COUNT_OF_COFFS = 5;
    //Порядок фильтра равен COUNT_OF_COFFS - 1
    public static final double[] COFFS_NUM =  {
            0.0003160360211,-0.001251073321, 0.001870144391,-0.001251073321,0.0003160360211
    };

    public static final double[] COFFS_DEN =  {
            1,   -3.977765799,    5.934066772,   -3.934827089,   0.9785262346
    };
}