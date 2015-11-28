package com.server;

public class SftpDetails {
	long port;
	String host, username, password;
	
	public SftpDetails(String host, long port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString() {
		return "SftpDetails [port=" + port + ", host=" + host + ", username="
				+ username + ", password=" + password + "]";
	}
}