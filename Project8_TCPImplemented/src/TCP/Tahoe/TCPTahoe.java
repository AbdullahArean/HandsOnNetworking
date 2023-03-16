package TCP.Tahoe;

import TCP.TCPPacket;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class TCPTahoe {


    // Send packets to the client
    public static void SendPackets(Socket clientSocket, ArrayList<byte[]> Packetsb) throws IOException {
        // Generate a random sequence number for the handshake packet
        Random random = new Random();
        int seqfixed = random.nextInt(100);

        // Send a handshake packet to start the conversation
        TCPPacket handshakepacket = new TCPPacket(clientSocket.getPort(), seqfixed, 0, new byte[0], false, true, false);
        clientSocket.getOutputStream().write(handshakepacket.toByteArray());
        clientSocket.getOutputStream().flush();

        // Wait for the client to respond with an acknowledgement
        byte[] handshakereturn = new byte[20];
        clientSocket.getInputStream().read(handshakereturn);
        TCPPacket handshakereturnpacket = TCPPacket.fromBytes(handshakereturn);

        // If the client acknowledged the handshake, print out the sequence number, acknowledgement flag, and window size
        if (handshakereturnpacket.getack()) {
            System.out.println("[SYN][ACK] " + handshakereturnpacket.getSequenceNumber() + " " + handshakereturnpacket.getack() + "[WindowSize] " + handshakereturnpacket.getWindowSize());
        } else {
            System.out.println("Connection Failed");
            return;
        }

        // Create an array of packets to be sent to the client
        ArrayList<TCPPacket> tcpPacketArrayList = new ArrayList<>();
        for (int i = 0; i < Packetsb.size(); i++) {
            tcpPacketArrayList.add(new TCPPacket(0, clientSocket.getPort(), handshakereturnpacket.getAckNumber() + i, 0, 0, 0, 5, 0, 0, Packetsb.get(i)));
        }
        int dupackcounter = 0;
        int transmissionround = 0;
        int lastack = seqfixed - 1;
        int tosend = 0;
        int cwnd = 1;
        int ssthresh = 50;
        int receivewind = 50;
        double EstimatedRTT = 0.2;
        double alpha = 0.125;
        double DevRTT = 0.2;
        double beta = 0.125;
        double RTO = 100;
        boolean flagfin= false;
        long StartTime = System.nanoTime();
        FileWriter writer = new FileWriter("pointstahoe.txt"); // create a new file
        // This is the loop where we keep sending packets until all packets are sent
        // The loop stops when all packets have been acknowledged
        // This loop also implements congestion control algorithm called TCP Reno
        while (tcpPacketArrayList.size() > tosend) {
            transmissionround++;
            System.out.println("==================[CWND] " + cwnd + " [SSthre]" + ssthresh+" ============");
            try {
                writer.write(cwnd+","+transmissionround+"\n"); // write each point's x and y coordinates to the file
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Sending packets as many as the congestion window size
            // We also measure Round Trip Time (RTT) for each packet
            long RTT_starttime = System.nanoTime();
            for (int i = 0; i < cwnd; i++) {
                // Get the packet from the array list and send it to the client
                clientSocket.getOutputStream().write(tcpPacketArrayList.get(tosend % tcpPacketArrayList.size()).toByteArray());
                clientSocket.getOutputStream().flush();
                tosend++;
                if(tosend>=tcpPacketArrayList.size()) {
                    flagfin=true;
                    break;
                };
                System.out.println(transmissionround + ":[ " + tcpPacketArrayList.get(tosend%tcpPacketArrayList.size()).getSeqNum() + " |Data]+[ ");
            }

            if(flagfin) break;

            // Receive ACKs
            try {
                // Set timeout for the socket
                // If the ACK is not received within the timeout period, a timeout exception is thrown
                clientSocket.setSoTimeout((int) (RTO));
                for (int i = 0; i < cwnd; i++) {
                    byte[] AckPacket = new byte[20];
                    clientSocket.getInputStream().read(AckPacket);
                    TCPPacket ACK = TCPPacket.fromBytes(AckPacket);

                    if (ACK.getack()) {
                        // If the ACK is received successfully
                        // Update the last acknowledged packet number
                        // The variable "seqfixed" is the initial sequence number used for this connection
                        // The last acknowledged packet number is the ACK number minus the initial sequence number minus one
                        lastack = ACK.getAckNumber() - seqfixed - 1 - 1;
                        System.out.println("[" + ACK.getAckNumber() + "][ACK] is received");
                    }
                    else {
                        // If the ACK is a duplicate ACK
                        System.out.println("[" + ACK.getAckNumber() + "][ACK] is received DUPLICATE");

                        // Increase the count of duplicate ACKs received
                        dupackcounter++;

                        // If three duplicate ACKs are received
                        if (dupackcounter == 3) {
                            // Perform fast retransmission
                            // Set the threshold to half of the congestion window size
                            ssthresh = Math.max(cwnd / 2, 1);

                            // Set the congestion window size to the minimum of the threshold and the receive window size
                            cwnd = 1;

                            // Resend the packet that has not been acknowledged
                            tosend = ACK.getAckNumber()-seqfixed-1-1;
                            break;
                        }
                    }
                }
            }
            catch (SocketTimeoutException e) {
                // Timeout occurred
                System.out.println("[TIMEOUT]");
                ssthresh = Math.min(receivewind,Math.max(cwnd / 2, 1));
                cwnd = 1;
                tosend = lastack;
                RTO = 2* RTO;
                continue;
            }
            catch (IndexOutOfBoundsException e){
                //e.printStackTrace();

            }

            // Calculate the new value of the congestion window
            if (cwnd < ssthresh) {
                System.out.println("[Slow Start]");
                cwnd=Math.min(receivewind,cwnd*2);
            }
            else {
                System.out.println("[Congestion Avoidance]");
                cwnd = Math.min(receivewind, cwnd+1);
            }
            long RTT_endtime = System.nanoTime();

            long duration = (RTT_endtime - RTT_starttime);
            double SampleRTT = (double) duration / 1_000_000.0;

            EstimatedRTT = (1 - alpha) * EstimatedRTT + alpha * SampleRTT;

            DevRTT = (1 - beta) * DevRTT + beta * Math.abs(SampleRTT - EstimatedRTT);

            RTO = EstimatedRTT + 4 * DevRTT;
            DecimalFormat df = new DecimalFormat("#0.000");
            System.out.println("SampleRTT: " + df.format(SampleRTT) + " ms");
            System.out.println("EstimatedRTT: " + df.format(EstimatedRTT) + " ms");
            System.out.println("DevRTT: " + df.format(DevRTT) + " ms");
            System.out.println("RTO: " + df.format(RTO) + " ms");
        }
        // Send FIN packet to client
        TCPPacket finpacket = new TCPPacket(clientSocket.getPort(), tosend+seqfixed, 0, new byte[0], false, false, true);
        clientSocket.getOutputStream().write(finpacket.toByteArray());
        clientSocket.getOutputStream().flush();
        byte[] finpacketreturn = new byte[20];
        clientSocket.getInputStream().read(finpacketreturn);
        TCPPacket finpacketreturnpacket = TCPPacket.fromBytes(finpacketreturn);
        if (finpacketreturnpacket.getack()) {
            System.out.println("[FIN][ACK] " + finpacketreturnpacket.getSequenceNumber() + " " + finpacketreturnpacket.getack() + "[WindowSize] " + finpacketreturnpacket.getWindowSize());
        } else {
            System.out.println("[FIN] Failed");
        }

        long endtime = System.nanoTime();


        long duration = (endtime - StartTime);
        double delay = (double) duration / 1_000_000.0;
        DecimalFormat df = new DecimalFormat("#0.000");
        System.out.println("Total delay: " + df.format(delay) + " ms\n");

        writer.close(); // close the file
        clientSocket.close();
    }
    /**
     * This method receives packets over a given socket and performs a TCP handshake.
     * Once the handshake is completed, it reads incoming packets and verifies their sequence numbers.
     * It writes the received data to a file and terminates the connection once a FIN packet is received.
     * @param socket the socket to receive packets from
     */
    public static void ReceivePackets(Socket socket) {
        try {
            byte[] buffer = new byte[1480]; // set buffer size to 1480 bytes
            int bytesRead; // number of bytes read
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // output stream to write bytes to an array
            int current; // current sequence number
            boolean flag = false; // handshake flag
            int expected = 0; // expected sequence number
            // Receive packets until end of stream is reached
            while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                // Extract TCP packet header from buffer
                TCPPacket header = TCPPacket.fromBytes(buffer);

                // Check if SYN flag is set and this is the first handshake
                if (header.getsyn() && !flag) {
                    flag = true;
                    // Send SYN-ACK as response to complete first handshake
                    System.out.println("[SYN]" + header.getSequenceNumber());
                    socket.getOutputStream().write(new TCPPacket(socket.getPort(), 60, header.getSequenceNumber() + 1, new byte[0], true, true, false).toByteArray());
                    socket.getOutputStream().flush();
                    // Update expected sequence number to next value after received SYN
                    expected = header.getSequenceNumber() + 1;
                    System.out.println("[ " + (expected - 1) + " |Data]+[" + expected);
                    continue;
                }
                // Check if FIN flag is set, indicating end of stream
                if (header.getfin()) {
                    System.out.println("[FIN]" + header.getSequenceNumber());
                    // Send FIN-ACK as response to complete closing handshake
                    socket.getOutputStream().write(new TCPPacket(socket.getPort(), 60, header.getSequenceNumber() + 1, new byte[0], true, true, true).toByteArray());
                    socket.getOutputStream().flush();
                    break;
                }
                // Generate a random number to simulate packet loss
                Random random = new Random(System.currentTimeMillis());
                int randomNumber = random.nextInt(100);

                // If packet is not lost, process packet and send ACK
                if (randomNumber > 10) {
                    current = header.getSequenceNumber();
                    if (current == expected) {
                        // Packet is in sequence, write data to output stream and send ACK
                        expected++;
                        byteArrayOutputStream.write(header.getData(), 0, bytesRead - 20);
                        socket.getOutputStream().write(new TCPPacket(socket.getPort(), current, expected, new byte[0], true, false, false).toByteArray());
                        socket.getOutputStream().flush();
                        System.out.println("[ " + current + " |Data]+[ " + expected);

                    } else {
                        // Packet is out of sequence, send NACK
                        socket.getOutputStream().write(new TCPPacket(socket.getPort(), current, expected, new byte[0], false, false, false).toByteArray());
                        socket.getOutputStream().flush();
                        System.out.println("XXXX[ " + current + " |Data]+[ " + expected);
                    }
                }
            }
            // Write received data to file and close streams
            FileOutputStream fileOutputStream = new FileOutputStream("Received_" + "hugefile" + System.currentTimeMillis() + ".pdf");
            byteArrayOutputStream.writeTo(fileOutputStream);
            fileOutputStream.close();
            //socket.getInputStream().close();
            //socket.getOutputStream().close();


        }
        catch (IOException e){
// Handle any IOExceptions thrown during processing
            e.printStackTrace();
        }
    }


}
