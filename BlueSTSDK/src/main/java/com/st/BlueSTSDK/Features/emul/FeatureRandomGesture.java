package com.st.BlueSTSDK.Features.emul;

import com.st.BlueSTSDK.Features.FeatureGesture;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeEmulator;

import java.util.Random;

public class FeatureRandomGesture  extends FeatureGesture implements NodeEmulator.EmulableFeature{

    private Random mRnd = new Random();

    public FeatureRandomGesture(Node parent) {
        super(parent);
    }

    @Override
    public byte[] generateFakeData() {

        byte data[] = new byte[1];
        byte delta = DATA_MAX - DATA_MIN+1;

        data[0] = (byte) (DATA_MIN + mRnd.nextInt(delta));

        return data;
    }

}
