package com.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SftpService {
	private static final String OPEN_CHANNEL = "sftp";

	public static void sendFile(String host, int port, String username,
			String password, String nodeName, String filename, SftpChannel sftpChannel) {
		new Thread(new SenderThread(host, port, username, password, nodeName, filename, sftpChannel)).start();
	}

	public static void sendFile(String host, int port, String username,
			String password, String nodeName, String filename, SftpChannel sftpChannel, String fromUsername, String fromNodeName, 
			String fromNodeContextPath, String toNodeContextPath) {
		new Thread(new SenderThread(host, port, username, password, nodeName, filename, sftpChannel, fromUsername, fromNodeName, 
									fromNodeContextPath, toNodeContextPath)).start();
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

		protected SenderThread(String host, int port, String username,
				String password, String nodeName, String filename, SftpChannel sftpChannel) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.nodeName = nodeName;
			this.filename = filename;
			this.sftpChannel = sftpChannel;
		}
		
		protected SenderThread(String host, int port, String username,
				String password, String nodeName, String filename, SftpChannel sftpChannel, String fromUsername, String fromNodeName,
				String fromNodeContextPath, String toNodeContextPath) {
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
		}

		@Override
		public void run() {
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
					System.out.printf("Sending %s to %s \n",
							Constants.GATEWAY_DIRECTORY + filename,
							String.format("/home/%s/%s/%s", username,nodeName, filename));
					
					sftp.put(Constants.GATEWAY_DIRECTORY + filename, 
							 String.format("/home/%s/%s/%s", username, nodeName, filename));
				} else if (sftpChannel.equals(SftpChannel.NODE)) {
					System.out.printf("Sending %s to %s \n",
							String.format("/%s/%s/%s/%s", fromNodeContextPath, fromUsername, fromNodeName, filename),
							String.format("/%s/%s/%s/%s", toNodeContextPath, username,nodeName, filename));
					
					sftp.put(String.format("/%s/%s/%s/%s", fromNodeContextPath, fromUsername, fromNodeName, filename), 
							 String.format("/%s/%s/%s/%s", toNodeContextPath, username, nodeName, filename));
				}
			} catch (JSchException e) {
				e.printStackTrace();
			} catch (SftpException e) {
				e.printStackTrace();
			} finally {
//            	System.out.println("Finished new file " + filename + " to " + randomNode.nodeName);
				if (channel != null) {
					channel.disconnect();
				}
				if (session != null) {
					session.disconnect();
				}
			}
		}
	}
}
