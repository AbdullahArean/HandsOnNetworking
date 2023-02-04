import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.nio.channels.ReadableByteChannel;
import java.io.FileOutputStream;
import java.nio.channels.Channels;




public class Client {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String baseURL = "http://localhost:8000/";
        String fileName = null;

        while(true){
            System.out.println("\n\nChoose Operation: ");
            System.out.println("1. Download Specific file");
            System.out.println("2. Upload File");
            System.out.println("3. Any number to quit program");
            System.out.print("Enter you Choice: ");
            int choice = scanner.nextInt();
            if(choice==1){
                System.out.print("Enter file name: ");
                fileName = scanner.next();
                URL url = new URL(baseURL+fileName);
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                System.out.println(fileName+" has been downloaded.");
            }else if(choice==2){
                System.out.print("Enter the file name to upload: ");
                fileName = scanner.next();
                URL url = new URL(baseURL+fileName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type",
                                                "application/octet-stream");
                connection.setRequestProperty("Content-Length",
                                    String.valueOf(new File(fileName).length()));
                OutputStream os = connection.getOutputStream();
                Files.copy(new File(fileName).toPath(), os);
                int resCode = connection.getResponseCode();
                if(resCode==200){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    System.out.println(reader.readLine());
                }else{
                    System.out.println("Upload Failed, Please Try Again...");
                }
                os.close();
            }else{
                break;
            }
        }
    }
}
