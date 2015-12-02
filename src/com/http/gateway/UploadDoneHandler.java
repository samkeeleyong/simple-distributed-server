package com.http.gateway;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.Constants;

public class UploadDoneHandler implements HttpHandler{
    
    public void handle(HttpExchange exchange) throws IOException{
        
        String response = "<!DOCTYPE html>" +
           "<html>" +
           "<head>" +
           "<title>Upload Done!</title>" +
           "</head>" + 
           "<body>" +
           "<h3>Main Page</h3>"+
           "<p>File Uploaded.</p>"+
           "<a href=\"http://"+  Constants.GATEWAY_IP +"/uploadFile\">Upload</a><br>" +
           "<a href=\"http://"+  Constants.GATEWAY_IP +"/listFiles\">Download</a>" +
           "</1body>" +
           "</html>";
        
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();   
    }
    
}