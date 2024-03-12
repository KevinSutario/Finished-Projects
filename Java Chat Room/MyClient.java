import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 6666);
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = br.readLine();
            // give username to server
            dout.writeUTF(username);
            dout.flush();

            // thread for listening messeges from the server
            new Thread(() -> {
                try (DataInputStream din = new DataInputStream(socket.getInputStream())) {
                    while (true) {
                        String message = din.readUTF();
                        //getting the current time
                        String timeSent = new SimpleDateFormat("HH:mm").format(new Date());
                        // Check if the message received is not the same as the one sent by the client
                        if (!message.startsWith(username + ":")) {
                            System.out.println(message + " (sent on " + timeSent + ")");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


            //waiting for user to type messsages then sending the messages to the server to be sent to other clients
            String message;
            while (true) {
                message = br.readLine();
                dout.writeUTF(message);
                dout.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
