package Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPConversion {
    public static String ipv4Toipv6(String ipv4Address) {
        String[] octets = ipv4Address.split("\\.");
        byte[] ipv6Bytes = new byte[16];
        ipv6Bytes[10] = (byte) 0xff;
        ipv6Bytes[11] = (byte) 0xff;
        ipv6Bytes[12] = (byte) Integer.parseInt(octets[0]);
        ipv6Bytes[13] = (byte) Integer.parseInt(octets[1]);
        ipv6Bytes[14] = (byte) Integer.parseInt(octets[2]);
        ipv6Bytes[15] = (byte) Integer.parseInt(octets[3]);
        InetAddress inet6Address = null;
        try {
            inet6Address = InetAddress.getByAddress(ipv6Bytes);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return inet6Address.getHostAddress();
    }
    public static String ipv6Toipv4(String ipv6Address) {
        InetAddress inet6Address = null;
        try {
            inet6Address = InetAddress.getByName(ipv6Address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        byte[] ipv6Bytes = inet6Address.getAddress();
        if (ipv6Bytes[0] != 0x20 || ipv6Bytes[1] != 0x01 || ipv6Bytes[2] != 0x00 || ipv6Bytes[3] != 0x00) {
            return null;
        }
        int a = ipv6Bytes[12] & 0xff;
        int b = ipv6Bytes[13] & 0xff;
        int c = ipv6Bytes[14] & 0xff;
        int d = ipv6Bytes[15] & 0xff;
        return a + "." + b + "." + c + "." + d;
    }

    public static void main(String[] args) {
        String ipv6Address = "2001:db8::2";
        String ipv4Address = ipv6Toipv4(ipv6Address);
        System.out.println("IPv4 Address: " + ipv4Address);
    }


}
