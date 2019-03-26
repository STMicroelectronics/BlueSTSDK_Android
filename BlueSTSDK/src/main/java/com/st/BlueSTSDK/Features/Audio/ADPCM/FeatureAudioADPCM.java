/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.Features.Audio.ADPCM;

import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Features.Audio.FeatureAudio;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

/**
 * Feature that contains the compressed audio data acquired form a microphone.
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureAudioADPCM extends FeatureAudio {

    public static final String FEATURE_NAME = "Audio";
    public static final String FEATURE_DATA_NAME = "ADPCM";

    /**
     * number of sample received for each feature notification
     */
    public static final int AUDIO_PACKAGE_SIZE=40;

    private ADPCMEngine adpcmEngine = new ADPCMEngine();
    private ADPCMManager mBVBvAudioSyncManager =null;

    protected static final Field AUDIO_FIELD = new Field(FEATURE_DATA_NAME,null, Field.Type.ByteArray,-128,127);

    /**
     * build an Audio ADPCM Feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureAudioADPCM(Node n) {
        super(FEATURE_NAME, n, new Field[]{ AUDIO_FIELD });
    }

    protected FeatureAudioADPCM(String name, Node n, Field data[]) {
        super(name,n,data);
        if(data[0]!=AUDIO_FIELD){
            throw new IllegalArgumentException("First data[0] must be FeatureAudioADPCM" +
                    ".AUDIO_FIELD");
        }//if
    }

    public short[] getAudio(Sample sample) {

        if (sample != null && sample.data!=null){
            int length = sample.data.length;
            short[] audioPckt = new short[length];
            getAudio(sample,audioPckt);
            return audioPckt;
        }
        //else
        return new short[]{};
    }

    /**
     * extract the audio sample from a feature sample, this function is useful for avoid to allocate
     * a new array at each sample
     * @param sample ble sample
     * @param outData array where store the audio sample
     * @return true if the sample is a valid sample
     */
    public static boolean getAudio(Sample sample, short outData[]){
        if (sample != null && sample.data!=null){
            int length = Math.min(sample.data.length,outData.length);
            for(int i = 0 ; i < length ; i++){
                if (sample.data[i] != null)
                    outData[i] = sample.data[i].shortValue();
            }
            return true;
        }
        return false;
    }


    /**
     * set the object synchronization parameters necessary to the decompression process
     * @param manager struct which contains the synchronization parameters
     */
    public void setAudioCodecManager(AudioCodecManager manager){
        mBVBvAudioSyncManager = ((ADPCMManager)manager);
    }

    /**
     * extract the audio data from the node raw data, in this case it read an array of 40 shorts.
     *
     * @param data       array where read the Field data (a 20 bytes array)
     * @param dataOffset offset where start to read the data (0 by default)
     * @return number of read bytes (20) and data extracted (the audio information, the 40 shorts array)
     * @throws IllegalArgumentException if the data array has not the correct number of elements
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if(data.length == 20){
            Number[] dataPkt = new Number[AUDIO_PACKAGE_SIZE];
            for (int i=0; i<AUDIO_PACKAGE_SIZE/2; i++) {
                dataPkt[2*i] = adpcmEngine.decode((byte)(data[i] & 0x0F), mBVBvAudioSyncManager);
                dataPkt[(2*i)+1] = adpcmEngine.decode((byte)((data[i] >> 4) & 0x0F), mBVBvAudioSyncManager);
            }
            Sample audioData = new Sample(dataPkt,getFieldsDesc());
            return new ExtractResult(audioData,20);
        }
        else{
            throw new IllegalArgumentException("There are no 20 bytes available to read");
        }
    }//update

    /**
     * ADPCM Engine class. It contains all the operations and parameters necessary to decompress the
     * audio received.
     */
    private static class ADPCMEngine {

        /** Quantizer step size lookup table */
        private static final short[] StepSizeTable={7,8,9,10,11,12,13,14,16,17,
                19,21,23,25,28,31,34,37,41,45,
                50,55,60,66,73,80,88,97,107,118,
                130,143,157,173,190,209,230,253,279,307,
                337,371,408,449,494,544,598,658,724,796,
                876,963,1060,1166,1282,1411,1552,1707,1878,2066,
                2272,2499,2749,3024,3327,3660,4026,4428,4871,5358,
                5894,6484,7132,7845,8630,9493,10442,11487,12635,13899,
                15289,16818,18500,20350,22385,24623,27086,29794,32767};

        /** Table of index changes */
        private static final byte[] IndexTable = {-1,-1,-1,-1,2,4,6,8,-1,-1,-1,-1,2,4,6,8};

        private short index;
        private int predsample;

        /**
         * Default Constructor
         */
        public ADPCMEngine() {
            this.index = 0;
            this.predsample = 0;
        }

        /**
         * ADPCM_Decode.
         * @param code: a byte containing a 4-bit ADPCM sample.
         * @return : a struct which contains a 16-bit ADPCM sample
         */
        public short decode(byte code,@Nullable ADPCMManager syncManager) {
            short step;
            int diffq;

            if(syncManager!=null && syncManager.isIntra()) {
                predsample = syncManager.getAdpcm_predsample_in();
                index = syncManager.getAdpcm_index_in();
                syncManager.reinit();
            }
            step = StepSizeTable[index];

            /* 2. inverse code into diff */
            diffq = step>> 3;
            if ((code&4)!=0)
            {
                diffq += step;
            }

            if ((code&2)!=0)
            {
                diffq += step>>1;
            }

            if ((code&1)!=0)
            {
                diffq += step>>2;
            }

            /* 3. add diff to predicted sample*/
            if ((code&8)!=0)
            {
                predsample -= diffq;
            }
            else
            {
                predsample += diffq;
            }

            /* check for overflow*/
            if (predsample > 32767)
            {
                predsample = 32767;
            }
            else if (predsample < -32768)
            {
                predsample = -32768;
            }

            /* 4. find new quantizer step size */
            index += IndexTable [code];
            /* check for overflow*/
            if (index < 0)
            {
                index = 0;
            }
            if (index > 88)
            {
                index = 88;
            }

            /* 5. save predict sample and index for next iteration */
            /* done! static variables */

            /* 6. return new speech sample*/
            return (short)predsample;
        }
    }
}
