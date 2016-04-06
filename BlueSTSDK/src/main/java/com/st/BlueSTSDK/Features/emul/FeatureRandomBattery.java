package com.st.BlueSTSDK.Features.emul;

import com.st.BlueSTSDK.Features.FeatureBattery;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeEmulator;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.util.Random;


public class FeatureRandomBattery extends FeatureBattery implements NodeEmulator.EmulableFeature {

    Random mRandom = new Random();
    int mCharge =5;
    public FeatureRandomBattery(Node parent) {
        super(parent);
    }


    @Override
    public byte[] generateFakeData() {
        byte data[] = new byte[7];

        byte temp[] = NumberConversion.LittleEndian.int16ToBytes((short)(mCharge*10));
        mCharge = (mCharge+10)%100;
        System.arraycopy(temp, 0, data, 0, 2);

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(mRandom.nextFloat()*1000));
        System.arraycopy(temp, 0, data, 2, 2);

        temp = NumberConversion.LittleEndian.int16ToBytes((short)((mRandom.nextFloat()*10)-5));
        System.arraycopy(temp, 0, data, 4, 2);

        temp = new byte[] {(byte) (mRandom.nextFloat()*4)};
        System.arraycopy(temp, 0, data, 5, 1);

        return data;
    }
}
