package com.st.BlueSTSDK.Utils.advertise;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Node;

public interface BleAdvertiseInfo {
    @NonNull String getName();

    byte getTxPower();

    @Nullable String getAddress();

    int getFeatureMap();

    byte getDeviceId();

    short getProtocolVersion();

    Node.Type getBoardType();

    boolean isBoardSleeping();

    boolean isHasGeneralPurpose();
}
