# Bank System using Server-Client architecture in Java Socket

This project is a basic implementation of a bank system using a server-client architecture in Java. The client can connect to the server, login to their account, and perform various operations such as checking account balance, crediting, and debiting the account. The server keeps track of all the transactions and login attempts made by the clients in the 'ServerProtocol.txt' file.

## Features
- The client can connect to the server and perform various operations such as checking account balance, crediting, and debiting the account.
- The client is prompted for the server's IP address, once entered it will connect to the server
- The server keeps track of all the transactions and login attempts made by the clients in the 'ServerProtocol.txt' file.
- The server uses this information to check the validity of login credentials, and to update the balance of the account after a credit or debit operation.
- The client can logout by sending "exit" to the server and close the connection.

## Protocol Developed
“Simple Client Server Socket Protocol”(SCSSP), a newly developed simple client-server protocol for this bank system can be defined as follows:

- The client establishes a connection to the server using the server's IP address and port number.
- The client sends a login request to the server in the format "L_clientid_-1_password", where clientid is the client's unique identifier and password is the client's account password.
- The server receives the login request and checks the provided credentials against the user information stored in the 'ServerProtocol.txt' file. If the credentials match, the server sends back the client's account balance and the last request id, otherwise it sends back an error message.
- After successful login, the client can perform various operations such as checking account balance, crediting, and debiting the account.
- For balance check, the client sends the request "B_clientid" to the server, the server checks the balance of the client in 'ServerProtocol.txt' and sends back the balance.
- For crediting, the client sends the request "C_clientid_reqid_amount" to the server, the server credits the specified amount to the client's account in 'ServerProtocol.txt' and sends back the updated balance.
- For debiting, the client sends the request "D_clientid_reqid_amount" to the server, the server debits the specified amount from the client's account in 'ServerProtocol.txt' and sends back the updated balance.
- The client can log out by sending "exit" to the server and close the connection.
- The server keeps track of all the transactions and login attempts made by the clients in the 'ServerProtocol.txt' file.
- The server uses this information to check the validity of login credentials, and to update the balance of the account after a credit or debit operation.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java 8 or higher
- A text editor or an IDE to run and edit the code

### Installing

1. Clone or download the repository to your local machine.
2. Open the project in your text editor or IDE.
3. Make sure that the path of 'ServerProtocol.txt' file is correct in the Server.java file.
4. Run the Server.java file, it will start the server and will wait for a client to connect.
5. Run the Client.java file, it will prompt for the server's IP address, enter the IP address and press enter.
6. The client will be connected to the server and will display a menu for the user to perform various operations.

## Built With

- Java 8

## Authors

- [Abdullah Ibne Hanif Arean](https://github.com/abdullaharean) - Initial implementation

## Acknowledgments
- This project was created for educational purposes, and it is a basic implementation of a bank system using a server-client architecture in Java.
- In real-world scenarios, it is recommended to use a database for storing sensitive information like login credentials and account balances, and also use encryption for secure communication between the client and the server.

## Note 
- This project is intended to be used for educational and testing purposes only.

