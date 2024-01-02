# SpringChat
SpringChat is a chatting application built using Spring Boot that showcases various capabilities of the framework. Some of the features demonstrated by this application include:

## Authentication
The application offers different authentication mechanisms such as session validation, cookies for remember me functionality, and basic authentication.

## HTTPS Connection
SpringChat uses a self-signed HTTPS connection to secure communication between the server and client.

## Websocket
The application employs Websocket to enable real-time messaging between users.

## File Download/Upload
SpringChat allows users to upload and download files, with the application using JPA persistence to store these files.

## Redirecting to Login
If you try to access protected resources without logging in, SpringChat will redirect you to the login page. Once logged in, you will be redirected to the desired page.

## Caching Files in Memory
The application caches files in memory, providing better performance. It reloads the files based on their date-modified.

## Session Management
Expired sessions are cleared after 1-hour idle, and the application keeps sessions in memory for better performance.

User Interface Features
The application also showcases various user interface capabilities such as modals, toasts or notifications, websockets, responsive design, and HTTP requests.

# Usage
To use SpringChat, follow these steps:

Build the application using the "mvn package" command.
Run the application using "java -jar target/simplechat-0.0.1-SNAPSHOT.jar".
Open http://localhost:62600/ in Firefox or Chrome.
Sign-up for at least two user accounts.
Start chatting! You can send text messages, upload files to create file messages, and download files by clicking on file-messages.




## How to use
1. Open codes in Intelij IDE and Build application running SpringChat.java file
3. Use Firefox or Chrome to open http://localhost:62600/ 
4. Sign-up at least two user accounts
5. Start chatting, you can do the following:
	* Send text massages
	* Upload files to create file messages
	* Download files by clicking on file-messages

