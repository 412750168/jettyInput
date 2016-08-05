/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bestidear.jettyinput.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.bestidear.jettyinput.socket.RCtcpService.RecvTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Instrumentation;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class RCBluetoothService {
    // Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    

    
    public static final int                        KEYCOD_EPAD_MOUSE_SELECT     = 201;
    public static final int                        KEYCOD_EPAD_MOUSE_SWITCH     = 200;
    public static final int                        MAX_TIME_OF_DOUBLE_TAP_SETP1 = 150;
    public static final int                        MAX_TIME_OF_DOUBLE_TAP_SETP2 = 500;
    public static final int                        MIN_TIME_OF_DOUBLE_TAP_SETP1 = 0;
    public static final int                        MIN_TIME_OF_DOUBLE_TAP_SETP2 = 50;
    /*-----------------------------------------------------------------------------*/
    public static final int                        PAD_MOUSE_SENSITIVE_RANGE    = 20;
    public static final int                        PAD_MOUSE_TIMER              = 5000;                                        // ms
    public static final int                        RC_EVENT_TYPE_ACK            = 0;                                           // client<-->server
    // client<--server
    public static final int                        RC_EVENT_TYPE_GET_PICTURE    = 6;                                           // client-->server
    public static final int                        RC_EVENT_TYPE_KEY            = 1;                                           // client-->server
    public static final int                        RC_EVENT_TYPE_SENSOR         = 4;                                           // client-->server
    public static final int 					   RC_EVENT_BLUETOOTH_SETTING_WIFI	= 10;	
    // touch or mouse mode
    public static final int                        RC_EVENT_TYPE_SERVICE        = 8;
    // client get server framebuffer
    public static final int                        RC_EVENT_TYPE_SWITCH_MODE    = 7;                                           // client-->server
    public static final int                        RC_EVENT_TYPE_TOUCH          = 2;                                           // client-->server
    public static final int                        RC_EVENT_TYPE_TRACKBALL      = 3;                                           // client-->server
    public static final int                        RC_EVENT_TYPE_UI_STATE       = 5;                                           // 5.
    public static final String                     READCMDS                     = "com.amlapp.remotecontrol.cmds";
    private Socket                                 clntSock                     = null;
    private int                                    curKey                       = -1;
    private boolean                                isMouseMode                  = false;
    private long                                   Key_cur_TIME                 = 0;
    private int                                    last_x                       = 0;
    private int                                    last_y                       = 0;
    private SparseArray<MotionEvent.PointerCoords> mPointers                    = new SparseArray<MotionEvent.PointerCoords>();
    private int                                    pointerId                    = 0;
    private RecvTask                               receiver                     = null;
    private InputStream                            recvStream                   = null;
    private OutputStream                           sendStream                   = null;
    private ServerSocket                           servSock                     = null;
    private boolean                                select                       = false;
    private RCSensorEventPool                      pool                         = null;
    private int                                    metaDate                     = 0;
    
    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
	public static final String SETTING_WIFI_BLUETOOTH = "setting_wifi_bluetooth";

    
	final Instrumentation mInstrumentation=new Instrumentation();
	private Context context;
	private SharePreferences sharePreferences;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public RCBluetoothService(Context mcontext, Handler handler,RCSensorEventPool Pool) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        pool = Pool;
        context = mcontext;
        sharePreferences = new SharePreferences(context);
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
      //  mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(RCService.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(RCService.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(RCService.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
//        bundle.putString(RCService.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        RCBluetoothService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(RCService.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
//        bundle.putString(RCService.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        RCBluetoothService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure":"Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                        MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                    Log.d("zzl:::","this is remote jettyInput is connected");
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (RCBluetoothService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice(),
                                    mSocketType);
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (RCBluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
     class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            //byte[] buffer = new byte[1024];
            //int bytes;
            byte[] cmd = null;
            // Keep listening to the InputStream while connected
            
            write(getWifiListToString().getBytes());// 发送wifi 列表
				
            while (true) {
                try {
                    // Read from the InputStream
                    //bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    //mHandler.obtainMessage(RCService.MESSAGE_READ, bytes, -1, buffer)
                     //       .sendToTarget();
					 
					 cmd= readCmd();
	                 Log.d("zzl:::","this is remote blue tooth service receive::" + cmd);
					 
				     if (cmd != null && cmd.length > 3) {
		                 
				    	 if (RCService.DEBUG) {
		                        Log.d(TAG, "cmd:" + cmd[0] + "cmd.length" + cmd.length
		                                + "time:" + System.currentTimeMillis());
		                    }
				    	 
		                    int operation = cmd[0] & 0xff;
		                    switch (operation) {
		                        case RC_EVENT_TYPE_KEY:
		                            if (cmd.length > 3) {
		                            	int code=cmd[2] & 0xff;
		                            	int action=cmd[1] & 0xff;
		                            	
		                            	if (code == KEYCOD_EPAD_MOUSE_SELECT) {
		                                    if (isMouseMode) {
		                                        int loc[] = ((RCService.ServiceHandler) mHandler).getCurLoc();
		                                        int what;
		                                        what = (KeyEvent.ACTION_DOWN == action) ? MotionEvent.ACTION_DOWN
		                                                : MotionEvent.ACTION_UP;
		                                        select = true;
		                                        sendTouchEvent(what, (loc[0]-2), (loc[1]-2));
		                                        Log.d("zzl:::","KEYCOD_EPAD_MOUSE_SELECT");
		                                    }
		                            	  }
		                                 else if(action==KeyEvent.ACTION_DOWN){ 
		                                	 
		                                	 if(code == KeyMap.RC_KEYCODE_SHIFT_LEFT)
				                            		if(sharePreferences.getShiftValue().equals("false"))
				                            			sharePreferences.putShiftValue("true");
				                            		else sharePreferences.putShiftValue("false");
		                                	 
		                                	 if(code == KeyMap.RC_KEYCODE_HOME){
		                                		 if(!isMiracastRunning()){
		                                			 if(sharePreferences.getShiftValue().equals("true") && code >= KeyMap.RC_KEYCODE_A && code <= KeyMap.RC_KEYCODE_Z)
		                                				 mInstrumentation.sendStringSync(KeyMap.GetBigSmallerChar(true, code));
		                                			 else mInstrumentation.sendKeyDownUpSync(KeyMap.getMap(code));
		                                		 }
		                                	 }else{  
		                                		 Log.d("zzl:::","!!!!!!!!this is to test shift key::::"+sharePreferences.getShiftValue()+"::::"+code);
		                                		 if(sharePreferences.getShiftValue().equals("true")&& code >= KeyMap.RC_KEYCODE_A && code <= KeyMap.RC_KEYCODE_Z)
	                                				 mInstrumentation.sendStringSync(KeyMap.GetBigSmallerChar(true, code));
	                                			 else mInstrumentation.sendKeyDownUpSync(KeyMap.getMap(code));
		                                	 }
		                               }
		                            }
		                            break;
		                        case RC_EVENT_TYPE_TOUCH:
		                            if (cmd.length < 5) {
		                                break;
		                            }
		                            int x = (cmd[2] & 0xff) << 8 | (cmd[3] & 0xff);
		                            int y = (cmd[4] & 0xff) << 8 | (cmd[5] & 0xff);
		                            sendTouchEvent(cmd[1] & 0xff, x, y);
                                    Log.d("zzl:::","RC_EVENT_TYPE_TOUCH");
		                        case RC_EVENT_TYPE_TRACKBALL:
		                            break;
		                        case RC_EVENT_TYPE_SWITCH_MODE:
		                            if (cmd.length < 4) {
		                                break;
		                            }
		                            if ((cmd[1] & 0xff) == KeyEvent.ACTION_UP
		                                    && (cmd[2] & 0xff) == KEYCOD_EPAD_MOUSE_SWITCH) {
		                                isMouseMode = ((cmd[3] & 0xff) == 1) ? true
		                                        : false;
		                                if (isMouseMode) {
		                                    setMousebyTcp(RCService.OSD_SHOW, 0, 0,
		                                            null);
		                                    Log.d("zzl:::","this is bluetooth mouse show ");

		                                } else {
		                                    setMousebyTcp(RCService.OSD_HIDE, 0, 0,
		                                            null);
		                                }
		                            }
		                            break;
		                        case RC_EVENT_TYPE_ACK:
		                            break;
		                        case RC_EVENT_TYPE_SENSOR:
		                            if (cmd.length > 5) {
		                                pool.setRCSensor(cmd);
		                            }
		                            break;
		                        case RC_EVENT_TYPE_GET_PICTURE:
		                            Log.d(TAG, "this function not support now");
		                            break;
		                        case RC_EVENT_TYPE_SERVICE:
		                            break;
		                            
		                        case RC_EVENT_BLUETOOTH_SETTING_WIFI:
		                        	byte []IpPass = new byte[cmd.length-1];// 去掉一个字节 的 cmd
		                        	
		                        	System.arraycopy(cmd, 1, IpPass, 0, cmd.length -1);
		                        	String str = new String(IpPass);
		                        	
		                        	Intent intent = new Intent();
		                        	intent.putExtra("cmd", str);
		                        	intent.setAction(SETTING_WIFI_BLUETOOTH);
		                        	context.sendBroadcast(intent);
		                        	
		                        	Log.d("zzl:::","*********************this is test bluetoothreceive ip and password::::"+str);
		                        	break;
		                    }
		                } else {
		                    try {
		                        Thread.sleep(10);
		                    } catch (InterruptedException e) {
		                    }
		                }
					 
					 //mHandler.obtainMessage(RCService.MESSAGE_READ, cmd.length, -1, cmd)
                      //      .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    RCBluetoothService.this.start();
                    break;
                }
            
        }
        }
        
        private String getWifiListToString(){
    		String str="";
    		
    		List<ScanResult> scanlist = getWifiList();
    		for (int i = 0; i < scanlist.size(); i++) {
    			if(scanlist.get(i).frequency > 5000 && scanlist.get(i).frequency < 6000){// 5G 
    				str = str + "5G_"+scanlist.get(i).SSID+"#"; // 是5G就加个5G的前缀
    				
    			}else{//2.4G
    				str = str + scanlist.get(i).SSID + "#";
    			}
    		}
    		return str;
    	}
    	
    	private List<ScanResult> getWifiList() {

    		List<ScanResult> list = new ArrayList<ScanResult>();

    		WifiManager mWifiManager = (WifiManager) context
    				.getSystemService(Context.WIFI_SERVICE);

    		if (mWifiManager != null){
    			mWifiManager.startScan();//这个是异步的
    			list = mWifiManager.getScanResults();
    		}
    		return list;
    	}
       
        private boolean isMiracastRunning(){
        	
        	 ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        	    List<RunningTaskInfo> list = am.getRunningTasks(100);
        	    for (RunningTaskInfo info : list) {
        	        if (info.topActivity.getPackageName().equals("com.amlogic.miracast") && info.baseActivity.getPackageName().equals("com.amlogic.miracast")) {
        	           return true;
        	        }
        	    }
        	return false;
        }
        
        /**
         * @param action
         * @param xv
         * @param yv
         */
        private void sendMouseEvent(int action, int xv, int yv) {
            if (RCService.DEBUG) Log.d(TAG, "x value:" + xv + "yv:" + yv);
            if (MotionEvent.ACTION_DOWN == action) {
                last_x = xv;
                last_y = yv;
                
                setMousebyTcp(RCService.OSD_MOV_ST, xv - last_x, yv - last_y, null);
            } else if (MotionEvent.ACTION_UP == action) {
                setMousebyTcp(RCService.OSD_MOV_ED, xv - last_x, yv - last_y, null);
            } else {
                setMousebyTcp(RCService.OSD_MOV_ING, xv - last_x, yv - last_y, null);
                last_x = xv;
                last_y = yv;
            }
        }
        
        private void setMousebyTcp(int what, int arg0, int arg1, Object obj) {
            if (mHandler != null) {
                Message msg = Message.obtain();
                msg.what = what;
                msg.arg1 = arg0;
                msg.arg2 = arg1;
                msg.obj = obj;
                mHandler.sendMessage(msg);
            }
        }

        /**
         * @param i
         * @param x
         * @param y
         */
        private void sendTouchEvent(int action, int xv, int yv) {
        	Log.d("zzl:::","ismouseMode :"+ isMouseMode + "select:" + select);
            if (isMouseMode && !select) {
                sendMouseEvent(action, xv, yv);
            } else {
                if (MotionEvent.ACTION_DOWN == action) {
                    Key_cur_TIME = SystemClock.uptimeMillis();
                    mPointers.clear();
                }
                MotionEvent.PointerCoords c = new MotionEvent.PointerCoords();
                c.x = xv;
                c.y = yv;
                mPointers.append(pointerId, c);
                int pointcount = mPointers.size();
                int[] pointerIds = new int[pointcount];
                MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointcount];
                for (int i = 0; i < pointcount; i++) {
                    pointerIds[i] = mPointers.keyAt(i);
                    pointerCoords[i] = mPointers.valueAt(i);
                }
                long time = SystemClock.uptimeMillis();
                if (select && (action == MotionEvent.ACTION_UP)) {
                    time = (Key_cur_TIME += 70);
                }
                MotionEvent ev = MotionEvent.obtain(Key_cur_TIME, time, action, xv,
                        yv, 0);
                ev.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                mInstrumentation.sendPointerSync(ev);
                ev.recycle();
                if (select && (action == MotionEvent.ACTION_UP)) {
                    select = false;
                }
            }
        }

	private byte[] readCmd() throws IOException {
            int bytes_left = 0;
            int bytes_read = 0;
            byte len_data[] = new byte[4];
            if (mmInStream == null || mmInStream.read(len_data, 0, 4) < 4) {
                return null;
            }
            int data_len = 0;
            for (int i = 0; i < 4; i++) {
                data_len += (len_data[i] & 0xff) << (8 * (3 - i));
            }
            if (data_len < 0 || data_len > 256) {
                return null;
            }
            bytes_left = data_len;
            byte[] read_recv = new byte[data_len];
            while (bytes_left > 0) {
                int read_len = mmInStream.read(read_recv, bytes_read,
                        bytes_left);
                if (read_len > 0) {
                    bytes_left -= read_len;
                    bytes_read += read_len;
                } else {
                    break;
                }
            }
            return read_recv;
        }
        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(RCService.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
