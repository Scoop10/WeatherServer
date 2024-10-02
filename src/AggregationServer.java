import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

// This class creates a new AggregationServer and then listens for new clients. 
// A client handler class is used in conjunction with this which is called on a new thread each time a client connects to the ServerSocket. 
public class AggregationServer extends Thread{
    // port is the port which the aggregation server is hosted on
    private static int port = 4567;
    // ServerSocket is the ServerSocket which is hosting the server and listening for clients
    private static ServerSocket serverSocket;
    // group is a threadGroup which contains all currently running threads
    private static ThreadGroup group;
    // The lamport clock is kept using an atomic integer
    private static AtomicInteger lamportClock = new AtomicInteger(0);
    // test is by default false. If set to true the aggregation server runs in test mode
    private static boolean test = false;
    // textOutput writes all the test prompts to the defined test results folder
    private static FileWriter textOutput;
    
    // Constructor for the aggregation server
    // Inputs: an integer port number, a boolean to define if in testing mode or not
    public AggregationServer(int port, boolean test){
        // Initialise the port
        AggregationServer.port = port;
        // Initialise the test
        AggregationServer.test = test;
    }
    
    // The main function starts the ServerSocket on a nominated port on the localhost. This could be deployed on a server if needed. 
    // No arguments are input to this function
    @Override
    public void run() {
        try {
            // Open a ServerSocket on the port specified above. Catch any exceptions in the catch clause. 
            AggregationServer.serverSocket = new ServerSocket(this.port);
            // Create a new file writer
            AggregationServer.textOutput = new FileWriter("testResultsServer.txt");
            // Print out confirmation messages to verify server startup
            System.out.println("Started the Aggregation Server on port ");
            System.out.println(port);
            System.out.println("\n");
            System.out.println("Waiting for connection. ");
            // Create a new ThreadGroup which will store all running threads
            AggregationServer.group = new ThreadGroup("Threads");
            // Run this indefinitely until the server is shutdown by the host. 
            while (true) {
                // Accept any new connections on the ServerSocket and store them in a temporary clientSocket variable
                Socket clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);
                // Print a confirmation message
                // System.out.println("Opened a new connection.");
                // Create and start a new thread to run the clientHandler function. Pass in the clientSocket created above as the only argument. 
                new Thread(group, new clientHandler(clientSocket, AggregationServer.test, AggregationServer.textOutput)).start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void shutdown(){
        try {
            // Kill all threads which are currently running
            group.interrupt();
            // Finally close the ServerSocket
            AggregationServer.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        try {            
            // Open a new scanner to receive the port
            Scanner input = new Scanner(System.in);
            // Prompt the user
            System.out.println("Input the port to host the server on: ");
            // Read in the port and convert to an int
            int port = Integer.parseInt(input.next());
            AggregationServer.port = port;
            input.close();

            AggregationServer newServer = new AggregationServer(port, false);

            new Thread(newServer).start();
            
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}