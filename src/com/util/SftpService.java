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
								String password, String nodeName, String filename) {
		new Thread(new SenderThread(host, port, username, password, nodeName, filename)).start();
	}

	private static class SenderThread implements Runnable {

		private String host;
		private int port;
		private String username;
		private String password;
		private String nodeName;
		private String filename;
		
		protected SenderThread(String host, int port, String username, String password, String nodeName, String filename) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.nodeName = nodeName;
			this.filename = filename;
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
				// TODO quick fix
				System.out
						.printf("Sending %s to %s \n",
								Constants.GATEWAY_DIRECTORY + filename, String
										.format("/home/%s/%s/%s", username,
												nodeName, filename));
				sftp.put(Constants.GATEWAY_DIRECTORY + filename, String.format("/home/%s/%s/%s", username, nodeName, filename));
			} catch (JSchException e) {
				e.printStackTrace();
			} catch (SftpException e) {
				e.printStackTrace();
			} finally {
//				try {
//					Files.delete(Paths.get(Constants.GATEWAY_DIRECTORY + filename));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				
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
