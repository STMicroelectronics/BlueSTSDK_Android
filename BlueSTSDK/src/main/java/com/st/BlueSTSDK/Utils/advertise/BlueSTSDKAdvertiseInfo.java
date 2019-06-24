package com.st.BlueSTSDK.Utils.advertise;

import com.st.BlueSTSDK.Node.Type;

public class BlueSTSDKAdvertiseInfo implements BleAdvertiseInfo {

    /**
     * device name
     */
    private final String mName;
    /**
     * device tx power
     */
    private final byte mTxPower;
    /**
     * device mac address
     */
    private final String mAddress;

    /**
     * bit map that tell us the available features
     */
    private final int mFeatureMap;
    /**
     * device id
     */
    private final byte mDeviceId;
    /**
     * Device ProtocolVersion (it is an unsigned char )
     */
    private final short mProtocolVersion;

    /**
     * board type -> is a super class of the device id
     */
    private final Type mBoardType;


    /**
     * board is in sleeping state
     */
    private final boolean mBoardSleeping;

    /**
     * board has general purpose info
     */
    private final boolean mHasGeneralPurpose;

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public byte getTxPower() {
        return mTxPower;
    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public int getFeatureMap() {
        return mFeatureMap;
    }

    @Override
    public byte getDeviceId() {
        return mDeviceId;
    }

    @Override
    public short getProtocolVersion() {
        return mProtocolVersion;
    }

    @Override
    public Type getBoardType() {
        return mBoardType;
    }

    @Override
    public boolean isBoardSleeping() {
        return mBoardSleeping;
    }

    @Override
    public boolean isHasGeneralPurpose() {
        return mHasGeneralPurpose;
    }

    public BlueSTSDKAdvertiseInfo(String mName, byte mTxPower, String mAddress, int mFeatureMap, byte mDeviceId, short mProtocolVersion, Type mBoardType, boolean mBoardSleeping, boolean mHasGeneralPurpose) {
        this.mName = mName;
        this.mTxPower = mTxPower;
        this.mAddress = mAddress;
        this.mFeatureMap = mFeatureMap;
        this.mDeviceId = mDeviceId;
        this.mProtocolVersion = mProtocolVersion;
        this.mBoardType = mBoardType;
        this.mBoardSleeping = mBoardSleeping;
        this.mHasGeneralPurpose = mHasGeneralPurpose;
    }


}
