package com.st.BlueSTSDK.Utils.advertise;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

import java.util.List;

public class LeScanCallback implements BluetoothAdapter.LeScanCallback {

    private Manager mBleManager;
    private List<AdvertiseFilter> mAdvFilters;


    public LeScanCallback(Manager bleManager, List<AdvertiseFilter> advFilters) {
        this.mBleManager = bleManager;
        this.mAdvFilters = advFilters;
    }

    private @Nullable
    BleAdvertiseInfo matchAdvertise(byte[] advertise){
        for (AdvertiseFilter filter : mAdvFilters){
            BleAdvertiseInfo res = filter.filter(advertise);
            if (res!=null)
                return res;
        }
        return null;
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] advetiseData) {
        BleAdvertiseInfo info = matchAdvertise(advetiseData);
        if(info == null){
            return;
        } // else
        Node node = new Node(bluetoothDevice,rssi,info);
        mBleManager.addNode(node);

    }
}
