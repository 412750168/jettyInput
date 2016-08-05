/**
 * @file RCService.java
 * @par Copyright: - Copyright 2014.
 * @version 1.0
 * @date 2013/03/03
 * @par function description: - 1
 * @warning This class may explode in your face.
 * @note If you inherit anything from this class, you're doomed.
 */
package net.bestidear.jettyinput.socket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import net.bestidear.jettyinput.ControlServlet;
import net.bestidear.jettyinput.ControlSocketServlet;
import net.bestidear.jettyinput.HostInterface;
import net.bestidear.jettyinput.R;
import net.bestidear.jettyinput.UnzipFile;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class RCService extends Service {
	public static final String TAG = "RCService";
	public static final int EVENT_ACK = 0;
	public static final int EVENT_KEY = 1;
	public static final int EVENT_TOUCH = 2;
	public static final int EVENT_TRACKBALL = 3;
	public static final int EVENT_SENSOR = 4;
	public static final int EVENT_UI_STATE = 5;
	public static final int EVENT_GET_SCREEN = 6;
	public static final int EVENT_KEY_MODE = 7;
	public static final int EVENT_SERVICE = 8;
	public static final String RC_CLIENT_SCAN = "amlogic-client-scan";
	public static final String RC_CLIENT_REQ = "amlogic-client-request-connect";
	public static final String RC_CLIENT_REQ_CONFIRM = "amlogic-client-request-connect-yes?";
	public static final String RC_CLIENT_REQ_OK = "amlogic-client-request-ok";
	public static final String RC_CLIENT_NO_CONNECT = "amlogic-client-no-connect";
	public static final String RC_SERVER_LISTEN = "amlogic-server-listen";
	public static final String RC_SERVER_IDLE = "amlogic-server-idle";
	public static final String RC_SERVER_USED = "amlogic-server-used";
	public static final String DEVICE_INFO = "ro.product.manufacturer";
	public static final int RC_UDP_PORT = 7001;
	public static final int RC_TCP_PORT = 7002;
	public static final int OSD_SHOW = 0;
	public static final int OSD_HIDE = 1;
	public static final int OSD_MOV_ST = 2;
	public static final int OSD_MOV_ING = 3;
	public static final int OSD_MOV_ED = 4;
	public final static int RC_DATA_LEN = 256;
	public static final int CMD_STOP_SERVICE = 203;
	public static final int CMD_START_SERVICE = 204;
	public static final int CMD_CHANGR_MOUSE = 205;
	public static final int CMD_GET_STATUS = 206;
	
	public static final int CMD_BLUETOOTH_OFF = 207;
	public static final int CMD_BLUETOOTH_ON  = 208;
	
	public static final int SERVICE_STATUS_UNINIT = 0;
	public static final int SERVICE_STATUS_RUNNING = 1;
	public static final int SERVICE_STATUS_STOP = 2;
	public static final String SERVERSTATUS = "net.bestidear.remoteControl.View";
	public static final String REMOTECMD = "net.bestidear.remoteControl.CONTROL";

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private RCBluetoothService mBluetoothService = null;

	public static final boolean DEBUG = true;
	private int mWScreenx = 0;
	private int mWScreeny = 0;
	private View mView = null;
	private ImageView mouseHandler = null;
	private WindowManager mWm = null;
	private WindowManager.LayoutParams mWmParams = null;
	private SharedPreferences mPrefs;
	private Timer mTimer;
	private TimerTask mTimerTask;
	private boolean mShowMouse = false;
	private TaskThread[] tasks = new TaskThread[3];

	private int mStatus = SERVICE_STATUS_UNINIT;
	private ServiceExecListener cmdReceiver = null;
	
	private RCSensor mRCSensor = null;
	// private ServiceThread osdthread = new ServiceThread();
	private int mScreenWidth = 0;
	private int mScreenHeight = 0;
	public static final String OSDRECEIVED = "android.amlapp.RemoteControl.OSTRECEIVED";
	private Handler mHandler = new ServiceHandler();
	public static boolean StartServiceFlag = false;

	Server mServer = null;
	RCSensorEventPool pool;
	/*-----------------------------------------------------------------------------*/
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public class ServiceHandler extends Handler {
		public byte[] getSendByte() {
			return getDefaultSendbyte();
		}

		public Context getContext() {
			return RCService.this;
		}

		public boolean isConnect() {
			return ((RCtcpService) tasks[1]).hasClientConnect();
		}

		public int[] getCurLoc() {
			int[] xy = new int[2];
			xy[0] = mWmParams.x;
			xy[1] = mWmParams.y;
			return xy;
		}

		public int[] getDisplaySize() {
			int[] xy = new int[2];
			if (mScreenWidth == 0 || mScreenHeight == 0) {
				Point point = new Point();
				Display display = mWm.getDefaultDisplay();
				display.getSize(point);
				mScreenWidth = (int) point.x;
				mScreenHeight = (int) point.y;
			}
			xy[0] = mScreenWidth;
			xy[1] = mScreenHeight;
			return xy;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OSD_SHOW:
				showMouseView();
				Log.d("zzl:::","this is osd handler show ");

				if (mTimer != null) {
					if (mTimerTask != null) {
						mTimerTask.cancel();
					}
				}
				break;
			case OSD_HIDE:
				// tcp relinks
				removeMouseView();
				break;
			case OSD_MOV_ST:
				showMouseView();
				Log.d("zzl:::","this is osd handler mov_st ");

				try {
					mWm.updateViewLayout(mView, mWmParams);
				} catch (Exception ex) {
				}
				break;
			case OSD_MOV_ING:
				mWmParams.x += msg.arg1;
				mWmParams.y += msg.arg2;
				valid();
				try {
					mWm.updateViewLayout(mView, mWmParams);
				} catch (Exception ex) {
				}
				break;
			case OSD_MOV_ED:
				mWmParams.x += msg.arg1;
				mWmParams.y += msg.arg2;
				valid();
				startTimerTask();
				break;
			}
		}
	}

	public class ServiceThread extends Thread {
		private BroadcastReceiver receiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(OSDRECEIVED)) {
					int xVal = intent.getIntExtra("x_value", 0);
					int yVal = intent.getIntExtra("y_value", 0);
					switch (intent.getIntExtra("cmd", -1)) {
					case OSD_SHOW:
						showMouseView();
						Log.d("zzl:::","this is osd broadcastreceiver show ");
						startTimerTask();
						break;
					case OSD_HIDE:
						// tcp relinks
						if (DEBUG) {
							Log.d(TAG, "OSD_HIDE at Service Thread");
						}
						removeMouseView();
						break;
					case OSD_MOV_ST:
						showMouseView();
						Log.d("zzl:::","this is osd broadcastreceiver mov_st ");
						try {
							mWm.updateViewLayout(mView, mWmParams);
						} catch (Exception ex) {
						}
						break;
					case OSD_MOV_ING:
						mWmParams.x += xVal;
						mWmParams.y += yVal;
						valid();
						try {
							mWm.updateViewLayout(mView, mWmParams);
						} catch (Exception ex) {
						}
						break;
					case OSD_MOV_ED:
						mWmParams.x += xVal;
						mWmParams.y += yVal;
						valid();
						try {
							mWm.updateViewLayout(mView, mWmParams);
						} catch (Exception ex) {
						} finally {
							startTimerTask();
						}
						break;
					}
				}
			}
		};

		
		public int[] getCurLoc() {
			int[] xy = new int[2];
			xy[0] = mWmParams.x;
			xy[1] = mWmParams.y;
			return xy;
		}

		public void run() {
			IntentFilter filter = new IntentFilter();
			filter.addAction(OSDRECEIVED);
			registerReceiver(receiver, filter);
		}

		public void pause() {
			unregisterReceiver(receiver);
		}
	}

	public byte[] getDefaultSendbyte() {
		if (mScreenWidth == 0 || mScreenHeight == 0) {
			Point point = new Point();
			Display display = mWm.getDefaultDisplay();
			display.getSize(point);
			mScreenWidth = (int) point.x;
			mScreenHeight = (int) point.y;
		}
		String deviceInfo = Build.MANUFACTURER;
		byte[] btemp = new byte[6 + deviceInfo.length()];
		btemp[0] = (byte) ((mScreenWidth >> 8) & 0xff);
		btemp[1] = (byte) ((mScreenWidth) & 0xff);
		btemp[2] = (byte) ((mScreenHeight >> 8) & 0xff);
		btemp[3] = (byte) ((mScreenHeight) & 0xff);
		btemp[4] = 0;
		btemp[5] = 0;
		System.arraycopy(deviceInfo.getBytes(), 0, btemp, 6,
				deviceInfo.length());
		return btemp;
	}

	private NsdManager nsdManager;
	private NsdManager.RegistrationListener nsdCallback;

	@Override
	public void onCreate() {
		super.onCreate();
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mPrefs = this.getSharedPreferences("remotecontrol",
				Context.MODE_PRIVATE);
		nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
		nsdCallback = new NsdManager.RegistrationListener() {

			@Override
			public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
					int errorCode) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onServiceRegistered(NsdServiceInfo serviceInfo) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRegistrationFailed(NsdServiceInfo serviceInfo,
					int errorCode) {
				// TODO Auto-generated method stub

			}
		};
		NsdServiceInfo nsdinfo = new NsdServiceInfo();
		nsdinfo.setServiceType("_tcp.");
		nsdinfo.setServiceName("remoteInput");
		nsdinfo.setPort(RC_TCP_PORT);
		nsdManager.registerService(nsdinfo, NsdManager.PROTOCOL_DNS_SD,
				nsdCallback);
		pool = new RCSensorEventPool();
		tasks[0] = new RCudpService(RC_UDP_PORT, mHandler);
		tasks[1] = new RCtcpService(this,RC_TCP_PORT, mHandler, pool);
		tasks[2] = new RCSensor(-1, mHandler, pool);
		tasks[0].start();
		tasks[1].start();
		tasks[2].start();
		
		new RCtcpThread().start(); 
		
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if(mBluetoothAdapter != null){
				if (mBluetoothAdapter.isDiscovering()) {
		            mBluetoothAdapter.cancelDiscovery();
		        }

		        // Request discover from BluetoothAdapter
		        mBluetoothAdapter.startDiscovery();
				}
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(task, 5000, 60000);
		
		mStatus = SERVICE_STATUS_RUNNING;
		cmdReceiver = new ServiceExecListener();
		IntentFilter filter = new IntentFilter();
		filter.addAction(REMOTECMD);
		registerReceiver(cmdReceiver, filter);
		createMouseView();
		sedIntent2View(SERVICE_STATUS_RUNNING);

		String mAbsPath = getApplicationContext().getFilesDir()
				.getAbsolutePath();
		File file = new File(mAbsPath + "/utooir.apk");
		if (!file.exists()) {
			copyAsset("utooir.apk", "utooir.apk");
			try {
				UnzipFile.unzipFile(mAbsPath + "/",
						this.getAssets().open("webcontent.zip"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		startJetty();
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mStatus == SERVICE_STATUS_STOP) {
			tasks[0].onResume();
			tasks[1].onResume();
			tasks[2].onResume();
		}

		if (mBluetoothAdapter != null && (!mBluetoothAdapter.isEnabled())) {
			// Intent enableIntent = new
			// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(discoverableIntent);
            
			
           // mBluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 3600);
			
			if (mBluetoothService == null)
				mBluetoothService = new RCBluetoothService(this, mHandler,pool);
				mBluetoothService.start();
		}

		StartServiceFlag = true;
		removeMouseView();
		mStatus = SERVICE_STATUS_RUNNING;
		sedIntent2View(SERVICE_STATUS_RUNNING);
		return super.onStartCommand(intent, flags, startId);
	}

	private void createMouseView() {
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = mInflater.inflate(R.layout.mouse_window, null);
		mouseHandler = (ImageView) mView.findViewById(R.id.mouse_image);
		mouseHandler
				.setImageResource(mPrefs.getInt("mouse", R.drawable.mouse0));
		mWm = (WindowManager) this.getApplicationContext().getSystemService(
				Context.WINDOW_SERVICE);
		mWmParams = new WindowManager.LayoutParams();
		mWmParams.type = 2018;// LayoutParams.TYPE_POINTER; // window type
		mWmParams.format = PixelFormat.RGBA_8888;
		mWmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| LayoutParams.FLAG_NOT_TOUCHABLE
				| LayoutParams.FLAG_NOT_FOCUSABLE
				| LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		mWmParams.gravity = Gravity.LEFT | Gravity.TOP;

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getMetrics(dm);

		mWScreenx = dm.widthPixels;
		mWScreeny = dm.heightPixels;
		if (DEBUG) {
			Log.d(TAG, "create view mWScreenx:" + mWScreenx + " mWScreeny:"
					+ mWScreeny);
		}
		mWmParams.x = mWScreenx / 2;
		mWmParams.y = mWScreeny / 2;
		mWmParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		mWmParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		mTimer = new Timer(true);
	}

	private void startTimerTask() {
		if (mTimer != null) {
			if (mTimerTask != null) {
				mTimerTask.cancel();
			}
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					removeMouseView();
				}
			};
			mTimer.schedule(mTimerTask, 6000);
		}
	}

	private void removeMouseView() {
		if (mShowMouse) {
			try {
				mWm.removeView(mView);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				mShowMouse = false;
			}
		}
	}

	private void showMouseView() {
		if (!mShowMouse) {
			try {
				mWm.addView(mView, mWmParams);
			} catch (Exception ex) {
			} finally {
				mShowMouse = true;
			}
		}
	}

	private void valid() {
		if (mScreenWidth == 0 || mScreenHeight == 0) {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			manager.getDefaultDisplay().getMetrics(dm);

			mScreenWidth = dm.widthPixels;
			mScreenHeight = dm.heightPixels;
		}
		if (DEBUG) {
			Log.d(TAG, "valid mScreenWidth" + mWmParams.x + " mScreenHeight:"
					+ mWmParams.y);
		}
		if (mWmParams.x > mScreenWidth) {
			mWmParams.x = mScreenWidth;
		} else if (mWmParams.y > mScreenHeight) {
			mWmParams.y = mScreenHeight;
		} else if (mWmParams.x < 0) {
			mWmParams.x = 0;
		} else if (mWmParams.y < 0) {
			mWmParams.y = 0;
		}
	}

	class ServiceExecListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			int cmd = intent.getIntExtra("cmd", -1);
			if (cmd == CMD_STOP_SERVICE) {
				tasks[0].Pause();
				tasks[1].Pause();
				tasks[2].Pause();
				// try {
				// mServer.stop();
				//
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				mStatus = SERVICE_STATUS_STOP;
				sedIntent2View(SERVICE_STATUS_STOP);
			}
			if (cmd == CMD_START_SERVICE) {
				tasks[0].onResume();
				tasks[1].onResume();
				tasks[2].onResume();
				// try {
				// mServer.start();
				//
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				mStatus = SERVICE_STATUS_RUNNING;
				sedIntent2View(SERVICE_STATUS_RUNNING);
			}
			if (cmd == CMD_CHANGR_MOUSE && mouseHandler != null) {
				int mouseId = intent.getIntExtra("id", -1);
				mouseHandler.setImageResource(mouseId != -1 ? mouseId
						: R.drawable.mouse0);
			}
			
			if(cmd == CMD_BLUETOOTH_OFF){
				if(mBluetoothService != null)
					mBluetoothService.stop();
			}
			
			if(cmd == CMD_BLUETOOTH_ON){
				if(mBluetoothService != null)
					mBluetoothService.stop();
				else if (mBluetoothService == null)
					mBluetoothService = new RCBluetoothService(RCService.this, mHandler,pool);
				mBluetoothService.start();
			}
		}
	}

	private void sedIntent2View(int status) {
		Intent s2cIntent = new Intent();
		s2cIntent.setAction(SERVERSTATUS);
		s2cIntent.putExtra("status", status);
		// sendBroadcastAsUser(s2cIntent, -2);
		sendBroadcast(s2cIntent);
	}

	private void startJetty() {
		String jetty_home = "jetty";
		int port = 8020;
		mServer = new Server();
		Connector connector = new SelectChannelConnector();
		connector.setPort(port);
		mServer.addConnector(connector);
		ServletContextHandler servletHandler = new ServletContextHandler(
				ServletContextHandler.NO_SESSIONS);
		servletHandler.setContextPath("/socket");
		// servletHandler.setResourceBase(this.getFilesDir().getAbsolutePath());
		// servletHandler.setWelcomeFiles(new String[]{"index.html"});

		ResourceHandler rs = new ResourceHandler();
		rs.setDirectoriesListed(false);
		rs.setWelcomeFiles(new String[] { "wcontrol.html" });
		rs.setResourceBase(this.getFilesDir().getAbsolutePath());

		ServletHolder socket = new ServletHolder(new ControlServlet());// or new
																		// ControlSocketServlet(this);
		servletHandler.addServlet(socket, "/*");
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new org.eclipse.jetty.server.Handler[] { rs,
				servletHandler });
		mServer.setHandler(handlers);
		String ipaddr = HostInterface.getIPv4Address();
		if (ipaddr.length() < 5)
			return;
		try {
			mServer.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
		// server.join();
	}

	String copyAsset(String absPath, String filename) {
		InputStream i_s = null;
		FileOutputStream fos;

		String sourcefilename = absPath + "/" + filename;

		File f = new File(sourcefilename);

		try {
			i_s = getAssets().open(filename);
			fos = openFileOutput(filename, Context.MODE_PRIVATE);
			while (i_s.available() > 0) {
				byte[] b = new byte[1024];
				int bytesread = i_s.read(b);
				fos.write(b, 0, bytesread);
			}
			fos.close();
			i_s.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return f.getName();
	}

	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		tasks[0].Pause();
		tasks[1].Pause();
		tasks[2].Pause();
		if (mBluetoothService != null)
			mBluetoothService.stop();
		sedIntent2View(SERVICE_STATUS_STOP);
		mStatus = SERVICE_STATUS_STOP;
		this.unregisterReceiver(cmdReceiver);
		nsdManager.unregisterService(nsdCallback);
		try {
			mServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
