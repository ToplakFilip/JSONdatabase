package client;

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Main {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 56789;

    public static void main(String[] args) {

        try (
                Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {

            Args commander = new Args();
            System.out.println("[ Client started ]");
            JCommander.newBuilder()
                    .addObject(commander)
                    .build()
                    .parse(args);

            String request = commander.request;
            String key = commander.key;
            String entry = commander.value;
            String fileName = commander.jsonFileName;
            Gson gson = new Gson();
            Map<String, Object> tempMap = new HashMap<>();
            if (!(fileName == null)) {
                String filepath = "src/main/java/client/data/" + fileName;
                BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath));
                JsonObject jsonObject = gson.fromJson(bufferedReader, JsonObject.class);
                String jsonString = jsonObject.toString();


                System.out.println("Sent: " + jsonString);
                jsonString = addingBracketsToKey(jsonString);

                output.writeUTF(jsonString);
            } else {
                switch (request) {
                    case "get":
                    case "delete":
                        tempMap.put("type", request);
                        tempMap.put("key", key);
                        String jsonString = gson.toJson(tempMap);
                        System.out.println("Sent: " + jsonString);
                        jsonString = addingBracketsToKey(jsonString);
                        output.writeUTF(jsonString);
                        break;
                    case "set":
                        tempMap.put("type", request);
                        tempMap.put("key", key);
                        tempMap.put("value", entry);
                        jsonString = gson.toJson(tempMap);
                        System.out.println("Sent: " + jsonString);
                        jsonString = addingBracketsToKey(jsonString);
                        output.writeUTF(jsonString);
                        break;
                    case "exit":
                        System.out.println("Sent: " + request);
                        output.writeUTF(request);
                        break;
                }
            }

            String receivedMessage = input.readUTF();
            System.out.println("Received: " + receivedMessage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String addingBracketsToKey(String string) {
        int lastIndex = string.indexOf("\"key\":") + 6;
        int firstIndex = string.indexOf(",\"value\"");
        if (firstIndex > lastIndex) {
            if (string.charAt(lastIndex) != '[') {
                int quoteNumber = 0;
                for (int i = lastIndex; i < string.length(); i++) {
                    if (string.charAt(i) == '"' && quoteNumber == 0) {
                        string = string.substring(0, i - 1) + ":[" + string.substring(i);
                        quoteNumber++;
                        i = i + 2;
                        string = string.substring(0, firstIndex + 1) + "]," + string.substring(firstIndex + 2);
                    }
                }
            }
        } else {
            firstIndex = string.lastIndexOf("}");
            if (string.charAt(lastIndex) != '[') {
                int quoteNumber = 0;
                for (int i = lastIndex; i < string.length(); i++) {
                    if (string.charAt(i) == '"' && quoteNumber == 0) {
                        string = string.substring(0, i - 1) + ":[" + string.substring(i);
                        quoteNumber++;
                        i = i + 2;
                        string = string.substring(0, firstIndex + 1) + "]}" + string.substring(firstIndex + 2);
                    }
                }
            }
        }
        return string;
    }
}
