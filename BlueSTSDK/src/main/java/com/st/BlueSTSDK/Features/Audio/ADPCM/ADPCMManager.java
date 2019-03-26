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

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;

/**
 * Class containing the sync data needed in a ADPCM stream decoding
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class ADPCMManager implements AudioCodecManager {

    private static final String CODEC_NAME = "ADPCM";
    private static final int SAMPLING_FREQ = 8000;
    private static final short CHANNELS = 1;

    /** index where you can find adpcm index value/description */
    public static final int ADPCM_INDEX_INDEX = 0;
    /** index where you can find adpcm predsample value/description*/
    public static final int ADPCM_PREDSAMPLE_INDEX = 1;

    private boolean intra_flag=false;
    private short adpcm_index_in=0;
    private int adpcm_predsample_in=0;

    public boolean isIntra() {
        return intra_flag;
    }

    public short getAdpcm_index_in() {
        return adpcm_index_in;
    }

    public int getAdpcm_predsample_in() {
        return adpcm_predsample_in;
    }

    @Override
    public void reinit(){
        intra_flag = false;
    }

    @Override
    public String getCodecName() {
        return CODEC_NAME;
    }

    @Override
    public int getSamplingFreq() {
        return SAMPLING_FREQ;
    }

    @Override
    public short getChannels() {
        return CHANNELS;
    }

    @Override
    public boolean isAudioEnabled() {
        return true;
    }

    @Override
    public void updateParams(Feature.Sample sample){
        synchronized (this) {
            adpcm_index_in = FeatureAudioADPCMSync.getIndex(sample);
            adpcm_predsample_in = FeatureAudioADPCMSync.getPredictedSample(sample);
            intra_flag = true;
        }
    }
}
