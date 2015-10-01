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
package com.st.BlueSTSDK;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.st.BlueSTSDK.Features.FeatureGenPurpose;
import com.st.BlueSTSDK.Utils.BLENodeDefines;
import com.st.BlueSTSDK.Utils.BleAdvertiseParser;
import com.st.BlueSTSDK.Utils.InvalidBleAdvertiseFormat;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/** Represent an object that can export some data (feature) using the ble connection
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 * */
public class Node{
    private static final String TAG = Node.class.getCanonicalName();
    /**
     * wait this time before retry to send a command to the ble api
     */
    private static final long RETRY_COMMAND_DELAY_MS =100;

    /** possible node working mode, if a node is build the board is in Application mode */
    public enum Mode {
        /** wait for an FW update throughout usb*/
        USB_DFU,
        /** wait for an FW update throughout ble */
        OTA_BLE_DFU,
        /** application FW is running */
        Application
    }//Mode enum

	/** node type, board that will answer to the connection */
    public enum Type {
        /** unknown board type */
        GENERIC,
        /** STEVAL-WESU1 board */
        STEVAL_WESU1,
        /** board based on a x NUCLEO board */
        NUCLEO
    }//Type

    /** state of the node */
    public enum State {
        /** dummy initial state */
        Init,
        /** the node is waiting for a connection, it is sending advertise message*/
        Idle,
        /** we open a connection with the node */
        Connecting,
        /** we are connected with the node, this status can be fired 2 times if we do a secure
         * connection using bt pairing */
        Connected,
        /** we are closing the node connection */
        Disconnecting,
        /** we saw the advertise message for some time but now we do not receive anymore*/
        Lost,
        /** we were connected with the node but now it is disappear without
         disconnecting */
        Unreachable,
        /** dummy final state */
        Dead
    }//State

    /**
     * Interface used for notify change on the ble connection
     * @author STMicroelectronics - Central Labs.
     */
    public interface BleConnectionParamUpdateListener {

        /**
         * method call when have new information about the rssi value
         * @param node node that update its rssi value
         * @param newRSSIValue new rssi
         */
        void onRSSIChanged(Node node,int newRSSIValue);

        /**
         * method call when we have new information about the tx power
         * @param node node that update its tx power
         * @param newPower  new transmission power
         */
        @SuppressWarnings("UnusedDeclaration")
        void onTxPowerChange(Node node, int newPower);
    }//BleConnectionParamUpdateListener

    /**
     * Interface where notify that the node change its internal state
     * @author STMicroelectronics - Central Labs.
     */
    public interface NodeStateListener {

        /**
         * function call when one node change its status
         * @param node note that change its status
         * @param newState new node status
         * @param prevState previous node status
         */
        void onStateChange(Node node, State newState, State prevState);
    }//NodeStateListener


    /**
     * test if a characteristics can be read
     * @param characteristic characteristic to read
     * @return true if we can read it
     */
    private static boolean charCanBeRead(BluetoothGattCharacteristic characteristic){
        return (characteristic.getProperties() &
                BluetoothGattCharacteristic.PROPERTY_READ )!=0;
    }//charCanBeRead

    /**
     *
     * test if a characteristics can be write
     * @param characteristic characteristic to write
     * @return true if we can write it
     */
    private static boolean charCanBeWrite(BluetoothGattCharacteristic characteristic){
        return (characteristic.getProperties() &
                (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE |
                        BluetoothGattCharacteristic.PROPERTY_WRITE))!=0;
    }//charCanBeWrite

    /** callback method to use when we send ble commands */
    private class GattNodeConnection extends BluetoothGattCallback {

        /** number of time that the ts has reset, the timestamp is reseted when we receive a ts with
         * a  value > 65435 and than a package with a smaller value */
        private int mNReset=0;
        /** last raw ts received from the board, it is a number between 0 and 2^16-1 */
        private int mLastTs=0;

        /**
         * if we are connecting it start to scan the device service/characteristics otherwise it
         * change the node status to idle or unreachable.
         * if there is an error the node status go to dead
         * @param gatt connection with the device
         * @param status command status
         * @param newState new node status
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
            Log.d(TAG,"Node: "+Node.this.getName()+" Status: "+status+" newState: " +
                    ""+newState+" boundState:"+mDevice.getBondState());
            if(status==BluetoothGatt.GATT_SUCCESS){
                if(newState==BluetoothGatt.STATE_CONNECTED){
                    mBleThread.post(mScanServicesTask);
                }else if (newState == BluetoothGatt.STATE_DISCONNECTED){
                    if(mConnection!=null) {
                        mConnection.close();
                        mConnection = null;
                    }//if
                    if (mUserAskToDisconnect){
                        //disconnect completed
                        Node.this.updateNodeStatus(State.Idle);
                    }else{
                        //we disconnect but the user didn't ask it
                        Node.this.updateNodeStatus(State.Unreachable);
                    }//if else
                }//if-else
            }else{
                Node.this.updateNodeStatus(State.Dead);
                if(mConnection!=null) {
                    mConnection.close();
                    mConnection = null;
                }//if
            }//if status
        }//onConnectionStateChange


        /**
         * update the node rssi information
         * @param gatt connection
         * @param rssi rssi with the device
         * @param status true if the read is successfully
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status){
            if(status == BluetoothGatt.GATT_SUCCESS){
                Node.this.updateRssi(rssi);
            }else{
                Log.e(TAG,"Impossible retrieve the rssi value");
            }//if-else
        }//onReadRemoteRssi

        /**
         * filter the list of all the service for keep only the known services
         * @param allServices list of all the service returned by the node
         * @return list of services handle by the sdk
         * @see com.st.BlueSTSDK.Utils.BLENodeDefines.Services
         */
        private List<BluetoothGattService> filterKnowServices(List<BluetoothGattService>
                                                                     allServices){
            List<BluetoothGattService> knowServices = new ArrayList<>(allServices.size());
            for(BluetoothGattService service : allServices){
                if(BLENodeDefines.Services.isKnowService(service.getUuid())){
                    knowServices.add(service);
                }//if
            }//for
            return knowServices;
        }//filterKnowServices

        /**
         * if present we build the debug service for be able to send/receive the debug information
         * @param connection device connection
         * @param debugService debug service
         * @return object that we can use for communicate with the debug console,
         * can be null if we didn't find all the needed characteristics
         */
        private Debug buildDebugService(BluetoothGatt connection,BluetoothGattService debugService){
            List<BluetoothGattCharacteristic> charList = debugService.getCharacteristics();
            BluetoothGattCharacteristic term=null,err=null;
            //search the term and err characteristics
            for(BluetoothGattCharacteristic temp : charList){
                if(temp.getUuid().equals(BLENodeDefines.Services.Debug.DEBUG_TERM_UUID))
                    term=temp;
                else if(temp.getUuid().equals(BLENodeDefines.Services.Debug.DEBUG_STDERR_UUID))
                    err=temp;
            }//for
            //if both are present we build the object
            if(term!=null && err!=null)
                return new Debug(Node.this,connection,term,err);
            else
                return null;
        }//buildDebugService

        /**
         * build and add the exported features from a ble characteristics
         * @param characteristic characteristics that is handle by the sdk
         */
        private void buildFeature(BluetoothGattCharacteristic characteristic){
            //extract the part of the uuid that contains the feature inside this
            // characteristics
            int featureMask = BLENodeDefines.FeatureCharacteristics.extractFeatureMask
                    (characteristic.getUuid());
            List<Feature> temp = new ArrayList<>();

            //we do the search in reverse order for have the feature in he correct order in case
            //of characteristics that export multiple feature

            long mask= 1L<<31; //1<<31
            //we test all the 32bit of the feature mask
            for(int i=0; i<32; i++ ) {
                if ((featureMask & mask) != 0) { //if the bit is up
                    Feature f =mMaskToFeature.get((int)mask);
                    if (f != null) {
                        f.setEnable(true);
                        temp.add(f);
                    }
                }
                mask = mask>>1;
            }//for

            //if it is a valid characteristics, we add it on the map
            if(temp.size()!=0){
                mCharFeatureMap.put(characteristic,temp);
            }//if
        }//buildFeature

        /**
         * build a generic feature from a compatible characteristics
         * @param characteristic characteristics that export the data
         */
        private void buildGenericFeature(BluetoothGattCharacteristic characteristic){
            Feature f = new FeatureGenPurpose(Node.this,characteristic);
            f.setEnable(true);
            mAvailableFeature.add(f);
            List<Feature> temp = new ArrayList<>(1);
            temp.add(f);
            mCharFeatureMap.put(characteristic, temp);
        }//buildGenericFeature

        /**
         * scan all the service searching for know characteristics + enable the found feature
         * @param gatt connection with the device
         * @param status operation result
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            Log.d(TAG,"onServicesDiscovered status:"+status+" boundState:" + mDevice.getBondState());

            if(status == BluetoothGatt.GATT_FAILURE) {
                Log.e(TAG, "onServicesDiscovered: Error discovering Service list");
                Node.this.updateNodeStatus(State.Dead);
                return;
            }//if

            //for avoid to update the data after that we connect
            //we can have an auto connect -> return if the previous status is unreachable
            if(Node.this.mState!=State.Connecting && Node.this.mState!=State.Unreachable)
                return;

            //List<BluetoothGattService> knowServices = filterKnowServices(gatt.getServices());
            List<BluetoothGattService> nodeServices = gatt.getServices();
            if(nodeServices.size()==0) { // the list is empty -> exit
                //if we are bonded or we don't need to be bounded there is a real problem
                //otherwise we simply wait the next call without notify an error
                if(mDevice.getBondState()!=BluetoothDevice.BOND_BONDING) {
                    Log.e(TAG, "onServicesDiscovered: Empty Service list");
                    Node.this.updateNodeStatus(State.Dead);
                }//if
                return;
            }//if

            mCharFeatureMap.clear();
            for(BluetoothGattService service : nodeServices){
                //check if it is a specific service
                if(service.getUuid().equals(BLENodeDefines.Services.Debug.DEBUG_SERVICE_UUID))
                    mDebugConsole = buildDebugService(gatt,service);
                else if(service.getUuid().equals(BLENodeDefines.Services.Config.CONFIG_CONTROL_SERVICE_UUID)) {
                    List<BluetoothGattCharacteristic> controlChar = service.getCharacteristics();
                    //check for the initialization characteristics
                    for(BluetoothGattCharacteristic characteristic : controlChar) {
                        Log.d(TAG, "UUID :" +characteristic.getUuid() + " >>>>> "+ BLENodeDefines.Services.Config.REGISTERS_ACCESS_UUID);
                        if (characteristic.getUuid().equals(BLENodeDefines.Services.Config.FEATURE_COMMAND_UUID))
                            mInitialization = characteristic;
                        if (characteristic.getUuid().equals(BLENodeDefines.Services.Config.REGISTERS_ACCESS_UUID))
                            mConfigControl = new ConfigControl(Node.this, characteristic,mConnection);
                    }//for
                }else{ //otherwise will contains feature characteristics

                    for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                        UUID uuid = characteristic.getUuid();
                        if(BLENodeDefines.FeatureCharacteristics.isFeatureCharacteristics(uuid))
                            buildFeature(characteristic);
                        else if (BLENodeDefines.FeatureCharacteristics
                                .isGeneralPurposeCharacteristics(uuid)){
                            buildGenericFeature(characteristic);
                        }//if-else
                    }//for

                }//if-else
            }//for each service
            //we have collected all the data from the node, now we are fully connected
            if(mDevice.getBondState()==BluetoothDevice.BOND_BONDED)
                Node.this.updateNodeStatus(State.Connected);
            else
                Node.this.updateNodeStatus(State.Connected);
            /*
            //TODO: DEBUG REMOVE ME
            Log.d(TAG,"Know char:\n");
            for(BluetoothGattCharacteristic temp : mCharFeatureMap.keySet()){
                Log.d(TAG,"Add Char: "+temp.getUuid().toString()+"\n");
            }
            */
        }//onServicesDiscovered

        /**
         * update all the feature inside this characteristic
         * @param characteristic updated characteristic
         * @return true if the characteristic is associated to some feature
         */
        boolean updateFeature(BluetoothGattCharacteristic characteristic){
            List<Feature> features = mCharFeatureMap.get(characteristic);
            if(features!=null){
                byte data[] = characteristic.getValue();
                int timeStamp = NumberConversion.LittleEndian.bytesToUInt16(data);

                if(mLastTs>((1<<16)-100) && mLastTs > timeStamp)
                    mNReset++;
                mLastTs=timeStamp;
                timeStamp = mNReset * (1<<16) + timeStamp;

                int dataOffset =2;
                for(Feature f: features){
                    dataOffset += f.update(timeStamp,data,dataOffset);
                }//for features
                return true;
            }else
                return false;
            //if feature list != null
        }

        /**
         * send back to the feature the response of its command
         * @param characteristic characteristics that contain the response data
         */
        private void dispatchCommandResponseData(BluetoothGattCharacteristic characteristic){
            byte data[] = characteristic.getValue();
            int timeStamps = NumberConversion.LittleEndian.bytesToUInt16(data);
            int mask = NumberConversion.BigEndian.bytesToInt32(data,2);
            byte reqType= data[6];
            Feature f =mMaskToFeature.get(mask);
            if(f!=null)
                f.commandResponseReceived(timeStamps,reqType, Arrays.copyOfRange(data, 7,
                        data.length));
        }//dispatchCommandResponseData

        /**
         * receive the notification change
         * @param gatt connection with the device
         * @param characteristic updated characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //if it comes form the console service we send to it
            if(mDebugConsole!=null &&
                    BLENodeDefines.Services.Debug.isDebugCharacteristics(characteristic.getUuid()))
                mDebugConsole.receiveCharacteristicsUpdate(characteristic);
            else if(characteristic.equals(mInitialization)) //if it is the commandCharacteristics
                dispatchCommandResponseData(characteristic);
            else if (mConfigControl != null && characteristic.getUuid().equals(BLENodeDefines.Services.Config.REGISTERS_ACCESS_UUID))
                mConfigControl.characteristicsUpdate(characteristic);
            else //otherwise is a feature characteristics
                updateFeature(characteristic);
        }//onCharacteristicChanged

        /**
         * receive the data after a reading
         * @param gatt connection with the device
         * @param characteristic characteristics that contain the response data
         * @param status command status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            Log.d(TAG,"Read Char: "+characteristic.getUuid().toString());
            if(status == BluetoothGatt.GATT_SUCCESS) {
                if (mDebugConsole != null &&
                        BLENodeDefines.Services.Debug.isDebugCharacteristics(characteristic.getUuid()))
                    mDebugConsole.receiveCharacteristicsUpdate(characteristic);
                else if (mConfigControl != null && characteristic.getUuid().equals(BLENodeDefines.Services.Config.REGISTERS_ACCESS_UUID))
                    mConfigControl.characteristicsUpdate(characteristic);
                else
                    updateFeature(characteristic);
            }else{
                if(mDevice.getBondState()!=BluetoothDevice.BOND_BONDING) {
                    Log.e(TAG,"Error reading the characteristics: "+characteristic);
                    Node.this.updateNodeStatus(State.Dead);
                }//if
            }//if-else
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Log.d(TAG,"DescriptorWrite: "+status);
            //descriptor write -> remove from the queue
            if(status == BluetoothGatt.GATT_SUCCESS)
                dequeueWriteDesc(descriptor);
            else{
                if(mDevice.getBondState()!=BluetoothDevice.BOND_BONDING) {
                    Log.e(TAG,"onDescriptorWrite Error writing the descriptor: "+descriptor);
                    Node.this.updateNodeStatus(State.Dead);
                }//if
            }//if-else
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //Log.d(TAG,"Characteristics Write "+status);
            if(mDebugConsole!=null &&
                    BLENodeDefines.Services.Debug.isDebugCharacteristics(characteristic.getUuid()))
                mDebugConsole.receiveCharacteristicsWriteUpdate(characteristic, status == BluetoothGatt.GATT_SUCCESS);
            else if (mConfigControl != null && characteristic.getUuid().equals(BLENodeDefines.Services.Config.REGISTERS_ACCESS_UUID))
                mConfigControl.characteristicsWriteUpdate(characteristic, status == BluetoothGatt.GATT_SUCCESS);

        }
    }//GattNodeConnection

    /** device associated with this node */
    private BluetoothDevice mDevice;
    /** gatt connection with the device */
    private BluetoothGatt mConnection;
    /** characteristics used for send command to a feature */
    private BluetoothGattCharacteristic mInitialization =null;
    /** thread where we do all the ble commands */
    private Handler mBleThread;

    /**
     * task that will disconnect the device
     */
    private Runnable mDisconnectTask = new Runnable() {
        @Override
        public void run() {
            if(mState==State.Disconnecting) {
                mConnection.disconnect();
                //we stop the connection -> we have not notification enabled
                mNotifyFeature.clear();
            }// if
        } //run
    };

    /**
     * task for ask an update rssi
     */
    private Runnable mUpdateRssiTask = new Runnable() {
        @Override
        public void run() {
            if(mConnection!=null)
                mConnection.readRemoteRssi();
        }
    };

    /**
     * task for ask to discover the device service, if the call fail this command will auto
     * submit itself after {@code RETRY_COMMAND_DELAY_MS}
     */
    private Runnable mScanServicesTask = new Runnable() {
        @Override
        public void run() {
            if(mConnection!=null &&
               !mConnection.discoverServices())
                mBleThread.postDelayed(this, RETRY_COMMAND_DELAY_MS);
        }//run
    };

    /**
     * tell us if is the first time that we call connect, if true we refresh the device cache if ask
     */
    private boolean mIsFirstConnection=true;

    /**
     * true if the user ask to reset the device cache of this device
     */
    private boolean mResetCache=false;

    /**
     * context used for open the connection
     */
    private Context mContext;

    /**
     * invoke an hide method for clear the device cache, in this way we can have device with same
     * name and mac that export different service/char in different connection (maybe because we
     * are developing on it)
     * @param gatt connection with the device
     * @return tue il the call is invoke correctly
     */
    private static boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            Method localMethod = gatt.getClass().getMethod("refresh");
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(gatt));
                Log.d(TAG, "Refreshing Device Cache...");
                return bool;
            }//if
        } catch (Exception localException) {
            Log.e(TAG, "An exception occurred while refreshing device cache.");
        }//try-catch
        return false;
    }//refreshDeviceCache

    /**
     * task that open a connection with the remote device
     */
    private Runnable mConnectionTask = new Runnable() {
        @Override
        public void run() {
            mConnection = mDevice.connectGatt(mContext,false,new GattNodeConnection());
            if(mConnection==null){
                mBleThread.postDelayed(this, RETRY_COMMAND_DELAY_MS);
            } else if(mIsFirstConnection && mResetCache) {
                refreshDeviceCache(mConnection);
                mIsFirstConnection=false;
            }//
        }//run
    };


    /**
     * listener that receive the status update, it will subscribe the node to the command
     * characteristics for received the notification..
     */
    private NodeStateListener mNotifyCommandChange = new NodeStateListener() {

        @Override
        public void onStateChange(Node node, State newState, State prevState) {
            if(newState == State.Connected && mInitialization !=null) {
                mConnection.setCharacteristicNotification(mInitialization,true);
                BluetoothGattDescriptor descriptor = mInitialization.getDescriptor(NOTIFY_CHAR_DESC_UUID);
                if(descriptor!=null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    enqueueWriteDesc(descriptor);
                }//if
            }//if
            //error during connection (connecting ->dead or connected -> dead )
            if((prevState == State.Connecting || prevState== State.Connected ) && newState==State
                    .Dead){
                Log.e(TAG,"Error connecting to the node:"+node.getName());
                //we disconnect -> free the gatt resource and connect again
                if(mConnection!=null){ mConnection.close(); mConnection=null;}
                //we stop the connection -> we have not notification enabled
                mNotifyFeature.clear();
                //remove the pending write request
                synchronized (mWriteDescQueue) {
                    mWriteDescQueue.clear();
                }
            }
            if(newState==State.Dead  || newState==State.Disconnecting){
                if(mBoundStateChange !=null) {
                    //clean the broadcast receiver
                    mContext.getApplicationContext().unregisterReceiver(mBoundStateChange);
                    mBoundStateChange = null;
                }
            }
        }//onStateChange
    };

    /**
     * we have to serialize the descriptor write, -> create a queue
     * + use the enqueueWriteDesc/dequeueWriteDesc for insert element on it
     */
    final private Queue<BluetoothGattDescriptor> mWriteDescQueue = new LinkedList<>();

    /** node state */
    private State mState = State.Init;
    /** last node rssi */
    private int mRssi;
    /** last time that we update the rssi */
    private Date mLastRssiUpdate;
    /** class where notify a node status change, we use a CopyOnWriteArrayList for permit to have
    a one shot listener that remove itself after that it did its work */
    private final CopyOnWriteArrayList<NodeStateListener> mStatusListener = new CopyOnWriteArrayList<>();
    /** class where notify change on the ble connection state  we use a CopyOnWriteArrayList for permit to have
     a one shot listener that remove itself after that it did its work */
    private final CopyOnWriteArrayList<BleConnectionParamUpdateListener> mBleConnectionListeners = new
            CopyOnWriteArrayList<>();
    /** true when the user ask to disconnect form the device */
    private boolean mUserAskToDisconnect;
    /** class that contains the advertise information */
    private BleAdvertiseParser mAdvertise;

    /** list of all the feature that are available in the advertise */
    private ArrayList<Feature> mAvailableFeature;
    /** map that join the build feature with the bitmask that tell us that the feature is present*/
    private Map<Integer,Feature> mMaskToFeature;
    /**
     * map that tell us whit feature we can update when we receive an update from a characteristics
     */
    private Map<BluetoothGattCharacteristic,List<Feature>> mCharFeatureMap= new HashMap<>();

    /** set that contains the features that are in notify*/
    final private Set<Feature> mNotifyFeature=new HashSet<>();

    /**
     * object used write/read debug message to/from the ble device,
     * null if the device doesn't export the debug service   */
    private Debug mDebugConsole;

    /**
     * object used write/read Registers  to/from the ble device,
     * null if the device doesn't export the debug service   */
    private ConfigControl mConfigControl;


    /** ms to wait before declare a node as lost */
    private static long NODE_LOST_TIMEOUT_MS=2000;
    /** thread where wait the timeout */
    private Handler mHandler;  //NOTE: since the handler is crate in a thread,
    // is better check that the class is not null in the case we try to use it before the thread
    // got exec
    /** task to run when the timeout expire, it will set the node status as lost */
    private Runnable mSetNodeLost = new Runnable() {
        @Override
        public void run() {
            if(mState==State.Idle){
                updateNodeStatus(State.Lost);
            }//if
        }//run
    };

    /**
     * store the new rssi, and call the call back if needed
     * @param rssi new rssi to store
     */
    protected void updateRssi(int rssi){
        mRssi =rssi;
        mLastRssiUpdate = new Date(); // now
        if(mState==State.Lost)
            updateNodeStatus(State.Idle);
        for(BleConnectionParamUpdateListener listener : mBleConnectionListeners){
            listener.onRSSIChanged(this, rssi);
        }//for
    }//updateRssi

    /**
     * store the new node status and call the call back if needed
     * @param newStatus new node status
     */
    protected void updateNodeStatus(State newStatus){
        State old =mState;
        mState = newStatus;
        for (NodeStateListener listener : mStatusListener) {
            listener.onStateChange(this, newStatus, old);
        }//if
    }//updateNodeStatus

    /**
     * crate a feature from its class instance
     * @param featureClass class object that represent the feature to build
     * @param <T> type of feature to build
     * @return the feature or null if the class doesn't has a method that request a node as a
     * parameter
     */
    protected @Nullable <T extends Feature> T buildFeatureFromClass(Class<T> featureClass){
        try {
            Constructor<T> constructor = featureClass.getConstructor(Node.class);
            return constructor.newInstance(this);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }//try-catch
    }//buildFeatureFromClass

    /**
     * using the advertise data, we build a list a possible feature that this node can export
     */
    private void buildAvailableFeatures(){
        int featureMask = mAdvertise.getFeatureMap();
        SparseArray<Class<? extends Feature>> decoder = Manager.sFeatureMapDecoder.get(
                mAdvertise.getDeviceId());
        mMaskToFeature = new HashMap<>(32);
        mAvailableFeature = new ArrayList<>(32);
        if(decoder==null){ // unknown board type -> no feature
            return;
        }

        long mask=1;
        //we test all the 32bit of the feature mask
        for(int i=0; i<32; i++ ){
            if((featureMask & mask)!=0) { //if the bit is up
                Class<? extends Feature> featureClass = decoder.get((int)mask);
                if (featureClass != null) { //and the decoder has a name for that bit
                    Feature f = buildFeatureFromClass(featureClass);
                    if(f!=null){
                        mAvailableFeature.add(f);
                        mMaskToFeature.put((int)mask, f);
                    }else {
                        Log.e(TAG,"Impossible build the feature: "+featureClass.getSimpleName());
                    }//if-else
                }//if !=null
            }//if !=0
            mask = mask << 1;
        }//for
    }//buildAvailableFeatures

    /**
     * create a thread where run the handler that will contain the timeout for understand if the
     * node go out of scope
     */
    private void initHandler(){
        new Thread( new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler(); //create the handler
                //put the first item
                mHandler.postDelayed(mSetNodeLost,NODE_LOST_TIMEOUT_MS);
                Looper.loop(); //start exec the item in the queue
            }//run
        }).start(); //run the thread
    }//initHandler

    /**
     * create a new node
     * @param device android ble device
     * @param rssi rssi of the advertise message
     * @param advertise advertise message for this node
     * @throws  InvalidBleAdvertiseFormat if the advertise is not well formed
     */
    public Node(BluetoothDevice device,int rssi,byte advertise[]) throws InvalidBleAdvertiseFormat{
        mDevice = device;
        updateRssi(rssi);
        updateNodeStatus(State.Idle);
        initHandler();
        mAdvertise = new BleAdvertiseParser(advertise);
        buildAvailableFeatures();
        addNodeStateListener(mNotifyCommandChange);
        Log.i(TAG, mAdvertise.toString());
    }


    /**
     * create a new node from the advertise, the advertise have to contain the mac address
     * @param advertise advertise message for this node
     * @throws  InvalidBleAdvertiseFormat if the advertise is not well formed, for this method is
     * mandatory that the advertise contains also the mac address
     */
    public Node(byte advertise[]) throws InvalidBleAdvertiseFormat{
        mAdvertise = new BleAdvertiseParser(advertise);
        String bleAddress = mAdvertise.getAddress();
        if(bleAddress==null){
            throw  new InvalidBleAdvertiseFormat("Device Address non present in the advertise");
        }
        mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bleAddress);
        updateNodeStatus(State.Idle);
        initHandler();
        buildAvailableFeatures();
        addNodeStateListener(mNotifyCommandChange);
        Log.i(TAG,mAdvertise.toString());
    }

    /**
     * implement for have an api equal to the ios one, not use it, use the version with the context
     */
    public void connect(){
        throw new UnsupportedOperationException("The method is not implemented, " +
                "use Connect(Context) instead.");
    }//connect

    /**
     * open a gatt connection
     * @param c context to use for open the connection
     */
    public void connect(Context c){
        connect(c, false);
    }//connect

    /**
     * open a gatt connection
     * @param c context to use for open the connection
     * @param resetCache if true the handle cache for this device will be clear,
     *                   the connection will be slower, it will done only the first time that you
     *                   call this function with the parameter true
     */
    public void connect(Context c,boolean resetCache){
        //we start the connection so we will stop to receive advertise, so we delete the timeout
        if(mHandler!=null) mHandler.removeCallbacks(mSetNodeLost);
        mUserAskToDisconnect=false;
        updateNodeStatus(State.Connecting);

        mBleThread = new Handler(c.getMainLooper());
        mContext=c;
        setBoundListener(c.getApplicationContext());
        mResetCache=resetCache;
        mBleThread.post(mConnectionTask);
    }//connect

    private BroadcastReceiver mBoundStateChange = null;
    private void setBoundListener(Context c){
        if(mBoundStateChange !=null)
            return;
        mBoundStateChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,intent.getAction());
                Log.d(TAG,"OnReceive change:"+intent.getIntExtra(BluetoothDevice
                        .EXTRA_BOND_STATE,-1));
                if(intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,BluetoothDevice.BOND_NONE)
                        == BluetoothDevice.BOND_BONDED){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice
                            .EXTRA_DEVICE);
                    Node n = Manager.getSharedInstance().getNodeWithTag(device.getAddress());
                    if(n!=null && mState==State.Connected) // is one of our devices and is
                    // already connected
                        //mBleThread.post(mScanServicesTask);
                        updateNodeStatus(State.Connected);
                    //if
                }//if
            }//onReceive
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        c.registerReceiver(mBoundStateChange, filter);
    }

    /**
     * close the node connection
     */
    public void disconnect(){
        if(!isConnected())
            return;
        mUserAskToDisconnect=true;
        updateNodeStatus(State.Disconnecting);

        // run the waitCompleteAllWriteRequest in a different handler for avoid to block the
        // ble/main thread, we use the handle used for update the rssi, since during disconnection
        // is not used
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                waitCompleteAllDescriptorWriteRequest(mDisconnectTask);
                mHandler.postDelayed(mSetNodeLost, NODE_LOST_TIMEOUT_MS);
            }
        });
    }//disconnect

    /**
     * node type
     * @return node type
     * @see com.st.BlueSTSDK.Node.Type
     */
    public Type getType(){
        return mAdvertise.getBoardType();
    }//getType

    /**
     * tell if the node is connected
     * @return true if the internal state is equal to connected
     */
    public boolean isConnected(){
        return mState == State.Connected;
    }

    /**
     * return a unique identification for the node
     * @return return the ble mac address
     */
    public String getTag(){
        return mDevice.getAddress();
    }//getTag

    /**
     * return the internal state of the node
     * @return internal state
     * @see com.st.BlueSTSDK.Node.State
     */
    public State getState(){return  mState; }

    /**
     * get the device name
     * @return the device name
     */
    public String getName(){
        return mAdvertise.getName();
    }//getName

    /**
     * get the device Protocol Version
     * @return the device Protocol Version
     */
    public short getProtocolVersion(){
        return mAdvertise.getProtocolVersion();
    } //getProtocolVersion

    /**
     * get a list of feature that the node export in the advertise, when the node is connected we
     * will know the feature exported thought the characteristics, so we enable some of them.
     * @return  list of feature that the node can export
     */
    public List<Feature> getFeatures(){
        return java.util.Collections.unmodifiableList(mAvailableFeature);
    }

    /**
     * search for a specific feature
     * @param type type of feature that we want search
     * @param <T> class that will want search
     * @return the feature of type {@code type} exported by this node, or null if not present
     */
    @SuppressWarnings("unchecked")
    public @Nullable <T extends Feature> T getFeature(Class<T> type){
        for (Feature f : mAvailableFeature) {
            if (type.isInstance(f)) {
                return (T) f;
            }
        }// for list
        return null;
    }//getFeature

    /**
     * add the listener for the node status change
     * @param listener object where notify a node status change
     */
    public void addNodeStateListener(NodeStateListener listener){
        if (listener != null && !mStatusListener.contains(listener))
            mStatusListener.add(listener);
    }

    /**
     * remove the node state listener form the node
     * @param listener listener to remove
     */
    public void removeNodeStateListener(NodeStateListener listener){
        mStatusListener.remove(listener);
    }//removeNodeStateListener

    /**
     * find the the gattCharacteristics corresponding to a feature
     * @param feature feature to search
     * @return null if the feature is not handle by the node, the characteristics otherwise
     */
    private BluetoothGattCharacteristic getCorrespondingChar(Feature feature){
        ArrayList<BluetoothGattCharacteristic> candidateChar = new ArrayList<>();
        for (Map.Entry<BluetoothGattCharacteristic,List<Feature>> e: mCharFeatureMap.entrySet()){
            List<Feature> featureList = e.getValue();
            if(featureList.contains(feature)){
                candidateChar.add(e.getKey());
            }
        }//for entry
        if(candidateChar.isEmpty())
            return null;
        else if(candidateChar.size()==1){
            return candidateChar.get(0);
        }else{ //we have to select the feature that permit us to have more datas
            int maxNFeature=0;
            BluetoothGattCharacteristic bestChar=null;
            for(BluetoothGattCharacteristic characteristic: candidateChar){
                int nFeature = mCharFeatureMap.get(characteristic).size();
                if(nFeature>maxNFeature){
                    maxNFeature=nFeature;
                    bestChar=characteristic;
                }//if
            }//for
            return bestChar;
        }//if-else
    }



    /**
     * async request for read a feature, the new value will be notify thought the feature listener
     * @see Feature.FeatureListener
     * @param feature to read
     * @return false if the feature is not handle by this node or disabled
     */
    public boolean readFeature(Feature feature){
        if(!feature.isEnabled())
            return false;
        final BluetoothGattCharacteristic characteristic = getCorrespondingChar(feature);
        if(characteristic!=null){
            if(!charCanBeRead(characteristic))
                return false;
            //since we have to wait that the write description are done, we have to wait a thread -> we
            //can not run directly in the bleThread, so we use the handler for the rssi, since the
            // load on that thread will be low
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Runnable readChar = this;
                    if (mConnection != null && isConnected()) {
                        //wait that the queue is empty and write the characteristics, if an error
                        // happen we enqueue again the command
                        waitCompleteAllDescriptorWriteRequest(new Runnable() {
                            @Override
                            public void run() {
                                if (!mConnection.readCharacteristic(characteristic))
                                    mHandler.postDelayed(readChar, RETRY_COMMAND_DELAY_MS);
                            }//run
                        });
                    }//if
                }//run
            });

            return true;
        }else
            return false;
    }//readFeature

    /* standard descriptor id used for enable/disable the notification */
    private final static UUID NOTIFY_CHAR_DESC_UUID = UUID.fromString
            ("00002902-0000-1000-8000-00805f9b34fb");

    /**
     * wait until all the task inside the @{code mWriteDescQueue} aren't completed
     * don't call this method from the bleThread otherwise it will freeze the app!
     * @param runWhenEmpty code that this function will post on the bleThread when the queue is
     *                     empty
     */
    private void waitCompleteAllDescriptorWriteRequest(Runnable runWhenEmpty){
        synchronized (mWriteDescQueue){
            while(!mWriteDescQueue.isEmpty()) {
                try {
                    mWriteDescQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }//try-catch
            }//while
            if(runWhenEmpty!=null)
                mBleThread.post(runWhenEmpty);
        }//synchronized
    }//waitCompleteAllDescriptorWriteRequest

    /**
     * add a descriptor to the queue for write it
     * @param desc descriptor to write
     */
    private void enqueueWriteDesc(final BluetoothGattDescriptor desc){
        synchronized (mWriteDescQueue){
            mWriteDescQueue.add(desc);
            //if the queue contains only the element that we just add
            if(mWriteDescQueue.size()==1) {
                mBleThread.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mConnection!=null &&  (isConnected() || mState==State.Disconnecting)) {
                            if (!mConnection.writeDescriptor(desc))
                                mBleThread.postDelayed(this, RETRY_COMMAND_DELAY_MS);
                        }//if
                    }//run
                });
            }//if
        }//synchronized
    }

    /**
     * remove the descriptor from the queue and if present start a new write
     * @param desc descriptor that we finis to write
     */
    private void dequeueWriteDesc(BluetoothGattDescriptor desc){
        synchronized (mWriteDescQueue){
            mWriteDescQueue.remove(desc);
            //if there still element in the queue, start write the head
            if(!mWriteDescQueue.isEmpty()) {
                mBleThread.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mConnection!=null &&  (isConnected() || mState==State.Disconnecting)) {
                            if (!mConnection.writeDescriptor(mWriteDescQueue.peek()))
                                mBleThread.postDelayed(this, RETRY_COMMAND_DELAY_MS);
                        }
                    }
                });
            }else
                mWriteDescQueue.notifyAll();
        }//synchronized
    }

    /**
     * send a request for enable/disable the notification update on a specific characteristics
     * @param characteristic characteristics to notify
     * @param enable true if you want enable the notification, false if you want disable it
     * @return true if the request is correctly send, false otherwise
     */
    boolean changeNotificationStatus(BluetoothGattCharacteristic characteristic,
                                          boolean enable){
        if(characteristic!=null && mConnection!=null && isConnected()){
            mConnection.setCharacteristicNotification(characteristic,enable);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(NOTIFY_CHAR_DESC_UUID);
            if(descriptor==null)
                return false;
            if(enable)
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            else
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            enqueueWriteDesc(descriptor);
            return true;
        }else
            return false;
    }

    /**
     * unsubscribe the notification for the node update of the feature
     * @param feature feature that you want stop to be notify
     * @return false if the feature is not handle by this node or disabled
     */
    public boolean disableNotification(Feature feature){
        if(!feature.isEnabled() && feature.getParentNode()!=this)
            return false;
        mNotifyFeature.remove(feature);
       return changeNotificationStatus(getCorrespondingChar(feature),false);
    }//disableNotification

    /**
     * ask to the node to notify when the feature change its value
     * @param feature feature to look
     * @return false if the feature is not handle by this node or disabled
     */
    public boolean enableNotification(Feature feature){
        if(!feature.isEnabled() && feature.getParentNode()!=this)
            return false;
        mNotifyFeature.add(feature);
        return changeNotificationStatus(getCorrespondingChar(feature), true);
    }//enableNotification

    /**
     * tell if the user ask for receive notification update about this feature
     * @param feature feature that you want know if is in notification mode
     * @return true if the use call enableNotification on this feature
     */
    public boolean isEnableNotification(Feature feature){
        return mNotifyFeature.contains(feature);
    }


    /**
     * enqueue the write command for be execute in the bleThread and only if we already do all the
     * writeDescription request
     * @param writeMe characteristics that we have to write
     */
    private void writeCharacteristics(final BluetoothGattCharacteristic writeMe){
        //since we have to wait that the write description are done, we have to wait a thread -> we
        //can not run directly in the bleThread, so we use the handler for the rssi, since the load
        // on that thread will be low
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final Runnable writeChar = this;
                if(mConnection!=null &&  isConnected()) {
                    //wait that the queue is empty and write the characteristics, if an error
                    // happen we enqueue again the command
                    waitCompleteAllDescriptorWriteRequest(new Runnable() {
                        @Override
                        public void run() {
                            if (!mConnection.writeCharacteristic(writeMe)) {
                                mHandler.postDelayed(writeChar, RETRY_COMMAND_DELAY_MS);
                            }//if
                        }//run
                    });
                }//if
            }//run
        });
    }

    /**
     * write some data to a feature characteristics, this method is accessible only by the
     * feature that know how the data interpreted in the node.
     * @param feature feature that will receive the data
     * @param data data that we have to send to the feature
     * @return true if the message is send without problem, false otherwise
     */
    boolean writeFeatureData(Feature feature,byte data[]){
        final BluetoothGattCharacteristic characteristic = getCorrespondingChar(feature);
        if(characteristic==null)
            return false;
        //not enable or not exist or not in write mode -> return false
        if(!charCanBeWrite(characteristic) || !feature.isEnabled())
            return false;

        characteristic.setValue(data);

        writeCharacteristics(characteristic);

        return true;
    }//writeFeatureData

    /**
     * compare two nodes. two nodes are equals if the tag function return the same value
     * @param node object to compare
     * @return true both object have the same tag value
     */
    @Override
    public boolean equals(Object node) {
        return (node instanceof Node) && (node == this || getTag().equals(((Node) node).getTag()));
    }//equals

    /**
     * the node transmission power, in mdb
     * @return transmission power in mdb
     */
    int getTxPowerLevel(){
        return mAdvertise.getTxPower();
    }//getTxPowerLevel

    /**
     * return the most recent value of rssi, for have feash data use readRssi and wait the answer
     * from the onRSSIChanged callback
     * @return last know rssi value
     * @see com.st.BlueSTSDK.Node.BleConnectionParamUpdateListener
     */
    public int getLastRssi(){
        return mRssi;
    }

    /**
     * request for an async read of the rssi, the value will be returned using the
     * {@link com.st.BlueSTSDK.Node.BleConnectionParamUpdateListener#onRSSIChanged(Node, int)} callback
     */
    public void readRssi(){
        if(mBleThread!=null)
            mBleThread.post(mUpdateRssiTask);
    }//readRssi


    /**
     * return the moment of the last rssi update that we received
     * @return date of the last rssi that we received
     */
    public Date getLastRssiUpdateDate(){
        return mLastRssiUpdate;
    }


    /**
     * set the class where do the callback in case the rssi or txpower will change.
     * <p> only one class at time can receive the notification</p>
     * @param listener class where do the callback
     */
    public void addBleConnectionParamListener(BleConnectionParamUpdateListener listener){
        if(listener!=null && !mBleConnectionListeners.contains(listener))
            mBleConnectionListeners.add(listener);
    }

    /**
     * remove the ble connection listener from this node
     * @param listener listener to remove
     */
    public void removeBleConnectionParamListener(BleConnectionParamUpdateListener listener){
        mBleConnectionListeners.remove(listener);
    }

    /**
     * function call when the manager receive a new advertise by this node
     * @param rssi rssi of the last advertise
     */
    void isAlive(int rssi){
        //remove the set lost task
        if(mHandler!=null) mHandler.removeCallbacks(mSetNodeLost);
        updateRssi(rssi);
        //start a new set lost task
        if(mHandler!=null) mHandler.postDelayed(mSetNodeLost,NODE_LOST_TIMEOUT_MS);

    }//isAlive

    /**
     * crate the package to send to the command characteristics
     * @param mask destination feature
     * @param type command type
     * @param data command parameters
     * @return data to send to the characteristics
     */
    private byte[] packageCommandData(int mask,byte type,byte data[]){
        byte calibPackage[] = new byte[data.length+4+1]; //4=sizeof(int) + 1 for the req type
        byte maskArray[] = NumberConversion.BigEndian.int32ToBytes(mask);
        System.arraycopy(maskArray, 0, calibPackage, 0, maskArray.length);
        calibPackage[4]=type;
        System.arraycopy(data, 0, calibPackage, 4 + 1, data.length);
        return calibPackage;
    }

    /**
     * send a command to a feature
     *
     * sending command to a general pourpose feature is not supported
     *
     * @param feature destination feature
     * @param type command type
     * @param data command parameters
     * @return true if the message is correctly send, false otherwise
     */
    boolean sendCommandMessage(Feature feature,byte type,byte data[]) {
        if (feature instanceof FeatureGenPurpose)
            return false;
        final BluetoothGattCharacteristic characteristic = getCorrespondingChar(feature);
        if (mInitialization == null || characteristic == null)
            return false;
        int mask = BLENodeDefines.FeatureCharacteristics.extractFeatureMask(characteristic.getUuid());
        //not enable or not exist or not in write mode -> return false
        int charProp = mInitialization.getProperties();
        if (!feature.isEnabled() ||
                ((charProp & (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                        | BluetoothGattCharacteristic.PROPERTY_WRITE)) == 0)) {
            return false;
        }//else
        mInitialization.setValue(packageCommandData(mask, type, data));
        writeCharacteristics(mInitialization);

        return true;
    }


    /***
     * tell if the node is pair with the device
     * @return true if the node is bonded with the device
     */
    boolean isBounded(){
        return mDevice.getBondState()==BluetoothDevice.BOND_BONDED;
    }

    /**
     * get the class that can be used for write to the node serial console
     * @return object used for send/receive message to/from serial console, null if the service
     * is not available
     */
    public @Nullable Debug getDebug(){return mDebugConsole; }//getDebug

    /**
     * get the class that permit to read/write the configuration register
     * @return null if the configuration service is not available or the class
     */
    public @Nullable ConfigControl getConfigRegister(){return mConfigControl; } //getConfigRegister

}//Node
