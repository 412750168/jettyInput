package net.bestidear.jettyinput;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class WifiSetServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7329879647923467875L;
	WifiConnect wifiConnect=null;
	WifiManager mWifiManager=null;
	Context mContext;
	
	public WifiSetServlet(Context mContext)
	{
		 this.mContext=mContext;
	}

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		mWifiManager=(WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		wifiConnect=new WifiConnect(mWifiManager);
		List<ScanResult> sr=mWifiManager.getScanResults();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String ssid=req.getParameter("ssid");
		String password=req.getParameter("password");
		wifiConnect.Connect(ssid, password, null);	
		super.doPost(req, resp);
	}

}
