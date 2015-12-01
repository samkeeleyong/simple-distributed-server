package com.server;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.util.Constants;
import com.util.SftpChannel;
import com.util.SftpService;

public class Gateway {
	private static final long REPLICATOR_DELAY = 5;
	private static final long REPLICATOR_ITERATION_RATE = 25;
	private static final ScheduledExecutorService REPLICATOR_EXECUTOR= Executors.newSingleThreadScheduledExecutor();
	
	private static final Executor FILE_WATCHER_EXECUTOR= Executors.newSingleThreadExecutor();

	public static void main(String[] args) throws IOException {
		System.out.println("Gateway is running.");
		
		System.out.println("Running replicator thread.");
		REPLICATOR_EXECUTOR.scheduleAtFixedRate(new Replicator(), REPLICATOR_DELAY, REPLICATOR_ITERATION_RATE, TimeUnit.SECONDS);
		
		FILE_WATCHER_EXECUTOR.execute(new NewFileWatcher());
		
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
		private String nodeName;
		
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
				nodeName = registerDetails[1];
				while (true) {
					String input = in.readLine();
					
					if (input.startsWith("CONFIRMTASK")) {
						String nodeName = input.split(",")[1];
					    NodeRegistry.confirmFinishTask(nodeName);
					}
				}
			} catch (IOException e) {
				System.out.println(e);
			} catch (NullPointerException npe) {
				System.out.println("Gateway:" + nodeName + " just died!");
				NodeRegistry.setDead(nodeName);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private static class NewFileWatcher implements Runnable {
		Path gatewayPath = Paths.get(Constants.GATEWAY_DIRECTORY);
		WatchService fileWatcher;
		
		@Override
        public void run() {
			
            try {
            	fileWatcher = gatewayPath.getFileSystem().newWatchService();
            	gatewayPath.register(fileWatcher, ENTRY_CREATE);
            	
                // get the first event before looping
                WatchKey key = fileWatcher.take();
                while(key != null) {
                    // we have a polled event, now we traverse it and 
                    // receive all the states from it
                    for (WatchEvent<?> event : key.pollEvents()) {
                        System.out.printf("Received uploaded file: %s\n", event.context() );
                        // send file to random node
                        if (!NodeRegistry.listEntries().isEmpty()) {
                        	NodeRegistry.NodeEntry randomNode = NodeRegistry.getRandomNode();
                        	String filename = ((Path)event.context()).toString();
                        	System.out.println("SFTP Service: sending new file " + filename + " to " + randomNode.nodeName);
                        	
                        	SftpService.sendFile(randomNode.sftpDetails.host, 
			                        			(int)randomNode.sftpDetails.port,
			                        			randomNode.sftpDetails.username,
			                        			randomNode.sftpDetails.password, randomNode.nodeName, filename, SftpChannel.GATEWAY);
                        	randomNode.filenames.add(filename);
                        	
                        	System.out.println("Finished new file " + filename + " to " + randomNode.nodeName);
                        }
                    }
                    key.reset();
                    key = fileWatcher.take();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
				e.printStackTrace();
			}
            
            System.out.println("Stop Watching");
        }
	}
}
