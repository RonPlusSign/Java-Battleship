import server.ConnectionSyntaxChecker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client class
 */
public class Client {
    private Socket socket;  //socket connected to Server
    private Scanner in; //input from server
    private PrintWriter out;    //write to server

    /**
     * Constructor (also creates the socket connection)
     *
     * @param serverAddress ip address of the Server (format like "192.168.0.1")
     * @throws IOException Thrown when creating the socket or when initializing input & output sources
     */
    public Client(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * The main thread of the client will listen for messages from the server.
     * The first message will be a "WELCOME" message.
     * Then we go into a loop listening for any of the other messages,
     * and handling each message appropriately.
     */
    public void play() {
        Scanner keyboard = new Scanner(new InputStreamReader(System.in));

        new Thread(() -> {  //read from Server input on another thread
            while (true) {
                String message = in.nextLine();
                if (!message.equals("PING"))
                    System.out.println(message);  //print server's messages to console
            }
        }).start();

        while (true) {  //send keyboard input to the server
            out.println(keyboard.nextLine());
        }
    }

    public static void main(String[] args) throws Exception {
        Client client;
        String serverAddress = "127.0.0.1"; //default server address (localhost)
        int port = 1337;    //default port

        if (args.length == 2
                && args[0] != null
                && args[1] != null
                && ConnectionSyntaxChecker.validIP(args[0])
                && ConnectionSyntaxChecker.validPort(args[1])) {
            serverAddress = args[0];
            port = Integer.parseInt(args[1]);
        } else if (args.length == 1){
            if(ConnectionSyntaxChecker.validIP(args[0]))
                serverAddress = args[0];
            else if (ConnectionSyntaxChecker.validPort(args[0]))
                port = Integer.parseInt(args[0]);
        }

        client = new Client(serverAddress, port);
        client.play();
    }

}