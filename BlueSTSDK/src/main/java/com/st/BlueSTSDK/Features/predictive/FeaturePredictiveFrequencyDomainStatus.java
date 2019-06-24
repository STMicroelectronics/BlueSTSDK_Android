package com.st.BlueSTSDK.Features.predictive;

import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeaturePredictiveFrequencyDomainStatus extends FeaturePredictive {
    private static final String FEATURE_NAME = "PredictiveFrequencyDomainStatus";


    private static Field buildFreqFieldNamed(String name){
        return new Field(name,"Hz", Field.Type.Float,(1<<16)/10.0f,0);
    }

    private static Field buildValueFieldNamed(String name){
        return new Field(name,"m/s^2", Field.Type.Float,(1<<16)/100.0f,0);
    }

    public FeaturePredictiveFrequencyDomainStatus(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                buildStatusFieldNamed("StatusFreq_X"),
                buildStatusFieldNamed("StatusFreq_Y"),
                buildStatusFieldNamed("StatusFreq_Z"),
                buildFreqFieldNamed("Freq_X"),
                buildFreqFieldNamed("Freq_Y"),
                buildFreqFieldNamed("Freq_Z"),
                buildValueFieldNamed("MaxAmplitude_X"),
                buildValueFieldNamed("MaxAmplitude_Y"),
                buildValueFieldNamed("MaxAmplitude_Z"),
        });
    }

    public static Status getStatusX(Sample s){
        return getStatusFromIndex(s,0);
    }

    public static Status getStatusY(Sample s){
        return getStatusFromIndex(s,1);
    }

    public static Status getStatusZ(Sample s){
        return getStatusFromIndex(s,2);
    }

    public static float getWorstXFrequency(Sample s){
        return getFloatFromIndex(s,3);
    }

    public static float getWorstYFrequency(Sample s){
        return getFloatFromIndex(s,4);
    }

    public static float getWorstZFrequency(Sample s){
        return getFloatFromIndex(s,5);
    }

    public static float getWorstXValue(Sample s){
        return getFloatFromIndex(s,6);
    }

    public static float getWorstYValue(Sample s){
        return getFloatFromIndex(s,7);
    }

    public static float getWorstZValue(Sample s){
        return getFloatFromIndex(s,8);
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 13)
            throw new IllegalArgumentException("There are no 13 bytes available to read");

        short timeStatus = NumberConversion.byteToUInt8(data,dataOffset+0);
        float freqX = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+1)/10.0f;
        float valueX = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+3)/100.0f;
        float freqY = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+5)/10.0f;
        float valueY = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+7)/100.0f;
        float freqZ = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+9)/10.0f;
        float valueZ = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+11)/100.0f;

        Sample s = new Sample(timestamp, new Number[]{
                extractXRawStatus(timeStatus),
                extractYRawStatus(timeStatus),
                extractZRawStatus(timeStatus),
                freqX,freqY,freqZ,
                valueX,valueY,valueZ
        },getFieldsDesc());

        return new ExtractResult(s,13);
    }
}
