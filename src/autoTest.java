public class autoTest {
    public static void main(String[] args){
        AggregationServer newServer = new AggregationServer(4567);

        new Thread(newServer).start();

        ContentServer newContentServer = new ContentServer(true);

        new Thread(newContentServer).start();

        getClient newGetClient = new getClient(true);

        new Thread(newGetClient).start();

    }
}