/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features

import com.st.blue_sdk.features.Feature.Type.*
import com.st.blue_sdk.features.acceleration.Acceleration
import com.st.blue_sdk.features.acceleration_event.AccelerationEvent
import com.st.blue_sdk.features.activity.Activity
import com.st.blue_sdk.features.audio.adpcm.AudioADPCMFeature
import com.st.blue_sdk.features.audio.adpcm.AudioADPCMSyncFeature
import com.st.blue_sdk.features.battery.Battery
import com.st.blue_sdk.features.beam_forming.BeamForming
import com.st.blue_sdk.features.carry_position.CarryPosition
import com.st.blue_sdk.features.co_sensor.COSensor
import com.st.blue_sdk.features.compass.Compass
import com.st.blue_sdk.features.direction_of_arrival.DirectionOfArrival
import com.st.blue_sdk.features.event_counter.EventCounter
import com.st.blue_sdk.features.extended.ai_logging.AiLogging
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusConfFeature
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusFeature
import com.st.blue_sdk.features.extended.audio_classification.AudioClassification
import com.st.blue_sdk.features.extended.binary_content.BinaryContent
import com.st.blue_sdk.features.extended.color_ambient_light.ColorAmbientLight
import com.st.blue_sdk.features.extended.euler_angle.EulerAngle
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration
import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivity
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigation
import com.st.blue_sdk.features.extended.gnss.GNSS
import com.st.blue_sdk.features.extended.hs_datalog_config.HSDataLogConfig
import com.st.blue_sdk.features.extended.ispu_control.ISPUControlFeature
import com.st.blue_sdk.features.extended.json_nfc.JsonNFC
import com.st.blue_sdk.features.extended.medical_signal.MedicalSignal16BitFeature
import com.st.blue_sdk.features.extended.medical_signal.MedicalSignal24BitFeature
import com.st.blue_sdk.features.extended.motion_algorithm.MotionAlgorithm
import com.st.blue_sdk.features.extended.motor_time_param.MotorTimeParameter
import com.st.blue_sdk.features.extended.navigation_control.NavigationControl
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetection
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassification
import com.st.blue_sdk.features.extended.neai_extrapolation.NeaiExtrapolation
import com.st.blue_sdk.features.extended.piano.Piano
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.predictive.PredictiveAccelerationStatus
import com.st.blue_sdk.features.extended.predictive.PredictiveFrequencyStatus
import com.st.blue_sdk.features.extended.predictive.PredictiveSpeedStatus
import com.st.blue_sdk.features.extended.qvar.QVAR
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeature
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeature.Companion.FSM_NAME
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeature.Companion.ML_CORE_NAME
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeature.Companion.STRED_NAME
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObject
import com.st.blue_sdk.features.external.std.BodySensorLocation
import com.st.blue_sdk.features.external.std.HeartRate
import com.st.blue_sdk.features.external.stm32.led_and_reboot.ControlLedAndReboot
import com.st.blue_sdk.features.external.stm32.network_status.NetworkStatus
import com.st.blue_sdk.features.external.stm32.switch_status.SwitchStatus
import com.st.blue_sdk.features.fft.FFTAmplitudeFeature
import com.st.blue_sdk.features.free_fall.FreeFall
import com.st.blue_sdk.features.general_purpose.GeneralPurpose
import com.st.blue_sdk.features.gyroscope.Gyroscope
import com.st.blue_sdk.features.humidity.Humidity
import com.st.blue_sdk.features.logging.sd.SDLoggingFeature
import com.st.blue_sdk.features.luminosity.Luminosity
import com.st.blue_sdk.features.magnetometer.Magnetometer
import com.st.blue_sdk.features.mems_gesture.MemsGesture
import com.st.blue_sdk.features.mems_norm.MemsNorm
import com.st.blue_sdk.features.mic_level.MicLevel
import com.st.blue_sdk.features.motion_intensity.MotionIntensity
import com.st.blue_sdk.features.ota.ImageFeature
import com.st.blue_sdk.features.ota.nrg.ExpectedImageTUSeqNumberFeature
import com.st.blue_sdk.features.ota.nrg.NewImageFeature
import com.st.blue_sdk.features.ota.nrg.NewImageTUContentFeature
import com.st.blue_sdk.features.ota.stm32wb.OTAControl
import com.st.blue_sdk.features.ota.stm32wb.OTAFileUpload
import com.st.blue_sdk.features.ota.stm32wb.OTAReboot
import com.st.blue_sdk.features.ota.stm32wb.OTAWillReboot
import com.st.blue_sdk.features.pedometer.Pedometer
import com.st.blue_sdk.features.pressure.Pressure
import com.st.blue_sdk.features.proximity.Proximity
import com.st.blue_sdk.features.proximity_gesture.ProximityGesture
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled
import com.st.blue_sdk.features.extended.scene_description.SceneDescription
import com.st.blue_sdk.features.remote.humidity.RemoteHumidity
import com.st.blue_sdk.features.remote.pressure.RemotePressure
import com.st.blue_sdk.features.remote.switch.RemoteSwitch
import com.st.blue_sdk.features.remote.temperature.RemoteTemperature
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusion
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionCompat
import com.st.blue_sdk.features.stepper_motor.StepperMotor
import com.st.blue_sdk.features.switchfeature.SwitchFeature
import com.st.blue_sdk.features.temperature.Temperature
import com.st.blue_sdk.logger.Loggable
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.utils.NumberConversion
import java.util.*

abstract class Feature<T>(
    val isEnabled: Boolean,
    val type: Type,
    val identifier: Int,
    val name: String,
    var maxPayloadSize: Int = 20,
    val hasTimeStamp: Boolean = true,
    val isDataNotifyFeature: Boolean = true
) where  T : Loggable {

    enum class Type(val suffix: String) {
        STANDARD(suffix = "-0001-11e1-ac36-0002a5d5c51b"),
        EXTENDED(suffix = "-0002-11e1-ac36-0002a5d5c51b"),
        GENERAL_PURPOSE(suffix = "0000-0003-11e1-ac36-0002a5d5c51b"),
        EXTERNAL_BLUE_NRG_OTA(suffix = "-8508-11e3-baa7-0800200c9a66"),
        EXTERNAL_STM32(suffix = "-8e22-4541-9d4c-21edae82ed19"),

        //EXTERNAL_STD_CHART(suffix = "0x2A05-0000-1000-8000-00805f9b34fb");
        EXTERNAL_STD_CHART(suffix = "-0000-1000-8000-00805f9b34fb");

        companion object {
            fun fromSuffix(suffix: String): Type {
                return entries.firstOrNull {
                    suffix.equals(other = it.suffix, ignoreCase = true)
                } ?: throw IllegalArgumentException("Type for suffix $suffix not found!")
            }
        }
    }

    abstract fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<T>

    abstract fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray?

    abstract fun parseCommandResponse(data: ByteArray): FeatureResponse?

    fun packCommandRequest(featureBit: Int?, commandId: Byte, data: ByteArray): ByteArray {
        val maskBytes =
            featureBit?.let { NumberConversion.BigEndian.int32ToBytes(it) } ?: byteArrayOf()
        return maskBytes + byteArrayOf(commandId) + data
    }

    fun unpackCommandResponse(data: ByteArray): RawCommandResponse? {
        if (data.size < 7) //if we miss some data
            return null

        val timeStamp: Int = NumberConversion.LittleEndian.bytesToUInt16(data)
        val mask: Int = NumberConversion.BigEndian.bytesToInt32(data, 2)
        val commandId = data[6]
        val payload = data.copyOfRange(7, data.size)

        return RawCommandResponse(timeStamp.toLong(), mask, commandId, payload)
    }

    val uuid: UUID?
        get() = when (type) {
            STANDARD -> {
                null
            }

            GENERAL_PURPOSE ->
                UUID.fromString(String.format("%04X${type.suffix}", identifier))

            else ->
                UUID.fromString(String.format("%08X${type.suffix}", identifier))
        }

    val mask: Int?
        get() = when (type) {
            STANDARD -> identifier
            else -> null
        }

    companion object {

        fun createFeature(
            boardModel: Boards.Model? = null,
            containsRemoteFeatures: Boolean = false,
            identifier: Int,
            type: Type,
            isEnabled: Boolean = true,
            maxPayloadSize: Int = 20
        ): Feature<*> = when (type) {
            STANDARD -> {
                if (containsRemoteFeatures) {
                    when (identifier) {
                        0x20000000 -> RemoteSwitch(isEnabled = isEnabled, identifier = identifier)
                        0x00100000 -> RemotePressure(isEnabled = isEnabled, identifier = identifier)
                        0x00080000 -> RemoteHumidity(isEnabled = isEnabled, identifier = identifier)
                        0x00040000 -> RemoteTemperature(
                            isEnabled = isEnabled,
                            identifier = identifier
                        )

                        else -> throw UnsupportedOperationException("$type unknown")
                    }
                } else {
                    when (boardModel) {
                        Boards.Model.SENSOR_TILE_BOX ->
                            when (identifier) {
                                0x40000000 -> AudioADPCMSyncFeature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x20000000 -> SwitchFeature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x10000000 -> MemsNorm(
                                    isEnabled = isEnabled,
                                    identifier = identifier,
                                    name = "Mems Norm Legacy"
                                )

                                0x08000000 -> AudioADPCMFeature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x04000000 -> MicLevel(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x02000000 -> AudioClassification(
                                    isEnabled = isEnabled,
                                    identifier = identifier,
                                    type = type
                                )

                                0x00800000 -> Acceleration(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00400000 -> Gyroscope(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00200000 -> Magnetometer(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00100000 -> Pressure(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00080000 -> Humidity(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00040000, 0x00010000 -> Temperature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00020000 -> Battery(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00004000 -> EulerAngle(
                                    isEnabled = isEnabled,
                                    identifier = identifier,
                                    type = type
                                )

                                0x00001000 -> SDLoggingFeature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000400 -> AccelerationEvent(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000200 -> EventCounter(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000100 -> MemsSensorFusionCompat(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000080 -> MemsSensorFusion(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000020 -> MotionIntensity(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000040 -> Compass(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000010 -> Activity(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000008 -> CarryPosition(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000002 -> MemsGesture(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000001 -> Pedometer(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                else -> throw UnsupportedOperationException("$type unknown")
                            }

                        else ->
                            when (identifier) {
                                0x40000000 -> AudioADPCMSyncFeature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x20000000 -> SwitchFeature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x10000000 -> DirectionOfArrival(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x08000000 -> AudioADPCMFeature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x04000000 -> MicLevel(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x02000000 -> Proximity(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x01000000 -> Luminosity(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00800000 -> Acceleration(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00400000 -> Gyroscope(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00200000 -> Magnetometer(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00100000 -> Pressure(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00080000 -> Humidity(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00040000, 0x00010000 -> Temperature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00020000 -> Battery(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00008000 -> COSensor(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00002000 -> StepperMotor(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00001000 -> SDLoggingFeature(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000800 -> BeamForming(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000400 -> AccelerationEvent(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000200 -> FreeFall(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000100 -> MemsSensorFusionCompat(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000080 -> MemsSensorFusion(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000040 -> Compass(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000020 -> MotionIntensity(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000010 -> Activity(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000008 -> CarryPosition(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000004 -> ProximityGesture(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000002 -> MemsGesture(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                0x00000001 -> Pedometer(
                                    isEnabled = isEnabled,
                                    identifier = identifier
                                )

                                else -> throw UnsupportedOperationException("$type unknown")
                            }
                    }
                }
            }

            EXTENDED -> when (identifier) {
                0x01 -> AudioOpusFeature(isEnabled = isEnabled, identifier = identifier)
                0x02 -> AudioOpusConfFeature(isEnabled = isEnabled, identifier = identifier)
                0x03 -> AudioClassification(isEnabled = isEnabled, identifier = identifier)
                0x04 -> AiLogging(isEnabled = isEnabled, identifier = identifier)
                0x05 -> FFTAmplitudeFeature(isEnabled = isEnabled, identifier = identifier)
                0x06 -> MotorTimeParameter(isEnabled = isEnabled, identifier = identifier)
                0x07 -> PredictiveSpeedStatus(isEnabled = isEnabled, identifier = identifier)
                0x08 -> PredictiveAccelerationStatus(isEnabled = isEnabled, identifier = identifier)
                0x09 -> PredictiveFrequencyStatus(isEnabled = isEnabled, identifier = identifier)
                0x0A -> MotionAlgorithm(isEnabled = isEnabled, identifier = identifier)
                0x0D -> EulerAngle(isEnabled = isEnabled, identifier = identifier)
                0x0E -> FitnessActivity(isEnabled = isEnabled, identifier = identifier)
                0x0F -> RegistersFeature(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    name = ML_CORE_NAME,
                    regName = "MLC"
                )

                0x10 -> RegistersFeature(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    name = FSM_NAME,
                    regName = "FSM"
                )

                0x11 -> HSDataLogConfig(isEnabled = isEnabled, identifier = identifier)
                // 0x12 is exposed by HSDataLog old fw even if it's not used
                0x13 -> ToFMultiObject(isEnabled = isEnabled, identifier = identifier)
                0x14 -> ExtConfiguration(isEnabled = isEnabled, identifier = identifier)
                0x15 -> ColorAmbientLight(isEnabled = isEnabled, identifier = identifier)
                0x16 -> QVAR(isEnabled = isEnabled, identifier = identifier)
                0x17 -> RegistersFeature(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    name = STRED_NAME,
                    regName = "Reg"
                )

                0x18 -> GNSS(isEnabled = isEnabled, identifier = identifier)
                0x19 -> NeaiAnomalyDetection(isEnabled = isEnabled, identifier = identifier)
                0x1A -> NeaiClassClassification(isEnabled = isEnabled, identifier = identifier)
                0x1B -> PnPL(isEnabled = isEnabled, identifier = identifier)
                0x1C -> Piano(isEnabled = isEnabled, identifier = identifier)
                0x1D -> EventCounter(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = EXTENDED
                )
                //0x1E -> Quasar?
                0x1F -> GestureNavigation(isEnabled = isEnabled, identifier = identifier)
                0x20 -> JsonNFC(isEnabled = isEnabled, identifier = identifier)
                0x21 -> MemsNorm(isEnabled = isEnabled, identifier = identifier, type = EXTENDED)
                0x22 -> BinaryContent(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    maxPayloadSize = maxPayloadSize
                )

                0x23 -> RawControlled(
                    isEnabled = isEnabled,
                    identifier = identifier
                )

                0x24 -> NeaiExtrapolation(isEnabled = isEnabled, identifier = identifier)
                0x25 -> ISPUControlFeature(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    maxPayloadSize = maxPayloadSize
                )
                0x26 -> MedicalSignal16BitFeature(
                    isEnabled = isEnabled,
                    identifier = identifier
                )
                0x27 -> MedicalSignal24BitFeature(
                    isEnabled = isEnabled,
                    identifier = identifier
                )
                0x28 -> NavigationControl(isEnabled = isEnabled, identifier = identifier)
                0x29 -> SceneDescription(isEnabled = isEnabled, identifier = identifier)

                else -> throw UnsupportedOperationException("$type unknown")

            }

            GENERAL_PURPOSE -> {
                GeneralPurpose(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    name = "GP_$identifier"
                )
            }

            EXTERNAL_STM32 -> when (identifier) {
                Integer.decode("0x0000fe11") -> OTAReboot(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                Integer.decode("0x0000fe22") -> OTAControl(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                Integer.decode("0x0000fe23") -> OTAWillReboot(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                Integer.decode("0x0000fe24") -> OTAFileUpload(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    maxPayloadSize = maxPayloadSize,
                    type = type
                )

                Integer.decode("0x0000fe41") -> ControlLedAndReboot(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                Integer.decode("0x0000fe42") -> SwitchStatus(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                Integer.decode("0x0000fe51") -> NetworkStatus(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                else -> throw UnsupportedOperationException("$type unknown")
            }

            EXTERNAL_BLUE_NRG_OTA -> when (identifier) {
                Integer.decode("0x122e8cc0") -> ImageFeature(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                Integer.decode("0x210f99f0") -> NewImageFeature(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                Integer.decode("0x2691aa80") -> NewImageTUContentFeature(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                Integer.decode("0x2bdc5760") -> ExpectedImageTUSeqNumberFeature(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                else -> throw UnsupportedOperationException("$type unknown")
            }

            EXTERNAL_STD_CHART -> when (identifier) {
                Integer.decode("0x00002a37") -> HeartRate(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                Integer.decode("0x00002A38") -> BodySensorLocation(
                    isEnabled = isEnabled,
                    identifier = identifier,
                    type = type
                )

                else -> throw UnsupportedOperationException("$type unknown")
            }
        }
    }
}
