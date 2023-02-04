import java.io.*;
import java.net.*;

public class FileServer {
    public static void main(String[] args) throws IOException {
        // Set the port number
        int port = 8280;

        // Create the server socket
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            // Listen for incoming connections
            Socket socket = serverSocket.accept();

            // Create a new thread to handle the client
            Thread thread = new FileServerThread(socket);
            thread.start();
        }
    }
}

