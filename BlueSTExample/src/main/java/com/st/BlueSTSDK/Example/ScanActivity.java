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
package com.st.BlueSTSDK.Example;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

/**
 * This activity will show a list of device that are supported by the sdk
 */
public class ScanActivity extends AppCompatActivity implements AbsListView.OnItemClickListener {

    /**
     * request id for the activity that will ask to the user to enable the bt
     */
    private static final int REQUEST_ENABLE_BT = 1;

    /**
     * number of millisecond that we spend looking for a new node
     */
    private final static int SCAN_TIME_MS = 10 * 1000; //10sec

    /**
     * class used for manage the ble adapter and that will keep the list of the discovered device
     */
    private Manager mManager;

    /**
     * adapter that will build the gui for each discovered node
     */
    private NodeArrayAdapter mAdapter;

    /**
     * listener that will change button gui when the discover stop
     */
    private Manager.ManagerListener mUpdateDiscoverGui = new Manager.ManagerListener() {

        /**
         * call the stopNodeDiscovery for update the gui state
         * @param m manager that start/stop the process
         * @param enabled true if a new discovery start, false otherwise
         */
        @Override
        public void onDiscoveryChange(Manager m, boolean enabled) {
            if (!enabled)
                ScanActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopNodeDiscovery();
                    }//run
                });
        }//onDiscoveryChange

        @Override
        public void onNodeDiscovered(Manager m, Node node) {
        }//onNodeDiscovered
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        AbsListView listView = (AbsListView) findViewById(R.id.nodeListView);
        //create the adapter and set it to the list view
        mAdapter = new NodeArrayAdapter(this);
        listView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);

        //retrieve the manager instance
        mManager = Manager.getSharedInstance();

        //add the already discovered nodes
        mAdapter.addAll(mManager.getNodes());

    }

    /**
     * clear the adapter and the manager list of nodes
     */
    private void resetNodeList() {
        mManager.resetDiscovery();
        mAdapter.clear();
        //some nodes can survive if they are bounded with the device
        mAdapter.addAll(mManager.getNodes());
    }//resetNodeList

    /**
     * check that the bluetooth is enabled and register the lister to the manager
     */
    @Override
    protected void onResume() {
        super.onResume();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //the adapter is !=null since we request in the manifest to have the bt capability
        final BluetoothAdapter btAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }//else

        //add the listener that will hide the progress indicator when the first device is discovered
        mManager.addListener(mUpdateDiscoverGui);
        //disconnect all the already discovered device
        mAdapter.disconnectAllNodes();
        //add as listener for the new nodes
        mManager.addListener(mAdapter);
        resetNodeList();
        startNodeDiscovery();
    }//onStart

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
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }//if
        super.onActivityResult(requestCode, resultCode, data);
    }//onActivityResult

    /**
     * stop the discovery and remove all the lister that we attach to the manager
     */
    @Override
    protected void onPause() {
        if (mManager.isDiscovering())
            mManager.stopDiscovery();
        //remove the listener add by this class
        mManager.removeListener(mUpdateDiscoverGui);
        mManager.removeListener(mAdapter);
        super.onPause();
    }//onPause

    /**
     * build the menu, it show the start/stop button in function of the manager state (if it is
     * scanning or not )
     *
     * @param menu menu where add the items
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scan, menu);

        boolean isScanning = (mManager != null) && mManager.isDiscovering();
        menu.findItem(R.id.menu_stop_scan).setVisible(isScanning);
        menu.findItem(R.id.menu_start_scan).setVisible(!isScanning);

        return true;
    }

    /**
     * called when the user select a menu item
     *
     * @param item item selected, it will remove the discovered nodes and start a new scan or
     *             stop the scanning
     * @return true if the item is handle by this method
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_start_scan) {
            resetNodeList();
            startNodeDiscovery();
            return true;
        }//else
        if (id == R.id.menu_stop_scan) {
            stopNodeDiscovery();
            return true;
        }//else
        return super.onOptionsItemSelected(item);

    }//onOptionsItemSelected

    /**
     * method start a discovery and update the gui for the new state
     */
    private void startNodeDiscovery() {
        mManager.startDiscovery(SCAN_TIME_MS);
        invalidateOptionsMenu(); //ask to redraw the menu for change the menu icon
    }

    /**
     * method that stop the discovery and update the gui state
     */
    private void stopNodeDiscovery() {
        mManager.stopDiscovery();
        invalidateOptionsMenu();//ask to redraw the menu for change the menu icon
    }

    /**
     * when a node is selected we start to connect with that node and show the demo activity
     *
     * @param parent   adapter where the click is item
     * @param view     item clicked
     * @param position position clicked
     * @param id       item id clicked
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Node n = mAdapter.getItem(position);

        Intent i = FeatureListActivity.getStartIntent(this, n);
        startActivity(i);
    }//onItemClick

}//ScanActivity
