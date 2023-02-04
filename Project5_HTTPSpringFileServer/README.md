# Java Socket File Server
![Group Chat Running](https://github.com/AbdullahArean/JavaGroupChatApp/blob/main/Java%20Group%20Chat%20App%20Screenshot.png)
This Java Group Chat App is a basic chat application that allows multiple clients to connect to a server and communicate with each other in real-time. The app uses a server-client architecture and implements the following features:

- A Server class that listens for incoming client connections and creates a new thread for each connected client
- A ClientHandler class that handles each client connection, reads messages from the client, and broadcasts the messages to all connected clients
- A Client class that connects to the server, sends messages to the server, and listens for messages from the other clients
## Getting Started
To run the app, you will need to have Java 8 or later installed on your machine.

Clone the repository to your local machine
```
git clone https://github.com/username/javagroupchat.git
```
Compile the Server.java and Client.java files
```
javac Server.java
javac Client.java
```
Start the server
```
java Server
```
Start the client by running the following command in a new terminal window.
```
java Client
```
The client will prompt you to enter a username for the group chat. Once you have entered a username, you will be able to send and receive messages from other clients connected to the server.
## How it works
- When the server starts, it creates a new ServerSocket on the specified port (1234 in this case) and waits for incoming client connections.
- When a client connects, the server creates a new ClientHandler object and starts a new thread for the client.
- The ClientHandler reads the client's username and adds the client to the list of connected clients. It then broadcasts a message to all connected clients that the new client has entered the chat.
- The ClientHandler then listens for messages from the client and broadcasts the messages to all connected clients.
- The client connects to the server and sends its username to the server. It then listens for messages from the other clients and displays them on the console.
## Conclusion
This is a basic Java group chat application that demonstrates the use of sockets and threads in Java. It can be easily enhanced with additional features such as private messaging, file sharing, and more.
