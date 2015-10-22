package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

/**
 * This feature contains the audio level from an array of microphones
 * since the number of microphones is not fixed it will consume all the available bytes from the
 * Bluetooth characteristics
 */
public class FeatureMicLevel extends Feature {

    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "Mic Level";
    /**
     * data units
     */
    public static final String FEATURE_UNIT = "db";
    /**
     * name of the data
     */
    public static final String FEATURE_DATA_NAME = "Mic";
    /**
     * max audio level handle by the sensor
     */
    public static final short DATA_MAX = 128;
    /**
     * min audio level handle by the sensor
     */
    public static final short DATA_MIN = 0;

    /**
     * build the feature
     *
     * @param n node that will provide the data
     */
    public FeatureMicLevel(Node n) {
        super(FEATURE_NAME, n,
                new Field[]{
                        new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt8,
                                DATA_MAX, DATA_MIN),
                });
    }//FeatureMicLevel

    /**
     * Get the auto level for the microphone {@code micLevel}
     *
     * @param s sample from the sensor
     * @param micLevel microphone to extract
     * @return microphone level or a negative number if the microphone doesn't exist
     */
    public static byte getMicLevel(Sample s,int micLevel) {
        if (micLevel < s.data.length)
                return s.data[micLevel].byteValue();
        //else
        return Byte.MIN_VALUE;
    }//getAccY

    /**
     * the number of microphone is not fixed so this function will read all the available data
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return data sample and the number of read bytes
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        int nMic = data.length-dataOffset;
        if (nMic <= 0)
            throw new IllegalArgumentException("There are no more than 1 byte available to read");

        //update the feature desc if needed
        if(mDataDesc.length!=nMic){
            Field temp[] = new Field[nMic];
            for(int i=0;i<nMic;i++){
                temp[i]= new Field(FEATURE_DATA_NAME+(i+1), FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX, DATA_MIN);
            }//for
            mDataDesc=temp;
        }//if mDataDesc

        Number levels[] = new Number[nMic];

        for(int i=0;i<nMic;i++){
            levels[i]=data[dataOffset+i];
        }//for

        return new ExtractResult(new Sample(timestamp,levels),nMic);
    }//extractData
}
