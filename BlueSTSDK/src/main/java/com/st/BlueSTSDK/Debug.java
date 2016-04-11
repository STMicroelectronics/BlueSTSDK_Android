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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.st.BlueSTSDK.Utils.BLENodeDefines;

import java.util.UUID;

/**
 * Class used for write and read from the debug console
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class Debug {

    /**
     * node that will send the data to this class
     */
    private final Node mNode;
    /**
     * characteristics used for send/read stdin/out
     */
    private final BluetoothGattCharacteristic mTermChar;
    /**
     * characteristics used for read the stdErr
     */
    private final BluetoothGattCharacteristic mErrChar;

    /**
     * class where the notify that we receive a new data
     */
    private DebugOutputListener mListener;
    /**
     * Max size of string to sent in the input char
     */
    public static final int MAX_STRING_SIZE_TO_SENT = 20;

    /**
     * @param n          node that will send the data
     * @param termChar   characteristic used for write/notify the stdin/out
     * @param errChar    characteristic used used for notify the stderr
     */
    Debug(Node n, BluetoothGattCharacteristic termChar,
          BluetoothGattCharacteristic errChar) {
        mNode = n;
        mTermChar = termChar;
        mErrChar = errChar;
    }//Debug

    /**
     * write a message to the stdIn of the debug console prepare the string to sent and check
     * if there is a current message in queue to be sent
     *
     * @param message message to send
     * @return number of char sent in Terminal standard characteristic, or -1 if was impossible send
     * the message
     */
    public int write(String message) {
        mNode.enqueueCharacteristicsWrite(mTermChar,message.getBytes());
        return (message.length() > MAX_STRING_SIZE_TO_SENT) ? MAX_STRING_SIZE_TO_SENT :message.length();
    }

    /**
     * set the output listener, only one listener can be set in this class
     * <p>
     *     If the listener is null we stop the notification
     * </p>
     *
     * @param listener class with the callback when something appear in the debug console
     */
    public void setDebugOutputListener(DebugOutputListener listener) {
        if(mListener==listener)
            return;
        mListener = listener;
        boolean enable = mListener != null;
        mNode.changeNotificationStatus(mTermChar, enable);
        mNode.changeNotificationStatus(mErrChar, enable);
    }

    /**
     * the node had received an update on this characteristics, if it is a debug characteristic we
     * sent its data to the listener
     *
     * @param characteristic characteristic that has been updated
     */
    void receiveCharacteristicsUpdate(BluetoothGattCharacteristic characteristic) {
        if (mListener == null)
            return;
        UUID charUuid = characteristic.getUuid();
        if (charUuid.equals(BLENodeDefines.Services.Debug.DEBUG_STDERR_UUID)) {
            mListener.onStdErrReceived(this, characteristic.getStringValue(0));
        } else if (charUuid.equals(BLENodeDefines.Services.Debug.DEBUG_TERM_UUID))
            mListener.onStdOutReceived(this, characteristic.getStringValue(0));
    }//receiveCharacteristicsUpdate

    /**
     * the node had finish to write a characteristics
     *
     * @param characteristic characteristic that has been write
     * @param status         true if the write end correctly, false otherwise
     */
    void receiveCharacteristicsWriteUpdate(BluetoothGattCharacteristic characteristic,
                                           boolean status) {
        if (mListener == null)
            return;
        UUID charUuid = characteristic.getUuid();
        if (charUuid.equals(BLENodeDefines.Services.Debug.DEBUG_TERM_UUID)) {
            String str = characteristic.getStringValue(0);
            if(str.length()>MAX_STRING_SIZE_TO_SENT)
                mListener.onStdInSent(this,str.substring(0,MAX_STRING_SIZE_TO_SENT) , status);
            else
                mListener.onStdInSent(this,str , status);
        }
    }//receiveCharacteristicsWriteUpdate

    /**
     * get the node that write/listen in this debug console
     *
     * @return get the node that write on this debug console
     */
    public Node getNode() {
        return mNode;
    }//getNode

    /**
     * Interface used for notify to the user the console activity
     * @author STMicroelectronics - Central Labs.
     */
    public interface DebugOutputListener {

        /**
         * a new message appear on the standard output
         *
         * @param debug   object that send the message
         * @param message message that someone write in the debug console
         */
        void onStdOutReceived(Debug debug, String message);

        /**
         * a new message appear on the standard error
         *
         * @param debug   object that send the message
         * @param message message that someone write in the error console
         */
        void onStdErrReceived(Debug debug, String message);

        /**
         * call when a message is send to the debug console
         *
         * @param debug       object that received the message
         * @param message     message that someone write in the debug console
         * @param writeResult true if the message is correctly send
         */
        void onStdInSent(Debug debug, String message, boolean writeResult);

    }//DebugOutputListener

}//Debug
