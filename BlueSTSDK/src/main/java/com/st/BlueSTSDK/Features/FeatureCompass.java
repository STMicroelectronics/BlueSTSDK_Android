package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;


/**
 * Feature that contains the orientation angle form the magnetic north
 */
public class FeatureCompass extends FeatureAutoConfigurable {

    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "Compass";
    /**
     * data units
     */
    public static final String FEATURE_UNIT = "°";
    /**
     * name of the data
     */
    public static final String FEATURE_DATA_NAME = "Angle";
    /**
     * max angle handle by the sensor
     */
    public static final float DATA_MAX = 360.0f;
    /**
     * min angle handle by the sensor
     */
    public static final float DATA_MIN = 0.0f;

    /**
     * extract the compass value from the sensor raw data
     *
     * @param sample sensor raw data
     * @return compass value or nan if the data array is not valid
     */
    public static float getCompass(Sample sample) {
        if(hasValidIndex(sample,0))
            return sample.data[0].floatValue();
        //else
        return Float.NaN;
    }

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n node that will update this feature
     */
    public FeatureCompass(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME,FEATURE_UNIT, Field.Type.Float,DATA_MAX,DATA_MIN)
        });
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 2)
            throw new IllegalArgumentException("There are no 2 bytes available to read");
        Sample temp = new Sample(timestamp,new Number[]{
                NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset)/100.0f
        },getFieldsDesc());
        return new ExtractResult(temp,2);
    }
}
