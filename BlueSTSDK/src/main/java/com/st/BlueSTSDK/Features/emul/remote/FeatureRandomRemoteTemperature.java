package com.st.BlueSTSDK.Features.emul.remote;


import com.st.BlueSTSDK.Features.remote.RemoteFeatureTemperature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeEmulator;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.util.Random;

public class FeatureRandomRemoteTemperature extends RemoteFeatureTemperature implements
        NodeEmulator.EmulableFeature {

    private Random mRnd = new Random();
    private short timeStamp = 0;

    /**
     * build a temperature feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureRandomRemoteTemperature(Node n) {
        super(n);
    }

    @Override
    public byte[] generateFakeData() {
        timeStamp++;
        byte data[] = new byte[4];

        System.arraycopy(NumberConversion.BigEndian.int16ToBytes(timeStamp++), 0, data, 0, 2);

        float delta = DATA_MAX - DATA_MIN;

        short rndData = (short) ((DATA_MIN + delta * mRnd.nextFloat()) * 10);

        System.arraycopy(NumberConversion.LittleEndian.int16ToBytes(rndData), 0, data, 2, 2);

        return data;
    }
}
