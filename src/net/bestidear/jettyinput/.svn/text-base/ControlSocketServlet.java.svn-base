package net.bestidear.jettyinput;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;



import android.app.Instrumentation;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;


 
public class ControlSocketServlet extends WebSocketServlet
{
	final Instrumentation mInstrumentation=new Instrumentation();
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TAG="ControlSocketServlet";
	private Timer                      mTimer;
    private TimerTask                  mTimerTask;
    private boolean                    mShowMouse             = false;
	Context mContext;
	InputMethodManager mIm;
	InputManager im;
	Surface mSurface;
    private WindowManager              mWm                    = null;
    private WindowManager.LayoutParams mWmParams              = null;
    private int                        mScreenWidth           = 0;
    private int                        mScreenHeight          = 0;
    private int mouseX=0;
    private int mouseY=0;
    private int startX=0;
    private int startY=0;
    
	ImageView  mMouseIcon=null;
        
    public void moveMouse(int X,int Y)
	{
		mouseX +=X;
		mouseY +=Y;
		if(mouseX<(-mScreenWidth/2)) mouseX =mScreenWidth/-2;
		if(mouseY<(-mScreenHeight/2)) mouseY =mScreenHeight/-2;
		if(mouseX>=mScreenWidth/2) mouseX=mScreenWidth/2-1;
		if(mouseY>=mScreenHeight/2) mouseY=mScreenHeight/2-1;
		
		mWmParams.x = mouseX;// - ww/2;
		mWmParams.y = mouseY;// - hh/2;		
		mWm.updateViewLayout(mMouseIcon, mWmParams);		
	}
    
	public void initMouse(int trueX,int trueY) {

		mWm = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		//InputManager im = (InputManager) this.getBaseContext().getSystemService(Context.INPUT_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager manager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getMetrics(dm);	
		
		mWmParams = new WindowManager.LayoutParams();        
		mScreenHeight = dm.heightPixels + 48;
		mScreenWidth = dm.widthPixels;
		 
        mMouseIcon = new ImageView(mContext);
        mMouseIcon.setBackgroundResource(R.drawable.mouse);
        
        mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                      | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                      | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mWmParams.format = PixelFormat.TRANSLUCENT;
        mWmParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;      
        
        mWmParams.x = mouseX;
        mWmParams.y = mouseY;          
        mTimer = new Timer(true);
         
	}
	
	public ControlSocketServlet(Context context)
	{
		super();
		this.mContext=context;		
		
		mIm=(InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
		
		initMouse(0,0);
		
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
	                mWm.removeView(mMouseIcon);
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
	                mWm.addView(mMouseIcon, mWmParams);
	            } catch (Exception ex) {
	            } finally {
	                mShowMouse = true;
	            }
	        }
	    }
	    
	
	final int CONTROL_MOUSE_HIDE=0x0001001;
	final int CONTROL_MOUSE_SHOW=0x0001002;
	final int CONTROL_MOUSE_MOVE =0x0001003;
	final int CONTROL_MOUSE_MOVE_START=0x0001004;
	final int CONTROL_MOUSE_MOVE_END =0x0001005;
	Handler handler=new Handler(){
		
		 @Override  
	        public void handleMessage(Message msg)  
	        {  
	            switch (msg.what)
	            {
	              case CONTROL_MOUSE_MOVE:  
	                  if(mShowMouse)
	                	  moveMouse(msg.arg1-startX,msg.arg2-startY);	                  
	                 break;
	              case CONTROL_MOUSE_SHOW:
	            	  showMouseView();
	            	  if (mTimer != null) {
	                        if (mTimerTask != null) {
	                            mTimerTask.cancel();
	                        }
	                    }
	            	break;
	              case CONTROL_MOUSE_HIDE:
	            	  removeMouseView();
	            	break;
	            	
	              case CONTROL_MOUSE_MOVE_START:
	                    showMouseView();
	                    startX=msg.arg1;
	                    startY=msg.arg2;
	                    try {
	                        mWm.updateViewLayout(mMouseIcon, mWmParams);
	                    } catch (Exception ex) {
	                    }
	                    break;
	                case CONTROL_MOUSE_MOVE_END:
	                    startTimerTask();
	                    break;
	            }  
	        }  
	};

	
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws javax.servlet.ServletException ,IOException 
    {
          String test=request.getPathInfo().toLowerCase();
//          Log.i(TAG,request.getHeader("x-wap-profile"));
		    if(test.startsWith("/control_left"))
		    {
			 mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
		    }
		    else if(test.startsWith("/control_right"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
		    }
		    else if(test.startsWith("/control_up"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
		    }
		    else if(test.startsWith("/control_down"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
		    }
		    else if(test.startsWith("/control_center"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
		    }
		    else if(test.startsWith("/control_enter"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
		    }
		    else if(test.startsWith("/control_volumeup"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP);
		    }
		    else if(test.startsWith("/control_volumedown"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
		    }
		    else if(test.startsWith("/control_home"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
		    }
		    else if(test.startsWith("/control_back"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
		    }
		    else if(test.startsWith("/control_recent"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
		    }
		    else if(test.startsWith("/control_setting"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SETTINGS);
		    }
		    else if(test.startsWith("/control_search"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SEARCH);
		    }
		    else if(test.startsWith("/control_power"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
		    }
		    else if(test.startsWith("/control_clear"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_CLEAR);
		    }
		    else if(test.startsWith("/control_backspace"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
		    }
		    else if(test.startsWith("/control_shift"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SHIFT_LEFT);
		    }
		    else if(test.startsWith("/control_tab"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB);
		    }
		    else if(test.startsWith("/control_pageup"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_PAGE_UP);
		    }
		    else if(test.startsWith("/control_pagedown"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_PAGE_DOWN);
		    }
		    else if(test.startsWith("/control_F6"))
		    {
		    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_F6);
		    }
		    else if(test.startsWith("/control_mousemove"))
		    {
		    	String xpos=request.getParameter("xpos");
		    	String ypos=request.getParameter("ypos");
		    	Log.i(TAG,xpos+":"+ypos);
		    	Message message=new Message();
		    	message.arg1=Integer.parseInt(xpos);
		    	message.arg2=Integer.parseInt(ypos);
		    	message.what=CONTROL_MOUSE_MOVE;
		    	handler.sendMessage(message);
		    }
		    else if(test.startsWith("/control_mouseshow"))
		    {
		    	handler.sendEmptyMessage(CONTROL_MOUSE_SHOW);
		    }
		    else if(test.startsWith("/control_mousehide"))
		    {
		    	handler.sendEmptyMessage(CONTROL_MOUSE_HIDE);
		    }
		    else if(test.startsWith("/control_mouseclick"))
		    {
		    	mInstrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
				        MotionEvent.ACTION_DOWN, mouseX+mScreenWidth/2, mouseY+mScreenHeight/2, 0));
		    	mInstrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
				        MotionEvent.ACTION_UP, mouseX+mScreenWidth/2, mouseY+mScreenHeight/2, 0));
		    	
		    }
		    else if(test.startsWith("text_"))
			{
				mInstrumentation.sendStringSync(test.substring(5));
			}
    };
    
    
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol)
    {
        return new ChatWebSocket();
    }
    
    
    class ChatWebSocket implements WebSocket.OnTextMessage,WebSocket.OnBinaryMessage
    {
       Connection mConnection=null;

		@Override
		public void onClose(int arg0, String arg1) {
			// TODO Auto-generated method stub
			Log.i(TAG,arg1);
		}

		@Override
		public void onOpen(Connection arg0) {
			// TODO Auto-generated method stub
			mConnection=arg0;
		}

		@Override
		public void onMessage(String arg0) {
			// TODO Auto-generated method stub
			Log.i(TAG,arg0);
			if(arg0.startsWith("control_"))
			{
			    if(arg0.equals("control_left"))
			    {
				 mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
			    }
			    else if(arg0.equals("control_right"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
			    }
			    else if(arg0.equals("control_up"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
			    }
			    else if(arg0.equals("control_down"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
			    }
			    else if(arg0.equals("control_center"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
			    }
			    else if(arg0.equals("control_enter"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
			    }
			    else if(arg0.equals("control_volumeup"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP);
			    }
			    else if(arg0.equals("control_volumedown"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
			    }
			    else if(arg0.equals("control_home"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
			    }
			    else if(arg0.equals("control_back"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
			    }
			    else if(arg0.equals("control_pageup"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_PAGE_UP);
			    }
			    else if(arg0.equals("control_pagedown"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_PAGE_DOWN);
			    }
			    else if(arg0.equals("control_F6"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_F6);
			    }
			    else if(arg0.equals("control_recent"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
			    }
			    else if(arg0.equals("control_power"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
			    }
			    else if(arg0.equals("control_clear"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_CLEAR);
			    }
			    else if(arg0.equals("control_backspace"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
			    }
			    else if(arg0.startsWith("control_notify"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_NOTIFICATION);
			    }
			    else if(arg0.equals("control_shift"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SHIFT_LEFT);
			    }
			    else if(arg0.equals("control_tab"))
			    {
			    	mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB);
			    }

			}
			else if(arg0.startsWith("code_"))
			{
//				mInstrumentation.sendStringSync(arg0.substring(5));
				mInstrumentation.sendCharacterSync(Integer.parseInt(arg0.substring(5)));
			}
			else if(arg0.startsWith("text_"))
			{
				mInstrumentation.sendStringSync(arg0.substring(5));
//				Intent intent = new Intent();
//				intent.setAction("net.bestidear.input.sendtext");
//				intent.putExtra("sendtext", arg0.substring(5));
//				mContext.sendBroadcast(intent);
			}
			else if(arg0.startsWith("mouse_"))
			{
				String[] text=arg0.split("_");
				mInstrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
				        MotionEvent.ACTION_DOWN, 20, 100, 0));
			}
			else if(arg0.startsWith("touchstart_"))
			{
				String[] text=arg0.split("_");
				int x=Integer.parseInt(text[1]);
				int y=Integer.parseInt(text[2]);
				Log.i("ControlSocketServlet",arg0);
				Message msg=Message.obtain();
				msg.what=CONTROL_MOUSE_MOVE_START;
				msg.arg1=x;
				msg.arg2=y;
				handler.sendMessage(msg);
//				mInstrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
//				        MotionEvent.ACTION_DOWN, x, y, 0));
			}
			else if(arg0.startsWith("touchend_"))
			{
				String[] text=arg0.split("_");
				int x=Integer.parseInt(text[1]);
				int y=Integer.parseInt(text[2]);
				Message msg=Message.obtain();
				msg.what=CONTROL_MOUSE_MOVE_END;
				msg.arg1=x;
				msg.arg2=y;
				handler.sendMessage(msg);
//				mInstrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
//				        MotionEvent.ACTION_UP, x, y, 0));
			}
			else if(arg0.startsWith("touchmove_"))
			{
				String[] text=arg0.split("_");
				int x=Integer.parseInt(text[1]);
				int y=Integer.parseInt(text[2]);
				Message msg=Message.obtain();
				msg.what=CONTROL_MOUSE_MOVE;
				msg.arg1=x;
				msg.arg2=y;
				handler.sendMessage(msg);
//				mInstrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
//				        MotionEvent.ACTION_MOVE, x, y, 0));
				
			}
		}

		@Override
		public void onMessage(byte[] arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
        
    }
    
    /**
	 * 向指定sys文件中写入指定的信息
	 * @param file   文件路径
	 * @param strWrite 写入的信息
	 * @return true 为写入成功，false为写入失败
	 */
	public static boolean writeFile(String file,String strWrite)
	{
		boolean res = false;
		File filetmp = new File(file);
		if (!filetmp.exists() || !filetmp.canWrite()) {        	
           return false;
        }			
		BufferedWriter out =null;
		try
		{
			out = new BufferedWriter(new FileWriter(file), 32);
    		out.write(strWrite);		
    	    res= true;
    		out.close();			 
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "IOException when write \""+strWrite+"\"to file:"+file+"   Exception info:"+e.getMessage());
		}	
		finally {
			if(out!=null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return res;
	}
    
}
