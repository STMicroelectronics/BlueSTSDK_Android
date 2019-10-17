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
package com.st.BlueSTSDK.Features.Audio.Opus;

import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Feature.Sample;
import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;

import java.util.Arrays;

/**
 * Class containing data needed in a Opus stream decoding
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class OpusManager implements AudioCodecManager {

    private static final String CODEC_NAME = "Opus";

    /* Opus NDK library loading */
    static {
        System.loadLibrary("opusUser");
    }

    private boolean isPlaying = false;

    /* Opus NDK functions */
    /** Opus Decoder Initialization function declaration*/
    private native int OpusDecInit(int sampFreq, int channels);
    /** Opus Decoder Decoding function declaration*/
    private native byte[] OpusDecode(byte[] input, int in_length, int frameSizePcm);

    /** Opus Decoder parameters */
    private float opusFrameSize;
    private int opusSamplingFreq;
    private short opusChannels;
    private int opusFrameSizePCM;

    private byte[] opusCoded;
    private int opusCodedLen = 0;
    private short[] opusOutputPCM;
    private boolean opusDecStart = false;

    /** Constructor. It loads default value statically from the Opus Configuration Feature */
    public OpusManager() {
        this.opusFrameSize = FeatureAudioOpusConf.getDefaultFrameSize();
        this.opusSamplingFreq = FeatureAudioOpusConf.getDefaultSamplingFreq();
        this.opusChannels = FeatureAudioOpusConf.getDefaultChannels();
        this.opusFrameSizePCM = FeatureAudioOpusConf.getDefaultFrameSizePCM();
        this.opusCoded = new byte[opusFrameSizePCM*2];
        this.opusOutputPCM = new short[opusFrameSizePCM];
    }

    @Override
    public void reinit() {
    }

    @Override
    public String getCodecName() {
        return CODEC_NAME;
    }

    @Override
    public int getSamplingFreq() {
        return opusSamplingFreq;
    }

    @Override
    public short getChannels() {
        return opusChannels;
    }

    @Override
    public boolean isAudioEnabled() {
        return isPlaying;
    }

    @Override
    public void updateParams(Sample sample){
        if (sample.data[0].byteValue()==FeatureAudioOpusConf.BV_OPUS_CONF_CMD) {
            this.opusFrameSize = FeatureAudioOpusConf.getFrameSize(sample);
            this.opusSamplingFreq = FeatureAudioOpusConf.getSamplingFreq(sample);
            this.opusChannels = FeatureAudioOpusConf.getChannels(sample);
            this.opusFrameSizePCM = (int)(((opusSamplingFreq/1000)*opusFrameSize));
            this.opusCoded = new byte[opusFrameSizePCM*2];
            this.opusOutputPCM = new short[opusFrameSizePCM];
        }
        if (sample.data[0].byteValue()==FeatureAudioOpusConf.BV_OPUS_CONTROL) {
            if (sample.data[1].byteValue() == FeatureAudioOpusConf.BV_OPUS_ENABLE_NOTIF_REQ) {
                isPlaying=true;
            } else if (sample.data[1].byteValue() == FeatureAudioOpusConf.BV_OPUS_DISABLE_NOTIF_REQ) {
                isPlaying=false;
            }
        }
    }

    /** Transport protocol Audio Packet reconstruction and Decoding method */
    @Nullable
    public short[] getDecodedPckt(byte[] audioSample) {
        if(!opusDecStart) {
            if (audioSample[0] == 0)
            {
                OpusDecInit(opusSamplingFreq,opusChannels);
                opusDecStart = true;
                System.arraycopy(audioSample, 1, opusCoded, 0, audioSample.length-1);
                opusCodedLen += audioSample.length - 1;
            }
            return null;
        }else {
            if (audioSample[0] == 0) {
                Arrays.fill(opusCoded, (byte) 0);
                System.arraycopy(audioSample, 1, opusCoded, 0, audioSample.length - 1);
                opusCodedLen = opusCodedLen + audioSample.length - 1;
            } else if (audioSample[0] == 64) {
                System.arraycopy(audioSample, 1, opusCoded, opusCodedLen, audioSample.length - 1);
                opusCodedLen = opusCodedLen + audioSample.length - 1;
            } else if (audioSample[0] == -128) {
                System.arraycopy(audioSample, 1, opusCoded, opusCodedLen, audioSample.length - 1);
                opusCodedLen = opusCodedLen + audioSample.length - 1;

                //decode
                byte[] OPUSdecoded = OpusDecode(opusCoded, opusCodedLen, opusFrameSizePCM);

                for (int i = 0; i < (OPUSdecoded.length / 2); i++){
                    opusOutputPCM[i] = (short)(((OPUSdecoded[2 * i]&0xFF)) | (OPUSdecoded[2 * i + 1]&0xFF)<<8 );
                }

                opusCodedLen = 0;

                return opusOutputPCM;
            }
            return null;
        }
    }
}
