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
                System.out.println(in.nextLine());  //print server's messages to console
            }
        }).start();

        while (true) {  //send keyboard input to the server
            out.println(keyboard.nextLine());
        }
    }

    /**
     * Function that checks if the argument is a valid ip (format: 127.0.0.1)
     *
     * @param ip the string that should contain the ip address
     * @return true if the argument is a valid ip address, false otherwise
     */
    public static boolean validIP(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            //divide the ip in parts
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            //check if each part has a valid value (from 0 to 255)
            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }

            //lastly, check if the ip string doesn't end with a dot. ( "127.0.0.1." isn't a valid ip address)
            return !ip.endsWith(".");

        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Function that checks if the argument is a valid port (from 1 to 65535)
     *
     * @param port the string that should contain the port value
     * @return true if the argument is a valid port, false otherwise
     */
    public static boolean validPort(String port) {
        try {
            if (port == null || port.isEmpty()) return false;

            int i = Integer.parseInt(port); //try to convert from String to int
            return (i >= 1) && (i <= 65535);    //check if the number is a valid port
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        Client client;

        if (args.length == 2
                && args[0] != null
                && args[1] != null
                && validIP(args[0])
                && validPort(args[1])) {
            client = new Client(args[0], Integer.parseInt(args[1]));
            client.play();

        } else {
            client = new Client("127.0.0.1", 1337);  //default address (localhost) and port
            client.play();
        }
    }
}