
import java.io.FileWriter;


public class autoTest {
    public static void main(String[] args){

        System.out.println("Clearing output file");

        try{FileWriter clearer = new FileWriter("testResultsServer.txt");

        clearer.write("");
        clearer.close();}
        catch(Exception e){
            e.printStackTrace();
        }

        System.out.println("Starting automated testing!");

        AggregationServer newServer = new AggregationServer(4567, true);

        System.out.println("\nNew Server Created! \n");

        Thread serverThread =  new Thread(newServer);
        serverThread.start();

        System.out.println("Beginning indiviudal tests with no concurrency!");

        System.out.println("Creating new Content Server to connect to AggregationServer!\n");

        ContentServer newContentServer = new ContentServer(true);

        System.out.println("Content Server Created! Beginning content server test!\n");

        try {
            Thread contentServerThread = new Thread(newContentServer);
            contentServerThread.start();
            contentServerThread.join();
        } catch (Exception e) {
            System.out.println("Exception " + e + "Occurred!! Shutting down Content Server!");
        }

        System.out.println("Starting GETClient testing!");

        GETClient newGetClient = new GETClient(true);

        try {
            Thread GETClientThread = new Thread(newGetClient);
            GETClientThread.start();
            GETClientThread.join();
        } catch (Exception e) {
            System.out.println("Exception " + e + "Occurred!! Shutting down GETClient!");
        }

        System.out.println("First stage of testing succeeded! Beginning concurrency tests!");

        // ContentServer newContentServer1 = new ContentServer(true);
        // ContentServer newContentServer2 = new ContentServer(true);
        // ContentServer newContentServer3 = new ContentServer(true);
        // ContentServer newContentServer4 = new ContentServer(true);

        // Thread thread1 = new Thread(newContentServer1);
        // Thread thread2 = new Thread(newContentServer2);
        // Thread thread3 = new Thread(newContentServer3);
        // Thread thread4 = new Thread(newContentServer4);

        // thread1.start();
        // thread2.start();
        // thread3.start();
        // thread4.start();

        newServer.shutdown();
    }
}