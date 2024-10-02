// A Java program for a Client
import java.io.*;
import java.net.*;
import java.util.*;


// The content server reads in data from a text file and sends a PUT request to the aggregation server.
public class ContentServer extends Thread { 
    // thisSocket is the socket which the Content Server is connected to the Aggregation Server on 
    private static Socket thisSocket;
    // The serverResponse is an InputStream which constantly listens for any response from the server on thisSocket
    private static InputStream serverResponse;
    // output is the OutputStream which the ContentServer sends data to the server via
    private static OutputStream output;
    // Reader is the reader which reads the data from the specified text file
    private static BufferedReader reader;
    // test defines whether or not to run this program in test mode
    private static boolean test;
    private static boolean gotInput = false;

    // Constructor for the content server
    // Inputs: a boolean test which sets the program to run in test mode if set to true
    public ContentServer(boolean test){
        // Set this test to be the test input in the constructor
        ContentServer.test = test;
    }

    // The run function is the main function for initilasing all required variables before starting the ContentServer
    // Inputs:  None
    // Outputs: None
    @Override
    public void run() {
        // If in test mode
        if(test){
            // Set the host to be localhost
            String hostname = "localhost";
            // Set the port to be the default port
            int port = 4567;
            // Run the testServer function
            testServer(hostname, port);
        }
        // If not in testing mode
        else{
            // prompt the user
            System.out.println("Input the Server URL ('host:port'): ");
            // Create a scanner to read user input
            Scanner input = new Scanner(System.in);
            // Get the URL from the user
            String URL = input.next();
            // Split the URL into hostname and port
            String[] splitURL = URL.split(":");
            String hostname = splitURL[0];
            int port = Integer.parseInt(splitURL[1]);
            // Run the server
            runServer(hostname, port, input);
        }
    }
        
    // runServer is a function which runs the server without being in test mode. It will start up the server and then wait for the user to input a file location
    // Inputs: hostname and port, the scanner which reads user input
    // Outputs: None
    public static void runServer(String hostname, int port, Scanner input){
        // Get the connection to the server
        try (Socket socket = new Socket(hostname, port)){
            // set this socket to be the socket setup in previous line
            thisSocket = socket;
            try {
                // Set the serverResponse to listen for incoming messages 
                serverResponse = thisSocket.getInputStream(); 
                reader = new BufferedReader(new InputStreamReader(serverResponse));
                // Run until 'shutdown' is input
                while(true){
                    // Prompt the user
                    System.out.println("Input the text file with stored weather data: ");
                    // Get user input
                    String filePath = input.next();
                    // Check if shutdown requested
                    if(filePath.equals("shutdown")){
                        // Shutdown program
                        socket.close();
                        break;
                    }
                    // Get the weather data from the text file and convert to JSON format
                    String JSONWeatherData = JSONParser.convertTextToJson(filePath);
                    // If an error occurred while parsing requested data
                    if(JSONWeatherData.equals("Error occurred")){
                        System.out.println("Invalid File Path! Try again!");
                        continue;
                    }
                    // Create new empty string to build the message with
                    String PUTRequest = "";
                    
                    // Append the standard headers
                    PUTRequest = PUTRequest.concat("PUT /weather.json HTTP/1.1\n");
                    PUTRequest = PUTRequest.concat("User-Agent: ATOMClient/1/0\n");
                    PUTRequest = PUTRequest.concat("Content-Type: application/json\n");
                    PUTRequest = PUTRequest.concat("Content-Length: " + JSONWeatherData.length() + "\n");
                    // Go to next line
                    PUTRequest = PUTRequest.concat("\n");
                    // Append the JSON formatted weather data
                    PUTRequest = PUTRequest.concat(JSONWeatherData);
                    PUTRequest = PUTRequest.concat("\n");
    
                    // Get the output stream to send messages
                    output = socket.getOutputStream();
                    // Create a PrintWriter object to send data over the socket
                    PrintWriter writer = new PrintWriter(output, true);
                    while(true){
                        // Send the PUTRequest
                        writer.println(PUTRequest);
                        writer.flush();
                        // Confirmation to user
                        System.out.println("Message sent to the server");
                        gotInput = false;
                        // Receive the status code from the server. if not received within 5 seconds send the PUT Request again
                        Thread responseThread = new Thread(() -> {
                                try{
                                        System.out.println("Received server response: ");
                                        System.out.println(reader.readLine());
                                        gotInput = true;
                                } catch(IOException i){
                                    
                                }
                            }
                        );
                        // Start the receiving thread 
                        responseThread.start();
                        try{
                            // Wait 5 seconds to see if thread will terminate
                            responseThread.join(5000);
                        } catch(InterruptedException i){
                        }
                        // If the thread didn't terminate, the gotInput boolean will be false
                        if(!gotInput){
                            System.out.println("Didn't receive response from server within 5 seconds. Resending request!");
                        }else{
                            break;
                        }
                    }
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

    // testServer runs the server in testing mode. This means that the user inputs are set and the messages are output to a text file
    public static void testServer(String hostname, int port){
        // Create a connection to the server on the specified hostname and port
        try (Socket socket = new Socket(hostname, port)){
            // Set this socket to be the created socket
            thisSocket = socket;
            try {
                // Get the input stream
                serverResponse = thisSocket.getInputStream(); 
                // get the file reader
                reader = new BufferedReader(new InputStreamReader(serverResponse));
                
                // set the filepath
                String filePath = "otherData.txt";
                // convert the text file to JSON format
                String JSONWeatherData = JSONParser.convertTextToJson(filePath);
                // initialise the empty string
                String PUTRequest = "";

                // Create the PUTRequest with appropriate headers and data from above. An error should never occur here with the set data
                PUTRequest = PUTRequest.concat("PUT /weather.json HTTP/1.1\n");
                PUTRequest = PUTRequest.concat("User-Agent: ATOMClient/1/0\n");
                PUTRequest = PUTRequest.concat("Content-Type: application/json\n");
                PUTRequest = PUTRequest.concat("Content-Length: " + JSONWeatherData.length() + "\n");
                PUTRequest = PUTRequest.concat("\n");
                PUTRequest = PUTRequest.concat(JSONWeatherData);
                PUTRequest = PUTRequest.concat("\n");
                // get the output stream
                output = socket.getOutputStream();
                // create print writer to send data
                PrintWriter writer = new PrintWriter(output, true);
                // Until a response is received
                while(true){
                    // Send the PUTRequest
                    writer.println(PUTRequest);
                    writer.flush();
                    // Confirmation to user
                    System.out.println("Message sent to the server");
                    gotInput = false;
                        // Receive the status code from the server. if not received within 5 seconds send the PUT Request again
                        Thread responseThread = new Thread(() -> {
                            try{
                                    System.out.println("Received server response: ");
                                    System.out.println(reader.readLine());
                                    gotInput = true;
                            } catch(IOException i){
                                
                            }
                        }
                    );
                    // Start the receiving thread 
                    responseThread.start();
                    try{
                        // Wait 5 seconds to see if thread will terminate
                        responseThread.join(5000);
                    } catch(InterruptedException i){
                    }
                    // If the thread didn't terminate, the gotInput boolean will be false
                    if(!gotInput){
                        System.out.println("Didn't receive response from server within 5 seconds. Resending request!");
                    }else{
                        break;
                    }
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
    // The main is used if the Content Server is run by a user. It creates a new content server object and then runs the server in non-testing mode
    public static void main(String[] args){
        ContentServer newContentServer = new ContentServer(false);
        new Thread(newContentServer).start();
    }
}