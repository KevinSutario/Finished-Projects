import java.io.*;
import java.net.*;
import java.util.*;

public class MyServer {
    private static ArrayList<DataOutputStream> outputStreams = new ArrayList<>();
    private static File messages;

    //this is for saving messages on a local file

    public static void main(String[] args) {
        try {
            // opening the messages file
            messages = new File("MessagesCSC1004Assignment1.txt");
            // making the file if it is not created yet
            if (!messages.exists()){
                messages.createNewFile();
            }

            //connect the socket to port 6666
            ServerSocket serverSocket = new ServerSocket(6666);
            System.out.println("Server started. Waiting for clients...");

            //connecting clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                outputStreams.add(outputStream);

                // multithreading
                Thread t = new Thread(() -> handleClient(clientSocket));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

            // Get the username from the client
            String username = inputStream.readUTF();
            System.out.println("Username received from client side: " + username);

            // Announcement when a new client has joined
            broadcastMessage("Server: " + username + " has joined the chat");

            // count to keep track the number of messages sent
            int count = 0;

            while (true) {
                String message = inputStream.readUTF();

                // checking if message is announcement from server because a problem was encountered
                //if not using this method, an infinite loop in the file side.
                if (!message.startsWith("Server:")) {
                    count++;
                    // sending message from one client to the rest
                    String messageToSend = username + ": " + message + " (messages sent = " + count + ")";
                    broadcastMessage(messageToSend);

                    //recording the messages in the file
                    messageToFile("MessagesCSC1004Assignment1.txt", messageToSend);
                }
            }
        } catch (IOException e) {

            //telling in server side when a client disconnected
            System.out.println("Client disconnected: " + clientSocket);
            outputStreams.removeIf(outputStream -> {
                try {
                    return outputStream == null || outputStream.equals(clientSocket.getOutputStream());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return false;
            });
        }
    }


    //method for sending messages in the group chat from one client to the rest
    private static void broadcastMessage(String message) {
        for (DataOutputStream outputStream : outputStreams) {
            try {
                outputStream.writeUTF(message);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // this method is to write the messages to the file
    private static void messageToFile(String fileName, String message) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
