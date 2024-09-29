import java.io.*;
import java.net.*;

public class clientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    public String[] latestData;

    public clientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run(){
        boolean firstPUT = true;
        try{
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException i){
             i.printStackTrace();
        }
        while(true){
            try {
                String firstLine = in.readLine();
                if("shutdown".equalsIgnoreCase(firstLine)){
                    break;
                } else if(firstLine == null || "".equals(firstLine)){
                    continue;
                }
                System.out.println("Server received: \n");
                System.out.println(firstLine);

                if(firstLine.contains("PUT")){
                    appendToCSV("weatherData.csv", PUTToArray(in));
                    OutputStream response = clientSocket.getOutputStream();
                    PrintWriter responseWriter = new PrintWriter(response, true);
                    if(firstPUT){
                        responseWriter.println("201 HTTP_CREATED HTTP/1.1\n");
                        firstPUT = false;
                        System.out.println(clientSocket.isClosed());
                    }
                    else{
                        responseWriter.println("200 OK HTTP/1.1\n");
                    }
                }
                else if(firstLine.contains("GET")){
                    String[] latestWeatherData = getLatestData();
                    String jsonReponse = JSONParser.arrayToJSON(latestWeatherData);
                    OutputStream response = clientSocket.getOutputStream();
                    PrintWriter responseWriter = new PrintWriter(response, true);
                    responseWriter.println(jsonReponse);
                }
                else{
                    String status = "400 Bad Request HTTP/1.1\n}";
                    OutputStream response = clientSocket.getOutputStream();
                    PrintWriter responseWriter = new PrintWriter(response, true);
                    responseWriter.println(status);
                    System.out.println("Returned status code 400");
                }
            } catch (IOException i) {
                System.out.println("Server exception: " + i.getMessage());
                i.printStackTrace();
                break;
            }
        }
    }

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

    static String[] PUTToArray(BufferedReader reader) throws IOException{
        for(int i = 0; i < 3; i++){
            String garbage = reader.readLine();
            System.out.println(garbage);
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
