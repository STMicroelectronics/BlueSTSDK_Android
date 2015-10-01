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
package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

/**
 * Feature that will contain the battery data, this is not the standard battery since will
 * contain more information.
 *
 * The information exported by this class are:
 * <ul>
 *     <li>Battery Level, % of remaining charge with one decimal position</li>
 *     <li>Voltage, battery voltage, in Volt with 3 decimal position</li>
 *     <li>Current, electric current that is used by the board, with one decimal position in mA</li>
 *     <li>Status, tell if the battery is charging, discharging or in low battery </li>
 * </ul>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureBattery extends Feature {

    /** feature name */
    public static final String FEATURE_NAME = "Battery";
    /** unit of the data exported by this feature */
    public static final String[] FEATURE_UNIT = {"%", "V", "mA", ""};
    /** name of the data exported by this feature */
    public static final String[] FEATURE_DATA_NAME = {"Level", "Voltage", "Current", "Status"};
    /** maximum value for the feature data */
    public static final short[] DATA_MAX = {100, 10, 10, 0xFF};
    /** minimum value for the feature data */
    public static final short[] DATA_MIN = {0, -10, -10, 0};

    /** index where you can find the percentage value/description */
    public static final int PERCENTAGE_INDEX = 0;
    /** index where you can find the voltage value/description */
    public static final int VOLTAGE_INDEX = 1;
    /** index where you can find the current value/description */
    public static final int CURRENT_INDEX = 2;
    /** index where you can find the status value/description */
    public static final int STATUS_INDEX = 3;

    /**
     * create a feature Battery
     * @param n node where the feature will read the data
     */
    public FeatureBattery(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[PERCENTAGE_INDEX], FEATURE_UNIT[PERCENTAGE_INDEX],
                        Field.Type.Float, DATA_MAX[PERCENTAGE_INDEX], DATA_MIN[PERCENTAGE_INDEX]),
                new Field(FEATURE_DATA_NAME[VOLTAGE_INDEX], FEATURE_UNIT[VOLTAGE_INDEX],
                        Field.Type.Float, DATA_MAX[VOLTAGE_INDEX], DATA_MIN[VOLTAGE_INDEX]),
                new Field(FEATURE_DATA_NAME[CURRENT_INDEX], FEATURE_UNIT[CURRENT_INDEX],
                        Field.Type.Float, DATA_MAX[CURRENT_INDEX], DATA_MIN[CURRENT_INDEX]),
                new Field(FEATURE_DATA_NAME[STATUS_INDEX], FEATURE_UNIT[STATUS_INDEX],
                        Field.Type.UInt8, DATA_MAX[STATUS_INDEX], DATA_MIN[STATUS_INDEX]),
        });
    }//FeatureBattery

    /**
     * extract the battery level from the data exported by this feature
     * @param s data exported by this feature
     * @return percentage of charge inside the battery, or nan if the data are not valid
     */
    public static float getBatteryLevel(Sample s) {
        if (s.data.length > PERCENTAGE_INDEX)
            if (s.data[PERCENTAGE_INDEX] != null)
                return s.data[PERCENTAGE_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getBatteryLevel

    /**
     * extract the battery voltage from the data exported by this feature
     * @param s data exported by this feature
     * @return battery voltage , or nan if the data are not valid
     */
    public static float getVoltage(Sample s) {
        if (s.data.length > VOLTAGE_INDEX)
            if (s.data[VOLTAGE_INDEX] != null)
                return s.data[VOLTAGE_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getVoltage

    /**
     * extract the current used by the system from the data exported by this feature
     * @param s data exported by this feature
     * @return current used by the system , or nan if the data are not valid
     */
    public static float getCurrent(Sample s) {
        if (s.data.length > CURRENT_INDEX)
            if (s.data[CURRENT_INDEX] != null)
                return s.data[CURRENT_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getCurrent

    /**
     * extract the battery status from the data exported by this feature
     * @param s data exported by this feature
     * @return battery status , or Error if data are not valid
     */
    public static BatteryStatus getBatteryStatus(Sample s) {
        int status = 0xFF;
        if (s.data.length > STATUS_INDEX)
            if (s.data[STATUS_INDEX] != null)
                status = s.data[STATUS_INDEX].byteValue();

        switch (status) {
            case 0x00:
                return BatteryStatus.LowBattery;
            case 0x01:
                return BatteryStatus.Discharging;
            case 0x02:
                return BatteryStatus.PluggedNotCharging;
            case 0x03:
                return BatteryStatus.Charging;
            case 0xFF:
                return BatteryStatus.Error;
            default:
                return BatteryStatus.Error;
        }//switch
    }//getBatteryStatus


    /**
     * extract the battery information from 7 byte
     * @param data array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (7) and data extracted (the battery information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 7)
            throw new IllegalArgumentException("There are no 7 bytes available to read");


        Sample temp = new Sample(timestamp,new Number[]{
                (float) NumberConversion.LittleEndian.bytesToInt16(data,dataOffset) / 10.0f,
                NumberConversion.LittleEndian.bytesToInt16(data,dataOffset + 2) / 1000.0f,
                NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 4),
                data[dataOffset + 6]
        });

        return new ExtractResult(temp,7);
    }

    /**
     * possible battery status
     */
    public enum BatteryStatus {
        /** low battery, when the battery capacity is below a threshold defined by the fw
         * programmer */
        LowBattery,
        /** the battery is discharging (the current is negative) */
        Discharging,
        /** the battery is fully charge and the cable is plugged */
        PluggedNotCharging,
        /** the battery is charging (current is positive */
        Charging,
        /** internal error or not valid status */
        Error
    }
}
