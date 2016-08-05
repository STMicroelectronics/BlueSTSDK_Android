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

import android.util.SparseArray;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureActivity;
import com.st.BlueSTSDK.Features.FeatureAudioADPCM;
import com.st.BlueSTSDK.Features.FeatureAudioADPCMSync;
import com.st.BlueSTSDK.Features.FeatureBattery;
import com.st.BlueSTSDK.Features.FeatureCarryPosition;
import com.st.BlueSTSDK.Features.FeatureDirectionOfArrival;
import com.st.BlueSTSDK.Features.FeatureFreeFall;
import com.st.BlueSTSDK.Features.FeatureSwitch;
import com.st.BlueSTSDK.Features.FeatureMemsGesture;
import com.st.BlueSTSDK.Features.FeaturePedometer;
import com.st.BlueSTSDK.Features.FeatureProximityGesture;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureLuminosity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeatureMemsGesture;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusion;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusionCompact;
import com.st.BlueSTSDK.Features.FeatureMicLevel;
import com.st.BlueSTSDK.Features.FeaturePedometer;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureProximity;
import com.st.BlueSTSDK.Features.FeatureProximityGesture;
import com.st.BlueSTSDK.Features.FeatureSwitch;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Features.remote.RemoteFeatureHumidity;
import com.st.BlueSTSDK.Features.remote.RemoteFeaturePressure;
import com.st.BlueSTSDK.Features.remote.RemoteFeatureSwitch;
import com.st.BlueSTSDK.Features.remote.RemoteFeatureTemperature;

import java.util.UUID;

/**
 * This class help to get list of services and characteristics available in the BlueST devices
 * <p>
 * It define the UUID and the name of the services and the characteristic UUID available in the
 * BlueST devices.
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class BLENodeDefines {

    /**
     * all the characteristics handle by this sdk must end with this value
     */
    private static final String COMMON_CHAR_UUID = "-11e1-ac36-0002a5d5c51b";

    /**
     * all the service handle by this sdk must to finish with this value
     */
    private final static String COMMON_UUID_SERVICES = "-11e1-9ab4-0002a5d5c51b";

    /**
     * This class help to get list of services available in the BlueST devices
     * <p>
     * It define the UUID and the name of the services available in the
     * BlueST devices.
     * </p>
     * <p>
     * A valid service UUID must have the form 00000000 -XXXX-11e1-9ab4-0002a5d5c51b,
     * where XXXX is the service id
     * </p>
     *
     * @author STMicroelectronics - Central Labs.
     * @version 1.0
     */
    public static class Services {

        private final static String SERVICE_UUID_FORMAT = "00000000-[0-9a-fA-F]{4}-11e1-9ab4-0002a5d5c51b";

        /**
         * return true if the service is handle by this sdk. It is handle by this sdk if the uuid is
         * 0000 0000-YYYY-11e1-9ab4-0002a5d5c51b
         *
         * @param uuid uuid of the service that we want test
         * @return true i the uuid ends with -11e1-9ab4-0002a5d5c51b
         */
        public static boolean isKnowService(UUID uuid) {
            String uuidString = uuid.toString();
            return uuidString.matches(SERVICE_UUID_FORMAT);
        }//isKnowService

        /**
         * Service for access to the board stdout/err
         * @author STMicroelectronics - Central Labs.
         */
        public static class Debug {

            /**
             * service UUID
             */
            public final static UUID DEBUG_SERVICE_UUID = UUID.fromString("0000000-000E" +
                    COMMON_UUID_SERVICES);

            /**
             * all the characteristics from this service will hand with this value
             */
            private static String COMMON_DEBUG_UUID_CHAR = "-000E" + COMMON_CHAR_UUID;

            /**
             * characteristic where you can write and read output commands
             */
            public final static UUID DEBUG_TERM_UUID = UUID.fromString
                    ("00000001" + COMMON_DEBUG_UUID_CHAR);

            /**
             * characteristic where the node will write error message
             */
            public final static UUID DEBUG_STDERR_UUID = UUID.fromString
                    ("00000002" + COMMON_DEBUG_UUID_CHAR);

            /**
             * true if is a valid debug characteristic
             *
             * @param charUuid characteristic uuid
             * @return true the param is equal to \code{DEBUG_STDERR_UUID} or \code{DEBUG_TERM_UUID}
             */
            public static boolean isDebugCharacteristics(UUID charUuid) {
                return charUuid.equals(DEBUG_STDERR_UUID) ||
                        charUuid.equals(DEBUG_TERM_UUID);
            }//isDebugCharacteristics

            private Debug(){}

        }//Debug

        /**
         * Service that permit to configure the board parameters or the features
         * @author STMicroelectronics - Central Labs.
         */
        public static class Config {

            /**
             * Service uuid
             */
            public final static UUID CONFIG_CONTROL_SERVICE_UUID = UUID.fromString("0000000-000F" +
                    COMMON_UUID_SERVICES);

            /**
             * all the characteristics of this service will end with this value
             */
            private static String COMMON_CONFIG_UUID_CHAR = "-000F" + COMMON_CHAR_UUID;

            /**
             * characteristic permit to send a command to a feature
             */
            public final static UUID FEATURE_COMMAND_UUID = UUID.fromString
                    ("00000002" + COMMON_CONFIG_UUID_CHAR);
            /**
             * characteristic permit to Manage register (read or write register values)
             */
            public final static UUID REGISTERS_ACCESS_UUID = UUID.fromString
                    ("00000001" + COMMON_CONFIG_UUID_CHAR);

            private Config(){}

        }//Config

        private Services(){}

    }//Services

    /**
     * This class define the characteristics associated with the features
     * <p>A feature characteristics must have the format XXXXXXXX-0001-11e1-ac36-0002a5d5c51b</p>
     * <p>XXXXXXXX is a number where only one bit is 1, if multiple bit is at 1 this means that
     * this characteristics will send the union of that features</p>
     * <p>The general purpose feature will be created if there is a characteristics with the format
     * XXXXXXXX-0003-11e1-ac36-0002a5d5c51b, in this case the the feature data are not parsed, but
     * just notify to the user as an array of byte
     * </p>
     * @author STMicroelectronics - Central Labs.
     */
    public static class FeatureCharacteristics {
        /**
         * all the valid characteristics have to finish with this value
         */
        public static final String COMMON_FEATURE_UUID = "0001" + COMMON_CHAR_UUID;

        /**
         * extract the fist 32 bits from the characteristics UUID
         *
         * @param uuid characteristics uuid
         * @return feature mask bit, the first 32 bit of the UUID
         */
        public static int extractFeatureMask(UUID uuid) {
            return (int) (uuid.getMostSignificantBits() >> 32);
        }

        /**
         * return true if the UUID can be a valid feature UUID
         *
         * @param uuid characteristics uuid to test
         * @return true if the uuid end with \code{COMMON_FEATURE_UUID}
         */
        public static boolean isFeatureCharacteristics(UUID uuid) {
            String uuidString = uuid.toString();
            return uuidString.endsWith(COMMON_FEATURE_UUID);
        }//isKnowService

        /**
         * all the valid general purpose characteristics have to finish with this value
         */
        public static final String GP_FEATURE_UUID = "0003" + COMMON_CHAR_UUID;

        /**
         * return true if the UUID can be a valid general purpose characteristics
         * @param uuid characteristics to to test
         * @return true if the uuid end with \code{GP_FEATURE_UUID}
         */
        public static boolean isGeneralPurposeCharacteristics(UUID uuid) {
            String uuidString = uuid.toString();
            return uuidString.endsWith(GP_FEATURE_UUID);
        }

        /**
         * array that map a feature mask with a feature class, for the generic devices
         */
        public static final SparseArray<Class<? extends Feature>> genericDeviceFeatures =
                new SparseArray<>();

        /**
         * array that map a feature mask with a feature class, for the STEVAL-WESU1 devices
         */
        public static final SparseArray<Class<? extends Feature>> STEVAL_WESU1_DeviceFeatures =
                new SparseArray<>();

        /**
         * array that map a feature mask with a feature class, for the SensorTile devices
         */
        public static final SparseArray<Class<? extends Feature>> SensorTile_DeviceFeatures =
                new SparseArray<>();
        public static final SparseArray<Class<? extends Feature>> BlueCoin_DeviceFeatures =
                new SparseArray<>();

        /**
         * array that map a feature mask with a feature class, for a nucleo devices
         */
        public static final SparseArray<Class<? extends Feature>> Nucleo_Generic_Features =
                new SparseArray<>();

        /**
         * array that map a feature mask with a feature class, for a nucleo devices
         */
        public static final SparseArray<Class<? extends Feature>> Nucleo_Remote_Features =
                new SparseArray<>();

        static {

            STEVAL_WESU1_DeviceFeatures.put(0x00800000, FeatureAcceleration.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00400000, FeatureGyroscope.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00200000, FeatureMagnetometer.class);
            //STEVAL_WESU1_DeviceFeatures.put(0x00080000, FeatureHumidity.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00040000, FeatureTemperature.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00100000, FeaturePressure.class);
            //STEVAL_WESU1_DeviceFeatures.put(0x01000000, FeatureLuminosity.class);
            //STEVAL_WESU1_DeviceFeatures.put(0x02000000, FeatureProximity.class);
            //STEVAL_WESU1_DeviceFeatures.put(0x04000000, FeatureMicLevel.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00020000, FeatureBattery.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00000400, FeatureAccelerationEvent.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00000200, FeatureFreeFall.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00000100, FeatureMemsSensorFusionCompact.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00000080, FeatureMemsSensorFusion.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00000010, FeatureActivity.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00000008, FeatureCarryPosition.class);
            //STEVAL_WESU1_DeviceFeatures.put(0x00000004, FeatureProximityGesture.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00000002, FeatureMemsGesture.class);
            STEVAL_WESU1_DeviceFeatures.put(0x00000001, FeaturePedometer.class);

            SensorTile_DeviceFeatures.put(0x40000000, FeatureAudioADPCMSync.class);
            SensorTile_DeviceFeatures.put(0x20000000, FeatureSwitch.class);
            SensorTile_DeviceFeatures.put(0x10000000, FeatureDirectionOfArrival.class);
            SensorTile_DeviceFeatures.put(0x08000000, FeatureAudioADPCM.class);
            SensorTile_DeviceFeatures.put(0x04000000, FeatureMicLevel.class); //1 mic
            SensorTile_DeviceFeatures.put(0x00800000, FeatureAcceleration.class);
            SensorTile_DeviceFeatures.put(0x00400000, FeatureGyroscope.class);
            SensorTile_DeviceFeatures.put(0x00200000, FeatureMagnetometer.class);
            SensorTile_DeviceFeatures.put(0x00100000, FeaturePressure.class);
            SensorTile_DeviceFeatures.put(0x00080000, FeatureHumidity.class);
            SensorTile_DeviceFeatures.put(0x00040000, FeatureTemperature.class);
            SensorTile_DeviceFeatures.put(0x00010000, FeatureTemperature.class);
            SensorTile_DeviceFeatures.put(0x00000400, FeatureAccelerationEvent.class);
            SensorTile_DeviceFeatures.put(0x00000200, FeatureFreeFall.class);
            SensorTile_DeviceFeatures.put(0x00000100, FeatureMemsSensorFusionCompact.class);
            SensorTile_DeviceFeatures.put(0x00000080, FeatureMemsSensorFusion.class);
            SensorTile_DeviceFeatures.put(0x00000010, FeatureActivity.class);
            SensorTile_DeviceFeatures.put(0x00000008, FeatureCarryPosition.class);
            SensorTile_DeviceFeatures.put(0x00000002, FeatureMemsGesture.class);
            SensorTile_DeviceFeatures.put(0x00000001, FeaturePedometer.class);

            BlueCoin_DeviceFeatures.put(0x40000000, FeatureAudioADPCMSync.class);
            BlueCoin_DeviceFeatures.put(0x20000000, FeatureSwitch.class);
            BlueCoin_DeviceFeatures.put(0x10000000, FeatureDirectionOfArrival.class);
            BlueCoin_DeviceFeatures.put(0x08000000, FeatureAudioADPCM.class);
            BlueCoin_DeviceFeatures.put(0x04000000, FeatureMicLevel.class);//4 microphone
            BlueCoin_DeviceFeatures.put(0x00800000, FeatureAcceleration.class);
            BlueCoin_DeviceFeatures.put(0x00400000, FeatureGyroscope.class);
            BlueCoin_DeviceFeatures.put(0x00200000, FeatureMagnetometer.class);
            BlueCoin_DeviceFeatures.put(0x00100000, FeaturePressure.class);
            BlueCoin_DeviceFeatures.put(0x00040000, FeatureTemperature.class);
            BlueCoin_DeviceFeatures.put(0x00000400, FeatureAccelerationEvent.class);
            BlueCoin_DeviceFeatures.put(0x00000200, FeatureFreeFall.class);
            BlueCoin_DeviceFeatures.put(0x00000100, FeatureMemsSensorFusionCompact.class);
            BlueCoin_DeviceFeatures.put(0x00000080, FeatureMemsSensorFusion.class);
            BlueCoin_DeviceFeatures.put(0x00000010, FeatureActivity.class);
            BlueCoin_DeviceFeatures.put(0x00000008, FeatureCarryPosition.class);
            BlueCoin_DeviceFeatures.put(0x00000002, FeatureMemsGesture.class);
            BlueCoin_DeviceFeatures.put(0x00000001, FeaturePedometer.class);


            Nucleo_Generic_Features.put(0x40000000, FeatureAudioADPCMSync.class);
            Nucleo_Generic_Features.put(0x20000000, FeatureSwitch.class);
            Nucleo_Generic_Features.put(0x10000000, FeatureDirectionOfArrival.class);
            Nucleo_Generic_Features.put(0x08000000, FeatureAudioADPCM.class);
            Nucleo_Generic_Features.put(0x04000000, FeatureMicLevel.class);
            Nucleo_Generic_Features.put(0x02000000, FeatureProximity.class);
            Nucleo_Generic_Features.put(0x01000000, FeatureLuminosity.class);
            Nucleo_Generic_Features.put(0x00800000, FeatureAcceleration.class);
            Nucleo_Generic_Features.put(0x00400000, FeatureGyroscope.class);
            Nucleo_Generic_Features.put(0x00200000, FeatureMagnetometer.class);
            Nucleo_Generic_Features.put(0x00100000, FeaturePressure.class);
            Nucleo_Generic_Features.put(0x00080000, FeatureHumidity.class);
            Nucleo_Generic_Features.put(0x00040000, FeatureTemperature.class);
            Nucleo_Generic_Features.put(0x00020000, FeatureBattery.class);
            Nucleo_Generic_Features.put(0x00010000, FeatureTemperature.class);
            Nucleo_Generic_Features.put(0x00000400, FeatureAccelerationEvent.class);
            Nucleo_Generic_Features.put(0x00000200, FeatureFreeFall.class);
            Nucleo_Generic_Features.put(0x00000100, FeatureMemsSensorFusionCompact.class);
            Nucleo_Generic_Features.put(0x00000080, FeatureMemsSensorFusion.class);
            Nucleo_Generic_Features.put(0x00000010, FeatureActivity.class);
            Nucleo_Generic_Features.put(0x00000008, FeatureCarryPosition.class);
            Nucleo_Generic_Features.put(0x00000004, FeatureProximityGesture.class);
            Nucleo_Generic_Features.put(0x00000002, FeatureMemsGesture.class);
            Nucleo_Generic_Features.put(0x00000001, FeaturePedometer.class);

            Nucleo_Remote_Features.put(0x20000000, RemoteFeatureSwitch.class);
            Nucleo_Remote_Features.put(0x00100000, RemoteFeaturePressure.class);
            Nucleo_Remote_Features.put(0x00080000, RemoteFeatureHumidity.class);
            Nucleo_Remote_Features.put(0x00040000, RemoteFeatureTemperature.class);


        }//static

        private FeatureCharacteristics(){}

    }//FeatureCharacteristics

    private  BLENodeDefines(){}
}//BLENodeDefines
