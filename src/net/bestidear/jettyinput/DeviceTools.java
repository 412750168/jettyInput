package net.bestidear.jettyinput;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public class DeviceTools {
	
	static final String  url="http://yekertech.com/biapi/api/device";
//	static final String  url="http://192.168.2.109:8080/biapi/api/device";
	static final String  packageName="net.bestidear.jettyinput";

	/**
	 * http 请求的回调接口
	 * @author alva
	 *
	 */
	public interface HttpCallbackListener {
		void onFinish(String response);
		
		void onError(Exception e);
	}
	
	/**
	 * 注册添加设备
	 * listener 请求回调，回调中为返回的字符串，json格式，
	 * 其中如含有validTime则是代表应用的有效期，可根据有效期时间设置是否有效。
	 */
	static public void regDevice(final HttpCallbackListener listener)
	{		
    			
    	new Thread(){
			public void run(){
			String responeString="";
		//创建httppost对象
		HttpPost httppost=new HttpPost(url);
		
		//设置包头
		httppost.setHeader("accept", "*/*");
		httppost.setHeader("user-agent", "android");
		httppost.setHeader("content-type", "application/json");
		
		
		JSONObject jsonObject=new JSONObject();
    	try {
			jsonObject.put("mac", getLocalMacAddress());  //mac地址
			jsonObject.put("packageName", packageName);
			jsonObject.put("packageVersion", "3.3.0");
			jsonObject.put("model", Build.MODEL);
			jsonObject.put("finger", Build.FINGERPRINT);
			jsonObject.put("os", "andorid");
			jsonObject.put("osVersion", Build.VERSION.RELEASE);
			jsonObject.put("name",Build.ID);
			jsonObject.put("type", Build.BOARD);
			jsonObject.put("description", "远程遥控");
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}    	
    	
    	
    	try {
			httppost.setEntity(new StringEntity(jsonObject.toString(),"utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DefaultHttpClient defaulthttpclient = new DefaultHttpClient();
		try{
			HttpResponse httpresponse = defaulthttpclient.execute(httppost);
			if (httpresponse.getStatusLine().getStatusCode() == 200){
				
				responeString = EntityUtils.toString(httpresponse.getEntity(), "UTF-8");
				 if(listener!=null){
			        	listener.onFinish(responeString);
			        }
			}
			else
			{
				 if(listener!=null){
			        	listener.onError(null);
			       }
			}
		}
		catch (ClientProtocolException clientprotocolexception){
			responeString = "socket_error";
			clientprotocolexception.printStackTrace();
			 if(listener!=null){
		        	listener.onError(null);
		       }
		}
		catch (IOException ioexception){
			responeString = "socket_error";
			ioexception.printStackTrace();
			 if(listener!=null){
		        	listener.onError(null);
		       }
		}
	}
	}.start();
}
	
	
	/**
	 * 获取设备信息
	 * 根据包名和mac地址获取当前设备的信息
	 * listener 请求回调，回调中为返回的字符串，json格式，
	 * 其中如含有validTime则是代表应用的有效期，可根据有效期时间设置是否有效。
	 */
	static public void getDevice(final HttpCallbackListener listener)
	{
    			
    	new Thread(){
			public void run(){
			String responeString="";
			String geturl=url+"?package="+packageName+"&mac="+URLEncoder.encode(getLocalMacAddress());
		//创建httpget对象
		HttpGet httpget = new HttpGet(geturl);
		//设置包头
		httpget.setHeader("accept", "*/*");
		httpget.setHeader("user-agent", "windows");
		
		DefaultHttpClient defaulthttpclient = new DefaultHttpClient();
		try{
			HttpResponse httpresponse = defaulthttpclient.execute(httpget);
			if (httpresponse.getStatusLine().getStatusCode() == 200){
				
				responeString = EntityUtils.toString(httpresponse.getEntity(), "UTF-8");
				 if(listener!=null){
			        	listener.onFinish(responeString);
			        }
			}
			else
			{
				 if(listener!=null){
			        	listener.onError(null);
			       }
			}
		}
		catch (ClientProtocolException clientprotocolexception){
			responeString = "socket_error";
			clientprotocolexception.printStackTrace();
			 if(listener!=null){
		        	listener.onError(null);
		       }
		}
		catch (IOException ioexception){
			responeString = "socket_error";
			ioexception.printStackTrace();
			 if(listener!=null){
		        	listener.onError(null);
		       }
		}
	}
	}.start();
}
	
	/** 
	 * 返回当前程序版本名 
	 */  
	public static String getAppVersionName(Context context) {  
	    String versionName = "";  
	    try {  
	        // ---get the package info---  
	        PackageManager pm = context.getPackageManager();  
	        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);  
	        versionName = pi.versionName;  
//	        int versioncode = pi.versionCode;
	        if (versionName == null || versionName.length() <= 0) {  
	            return "";  
	        }  
	    } catch (Exception e) {  
	        Log.e("VersionInfo", "Exception", e);  
	    }  
	    return versionName;  
	}
	
	/**
	 * 判断设备安装的应用是否合法
	 * 根据包名和 型号 id 判断 该设备是否能使用
	 * listener 请求回调，回调中为返回的字符串，json格式，
	 * 
	 */
	static public void IsDeviceValid(final HttpCallbackListener listener)
	{
    			
    	new Thread(){
			public void run(){
			String responeString="";
			String geturl=url+"/valid?package="+packageName+"&mode="+Build.MODEL+"&name="+Build.ID;
		//创建httpget对象
		HttpGet httpget = new HttpGet(geturl);
		//设置包头
		httpget.setHeader("accept", "*/*");
		httpget.setHeader("user-agent", "windows");
		
		DefaultHttpClient defaulthttpclient = new DefaultHttpClient();
		try{
			HttpResponse httpresponse = defaulthttpclient.execute(httpget);
			if (httpresponse.getStatusLine().getStatusCode() == 200){
				
				responeString = EntityUtils.toString(httpresponse.getEntity(), "UTF-8");
				 if(listener!=null){
			        	listener.onFinish(responeString);
			        }
			}
			else
			{
				 if(listener!=null){
			        	listener.onError(null);
			       }
			}
		}
		catch (ClientProtocolException clientprotocolexception){
			responeString = "socket_error";
			clientprotocolexception.printStackTrace();
			 if(listener!=null){
		        	listener.onError(null);
		       }
		}
		catch (IOException ioexception){
			responeString = "socket_error";
			ioexception.printStackTrace();
			 if(listener!=null){
		        	listener.onError(null);
		       }
		}
	}
	}.start();
}
	
	/**
	 * 获取当前机器的mac地址
	 * @return 返回mac地址信息
	 */
	static public String getLocalMacAddress() {
		String Mac="";
		try{
			
			String path="sys/class/net/wlan0/address";
			if((new File(path)).exists())
			{
	        	FileInputStream fis = new FileInputStream(path);
				byte[] buffer = new byte[8192];
		        int byteCount = fis.read(buffer);
		        if(byteCount>0)
		        {
		        	Mac = new String(buffer, 0, byteCount, "utf-8");
		        }
			}
	        Log.v("alva***wifi**mac11**", ""+Mac);
	        if(Mac==null||Mac.length()==0)
	        {
	        	path="sys/class/net/eth0/address";
				FileInputStream fis_name = new FileInputStream(path);
				byte[] buffer_name = new byte[8192];
		        int byteCount_name = fis_name.read(buffer_name);
		        if(byteCount_name>0)
		        {
		        	Mac = new String(buffer_name, 0, byteCount_name, "utf-8");
		        }
	        }
	        Log.v("alva***eth0**mac11**", ""+Mac);
	        
	        if(Mac.length()==0||Mac==null){
	        	return "";
	        }
		}catch(Exception io){
			Log.v("alva**exception*", ""+io.toString());
		}
		
		return Mac.trim();
	}
	
}
