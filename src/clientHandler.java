import java.io.*;
import java.net.*;
import java.util.*;

// The clientHandler is created when a new connection is made. It's primary purpose is to handle all incoming messages and to send the correct responses.
// When a PUT Message is received it will read in the sent information and store that in a file called weatherData.csv
// When a GET Message is received it will read the last line of data from the stored csv file and send that to the requesting client. 
// If any other message is received it will return an error code and disregard any infromation sent in that message.
public class clientHandler implements Runnable {
    // Initialise all private ghobal variables which are used throughout the program:
    // clientSocket is the socket which the client running on this thread is connected via.
    private Socket clientSocket;
    // out is a print writer which is used to write data to the connected client via the socket
    private PrintWriter out;
    // in is a reader which reads in any data which is sent from the connected client via the socket
    private BufferedReader in;

    // The clientHandler constructor assigns the socket which is passed is an argument to be the clientSocket assigned to this thread
    // Inputs: A socket
    // Outputs: None
    public clientHandler(Socket clientSocket){
        // Assign this clientHandlers socket to be the passed in socket from the aggregation server
        this.clientSocket = clientSocket;
    }

    // The run function is called when the new thread is started in the aggregation server class
    // This performs the message sending and receiving across the assigned client socket.
    // Inputs: None
    // Outputs: None
    @Override
    public void run(){
        // Create a boolean which is set to true. This is used to determine if a 201 (first PUT) or 200 (every PUT after that) code should be returned. 
        boolean firstPUT = true;
        // Run this until the client inputs "shutdown" which will then shut down this clientHandler
        try {
            while(true){
                // Try to create a new out and in variable (global variables) which are linked to the assigned clientSocket. Catch any IOexceptions and print them
                try{
                    // Create a new PrintWriter which uses the clientSockets output stream to send data over the socket
                    this.out = new PrintWriter(clientSocket.getOutputStream(), true);
                    // Create a new BufferedReader which uses the clientSockets input stream to read data from the socket
                    this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                } catch (IOException i){
                    i.printStackTrace();
                } catch(Exception e){
                    e.printStackTrace();
                }
                // Try this block and catch any exceptions
                // Read in a single line from the input stream
                String firstLine = in.readLine();
                // If the line reads "shutdown":
                if("shutdown".equalsIgnoreCase(firstLine)){
                    // Break the while loop and shut down the client connection
                    this.in.close();
                    this.clientSocket.close();
                    break;
                } 
                // Else if the client has sent nothing:
                else if(firstLine == null || "".equals(firstLine)){
                    System.out.println("Connection Closing!");
                    // Break the loop
                    break;
                }

                // When a new line has been read (THIS SHOULD ALWAYS BE A GET OR PUT)
                // Print out a verification on the server side of what the received message is
                System.out.println("Server received: \n");
                System.out.println(firstLine);
                // If the received request is a PUT request
                if(firstLine.contains("PUT")){
                    // Call the appendToCSV function which will store the received JSON in the weatherData CSV file
                    // PUTToArray is used as the second argument. This function returns a String[] which is used as the second argument. 
                    appendToCSV("weatherData.csv", PUTToArray(in));
                    // If this is the first PUT request which has been received from this client
                    if(firstPUT){
                        // Send the 201 status code to the client
                        out.println("201 HTTP_CREATED HTTP/1.1\n");
                        // Set the firstPUT boolean to be false for the rest of this clients active time
                        firstPUT = false;
                        // Server side verification
                        System.out.println("Returned status code 201");
                    }
                    // If the client has sent a PUT request before
                    else{
                        // Send back the 200 status code
                        out.println("200 OK HTTP/1.1\n");
                        // Server side verification
                        System.out.println("Returned status code 200");
                    }
                }
                // Else if a GET request is received
                else if(firstLine.contains("GET")){
                    // Call the getLatestData function which will get the last line in the CSV and return it as a String[].
                    String[] latestWeatherData = getLatestData();
                    // Create a json formatted string from the String[] which was created in the last line. The JSONParser class is used to do this.
                    String jsonReponse = JSONParser.arrayToJSON(latestWeatherData);
                    // Send the json formatted string across the socket to the client
                    out.println(jsonReponse);
                    // Server side verification
                    System.out.println("Returned latest data");
                }
                // Else if any other request is made. This would be an invalid request
                else{
                    // Create a string with the proper 400 status code formatting
                    String status = "400 Bad Request HTTP/1.1\n}";
                    out.println(status);
                    // Server side verification
                    System.out.println("Returned status code 400");
                }
            }
        } catch (IOException i) {
            System.out.println("Server exception: " + i.getMessage());
            i.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("A connection has closed!");
    }


    // The appendToCSV function receives the latest data from a PUT request and appends it to the next line of the weatherData.csv file.
    // Inputs: A string which contains the path to the weatherData.csv file. A String[] which contains the data received from the PUT request
    // Outputs: None
    public static void appendToCSV(String filePath, String[] latestData){

        List<String[]> currentEntries = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("entries.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] currentLine = line.split(",");
                currentEntries.add(currentLine);
            }
        } catch (FileNotFoundException e) {
            // File not found, return empty list
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (currentEntries.size() < 20){
            currentEntries.add(latestData);
            // Create a new CSV writer which writes to the specified file (filePath)
            try(BufferedWriter CSVWriter = new BufferedWriter(new FileWriter(filePath, true))){
                // Loop through every line of JSON formatted string
                for(int i = 0; i < 17; i++){
                    // Write the value at the current position to the CSV line
                    CSVWriter.write(latestData[i]);
                    // Add a comme to indicate a new column
                    CSVWriter.write(",");
                }
                CSVWriter.newLine();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        } else{
            currentEntries.remove(0);
            currentEntries.add(latestData);
            // Create a new CSV writer which writes to the specified file (filePath)
            try(BufferedWriter CSVWriter = new BufferedWriter(new FileWriter(filePath, false))){
                for(int i = 0; i < 20; i++){
                    // Create a new line in the CSV
                    CSVWriter.newLine();
                    // Loop through every line of JSON formatted string
                    for(int j = 0; j < 17; j++){
                        // Write the value at the current position to the CSV line
                        CSVWriter.write(currentEntries.get(i)[j]);
                        // Add a comma to indicate a new column
                        CSVWriter.write(",");
                    }
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("entries.txt"))) {
            for (String[] entry : currentEntries) {
                for(int i = 0; i < entry.length; i++){
                    writer.write(entry[i]);
                    if(i != entry.length -1){
                        writer.write(",");
                    }
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // The getLatestData function is used to return the latest data from the csv which stores all the weather data. 
    // Inputs: None
    // Outputs: A String[] which contains the latest data
    public String[] getLatestData(){
        // Set the file path to be the weatherData.csv file
        String filePath = "weatherData.csv";
        // Open the weatherCSV as a random access file in read mode
        try(RandomAccessFile weatherCSV = new RandomAccessFile(filePath, "r")){
            // get the length of the CSV and take 1 off to get the end index of the file
            long fileLength = weatherCSV.length() - 1;
            // Create a new string builder which will get the last line of the csv in reverse
            StringBuilder lastLineReversed = new StringBuilder();
            // Iterate through the entire CSV file from the end
            for(long pointer = fileLength; pointer >= 0; pointer--){
                // Set the current space to be where the pointer is
                weatherCSV.seek(pointer);
                // get the character at the position of the pointer
                char currentCharacter = (char) weatherCSV.read();
                // If the currentCharacter is a new line and the pointer is not at the end of the file
                if(currentCharacter == '\n' && pointer != fileLength){
                    // break the for loop
                    break;
                }
                // If not at a newline append the character which the pointer is at
                lastLineReversed.append(currentCharacter);
            }
            // reverse the string to get the last line of the csv in the correct order
            String lastLineString = lastLineReversed.reverse().toString();
            // Split the string into an array using "," as a delimiter
            String[] lastLineArray = lastLineString.split(",");
            // // 
            // String[] returnArray = new String[17];
            // for(int i = 0; i < lastLineArray.length - 1; i++){
            //     returnArray[i] = lastLineArray[i];
            // }
            return lastLineArray;
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
