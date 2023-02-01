import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.net.NetworkInterface;
import java.net.Inet6Address;
import java.util.Enumeration;
import java.net.SocketException;


public class Server {
    // Declare a ServerSocket object
    private ServerSocket serverSocket;

    // Constructor to initialize the server socket
    public Server (ServerSocket serverSocket) {
        this.serverSocket= serverSocket;
    }
    public Server(){
        
    }

    // Method to start the server
    public void startServer(){
        try{
            // Loop until the server socket is closed
            while(!serverSocket.isClosed()){
                // Accept incoming connections
                Socket socket = serverSocket.accept();
                System.out.println("A new Client Just Got Connected");
                // Handle the client connection
                ClientHandler clientHandler = new ClientHandler(socket);
                // Start a new thread for the client
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to close the server socket
    public void closeServerSocket(){
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getIP(){
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

                    // *EDIT*
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
    // Main method
    public static  void main( String[] args) throws IOException {
        String ip = null;
        Server srv = new Server();
        ip = srv.getIP();
        System.out.println("Give The Dedicated Port Number for Group Chat");
        Scanner scanner = new Scanner(System.in);
        int port = scanner.nextInt();
        System.out.println("To join the group chat give server ip: " + ip + "\nserver port: "+port);
        // Create a new server socket
        ServerSocket serverSocket = new ServerSocket(port);
        // Create a new server object
        Server server = new Server(serverSocket);
        // Start the server
        server.startServer();
    }
}
