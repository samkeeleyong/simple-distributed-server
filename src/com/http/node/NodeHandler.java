package com.http.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.Constants;

public class NodeHandler implements HttpHandler {

	private String username;
	private String nodename;
	
	public NodeHandler(String username, String nodename) {
		this.username = username;
		this.nodename = nodename;
	}
	
	public void handle(HttpExchange t) throws IOException {
		Headers h = t.getResponseHeaders();
		String filename = queryToMap(t.getRequestURI().getQuery());

//		String.format("/home/%s/%s/%s", fromUsername, fromNodeName, filename),
		File file = new File(String.format("/home/%s/%s/%s",username, nodename, filename));

		FileInputStream fis = new FileInputStream(file);
		OutputStream os = t.getResponseBody();
		byte[] buffer = new byte[1024];
		int numRead;

		h.set("Content-disposition", "attachment; filename=" + filename);

		t.sendResponseHeaders(200, file.length());

		while ((numRead = fis.read(buffer, 0, buffer.length)) != -1) {
			os.write(buffer, 0, numRead);
		}

		os.close();
		fis.close();
	}

	public String queryToMap(String query) {
		String result = null;

		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair[0].equals("filename")) {
				result = pair[1];
			}
		}
		return result;
	}
}