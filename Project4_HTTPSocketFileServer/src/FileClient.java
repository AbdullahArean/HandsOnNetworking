import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    public static void main(String[] args) throws IOException {
        // Set the server's IP address and the file name you want to download
        String serverIP = "localhost";
        System.out.println("What do you want to do? 1. Send\n2.Receive");
        String userinp = (new Scanner(System.in)).nextLine();
        Socket socket = null;
        // Create a socket to connect to the server
        socket = new Socket(serverIP, 8280);
        if (userinp.equals("2")) {
            System.out.println("Give the file name you want to download! (will be downloaded in out folder)");
            String workingDirectory = System.getProperty("user.dir");
            String userinp1 = (new Scanner(System.in)).nextLine();
            String saveFilePath = workingDirectory + File.separator + "serverstorage"+File.separator+userinp1+".txt";
            // Open an output stream to send the GET request
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send the GET request
            out.println("GET /" + saveFilePath + " HTTP/1.0");
            out.println();
            out.flush();

            // Open an input stream to read the server's response
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Read the server's response
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            // Check if the server's response is a "200 OK"
            if (response.toString().startsWith("HTTP/1.0 200 OK")) {
                // Extract the file data from the response
                int fileStart = response.indexOf("\r\n\r\n") + 4;
                int fileEnd = response.length() - 1;
                String fileData = response.substring(fileStart, fileEnd);
                saveFilePath = workingDirectory + File.separator + "clientstorage"+File.separator+"Received"+System.currentTimeMillis()+"_"+userinp1+".txt";

                // Open a file output stream to save the file
                FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);

                // Write the file data to the file output stream
                fileOutputStream.write(fileData.getBytes());

                // Close the file output stream
                fileOutputStream.close();
                System.out.println("File saved to disk.");
            } else {
                System.out.println("Error: " + response);
            }
        }
        else if(userinp.equals("1")){
            // Open an output stream to send the request
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Open a file input stream to read the file
            String workingDirectory = System.getProperty("user.dir");
            String userinp1 = (new Scanner(System.in)).nextLine();
            String saveFilePath = workingDirectory + File.separator + "src"+File.separator+userinp1+".txt";
            FileInputStream fileInputStream = new FileInputStream(saveFilePath);

            // Read the file into a byte array
            byte[] fileData = new byte[fileInputStream.available()];
            fileInputStream.read(fileData);

            // Close the file input stream
            fileInputStream.close();

            // Send a POST request to the server with the file data
            out.println("POST /example.txt HTTP/1.0");
            out.println("Content-Length: " + fileData.length);
            out.println();
            out.write(new String(fileData));
            out.flush();

            // Open an input stream to read the server's response
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Read the server's response
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            // Print the server's response
            System.out.println(response);

        }

        // Close the socket
        socket.close();
    }
}