package com.st.BlueSTSDK.Features.Audio.Opus

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BlueVoiceOpusTransportProtocolEncodeTest{

    private val data = byteArrayOf(0x01,0x02,0x03)
    @Test
    fun smallPackageStartWith0x20(){
        val encodedData = BlueVoiceOpusTransportProtocol.packData(data,10)
        assertEquals(1,encodedData.size)
        val packageData = encodedData.first()
        assertEquals(data.size+1,packageData.size)
        assertEquals(0x20.toByte(),packageData[0])
        assertArrayEquals(packageData.drop(1).toByteArray(),data)

    }

    @Test
    fun splitDataStartWith0x00EndsWith0x80(){
        val encodedData = BlueVoiceOpusTransportProtocol.packData(data,3)

        assertEquals(2,encodedData.size)
        val firstPackageData = encodedData[0]
        assertEquals(3,firstPackageData.size)
        assertEquals(0x00.toByte(),firstPackageData[0])
        assertEquals(data.dropLast(1),firstPackageData.drop(1))

        val lastPackageData = encodedData[1]
        assertEquals(lastPackageData.size,2)
        assertEquals(lastPackageData[0],0x80.toByte())
        assertEquals(data[2],lastPackageData[1])

    }

    @Test
    fun middlePackageStartWith0x40(){
        val encodedData = BlueVoiceOpusTransportProtocol.packData(data,2)

        assertEquals(3,encodedData.size)
        val middlePackageData = encodedData[1]
        assertEquals(2,middlePackageData.size)
        assertEquals(0x40.toByte(),middlePackageData[0])
        assertEquals(data[1],middlePackageData[1])

    }
}


@RunWith(JUnit4::class)
class BlueVoiceOpusTransportProtocolDecodeTest{

    @Test
    fun singlePackageStartWith0x20(){
        val decoder = BlueVoiceOpusTransportProtocol(2)
        val encodedData = byteArrayOf(0x20.toByte(),0x01,0x02)
        val decodedData = decoder.unpackData(encodedData)

        assertNotNull(decodedData)
        assertEquals(2,decodedData?.size)
        assertArrayEquals(encodedData.drop(1).toByteArray(),decodedData)
    }


    @Test
    fun multiplePacakgeAreMerged(){
        val decoder = BlueVoiceOpusTransportProtocol(3)
        val encodedData = listOf(
                byteArrayOf(0x00.toByte(),0x01,0x02),
                byteArrayOf(0x80.toByte(),0x03)
        )
        var decodedData = decoder.unpackData(encodedData[0])
        assertNull(decodedData)
        decodedData = decoder.unpackData(encodedData[1])
        assertNotNull(decodedData)
        assertEquals(3,decodedData?.size)
        assertArrayEquals(byteArrayOf(0x1,0x2,0x3),decodedData)
    }

    @Test
    fun multiplePacakgeAreMerged_2(){
        val decoder = BlueVoiceOpusTransportProtocol(3)
        val encodedData = listOf(
                byteArrayOf(0x00.toByte(),0x01),
                byteArrayOf(0x40.toByte(),0x02),
                byteArrayOf(0x80.toByte(),0x03)
        )
        var decodedData = decoder.unpackData(encodedData[0])
        assertNull(decodedData)
        decodedData = decoder.unpackData(encodedData[1])
        assertNull(decodedData)
        decodedData = decoder.unpackData(encodedData[2])
        assertNotNull(decodedData)
        assertEquals(3,decodedData?.size)
        assertArrayEquals(byteArrayOf(0x1,0x2,0x3),decodedData)
    }

}