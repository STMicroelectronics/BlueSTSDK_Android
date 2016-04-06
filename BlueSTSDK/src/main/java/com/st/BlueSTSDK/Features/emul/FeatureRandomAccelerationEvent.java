package com.st.BlueSTSDK.Features.emul;


import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeEmulator;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.util.Random;

public class FeatureRandomAccelerationEvent extends FeatureAccelerationEvent implements
        NodeEmulator.EmulableFeature {

    Random mRandom = new Random();

    /**
     * set of enabled algorithm
     */
    private DetectableEvent mEnabledEvent;

    public FeatureRandomAccelerationEvent(Node parent) {
        super(parent);
    }

    @Override
    public boolean detectEvent(DetectableEvent event, boolean enable){

        if(enable)
            mEnabledEvent=event;
        else
            mEnabledEvent=null;

        return true;
    }


    @Override
    public DetectableEvent getEnabledEvent() {
        return mEnabledEvent;
    }

    @Override
    public byte[] generateFakeData() {
        if(mEnabledEvent== DetectableEvent.PEDOMETER)
            return NumberConversion.LittleEndian.int16ToBytes((short)mRandom.nextInt());
        else
            return new byte[]{0};
    }
}
