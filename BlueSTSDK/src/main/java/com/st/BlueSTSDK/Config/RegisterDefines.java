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
package com.st.BlueSTSDK.Config;


import java.util.HashMap;

/**
 * This class help to get list of Registers available for The BlueST devices
 * <p>
 *     It define the Register list available for the device
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class RegisterDefines {

    /**
     * This enum contains the registers name the suffix __XY represent the target available for that
     * registers P = Persistent, S=Session, if both are specified the register is available in both
     * side persistent and session.
     */
    public enum RegistersName{
        /*Mandatory registers*/
        FW_VER,
        LED_CONFIG,
        BLE_LOC_NAME,
        BLE_PUB_ADDR,
        BLE_ADDR_TYPE,

        BATTERY_LEVEL,
        BATTERY_VOLTAGE,
        CURRENT,
        PWRMNG_STATUS,

        /*optional generic*/
        RADIO_TXPWR_CONFIG,
        TIMER_FREQ,
        PWR_MODE_CONFIG,
        HW_FEATURES_MAP,
        HW_FEATURE_CTRLS_0001,
        HW_FEATURE_CTRLS_0002,
        HW_FEATURE_CTRLS_0004,
        HW_FEATURE_CTRLS_0008,
        HW_FEATURE_CTRLS_0010,
        HW_FEATURE_CTRLS_0020,
        HW_FEATURE_CTRLS_0040,
        HW_FEATURE_CTRLS_0080,
        HW_FEATURE_CTRLS_0100,
        HW_FEATURE_CTRLS_0200,
        HW_FEATURE_CTRLS_0400,
        HW_FEATURE_CTRLS_0800,
        HW_FEATURE_CTRLS_1000,
        HW_FEATURE_CTRLS_2000,
        HW_FEATURE_CTRLS_4000,
        HW_FEATURE_CTRLS_8000,
        SW_FEATURES_MAP,
        SW_FEATURE_CTRLS_0001,
        SW_FEATURE_CTRLS_0002,
        SW_FEATURE_CTRLS_0004,
        SW_FEATURE_CTRLS_0008,
        SW_FEATURE_CTRLS_0010,
        SW_FEATURE_CTRLS_0020,
        SW_FEATURE_CTRLS_0040,
        SW_FEATURE_CTRLS_0080,
        SW_FEATURE_CTRLS_0100,
        SW_FEATURE_CTRLS_0200,
        SW_FEATURE_CTRLS_0400,
        SW_FEATURE_CTRLS_0800,
        SW_FEATURE_CTRLS_1000,
        SW_FEATURE_CTRLS_2000,
        SW_FEATURE_CTRLS_4000,
        SW_FEATURE_CTRLS_8000,
        BLE_DEBUG_CONFIG,
        USB_DEBUG_CONFIG,
        HW_CALIBRATION_MAP,
        SW_CALIBRATION_MAP,

        DFU_REBOOT,
        HW_CALIBRATION,
        HW_CALIBRATION_STATUS,
        SW_CALIBRATION,
        SW_CALIBRATION_STATUS;

        /**
         * Returns a string containing a concise, human-readable description of this
         * object. In this case, the enum constant's name is returned.
         *
         * @return a printable representation of this object.
         */
        @Override
        public String toString() {
            String strRet = super.toString();
            strRet = strRet.substring(0, strRet.lastIndexOf("__"))
                    .replace("_", " ")
                    .replace("SW", "SOFTWARE")
                    .replace("HW", "HARDWARE")
                    .replace("CTRL", "");

            return  strRet;
        }
    }

    /**
     * array that map the registers of BlueST devices
     */
    private static final HashMap<RegistersName, Register> mapRegisters = new HashMap<>();

    static {
        mapRegisters.put(RegistersName.FW_VER, new Register(0x00, 1, Register.Access.R, Register.Target.BOTH));
        mapRegisters.put(RegistersName.LED_CONFIG, new Register(0x02, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.BLE_LOC_NAME, new Register(0x03, 8, Register.Access.RW, Register.Target.PERSISTENT));
        mapRegisters.put(RegistersName.BLE_PUB_ADDR, new Register(0x0B, 3, Register.Access.RW, Register.Target.PERSISTENT));
        mapRegisters.put(RegistersName.BLE_ADDR_TYPE, new Register(0x0E, 1, Register.Access.RW, Register.Target.PERSISTENT));

        mapRegisters.put(RegistersName.BATTERY_LEVEL, new Register(0x03, 1, Register.Access.R, Register.Target.SESSION));
        mapRegisters.put(RegistersName.BATTERY_VOLTAGE, new Register(0x04, 2, Register.Access.R, Register.Target.SESSION));
        mapRegisters.put(RegistersName.CURRENT, new Register(0x06, 2, Register.Access.R, Register.Target.SESSION));
        mapRegisters.put(RegistersName.PWRMNG_STATUS, new Register(0x08, 1, Register.Access.R, Register.Target.SESSION));


        mapRegisters.put(RegistersName.RADIO_TXPWR_CONFIG, new Register(0x20, 1, Register.Access.RW, Register.Target.PERSISTENT));
        mapRegisters.put(RegistersName.TIMER_FREQ, new Register(0x21, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.PWR_MODE_CONFIG, new Register(0x22, 1, Register.Access.RW, Register.Target.BOTH));

        mapRegisters.put(RegistersName.HW_FEATURES_MAP, new Register(0x23, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0001, new Register(0x24, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0002, new Register(0x25, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0004, new Register(0x26, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0008, new Register(0x27, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0010, new Register(0x28, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0020, new Register(0x29, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0040, new Register(0x2A, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0080, new Register(0x2B, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0100, new Register(0x2C, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0200, new Register(0x2D, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0400, new Register(0x2E, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_0800, new Register(0x2F, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_1000, new Register(0x30, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_2000, new Register(0x31, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_4000, new Register(0x32, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.HW_FEATURE_CTRLS_8000, new Register(0x33, 1, Register.Access.RW, Register.Target.BOTH));

        mapRegisters.put(RegistersName.SW_FEATURES_MAP, new Register(0x34, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0001, new Register(0x35, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0002, new Register(0x36, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0004, new Register(0x37, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0008, new Register(0x38, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0010, new Register(0x39, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0020, new Register(0x3A, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0040, new Register(0x3B, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0080, new Register(0x3C, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0100, new Register(0x3D, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0200, new Register(0x3E, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0400, new Register(0x3F, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_0800, new Register(0x40, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_1000, new Register(0x41, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_2000, new Register(0x42, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_4000, new Register(0x43, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.SW_FEATURE_CTRLS_8000, new Register(0x44, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.BLE_DEBUG_CONFIG, new Register(0x45, 1, Register.Access.RW, Register.Target.BOTH));
        mapRegisters.put(RegistersName.USB_DEBUG_CONFIG, new Register(0x46, 1, Register.Access.RW, Register.Target.BOTH));

        mapRegisters.put(RegistersName.HW_CALIBRATION_MAP, new Register(0x47, 1, Register.Access.R, Register.Target.PERSISTENT));
        mapRegisters.put(RegistersName.SW_CALIBRATION_MAP, new Register(0x48, 1, Register.Access.R, Register.Target.PERSISTENT));

        mapRegisters.put(RegistersName.DFU_REBOOT, new Register(0xF0, 1, Register.Access.W, Register.Target.SESSION));
        mapRegisters.put(RegistersName.HW_CALIBRATION, new Register(0xF1, 1, Register.Access.RW, Register.Target.SESSION));
        mapRegisters.put(RegistersName.HW_CALIBRATION_STATUS, new Register(0xF2, 1, Register.Access.R, Register.Target.SESSION));
        mapRegisters.put(RegistersName.SW_CALIBRATION, new Register(0xF3, 1, Register.Access.RW, Register.Target.SESSION));
        mapRegisters.put(RegistersName.SW_CALIBRATION_STATUS, new Register(0xF4, 1, Register.Access.R, Register.Target.SESSION));
    }

    /**
     * Returns the register class of the available registers.
     *
     * @param name input RegistersName enum to find
     * @return the relative register if exist else null
     */
    public static Register lookUp(RegistersName name){
        return mapRegisters.get(name);
    }

    /**
     * Returns the request register from the map.
     *
     * @param address register address to find, it must match exactly
     * @param target  register target to find
     * @return the relative register with the specified address and target,
     * the target if a register match the criteria else it return null
     */
    public static Register lookUpFromAddress(int address, Register.Target target){
        Register regRet = null;
        for (Register r: mapRegisters.values()) {
            if (r.getAddress() == address && ((r.getTarget() == target) || (r.getTarget() == Register.Target.BOTH)) ) {
                regRet = r;
                break;
            }
        }
        return  regRet;
    }

    /**
     * Returns the request register name from the map
     *
     * @param address register address to find, it must match exactly
     * @param target  register target to find
     * @return the relative register name with the specified address and target,
     * the target if a register match the criteria else it return null
     */
    public static RegistersName lookUpRegisterNameFromAddress(int address, Register.Target target){
        RegistersName regRet = null;
        for (RegistersName n: mapRegisters.keySet()) {
            Register r = mapRegisters.get(n);
            if (r.getAddress() == address && ((r.getTarget() == target) || (r.getTarget() == Register.Target.BOTH)) ) {
                regRet = n;
                break;
            }
        }
        return  regRet;
    }


}
