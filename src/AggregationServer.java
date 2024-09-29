import java.net.*;

public class AggregationServer extends Thread{
    public static void main(String[] args) {
        int port = 4567;

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Started the Aggregation Server on port ");
            System.out.println(port);
            System.out.println("\n");

            System.out.println("Waiting for Weather Server to connect. ");

            while (true) { 
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected to a client.");
                new Thread(new clientHandler(clientSocket)).start();
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}