package com.http.gateway;

import java.io.IOException;

import com.server.NodeRegistry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GatewayRedirectHandler implements HttpHandler{
    
    public void handle(HttpExchange exchange) throws IOException{
    	
    	String filename = queryToMap(exchange.getRequestURI().getQuery());
    	NodeRegistry.NodeEntry redirectedNode = NodeRegistry.decide(filename);
    	
    	System.out.println("This is the node i decided:" + redirectedNode);
    	String host = redirectedNode.nodeHttpHost;
    	int port = redirectedNode.nodeHttpPort;
    	
        String nodeUrl = String.format("http://%s:%d/get/?filename=%s", host, port, filename); 
        System.out.println("Redirecting request to " + nodeUrl);
        redirect(exchange, nodeUrl);
    }
    
    public void redirect(HttpExchange exchange, String uri)
        throws IOException {
        exchange.getResponseHeaders().add("Location", uri);
        exchange.sendResponseHeaders(302, 0);
    }   
    
    public String queryToMap(String query) {
        String result = null;
      
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
                if(pair[0].equals("filename")){
                    result = pair[1];
                }
        }
        return result;
    }            
}
