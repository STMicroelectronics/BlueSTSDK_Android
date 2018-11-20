package com.st.BlueSTSDK.Features;

import android.support.annotation.IntDef;
import android.util.SparseArray;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FeatureAILogging extends Feature {
    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "AILogging";

    public static final String FEATURE_UNIT[] = {null};
    public static final String FEATURE_DATA_NAME[] ={"status"};
    public static final Number DATA_MAX[] = {0xFF};
    public static final Number DATA_MIN[] = {0x00};

    public static final int LOG_ENABLE_INDEX = 0;

    /**
     * enum for choose the type of firmware to upload
     */
    @IntDef({LOGGING_STOPPED, LOGGING_STARTED,LOGGING_NO_SD, LOGGING_IO_ERROR,LOGGING_UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoggingStatus {}

    public static final int LOGGING_STOPPED=0x00;
    public static final int LOGGING_STARTED=0x01;
    public static final int LOGGING_NO_SD=0x02;
    public static final int LOGGING_IO_ERROR =0x03;
    private static final int LOGGING_UPDATE=0x04;
    public static final int LOGGING_UNKNOWN=0XFF;


    public static boolean isLogging(Sample s){
        return getLoggingStatus(s)==LOGGING_STARTED;
    }

    public static @FeatureAILogging.LoggingStatus
    int getLoggingStatus(Sample s){
        if(hasValidIndex(s,LOG_ENABLE_INDEX))
            return s.data[LOG_ENABLE_INDEX].intValue();
        return LOGGING_UNKNOWN;
    }

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public FeatureAILogging(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[LOG_ENABLE_INDEX],
                        FEATURE_UNIT[LOG_ENABLE_INDEX], Field.Type.UInt8,
                        DATA_MAX[LOG_ENABLE_INDEX],
                        DATA_MIN[LOG_ENABLE_INDEX]),
        });
    }



    private static Set<Feature> buildFeatureSet(Node node, long featureMask){
        Set<Feature> outList = new HashSet<>(32);
        SparseArray<Class<? extends Feature>> featureMap =
                Manager.getNodeFeatures(node.getTypeId());
        long mask= 1L<<31; //1<<31
        //we test all the 32bit of the feature mask
        for(int i=0; i<32; i++ ) {
            if ((featureMask & mask) != 0) { //if the bit is up
                Class<? extends Feature> featureClass = featureMap.get((int)mask);
                if(featureClass!=null) {
                    Feature f = node.getFeature(featureClass);
                    if(f!=null)
                        outList.add(f);
                }//if featureClass
            }//if mask
            mask = mask>>1;
        }//for
        return outList;
    }

    public void startLogging(long logMask, float enviromentalFreq,float innertialFreq,byte audioVolume){
        byte message[] = new byte[10];
        byte temp[];
        message[0]=LOGGING_STARTED;
        temp = NumberConversion.LittleEndian.uint32ToBytes(logMask);
        System.arraycopy(temp,0,message,1,temp.length);
        int envFreqdHZ = Math.round(enviromentalFreq*10);
        temp =NumberConversion.LittleEndian.uint16ToBytes(envFreqdHZ);
        System.arraycopy(temp,0,message,5,temp.length);
        int inertialFreqdHZ = Math.round(innertialFreq*10);
        temp =NumberConversion.LittleEndian.uint16ToBytes(inertialFreqdHZ);
        System.arraycopy(temp,0,message,7,temp.length);
        message[9] = audioVolume;
        writeData(message);
    }

    public void stopLogging(){
        byte message[] = {LOGGING_STOPPED};
        writeData(message);
    }

    public void updateAnnotation(String lablel){
        byte labelBytes[] = lablel.getBytes(Charset.forName("UTF8"));
        int finalLength = Math.min(18,labelBytes.length); // 18 = 20 (max ble length) -1 (status byte) -1 (string terminator)
        byte message[] = new byte[finalLength+2]; // +1 is the status +1 is the string terminator
        message[0]=LOGGING_UPDATE;
        System.arraycopy(labelBytes,0,message,1,finalLength);
        message[message.length-1]='\0';
        writeData(message);
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 1)
            throw new IllegalArgumentException("There are no bytes available to read");

        byte status = data[dataOffset];

        return new ExtractResult(new Sample(timestamp,new Number[]{status},getFieldsDesc()),1);
    }
}
