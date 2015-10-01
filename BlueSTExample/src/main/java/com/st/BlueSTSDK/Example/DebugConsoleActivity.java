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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity that show a console with the stdout and stderr message from the board and permit to
 * send string to the stdin
 */
public class DebugConsoleActivity extends AppCompatActivity {

    private final static String NODE_FRAGMENT = DebugConsoleActivity.class.getCanonicalName() + "" +
            ".NODE_FRAGMENT";

    private final static String NODE_TAG = DebugConsoleActivity.class.getCanonicalName() + "" +
            ".NODE_TAG";


    /**
     * text view where we will dump the console out
     */
    private TextView mConsole;

    /**
     * scroll view attached to the text view, we use it for keep visualized the last line
     */
    private ScrollView mConsoleView;

    /**
     * text edit where the user will write its commands
     */
    private EditText mUserInput;

    /** node that will send the information */
    private Node mNode;

    /** fragment used for keep the connection open */
    private NodeContainerFragment mNodeContainer;

    /**
     * object that will send/receive commands from the node
     */
    private Debug mDebugService;

    /**
     * enable the console when the node connect
     */
    private Node.NodeStateListener mNodeStateChangeListener = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {
            if (newState == Node.State.Connected) {
                setUpConsoleService(node.getDebug());
            } else if (newState == Node.State.Dead) {
                DebugConsoleActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DebugConsoleActivity.this, R.string.DebugNotAvailable,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }//if-else
        }//onStateChange
    };//NodeStateListener

    /**
     * create an intent for start the activity that will log the information from the node
     *
     * @param c    context used for create the intent
     * @param node note that will be used by the activity
     * @return intent for start this activity
     */
    public static Intent getStartIntent(Context c, @NonNull Node node) {
        Intent i = new Intent(c, DebugConsoleActivity.class);
        i.putExtra(NODE_TAG, node.getTag());
        i.putExtras(NodeContainerFragment.prepareArguments(node));
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load the gui
        setContentView(R.layout.activity_debug_console);
        mConsoleView = (ScrollView) findViewById(R.id.consoleView);
        mConsole = (TextView) findViewById(R.id.deviceConsole);
        mUserInput = (EditText) findViewById(R.id.inputText);

        /**
         * when the user click on the button "send" we send the message and delete the text the
         * textview
         */
        mUserInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    mDebugService.write(v.getText().toString() + "\n");
                    v.setText(""); //reset the string
                    handled = true;
                }//if
                return handled;
            }//onEditorAction
        });

        // recover the node
        String nodeTag = getIntent().getStringExtra(NODE_TAG);
        mNode = Manager.getSharedInstance().getNodeWithTag(nodeTag);

        //create/recover the NodeContainerFragment
        if (savedInstanceState == null) {
            Intent i = getIntent();
            mNodeContainer = new NodeContainerFragment();
            mNodeContainer.setArguments(i.getExtras());
            getFragmentManager().beginTransaction()
                    .add(mNodeContainer, NODE_FRAGMENT).commit();

        } else {
            mNodeContainer = (NodeContainerFragment) getFragmentManager()
                    .findFragmentByTag(NODE_FRAGMENT);
        }//if-else

    }//onCreate

    /**
     * when the node connected check the presence of the debug service and enable the gui if it
     * present otherwise it will show an error message
     *
     * @param debugService debug service return from the node, null if not present
     */
    private void setUpConsoleService(Debug debugService) {
        mDebugService = debugService;
        if (mDebugService != null) {
            Resources res = DebugConsoleActivity.this.getResources();
            mDebugService.setDebugOutputListener(new UpdateConsole(
                    res.getColor(R.color.OutMsg),
                    res.getColor(R.color.InMsg),
                    res.getColor(R.color.ErrorMsg)));
            DebugConsoleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUserInput.setEnabled(true);
                }
            });
        } else {
            DebugConsoleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DebugConsoleActivity.this, R.string.DebugNotAvailable,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * if the node is connected enable the gui otherwise attach a listener for do it when the node
     * connects
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mNode.isConnected()) {
            setUpConsoleService(mNode.getDebug());
        } else
            mNode.addNodeStateListener(mNodeStateChangeListener);
    }//onResume


    /**
     * remove the listener that we add to the node and debug object
     */
    @Override
    protected void onPause() {
        mNode.removeNodeStateListener(mNodeStateChangeListener);

        if (mDebugService != null)
            mDebugService.setDebugOutputListener(null);

        super.onPause();
    }//onPause

    /**
     * if we have to leave this activity, we force to keep the connection open, since we go back
     * in the {@link FeatureListActivity}
     */
    @Override
    public void onBackPressed() {
        mNodeContainer.keepConnectionOpen(true);
        super.onBackPressed();
    }//onBackPressed

    /**
     * call when the user press the back button on the menu bar, we are leaving this activity so
     * we keep the connection open since we are going int the {@link FeatureListActivity}
     *
     * @param item menu item clicked
     * @return true if the item is handle by this function
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button, we don't return for permit to
            // do the standard action of this button
            case android.R.id.home:
                mNodeContainer.keepConnectionOpen(true);
        }//switch

        return super.onOptionsItemSelected(item);
    }

    /**
     * listener for debug message, it will update the textview with received message
     * <p>
     * The different message will be show with different color
     * </p>
     * <p>
     * We avoid to print the date if 2 message from the same stream arrive in a short time frame
     * </p>
     */
    private class UpdateConsole implements Debug.DebugOutputListener {

        /**
         * if 2 message with a distance below this value we avoid to reprint the date
         */
        private static final long PAUSE_DETECTION_TIME_MS = 100; //ms

        /**
         * format used for print the date
         */
        private final SimpleDateFormat DATE_FORMAT =
                new SimpleDateFormat("yyMMdd HH:mm:ss.SSS", Locale.getDefault());

        /**
         * true if we are receiving a message in the stdout stream
         */
        private boolean mStdOutReceiving = false;

        /**
         * true if we are receiving a message in the stdIn stream
         */
        private boolean mStdInReceiving = false;

        /**
         * true if we are receiving a message in the stdErr stream
         */
        private boolean mStdErrReceiving = false;

        /**
         * time when we receive the last message
         */
        private Date mLastMessageReceived = new Date();

        /**
         * color used for the stderr message
         */
        private int mErrorColor;

        /**
         * color used for the received message
         */
        private int mReceivedColor;

        /**
         * color used for the send message
         */
        private int mSendColor;

        /**
         * Set the color to use for the debug message
         * <p>
         * The color are in the form 0xAARRGGBB
         * </p>
         *
         * @param sendColor     color to use for the send message
         * @param receivedColor color to used for the received color
         * @param errorColor    color to use for show error message
         * @see android.graphics.Color
         */
        public UpdateConsole(int sendColor, int receivedColor, int errorColor) {
            mErrorColor = errorColor;
            mSendColor = sendColor;
            mReceivedColor = receivedColor;
        }//UpdateConsole

        /**
         * add the prefix with the date and the message direction to a message string
         * @param append true if we want add timestamp
         * @param dir message direction, <  the message arrive from the node, > the message is
         *            send from the user
         * @return string with the date and the direction appended
         */
        private String appendDateTime(boolean append, char dir) {
            String str = "";
            Date now = new Date();
            if (append || (now.getTime() - mLastMessageReceived.getTime() > PAUSE_DETECTION_TIME_MS)) {
                str += "\n[" + DATE_FORMAT.format(now) + dir + "]";
            }//if
            mLastMessageReceived = now;

            return str;
        }//appendDateTime

        /**
         * append to the console a message and scroll it down
         * @param message message to append
         */
        private void updateConsole(final SpannableStringBuilder message){
            DebugConsoleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConsole.append(message);
                    mConsoleView.fullScroll(View.FOCUS_DOWN);
                }//run
            });
        }//updateConsole

        @Override
        public void onStdOutReceived(Debug debug, final String message) {
            final SpannableStringBuilder displayText = new SpannableStringBuilder();
            displayText.append(appendDateTime(!mStdOutReceiving, '<'));
            displayText.append(message);
            mStdOutReceiving = true;
            mStdInReceiving = false;
            mStdErrReceiving = false;
            displayText.setSpan(new ForegroundColorSpan(mReceivedColor), 0, displayText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            updateConsole(displayText);
        }//onStdOutReceived

        @Override
        public void onStdErrReceived(Debug debug, String message) {
            final SpannableStringBuilder displayText = new SpannableStringBuilder();
            displayText.append(appendDateTime(!mStdErrReceiving, '<'));
            displayText.append(message);
            mStdOutReceiving = false;
            mStdInReceiving = false;
            mStdErrReceiving = true;
            displayText.setSpan(new ForegroundColorSpan(mErrorColor), 0, displayText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            updateConsole(displayText);
        }//onStdErrReceived

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            final SpannableStringBuilder displayText = new SpannableStringBuilder();
            displayText.append(appendDateTime(!mStdInReceiving, '>'));
            displayText.append(message);
            mStdOutReceiving = false;
            mStdInReceiving = true;
            mStdErrReceiving = false;
            displayText.setSpan(new ForegroundColorSpan(mSendColor), 0, displayText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            updateConsole(displayText);
        }//onStdInSent
    }

}

