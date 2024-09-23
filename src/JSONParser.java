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

    // public static HashMap<String, String> readJsonFile(String filePath) {
    //     HashMap<String, String> dataMap = new HashMap<>();
    //     try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
    //         StringBuilder jsonContent = new StringBuilder();
    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             jsonContent.append(line);
    //         }
    //         String jsonString = jsonContent.toString().trim();
    //         jsonString = jsonString.substring(1, jsonString.length() - 1); // Remove curly braces

    //         String[] keyValuePairs = jsonString.split(",");

    //         for (String pair : keyValuePairs) {
    //             String[] entry = pair.split(":");
    //             String key = entry[0].trim().replaceAll("\"", "");
    //             String value = entry[1].trim().replaceAll("\"", "");
    //             dataMap.put(key, value);
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //     return dataMap;
    // }
}
