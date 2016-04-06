package com.st.BlueSTSDK.Features.emul;

import com.st.BlueSTSDK.Features.FeaturePedometer;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeEmulator;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.util.Random;

public class FeatureRandomPedometer extends FeaturePedometer implements NodeEmulator.EmulableFeature {

    private Random mRnd = new Random();
    private int nStep=0;

    public FeatureRandomPedometer(Node parent) {
        super(parent);
    }

    @Override
    public byte[] generateFakeData() {
        nStep++;
        byte fakeData[] = new byte[8];

        byte temp[] = NumberConversion.LittleEndian.int32ToBytes(nStep);
        System.arraycopy(temp,0,fakeData,0,4);

        temp = NumberConversion.LittleEndian.floatToBytes(mRnd.nextFloat()*10);
        System.arraycopy(temp,0,fakeData,4,4);

        return fakeData;
    }
}
