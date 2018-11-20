package com.st.BlueSTSDK.Features;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

public class FeatureAudioSceneClassification extends Feature {

    public static final String FEATURE_NAME = "Audio Scene Classification";
    public static final String FEATURE_UNIT = null;
    public static final String FEATURE_DATA_NAME = "SceneType";
    public static final short DATA_MAX = 3;
    public static final short DATA_MIN = 0;

    /**
     * Enum containing the possible result of the gesture detection
     */
    public enum Scene {
        UNKNOWN,
        INDOOR,
        OUTDOOR,
        IN_VEICLE,
        ERROR

    }//Position

    /**
     * extract the gesture from a sensor sample
     * @param sample data read from the node
     * @return gesture detected by the node
     */
    public static Scene getScene(Sample sample){
        if(hasValidIndex(sample,0)){
            int activityId = sample.data[0].byteValue();
            switch (activityId){
                case -1:
                    return Scene.UNKNOWN;
                case 0x00:
                    return Scene.INDOOR;
                case 0x01:
                    return Scene.OUTDOOR;
                case 0x02:
                    return Scene.IN_VEICLE;
                default:
                    return Scene.ERROR;
            }//switch
        }//if
        //else
        return Scene.ERROR;
    }//getGesture

    /**
     * build a carry gesture feature
     * @param n node that will send data to this feature
     */
    public FeatureAudioSceneClassification(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX,DATA_MIN)
        });
    }//FeatureGesture

    /**
     * read a byte with the audio scene send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the audio scene information)
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
