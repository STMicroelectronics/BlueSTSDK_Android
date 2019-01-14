package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeatureMagnetometerNorm extends Feature {
    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "MagnetometerNorm";
    /**
     * data units
     */
    public static final String FEATURE_UNIT = "mGa";
    /**
     * name of the data
     */
    public static final String FEATURE_DATA_NAME = "Norm";
    /**
     * max Magnetometer handle by the sensor
     */
    public static final short DATA_MAX = 2000;
    /**
     * min Magnetometer handle by the sensor
     */
    public static final short DATA_MIN = 0;

    /**
     * build the feature
     *
     * @param n node that will provide the data
     */
    public FeatureMagnetometerNorm(Node n) {
        super(FEATURE_NAME, n,
                new Field[]{
                        new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.Int16,
                                DATA_MAX, DATA_MIN)
                });
    }//FeatureMagnetometerNorm

    public static float getMagnetometerNorm(Sample sample){
        if(hasValidIndex(sample,0)){
            return sample.data[0].floatValue();
        }
        return -1;
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
