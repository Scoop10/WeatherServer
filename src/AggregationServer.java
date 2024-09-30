import java.net.*;

// This class creates a new AggregationServer and then listens for new clients. 
// A client handler class is used in conjunction with this which is called on a new thread each time a client connects to the ServerSocket. 
public class AggregationServer extends Thread{
    private int port;
    
    public AggregationServer(int port){
        // Initialise the port
        this.port = port;
    }
    
    // The main function starts the ServerSocket on a nominated port on the localhost. This could be deployed on a server if needed. 
    // No arguments are input to this function
    @Override
    public void run() {
        // Open a ServerSocket on the port specified above. Catch any exceptions in the catch clause. 
        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            // Print out confirmation messages to verify server startup
            System.out.println("Started the Aggregation Server on port ");
            System.out.println(port);
            System.out.println("\n");
            System.out.println("Waiting for connection. ");
            // Run this indefinitely until the server is shutdown by the host. 
            while (true) { 
                // Accept any new connections on the ServerSocket and store them in a temporary clientSocket variable
                Socket clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);
                // Print a confirmation message
                System.out.println("Opened a new connection.");
                // Create and start a new thread to run the clientHandler function. Pass in the clientSocket created above as the only argument. 
                new Thread(new clientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}