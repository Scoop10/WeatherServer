import java.io.*;

public class JSONParser {
    public static String convertTextToJson(String filePath) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] keyValue = line.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    jsonBuilder.append("  \"").append(key).append("\": \"").append(value).append("\",\n");
                }
            }
            // Remove the last comma and newline
            int lastCommaIndex = jsonBuilder.lastIndexOf(",");
            if (lastCommaIndex != -1) {
                jsonBuilder.deleteCharAt(lastCommaIndex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        jsonBuilder.append("}\n");
        return jsonBuilder.toString();
    }

    public static String arrayToJSON(String[] array){
        Integer arrayLength = array.length;
        if(arrayLength != 17){
            System.out.println("Array is not the correct length");
            return "Error occurred";
        }
        StringBuilder jsonOutput = new StringBuilder();
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
        String jsonString = jsonOutput.toString();
        return jsonString;
    }

}
