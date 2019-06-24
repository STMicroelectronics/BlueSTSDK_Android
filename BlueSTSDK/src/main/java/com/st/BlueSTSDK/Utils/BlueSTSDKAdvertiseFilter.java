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
package com.st.BlueSTSDK.Utils;


import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter;
import com.st.BlueSTSDK.Utils.advertise.BlueSTSDKAdvertiseInfo;

import static com.st.BlueSTSDK.Utils.advertise.AdvertiseParser.DEVICE_NAME_TYPE;
import static com.st.BlueSTSDK.Utils.advertise.AdvertiseParser.TX_POWER_TYPE;
import static com.st.BlueSTSDK.Utils.advertise.AdvertiseParser.VENDOR_DATA_TYPE;
import static com.st.BlueSTSDK.Utils.advertise.AdvertiseParser.split;

/**
 * Extract the data form an advertise used by a device that follow the BlueST protocol.
 * It will throw an exception if the advertise is not valid
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class BlueSTSDKAdvertiseFilter implements AdvertiseFilter {

    private final static int VERSION_PROTOCOL_SUPPORTED_MIN = 0x01;
    private final static int VERSION_PROTOCOL_SUPPORTED_MAX = 0x01;

    @Nullable
    @Override
    public BlueSTSDKAdvertiseInfo filter(byte[] advData) {
        SparseArray<byte[]> splitAdv = split(advData);
        byte data[]  = splitAdv.get(TX_POWER_TYPE);
        byte txPower;
        if(data!=null){
            txPower = data[0];
        }else{
            txPower = 0;
        }
        String name;
        data = splitAdv.get(DEVICE_NAME_TYPE);
        if(data!=null){
            name = new String(data);
        }else{
            name = null;
        }

        data = splitAdv.get(VENDOR_DATA_TYPE);
        if(data!=null){

            if ((data.length != 6 ) && (data.length != 12 )) {
                return null;
            }// if length

            short protocolVersion = NumberConversion.byteToUInt8(data,0);
            if ((protocolVersion < VERSION_PROTOCOL_SUPPORTED_MIN) ||(protocolVersion > VERSION_PROTOCOL_SUPPORTED_MAX)) {
                return null;
            }

            byte deviceId = (byte)(((data[1] & 0x80) == 0x80) ? (data[1] & 0xFF) : (data[1] & 0x1F));
            Node.Type boardType = getNodeType(deviceId);
            boolean boardSleeping = getNodeSleepingState(data[1]);
            boolean hasGeneralPurpose = getHasGenericPurposeFeature(data[1]);
            int featureMap = NumberConversion.BigEndian.bytesToInt32(data, + 2);

            String address = null;
            if ((data.length == 12 )) {
                address = String.format("%02X:%02X:%02X:%02X:%02X:%02X", data[+ 6],
                        data[7], data[ 8],
                        data[9], data[ 10],
                        data[11]);
            }
            return new BlueSTSDKAdvertiseInfo(name,txPower, address, featureMap, deviceId, protocolVersion, boardType, boardSleeping, hasGeneralPurpose);
        }else{
            return null;
        }
    }

    /**
     * parse the node type field
     *
     * @param nodeType node type field
     * @return board type
     */
    private Node.Type getNodeType(byte nodeType) {

        short temp = (short) (nodeType & 0xFF);
        if (temp == 0x01)
            return Node.Type.STEVAL_WESU1;
        if(temp == 0x02)
            return Node.Type.SENSOR_TILE;
        if(temp == 0x03)
            return Node.Type.BLUE_COIN;
        if(temp == 0x04)
            return Node.Type.STEVAL_IDB008VX;
        if(temp == 0x05)
            return Node.Type.STEVAL_BCN002V1;
        if(temp == 0x06)
            return Node.Type.SENSOR_TILE_BOX;
        if(temp == 0x07)
            return Node.Type.DISCOVERY_IOT01A;
        if (temp >= 0x80 && temp <= 0xff)
            return Node.Type.NUCLEO;
        else // 0 or user defined
            return Node.Type.GENERIC;

    }

    /**
     * parse the node type field to check if board is sleeping
     *
     * @param nodeType node type field
     * @return boolean false running true is sleeping
     */
    private boolean getNodeSleepingState(byte nodeType) {
        return((nodeType & 0x80) != 0x80 && ((nodeType & 0x40) == 0x40));
    }

    /**
     * parse the node type field to check if board has generic purpose implemented
     *
     * @param nodeType node type field
     * @return boolean false if the device has Generic purpose servicess and char
     */
    private boolean getHasGenericPurposeFeature(byte nodeType) {
        return((nodeType & 0x80) != 0x80 && ((nodeType & 0x20) == 0x20));
    }

}
