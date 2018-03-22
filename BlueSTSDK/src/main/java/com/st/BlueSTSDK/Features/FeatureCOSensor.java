package com.st.BlueSTSDK.Features;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;


public class FeatureCOSensor extends Feature {

    private final static String FEATURE_NAME = "CO Sensor";
    /**
     * data units
     */
    public static final String FEATURE_UNIT = "ppm";
    /**
     * name of the data
     */
    public static final String FEATURE_DATA_NAME = "CO Concentration";
    /**
     * max acceleration handle by the sensor
     */
    public static final float DATA_MAX = 1000000;
    /**
     * min acceleration handle by the sensor
     */
    public static final float DATA_MIN = 0;


    private static final byte SET_SENSITIVITY_CMD = 1;
    private static final byte GET_SENSITIVITY_CMD = 0;

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public FeatureCOSensor(Node n) {
        super(FEATURE_NAME, n,
                new Field[]{
                    new Field(FEATURE_DATA_NAME,FEATURE_UNIT, Field.Type.Float,DATA_MAX,DATA_MIN)
                });
    }

    /**
     * extract the luminosity value from the feature raw data
     *
     * @param s feature raw data
     * @return luminosity value or -1 if the data array is not valid
     */
    public static float getGasPresence(Sample s) {
        if (hasValidIndex(s,0))
            return s.data[0].floatValue();
        //else
        return Float.NaN;
    }

    public void setSensorSensitivity(float newSensitivity){
        byte data[] = NumberConversion.LittleEndian.floatToBytes(newSensitivity);
        sendCommand(SET_SENSITIVITY_CMD,data);
    }

    public void requestSensitivity(){
        sendCommand(GET_SENSITIVITY_CMD,new byte[]{});
    }


    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 4)
            throw new IllegalArgumentException("There are no 4 bytes available to read");
        Sample temp = new Sample(timestamp,new Number[]{
                NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset)/100.0
        },getFieldsDesc());
        return new ExtractResult(temp,4);
    }

    @Override
    protected void parseCommandResponse(int timeStamp, byte commandType, byte[] data) {
        if(commandType == GET_SENSITIVITY_CMD){
            float sensitivity = NumberConversion.LittleEndian.bytesToFloat(data);
            notifySensitivityRead(sensitivity);
        }
    }

    private void notifySensitivityRead(final float sensitivity) {
        for (final FeatureListener listener : mFeatureListener) {
            if (listener instanceof FeatureCOSensorListener)
                sThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((FeatureCOSensorListener) listener)
                                .onSensorSensitivityRead(FeatureCOSensor.this, sensitivity);
                    }//run
                });
        }//for
    }//notifyUpdate

    public interface FeatureCOSensorListener extends FeatureListener{

        void onSensorSensitivityRead(@NonNull FeatureCOSensor feature, float sensitivity);

    }

}
