public class autoTest {
    public static void main(String[] args){
        AggregationServer newServer = new AggregationServer(4567);

        new Thread(newServer).start();

        
    }
}
