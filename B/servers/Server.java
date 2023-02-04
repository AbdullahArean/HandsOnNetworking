import java.io.*;
import java.net.*;
import com.sun.net.httpserver.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new FileHandler());
        server.setExecutor(null);
        server.start();
    }

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();

            try{
                //Setting Headers for the response message
                Headers responseHeaders = t.getResponseHeaders();
                responseHeaders.add("Access-Control-Allow-Origin", "*");
                responseHeaders.add("Access-Control-Allow-Headers","origin, content-type, accept, authorization");
                responseHeaders.add("Access-Control-Allow-Credentials", "true");
                responseHeaders.add("Access-Control-Allow-Methods", "GET, POST");

                if(method.equals("GET")){
                    try{
                        System.out.println("GET request received");
                        String fileName = t.getRequestURI().toString().substring(1);
                        System.out.println(fileName);
                        t.sendResponseHeaders(200, Files.size(Paths.get(fileName)));
                        Files.copy(Paths.get(fileName), t.getResponseBody());
                        t.getResponseBody().close();
                    }catch (Exception e){
                        byte[] response = "Bad request".getBytes();
                        t.sendResponseHeaders(400, response.length);
                        OutputStream os = t.getResponseBody();
                        os.write(response);
                        os.close();
                    }
                }else if(method.equals("POST")){
                    InputStream is = t.getRequestBody();
                    FileOutputStream fos = new FileOutputStream("b.txt");
                    byte [] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead=is.read(buffer))!=-1){
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();
                    String response = "File uploaded succefully";
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }else{
                    throw new Exception("Not Valid Request Method");
                }
            }catch (Exception e){
                byte[] response = "Bad request".getBytes();
                t.sendResponseHeaders(400, response.length);
                OutputStream os = t.getResponseBody();
                os.write(response);
                os.close();
                e.printStackTrace();
            }
        }
    }
}
