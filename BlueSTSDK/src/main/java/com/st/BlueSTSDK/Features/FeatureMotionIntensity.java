package com.st.BlueSTSDK.Features;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

public class FeatureMotionIntensity extends Feature {
    public static final String FEATURE_NAME = "MotionIntensity";
    public static final String FEATURE_UNIT = null;
    public static final String FEATURE_DATA_NAME = "Intensity";
    public static final float DATA_MAX = 10;
    public static final float DATA_MIN = 0;

    /**
     * extract the motion intensity from the sample
     * @param sample data read from the node
     * @return motion intensity or a negative number
     */
    public static byte getMotionIntensity(Sample sample){
        if(hasValidIndex(sample,0))
            return sample.data[0].byteValue();
        return -1;
    }//getMotionIntensity

    /**
     * build a activity feature
     * @param n node that will send data to this feature
     */
    public FeatureMotionIntensity(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX,DATA_MIN),
        });
    }//FeatureMotionIntensity


    /**
     * read a byte with the activity data send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the motion intensity)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {
        if (data.length - dataOffset < 1)
            throw new IllegalArgumentException("There are no 1 byte available to read");
        Sample temp = new Sample(timestamp,new Number[]{
                data[dataOffset]
        },getFieldsDesc());
        return new ExtractResult(temp,1);
    }//extractData

}
