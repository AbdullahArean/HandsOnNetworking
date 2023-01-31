import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements  Runnable{
    // ArrayList to store all connected clients
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    // Socket for the current client
    private Socket socket;
    // BufferedReader and BufferedWriter for reading and writing data
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    // String to store the client's username
    private String clientUsername;

    // Constructor to initialize the client handler
    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            // Initialize the BufferedWriter and BufferedReader
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            // Read the client's username
            this.clientUsername = bufferedReader.readLine();
            // Add the client to the array list
            clientHandlers.add(this);
            // Broadcast a message to all clients that this client has entered the chat
            broadcastMessage("Server: "+ clientUsername + " has entered the chat!");

        } catch (Exception e) {
            // Close all resources if an exception is thrown
            closeEverything(socket, bufferedReader,bufferedWriter);
        }
    }

    // Method to remove the client from the array list
    public  void removeClientHandler(){
        clientHandlers.remove(this);
        // Broadcast a message to all clients that this client has left the chat
        broadcastMessage("Server " +clientUsername +"has left the chat" );
    }

    // Overridden run method
    @Override
    public void run() {
        String messageFromClient;
        // Loop until the socket is connected
        while(socket.isConnected()){
            try {
                // Read a message from the client
                messageFromClient = bufferedReader.readLine();
                // Broadcast the message to all clients
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                // Close all resources if an exception is thrown
                closeEverything(socket, bufferedReader,bufferedWriter);
                break;
            }
        }
    }

    // Method to broadcast a message to all clients
    private void broadcastMessage(String messagesent) {
        for(ClientHandler clientHandler : clientHandlers){
            try{
                // Make sure the message is not sent to the sender
                if(clientHandler != this) {
                    clientHandler.bufferedWriter.write(messagesent);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                // Close all resources if an exception is thrown
                closeEverything(socket, bufferedReader,bufferedWriter);
            }
        }
    }

    // Method to close all resources
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
