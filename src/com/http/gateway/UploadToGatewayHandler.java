package com.http.gateway;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.Constants;

public class UploadToGatewayHandler implements HttpHandler {

	@Override
	public void handle(final HttpExchange t) throws IOException {
		for (Map.Entry<String, List<String>> header : t.getRequestHeaders().entrySet()) {
			System.out.println(header.getKey() + ": "
					+ header.getValue().get(0));
		}
		DiskFileItemFactory d = new DiskFileItemFactory();

		try {
			ServletFileUpload up = new ServletFileUpload(d);
			List<FileItem> result = up.parseRequest(new RequestContext() {

				@Override
				public String getCharacterEncoding() {
					return "UTF-8";
				}

				@Override
				public int getContentLength() {
					return 0; // tested to work with 0 as return
				}

				@Override
				public String getContentType() {
					return t.getRequestHeaders().getFirst("Content-type");
				}

				@Override
				public InputStream getInputStream() throws IOException {
					return t.getRequestBody();
				}

			});

			t.getResponseHeaders().add("Content-type", "text/plain");
			t.getResponseHeaders().add("Location",
					"http://" + Constants.GATEWAY_IP + "/uploadDone");
			t.sendResponseHeaders(302, 0);

			OutputStream os = t.getResponseBody();
			for (FileItem fi : result) {
				os.write(fi.getName().getBytes());
				os.write("\r\n".getBytes());
				System.out.println("File-Item: " + fi.getFieldName() + " = "
						+ fi.getName());

				String fileName = fi.getName();
				System.out.println("FILE : " + fileName);

				File path = new File(Constants.GATEWAY_DIRECTORY);// GATEWAY DIR
				if (!path.exists()) {
					boolean status = path.mkdirs();
				}

				File uploadedFile = new File(path + "/" + fileName);
				fi.write(uploadedFile);
				// fileList.add(fileName);
			}
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
