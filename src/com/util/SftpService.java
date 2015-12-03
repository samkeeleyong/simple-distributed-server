package com.util;

import java.io.PrintWriter;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SftpService {
	private static final String OPEN_CHANNEL = "sftp";

	public static void sendFile(String host, int port, String username,
			String password, String nodeName, String filename, SftpChannel sftpChannel, String toNodeContextPath) {
		new Thread(new SenderThread(host, port, username, password, nodeName, filename, sftpChannel, toNodeContextPath)).start();
	}

	public static void sendFile(String host, int port, String username,
			String password, String nodeName, String filename, SftpChannel sftpChannel, String fromUsername, String fromNodeName, 
			String fromNodeContextPath, String toNodeContextPath, PrintWriter printWriter) {
		new Thread(new SenderThread(host, port, username, password, nodeName, filename, sftpChannel, fromUsername, fromNodeName, 
									fromNodeContextPath, toNodeContextPath, printWriter)).start();
	}

	private static class SenderThread implements Runnable {

		private String host;
		private int port;
		private String username;
		private String password;
		private String nodeName;
		private String filename;
		private SftpChannel sftpChannel;
		
		private String fromUsername;
		private String fromNodeName;
		
		private String fromNodeContextPath;
		private String toNodeContextPath;
		private PrintWriter printWriter;

		protected SenderThread(String host, int port, String username,
				String password, String nodeName, String filename, SftpChannel sftpChannel, String toNodeContextPath) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.nodeName = nodeName;
			this.filename = filename;
			this.sftpChannel = sftpChannel;
			
			this.toNodeContextPath = toNodeContextPath;
		}
		
		protected SenderThread(String host, int port, String username,
				String password, String nodeName, String filename, SftpChannel sftpChannel, String fromUsername, String fromNodeName,
				String fromNodeContextPath, String toNodeContextPath, PrintWriter printWriter) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.nodeName = nodeName;
			this.filename = filename;
			this.sftpChannel = sftpChannel;
			
			this.fromUsername = fromUsername;
			this.fromNodeName = fromNodeName;
			
			this.fromNodeContextPath = fromNodeContextPath;
			this.toNodeContextPath = toNodeContextPath;
			
			this.printWriter = printWriter;
		}

		@Override
		public void run() {
			boolean error = false;
			Session session = null;
			Channel channel = null;
			try {
				JSch ssh = new JSch();
				JSch.setConfig("StrictHostKeyChecking", "no");
				// ssh.setKnownHosts("/path/of/known_hosts/file");
				session = ssh.getSession(username, host, port);
				session.setPassword(password);
				session.connect();
				channel = session.openChannel(OPEN_CHANNEL);
				channel.connect();
				ChannelSftp sftp = (ChannelSftp) channel;
				
				if (sftpChannel.equals(SftpChannel.GATEWAY)) {
					String sendFormat = "%s/%s/%s/%s";
					
					if (toNodeContextPath.startsWith("C")) {
						sendFormat = "%s\\%s\\%s\\%s";
					}
					
					System.out.printf("Sending %s to %s \n",
							Constants.GATEWAY_DIRECTORY + filename,
							String.format(sendFormat, toNodeContextPath, username,nodeName, filename));
					
					sftp.put(Constants.GATEWAY_DIRECTORY + filename, 
							 String.format(sendFormat, toNodeContextPath, username, nodeName, filename));
				} else if (sftpChannel.equals(SftpChannel.NODE)) {
					String sendFormat = "%s/%s/%s/%s";
					
					if (fromNodeContextPath.startsWith("C:")) {
						sendFormat = "%s\\%s\\%s\\%s";
					}
					if (toNodeContextPath.startsWith("C:")) {
						sendFormat = "%s\\%s\\%s\\%s";
					}
					
					System.out.printf("Sending %s to %s \n",
							String.format(sendFormat, fromNodeContextPath, fromUsername, fromNodeName, filename),
							String.format(sendFormat, toNodeContextPath, username,nodeName, filename));
					
					sftp.put(String.format(sendFormat, fromNodeContextPath, fromUsername, fromNodeName, filename), 
							 String.format(sendFormat, toNodeContextPath, username, nodeName, filename));
				}
			} catch (JSchException e) {
				error = true;
				e.printStackTrace();
			} catch (SftpException e) {
				error = true;
				e.printStackTrace();
			} finally {
            	if (!error && sftpChannel.equals(SftpChannel.NODE)) {
					printWriter.println("CONFIRMTASK," + fromNodeName);
            	}
				if (channel != null) {
					channel.disconnect();
				}
				if (session != null) {
					session.disconnect();
				}
				if (!error) {
					System.out.println("Finished Sending Replicate request");
				}
			}
		}
	}
}
