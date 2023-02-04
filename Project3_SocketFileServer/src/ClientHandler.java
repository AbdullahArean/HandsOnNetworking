import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// ClientHandler class
class ClientHandler extends Thread
{
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;


    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run()
    {
        String received;
        while (true)
        {
            try {
                dos.writeUTF("What do you want?[1. file1 | 2. file2 | 3.file3 | 4. EXIT ]..\n"+
                        "Type (1/2/3) to get file or 4 to terminate connection.");
                received = dis.readUTF();

                if(received.equals("4"))
                {
                    System.out.println("Client " + this.s + " sends exit...");
                    this.s.close();
                    System.out.println("Client " + this.s + " Disconnected");
                    break;
                }
                Date date = new Date();
                switch (received) {

                    case "1" :
                        sendFile(
                                "file1.txt");

                        break;
                    case "2" :
                        sendFile(
                                "file2.txt");

                        break;
                    case "3" :
                        sendFile(
                                "file3.txt");

                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    private void sendFile(String fileName)
            throws Exception
    {
        String workingDirectory = System.getProperty("user.dir");
        String saveFilePath = workingDirectory + File.separator + "serverstorage"+ File.separator +fileName;

        int bytes = 0;
        File file = new File(saveFilePath);
        FileInputStream fileInputStream
                = new FileInputStream(file);

        // Here we send the File to Server
        dos.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            // Send the file to Server Socket
            dos.write(buffer, 0, bytes);
            dos.flush();
        }
        System.out.println("File is sent");
        // close the file here
        fileInputStream.close();
    }
}
