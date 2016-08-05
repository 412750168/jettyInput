/** @file RCSensor.java
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
import android.os.Handler;
import java.io.IOException;
import java.io.OutputStream;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;
import java.util.ArrayList;

public class RCSensor extends TaskThread {

    private LocalServerSocket mLocalServerSock = null;
    private LocalSocket mLocalSock = null;
    private LocalSocket mLastSock = null;
    private RCSensorEventPool mSensorPool = null;
    public static boolean SENSOR_DEBUG = false;
    private static String SENSOR_SOCKET = "remote_control_sensor";
    private static final String TAG = RCService.TAG;
    private static boolean mStopSensorFlag;
    private Thread mOperThread = null;
    public RCSensor( int port, Handler handler,RCSensorEventPool mSensorPool ) {
        super( port, handler );
        this.mSensorPool = mSensorPool;
    }
    protected void Destory() {
        if ( mLocalSock != null ) {
            try {
                mLocalSock.close();
            } catch ( IOException ex ){}
             finally  {
                mLocalSock = null;
            }
        }
        if ( mLocalServerSock != null ) {
            try {
                mLocalServerSock.close();
            } catch ( IOException ex ){}
             finally  {
                mLocalServerSock = null;
            }
        }
    }
    protected void onPrepare() {
        try {
            mLocalServerSock = new LocalServerSocket( SENSOR_SOCKET );
            if(SENSOR_DEBUG)
                Log.d( TAG, "create mLocalServerSock successed" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
    protected void onRunning() {
        while ( mLocalServerSock != null ) {
            try {
                mLocalSock = mLocalServerSock.accept();
                if ( mLastSock != null ) {
                    closeSock( mLastSock );
                } else {
                    mLastSock = mLocalSock;
                }
                Log.d( TAG, "accept client successed" );
                mStopSensorFlag = false;
                if(mOperThread!=null){
                    mOperThread.interrupt();
                }
                startSendSensor();
                try {
                    Thread.sleep( 10000 );
                } catch ( InterruptedException ex ){}
            } catch ( Exception e ) {
                e.printStackTrace();
                if ( mLocalSock != null ) {
                    try {
                        mLocalSock.close();
                    } catch ( IOException ex ){}
                     finally  {
                        mLocalSock = null;
                    }
                }
            }
        }
    }
    protected void onStop() {
        mStopSensorFlag = true;
        if ( mLocalSock != null ) {
            closeSock( mLocalSock );
        }
        if(mOperThread!=null) {
            mOperThread.interrupt();
        }
    }
    
    private void startSendSensor() {
        mOperThread = new Thread(){
            public void run() {
                try {
                    byte[] data = mSensorPool.getRCSensor();
                    if ( mLocalSock != null ) {
                        OutputStream out = mLocalSock.getOutputStream();
                        while ( mStopSensorFlag || out != null ) {
                            if ( data != null) {
                                out.write( data, 0, data.length );
                                out.flush();
                            } else {
                                try {
                                    Thread.sleep( 1000 );
                                } catch ( InterruptedException e ){}
                            }
                            data = mSensorPool.getRCSensor();
                        }
                    }
                    } catch ( IOException ex ) {
                        closeSock( mLocalSock );
                        ex.printStackTrace();
                }
            }
        };
        mOperThread.run();
    }
    private void closeSock( LocalSocket sock ) {
        try {
            byte[]buffer = "bye" .getBytes();
            if ( sock.isConnected() ) {
                sock.getOutputStream().write( buffer, 0, 3 );
                sock.shutdownOutput();
                sock.shutdownInput();
                sock.close();
            }
        } catch ( IOException ex ){}
         finally  {
            sock = null;
        }
    }
}

//-------------------------------------------------------------------------

class RCSensorEventPool {
    private static int SensorEvents = 4;
    private ArrayList<byte[]> sm = new ArrayList<byte[]>(SensorEvents);
    public synchronized byte[]getRCSensor() {
        try{  
            while (sm.size() == 0) {
                this.wait();
            }
        }catch(InterruptedException e){  
            e.printStackTrace();  
        }catch(IllegalMonitorStateException e){  
            e.printStackTrace();  
        }
        //Log.d("SensorManager","getRCSensor:"+sm.get(0).toString()+"times:"+System.currentTimeMillis());
        return sm.remove(0);
    }
    
    public synchronized void setRCSensor( byte[]data ) {
        if(sm.size()==SensorEvents){
            sm.remove(0);
        }
        byte[] sensorEvent= new byte[22];
        sensorEvent[3] = ( byte ) ( ( data.length >> 24 ) & 0xff );
        sensorEvent[2] = ( byte ) ( ( data.length >> 16 ) & 0xff );
        sensorEvent[1] = ( byte ) ( ( data.length >> 8 ) & 0xff );
        sensorEvent[0] = ( byte ) ( data.length & 0xff );
        System.arraycopy( data, 0, sensorEvent, 4, data.length );
        sm.add(sensorEvent);
        this.notify();
        //Log.d("SensorManager","setRCSensor:"+data.toString()+"times:"+System.currentTimeMillis());
    }
}
