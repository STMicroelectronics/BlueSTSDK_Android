package com.st.BlueSTSDK.Features;


import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent.AccelerationEvent;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.TestUtil.TestUtil;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestAccEventFeature {

    public class FeatureAccelerationEventTestable extends FeatureAccelerationEvent{
        int timestamp=0;
        public FeatureAccelerationEventTestable(){
            super(null);
        }

        public boolean sendCommand(byte commandType,@NonNull byte[] data){
            parseCommandResponse(timestamp++,commandType,data);
            return true;
        }
    }

    @Test
    public void testNullSampleAccEvent(){
        Assert.assertEquals(AccelerationEvent.ERROR, FeatureAccelerationEvent
                .getAccelerationEvent(null));
    }

    @Test
    public void testInvalidSampleAccEvent(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(AccelerationEvent.ERROR, FeatureAccelerationEvent
                .getAccelerationEvent(s));
    }

    @Test
    public void testGetSampleAccEvent(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0x01}, new Field[]{});
        Assert.assertEquals(AccelerationEvent.ORIENTATION_TOP_RIGHT,
                FeatureAccelerationEvent.getAccelerationEvent(s));
    }

    @Test
    public void testNullSamplePedometerSteps(){
        Assert.assertTrue(FeatureAccelerationEvent.getPedometerSteps(null) < 0);
    }

    @Test
    public void testInvalidSamplePedometerSteps(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{1}, new Field[]{});
        Assert.assertTrue(FeatureAccelerationEvent.getPedometerSteps(null) < 0);
    }

    @Test
    public void testGetSamplePedometerSteps(){
        int nStep = 1234;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0x100,nStep}, new Field[]{});
        Assert.assertEquals(AccelerationEvent.PEDOMETER,
                FeatureAccelerationEvent.getAccelerationEvent(s));
        Assert.assertEquals(nStep,
                FeatureAccelerationEvent.getPedometerSteps(s));
    }

    @Test
    public void defaultNoEventAreEnabled(){
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        Assert.assertEquals(FeatureAccelerationEvent.DetectableEvent.NONE, f.getEnabledEvent());
    }

    @Test
    public void setNoEventWhenDisableTheCurrentEvent(){
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.DOUBLE_TAP, true);
        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.DOUBLE_TAP, false);
        Assert.assertEquals(FeatureAccelerationEvent.DetectableEvent.NONE, f.getEnabledEvent());
    }

    @Test
    public void setRightEventWhenEnabled(){
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.DOUBLE_TAP, true);
        Assert.assertEquals(FeatureAccelerationEvent.DetectableEvent.DOUBLE_TAP,f.getEnabledEvent());
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureAccelerationEvent(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureAccelerationEvent(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1}, 1);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithPedometerInvalidSize() throws Throwable {
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER,true);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithPedometerInvalidSizeWithOffset() throws Throwable {
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER,true);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1,2}, 1);
    }


    @Test
    public void enableEventHasCallback(){
        FeatureAccelerationEvent.FeatureAccelerationEventListener listener =
                mock(FeatureAccelerationEvent.FeatureAccelerationEventListener.class);
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.addFeatureListener(listener);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);
        //TestUtil.execAllAsyncTask();

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER, false);

        verify(listener,timeout(200)).onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent
                .PEDOMETER, false);
    }

    @Test
    public void enableANewEventDisableThePreviousOne(){
        FeatureAccelerationEvent.FeatureAccelerationEventListener listener =
                mock(FeatureAccelerationEvent.FeatureAccelerationEventListener.class);
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.addFeatureListener(listener);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.DOUBLE_TAP, true);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER,
                        false);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.DOUBLE_TAP,
                        true);

    }

    @Test
    public void enableNoneDisableThePreviousOne(){
        FeatureAccelerationEvent.FeatureAccelerationEventListener listener =
                mock(FeatureAccelerationEvent.FeatureAccelerationEventListener.class);
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.addFeatureListener(listener);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.NONE, true);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER,
                        false);

    }
}
