import java.io.IOException;
import java.net.UnknownHostException;
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
    private String username;    //stored for gui purposes (it might be showed to the user)

    /**
     * Constructor (also creates the socket connection)
     *
     * @param serverAddress ip address of the Server (format like "192.168.0.1")
     * @throws IOException Thrown when creating the socket or when initializing input & output sources
     */
    public Client(String serverAddress) throws IOException {
        socket = new Socket(serverAddress, 58901);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);

    }

    /**
     * The main thread of the client will listen for messages from the server.
     * The first message will be a "WELCOME" message.
     * Then we go into a loop listening for any of the other messages,
     * and handling each message appropriately.
     */
    public void play() throws Exception {   //manage here the commands
        System.out.println("PLAY");

        while (true) {
            String message = "";
            try {
                while (true) {  //manage messages
                    if (in.hasNextLine()) {
                        message = in.nextLine();
                        System.out.println(message);

                        if (message.startsWith("GRID")) {
                            System.out.println("Set your ships");
                            //TODO: implement here GUI ships positioning using number of boats
                            //GRID command is followed by the number of boats. See README.md for more info

                            setShips();

                        }
                    } else if (message.startsWith("WIN")) {
                        System.out.println("YOU WIN!");
                        //instead of closing connection, we could ask the user if he wants to play again
                        //if yes, we could add the client to the server queue (using the Game class)
                        socket.close();
                    } else if (message.startsWith("LOST")) {
                        //instead of closing connection, we could ask the user if he wants to play again
                        //if yes, we could add the client to the server queue (using the Game class)
                        System.out.println("YOU LOST :(");
                        socket.close();
                    } else if (message.startsWith("TURN")) {
                        try {
                            Scanner inputTastiera = new Scanner(System.in);
                            message = inputTastiera.nextLine();
                            out.println(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Connection to server lost.");
                socket.close();
                e.printStackTrace();
            }
        }
    }

    /**
     * Function that manages the set of all the ships based on the Server messages
     */
    private void setShips() {
        boolean boatsFinished = false;
        String message;
        Scanner inputTastiera = new Scanner(System.in);

        while (!boatsFinished) {
            try {
                message = inputTastiera.nextLine();
                out.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                while (!in.hasNextLine()) {
                }    //wait for server answer

                message = in.nextLine();

                System.out.println("Message from server: " + message);

                if (message.startsWith("OK")) {
                    //System.out.println(message);
                    continue;
                } else if (message.startsWith("READY")) {
                    boatsFinished = true;
                } else if (message.startsWith("ERROR 1")) {
                    System.out.println("Invalid boat position");
                } else if (message.startsWith("ERROR 2")) {
                    System.out.println("Selected boat size not available");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Finished ship set");
        waitForOpponent();
    }

    /**
     * Function that loops until the Server says that the game attacks can start (using PLAY command)
     */
    private void waitForOpponent() {
        while (true) {
            while (!in.hasNextLine()) {
            }    //wait for server input

            String message = in.nextLine();

            System.out.println("Message from server: " + message);

            if (message.startsWith("PLAY")) break;
            else System.out.println(message);
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client("127.0.0.1");
        System.out.println("Client created");

        client.play();
    }
}