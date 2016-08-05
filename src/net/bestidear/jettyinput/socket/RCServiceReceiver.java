/** @file RCServiceReceiver.java
 *  @par Copyright:
 *  - Copyright 2014.
 *  @version  1.0
 *  @date     2013/03/03
 *  @par function description:
 *  - 1
 *  @warning This class may explode in your face.
 *  @note If you inherit anything from this class, you're doomed.
 */
package net.bestidear.jettyinput.socket;

import net.bestidear.jettyinput.DeviceTools;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class RCServiceReceiver extends BroadcastReceiver {
    private static String TAG = "RCService";
    public static String PREF_REBOOTCOMPLE = "remotecontrol";
    public static String PREF_REBOOT_START = "REBOOT_AUTO_START";
    private static String START_SERVICE = "net.bestidear.remoteControl.RC_START";
    private static String STOP_SERVICE = "net.bestidear.remoteControl.RC_STOP";

    @Override public void onReceive( Context context, Intent intent ) {
    	
    	// test to boot completed to open service
    	
    	Intent serviceIntent = new Intent(context, RCService.class );
        context.startService(serviceIntent);
    	
    	int flag=1;
    	flag=Settings.Global.getInt(context.getContentResolver(), "mobile_remote_on",1);
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) &&(flag==1)){
                Intent serviceLauncher = new Intent(context, RCService.class );
                context.startService(serviceLauncher);
        }
        else if(intent.getAction().equals("net.bestidear.remote.action.CONTROL"))
        {//添加设置控制是否启动
        	flag=intent.getIntExtra("mobile_remote_on", 1);
        	Log.i(TAG,"---------------mobile_remote_on flag is:"+flag);
        	 if(flag==1)
        	 {
        		  Intent serviceLauncher = new Intent(context, RCService.class );
                  context.startService(serviceLauncher);
        	 }
        	 else
        	 {
        		 Intent stopIntent = new Intent();
                 stopIntent.setAction(RCService.REMOTECMD);
                 stopIntent.putExtra("cmd", RCService.CMD_STOP_SERVICE);
                 context.sendBroadcast(stopIntent);
        	 }
        }
        else if(intent.getAction().equals(START_SERVICE)) {
            {
                Intent startIntent = new Intent(context, RCService.class);
                context.startService(startIntent);
            }
        }else if(intent.getAction().equals(STOP_SERVICE)) {
                Intent stopIntent = new Intent();
                stopIntent.setAction(RCService.REMOTECMD);
                stopIntent.putExtra("cmd", RCService.CMD_STOP_SERVICE);
                context.sendBroadcast(stopIntent);
        }else if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") && ( flag==1)){//
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Log.d(TAG,""+(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected())+" "+(manager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).isConnected()));
            if(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()
                || manager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).isConnected()) {
                Intent startIntent = new Intent(context, RCService.class);
                context.startService(startIntent);
                DeviceTools.regDevice(null);
            }else{
                Intent stopIntent = new Intent();
                stopIntent.setAction(RCService.REMOTECMD);
                stopIntent.putExtra("cmd", RCService.CMD_STOP_SERVICE);
                context.sendBroadcast(stopIntent);
            }
        }else if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
        	int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
        	if(bluetoothState == BluetoothAdapter.STATE_OFF){
        		Log.d("zzl:::","this is bluetooth off");
        		
        	/*	Intent stopIntent = new Intent();
                stopIntent.setAction(RCService.REMOTECMD);
                stopIntent.putExtra("cmd", RCService.CMD_BLUETOOTH_OFF);
                context.sendBroadcast(stopIntent);
                */
        	}else{
        		Log.d("zzl:::","this is bluetooth on");
        	/*	Intent startIntent = new Intent();
                startIntent.setAction(RCService.REMOTECMD);
                startIntent.putExtra("cmd", RCService.CMD_BLUETOOTH_ON);
                context.sendBroadcast(startIntent);
                */
        	}
        }
    }
}
