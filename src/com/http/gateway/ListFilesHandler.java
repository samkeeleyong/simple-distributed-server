package com.http.gateway;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.server.NodeRegistry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.Constants;

public class ListFilesHandler implements HttpHandler{
    
    public void handle(HttpExchange exchange) throws IOException {   
        String StartResponse = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<title>Download Page</title>" +
            "</head>" + 
            "<body>" ;
        String EndResponse = "<br> <br>" +
            "<a href=\"http://" + Constants.GATEWAY_IP + "/mainPage\">Go Back to Main Page</a>" +
            "</body>" +
            "</html>";
        String MiddleResponse = "<h3>Available Files</h3>";
        String finalResponse;
        
        List<String> fileList = NodeRegistry.getAllFiles();
        for (String file: fileList){
            MiddleResponse = MiddleResponse + "<a href=\"http://" + Constants.GATEWAY_IP + "/get/?filename=" +file+"\">"
                    + file + "</a> <br>";
            System.out.println("Listing File:" +file);
        }
        
        finalResponse = StartResponse  + MiddleResponse + EndResponse;
        
        exchange.sendResponseHeaders(200, finalResponse.length());
        OutputStream os = exchange.getResponseBody();
        os.write(finalResponse.getBytes());
        os.close();    
    }
    
}