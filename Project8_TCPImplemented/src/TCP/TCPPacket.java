package TCP;

import java.nio.ByteBuffer;

public class TCPPacket {
    private static final int ACK_FLAG = 16,SYN_FLAG = 2,FIN_FLAG = 1;
    private int flags,sourcePort,destinationPort, sequenceNumber, ackNumber,dataOffset,windowSize,checksum,urgentPointer;
    private byte[] packetData;
    private boolean fin,ack, syn, urg, psh, rst;
    public void PopulateFlagAttributes() {
        try {
            this.urg = ((flags & 0x20) != 0);
            this.ack = ((flags & 0x10) != 0);
            this.psh = ((flags & 0x08) != 0);
            this.rst = ((flags & 0x04) != 0);
            this.syn = ((flags & 0x02) != 0);
            this.fin = ((flags & 0x01) != 0);

        }
        catch (Exception e){

        }

    }


    /**
     * Constructs a TCP packet with the specified parameters.
     * @param sourcePort      the source port.
     * @param destinationPort the destination port.
     * @param sequenceNumber  the sequence number.
     * @param ackNumber       the acknowledgement number.
     * @param dataOffset      the data offset.
     * @param flags           the flags.
     * @param windowSize      the window size.
     * @param checksum        the checksum.
     * @param urgentPointer   the urgent pointer.
     * @param packetData      the packet data.
     */
    public TCPPacket(int sourcePort, int destinationPort, int sequenceNumber, int ackNumber, int dataOffset, int flags, int windowSize, int checksum, int urgentPointer, byte[] packetData) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.sequenceNumber = sequenceNumber;
        this.ackNumber = ackNumber;
        this.dataOffset = dataOffset;
        this.flags = flags;
        this.windowSize = windowSize;
        this.checksum = checksum;
        this.urgentPointer = urgentPointer;
        this.packetData = packetData;
        PopulateFlagAttributes();

    }


    /**
     * Constructs a TCP packet with the specified parameters.
     * @param sourcePort      the source port.
     * @param destinationPort the destination port.
     * @param seqNum          the sequence number.
     * @param ackNumber       the acknowledgement number.
     * @param data            the data .
     * @param ack             the ack flag.
     * @param syn             the syn flag.
     * @param fin             the fin flag.
     */
    public TCPPacket(int destinationPort,int seqNum, int ackNumber, byte[] data, boolean ack, boolean syn, boolean fin) {
        this.sourcePort = 0;
        this.destinationPort = destinationPort;
        this.sequenceNumber = seqNum;
        this.ackNumber = ackNumber;
        this.dataOffset = 5; // assuming no TCP options
        this.flags = 0;
        if (ack) {
            this.flags |= ACK_FLAG;
        }
        if (syn) {
            this.flags |= SYN_FLAG;
        }
        if (fin) {
            this.flags |= FIN_FLAG;
        }
        this.windowSize = 0;
        this.checksum = 0;
        this.urgentPointer = 0;
        this.packetData = data;
        PopulateFlagAttributes();
    }


    public static TCPPacket fromBytes(byte[] buffer) {
        int sourcePort = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
        int destinationPort = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
        int sequenceNumber = ((buffer[4] & 0xFF) << 24) | ((buffer[5] & 0xFF) << 16) | ((buffer[6] & 0xFF) << 8) | (buffer[7] & 0xFF);
        int ackNumber = ((buffer[8] & 0xFF) << 24) | ((buffer[9] & 0xFF) << 16) | ((buffer[10] & 0xFF) << 8) | (buffer[11] & 0xFF);
        int dataOffset = ((buffer[12] & 0xF0) >> 4);
        int flags = (buffer[13] & 0xFF);
        int windowSize = ((buffer[14] & 0xFF) << 8) | (buffer[15] & 0xFF);
        int checksum = ((buffer[16] & 0xFF) << 8) | (buffer[17] & 0xFF);
        int urgentPointer = ((buffer[18] & 0xFF) << 8) | (buffer[19] & 0xFF);

        // extract packet data
        int packetDataLength = buffer.length - 20; // header is 20 bytes long
        byte[] packetData = new byte[packetDataLength];
        System.arraycopy(buffer, 20, packetData, 0, packetDataLength);

        return new TCPPacket(sourcePort, destinationPort, sequenceNumber, ackNumber, dataOffset, flags, windowSize, checksum, urgentPointer, packetData);
    }
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getAckNumber() {
        return ackNumber;
    }

    public int getWindowSize() {
        return windowSize;
    }

    /**
     Converts the TCP packet object to a byte array
     @return a byte array representing the TCP packet
     */
    public byte[] toByteArray() {
        // Allocate a byte buffer to hold the TCP packet
        ByteBuffer buffer = ByteBuffer.allocate(20 + packetData.length);

        // Write the source and destination ports, sequence and acknowledgment numbers,
        // data offset, flags, window size, checksum, and urgent pointer to the buffer
        buffer.putShort((short) sourcePort);
        buffer.putShort((short) destinationPort);
        buffer.putInt(sequenceNumber);
        buffer.putInt(ackNumber);
        buffer.put((byte) ((dataOffset << 4) | 0));
        buffer.put((byte) flags);
        buffer.putShort((short) windowSize);
        buffer.putShort((short) checksum);
        buffer.putShort((short) urgentPointer);

        // Write the packet data to the buffer
        buffer.put(packetData);

        // Return the byte array representation of the TCP packet
        return buffer.array();
    }








    public int getSeqNum() {
        return sequenceNumber;
    }
    public boolean getsyn(){return  syn;}

    public byte[] getData() {
        return packetData;
    }
    public boolean getack(){
        return this.ack;
    }

    public boolean getfin() {
        return fin;
    }
}
