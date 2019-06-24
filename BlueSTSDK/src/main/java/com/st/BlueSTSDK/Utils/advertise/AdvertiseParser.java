package com.st.BlueSTSDK.Utils.advertise;

import android.util.SparseArray;

public class AdvertiseParser {

    public final static byte FLAG_DATA_TYPE = 0x01;
    public final static byte INCOMPLETE_LIST_OF_128_UUID = 0x06;
    public final static byte DEVICE_NAME_TYPE = 0x09;
    public final static byte TX_POWER_TYPE = 0x0A;
    public final static byte VENDOR_DATA_TYPE = (byte) 0xff;

    public static SparseArray<byte[]> split(byte advertise[]){
        SparseArray<byte[]> splitAdvertise = new SparseArray<>();
        int ptr = 0;
        while (ptr < advertise.length - 2) {
            int length = advertise[ptr++] & 0xff;
            if (length == 0)
                break;

            final byte type = (advertise[ptr++]);
            //min between the length field and the remaining array length
            final int fieldLength = Math.min(length-1,advertise.length-ptr);

            byte data[] = new byte[fieldLength];
            System.arraycopy(advertise,ptr,data,0,fieldLength);
            splitAdvertise.put(type,data);

            ptr += fieldLength;
        }
        return splitAdvertise;
    }

}
