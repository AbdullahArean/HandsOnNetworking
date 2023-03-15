# TCPTahoe
### Introduction
TCP is referred as Transmission Control Protocol which uses both flow control and congestion control algorithms to ensure reliable data transfer over computer networks.<br>
Flow control is used to regulate the amount of data that is sent by the sender, to prevent the receiver from being overwhelmed with data. 
This is achieved through a sliding window mechanism, where the receiver advertises the amount of free buffer space available to the sender. 
The sender then sends data up to the maximum size of the sliding window, and waits for an acknowledgement from the receiver before sending more data. 
We have implemented this feature(flow control) in this Lab.<br>
Congestion control, on the other hand, is used to prevent network congestion by regulating the rate at which data is sent. 
This is done by monitoring the network for signs of congestion, such as packet loss or delay, and adjusting the sending rate accordingly. 
There are several congestion control algorithms used in TCP, including TCP Tahoe, TCP Reno, and TCP New Reno. 
But we have implemented TCP Tahoe in this Lab which also have the feature of congestion control.TCP Tahoe is an early congestion control algorithm 
used in computer networks to prevent network congestion by regulating the rate at which data is transmitted between two endpoints. 
It was one of the first congestion control algorithms developed for TCP (Transmission Control Protocol), which is the protocol used for reliable data transfer over the Internet.

### Objectives
The preliminary objective of this experiment is to implement the features of TCP Reno and enriched the knowledge by realizing its proper working mode. More objectives are as follows: 
 - To understand the principal of TCP Tahoe and TCP Reno.
 - To implement the TCP Tahoe flow control and congestion control algorithm.
 - To implement the TCP Reno congestion control algorithm.
 - To be able to compare TCP Reno with TCP Tahoe.
 - To realize the fact how TCP ensures the reliability of data transfer.
 - To get the deep knowledge of timing, acknowledgement and duplicate acknowledgement and their uses and working principal in TCP Reno.
 - To get to know about the phases of TCP Tahoe as well asTCP Reno such as Slow start, Congestion avoidance, Fast Retransmission and Fast Recovery.
 - To be able to plot the graph of both TCP Tahoe and TCP Reno for the visualization of the comparison. 

### Theory
TCP is one of the protocols of the transport layer for network communication. TCP provides reliable, ordered, and error-checked delivery of a stream of bytes between applications running on hosts communicating via an IP network. TCP is connection-oriented, and a connection between client and server is established before data can be sent. The server must be listening (passive open) for connection requests from clients before a connection is established. Three-way handshake (active open), retransmission, and error-detection adds to reliability. Thus TCP can maintain various operations to establish perfect communications between a pair of hosts, e.g connection management, error detection, error recovery, congestion control, connection termination, flow control, etc. In this lab, we will have a look at the flow control mechanism and congestion control mechanisms of the TCP protocol.

TCP uses a sliding window flow control protocol. In each TCP segment, the receiver specifies in the receive window field the amount of additionally received data (in bytes) that it is willing to buffer for the connection. The sending host can send only up to that amount of data before it must wait for an acknowledgement and window update from the receiving host.

TCP Tahoe is a congestion control algorithm that is used to manage the flow of data in a network and prevent congestion. It was one of the first congestion control algorithms implemented in TCP and is still widely used today.

TCP Reno is a congestion control algorithm used in the Transmission Control Protocol (TCP) to manage network congestion. It was named after the city of Reno, Nevada, where the algorithm was first presented at a conference in 1990. TCP Reno is an extension of the earlier TCP Tahoe algorithm, and it introduces a new mechanism called "fast recovery" to improve network performance.

### Methodology
i. The concept of Sliding Window in TCP<br>
The sliding window is a key concept in TCP (Transmission Control Protocol), which is the protocol used for reliable data transfer over the Internet. The sliding window is used for flow control, which regulates the amount of data that is sent by the sender, to prevent the receiver from being overwhelmed with data. Basically, the sliding window works by allowing the sender to send a certain number of packets without waiting for an acknowledgement from the receiver. The sender maintains a buffer of packets to be sent and a pointer to the next packet to be sent. The receiver advertises the amount of free buffer space available to the sender, which is known as the receiver window. The sender sends data up to the maximum size of the receiver window, and waits for an acknowledgement from the receiver before sending more data. The receiver sends an acknowledgement for each packet received, which includes the number of the next expected packet. This allows the sender to update its pointer to the next packet to be sent. Usually, the size of the sliding window determines the maximum amount of unacknowledged data that can be in transit at any given time. If the receiver's buffer becomes full, it advertises a smaller receiver window, causing the sender to slow down its transmission rate to prevent overwhelming the receiver.

However, the sliding window mechanism ensures that the sender does not overwhelm the receiver with too much data, while allowing for efficient data transfer by allowing the sender to send multiple packets without waiting for an acknowledgement for each packet. It is an essential component of TCP's flow control mechanism, which helps to ensure reliable and efficient data transfer over the Internet.

ii. Congestion Control Protocol of TCP Tahoe<br>
The TCP Tahoe congestion control protocol works by using a mechanism called slow start to increase the sending rate of data exponentially until congestion is detected. Once congestion is detected, the protocol switches to congestion avoidance mode, which reduces the sending rate linearly in response to detected congestion. When the congestion clears, the protocol resumes slow start. The slow start mechanism gradually increases the sending rate of data until the first packet loss is detected, indicating that the network is congested. When packet loss is detected, the protocol switches to congestion avoidance mode, where the sending rate is reduced linearly in response to detected congestion. This reduction in sending rate helps to prevent further congestion and packet loss, ensuring that the network remains stable and reliable. In addition to slow start and congestion avoidance mechanisms, TCP Tahoe also uses a timeout mechanism to ensure reliable data transfer. If an acknowledgement is not received from the receiver within a certain amount of time, the protocol assumes that the packet was lost and retransmits it. This helps to ensure that all packets are received by the receiver, even in the presence of network congestion and packet loss.

iii. Congestion Control Protocol of TCP Reno<br>
TCP Reno uses a combination of slow-start, congestion avoidance, and fast recovery algorithms to control the transmission rate and avoid network congestion. The slow-start algorithm initializes the transmission rate to a low value, then increases it gradually until it detects network congestion. When congestion is detected, TCP Reno enters the congestion avoidance phase and reduces the transmission rate to prevent further congestion. If packet loss occurs, the fast recovery algorithm is triggered to quickly recover the lost packets and restore the transmission rate. Overall, TCP Reno uses a combination of algorithms to maintain network stability, ensure reliable transmission, and avoid congestion.

iv. TCP Tahoe Vs TCP Reno
TCP Tahoe and TCP Reno are work in a similar manner, but there are some key differences between them. They are as follows:<br>

##### Characteristics of Fast Recovery Phase<br>
The most significant difference between TCP Tahoe and TCP Reno is the way they handle packet loss. In TCP Tahoe, when a packet loss is detected, the congestion window is reduced to 1 and the slow start phase is entered. This means that the congestion window size is increased exponentially until the slow start threshold is reached, and then increased linearly.

In TCP Reno, a fast recovery phase is added. When a packet loss is detected, the congestion window is halved, and the fast recovery phase is entered. In this phase, the congestion window size is kept constant until all lost packets are retransmitted. After that, the congestion avoidance phase is entered, and the congestion window size is increased linearly.

##### Change in Congestion Window size<br>
In TCP Tahoe, the congestion window size is reduced to 1 when a packet loss is detected. This can lead to a significant decrease in throughput, especially in high bandwidth networks.

In TCP Reno, the congestion window size is halved when a packet loss is detected. This means that the throughput is not reduced as much as in TCP Tahoe, and the network can recover more quickly from congestion.

However, the main difference between TCP Tahoe and TCP Reno is the way they handle packet loss. Figure-1 illustrates the evolution of TCP’s congestion window for both TCP Tahoe and Reno.

### Implementation Process			
Basically, TCP Tahoe and Reno consists of four phases: slow start, congestion avoidance, fast retransmit, and fast recovery.<br>
i. Slow Start: When a connection is established, the congestion window size is initially set to 1. The window size is increased by 1 for each ACK received, doubling the window size every round trip time (RTT) until the slow start threshold is reached.<br>
ii. Congestion Avoidance: Once the slow start threshold is reached, the congestion window size is increased linearly, by 1/cwnd for each ACK received, where cwnd is the current congestion window size.<br>
iii. Fast Retransmit: When three duplicate ACKs are received, TCP Tahoe and Reno assumes that a packet has been lost and immediately retransmits the lost packet.<br>
iv. Fast Recovery: After retransmitting the lost packet, TCP Reno enters the fast recovery phase. The congestion window size is halved and 3 is added to it, and then kept constant until all lost packets are retransmitted. After that, the congestion avoidance phase is entered, and the congestion window size is increased linearly.But TCP Tahoe starts its congestion window from 1 and continue the slow start phase.<br>

### Class Overview
Our Implemented classes are briefly described below:<br>
1.TCPPacket.java: This is a Java class that represents a TCP packet. It has several constructors for creating a TCP packet object with different parameters, including the source and destination port numbers, sequence number, acknowledgment number, data offset, flags, window size, checksum, urgent pointer, and packet data.

The class also has a method for converting a byte array to a TCP packet object, as well as getters for some of the packet's attributes such as the sequence number, acknowledgment number, and window size.

The flags attribute is an integer that represents the TCP flags. The PopulateFlagAttributes() method extracts individual flag values from the integer flags attribute and sets their corresponding boolean attributes, such as ack, syn, and fin.

This class can be used to create, manipulate, and analyze TCP packets in Java applications.<br>
<br>
2. TCPTahoe.java: This is a Java program that implements TCP Tahoe, a congestion control algorithm used in TCP.

The program starts by establishing a connection with a client and sending a handshake packet. It then creates an array of packets to be sent to the client and enters a loop to send and receive packets until all packets have been acknowledged.

The congestion control algorithm is implemented within the loop using the TCP Tahoe algorithm. The congestion window size (cwnd) is initially set to 1 and increases linearly by one for each successful transmission until a packet is lost. When a packet is lost, the cwnd is set to 1, and the slow start phase starts again. If three duplicate ACKs are received, the program performs fast retransmission and sets the threshold to half of the congestion window size.

The program measures Round Trip Time (RTT) for each packet and uses it to adjust the timeout period. It also writes the cwnd and transmission round to a file for plotting purposes.

The program sends a flagfin to break out of the loop once all packets have been sent, and it closes the connection with the client.<br>
<br>
3. TCPReno.java: This is a Java code for implementing the TCP Reno algorithm for congestion control. It sends packets to a client socket and receives acknowledgements for the sent packets. The code defines a method called SendPackets that takes a Socket object and an ArrayList of byte arrays as input parameters. The ArrayList contains the data to be sent to the client as packets.

The code starts by generating a random sequence number for the handshake packet and sends it to the client to initiate the conversation. Then, it waits for the client to respond with an acknowledgement. If the client acknowledges the handshake, the code prints out the sequence number, acknowledgement flag, and window size. If the client does not acknowledge the handshake, the connection is considered failed, and the method returns.

After the handshake, the code creates an array of packets to be sent to the client. It initializes several variables for use in the congestion control algorithm, including the congestion window size (cwnd), slow start threshold (ssthresh), receive window size (receivewind), estimated round-trip time (EstimatedRTT), deviation of round-trip time (DevRTT), and retransmission timeout (RTO). It also initializes some counters for use in the loop, including a counter for duplicate ACKs received (dupackcounter), a counter for the transmission round (transmissionround), and a variable to keep track of the last acknowledged packet number (lastack).

The main loop of the code sends packets to the client and receives acknowledgements. It implements the TCP Reno algorithm for congestion control. It sends packets as many as the congestion window size and measures Round Trip Time (RTT) for each packet. Then, it receives ACKs from the client and updates the last acknowledged packet number. If a duplicate ACK is received, the code increases the counter of duplicate ACKs received. If three duplicate ACKs are received, the code performs fast retransmission and sets the threshold to half of the congestion window size.

The code continues sending packets and receiving acknowledgements until all packets are sent and acknowledged. For each congestion window size and transmission round, the code writes the data to a file called "pointsreno.txt" in the format "cwnd, transmissionround". Finally, the code closes the file and returns.<br>
<br>
4. Server.java: This is a Java program that creates a server to receive TCP packets from clients and send them to a Tahoe congestion control protocol. Here is an explanation of the code:
<br>
i. The first line of code specifies the package name as "Driver".<br>
ii. The next two lines import the TCPTahoe class from the TCP.Tahoe package and the necessary Java libraries.<br>
iii. The program creates a class called Server with a static variable packetlengthfixed set to 1460.<br>
iv. The main function starts by creating a server socket on port 5051, printing a message to indicate that the server has started and the server's IP address, and then entering an infinite loop to wait for client connections.<br>
v. When a new client connects, the program creates a new thread to handle the client's connection and starts it.<br>
vi. The ClientHandler class is responsible for handling each client connection. It receives packets from the client through the TCPTahoe.ReceivePackets method and closes the connection when done.<br>
vii. The BytePacketize method reads the bytes from a given file and divides them into packets of fixed length, and then returns an ArrayList of these packets.<br>
viii. The getIP method uses Java's NetworkInterface class to obtain the IP address of the server.<br>
ix. The program uses Java's socket and networking libraries to create and manage the TCP connections with clients, as well as to communicate with the Tahoe congestion control protocol.<br>

### Experience
By doing this experiment, we have learnt a lot of things and gathered experience on various things. Our learnings and experiences are mentioned below: <br>
i. Understanding the basics of TCP Tahoe and TCP Reno congestion control algorithm and how they operate in a network.<br>
ii. Gaining practical knowledge of Java programming language, network protocols, and socket programming.<br>
iii. Learning how to implement TCP Tahoe and TCP Reno algorithm in Java from scratch and how to test it for different network scenarios and conditions.<br>
iv. Getting hands-on experience with network simulation tools to simulate network conditions and evaluate the performance of both TCP Tahoe and TCP Reno algorithm.<br>
v. Gaining experience with congestion control algorithms, such as slow start, congestion avoidance, and fast retransmit, used in TCP Thaoe as well as TCP Reno to detect network congestion and reduce data transmission rate.<br>
vi. Learning how to monitor network congestion and estimate the network's available bandwidth to adjust the transmission rate and maintain high network throughput.<br>
vii. Enhancing problem-solving skills and critical thinking by applying TCP Tahoe and Reno algorithm to real-world network scenarios and addressing various network challenges.<br>
viii. Developing skills in network troubleshooting, debugging, and optimization to diagnose and fix network-related issues in the implementation of both TCP Tahoe and TCP Reno algorithm.<br>
ix. Finally, being able to compare TCP Reno with TCP Tahoe and the change in Fast Recovery and Fast Transmission segments.<br> 
<br>
Overall, this experiment was a valuable learning experience as it helped us to understand the concepts of TCP Reno and its working strategy in a precise and formal manner. Moreover, it enhance our knowledge about congestion control protocol (TCP Reno) and make it deeply understandable. Furthermore, it makes us capable of understanding the discrepancy in congestion control between TCP Reno and TCP Tahoe.
