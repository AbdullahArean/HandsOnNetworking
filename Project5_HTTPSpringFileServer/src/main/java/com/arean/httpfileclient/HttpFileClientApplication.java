package com.arean.httpfileclient;

        import java.io.*;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.nio.file.Files;
        import java.util.Scanner;

public class HttpFileClientApplication {

    private static final String boundary =  "*****";
    private static final String space = "\r\n";
    private static final String gap = "--";
    private static Scanner scn;

    public static void main(String[] args) throws IOException {
        scn =new Scanner(System.in);
        while(true){
            System.out.println("What do want to do?\n1. POST (Uplaod)\n2. GET (Download)\n3. Exit");
            String userinp = scn.nextLine();
            switch (userinp){
                case "1":
                        filesendprompt();
                        break;
                case "2":
                    filereceiveprompt();
                    break;
                case "3":
                    System.out.println("Successfully Exited");
                    return;
                default:
                    System.out.println("Invalid Input. Try again with a valid input.");
            }

        }
    }
    private static void filesendprompt() throws IOException {
        while (true)
        {
            System.out.println("What do you want to Upload to server?\n1. file1.txt\n2. file2.txt\n3. file3.txt\n4. Back to main");
            String tosend = scn.nextLine();
            if(tosend.equals("1")){ filesend("file1.txt");}
            else if(tosend.equals("2")){ filesend("file2.txt");}
            else if(tosend.equals("3")){ filesend("file3.txt");}

            else if(tosend.equals("4"))
            {
                return;
            }
            else{
                System.out.println("No such file. Try again with a valid input.");
            }
        }

    }
    private static void filereceiveprompt() throws IOException {
        while (true)
        {
            System.out.println("What do you want to Download from server?\n1. file1.txt\n2. file2.txt\n3. file3.txt\n4. Back to main");
            String tosend = scn.nextLine();
            if(tosend.equals("1")){ fileget("file1.txt");}
            else if(tosend.equals("2")){ fileget("file2.txt");}
            else if(tosend.equals("3")){ fileget("file3.txt");}

            else if(tosend.equals("4"))
            {
                return;
            }
            else{
                System.out.println("No such file. Try again with a valid input.");
            }
        }

    }
    private static void filesend(String filename) throws IOException {
        URL targetUrl = new URL("http://localhost:8080/post/");
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); // Indicates file transmission
        connection.setDoOutput(true); // Indicates POST request
        DataOutputStream requestStream = new DataOutputStream(connection.getOutputStream());
        String workingDirectory = System.getProperty("user.dir");
        String absoluteFilePath = "";
        absoluteFilePath = workingDirectory + File.separator +"clientstorage" + File.separator + filename;
        File fileToSend = new File(absoluteFilePath);
        String fileName = fileToSend.getName();
        String fieldName = "file";
        requestStream.writeBytes(gap + boundary + space);
        requestStream.writeBytes("Content-Disposition: form-data; name=\"" +
                fieldName + "\";filename=\"" +
                fileName + "\"" + space);
        requestStream.writeBytes(space);

        byte[] fileBytes = Files.readAllBytes(fileToSend.toPath());
        requestStream.write(fileBytes);
        requestStream.writeBytes(space);
        requestStream.writeBytes(gap + boundary + gap + space);
        requestStream.flush();
        requestStream.close();

        int responseCode = connection.getResponseCode();
        System.out.println("POST Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = responseReader.readLine()) != null) {
                response.append(inputLine);
            }
            responseReader.close();
            System.out.println(response+ "\nSaved to uploads folder!");
        }
        else {
            System.out.println("POST request did not succeed.");
        }
    }
    private static void fileget(String filename) throws IOException {
        String url = "http://localhost:8080/get/"+filename;
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        System.out.println("GET Response Code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = extractFileName(conn, url);
            InputStream inputStream = conn.getInputStream();
            String workingDirectory = System.getProperty("user.dir");
            String saveFilePath = workingDirectory + File.separator +"clientstorage"+ File.separator + "received"+System.currentTimeMillis()+fileName;
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
            int bytesRead;
            byte[] buffer = new byte[400096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            System.out.println("File downloaded");
        } else {
            System.out.println("GET request failed");
        }
    }

    private static String extractFileName(HttpURLConnection conn, String url) {
        String fileName = "";
        String disposition = conn.getHeaderField("Content-Disposition");
        if (disposition != null) {
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                fileName = disposition.substring(index + 10,
                        disposition.length() - 1);
            }
        } else {
            fileName = url.substring(url.lastIndexOf("/") + 1);
        }
        return fileName;
    }

}