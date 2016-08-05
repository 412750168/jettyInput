package net.bestidear.jettyinput.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class RCtcpThread extends Thread{

	private  static final int TCP_PORT_2 = 7003;
	private ServerSocket server = null;
	private Socket socket = null;
	
	public RCtcpThread() {
		try {
			server = new ServerSocket(TCP_PORT_2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(server != null){
			try {
				 socket = server.accept();
				TimerTask task = new TimerTask() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(socket != null)
							try {
								socket.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
				};
				Timer timer = new Timer();
				timer.schedule(task, 10000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if(server != null)
					try {
						server.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				e.printStackTrace();
			}
		}
	}
	
}
