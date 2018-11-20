package com.st.BlueSTSDK.Features;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeatureFFTAmplitude extends DeviceTimestampFeature {
    public static final String FEATURE_NAME = "FFT Amplitude";
    /**
     * build a proximity feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureFFTAmplitude(Node n) {
        super(FEATURE_NAME, n , new Field[]{
                new Field("ReceiveStatus","%",Field.Type.UInt8,0,100),
                new Field("N Sample",null,Field.Type.UInt16,0,(1<<16)-1),
                new Field("N Components",null,Field.Type.UInt8,0,(1<<8)-1),
                new Field("Frequency Step","Hz",Field.Type.Float,0,Float.MAX_VALUE),
                new Field("AmptiudeX",null,Field.Type.ByteArray,0,Float.MAX_VALUE),
                new Field("AmptiudeY",null,Field.Type.ByteArray,0,Float.MAX_VALUE),
                new Field("AmptiudeZ",null,Field.Type.ByteArray,0,Float.MAX_VALUE),
        });
    }//FeatureProximity

    public static boolean isComplete(Sample s){
        if (s instanceof  FFTSample){
            return ((FFTSample) s).isComplete();
        }
        return false;
    }

    public static int getDataLoadPercentage(Sample s){
        if (s instanceof  FFTSample){
            return ((FFTSample) s).getDataLoadPercentage();
        }
        return -1;
    }

    public static int getNSample(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).nSample;
        return 0;
    }

    public static int getNComponents(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).nComponents;
        return 0;
    }

    public static float getFreqStep(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).freqStep;
        return 0;
    }

    public static float[] getXComponent(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).getComponent(0);
        return new float[0];
    }

    public static float[] getYComponent(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).getComponent(1);
        return new float[0];
    }

    public static float[] getZComponent(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).getComponent(2);
        return new float[0];
    }

    public static float[] getComponent(Sample s, int index){
        if(s instanceof FFTSample)
            return ((FFTSample) s).getComponent(index);
        return new float[0];
    }


    private static class FFTSample extends Sample{

        public final int nSample;
        public final short nComponents;
        public final float freqStep;

        private byte[] rawData;
        private int nLastData;

        FFTSample(long timestamp, @NonNull Field[] dataDesc,int nSample, short nComponents, float freqStep ) {
            super(timestamp, new Number[0], dataDesc);
            this.nSample = nSample;
            this.nComponents = nComponents;
            this.freqStep = freqStep;
            rawData = new byte[nSample*nComponents*4]; // components * 4 byte each float
            nLastData =0;
        }

        void appendData(byte[] data , int offset){
            int dataToCopy = data.length - offset;
            System.arraycopy(data,offset,rawData,nLastData,dataToCopy);
            nLastData += dataToCopy;
        }

        int getDataLoadPercentage(){
            if(rawData.length == 0)
                return 0;
            return (nLastData*100)/rawData.length;
        }

        boolean isComplete(){
            return nLastData == rawData.length;
        }

        private static float[] extractFloat(byte rawData[],int startOffset, int nFloat){
            float[] out = new float[nFloat];
            for (int i = 0 ; i<nFloat ; i++){
                out[i] = NumberConversion.LittleEndian.bytesToFloat(rawData,startOffset+4*i);
            }
            return out;
        }

        float[] getComponent(int index){
            if(index>=nComponents)
                throw new IllegalArgumentException("Max component is "+nComponents);
            int startOffset = index * nSample *4;
            return extractFloat(rawData,startOffset,nSample);
        }


    }

    private FFTSample readHeaderData(long timestamp, byte[] data, int dataOffset){
        if(data.length-dataOffset < 7){
            throw new IllegalArgumentException("There are no 7 bytes available to read");
        }

        int nSample =  NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset);
        short nComponents =  NumberConversion.byteToUInt8(data, dataOffset+2);
        float freqStep = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset+3);

        FFTSample sample = new FFTSample(timestamp,getFieldsDesc(),nSample,nComponents,freqStep);

        sample.appendData(data,dataOffset+7);

        return sample;
    }


    private FFTSample mPartialSample;

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        Sample returnSample;
        if(mPartialSample == null){
            mPartialSample = readHeaderData(timestamp,data,dataOffset);
            returnSample = mPartialSample;
        }else{
            mPartialSample.appendData(data,dataOffset);
            returnSample = mPartialSample;
            if(mPartialSample.isComplete()){
                mPartialSample = null;
            }
        }
        return new ExtractResult(returnSample,data.length);
    }
}
