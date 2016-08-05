/**
 * @file RCtcpService.java
 * @par Copyright: - Copyright 2014.
 * @version 1.0
 * @date 2013/03/03
 * @par function description: - 1
 * @warning This class may explode in your face.
 * @note If you inherit anything from this class, you're doomed.
 */
package net.bestidear.jettyinput.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class RCtcpService extends TaskThread {
	final Instrumentation mInstrumentation = new Instrumentation();

	class RecvTask extends Thread {
		private InputStream mIs = null;
		private boolean stop = false;

		RecvTask(InputStream is) {
			if (is != null) {
				this.mIs = is;
			}
		}

		public void pause() {
			if (!stop) {
				stop = true;
			}
			if (mIs != null) {
				try {
					mIs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mIs = null;
			}
		}

		private byte[] readCmd() throws IOException {
			int bytes_left = 0;
			int bytes_read = 0;
			byte len_data[] = new byte[4];
			if (mIs == null || mIs.read(len_data, 0, 4) < 4) {
				return null;
			}
			int data_len = 0;
			for (int i = 0; i < 4; i++) {
				data_len += (len_data[i] & 0xff) << (8 * (3 - i));
			}
			if (data_len < 0 || data_len > RCService.RC_DATA_LEN) {
				return null;
			}
			bytes_left = data_len;
			byte[] read_recv = new byte[data_len];
			while (bytes_left > 0) {
				int read_len = recvStream.read(read_recv, bytes_read,
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

		@Override
		public void run() {
			byte[] cmd = null;
			while (!stop && mIs != null) {
				try {
					cmd = readCmd();
					Log.d("zzl:::", "this is remote service receive::" + cmd);
				} catch (IOException e) {
					continue;
				}
				if (cmd != null && cmd.length > 3) {
					if (RCService.DEBUG) {
						Log.d(TAG, "cmd:" + cmd[0] + "cmd.length" + cmd.length
								+ "time:" + System.currentTimeMillis());
					}
					int operation = cmd[0] & 0xff;
					switch (operation) {
					case RC_EVENT_TYPE_KEY:
						if (cmd.length > 3) {
							int code = cmd[2] & 0xff;
							int action = cmd[1] & 0xff;
							if (code == KEYCOD_EPAD_MOUSE_SELECT) {
								if (isMouseMode) {
									int loc[] = ((RCService.ServiceHandler) mHandler)
											.getCurLoc();
									int what;
									what = (KeyEvent.ACTION_DOWN == action) ? MotionEvent.ACTION_DOWN
											: MotionEvent.ACTION_UP;
									select = true;
									sendTouchEvent(what, (loc[0] - 2),
											(loc[1] - 2));
								}
							} else if (action == KeyEvent.ACTION_DOWN){
								//mInstrumentation.sendKeyDownUpSync(KeyMap
								//		.getMap(code));
								
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
					case RC_EVENT_TYPE_TRACKBALL:
						break;
					case RC_EVENT_TYPE_SWITCH_MODE:
						if (cmd.length < 4) {
							break;
						}
						if ((cmd[1] & 0xff) == KeyEvent.ACTION_UP
								&& (cmd[2] & 0xff) == KEYCOD_EPAD_MOUSE_SWITCH) {
							isMouseMode = ((cmd[3] & 0xff) == 1) ? true : false;
							if (isMouseMode) {
								setMousebyTcp(RCService.OSD_SHOW, 0, 0, null);
								Log.d("zzl:::", "this is tcp mouse show ");
							} else {
								setMousebyTcp(RCService.OSD_HIDE, 0, 0, null);
								Log.d("zzl:::", "this is tcp mouse hide ");
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
					}
				} else {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private boolean isMiracastRunning(){
    	
   	 ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
   	    List<RunningTaskInfo> list = am.getRunningTasks(100);
   	    for (RunningTaskInfo info : list) {
   	        if (info.topActivity.getPackageName().equals("com.amlogic.miracast") && info.baseActivity.getPackageName().equals("com.amlogic.miracast")) {
   	           return true;
   	        }
   	    }
   	return false;
   }
	
	public static final int KEYCOD_EPAD_MOUSE_SELECT = 201;
	public static final int KEYCOD_EPAD_MOUSE_SWITCH = 200;
	public static final int MAX_TIME_OF_DOUBLE_TAP_SETP1 = 150;
	public static final int MAX_TIME_OF_DOUBLE_TAP_SETP2 = 500;
	public static final int MIN_TIME_OF_DOUBLE_TAP_SETP1 = 0;
	public static final int MIN_TIME_OF_DOUBLE_TAP_SETP2 = 50;
	/*-----------------------------------------------------------------------------*/
	public static final int PAD_MOUSE_SENSITIVE_RANGE = 20;
	public static final int PAD_MOUSE_TIMER = 5000; // ms
	public static final int RC_EVENT_TYPE_ACK = 0; // client<-->server
	// client<--server
	public static final int RC_EVENT_TYPE_GET_PICTURE = 6; // client-->server
	public static final int RC_EVENT_TYPE_KEY = 1; // client-->server
	public static final int RC_EVENT_TYPE_SENSOR = 4; // client-->server
	// touch or mouse mode
	public static final int RC_EVENT_TYPE_SERVICE = 8;
	// client get server framebuffer
	public static final int RC_EVENT_TYPE_SWITCH_MODE = 7; // client-->server
	public static final int RC_EVENT_TYPE_TOUCH = 2; // client-->server
	public static final int RC_EVENT_TYPE_TRACKBALL = 3; // client-->server
	public static final int RC_EVENT_TYPE_UI_STATE = 5; // 5.
	private static String TAG = RCService.TAG;
	public static final String READCMDS = "com.amlapp.remotecontrol.cmds";
	private Socket clntSock = null;
	private int curKey = -1;
	private boolean isMouseMode = false;
	private long Key_cur_TIME = 0;
	private int last_x = 0;
	private int last_y = 0;
	private SparseArray<MotionEvent.PointerCoords> mPointers = new SparseArray<MotionEvent.PointerCoords>();
	private int pointerId = 0;
	private RecvTask receiver = null;
	private InputStream recvStream = null;
	private OutputStream sendStream = null;
	private ServerSocket servSock = null;
	private boolean select = false;
	private RCSensorEventPool pool = null;
	private int metaDate = 0;
	private Context mContext;
	private SharePreferences sharePreferences;
	
	public RCtcpService(Context context,int port, Handler handler, RCSensorEventPool pool) {
		super(port, handler);
		this.pool = pool;
		mContext = context;
		sharePreferences = new SharePreferences(mContext);
	}

	/**
     *
     */
	protected void Destory() {
	}

	/**
     *
     */
	protected void onPrepare() {
		try {
			servSock = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasClientConnect() {
		if (recvStream == null || sendStream == null) {
			return false;
		}
		return true;
	}

	/**
     *
     */
	protected void onRunning() {
		while (servSock != null && mStatus == RUNNING_STATUS_RUNNING) {
			try {
				clntSock = servSock.accept();
				recvStream = clntSock.getInputStream();
				sendStream = clntSock.getOutputStream();
				if (recvStream == null || sendStream == null) {
					clntSock.close();
				} else {
					if (receiver != null) {
						receiver.pause();
						receiver = null;
					}

					receiver = new RecvTask(recvStream);
					if (receiver != null) {
						receiver.start();
					}
				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
	}

	/**
     *
     */
	protected void onStop() {
		if (receiver != null) {
			receiver.pause();
			receiver = null;
		}
		try {
			if (null != recvStream) {
				recvStream.close();
				recvStream = null;
			}
			if (null != sendStream) {
				sendStream.close();
				sendStream = null;
			}
			if (null != servSock) {
				servSock.close();
				servSock = null;
			}
		} catch (IOException ex) {
			Log.i(TAG, "Tcp disclose exception " + ex.fillInStackTrace());
		}
	}

	/**
	 * @param i
	 * @param x
	 * @param y
	 */
	private void sendTouchEvent(int action, int xv, int yv) {
		Log.d("zzl:::", "******" + "this is tcp mouse show event");
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

	/**
	 * @param action
	 * @param xv
	 * @param yv
	 */
	private void sendMouseEvent(int action, int xv, int yv) {
		if (RCService.DEBUG)
			Log.d(TAG, "x value:" + xv + "yv:" + yv);
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
}
