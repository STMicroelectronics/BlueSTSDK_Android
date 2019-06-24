package com.st.BlueSTSDK.Features.predictive;

import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeaturePredictiveSpeedStatus extends FeaturePredictive {

    private static final String FEATURE_NAME = "PredictiveSpeedStatus";

    public static Status getStatusX(Sample s){
        return getStatusFromIndex(s,0);
    }

    public static Status getStatusY(Sample s){
        return getStatusFromIndex(s,1);
    }

    public static Status getStatusZ(Sample s){
        return getStatusFromIndex(s,2);
    }

    public static float getSpeedX(Sample s){
        return getFloatFromIndex(s,3);
    }

    public static float getSpeedY(Sample s){
        return getFloatFromIndex(s,4);
    }

    public static float getSpeedZ(Sample s){
        return getFloatFromIndex(s,5);
    }


    private static Field buildSpeedFieldNamed(String name){
        return new Field(name,"mm/s",Field.Type.Float,Float.MAX_VALUE,0);
    }

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public FeaturePredictiveSpeedStatus(Node n) {
        super(FEATURE_NAME, n, new Field[]{
            buildStatusFieldNamed("StatusSpeed_X"),
            buildStatusFieldNamed("StatusSpeed_Y"),
            buildStatusFieldNamed("StatusSpeed_Z"),
            buildSpeedFieldNamed("RMSSpeed_X"),
            buildSpeedFieldNamed("RMSSpeed_Y"),
            buildSpeedFieldNamed("RMSSpeed_Z"),
        });
    }


    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {

        if (data.length - dataOffset < 12)
            throw new IllegalArgumentException("There are no 12 bytes available to read");

        short timeStatus = NumberConversion.byteToUInt8(data,dataOffset+0);
        float speedX = NumberConversion.LittleEndian.bytesToFloat(data,dataOffset+1);
        float speedY = NumberConversion.LittleEndian.bytesToFloat(data,dataOffset+5);
        float speedZ = NumberConversion.LittleEndian.bytesToFloat(data,dataOffset+9);

        Sample s = new Sample(timestamp, new Number[]{
                extractXRawStatus(timeStatus),
                extractYRawStatus(timeStatus),
                extractZRawStatus(timeStatus),
                speedX,
                speedY,
                speedZ
        },getFieldsDesc());

        return new ExtractResult(s,12);
    }
}
