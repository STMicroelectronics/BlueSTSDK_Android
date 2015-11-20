package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Feature;

import org.junit.Assert;
import org.junit.Test;

public class TestGestureFeature {

    @Test
    public void testInvalidSampleGestureId(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{});
        Assert.assertEquals(FeatureGesture.Gesture.ERROR,
                FeatureGesture.getGesture(s));
    }

    @Test
    public void testSampleActivityId(){
        byte activityId = 0x02;
        Feature.Sample s = new Feature.Sample(100,new Number[]{activityId});
        Assert.assertEquals(FeatureGesture.Gesture.LEFT,
                FeatureGesture.getGesture(s));
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureGesture(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureGesture(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3, 4, 5, 6,7}, 7);
    }


    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeatureGesture(null);
        int offset = 0;
        byte data[] = new byte[offset+7];

        data[offset]=0x00;

        int readByte = UpdateFeatureUtil.callUpdate(f, 2, data, offset);

        Assert.assertEquals(1, readByte);
        Assert.assertEquals(2, f.getSample().timestamp);
        Assert.assertEquals(FeatureGesture.Gesture.UNKNOWN,
                FeatureGesture.getGesture(f.getSample()));

    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeatureActivity(null);
        int offset = 0;
        byte data[] = new byte[offset+7];

        data[offset]=0x01;

        int readByte = UpdateFeatureUtil.callUpdate(f, 2, data, offset);

        Assert.assertEquals(1, readByte);
        Assert.assertEquals(2, f.getSample().timestamp);
        Assert.assertEquals(FeatureGesture.Gesture.TAP,
                FeatureGesture.getGesture(f.getSample()));

    }

    @Test
    public void testStatusValue(){

        Feature.Sample s = new Feature.Sample(100,new Number[]{0,0,0,0x00});
        Assert.assertEquals(FeatureGesture.Gesture.UNKNOWN,
                FeatureGesture.getGesture(s));

        s = new Feature.Sample(100,new Number[]{0x01});
        Assert.assertEquals(FeatureGesture.Gesture.TAP,
                FeatureGesture.getGesture(s));

        s = new Feature.Sample(100,new Number[]{0x02});
        Assert.assertEquals(FeatureGesture.Gesture.LEFT,
                FeatureGesture.getGesture(s));

        s = new Feature.Sample(100,new Number[]{0x03});
        Assert.assertEquals(FeatureGesture.Gesture.RIGHT,
                FeatureGesture.getGesture(s));


        //other value are errors
        s = new Feature.Sample(100,new Number[]{0xFF});
        Assert.assertEquals(FeatureGesture.Gesture.ERROR,
                FeatureGesture.getGesture(s));

    }
    
}
