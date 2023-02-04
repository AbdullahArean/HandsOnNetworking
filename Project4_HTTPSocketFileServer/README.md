# HTTP SocketFileServer

This project is a basic implementation of a HTTP protocol using GET/POST method in Java. It is implemented by using Socket programming. The client can connect to the server, make a request to the server for a particular file(i.e. file1, file2 and file3) to upload or download. As response, the server processes the request and encounters the client's need. Note that multiple client can access the network which implies we have implemented multi-client server in our implementation.  

## Features

This project uses a server-client architecture and implements the following features:
- A Server class (i.e. FileServer.java) that listens for incoming client connections and creates a new thread for each connected client.
- A ServerThread class (i.e. FileServerThread.java) that handles each client connection, reads request from the client, and processes the request accordingly for upload or download request from each client.
- A Client class (i.e. FileClient.java) that connects to the server, sends the message to the server which file a client wants to upload or download, and receives a response from the server accordingly.

## Protocol Develoved

“Hypertext Transfer Protocol”(HTTP), a simple client-server protocol for the file sharing system can be defined as follows:

- The client establishes a connection to the server using the server's IP address and port number.
- The client sends a upload/download request of a file to the server. For example, at first, the client has 2 choices such as Download and Upload and they are mapped by 1 and 2 respectively(i.e. Download-->1 and Upload-->2)and there are 3 files such as file1.txt, file2.txt and file3.txt. Now, those files are mapped by 1,2 and 3 respectively(i.e. file1.txt-->1, file2.txt-->2, file3.txt-->3). If the client encounter a 4, it will be exited from the server and for any other input the server will show the client that the input is invalid.
-The server receives the file request and checks the provided request and matches whether that file is available or not. If the file is available it sends the file to the client
- The client at last get an upload response or can download a file if it requests for a valid/available file.
- The client can log out by encountering a "4" to the server and close the connection.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java 8 or higher
- A text editor or an IDE to run and edit the code

### Installing

1. Clone or download the repository to your local machine.
2. Open the project in your text editor or IDE.
3. Make sure that the path of 'file1.txt', 'file2.txt', 'file3.txt' files are correct in the Server.java file.
4. Run the Server.java file, it will start the server and will wait for a client to connect.
5. Run the Client.java file, it will prompt for the server's IP address, enter the IP address and press enter.
6. The client will be connected to the server and will display a menu for the user to perform various operations.

## Built With

- Java 8

## Authors

- [Abdullah Ibne Hanif Arean](https://github.com/abdullaharean) 
- [Mehadi Hasan Santo](https://github.com/Mehadi-Hasan-Santo))

## Acknowledgment
- This project was created for educational purposes, and it is a basic implementation of HTTP for file sharing system using a server-client architecture in Java.
- This project includes HTTP GET/POST methods which are used for uploading and downloading files.

## Note 
- This project is intended to be used for educational and testing purposes only.

## Conclusion
This is a basic Hypertext Transfer Protocol that demonstrates the use of sockets, threads and HTTP GET/POST methods in Java. It can be easily enhanced with additional features according to user requirements.
