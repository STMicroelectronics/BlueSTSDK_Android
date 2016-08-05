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

import com.st.BlueSTSDK.BuildConfig;
import com.st.BlueSTSDK.Node;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class,manifest = "src/main/AndroidManifest.xml", sdk = 21)
public class BleAdvertiseParserTest {

    @Test
    public void testTransmissionPower() throws Exception{
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                2,0x0A,0x40 /* 64dB? */, // Trasmission Power
        };
        BleAdvertiseParser parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals(64,parser.getTxPower());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                2,0x0A,(byte)0x7F /* 127? */, // Trasmission Power
        };
        parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals(127,parser.getTxPower());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                2,0x0A,(byte)0x80 /* -128 */, // Trasmission Power
        };
        parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals(-128,parser.getTxPower());
    }

    @Test
    public void testBoardName() throws Exception{
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                2,0x09,'c'
        };
        BleAdvertiseParser parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals("c",parser.getName());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
                6,0x09,'h','e','l','l','o'
        };
        parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals("hello",parser.getName());
    }

    @Test
    public void testProtocolVersion() throws Exception{
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x00,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
        };
        BleAdvertiseParser parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals(1,parser.getProtocolVersion());
    }

    @Test(expected= InvalidBleAdvertiseFormat.class)
    public void testWrongProtocolVersion() throws Exception{
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0xFF,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, //vendor data is mandatory
        };
        new BleAdvertiseParser(advertise);
    }

    @Test
    public void testBoardAddress() throws Exception{
        byte advertise[] = new byte[]{
                13,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00,(byte)0xEF, (byte)0xBE, (byte)0x00, (byte)0xAD, (byte)0xDE, (byte)0x02
        };
        BleAdvertiseParser parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals("EF:BE:00:AD:DE:02",parser.getAddress());
    }

    @Test
    public void testBoardAddressAbsent() throws Exception{
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00
        };
        BleAdvertiseParser parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals(null,parser.getAddress());
    }

    @Test
    public void testVendorField() throws Exception{
        byte advertise[] = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };
        BleAdvertiseParser parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals(0,parser.getFeatureMap());
        Assert.assertEquals(Node.Type.NUCLEO,parser.getBoardType());
        Assert.assertEquals((byte)0x80,parser.getDeviceId());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x00
        };
        parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals(0xFF0000,parser.getFeatureMap());
        Assert.assertEquals( Node.Type.GENERIC,parser.getBoardType());
        Assert.assertEquals(0x00,parser.getDeviceId());

        advertise = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x01, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00
        };
        parser = new BleAdvertiseParser(advertise);
        Assert.assertEquals(0xFFFF0000,parser.getFeatureMap());
        Assert.assertEquals(Node.Type.STEVAL_WESU1,parser.getBoardType() );
        Assert.assertEquals(0x01,parser.getDeviceId());

    }

    @Test(expected= InvalidBleAdvertiseFormat.class)
    public void testVendorFieldSmallSize() throws Exception {
        byte[] advertise = new byte[]{
                5,(byte) 0xFF, (byte) 0xEF, (byte) 0xBE, (byte) 0x00, (byte) 0xAD,
                0x1B, (byte) 0xEF, (byte) 0xBE, (byte) 0x00, (byte) 0xAD //fake datas
        };
        new BleAdvertiseParser(advertise);
    }

    @Test(expected= InvalidBleAdvertiseFormat.class)
    public void testVendorFieldBigSize() throws Exception {
        byte[] advertise = new byte[]{
                7, (byte)0xFF, (byte) 0xEF, (byte) 0xBE, (byte) 0x00, (byte) 0xAD, (byte) 0xDE,
                (byte) 0x02,(byte) 0x02
        };
        new BleAdvertiseParser(advertise);
    }

    @Test(expected= InvalidBleAdvertiseFormat.class)
    public void testEmptyAdvertise() throws Exception {
        new BleAdvertiseParser(new byte[]{});
    }

    @Test(expected= InvalidBleAdvertiseFormat.class)
    public void testInvalidNodeType() throws Exception {
        byte advertise[]  = new byte[]{
                7,(byte)0xFF,(byte)0x01,(byte)0x10, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00
        };
        new BleAdvertiseParser(advertise);
    }

    @Test(expected= InvalidBleAdvertiseFormat.class)
    public void testNoVendorSpecific() throws Exception {
        byte advertise[]  = new byte[]{
                //vendor specific is 0xff we have 0xfe
                7,(byte)0xFE,(byte)0x01,(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00,
                6,0x09,'h','e','l','l','o'
        };
        new BleAdvertiseParser(advertise);
    }
}