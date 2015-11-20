package com.st.BlueSTSDK.Utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Log.FeatureLogCSVFile;
import com.st.BlueSTSDK.Log.FeatureLogDB;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for an activity that have to log the feature data, this code manage the permission
 * required for store file on the disk and send the collected data by mail
 * the activity will instantiate a menu item for start/stop the log activity
 */
public abstract class LogFeatureActivity extends AppCompatActivity {

    /**
     * id used for request the write permission
     */
    private static final int REQUEST_WRITE_ACCESS=1;

    /**
     * object used for log the data
     */
    private Feature.FeatureLoggerListener mCurrentLogger;

    /**
     * return the node to log
     * @return node that we want to log
     */
    protected abstract Node getNodeToLog();

    /**
     * return the directory path where we will store the log file if generated
     * @return directory where store the log file
     */
    protected abstract String getLogDirectory();

    /**
     * return the logger that we will use for store the data
     * @return logger used for log the feature data
     */
    protected abstract Feature.FeatureLoggerListener getLogger();

    /**
     * create a menu item for start/stop the logging
     * @param menu menu where add out item
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_feature, menu);

        if (mCurrentLogger==null) {
            menu.findItem(R.id.startLog).setVisible(true);
            menu.findItem(R.id.stopLog).setVisible(false);
        } else {
            menu.findItem(R.id.startLog).setVisible(false);
            menu.findItem(R.id.stopLog).setVisible(true);
        }//if-else

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    private boolean checkWriteSDPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                final View viewRoot = ((ViewGroup) this
                        .findViewById(android.R.id.content)).getChildAt(0);
                Snackbar.make(viewRoot, R.string.WriteSDRationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(LogFeatureActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_WRITE_ACCESS);
                            }//onClick
                        }).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_ACCESS);
            }//if-else
            return false;
        }else
            return  true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //we have the permission try start logging
                    registerLoggerListener(getNodeToLog());
                } else {
                    Toast.makeText(this, R.string.WriteSDNotGranted, Toast.LENGTH_SHORT).show();
                    mCurrentLogger=null;
                    invalidateOptionsMenu();
                }//if-else
                break;
            }//REQUEST_LOCATION_ACCESS
        }//switch
    }//onRequestPermissionsResult

    /**
     * set the logger for all the feature in the node
     * @param n node that will be logged
     */
    private void registerLoggerListener(Node n){
        List<Feature> features = n.getFeatures();
        for (Feature f : features) {
            f.addFeatureLoggerListener(mCurrentLogger);
        }//for
    }//registerLoggerListener

    /**
     * stop the previous logger and star a new one
     * @param n node where add the logger
     */
    protected void startLogging(Node n) {
        if (mCurrentLogger!=null)
            stopLogging(n);
        mCurrentLogger = getLogger();
        //if api >23 and we will store on disk
        if((mCurrentLogger instanceof FeatureLogCSVFile ||
                mCurrentLogger instanceof FeatureLogDB) &&
           (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
            if(checkWriteSDPermission()){
                registerLoggerListener(n);
            }//if
        }else{
            registerLoggerListener(n);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.startLog) {
            startLogging(getNodeToLog());
            invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.stopLog) {
            stopLogging(getNodeToLog());
            invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * remove empty file from a list of file
     * @param allFiles array of file to filter
     * @return files with size >0
     */
    private @NonNull File[] removeEmptyFile(File[] allFiles){
        ArrayList<File> temp = new ArrayList<>();
        for(File f: allFiles){
            if(f.length()>0)
                temp.add(f);
        }
        return temp.toArray(new File[temp.size()]);
    }

    /**
     * stop a logger
     * @param n node where stop the logger
     * <p>
     * in case we are running the {@link com.st.BlueSTSDK.Log.FeatureLogDB} logger the data will be
     * dumped on a csv files
     * </p>
     */
    protected void stopLogging(Node n) {
        if (mCurrentLogger==null)
            return;
        List<Feature> features = n.getFeatures();
        for (Feature f : features) {
            f.removeFeatureLoggerListener(mCurrentLogger);
        }//for

        final String directoryPath =getLogDirectory();
        File exportFiles[] = null;
        if (mCurrentLogger instanceof FeatureLogCSVFile) {
            ((FeatureLogCSVFile) mCurrentLogger).closeFiles();
            exportFiles = FeatureLogCSVFile.getLogFiles(directoryPath);
        }//if

        if (mCurrentLogger instanceof FeatureLogDB) {
            FeatureLogDB db = (FeatureLogDB) mCurrentLogger;
            exportFiles = db.dumpToFile(directoryPath);
        }

        //if we have something to export
        if(exportFiles!=null && exportFiles.length>0) {
            exportFiles = removeEmptyFile(exportFiles);
            if(exportFiles.length>0) //if we have a non empty
                exportDataByMail(exportFiles);
        }//if !=null

        mCurrentLogger=null;
    }

    private final static String EMAIL_TITLE = "BlueSTSDK Data";

    /**
     * create a mail with the log files as attached
     * @param logFiles file to attach
     */
    private void sendLogByMail(File[] logFiles) {

        final Intent emailIntent =
                new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_TITLE);

        ArrayList<Uri> uris = new ArrayList<>();
        // convert from paths to Android friendly Parcelable Uri's
        for (File file : logFiles) {
            uris.add(Uri.fromFile(file));
        }
        emailIntent.putParcelableArrayListExtra
                (Intent.EXTRA_STREAM, uris);

        startActivity(Intent.createChooser(emailIntent, "Sent mail"));
    }//sendLogByMail

    /**
     * ask if we want export the data by mail
     * @param logs file to export
     */
    private void exportDataByMail(final File[] logs) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.askExprotByMailTitle)
                .setMessage(R.string.askExprotByMailMessage)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendLogByMail(logs);
                    }//onClick
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }//exportDataByMail

}
