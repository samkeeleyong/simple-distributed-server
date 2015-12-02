package com.http.gateway;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.Constants;

public class UploadPageHandler implements HttpHandler{
    
    public void handle(HttpExchange exchange) throws IOException{

        String response ="<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head lang=\"en\">\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title></title>\n" +
            "    <script type=\"text/javascript\">\n" +
            "\n" +
            "        function processForm(frm)\n" +
            "        {\n" +
            "            var fu1 = document.getElementsByName(\"datafile\");\n" +
            "            var filename = fu1[0].value;\n" +
            "            filename = filename.substring(filename.lastIndexOf(\"\\\\\")+1);\n" +
            "            \n" +
            "			if(frm.datafile.value.trim()==''){\n" +
            "				alert(\"Please select file!\");\n" +
           "                                frm.action = \"http://" + Constants.GATEWAY_IP + "/mainPage\";\n" +
            "			} else {\n" +
            "				alert(\"You selected \" + filename);\n" +
            "				frm.action = \"http://" + Constants.GATEWAY_IP + "/upload/?fileName=\"+filename;\n" +
            "			}\n" +
            "				return true;\n" +
            "        }\n" +
            "\n" +
            "    </script>\n" +
            "</head>\n" +
            "<body>\n" +
            "\n" +
            "<form name=\"myForm\" enctype=\"multipart/form-data\" method=\"post\" acceptcharset=\"UTF-8\" onsubmit=\"processForm(this);\">\n" +
            "    <p>\n" +
            "        Choose a file:<br>\n" +
            "        <input type=\"file\" name=\"datafile\" size=\"40\">\n" +
            "    </p>\n" +
            "    <div>\n" +
            "        <input type=\"submit\" value=\"Upload\">\n" +
            "    </div>\n" +
            "</form>\n" +
            "\n" +
            "</body>\n" +
            "</html>";
                
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();  
    }        
}