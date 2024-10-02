import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// The clientHandler is created when a new connection is made. It's primary purpose is to handle all incoming messages and to send the correct responses.
// When a PUT Message is received it will read in the sent information and store that in a file called weatherData.csv
// When a GET Message is received it will read the last line of data from the stored csv file and send that to the requesting client. 
// If any other message is received it will return an error code and disregard any infromation sent in that message.
public class clientHandler implements Runnable {
    // Initialise all private ghobal variables which are used throughout the program:
    private static AtomicInteger lamportClock = new AtomicInteger(0);
    // clientSocket is the socket which the client running on this thread is connected via.
    private Socket clientSocket;
    // out is a print writer which is used to write data to the connected client via the socket
    private PrintWriter out;
    // in is a reader which reads in any data which is sent from the connected client via the socket
    private BufferedReader in;
    // test defines if the clientHandler is to run in test mode or not
    private static boolean test;
    // textOutput is the FileWriter which writes all messages to a txt file if in test mode
    private static FileWriter textOutput;

    // The clientHandler constructor assigns the socket which is passed is an argument to be the clientSocket assigned to this thread
    // Inputs: A socket
    // Outputs: None
    public clientHandler(Socket clientSocket, boolean test, FileWriter textOutput){
        // Assign this clientHandlers socket to be the passed in socket from the aggregation server
        this.clientSocket = clientSocket;
        clientHandler.test = test;
        clientHandler.textOutput = textOutput;
    }

    // The run function is called when the new thread is started in the aggregation server class
    // This performs the message sending and receiving across the assigned client socket.
    // Inputs: None
    // Outputs: None
    @Override
    public void run(){
        try{
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
                        this.clientSocket.close();
                        break;
                    } 
                    // Else if the client has sent nothing - This will occur when the connection ends
                    else if(firstLine == null || "".equals(firstLine)){
                        if(!test){
                            System.out.println("Connection Closing!");
                        }
                        else{
                            textOutput.write("Connection Closing!\n");
                        }
                        // Break the loop
                        break;
                    }
                    // To be done
                    lamportClock.incrementAndGet();
                    // When a new line has been read (THIS SHOULD ALWAYS BE A GET OR PUT)
                    // Print out a verification on the server side of what the received message is
                    if(!test){
                        System.out.println("Server received: \n" + firstLine);
                    }
                    else{
                        textOutput.write("Server received: \n" + firstLine + "\n");
                    }
                    // If the received request is a PUT request
                    if(firstLine.contains("PUT")){
                        // Print out the header information
                        for(int i = 0; i < 3; i++){
                            if(!test){
                                String headers = in.readLine();
                                System.out.println(headers);
                            }
                            else{
                                String headers = in.readLine();
                                textOutput.write(headers + "\n");
                            }
                        }
                        // Get the PUT array data into a String[] format
                        String[] PUTArray = PUTToArray(in);
                        // If the data is not the correct size
                        if(PUTArray[0].equals("Error occurred")){
                            if(!test){
                                System.out.println("Data in PUT Request was not the correct size");
                            }else{
                                textOutput.write("Data in PUT Request was not the correct size\n");
                            }
                        }
                        // Call the appendToCSV function which will store the received JSON in the weatherData CSV file
                        appendToCSV("weatherData.csv", PUTArray);
                        // If this is the first PUT request which has been received from this client
                        if(firstPUT){
                            // Send the 201 status code to the client
                            out.println("201 HTTP_CREATED HTTP/1.1");
                            // Set the firstPUT boolean to be false for the rest of this clients active time
                            firstPUT = false;
                            // Server side verification
                            if(!test){
                                System.out.println("Returned status code 201");
                            }
                            else{
                                textOutput.write("Returned status code 201\n");
                            }
                        }
                        // If the client has sent a PUT request before
                        else{
                            // Send back the 200 status code
                            out.println("200 OK HTTP/1.1");
                            // Server side verification
                            if(!test){
                                System.out.println("Returned status code 200");
                            }
                            else{
                                textOutput.write("Returned status code 200\n");
                            }
                        }
                    }
                    // Else if a GET request is received
                    else if(firstLine.contains("GET")){
                        try {
                            // Print out the GET request
                            textOutput.write("Server received:\n");
                            textOutput.write(firstLine + "\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Call the getLatestData function which will get the last line in the CSV and return it as a String[].
                        String[] latestWeatherData = getLatestData();
                        // Create a json formatted string from the String[] which was created in the last line. The JSONParser class is used to do this.
                        String jsonReponse = JSONParser.arrayToJSON(latestWeatherData);
                        if(jsonReponse.equals("Error occurred")){
                            if(!test){
                                System.out.println("Latest Data Array has incorrect size");
                            }else{
                                textOutput.write("Latest Data Array has incorrect size\n");
                            }
                            continue;
                        }
                        // Send the json formatted string across the socket to the client
                        out.println(jsonReponse);
                        // Server side verification
                        if(!test){
                            System.out.println("Returned latest data");
                        }
                        else{
                            textOutput.write("Returned latest data\n");
                        }
                    }
                    // Else if any other request is made. This would be an invalid request
                    else{
                        // Create a string with the proper 400 status code formatting
                        String status = "400 Bad Request HTTP/1.1\n}";
                        out.println(status);
                        // Server side verification
                        if(!test){
                            System.out.println("Returned status code 400");
                        }
                        else{
                            textOutput.write("Returned status code 400\n");
                        }
                    }
                }
            } catch (IOException i) {
                if(!test){
                    System.out.println("Server exception: " + i.getMessage());
                }
                else{
                    textOutput.write("Server exception: " + i.getMessage() + "\n");
                }
                i.printStackTrace();
            } catch (Exception e){
                if(!test){
                    System.out.println("Server exception: " + e.getMessage());
                }
                else{
                    textOutput.write("Server exception: " + e.getMessage() + "\n");
                }
                e.printStackTrace();
            }
            if(!test){
                System.out.println("A connection has closed!");
            }
            else{
                textOutput.write("A connection has closed!\n");
            }
        } catch(Exception e){

        }
    }


    // The appendToCSV function receives the latest data from a PUT request and appends it to the next line of the weatherData.csv file.
    // Inputs: A string which contains the path to the weatherData.csv file. A String[] which contains the data received from the PUT request
    // Outputs: None
    public static void appendToCSV(String filePath, String[] latestData){
        // Create a new currenEntries list to get the entries which are currently stored in the entries.txt
        List<String[]> currentEntries = new LinkedList<>();
        // Create a new reader to read the stored entries
        try (BufferedReader reader = new BufferedReader(new FileReader("entries.txt"))) {
            // Initialise a line string which will be used to read in each entry
            String line;
            // Until the reader gets to the end of the file get the next line
            while ((line = reader.readLine()) != null) {
                // Split the current entry into it's individual columns
                String[] currentLine = line.split(",");
                // Add the entry to the currentEntries list
                currentEntries.add(currentLine);
            }
        } catch (FileNotFoundException e) {
            // File not found, return empty list
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If the limit of 20 entries hasn't yet been reached
        if (currentEntries.size() < 20){
            // Append the latest entry to the list
            currentEntries.add(latestData);
            // Create a new CSV writer which writes to the specified file (filePath)
            try(BufferedWriter CSVWriter = new BufferedWriter(new FileWriter(filePath))){
                for(int i = 0; i < currentEntries.size(); i++){
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
        // When the 20 entry limit is reached
        else{
            // Remove the oldest data
            currentEntries.remove(0);
            // Append the latest data
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
        // Create a writer to write the latest entries to "entries.txt"
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("entries.txt"))) {
            // For each of the currentEntries
            for (String[] entry : currentEntries) {
                // For all 17 data fields
                for(int i = 0; i < entry.length; i++){
                    // Write this data value to the entries.txt file
                    writer.write(entry[i]);
                    // If not the end of the current entry
                    if(i != entry.length -1){
                        // write a comma
                        writer.write(",");
                    }
                }
                // Next line in the txt file
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
            return lastLineArray;
        } catch (IOException i){
            i.printStackTrace();
            String[] errorArray = {"Error occured"};
            return errorArray;
        }
    }

    // The PUTToArray function takes a PUT function and reads all of the 17 data values which are expected into a String[] which is then returned
    // Inputs: A BufferedReader which will contain the data to PUT
    // Ouptuts: A String[] which contains the data to PUT
    static String[] PUTToArray(BufferedReader reader) throws IOException{
        // Create a new empty string to store the complete message
        String message = "";
        // Create an empty string to read each line
        String messageLine;
        // until the } character is encountered. This will mark the end of the message data
        String endString = "}";
        while(!(endString.equals(messageLine = reader.readLine()))){
            // if a null line is encountered then continue
            if(messageLine == null){
                continue;
            }
            // Add the current message line to the total message
            message = message.concat(messageLine);
            // Add a new line character
            message = message.concat("\n");
        }

        // trim any whitespace from start and end
        String jsonString = message.trim();
        // Remove front curly brace
        jsonString = jsonString.substring(1, jsonString.length()); 
        // Split into key:value pairs
        String[] keyValuePairs = jsonString.split(",");
        // If sent data is not the correct length
        if(keyValuePairs.length != 17){
            // Initialise an empty String[]
            String[] error = new String[1];
            // Write the error message and return it
            error[0] = "Error occurred";
            return error;
        }
        // Start a new counter at 0
        int counter = 0;
        // Create a new String[] to store each data point
        String[] latestData = new String[17];
        // For each key:value pair in the message
        for (String pair : keyValuePairs) {
            // Split the pairs into key and data 
            String[] entry = pair.split(":", 2);
            // Remove any unwanted characted 
            String value = entry[1].trim().replaceAll("\"", "");
            // Add the current data point to the array
            latestData[counter] = value;
            // Iterate the counter
            counter += 1;
        }
        // Return the String[] with all new data
        return latestData;
    }
}
