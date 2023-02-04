import java.io.*;
import java.net.Socket;
import java.util.Scanner;

class FileServerThread extends Thread {
    private Socket socket;

    public FileServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // Open input and output streams for the client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Read the client's request
            String request = in.readLine();

            // Check if the request is a GET request
            if (request.startsWith("GET")) {
                // Extract the file name from the request
                String fileName = request.substring(request.indexOf("/") + 1, request.lastIndexOf("/") - 5);
                File file = new File(fileName);
                int temp = (int) file.length();

                // Open the requested file
                FileInputStream fileInputStream = new FileInputStream(fileName);

                // Create a byte array to hold the contents of the file
                byte[] fileData = new byte[temp];

                // Read the file into the byte array
                fileInputStream.read(fileData);

                // Send the file data to the client with the appropriate HTTP response headers
                out.write("HTTP/1.0 200 OK\r\n");
                out.write("Content-Type: application/octet-stream\r\n");
                out.write("Content-Length: " + fileData.length + "\r\n");
                out.write("Content-Disposition: attachment; filename=" + fileName + "\r\n");
                out.write("\r\n");
                out.flush();
                socket.getOutputStream().write(fileData);

                // Close the file input stream
                fileInputStream.close();
            }
            // Check if the request is a POST request
            if (request.startsWith("POST")) {
                // Extract the file name and data from the request
                String[] requestLines = request.split("\n");
                //String fileName = requestLines[1].split(" ")[1];
                String fileData = requestLines[requestLines.length - 1];

                // Open a file output stream to save the file
                String workingDirectory = System.getProperty("user.dir");
                String saveFilePath = workingDirectory + File.separator + "serverstorage"+File.separator+"Uploaded_"+System.currentTimeMillis()+".txt";
                FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);

                // Write the file data to the file output stream
                fileOutputStream.write(fileData.getBytes());

                // Close the file output stream
                fileOutputStream.close();

                // Send a "200 OK" response to the client
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("HTTP/1.0 200 OK");
                out.println();
                out.flush();
            } else {
                // Send a "405 Method Not Allowed" response to the client
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("HTTP/1.0 405 Method Not Allowed");
                // Send a "404 Not Found" response if the request is not a GET request
                out.write("HTTP/1.0 404 Not Found\r\n");
                out.write("\r\n");
                out.flush();
            }

            // Close the client's socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
