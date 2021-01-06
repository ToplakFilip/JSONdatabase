package server;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

import com.google.gson.Gson;

public class Main {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 56789;
    private static final Database database = new Database();

    public static void main(String[] args) throws IOException {

        System.out.println("Server started!");
        ExecutorService executor = Executors.newFixedThreadPool(100);

        while (true) {
            Socket s;
            try (ServerSocket server = new ServerSocket(SERVER_PORT, 50, InetAddress.getByName(SERVER_ADDRESS))) {
                s = server.accept();
                DataInputStream input = new DataInputStream(s.getInputStream());
                DataOutputStream output = new DataOutputStream(s.getOutputStream());

                Future<String> futureString = executor.submit(new Task(input, output, database));
                String outputString = futureString.get();

                if (outputString.equals("stop")) {
                    executor.shutdown();
                    break;
                }
            } catch (IOException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Task implements Callable<String> {

    // private final StringCommand commands;
    final DataOutputStream dos;
    final DataInputStream inp;
    final Database database;
    final Gson gson = new Gson();

    public Task(DataInputStream inp, DataOutputStream dos, Database database) {
        this.inp = inp;
        this.dos = dos;
        this.database = database;

    }

    public String call() {
        try {

            String receivedMessage = inp.readUTF(); // READING MESSAGE
            if (receivedMessage.equals("exit")) {
                dos.writeUTF("{\"response\":\"OK\"}");
                return "stop";
            }
            // CONVERTING MESSAGE INTO COMMANDS
            StringCommand commands = gson.fromJson(receivedMessage, StringCommand.class);
            String sendMessage = "";
            String request = commands.getType();
            ArrayList<String> key = commands.getKey();
            switch (request) {
                case "get":
                    sendMessage = database.get(key);
                    break;
                case "delete":
                    sendMessage = database.delete(key);
                    break;
                case "set":
                    sendMessage = database.set(key, commands.getValue());
                    break;
            }
            dos.writeUTF(sendMessage); // SENDING MESSAGE
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        try {
            this.dos.close();
            this.inp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
