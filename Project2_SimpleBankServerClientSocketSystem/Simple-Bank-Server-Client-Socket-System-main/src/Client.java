// A Java program for a Client

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Integer.parseInt;

public class Client {
    public static ArrayList<String> ClientTransaction;
    private String lastreqid;
    private static String clientid = null;
    public static Scanner scanner;
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;
    private BufferedReader in;
    private int ClientReqid;

    public Client(String address, int port) throws IOException {
        // establish a connection
        try {
            socket = new Socket(address, port);
            // Read output from server
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String output = in.readLine();
            System.out.println("Received from server: " + output);

            // takes input from terminal
            input = new DataInputStream(System.in);

            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //new section
        String userinp = " ";
        while (!userinp.equals("2")) {
            System.out.println("Please Select From The Menu:");
            System.out.println("1. LogIn Into Your Account");
            System.out.println("2. Exit");
            userinp = scanner.nextLine();
            if (userinp.equals("1")) {
                login();
                ClientReqid=10;
                if (clientid != null) {
                    String userinpline = " ";
                    while (!userinpline.equals("4")) {
                        ClientReqid+=1;
                        System.out.println("Please Select From The Menu:");
                        System.out.println("1. Balance Check");
                        System.out.println("2. Credit ");
                        System.out.println("3. Debit");
                        System.out.println("4. Logout");
                        userinpline = scanner.nextLine();
                        switch (Integer.parseInt(userinpline)) {
                            case 1:
                                balance();
                                break;
                            case 2:
                                credit();
                                break;
                            case 3:
                                debit();
                                break;
                            case 4:
                                clientid = null;
                                lastreqid = null;
                                ClientTransaction.clear();
                                break;
                            default:
                        }


                    }

                } else {
                    System.out.println("Client ID or Password Invalid");
                }
            }
        }
        send("exit");
        try {
            input.close();
            out.close();
            socket.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String[] args){
        ClientTransaction = new ArrayList<>();
        System.out.print("Please Provide The Bank IP Address: ");
        scanner = new Scanner(System.in);
        String ipinp = scanner.nextLine();
        try {
            Client client = new Client(ipinp, 7777);
        } catch (IOException e) {
            System.out.println("Something went wrong, Server ip invalid.");
        }
    }

    // constructor to put ip address and port
    private void login() throws IOException {
        System.out.print("Client Id: ");
        String temp1 = scanner.nextLine();
        System.out.print("Client Password: ");
        String temp2 = scanner.nextLine();
        String logincred = ClientReqid+"#L_" + temp1 + "_-1_" + temp2;
        String severoutput = send(logincred);
        if (!severoutput.equals("Unsuccessful to Log In")) {
            clientid = temp1;
            lastreqid = severoutput;
        } else clientid = null;

    }

    private void balance() throws IOException {
        String logincred = ClientReqid+"#B_" + clientid + "_-1_" + "-1";
        send(logincred);
    }

    private void credit(){
        //hadle input problem
        System.out.print("Give the amount to get credited: ");
        String amount = scanner.nextLine();
        lastreqid = String.valueOf(Integer.parseInt(lastreqid)+1);
        String creditcred = ClientReqid+"#C_" + clientid + "_" + lastreqid + "_" + amount;
        ClientTransaction.add(creditcred);
        send(creditcred);
    }

    private void debit(){
        System.out.print("Give the amount to get debited: ");
        String amount = scanner.nextLine();
        lastreqid = String.valueOf(Integer.parseInt(lastreqid)+1);
        String debitcred = ClientReqid+"#D_" + clientid + "_" +  lastreqid+ "_" + amount;
        ClientTransaction.add(debitcred);
        send(debitcred);
    }
    private String send(String clientprotocol){
        String output = "-1#-1";
        String[] ser = new String[0];
        long startTime = System.currentTimeMillis(),endTime = startTime + 500,errorpercentage = 99, currentnumber;

        try {

            String[] cl = clientprotocol.split("#");
            while (System.currentTimeMillis() < endTime) {
                //This Indicates handling failure of request messages,process failure
                currentnumber = ThreadLocalRandom.current().nextInt(1, 101);
                if(!(currentnumber>errorpercentage)){
                    continue;
                }
                out.writeUTF(clientprotocol);
                output = in.readLine();
                try{
                ser = output.split("#");
                if (Objects.equals(ser[0], cl[0]) )break;
                }catch (Exception ignored){

                }
            }
            if(output==null){ return "Received from server: Error!";}
                ser = output.split("#");
            } catch (IOException ignored) {
        }
        endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Received from server: " + output +" \nTime taken: "+elapsedTime);
        try {
            return ser[1];
        }catch (ArrayIndexOutOfBoundsException ignored){
            return null;
        }
    }
}
