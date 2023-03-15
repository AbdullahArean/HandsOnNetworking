package Driver;

import TCP.Reno.TCPReno;
import TCP.Tahoe.TCPTahoe;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import static Driver.Server.BytePacketize;

public class ClientTahoe {

    public static void main(String [] args) {
        try {
            Scanner scn = new Scanner(System.in);
            System.out.println("Give The Server IP: ");
            String IP = scn.nextLine();
            // getting localhost ip
            InetAddress ip = InetAddress.getByName(IP);
            // establish the connection with server port 5056
            Socket socket = new Socket(ip, 5051);
            System.out.println("Connected to server");
            //Receive Packet and Send Acknowledgement
            //Handshake 2nd Part
            //look for packets
            String filepath = "hugefile.pdf";
            //Make Packets
            //Send Packets, Get Ack, Do Flow Control , Do Congestion Control
            TCPTahoe.SendPackets( socket, BytePacketize(filepath));
            //TCPTahoe.ReceivePackets(socket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}