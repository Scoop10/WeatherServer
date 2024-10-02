import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

// The GETClient is used to get weather data from the AggregationServer and outputs it to the command line
public class GETClient extends Thread {
    // test is used to determine if the program is to run in test mode or not
    private static boolean test;
    // hostname is the host which the server is running on 
    private static String hostname;
    // port is the port the server is running on 
    private static int port;
    private static Scanner input;
    // Constructor to set test mode or not
    public GETClient(boolean test){
        GETClient.test = test;
    }

    // Run will run the getClient program
    // Inputs: none
    // Outputs: none
    @Override
    public void run(){
        // if not in testing mode
        if(!test){
            // Prompt the user
            System.out.println("Input the Server URL in the format 'servername:portnumber' : ");
            // Get user input
            GETClient.input = new Scanner(System.in);
            String URL = input.next();
            // Split into {hostname,port}
            String[] splitURL = URL.split(":");
            hostname = splitURL[0];
            port = Integer.parseInt(splitURL[1]);
        }   
        // if in testing mode
        else{
            // set the hostname and port to default
            hostname = "localhost";
            port = 4567;
        }
       
        // Empty GETRequest
        String GETRequest = "";
        // Create GET Request
        GETRequest = GETRequest.concat("GET HTTP/1.1\n");
        // Open up a connection to the specified server
        try (Socket socket = new Socket(hostname, port)) {
            // get the outputstream and printWriter to send data 
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            // Send the GETRequest
            writer.println(GETRequest);
            writer.flush();
            // Confirm with user
            System.out.println("Message sent to the server");
            // get input stream to receive response
            try(InputStream serverMessage = socket.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(serverMessage))){
                // Create empty string
                String message = "";
                String messageLine;
                String endString = "}";
                // until a } is read in
                while(!(endString.equals(messageLine = reader.readLine()))){
                    // If nothing is sent then continue receiving
                    if(messageLine == null){
                        continue;
                    }
                    // Add the readline into the message
                    message = message.concat(messageLine);
                    message = message.concat("\n");
                }
                // Trim whitespace
                String jsonString = message.trim();
                // Remove front curly brace
                jsonString = jsonString.substring(1, jsonString.length()); 
                // Print out the received data
                System.out.print("Received Data:\n" + jsonString + "\n");
            }
        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("I/O error: " + e.getMessage());
        }

        System.out.println("\nGET Succeeded. Sending BAD Request!\n");

        String BADRequest = "";
        BADRequest = BADRequest.concat("BAD HTTP/1.1\n");

        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(BADRequest);
            writer.flush();
            System.out.println("BAD Message sent to the server");

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
        
                String jsonString = message.trim();
                if(jsonString.charAt(0) == '{'){
                    jsonString = jsonString.substring(1, jsonString.length()); // Remove curly braces   
                    System.out.print(jsonString + "\n");
                }
                else{
                    System.out.print(message);
                }
        
                socket.close();
            } catch (UnknownHostException e) {
                System.out.println("Server not found: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("I/O error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("I/O error: " + e.getMessage());
        }
        
        if(test){
            return;
        }

        while (true) {
            try {
                String inputMessage = input.next();
                if(inputMessage.equals("shutdown")){
                    break;
                }
                try (Socket socket = new Socket(hostname, port)) {
                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println(inputMessage);
                    writer.flush();
                    System.out.println("Your Message was sent to the server");
    
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
                
                        String jsonString = message.trim();
                        if(jsonString.charAt(0) == '{'){
                            jsonString = jsonString.substring(1, jsonString.length()); // Remove curly braces   
                            System.out.print(jsonString + "\n");
                        }
                        else{
                            System.out.print(message);
                        }
                    } catch (UnknownHostException e) {
                        System.out.println("Server not found: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("I/O error: " + e.getMessage());
                    }
                } catch (UnknownHostException e) {
                    System.out.println("Server not found: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("I/O error: " + e.getMessage());
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        GETClient newClient = new GETClient(false);
        new Thread(newClient).start();
    }
}