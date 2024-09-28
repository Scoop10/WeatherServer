import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

public class getClient {
    public static void main(String[] args){
        System.out.println("Input the Server URL in the format 'servername:portnumber' : ");

        Scanner input = new Scanner(System.in);
        String URL = input.next();
        String[] splitURL = URL.split(":");
        String hostname = splitURL[0];
        int port = Integer.parseInt(splitURL[1]);

        input.close();

        String GETRequest = "";

        GETRequest = GETRequest.concat("GET HTTP/1.1\n");

        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(GETRequest);
            writer.flush();
            System.out.println("Message sent to the server");

            try(InputStream serverMessage = socket.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(serverMessage))){
                String message = "";
                String messageLine;
                String endString = "}";
                while(!(endString.equals(messageLine = reader.readLine()))){
                    if(messageLine == null){
                        continue;
                    }
                    message = message.concat(messageLine);
                    message = message.concat("\n");
                }
        
                message = message.concat("}\n");
        
                String jsonString = message.trim();
                jsonString = jsonString.substring(1, jsonString.length() - 1); // Remove curly braces
        
                String[] keyValuePairs = jsonString.split(",");
        
                int counter = 0;
                String[] latestData = new String[17];
        
                for (String pair : keyValuePairs) {
                    String[] entry = pair.split(":", 2);
                    String value = entry[1].trim().replaceAll("\"", "");
                    latestData[counter] = value;
                    counter += 1;
                }

                for(int i = 0; i < latestData.length; i++){
                    System.out.println(latestData[i]);
                }
            }


        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
}
