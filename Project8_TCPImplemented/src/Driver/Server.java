package Driver;

import TCP.Tahoe.TCPTahoe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

import static java.lang.Math.min;

public class Server {
    private static final Integer  packetlengthfixed =1460;

    public static void main(String [] args) {
        try {

            // server is listening on port 5056
            ServerSocket serverSocket = new ServerSocket(5051);
            System.out.println("Server started....\nServer IP: "+getIP());

            System.out.println("Waiting for a client ...");

            // running infinite loop for getting
            // client request
            while (true)
            {
                Socket clientSocket = serverSocket.accept();
                // socket object to receive incoming client requests
                System.out.println("[New Client]: " + clientSocket);
                System.out.println("[Client Address]" + clientSocket.getInetAddress().getHostAddress());
                System.out.println("Assigning new thread for this client");
                Thread t = new Thread(new Server.ClientHandler(clientSocket));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ClientHandler implements Runnable {
        public static Socket clientSocket = null;

        public ClientHandler(Socket socket) {
            clientSocket = socket;
        }

        public void run() {
            //String filepath = "28_32srs.pdf";
            //TCPReno.SendPackets( clientSocket, BytePacketize(filepath));

            TCPTahoe.ReceivePackets(clientSocket);
            try {
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
    public static ArrayList<byte[]> BytePacketize(String filepath) throws IOException {
        File file = new File(filepath);
        byte[] fileData = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(fileData);
        fileInputStream.close();
        int offset = 0;
        ArrayList<byte[]> Packetsbyte = new ArrayList<>();
        while (offset < fileData.length) {
            int length = min(packetlengthfixed, fileData.length - offset);
            byte[] packetData = new byte[length];
            System.arraycopy(fileData, offset, packetData, 0, length);
            Packetsbyte.add(packetData);
            offset += length;
        }
        return Packetsbyte;
    }
    private static String getIP(){
        String ip=null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // EDIT
                    if (addr instanceof Inet6Address) continue;

                    ip = addr.getHostAddress();
                    // System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        } catch (SocketException e) {
            System.out.println(e);
        }
        return ip;
    }
}
