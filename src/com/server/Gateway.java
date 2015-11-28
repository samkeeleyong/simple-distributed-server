package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.util.Constants;

public class Gateway {
	private static final String USERNAME = "guestuser";
	private static final String HOST = "localhost";
	private static final int PORT = 22;
	private static final String PASSWORD = "guestuser";
	private static final String FILENAME = "/home/samuel/Desktop/jsch-0.1.53.jar";
	
	private static final long REPLICATOR_DELAY = 5;
	private static final long REPLICATOR_ITERATION_RATE = 3;
	private static final ScheduledExecutorService REPLICATOR_EXECUTOR= Executors.newSingleThreadScheduledExecutor();

	public static void main(String[] args) throws IOException {
		System.out.println("Gateway is running.");
		
		
		System.out.println("Running replicator thread.");
		REPLICATOR_EXECUTOR.scheduleAtFixedRate(new Replicator(), REPLICATOR_DELAY, REPLICATOR_ITERATION_RATE, TimeUnit.SECONDS);

		try (ServerSocket listener = new ServerSocket(Constants.SERVER_SOCKET_PORT);) {
			while (true) {
				new Thread(new NodeChannelThread(listener.accept())).start();
			}
		}
	}

	
	/*
	 * @desc This is the single direct communication
	 * 		 between the gateway and a node.
	 * @message SENDFILE - order a node to send a file to another node.
	 * 		    
	 */
	private static class NodeChannelThread implements Runnable {
		private Socket socket;
		private BufferedReader in; // to receive messages
		private PrintWriter printWriter; // to send messages

		public NodeChannelThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				printWriter = new PrintWriter(socket.getOutputStream(), true);

				String[] registerDetails = in.readLine().split(",");				
				NodeRegistry.register(registerDetails[1], 
									  new SftpDetails(registerDetails[2], 
													  Long.parseLong(registerDetails[3]),
													  registerDetails[4],
													  registerDetails[5]),
									  printWriter);
				while (true) {
					String input = in.readLine();
					if (input == null) {
						return;
					}
				}
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
