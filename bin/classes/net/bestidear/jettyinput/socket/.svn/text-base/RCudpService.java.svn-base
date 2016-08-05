/**
 * @file RCudpService.java
 * @par Copyright: - Copyright 2014.
 * @version 1.0
 * @date 2013/03/03
 * @par function description: - 1
 * @warning This class may explode in your face.
 * @note If you inherit anything from this class, you're doomed.
 */
package net.bestidear.jettyinput.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class RCudpService extends TaskThread {
    private static final String TAG             = RCService.TAG;
    private static boolean      mConnectConfirm = false;
    private DatagramPacket      mPackdata       = null;
    private DatagramSocket      mServerSocket   = null;
    
    public RCudpService(int port, Handler handler) {
        super(port, handler);
    }
    
    protected void Destory() {
        mConnectConfirm = false;
        if (mServerSocket != null) {
            mServerSocket.disconnect();
            mServerSocket.close();
            mServerSocket = null;
        }
    }
    
    /**
     *
     */
    @Override
    protected void onPrepare() {
        try {
            mServerSocket = new DatagramSocket(new InetSocketAddress(port));
            if (!mServerSocket.getReuseAddress()) {
                mServerSocket.setReuseAddress(true);
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] recvdata = new byte[RCService.RC_DATA_LEN];
        mPackdata = new DatagramPacket(recvdata, recvdata.length);
    }
    
    /**
     *
     */
    @Override
    protected void onRunning() {
        while (mServerSocket != null && mPackdata != null) {
            if (mStatus == RUNNING_STATUS_RUNNING) {
                try {
                    mServerSocket.receive(mPackdata);
                    String recvStr = new String(mPackdata.getData(), 0,
                            mPackdata.getLength());
                    if (recvStr.equals(RCService.RC_CLIENT_SCAN)) {
                        byte[] sendcode = ((RCService.ServiceHandler) mHandler)
                                .getSendByte();
                        if (((RCService.ServiceHandler) mHandler).isConnect()) {
                            sendcode[4] = 1;
                        }
                        if (RCService.DEBUG) Log.d(TAG, "sendcode.length:" + sendcode.length);
                        DatagramPacket sendPacket = new DatagramPacket(
                                sendcode, sendcode.length,
                                mPackdata.getAddress(), mPackdata.getPort());
                        mServerSocket.send(sendPacket);
                        continue;
                    }
                    if (recvStr.equals(RCService.RC_CLIENT_REQ)) {
                        byte[] sendcode = RCService.RC_CLIENT_REQ_CONFIRM
                                .getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(
                                sendcode, sendcode.length,
                                mPackdata.getAddress(), mPackdata.getPort());
                        mServerSocket.send(sendPacket);
                        mConnectConfirm = true;
                        continue;
                    }
                    if (recvStr.equals(RCService.RC_CLIENT_REQ_OK)) {
                       if (RCService.DEBUG) {
                            Log.d(TAG, "recvStr is RC_CLIENT_REQ_OK");
                        }
                        byte[] sendcode = mConnectConfirm ? RCService.RC_SERVER_LISTEN
                                .getBytes() : RCService.RC_CLIENT_NO_CONNECT
                                .getBytes();
                        mConnectConfirm = false;
                        DatagramPacket sendPacket = new DatagramPacket(
                                sendcode, sendcode.length,
                                mPackdata.getAddress(), mPackdata.getPort());
                        mServerSocket.send(sendPacket);
                        mConnectConfirm = true;
                        continue;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    /**
     *
     */
    @Override
    protected void onStop() {
        mConnectConfirm = false;
    }
}
