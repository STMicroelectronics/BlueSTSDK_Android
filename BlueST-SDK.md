# BlueST SDK v2

BlueST is a multi-platform library (Android and iOS supported) that permits easy access to the data
exported by a Bluetooth Low Energy (BLE) device that implements the BlueST protocol.

## BlueST Protocol

### Advertise

The library will show only the device that has a vendor-specific field formatted in the following
way:

|Length (Byte) | 8   | 1   |1  |2   |1    |1   | 1   | 3   | 6   |
|---|---|---|---|---|---|---|---|---|---|
| Name   | `Board Name`  | `Length` | `Advertising Type` | `Manufacter ID` | `SDK Version` | `Device ID`   | `Firwmare ID` | `Option bytes` | `Device MAC` (optional)|
| Value  | 0x09XXXXXXXXXXXXXX | 0x09/0x0F | 0xFF | 0x30 + 0x00 (STM)| 0x02 | 0xXX  | 0xXX   | 0xXXXXXX  | 0xXXXXXXXXXXXX   |

- The **Board Name** field indicates the complete name of the board.

- The **Length** field indicates the length of the succeeding fields in bytes. This field can only
  take two values:
    - 0x09 (9 Bytes) if there isn't Device MAC field
    - 0x0F (15 Bytes) if there is Device MAC field

- The **Advertising Type** is always 0xFF and indicates the use of Manufacturer Specific Data.
  Manufacturer specific data can be used to add any custom data into advertising packets, using any
  format that is suitable for the application.


- The **Manufacturer ID** field indicates the Manufacturer ID of STMicroelectronics which
  corresponds to 0x0030. This field will have a length of 2 Bytes and will be located after the "
  Length" field.

- The **SDK Version** indicates the version of SDK protocol that is in use. Now is 0x02.

- The **Device ID** is a number that identifies the type of device.
  Currently used values are:
    - 0x00 for a generic device
    - 0x01 is reserved for
      the [STEVAL-WESU1](http://www.st.com/en/evaluation-tools/steval-wesu1.html) board
    - 0x02 is reserved for
      the [STEVAL-STLKT01V1 (SensorTile)](http://www.st.com/content/st_com/en/products/evaluation-tools/solution-evaluation-tools/sensor-solution-eval-boards/steval-stlkt01v1.html)
      board
    - 0x03 is reserved for
      the [STEVAL-BCNKT01V1 (BlueCoin)](http://www.st.com/content/st_com/en/products/evaluation-tools/solution-evaluation-tools/sensor-solution-eval-boards/steval-bcnkt01v1.html)
      board
    - 0x04 is reserved for
      the [STEVAL-IDB008V1/2 (BlueNRG-2)](http://www.st.com/content/st_com/en/products/evaluation-tools/solution-evaluation-tools/communication-and-connectivity-solution-eval-boards/steval-idb008v2.html)
      board
    - 0x05 is reserved for
      the [STEVAL-BCN002V1B (BlueNRG-Tile)](https://www.st.com/content/st_com/en/products/evaluation-tools/solution-evaluation-tools/sensor-solution-eval-boards/steval-bcn002v1b.html)
      board
    - 0x06 is reserved for
      the [STEVAL-MKSBOX1V1 (SensorTile.Box )](https://www.st.com/sensortilebox) board
    - 0x07 is reserved for
      the [B-L475E-IOT01A (IoT Node)] (https://www.st.com/en/evaluation-tools/b-l475e-iot01a.html)
      board
    - 0x08 is reserved for
      the [STEVAL-STWINKT1 (STWIN)] (https://www.st.com/en/evaluation-tools/steval-stwinkt1.html)
      board
    - 0x09 is reserved for
      the [STEVAL-STWINKT1B (STWIN1B)] (https://www.st.com/en/evaluation-tools/steval-stwinkt1b.html)
      board
    - 0x0A is reserved for
      the [B-L4S5I-IOT01A] (https://www.st.com/en/evaluation-tools/b-l4s5i-iot01a.html) board
    - 0x0B is reserved for
      the [B-U585I-IOT02A] (https://www.st.com/en/evaluation-tools/b-u585i-iot02a.html) board
    - 0x0C is reserved for
      the [ASTRA] (https://www.st.com/content/st_com/en/products/evaluation-tools/solution-evaluation-tools/communication-and-connectivity-solution-eval-boards/steval-astra1b.html)
      board
    - 0x0D is reserved for the [SensorTile.Box PRO] board
    - 0x0E is reserved for the [STWIN.Box] board
    - 0x0F is reserved for
      the [STEVAL-PROTEUS] (https://www.st.com/en/evaluation-tools/steval-proteus1.html) board
    - 0x10 is reserved for the [STSYS-SBU06] board
    - 0x11 to 0x7B is reserved for ST Boards.
    - 0x7C is reserved for
      the [Nucleo STM32F446RE] (https://www.st.com/en/evaluation-tools/nucleo-f446re.html) board
    - 0x7D is reserved for
      the [Nucleo STM32L053R8] (https://www.st.com/en/evaluation-tools/nucleo-l053r8.html) board
    - 0x7E is reserved for
      the [Nucleo STM32L476RG] (https://www.st.com/en/evaluation-tools/nucleo-l476rg.html) board
    - 0x7F is reserved for
      the [Nucleo STM32F401RE] (https://www.st.com/en/evaluation-tools/nucleo-f401re.html) board
    - 0x80 is reserved for the [Nucleo] board with expansions stack
    - 0x81 to 0x8A is reserved for the [WB] boards
    - 0x86 is reserved for the [WB] board in FOTA mode
    - 0x8B to 0x8F is reserved for ST boards

You should use a value between 0xF0 and 0xFF for your custom board.

- The **Firmware ID** is a byte that uniquely identifies the firmware that runs on a specific board.
  Since the values 0x00 and 0xFF are reserved, up to 254 different firmware types can be supported
  on any single board.
  The board/firmware capabilities are uniquely identified by the Device ID / Firmware ID
  combination. The client that needs to determine capabilities of the discovered board needs to use
  a lookup table where an extensive description of the firmware features can be found. This table
  will be in JSON format and can be obtained by the client using the procedure described later in
  this document.
  The meaning of the three bytes that follow the “Firmware ID” field in the advertisement frame
  depend on the firmware ID value.

|Firmware ID   | Usage | Comment     |
|---|---|---|
| `0x00` | Reserved      | Used to extend the number of <br />firmware versions beyond 254 |
| `0x01-0xFE` | Assigned to official firwmare releases |      |
| `0xFF` | Experimental firmware   | Not listed in official Database    |

The official database can be found at:
https://github.com/STMicroelectronics/appconfig
Looking the tag associated to BlueST-SDK version (example: V4.16.0 -> tag 4.16.0)

- There are at most three **option bytes** available, whose meaning can be defined by the firmware
  developer to convey different types of information. <br />The translation between each byte and
  its meaning is performed by the client, for example the mobile application, according to the
  mapping table defined in the JSON file.
  Each byte can be interpreted using one of the following options:
    - An **integer**, which can be adjusted using an offset and a scaling factor
    - A **string enumeration**, i.e. the byte is used as an index of a string array
    - An **icon enumeration**, i.e. the byte is used as an index of an array of icons

It should be noticed that option bytes are sent in the advertisement frame so they can be processed
and visualized by client without establishing a BLE connection with the target. This can be useful
to notify warning and alarms, to report status or specific measurements.  
A client can visualize information carried in custom option bytes received by multiple targets
simultaneously.

Integer format
An example of an integer custom byte being used to report the battery level is shown below.

```json
{ 

  "format": "int", 
  "name": "battery", 
  "negative_offset": 0, 
  "scale_factor": 10, 
  "type": "%" 
} 
```

String enumeration format

```json
{ 
  "format": "enum_string", 
  "name": "alarm", 
  "string_values": [ 
    { 
      "display_name": "No Alarm", 
      "value": 0 
    }, 
    { 
      "display_name": "Low Battery", 
      "value": 1 
    }, 
    { 
      "display_name": "SD Full", 
      "value": 2 
    } 
  ], 
  "type": "string" 
} 
```

Icon enumeration format

```json
{ 
    "format": "enum_icon", 
    "icon_values": [ 
      { 
        "comment": "icon for Log on going", 
        "icon_code": 1, 
        "value": 0 
      }, 
      { 
        "comment": "icon for low battery", 
        "icon_code": 4, 
        "value": 1 
      }, 
      { 
        "comment": "icon for SD full", 
        "icon_code": 10, 
        "value": 2 
      } 
    ], 
    "name": "status", 
    "type": "icon" 
} 
```

- The **Device MAC** field exposes the MAC ADDRESS of a device. This field is optional and useful
  only for obtaining the device MAC address on an iOS device.

For more information, see
the [user manual](https://www.st.com/resource/en/user_manual/dm00550659-getting-started-with-the-bluest-protocol-and-sdk-stmicroelectronics.pdf)
.

### Characteristics/Features

The SDK is searching in all the services the know characteristics.

- The features that have an UUID such as: <code>*XXXXXXXX*-0001-11e1-ac36-0002a5d5c51b</code>, and
  are called `Basic Feature` and a single bluetooth characteristics can export multiple of this
  basic features.
  The first 32bits are interpreted as the feature mask, if they are set to 1 it means that the
  characteristics is exporting the data of that feature.
  In case of multiple features mapped in a single characteristic, the data must be in the same order
  as the bit mask.
  The characteristic data format must be:

| Length (Byte) |     2     |         >1         |          >1         |       |
|:-------------:|:---------:|:------------------:|:-------------------:|:-----:|
|  Name         | Timestamp | First Feature Data | Second Feature Data | ..... |

The first 2 bytes are used to communicate a time stamp. This is especially useful for recognizing
any data loss.
Since the BLE packet max length is 20 bytes, the max size for a feature data field is 18 bytes.

- The other ST characteristics have the format: <code>*XXXXXXXX*-0002-11e1-ac36-0002a5d5c51b</code>
  and are called `Extended Feature`. This features could not be exported on a single bluetooth
  caracteristic

- The last ST characteristics have the format: <code>*XXXXXXXX*-0003-11e1-ac36-0002a5d5c51b</code>
  and are called `General Purpose Feature`. This features are not present inside the SDK but the
  Application is able to decode them parsing the Firmware model where there is a section that
  explains how this features must be read.
  For example

 ```json
 "characteristics": [
    {
    "name": "test_uint16",
    "uuid": "00000000-0003-11e1-ac36-0002a5d5c51b",
    "uuid_type": 3,
    "format_notify": [
      {
        "length": 2,
        "name": "timestamp"
      },
      {
        "length": 2,
        "name": "test_uint16",
        "type": "UInt16",
        "offset": 0.0,
        "scalefactor": 1.0,
        "min" : 100,
        "max" : 200,
        "unit": "m/s"
      }
    ]
  },
  {
    "name": "test_int32",
    "uuid": "00010000-0003-11e1-ac36-0002a5d5c51b",
    "uuid_type": 3,
    "format_notify": [
      {
        "length": 2,
        "name": "timestamp"
      },
      {
        "length": 4,
        "name": "test_int32",
        "type": "Int16",
        "offset": -100.0,
        "scalefactor": 0.02,
        "min" : -100,
        "max" : 300,
        "unit": "kg"
      }
    ]
  },
  {
    "name": "test_int8_uint32",
    "uuid": "00020000-0003-11e1-ac36-0002a5d5c51b",
    "uuid_type": 3,
    "format_notify": [
      {
        "length": 2,
        "name": "timestamp"
      },
      {
        "length": 1,
        "name": "test_int8",
        "type": "Int8",
        "offset": 16.0,
        "scalefactor": 10.0,
        "min":200,
        "unit": "km"
      },
      {
        "length": 4,
        "name": "test_uint32",
        "type": "UInt32",
        "offset": 100.0,
        "scalefactor": 100.0,
        "max": 10000
      }
    ]
  }
]
 ```

## Special Service: [Debug]

If available, the debug service must have the UUID <code>00000000-000E-11e1-9ab4-0002a5d5c51b</code>
and will contains 2 characteristics:

- <code>00000001-000E-11e1-ac36-0002a5d5c51b</code> (Notify/Write) is used to send string commands
  to the board and to notify the user of the result.
- <code>00000002-000E-11e1-ac36-0002a5d5c51b</code> (Notify) is used by the board to notify the user
  of an error message.

## Example

The SDK is compatible with different ST firmware as:

- [FP-SNS-ALLMEMS1](http://www.st.com/content/st_com/en/products/embedded-software/mcus-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-sns-allmems1.html):
  STM32 ODE function pack for IoT node with BLE connectivity, digital microphone, environmental and
  motion sensors
- [FP-ATR-BLE1](https://www.st.com/content/st_com/en/products/embedded-software/mcu-mpu-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-atr-ble1.html):
  STM32Cube function pack for asset tracking using BLE connectivity for the SensorTile.box wireless
  multi sensor development kit. You can monitor and log the sensor data and communicate with the aws
  dashboard using the ST Asset Tracking app.
- [FP-SNS-STBOX1](https://www.st.com/en/embedded-software/fp-sns-stbox1.html): STM32Cube function
  pack for the Pro Mode of the SensorTile.box wireless multi sensor development kit

And it is used in different application as:

- [ST BLE Sensor](https://github.com/STMicroelectronics/STBlueMS_Android)
- [ST BLE StarNet](https://github.com/STMicroelectronics/STSensNet_Android)
- [ST Asset Tacking](https://github.com/STMicroelectronics/STAssetTracking_Android)


