package com.st.blue_sdk.features.extended.medical_signal


data class MedicalSignalType(
    val numberOfSignals: Int,
    val precision: MedicalPrecision,
    val description: String,
    val yMeasurementUnit: String? = null,
    var minGraphValue: Int = 0,
    var maxGraphValue: Int = 0,
    var nLabels: Int = 0,
    var isAutoscale: Boolean = true,
    var showLegend: Boolean = true,
    var signalLabels: List<String> = listOf(),
    var cubicInterpolation: Boolean = false,
    var displayWindowTimeSecond: Int = 5
)

fun Byte.MedicalSignalType(): MedicalSignalType {
    return when (this.toInt()) {
        0x00 -> MedicalSignalType(1, MedicalPrecision.UBIT24, "RNB_RED (PPG1)", showLegend = false)
        0x01 -> MedicalSignalType(
            1, MedicalPrecision.UBIT24, "RNB_BLUE (PPG2)", showLegend = false,
            //minGraphValue = -6000,
            //maxGraphValue = 50000,
            //isAutoscale = false,
            nLabels = 10
        )

        0x02 -> MedicalSignalType(1, MedicalPrecision.UBIT24, "PPG3", showLegend = false)
        0x03 -> MedicalSignalType(1, MedicalPrecision.UBIT24, "PPG4", showLegend = false)
        0x04 -> MedicalSignalType(1, MedicalPrecision.UBIT24, "PPG5", showLegend = false)
        0x05 -> MedicalSignalType(1, MedicalPrecision.UBIT24, "PPG6", showLegend = false)
        0x06 -> MedicalSignalType(
            1, MedicalPrecision.BIT16, "Electromyography", showLegend = true
        )

        0x07 -> MedicalSignalType(
            4, MedicalPrecision.BIT16, "Bio impedance",
            signalLabels = listOf("ACp", "ACq", "DCp", "DCq"))

        0x08 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "Galvanic Skin Response",
            showLegend = false)

        0x09 -> MedicalSignalType(
            3,
            MedicalPrecision.BIT16,
            "Accelerometer",
            signalLabels = listOf("X", "Y", "Z"),
            //displayWindowTimeSecond = 5,
            cubicInterpolation = true)

        0x0A -> MedicalSignalType(
            3,
            MedicalPrecision.BIT16,
            "Gyroscope",
            signalLabels = listOf("Gx", "Gy", "Gz"),
            cubicInterpolation = true)

        0x0B -> MedicalSignalType(
            3,
            MedicalPrecision.BIT16,
            "Magnetometer",
            signalLabels = listOf("Mx", "My", "Mz"),
            cubicInterpolation = true)

        0x0C -> MedicalSignalType(
            1, MedicalPrecision.UBIT24, "Pressure", showLegend = false)

        0x0D -> MedicalSignalType(
            1, MedicalPrecision.BIT16, "Temperature", showLegend = false)

        0x10 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 1",
            showLegend = false)

        0x11 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 2",
            showLegend = false)

        0x12 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 3",
            showLegend = false)

        0x13 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 4",
            showLegend = false)

        0x14 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 5",
            showLegend = false)

        0x15 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 6",
            showLegend = false)

        0x16 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 7",
            showLegend = false)

        0x17 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 8",
            showLegend = false)

        0x18 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 9",
            showLegend = false)

        0x19 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 10",
            showLegend = false)

        0x1A -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 11",
            showLegend = false)

        0x1B -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "ECG Channel 12",
            showLegend = false)

        0x20 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "Bio impedance dZ")

        0x21 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "Bio impedance Z0")

        0x22 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "Bio impedance Ze")

        0x23 -> MedicalSignalType(
            1,
            MedicalPrecision.BIT16,
            "Bio impedance Zc")

        else -> MedicalSignalType(0, MedicalPrecision.NOT_DEFINED, "Not Supported")
    }
}


enum class MedicalPrecision {
    BIT16,
    UBIT16,
    BIT24,
    UBIT24,
    NOT_DEFINED
}