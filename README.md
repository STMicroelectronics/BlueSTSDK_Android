# BlueST SDK

BlueST is a multi-platform library (Android and iOS supported) that permits easy access to the data exported by a Bluetooth Low Energy (BLE) device that implements the BlueST protocol.

## BlueST Protocol

### Advertise
The library will show only the device that has a vendor-specific field formatted in the following way:

|Length|  1       |1           | 1                |1          | 4              | 6        |
|------|----------|------------|------------------|-----------|----------------|----------|
|Name  | Length   | Field Type | Protocol Version | Device Id | Feature Mask   | Device MAC (optional)|
|Value | 0x07/0xD | 0xFF       | 0x01             | 0xXX      | 0xXXXXXXXX | 0xXXXXXXXXXXXX|


 - The Field Length must be 7 or 13 bytes long.
 
 - The Device Id is a number that identifies the type of device. It is used to select different types of feature mask and can manage more than 32 features.
Currently used values are:
    - 0x00 for a generic device
    - 0x01 is reserved for the [STEVAL-WESU1](http://www.st.com/en/evaluation-tools/steval-wesu1.html) board
    - 0x02 is reserved for the [STEVAL-STLKT01V1 (SensorTile)](http://www.st.com/content/st_com/en/products/evaluation-tools/solution-evaluation-tools/sensor-solution-eval-boards/steval-stlkt01v1.html) board
    - 0x03 is reserved for the [STEVAL-BCNKT01V1 (BlueCoin)](http://www.st.com/content/st_com/en/products/evaluation-tools/solution-evaluation-tools/sensor-solution-eval-boards/steval-bcnkt01v1.html) board
    - 0x04 is reserved for the [STEVAL-IDB008V1/2 (BlueNRG-2)](http://www.st.com/content/st_com/en/products/evaluation-tools/solution-evaluation-tools/communication-and-connectivity-solution-eval-boards/steval-idb008v2.html) board
    - 0x80 for a generic Nucleo board
    - 0x81 for a Nucleo board exporting remote feature

  You should use a value between 0x04 and 0x7F for your custom board, as values between 0x80 and 0xFF are reserved for ST Nucleo boards.
 
 - The feature mask is a bit field that provides information regarding what characteristics/features are exported by the board.
Currently, bits are mapped in the following way:
  
   |Bit|31|30|29|28|27|26|25|24|23|22|21|20|19|18|17|16|
   |:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
   |Feature|RFU|ADPCM Sync|Switch|Direction of arrival|ADPC Audio|MicLevel|Proximity|Lux|Acc|Gyro|Mag|Pressure|Humidity|Temperature|Battery|Second Temperature|
   
   |Bit|15|14|13|12|11|10|9|8|7|6|5|4|3|2|1|0|
   |:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
   |Feature|CO Sensor|RFU|RFU|SD Logging|Beam forming|AccEvent|FreeFall|Sensor Fusion Compact|Sensor Fusion|Motion intensity|Compass|Activity|Carry Position|ProximityGesture|MemsGesture|Pedometer|
You can use one of the RFU bits or define a new device and decide how to map the feature. 
To see how the data is exported by pre-defined features, consult the export method [<code> Feature.ExtractResult Feature.extractData(long,byte[],int)</code>](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Feature.html#extractData-long-byte:A-int-).  within the feature class definition.


- The device MAC address is optional and useful only for obtaining the device MAC address on an iOS device.


### Characteristics/Features
 The characteristics managed by the SDK must have a UUID such as: <code>*XXXXXXXX*-0001-11e1-ac36-0002a5d5c51b</code>.
 The SDK will scan all the services, searching for characteristics that match that pattern. 
 
 The first part of the UUID will have the bit set to 1 for each feature exported by the characteristics.
 
 In case of multiple features mapped in a single characteristic, the data must be in the same order as the bit mask.
 
 The characteristic data format must be:
 
| Length |     2     |         >1         |          >1         |       |
|:------:|:---------:|:------------------:|:-------------------:|:-----:|
|  Name  | Timestamp | First Feature Data | Second Feature Data | ..... |
  
 The first 2 bytes are used to communicate a time stamp. This is especially useful for recognizing any data loss.
 
 Since the BLE packet max length is 20 bytes, the max size for a feature data field is 18 bytes.
 
#### Remote Feature
This type of Feature are created for handle the case when the node collect information from 
other boards the user want to know also how produced the data.

For this type of feature a node ID is attach at the beginning of a standard feature update message.

For this type of feature the characteristic data format must be:
 
| Length |     2     |        2         |      >1       |       |
|:------:|:---------:|:----------------:|:-------------:|:-----:|
|  Name  |  NodeID   | Remote timestamp | Feature Data  | ..... |


### Special Services
#### [Debug](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Debug.html)
If available, the debug service must have the UUID <code>00000000-000E-11e1-9ab4-0002a5d5c51b</code> and will contains 2 characteristics:

- <code>00000001-000E-11e1-ac36-0002a5d5c51b</code> (Notify/Write) is used to send string commands to the board and to notify the user of the result.
- <code>00000002-000E-11e1-ac36-0002a5d5c51b</code> (Notify) is used by the board to notify the user of an error message.

#### Configuration
If available, the configuration service must have the UUID <code>00000000-000F-11e1-9ab4-0002a5d5c51b</code> and will contain 2 characteristics:

- <code>00000002-000F-11e1-ac36-0002a5d5c51b</code> (Notify/Write): it can be used to send command/data to a specific feature.

    The request message must have the following format:
    
    | Length |             4            |    1    | 0-15         |
    |:------:|:------------------------:|:-------:|--------------|
    |  Name  | Destination Feature Mask | Command Id | Command Data |
  
    Where the first 4 bytes will select the recipient of the command/data package.
  
    The optional command answer must have the following format:
    
    | Length |     2     |          4          |      1     |     0-13    |
    |:------:|:---------:|:-------------------:|:----------:|:-----------:|
    |  Name  | Timestamp | Sender Feature Mask | Command Id | Answer Data |
    
  From the SDK point of view the messages are sent using the method [Feature.sendCommand](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Feature.html#sendCommand-byte-byte:A-) and the answer is notified with a callback passed through the method [Feature.parseCommandResponse](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Feature.html#parseCommandResponse-int-byte-byte:A-).

  If this characteristic does not exist, but the characteristics that export the feature is in 
  write mode, the *command id* and the *command data* are sending directly to the feature 
  characteristics. In this case is not possible answer to the command.

- <code>00000001-000F-11e1-ac36-0002a5d5c51b</code> (Read/Write/Notify): if available it is used to access the board configuration register that can be modified using the [ConfigControl](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/ConfigControl.html) class.


### Example
The SDK is compatible with different ST firmware as:
 - [FP-SNS-MOTENV1](http://www.st.com/content/st_com/en/products/embedded-software/mcus-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-sns-motenv1.html): STM32 ODE function pack for IoT node with BLE connectivity and environmental and motion sensors
 - [FP-SNS-ALLMEMS1](http://www.st.com/content/st_com/en/products/embedded-software/mcus-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-sns-allmems1.html): STM32 ODE function pack for IoT node with BLE connectivity, digital microphone, environmental and motion sensors
 - [FP-SNS-FLIGHT1](http://www.st.com/content/st_com/en/products/embedded-software/mcus-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-sns-flight1.html): STM32 ODE function pack for IoT node with NFC, BLE connectivity and environmental, motion and time-of-flight sensors
 - [FP-NET-BLESTAR1](http://www.st.com/content/st_com/en/products/embedded-software/mcus-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-net-blestar1.html): STM32 ODE function pack for creating a BLE star network connected via Wi-Fi to IBM Watson IoT cloud

And it is used in different application as:
 - [ST BlueMS](https://github.com/STMicroelectronics-CentralLabs/STBlueMS_Android)
 - [ST SensNet](https://github.com/STMicroelectronics-CentralLabs/STSensNet_Android)

## How to install the library
### As an external library
1. Clone the repository
2. Add the BlueSTSDK directory as a submodule of your project: File->Import Module

### As a git submodule
1. Add the repository as a submodule:
  
  ```Shell
  $ git submodule add https://github.com/STMicroelectronics-CentralLabs/BlueSTSDK_Android.git BlueSTSDK
  ```
2. Add the SDK as a project submodule in the *settings.gradle* file, adding the line:
<pre>include ':BlueSTSDK:BlueSTSDK'</pre>

## Main library actors

### [Manager](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Manager.html)
This is a singleton class that starts/stops the discovery process and stores the retrieved nodes.
Before starting the scanning process, it is also possible to define a new deviceId and to register/add new features to already-defined devices

The Manager will notify a node discovery through the [<code>Manager.ManagerListener</code>](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Manager.ManagerListener.html) class.
Note that each callback is performed asynchronously by a background thread.

### [Node](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Node.html)
This class represents a remote device.

From this class you can recover what features are exported by a node and read/write data from/to the device.
The node will export all the features that are set to 1 in the advertise message. Once the device is connected, scanning and enabling of available characteristics are performed. At this point it is possible to request/send data related to the discovered features.

A node notifies its RSSI (signal strength) through the [<code>Node.BleConnectionParamUpdateListener</code>](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Node.BleConnectionParamUpdateListener.html) class.
A node notifies any change of its state through the [<code>Node.NodeStateListener</code>](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Node.NodeStateListener.html) class.

A node can be in one of following states:
- **Idle**: the node is waiting for a connection and sending an advertise message
- **Connecting**: a connection with the node was triggered, the node is performing the discovery of device services/characteristics
- **Connected**: connection with the node was successful. Note: this status can be fired twice if a secure connection with BLE pairing was performed
- **Disconnecting**: ongoing disconnection, once disconnected the node goes back to the idle state
- **Lost**: the device sent an advertise, however currently it is not reachable
- **Unreachable**: we were connected with the node, however we lost the connection

Note that each callback is performed asynchronously by a background thread.


### [Feature](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Feature.html)
This class represents data exported by the node.

Each Feature has an array of  [<code>Field</code>](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Features/Field.html) that describes the data exported.

Data are received from a BLE characteristic and contained in a class  [<code>Feature.Sample</code>](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Feature.Sample.html). The user is notified of data using a listener pattern.

The data exported by the Sample can be extracted using the static utility methods of the class.

Note that each callback is performed asynchronously by a background thread.

Available features can be retrieved from [Features package](https://stmicroelectronics-centrallabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Features/package-summary.html).

#### How to add a new Feature

 1. Extend the class Feature: 
    1.	Create an array of [<code>Feature.Field</code>](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Features/Field.html) that will describe the data exported by the new feature
    2.	Create a constructor that accepts only the node as a parameter. From this constructor call the [super constructor](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Feature.html#Feature-java.lang.String-com.st.BlueSTSDK.Node-com.st.BlueSTSDK.Features.Field:A-), passing the feature name and the feature field.
    3.  Implement the method [<code> Feature.ExtractResult Feature.extractData(long,byte[],int)</code>](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Feature.html#extractData-long-byte:A-int-). 
    4.  Create a utility static method that extracts the data from the Feature.Sample class 
 2. If you want to use the advertise feature bitmask and our characteristics UUID format, before
    start the scanning register the new feature:
 
    ```Java
    // add the feature to the Nucleo device
    byte deviceId = (byte) 0x80;
    SparseArray<Class<? extends Feature>> temp = new SparseArray<>();
    // the feature will be mapped in the characteristic 
    // 0x10000000-0001-11e1-ac36-0002a5d5c51b
    temp.append(0x10000000,MyNewFeature.class);
    try {
        Manager.addFeatureToNode(deviceId,temp);
    } catch (InvalidFeatureBitMaskException e) {
    	e.printStackTrace();
    }
    ```

    Otherwise you can register the characteristics before call the connect method:
    ```Java
    Node node=...
    UUIDToFeatureMap map = new UUIDToFeatureMap();
    map.put(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"), FeatureHeartRate.class);
    node.addExternalCharacteristics(map)
    ```

## Log
The SDK defines some class that will log the feature data.
Using the class [FeatureLogCSVFile](https://stmicroelectronics-centrallabs.github.io/BlueSTSDK_Android/javadoc/com/st/BlueSTSDK/Log/FeatureLogCSVFile.html) each feature will have its file, and the data logged will be:
- Node address (on Android) or name (on iOS)
- Timestamp, the message id
- RawData, the data received by the feature extractData method
- one colunm for each data extracted by the feature

## Docs
You can find the documentation at this link: [JavaDoc](https://stmicroelectronics-centralLabs.github.io/BlueSTSDK_Android/javadoc)



## License
COPYRIGHT(c) 2015 STMicroelectronics

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
   1. Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
   3. Neither the name of STMicroelectronics nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

