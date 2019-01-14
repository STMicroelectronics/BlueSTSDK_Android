package com.st.BlueSTSDK.Features;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeatureMotorTimeParameter extends Feature {
    public static final String FEATURE_NAME = "MotorTimeParameter";
    public static final String FEATURE_ACC_UNIT = "m/s^2";
    public static final String FEATURE_SPEED_UNIT = "mm/s";

    public static final String[] FEATURE_DATA_NAME = {"Acc X Peak", "Acc Y Peak", "Acc Z Peak"
            ,"RMS Speed X","RMS Speed Y","RMS Speed Z"};
    public static final short DATA_ACC_MAX = 2000;
    public static final short DATA_ACC_MIN = -2000;

    public static final float DATA_SPEED_MAX = 2000;
    public static final float DATA_SPEED_MIN = 0.0f;

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public FeatureMotorTimeParameter(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[0],FEATURE_ACC_UNIT,Field.Type.Float,DATA_ACC_MAX,DATA_ACC_MIN),
                new Field(FEATURE_DATA_NAME[1],FEATURE_ACC_UNIT,Field.Type.Float,DATA_ACC_MAX,DATA_ACC_MIN),
                new Field(FEATURE_DATA_NAME[2],FEATURE_ACC_UNIT,Field.Type.Float,DATA_ACC_MAX,DATA_ACC_MIN),
                new Field(FEATURE_DATA_NAME[3],FEATURE_SPEED_UNIT,Field.Type.Float,DATA_SPEED_MAX,DATA_SPEED_MIN),
                new Field(FEATURE_DATA_NAME[4],FEATURE_SPEED_UNIT,Field.Type.Float,DATA_SPEED_MAX,DATA_SPEED_MIN),
                new Field(FEATURE_DATA_NAME[5],FEATURE_SPEED_UNIT,Field.Type.Float,DATA_SPEED_MAX,DATA_SPEED_MIN)
        });
    }

    private static float extractFloatOrNan(Sample s, int index){
        if(hasValidIndex(s,index))
            return s.data[index].floatValue();
        return Float.NaN;
    }

    public static float getAccPeakX(Sample s){
        return extractFloatOrNan(s,0);
    }

    public static float getAccPeakY(Sample s){
        return extractFloatOrNan(s,1);
    }

    public static float getAccPeakZ(Sample s){
        return extractFloatOrNan(s,2);
    }

    public static float getRMSSpeedX(Sample s){
        return extractFloatOrNan(s,3);
    }

    public static float getRMSSpeedY(Sample s){
        return extractFloatOrNan(s,4);
    }

    public static float getRMSSpeedZ(Sample s){
        return extractFloatOrNan(s,5);
    }


    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 18)
            throw new IllegalArgumentException("There are no 18 bytes available to read");

        float maxAccX = NumberConversion.LittleEndian.bytesToInt16(data,dataOffset+0)/100.0f;
        float maxAccY = NumberConversion.LittleEndian.bytesToInt16(data,dataOffset+2)/100.0f;
        float maxAccZ = NumberConversion.LittleEndian.bytesToInt16(data,dataOffset+4)/100.0f;

        float speedX = NumberConversion.LittleEndian.bytesToFloat(data,dataOffset+6);
        float speedY = NumberConversion.LittleEndian.bytesToFloat(data,dataOffset+10);
        float speedZ = NumberConversion.LittleEndian.bytesToFloat(data,dataOffset+14);

        Sample sample = new Sample(new Number[]{maxAccX,maxAccY,maxAccZ,speedX,speedY,speedZ},getFieldsDesc());

        return new ExtractResult(sample,18);
    }
}
