package com.namenode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;

import com.http.node.NodeHandler;
import com.sun.net.httpserver.HttpServer;
import com.util.SftpChannel;
import com.util.SftpService;

public class Node {

	private static final String NODE_NAME = "Node1";
	private static final int HTTP_PORT = 8081;
	private static final String HTTP_HOST = "localhost";
	
	
	private static final int PORT = 4444;
	private static final String SERVER_ADDRESS = "localhost";

	private static final String HOST = "localhost";
	private static final String SFTP_PORT = "22";
	private static final String USERNAME = "guestuser";
	private static final String PASSWORD = "guestuser";

	private static BufferedReader in;
	private static PrintWriter printWriter;

	public static void main(String[] args) throws IOException {
		System.out.println(NODE_NAME + " is Running.");
		
		HttpServer server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        server.createContext("/get/", new NodeHandler(USERNAME, NODE_NAME));
        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
        server.start();
        
		try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			printWriter = new PrintWriter(socket.getOutputStream(), true);

			// send register message
			printWriter.println(String.format("REGISTER,%s,%s,%s,%s,%s,%s,%d",
					NODE_NAME, HOST, Long.parseLong(SFTP_PORT), USERNAME, PASSWORD, HTTP_HOST, HTTP_PORT));

			// Process all messages from server, according to the protocol.
			while (true) {
				String line = in.readLine();
				if (line.startsWith("SENDFILE")) {
					String[] sendFileParams = line.split(",");
					
					System.out.println(NODE_NAME + ": Being asked to send file, " + sendFileParams[6] + ", to " + sendFileParams[5]);
					SftpService.sendFile(sendFileParams[1],
							Integer.parseInt(sendFileParams[2]),
							sendFileParams[3], sendFileParams[4],
							sendFileParams[5], sendFileParams[6], SftpChannel.NODE, USERNAME, NODE_NAME);
					printWriter.println("CONFIRMTASK," + NODE_NAME);
				}
			}
		}
	}
}
