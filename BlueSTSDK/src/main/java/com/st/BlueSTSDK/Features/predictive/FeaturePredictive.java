package com.st.BlueSTSDK.Features.predictive;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

public abstract class FeaturePredictive extends Feature {
    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param name     name of the feature
     * @param n        node that will update this feature
     * @param dataDesc description of the data that belong to this feature
     */
    public FeaturePredictive(String name, Node n, @NonNull Field[] dataDesc) {
        super(name, n, dataDesc);
    }


    public enum Status {
        GOOD,
        WARNING,
        BAD,
        UNKNOWN;

        static Status fromByte(byte value){
            switch (value){
                case 0x00:
                    return GOOD;
                case 0x01:
                    return WARNING;
                case 0x02:
                    return BAD;
                default:
                    return UNKNOWN;
            }
        }
    }

    protected static Status getStatusFromIndex(Sample s, int i){
        if(hasValidIndex(s,i)){
            return Status.fromByte(s.data[i].byteValue());
        }
        return Status.UNKNOWN;
    }

    protected static byte extractXRawStatus(short value){
        return (byte)((value >> 4) & 0x03);
    }

    protected static byte extractYRawStatus(short value){
        return (byte)((value >> 2) & 0x03);
    }

    protected static byte extractZRawStatus(short value){
        return (byte)((value ) & 0x03);
    }

    protected static Field buildStatusFieldNamed(String name){
        return new Field(name,null, Field.Type.UInt8,4,0);
    }
}
