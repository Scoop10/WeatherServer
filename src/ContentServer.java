// A Java program for a Client
import java.io.*;
import java.net.*;
import java.util.*;

// PUT /weather.json HTTP/1.1
// User-Agent: ATOMClient/1/0
// Content-Type: (You should work this one out)
// Content-Length: (And this one too)


public class ContentServer extends Thread { 
    private static Socket thisSocket;
    private static InputStream serverResponse;
    private static OutputStream output;
    private static BufferedReader reader;

    public ContentServer(){
        
    }

    @Override
    public void run() {
        System.out.println("Input the Server URL: ");

        Scanner input = new Scanner(System.in);
        String URL = input.next();
        String[] splitURL = URL.split(":");
        String hostname = splitURL[0];
        int port = Integer.parseInt(splitURL[1]);

        runServer(hostname, port, input);
    }
        
        

    public static void runServer(String hostname, int port, Scanner input){
        try (Socket socket = new Socket(hostname, port)){
            thisSocket = socket;
            try {
                serverResponse = thisSocket.getInputStream(); 
                reader = new BufferedReader(new InputStreamReader(serverResponse));
                while(true){
                    System.out.println("Input the text file with stored weather data: ");
                    String filePath = input.next();

                    if(filePath.equals("shutdown")){
                        socket.close();
                        break;
                    }
    
                    String JSONWeatherData = JSONParser.convertTextToJson(filePath);
                    String PUTRequest = "";
    
                    PUTRequest = PUTRequest.concat("PUT /weather.json HTTP/1.1\n");
                    PUTRequest = PUTRequest.concat("User-Agent: ATOMClient/1/0\n");
                    PUTRequest = PUTRequest.concat("Content-Type: application/json\n");
                    PUTRequest = PUTRequest.concat("Content-Length: " + JSONWeatherData.length() + "\n");
                    PUTRequest = PUTRequest.concat("\n");
                    PUTRequest = PUTRequest.concat(JSONWeatherData);
                    PUTRequest = PUTRequest.concat("\n");
    
    
                    output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println(PUTRequest);
                    writer.flush();
                    System.out.println("Message sent to the server");
                    System.out.println("Received server response: ");
                    System.out.println(reader.readLine());
                }
            } catch (IOException i){
                i.printStackTrace();
            }
        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
}