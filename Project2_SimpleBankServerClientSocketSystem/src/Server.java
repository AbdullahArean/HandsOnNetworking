import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class Server {
    public String lastreqid1;
    public static ArrayList<String> serverdb;
    private PrintWriter out1;
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private String clientid = null;



    public Server(int port) throws IOException {
        try {
            server = new ServerSocket(port);
            System.out.println("Bank Server started....\nBank Server IP: "+getIP());

            System.out.println("Waiting for a client ...");

            socket = server.accept();
            System.out.println("Client accepted");

            out1 = new PrintWriter(socket.getOutputStream(), true);
            out1.println("Server Got Connected");


            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));

            String line = "";
            String[] cr = {"-1","9"};
            while (!line.equals("exit")) {
                try {
                    line = in.readUTF();
                    cr = line.split("#");
                    if (cr[1].charAt(0) == 'L') {
                        if (login(cr[1])==1)
                            out1.println(cr[0]+"#"+lastreqid1);
                        if (login(cr[1])==0) {
                            out1.println(cr[0]+"#"+"Unsuccessful to Log In");
                        }
                        else {

                        }
                    }
                    else if (cr[1].charAt(0) == 'B' && clientid != null) {
                        out1.println(cr[0]+"#"+getBalance());
                    }
                    else if (cr[1].charAt(0) == 'C' && clientid != null) {
                        out1.println(cr[0]+"#"+credit(cr[1]));
                    }
                    else if (cr[1].charAt(0) == 'D' && clientid != null) {
                        out1.println(cr[0]+"#"+debit(cr[1]));
                    }
                    else if (cr[1].charAt(0) == 'O' && clientid != null) {
                        String[] temp = cr[1].split("_");
                        System.out.println("User: "+temp[3]+" Logged Out");
                    }
                } catch (Exception ignored) {

                }
            }

            // close connection
            socket.close();
            in.close();
        }catch (Exception e){

        }
    }

    public static void fileswrite(String credential) {
        try {
            // write strings to file
            serverdb.clear();
            File file = new File("ServerProtocol.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.append(credential);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void filesread() throws IOException {
        try {
            File file = new File("ServerProtocol.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                serverdb.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        serverdb = new ArrayList<>();
        filesread();
        Server server = new Server(7777);
    }

    public int login(String line) {

        // login logic here
        boolean ans = false;
        String[] temps = line.split("_");
        String temp = temps[1];
        if(temp.equals(clientid)) return -1;

        String temp2 = " ";
        for (String s : serverdb) {
            if(s.startsWith("U_"+temp)){
                String[] temps2 = s.split("_");
                temp2 = temps2[2];
            }
            if (s.equals(line)) {
                String[] strings = s.split("_");
                clientid = strings[1];
                ans= true;
            }
        }

        if(ans) {lastreqid1 = temp2; System.out.println("User: "+clientid+" Logged In"); return 1;}
        return 0;

    }

    public String getBalance() {
        String temp = null;
        for (String item : serverdb) {
            if (item.startsWith("U_" + clientid)) {
                temp = item;
            }
        }
        String[] strings = temp.split("_");
        String balance = strings[3];
        //System.out.println("User: " + clientid + ", Balance: " + balance);
        return "User: " + clientid + ", Balance: " + balance;
    }

    public String credit(String line) {
        // credit logic here
        String[] BalanceQuery = getBalance().split(" ");
        int balance = Integer.parseInt(BalanceQuery[3]);
        for (String s : serverdb) {
            if (s.equals(line)) {
                System.out.println("Transaction done already");
                return "Transaction done already";
            }
        }
        String[] clientrequest = line.split("_");
        //solve for any negative number input of user
        if(Integer.parseInt(clientrequest[3])<=0){
            return "Transaction not possible";
        }
        fileswrite("C_" + clientid + "_" + clientrequest[2] + "_" + clientrequest[3]);
        balance = balance + Integer.parseInt(clientrequest[3]);
        fileswrite( "U_" + clientid + "_" + clientrequest[2] + "_" + balance);
        try{
            filesread();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("User: "+clientrequest[1]+" Credited: "+clientrequest[3]+" , Balance: "+ balance);
        return ("User: "+clientrequest[1]+" Credited: "+clientrequest[3]+" , Balance: "+ balance);

    }

    public String debit(String line) {

        // debit logic here
        String[] BalanceQuery = getBalance().split(" ");
        int balance = Integer.parseInt(BalanceQuery[3]);
        for (String s : serverdb) {
            if (s.equals(line)) {
                System.out.println("Transaction done already");
                return "Transaction done already";
            }
        }
        String[] clientrequest = line.split("_");
        //solve for any negative number input of user
        if(Integer.parseInt(clientrequest[3])<=0 || Integer.parseInt(clientrequest[3])>Integer.parseInt(BalanceQuery[3])){
            return "Transaction not possible";
        }
        fileswrite("D_" + clientid + "_" + clientrequest[2] + "_" + clientrequest[3]);
        balance = balance - Integer.parseInt(clientrequest[3]);
        fileswrite( "U_" + clientid + "_" + clientrequest[2] + "_" + balance);
        try{
            filesread();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("User: "+clientrequest[1]+" Debited: "+clientrequest[3]+" , Balance: "+ balance);
        return ("User: "+clientrequest[1]+" Debited: "+clientrequest[3]+" , Balance: "+ balance);
    }

    private String getIP(){
        String ip=null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // EDIT
                    if (addr instanceof Inet6Address) continue;

                    ip = addr.getHostAddress();
                    // System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        } catch (SocketException e) {
            System.out.println(e);
        }
        return ip;
    }
}
