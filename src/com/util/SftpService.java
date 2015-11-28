package com.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SftpService {
	private static final String OPEN_CHANNEL = "sftp";
	
	public static void sendFile(String HOST, int PORT, String USERNAME, String PASSWORD, String filename) {
		Session session = null;
	    Channel channel = null;
	    try {
	        JSch ssh = new JSch();
	        JSch.setConfig("StrictHostKeyChecking", "no");
//	        ssh.setKnownHosts("/path/of/known_hosts/file");
	        session = ssh.getSession(USERNAME, HOST, PORT);
	        session.setPassword(PASSWORD);
	        session.connect();
	        channel = session.openChannel(OPEN_CHANNEL);
	        channel.connect();
	        ChannelSftp sftp = (ChannelSftp) channel;
	        System.out.println("SFTP Service: sending " + filename + "to /home/guestuser/" + filename);
	        // TODO quick fix
	        sftp.put(filename, "/home/guestuser/" + filename.split("/")[2]);
	    } catch (JSchException e) {
	        e.printStackTrace();
	    } catch (SftpException e) {
	        e.printStackTrace();
	    } finally {
	        if (channel != null) {
	            channel.disconnect();
	        }
	        if (session != null) {
	            session.disconnect();
	        }
	    }
	}
}
