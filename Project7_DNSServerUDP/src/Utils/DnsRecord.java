package Utils;

import java.io.*;
import java.util.ArrayList;

public class DnsRecord {
    private String name;
    private String value;
    private short type;
    private short ttl;

    public DnsRecord(String name, String value, short type, short ttl) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.ttl = ttl;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        String ans = switch (this.type) {
            case 1 -> "A";
            case 2 -> "AAAA";
            case 3 -> "NS";
            case 4 -> "CNAME";
            case 5 -> "MX";
            default -> "type";
        };
        return ans;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setType(short type) {
        this.type = type;
    }

    public void setTtl(short ttl) {
        this.ttl = ttl;
    }
    public short getTTL() {
        return this.ttl;
    }

    public static void writeRecordsToFile(ArrayList<DnsRecord> records, String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("Name\tValue\tType\tTTL");
            int i=0;
            for (DnsRecord record : records) {
                if(i!=0) {
                    writer.printf("%s\t%s\t%s\t%d%n", record.getName(), record.getValue(),
                            record.getType(), record.getTTL());
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static ArrayList<DnsRecord> readRecordsFromFile(String fileName) throws IOException {
        new BufferedReader(new FileReader(fileName));
        BufferedReader br;
        String line;
        br = new BufferedReader(new FileReader(fileName));
        ArrayList<DnsRecord> records = new ArrayList<>();
        int count = 0;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\s+");
            String name = parts[0];
            String value = parts[1];
            short type = switch (parts[2]) {
                case "A" -> 1;
                case "AAAA" -> 2;
                case "NS" -> 3;
                case "CNAME" -> 4;
                case "MX" -> 5;
                default -> 9;
            };
            short ttl = 60;
            records.add(new DnsRecord(name, value, type, ttl));
            count++;
        }
        br.close();
        return records;
    }

    public static void main(String[] args) throws IOException {
        ArrayList<DnsRecord> localstorage = readRecordsFromFile("dns_records_auth.txt");
        for(int i=0; i<localstorage.size(); i++){
            System.out.println(localstorage.get(i).name+" "+ localstorage.get(i).value+ " "+ localstorage.get(i).type+" "+ localstorage.get(i).ttl);
        }
        localstorage.add(new DnsRecord("google.com", "8.8.8.8", (short) 1, (short) 60));
        writeRecordsToFile(localstorage,"dns_records_auth.txt");
        System.out.println("After Insertion");
        ArrayList<DnsRecord> localstorage1 = readRecordsFromFile("dns_records_auth.txt");
        for(int i=0; i<localstorage1.size(); i++){
            System.out.println(localstorage1.get(i).name+" "+ localstorage1.get(i).value+ " "+ localstorage1.get(i).type+" "+ localstorage1.get(i).ttl);
        }

    }
}
