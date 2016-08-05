/**
 * @file TaskThread.java
 * @par Copyright: - Copyright 2014 
 * @version 1.0
 * @date 2013/03/03
 * @par function description: 1 parent class for udp ,tcp and sensor thread.
 * @warning This class may explode in your face.
 * @note If you inherit anything from this class, you're doomed.
 */
package net.bestidear.jettyinput.socket;

import android.os.Handler;

public class TaskThread extends Thread {
    public static final int     RUNNING_STATUS_RUNNING = 1;
    public static final int     RUNNING_STATUS_STOP    = 2;
    public static final int     RUNNING_STATUS_UNSTART = 0;
    protected int               mStatus;
    private Thread              mThread;
    protected int               port;
    protected Handler           mHandler;
    
    public TaskThread(int port, Handler handler) {
        this.port = port;
        this.mHandler = handler;
        mStatus = RUNNING_STATUS_UNSTART;
    }
    
    protected void Destory() {}
    
    /**
     *
     */
    protected void onPrepare() {}
    
    /**
     *
     */
    protected void onRunning() {}
    
    /**
     *
     */
    protected void onStop() {}
    
    public void Pause() {
        mStatus = RUNNING_STATUS_STOP;
        onStop();
    }
    
    public void onResume() {
        if (mThread != null && mThread.isAlive()) {
            mStatus = RUNNING_STATUS_RUNNING;
        }
        start();
    }
    
    @Override
    public void run() {
        if (mStatus == RUNNING_STATUS_UNSTART) {
            onPrepare();
        }
        mStatus = RUNNING_STATUS_RUNNING;
        onRunning();
        onStop();
    }
    
    public void start() {
        if (mStatus != RUNNING_STATUS_RUNNING) {
            mStatus = RUNNING_STATUS_UNSTART;
        }
        mThread = new Thread(this);
        mThread.start();
    }
}
