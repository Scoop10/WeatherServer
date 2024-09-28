import java.io.*;
import java.net.*;

public class AggregationServer extends Thread{

    public static void appendToCSV(String filePath, String[] latestData){
        try(BufferedWriter CSVWriter = new BufferedWriter(new FileWriter(filePath, true))){
            for(int i = 0; i < 17; i++){
                CSVWriter.write(latestData[i]);
                CSVWriter.write(",");
            }
            CSVWriter.newLine();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        int port = 4567;

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Started the Aggregation Server on port ");
            System.out.println(port);
            System.out.println("\n");

            System.out.println("Waiting for Weather Server to connect. ");

            while (true) { 
                Socket socket = serverSocket.accept();
                // socket.setKeepAlive(true);
                System.out.println("Connected to a client.");
                clientHandler newClient = new clientHandler(socket);
                newClient.start();
                newClient.join();
                String[] latestData = newClient.getLatestData();
                appendToCSV("weatherData.csv", latestData);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}