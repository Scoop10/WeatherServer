import java.io.*;

// The JSON Parser class is used to perform JSON string formatting

public class JSONParser {
    // The convertTextToJson function reads in a text file with 17 data points and converts this into a json formatted string
    // Inputs: A string containing the path to the text file
    // Outputs: A string in JSON format
    public static String convertTextToJson(String filePath) {
        // Create a new StringBuilder object
        StringBuilder jsonBuilder = new StringBuilder();
        // Add opening curly brace
        jsonBuilder.append("{\n");
        // Create a new reader to read in the text file`
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Initialise an empty string to read each line
            String line;
            // Keep reading until the end of the file
            while ((line = reader.readLine()) != null) {
                // Split the current line into a key and value pair
                String[] keyValue = line.split(":", 2);
                // If the key and value is split properly
                if (keyValue.length == 2) {
                    // Get the key
                    String key = keyValue[0].trim();
                    // Get the value
                    String value = keyValue[1].trim();
                    // Format into JSON format
                    jsonBuilder.append("  \"").append(key).append("\": \"").append(value).append("\",\n");
                }  
                // If the key and value wasn't split properly
                else{
                    return("Error occurred");
                }
            }
            // Remove the last comma and newline
            int lastCommaIndex = jsonBuilder.lastIndexOf(",");
            if (lastCommaIndex != -1) {
                jsonBuilder.deleteCharAt(lastCommaIndex);
            }
        } catch (IOException e) {
            return "Error occurred";
        }
        // Add the last curly brace
        jsonBuilder.append("}\n");
        // Return the built string
        return jsonBuilder.toString();
    }

    // The arrayToJSON function takes a String[] from the stored data and formats it into a JSON String
    // Inputs: A string[] array of latest stored data values
    // Outputs: A string in JSON format
    public static String arrayToJSON(String[] array){
        // Get the length of the array
        Integer arrayLength = array.length;
        // If not the correct length
        if(arrayLength != 17){
            // Return an error message
            System.out.println("Array is not the correct length");
            return "Error occurred";
        }
        // Create an empty StringBuilder
        StringBuilder jsonOutput = new StringBuilder();
        // Append the message in the correct order
        jsonOutput.append("{").append("\n");
        jsonOutput.append("id").append(" : ").append(array[0]).append(",").append("\n");
        jsonOutput.append("name").append(" : ").append(array[1]).append(",").append("\n");
        jsonOutput.append("state").append(" : ").append(array[2]).append(",").append("\n");
        jsonOutput.append("time_zone").append(" : ").append(array[3]).append(",").append("\n");
        jsonOutput.append("lat").append(" : ").append(array[4]).append(",").append("\n");
        jsonOutput.append("lon").append(" : ").append(array[5]).append(",").append("\n");
        jsonOutput.append("local_date_time").append(" : ").append(array[6]).append(",").append("\n");
        jsonOutput.append("local_date_time_full").append(" : ").append(array[7]).append(",").append("\n");
        jsonOutput.append("air_temp").append(" : ").append(array[8]).append(",").append("\n");
        jsonOutput.append("apparent_t").append(" : ").append(array[9]).append(",").append("\n");
        jsonOutput.append("cloud").append(" : ").append(array[10]).append(",").append("\n");
        jsonOutput.append("dewpt").append(" : ").append(array[11]).append(",").append("\n");
        jsonOutput.append("press").append(" : ").append(array[12]).append(",").append("\n");
        jsonOutput.append("rel_hum").append(" : ").append(array[13]).append(",").append("\n");
        jsonOutput.append("wind_dir").append(" : ").append(array[14]).append(",").append("\n");
        jsonOutput.append("wind_spd_kmh").append(" : ").append(array[15]).append(",").append("\n");
        jsonOutput.append("wind_spd_kt").append(" : ").append(array[16]).append("\n");
        jsonOutput.append("}");
        // Convert StringBuilder to String
        String jsonString = jsonOutput.toString();
        // Return the string
        return jsonString;
    }

}
