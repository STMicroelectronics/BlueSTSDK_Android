package com.st.BlueSTSDK.Utils.advertise;

import android.support.annotation.Nullable;

public interface AdvertiseFilter {
    @Nullable
    BleAdvertiseInfo filter(byte[] advData);
}
