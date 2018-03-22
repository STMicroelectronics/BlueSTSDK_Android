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


import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

/**
 * Extract the data form an advertise used by a device that follow the BlueST protocol.
 * It will throw an exception if the advertise is not valid
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class BleAdvertiseParser {

    private final static int VENDOR_DATA_TYPE = 0xff;
    private final static int DEVICE_NAME_TYPE = 0x09;
    private final static int TX_POWER_TYPE = 0x0A;

    private final static int VERSION_PROTOCOL_SUPPORTED_MIN = 0x01;
    private final static int VERSION_PROTOCOL_SUPPORTED_MAX = 0x01;

    /**
     * device name
     */
    private String mName;
    /**
     * device tx power
     */
    private byte mTxPower;
    /**
     * device mac address
     */
    private String mAddress;

    /**
     * bit map that tell us the available features
     */
    private int mFeatureMap;
    /**
     * device id
     */
    private byte mDeviceId;
    /**
     *  Device ProtocolVersion (it is an unsigned char )
     */
    private short mProtocolVersion;

    /**
     * board type -> is a super class of the device id
     */
    private Node.Type mBoardType;

    /**
     * board is in sleeping state
     */
    private boolean mBoardSleeping;

    /**
     * board has general purpose info
     */
    private boolean mHasGeneralPurpose;

    /**
     * parse the advertise data
     *
     * @param advertise ble advertise data
     * @throws InvalidBleAdvertiseFormat throw if the advertise doesn't respect the BlueST format
     */
    public BleAdvertiseParser(byte advertise[]) throws InvalidBleAdvertiseFormat {

        mAddress = null;

        if(advertise.length<7){
            throw  new InvalidBleAdvertiseFormat("Vendor data is mandatory," +
                    "this advertise has not enough byte for contain it");
        }

        boolean parsedVendorData=false;
        int ptr = 0;
        /* advertise format: length|type|data -> we can skip the last 2 byte since the contains
        data for secure */
        while (ptr < advertise.length - 2) {
            int length = advertise[ptr++] & 0xff;
            if (length == 0)
                break;

            final int type = (advertise[ptr++] & 0xff);

            switch (type) {
                case TX_POWER_TYPE:
                    mTxPower = advertise[ptr];
                    break;
                case DEVICE_NAME_TYPE:
                    mName = new String(advertise, ptr, length - 1);
                    break;
                case VENDOR_DATA_TYPE:
                    parsedVendorData=true;
                    parseVendorField(advertise, ptr, length);
                    break;
            }
            ptr += (length - 1);
        }
        if(!parsedVendorData){
            throw  new InvalidBleAdvertiseFormat("Vendor data is mandatory," +
                    "this advertise does not have it");
        }
    }

    /**
     * parse the node type field
     *
     * @param nodeType node type field
     * @return board type
     * @throws InvalidBleAdvertiseFormat if is not a know board type
     */
    private static Node.Type getNodeType(byte nodeType) throws InvalidBleAdvertiseFormat {
        short temp = (short) (nodeType & 0xFF);
        if (temp == 0x01)
            return Node.Type.STEVAL_WESU1;
        if(temp == 0x02)
            return Node.Type.SENSOR_TILE;
        if(temp == 0x03)
            return Node.Type.BLUE_COIN;
        if(temp == 0x04)
            return Node.Type.STEVAL_IDB008VX;
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
    private static boolean getNodeSleepingState(byte nodeType) {
        return((nodeType & 0x80) != 0x80 && ((nodeType & 0x40) == 0x40));
    }

    /**
     * parse the node type field to check if board has generic purpose implemented
     *
     * @param nodeType node type field
     * @return boolean false if the device has Generic purpose servicess and char
     */
    private static boolean getHasGenericPurposeFeature(byte nodeType) {
        return((nodeType & 0x80) != 0x80 && ((nodeType & 0x20) == 0x20));
    }

    /**
     * parse the vendor specific data filed
     *
     * @param advertise   ble advertise data
     * @param startOffset offset where the vendor specific filed start
     * @param length      length of the vendor specific field
     * @throws InvalidBleAdvertiseFormat throw it the length is different from the expected ones
     */
    private void parseVendorField(byte advertise[], int startOffset,
                                  int length) throws InvalidBleAdvertiseFormat {
        if ((length != 7 ) && (length != 13 )) {
            throw new InvalidBleAdvertiseFormat("Vendor Specific field must be of length 7  or 13 (not " +
                    "" + length +")");
        }// if length


        mProtocolVersion = NumberConversion.byteToUInt8(advertise,startOffset);
        if ((mProtocolVersion < VERSION_PROTOCOL_SUPPORTED_MIN) ||(mProtocolVersion > VERSION_PROTOCOL_SUPPORTED_MAX)) {
            throw new InvalidBleAdvertiseFormat("Protocol version "+ mProtocolVersion + " Unsupported. Version must be ["
                    +VERSION_PROTOCOL_SUPPORTED_MIN + ", " + VERSION_PROTOCOL_SUPPORTED_MAX + "]");
        }

        mDeviceId = (byte)(((advertise[startOffset+1] & 0x80) == 0x80) ? (advertise[startOffset+1] & 0xFF) : (advertise[startOffset+1] & 0x1F));
        mBoardType = getNodeType(mDeviceId);
        mBoardSleeping = getNodeSleepingState(advertise[startOffset+1]);
        mHasGeneralPurpose = getHasGenericPurposeFeature(advertise[startOffset+1]);
        mFeatureMap = NumberConversion.BigEndian.bytesToInt32(advertise, startOffset + 2);

        if ((length == 13 )) {
            mAddress = String.format("%02X:%02X:%02X:%02X:%02X:%02X", advertise[startOffset + 6],
                    advertise[startOffset + 7], advertise[startOffset + 8],
                    advertise[startOffset + 9], advertise[startOffset + 10],
                    advertise[startOffset + 11]);
        }
    }//parseVendorField

    /**
     * get the device name
     *
     * @return device name
     */
    public String getName() {
        return mName;
    }

    /**
     * get the device tx power in mdb
     *
     * @return tx power in mdb
     */
    public byte getTxPower() {
        return mTxPower;
    }

    /**
     * get the device mac address
     *
     * @return mac address
     */
    public String getAddress() {
        return mAddress;
    }

    /**
     * get the device protocol version
     *
     * @return unsigned char with the protocol version
     */
    public short getProtocolVersion() {
        return mProtocolVersion;
    }

    /**
     * get the board type
     *
     * @return board type
     */
    public Node.Type getBoardType() {
        return mBoardType;
    }

    /**
     * get the sleeping
     *
     * @return board Sleeping state
     */
    public boolean getBoardSleeping() {
        return mBoardSleeping;
    }
    /**
     * general purpose available state
     *
     * @return return if the general purpose is available
     */
    public boolean getBoardHasGP() {
        return mHasGeneralPurpose;
    }
    /**
     * get the raw device id data filed
     *
     * @return device id
     */
    public byte getDeviceId() {
        return mDeviceId;
    }

    /**
     * get the bitmap that describe the available feature in this node
     *
     * @return feature bitmap
     */
    public int getFeatureMap() {
        return mFeatureMap;
    }

    /**
     * print the advertise data
     * @return string that contains the advertise data
     */
    @Override
    public String toString() {
        return "Name: "+mName+
                "\n\tTxPower: "+mTxPower+
                "\n\tAddress: "+mAddress+
                "\n\tFeature Mask: 0x"+String.format("%X", mFeatureMap)+
                "\n\tProtocol Version: 0x"+mProtocolVersion;
    }

}
