// Java implementation for a client
// Save file as Client.java

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client class
public class Client
{
    private static DataInputStream dis = null;
    private static DataOutputStream dos =null;

    public static void main(String[] args) throws IOException
    {
        try
        {
            Scanner scn = new Scanner(System.in);
            System.out.println("Give The Server IP: ");
            String IP = scn.nextLine();

            // getting localhost ip
            InetAddress ip = InetAddress.getByName(IP);

            // establish the connection with server port 5056
            Socket s = new Socket(ip, 5050);

            // obtaining input and out streams
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of
            // information between client and client handler
            while (true)
            {
                System.out.println(dis.readUTF());
                String tosend = scn.nextLine();
                dos.writeUTF(tosend);
                if(tosend.equals("1")){ receiveFile("file1.txt"); continue;}
                else if(tosend.equals("2")){ receiveFile("file2.txt"); continue;}
                else if(tosend.equals("3")){ receiveFile("file3.txt"); continue;}

                // If client sends exit,close this connection
                // and then break from the while loop
                else if(tosend.equals("4"))
                {
                    System.out.println("Closing this connection : " + s);
                    System.out.println("Connection closed");
                    break;
                }
                else{
                    System.out.println("No such file. Try again with a valid input.");
                    continue;
                }
            }

            // closing resources
            scn.close();
            dis.close();
            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private static void receiveFile(String fileName)
            throws Exception
    {
        String workingDirectory = System.getProperty("user.dir");
        String saveFilePath = workingDirectory + File.separator + "clientstorage"+File.separator+"Received"+System.currentTimeMillis()+"_"+fileName;
        int bytes = 0;
        FileOutputStream fileOutputStream
                = new FileOutputStream(saveFilePath);

        long size
                = dis.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
                && (bytes = dis.read(
                buffer, 0,
                (int)Math.min(buffer.length, size)))
                != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        // Here we received file
        System.out.println("File is Received. Check clientstorage folder to get the file");
        fileOutputStream.close();
    }
}