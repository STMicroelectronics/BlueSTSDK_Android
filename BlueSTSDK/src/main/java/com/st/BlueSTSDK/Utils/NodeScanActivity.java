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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.R;

/**
 * Extend this activity if you need to start a device scan using the {@link Manager}.
 * This class will check that the user has the bluetooth enabled and for api &gt; 23 it check that
 * the user granted the location and enable the location service
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class NodeScanActivity extends AppCompatActivity {
    private final static String TAG = NodeScanActivity.class.getCanonicalName() ;
    private final static String SCAN_TIMEOUT = NodeScanActivity.class.getCanonicalName() + "" +
            ".SCAN_TIMEOUT";

    /**
     * request id for the activity that will ask to the user to enable the bt
     */
    private static final int REQUEST_ENABLE_BT = 1;
    /**
     * request id for grant the location permission
     */
    private static final int REQUEST_LOCATION_ACCESS = 2;

    /**
     * class used for manage the ble adapter and that will keep the list of the discovered device
     */
    protected Manager mManager;

    /**
     * !=0 if we have a start scanning request pending
     */
    private int mLastTimeOut=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mManager = Manager.getSharedInstance();
    }//onCreate

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(SCAN_TIMEOUT, mLastTimeOut);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLastTimeOut=savedInstanceState.getInt(SCAN_TIMEOUT, 0);
    }

    /**
     * check that the bluetooth is enabled
     * @return true if the bluetooth is enable false if we ask to the user to enable it
     */
    private boolean enableBluetoothAdapter(){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //the adapter is !=null since we request in the manifest to have the bt capability
        final BluetoothAdapter btAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }else
            return true;
    }//enableBluetoothAdapter

    /**
     * check that the location service is enabled
     * @return true if the location service is enabled, false if we ask to the user to do it
     */
    private boolean enableLocationService(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean providerEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) |
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!providerEnabled) {
            Resources res = getResources();
            // notify user
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(res.getString(R.string.EnablePositionService));
            dialog.setPositiveButton(res.getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            paramDialogInterface.cancel();
                        }
                    });
            dialog.setNegativeButton(res.getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            paramDialogInterface.cancel();
                            final View viewRoot = ((ViewGroup) NodeScanActivity.this
                                    .findViewById(android.R.id.content)).getChildAt(0);
                            if (viewRoot !=null)
                                Snackbar.make(viewRoot,  R.string.LocationNotEnabled, Snackbar.LENGTH_SHORT).show();
                            else{
                                Toast.makeText(NodeScanActivity.this,R.string.LocationNotEnabled,Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        }//onClick
                    });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });

        }//if
        return providerEnabled;
    }//enableLocationService

    /**
     * check to have the permission needed for start a bluetooth scanning
     * @return true if we have ti false if we ask for it
     */
    private boolean checkBlePermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                final View viewRoot = ((ViewGroup) this
                        .findViewById(android.R.id.content)).getChildAt(0);
                Snackbar.make(viewRoot, R.string.LocationCoarseRationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(NodeScanActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_LOCATION_ACCESS);
                            }//onClick
                        }).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_ACCESS);
            }//if-else
            return false;
        }else
            return  true;
    }//checkBlePermission


    /**
     * check to have the permission and the service enabled needed for stat a bluetooth scanning
     * @return true if we have all the requirements, false if we ask for something
     */
    private boolean checkAdapterAndPermission(){
        if(enableBluetoothAdapter()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(enableLocationService())
                    if(checkBlePermission())
                        return true;
            }else
                return true;
        }//if
        return false;
    }//checkAdapterAndPermission

    @Override
    protected  void onResume(){
        super.onResume();
        if(mLastTimeOut!=0)
            startNodeDiscovery(mLastTimeOut);

    }//onResume

    /**
     * method start a discovery and update the gui for the new state
     * @param timeoutMs time to wait before stop the discovery
     */
    public void startNodeDiscovery(int timeoutMs) {
        mLastTimeOut=timeoutMs;
        if(checkAdapterAndPermission()) {
            mManager.startDiscovery(timeoutMs);
            mLastTimeOut=0;
        }
    }

    /**
     * method that stop the discovery and update the gui state
     */
    public void stopNodeDiscovery() {
        mManager.stopDiscovery();
    }

    /**
     * return after ask to the user to enable the bluetooth
     *
     * @param requestCode request code id
     * @param resultCode  request result, if is {@code Activity.RESULT_CANCELED} the activity is
     *                    closed since we need the bluetooth
     * @param data        request data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth -> close all
        if (requestCode == REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_CANCELED) {
                final View viewRoot = ((ViewGroup) this
                        .findViewById(android.R.id.content)).getChildAt(0);
                if (viewRoot !=null)
                    Snackbar.make(viewRoot,  R.string.bluetoothNotEnabled, Snackbar.LENGTH_SHORT).show();
                else{
                    Toast.makeText(this,R.string.bluetoothNotEnabled,Toast.LENGTH_SHORT).show();
                }
                finish();
            }else {
                //bluetooth enable -> try to start scanning
                startNodeDiscovery(mLastTimeOut);
            }//if result
        }//if request
        super.onActivityResult(requestCode, resultCode, data);
    }//onActivityResult

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (grantResults.length == 0 )
            return;

        switch (requestCode) {
            case REQUEST_LOCATION_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //we have the permission try to start the scan again
                    startNodeDiscovery(mLastTimeOut);
                } else {

                    final View viewRoot = ((ViewGroup) this
                            .findViewById(android.R.id.content)).getChildAt(0);
                    if (viewRoot !=null)
                        Snackbar.make(viewRoot,  R.string.LocationNotGranted, Snackbar.LENGTH_SHORT).show();
                    else{
                        Toast.makeText(this,R.string.LocationNotGranted,Toast.LENGTH_SHORT).show();
                    }
                }//if-else
                break;
            }//REQUEST_LOCATION_ACCESS
        }//switch
    }//onRequestPermissionsResult
}

