import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    static Socket socket = null;
    public static void main(String[] args) throws IOException {
        // Set the server's IP address and the file name you want to download
        String serverIP = "localhost";
        // Create a socket to connect to the server
        socket = new Socket(serverIP, 8480);

        while (true) {
            System.out.println("What do you want to do? 1. Receive (Download) \n2.Send (Upload)\n3.Exit");
            String userinp = (new Scanner(System.in)).nextLine();
            boolean getout=true;
            switch (userinp) {
                case "1" -> {
                    while (getout) {
                        System.out.println("""
                                Give the file name you want to download! (will be downloaded in out folder)
                                1. file1\s
                                2. file2\s
                                3.file3\s
                                4. exit""");
                        switch ((new Scanner(System.in)).nextLine()) {
                            case "1" -> downloadfiles("file1");
                            case "2" -> downloadfiles("file2");
                            case "3" -> downloadfiles("file3");
                            case "4" -> getout = false;
                            default -> System.out.println("Invalid Input");
                        }

                    }
                }
                case "2" -> {
                    while (getout) {
                        System.out.println("""
                                Give the file name you want to upload! (will be uploaded in serverstorage folder)
                                1. file1\s
                                2. file2\s
                                3.file3\s
                                4. exit""");

                        switch ((new Scanner(System.in)).nextLine()) {
                            case "1" -> uploadfiles("file1");
                            case "2" -> uploadfiles("file2");
                            case "3" -> uploadfiles("file3");
                            case "4" -> getout = false;
                            default -> System.out.println("Invalid Input! Try Again");
                        }

                    }
                }
                case "3" -> {
                    // Close the socket
                    socket.close();
                    return;
                }
                default -> System.out.println("Invalid Input");
            }
        }


    }

    private static void uploadfiles(String filename) throws IOException {
        // Open an output stream to send the request
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Open a file input stream to read the file
        String workingDirectory = System.getProperty("user.dir");
        String saveFilePath = workingDirectory + File.separator + "serverstorage"+File.separator+filename+".txt";
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

    private static void downloadfiles(String filename) throws IOException {
        String workingDirectory = System.getProperty("user.dir");
        String saveFilePath = workingDirectory + File.separator + "serverstorage" + File.separator + filename+".txt";



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
            saveFilePath = workingDirectory + File.separator + "clientstorage" + File.separator + "Received" + System.currentTimeMillis() + "_" + filename + ".txt";

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
}