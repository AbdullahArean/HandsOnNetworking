package Iterative;

import Utils.DnsRecord;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;

public class AuthDNSServercsedu {

    public static void main(String[] args) {
        try {
            //Select a Random Port Number
            int port = 300;
                    //ThreadLocalRandom.current().nextInt(1000, 9999);
            DatagramSocket serverSocket = new DatagramSocket(port);
            System.out.println("DNS Resolution Server started....\nServer IP: " + getIP() + " Server Port: " + port);
            System.out.println("Waiting for Continuously receive requests from clients ...");

            while (true) {
                byte[] receiveData = new byte[4096];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                new Thread(new DNSUDPHandler(serverSocket, receivePacket)).start();
            }
        } catch (Exception e) {
            System.out.println("Server Failed : " + e.getMessage());
        }
    }

    private static String getIP() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet6Address) continue;
                    ip = addr.getHostAddress();
                }
            }
        } catch (Exception e) {
            System.out.println("Server Failed: " + e.getMessage());
        }
        return ip;
    }


    public static class DNSUDPHandler implements Runnable {
        private final DatagramSocket serverSocket;
        private final DatagramPacket receivePacket;
        public static ArrayList<DnsRecord> localstorage;


        public DNSUDPHandler(DatagramSocket serverSocket, DatagramPacket receivePacket) {
            this.serverSocket = serverSocket;
            this.receivePacket = receivePacket;
        }

        @Override
        public void run() {
            try {
                byte[] sendData = handleDNSRequest(receivePacket.getData());
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                serverSocket.send(sendPacket);
            } catch (Exception e) {
                System.out.println("Server Failed: (handler) " + e.getMessage());
            }
        }

        public static byte[] handleDNSRequest(byte[] request) throws Exception {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(request));
            System.out.println("\n\nStart response decode");
            short tid = dataInputStream.readShort();
            System.out.println("Transaction ID: " + tid); // ID
            short flags = dataInputStream.readByte();
            int QR = (flags & 0b10000000) >>> 7;
            int opCode = (flags & 0b01111000) >>> 3;
            int AA = (flags & 0b00000100) >>> 2;
            int TC = (flags & 0b00000010) >>> 1;
            int RD = flags & 0b00000001;
            System.out.println("QR " + QR);
            System.out.println("Opcode " + opCode);
            System.out.println("AA " + AA);
            System.out.println("TC " + TC);
            System.out.println("RD " + RD);
            flags = dataInputStream.readByte();
            int RA = (flags & 0b10000000) >>> 7;
            int Z = (flags & 0b01110000) >>> 4;
            int RCODE = flags & 0b00001111;
            System.out.println("RA " + RA);
            System.out.println("Z " + Z);
            System.out.println("RCODE " + RCODE);

            short QDCOUNT = dataInputStream.readShort();
            short ANCOUNT = dataInputStream.readShort();
            short NSCOUNT = dataInputStream.readShort();
            short ARCOUNT = dataInputStream.readShort();

            System.out.println("Questions: " + String.format("%s", QDCOUNT));
            System.out.println("Answers RRs: " + String.format("%s", ANCOUNT));
            System.out.println("Authority RRs: " + String.format("%s", NSCOUNT));
            System.out.println("Additional RRs: " + String.format("%s", ARCOUNT));

            String QNAME = "";
            int recLen;
            byte[] record = new byte[0];
            while ((recLen = dataInputStream.readByte()) > 0) {
                record = new byte[recLen];
                for (int i = 0; i < recLen; i++) {
                    record[i] = dataInputStream.readByte();
                }
                QNAME += new String(record, StandardCharsets.UTF_8) + ".";
            }
            QNAME = QNAME.substring(0, QNAME.length() - 1); // remove trailing period
            System.out.println("Hostname: " + QNAME);
            int k;
            localstorage = DnsRecord.readRecordsFromFile("dns_records_it_auth.txt");
            ArrayList<Integer> ipmatched = new ArrayList<Integer>();
            for(k=0; k<localstorage.size(); k++){
                if(localstorage.get(k).getName().equals(QNAME)){
                    ipmatched.add(k);
                }
            }
           // DnsRecord.writeRecordsToFile(localstorage,"dns_records_auth.txt");
            short QTYPE = dataInputStream.readShort();
            short QCLASS = dataInputStream.readShort();
            System.out.println("Record Type: " + String.format("%s", QTYPE));
            System.out.println("Class: " + String.format("%s", QCLASS));

            // Construct DNS response
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // Write header
            dataOutputStream.writeShort(tid); // ID
            dataOutputStream.writeShort(0x8180); // Flags
            dataOutputStream.writeShort(1); // Questions
            dataOutputStream.writeShort(ipmatched.size());// Answers RRs
            dataOutputStream.writeShort(0); // Authority RRs
            dataOutputStream.writeShort(0); // Additional RRs

            // Write query
            String[] domainParts = QNAME.split("\\.");

            int queryLength = 0;
            for (int i = 0; i < domainParts.length; i++) {
                byte[] domainBytes = domainParts[i].getBytes(StandardCharsets.UTF_8);
                dataOutputStream.writeByte(domainBytes.length);
                dataOutputStream.write(domainBytes);
                queryLength += domainBytes.length + 1;
            }
            // No more parts
            dataOutputStream.writeByte(0);
            queryLength++;

            // Type 0x01 = A (Host Request)
            dataOutputStream.writeShort(QTYPE);
            // Class 0x01 = IN
            dataOutputStream.writeShort(QCLASS);
            // Write answer
            int count = 0;
            while (count<ipmatched.size()){
                // Find the position of the name in the DNS response
                int namePos = 12 + queryLength + 4; // 12 bytes for header, query length, and 4 bytes for type and class
                int offset = namePos - 12; // offset from start of message (12 bytes)
                dataOutputStream.writeShort(0xc000 | offset); // set first two bits to 11
                dataOutputStream.writeShort(QTYPE); // Type
                dataOutputStream.writeShort(QCLASS); // Class
                dataOutputStream.writeInt(localstorage.get(ipmatched.get(count)).getTTL()); // TTL
                InetAddress inetAddress = InetAddress.getByName(localstorage.get(ipmatched.get(count)).getValue());
                byte[] ipAddressBytes = inetAddress.getAddress();
                dataOutputStream.writeShort(ipAddressBytes.length); // Data length
                dataOutputStream.write(ipAddressBytes);
                count++;
            }
            return outputStream.toByteArray();
        }
    }
}
