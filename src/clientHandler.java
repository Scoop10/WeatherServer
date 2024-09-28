import java.io.*;
import java.net.*;

public class clientHandler extends Thread {
    private Socket clientSocket;
    public String[] latestData;

    public clientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public String[] getLatestData(){
        String filePath = "weatherData.csv";
        try(RandomAccessFile weatherCSV = new RandomAccessFile(filePath, "r")){
            long fileLength = weatherCSV.length() - 1;
            StringBuilder lastLineReversed = new StringBuilder();

            for(long pointer = fileLength; pointer >= 0; pointer--){
                weatherCSV.seek(pointer);
                char currentCharacter = (char) weatherCSV.read();
                if(currentCharacter == '\n' && pointer != fileLength){
                    break;
                }
                lastLineReversed.append(currentCharacter);
            }
            String lastLineString = lastLineReversed.reverse().toString();
            String[] lastLineArray = lastLineString.split(",");
            String[] returnArray = new String[17];
            for(int i = 0; i < lastLineArray.length - 1; i++){
                returnArray[i] = lastLineArray[i];
            }
            return returnArray;
        } catch (IOException i){
            i.printStackTrace();
            String[] errorArray = {"Error occured"};
            return errorArray;
        }
    }
    
    public void run(){
        try(InputStream input = clientSocket.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            
            String firstLine = reader.readLine();
            if(firstLine.contains("PUT")){
                this.latestData = PUTToArray(reader);
            }
            else if(firstLine.contains("GET")){
                String[] latestWeatherData = getLatestData();
                String jsonReponse = JSONParser.arrayToJSON(latestWeatherData);
                OutputStream response = clientSocket.getOutputStream();
                PrintWriter responseWriter = new PrintWriter(response, true);
                responseWriter.println(jsonReponse);
            }
        } catch (IOException i) {
            System.out.println("Server exception: " + i.getMessage());
            i.printStackTrace();
        }
    }

    static String[] PUTToArray(BufferedReader reader) throws IOException{
        for(int i = 0; i < 3; i++){
            String garbage = reader.readLine();
        }
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

        message = message.concat("}\n");

        String jsonString = message.trim();
        jsonString = jsonString.substring(1, jsonString.length() - 1); // Remove curly braces

        String[] keyValuePairs = jsonString.split(",");

        int counter = 0;
        String[] latestData = new String[17];

        for (String pair : keyValuePairs) {
            String[] entry = pair.split(":", 2);
            String value = entry[1].trim().replaceAll("\"", "");
            latestData[counter] = value;
            counter += 1;
        }

        return latestData;
    }
}
