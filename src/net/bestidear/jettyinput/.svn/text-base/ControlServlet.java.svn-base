package net.bestidear.jettyinput;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.app.Instrumentation;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class ControlServlet extends HttpServlet {
	
	final Instrumentation mInstrumentation=new Instrumentation();
	private static final long serialVersionUID = 1L;
	private static final String TAG="ControlServlet";
	
	  @Override
	    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
	        throws javax.servlet.ServletException ,IOException 
	    {
	          String test=request.getPathInfo().toLowerCase();
//	          Log.i(TAG,request.getHeader("x-wap-profile"));
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
			    else if(test.startsWith("text_"))
				{
					mInstrumentation.sendStringSync(test.substring(5));
				}
	    };
	    
}
