package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeatureGyroscopeNorm extends Feature {
    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "GyroscopeNorm";
    /**
     * data units
     */
    public static final String FEATURE_UNIT = "dps";
    /**
     * name of the data
     */
    public static final String FEATURE_DATA_NAME = "Norm";
    /**
     * max Gyroscope handle by the sensor
     */
    public static final short DATA_MAX = 2000;
    /**
     * min Gyroscope handle by the sensor
     */
    public static final short DATA_MIN = 0;

    /**
     * build the feature
     *
     * @param n node that will provide the data
     */
    public FeatureGyroscopeNorm(Node n) {
        super(FEATURE_NAME, n,
                new Field[]{
                        new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.Float,
                                DATA_MAX, DATA_MIN)
                });
    }//FeatureGyroscopeNorm

    public static float getGyroscopeNorm(Sample sample){
        if(hasValidIndex(sample,0)){
            return sample.data[0].floatValue();
        }
        return Float.NaN;
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 2)
            throw new IllegalArgumentException("There are no 2 bytes available to read");
        Sample temp = new Sample(timestamp,new Number[]{
                NumberConversion.LittleEndian.bytesToInt16(data, dataOffset)/10.0f
        },getFieldsDesc());
        return new ExtractResult(temp,2);
    }
}
