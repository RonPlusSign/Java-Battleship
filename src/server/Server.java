package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    //Array containing Players connected to this server
    private static ArrayList<Player> clientsQueue = new ArrayList<>();
    //Incremental ID representing each client connected
    private static int clientCount = 0, //number of clients connected since the Server has been created
            port = 1337,    //default port that the server uses
            gridLength = 10;      //default grid length (the Grid is square shaped)
    private static final int MAX_GAMES_NUMBER = 10;  //number of same games that can be managed

    /**
     * Main Server function that manages Clients queue and starts the Games
     */
    public static void main(String[] args) {
        checkArgs(args);

        //Socket opening on port xxxx
        try (ServerSocket listener = new ServerSocket(port)) {
            System.out.println("Server is Running...");
            ExecutorService pool = Executors.newFixedThreadPool(MAX_GAMES_NUMBER);
            while (true) {
                //New socket creation
                Socket newSocket = listener.accept();
                if (newSocket.isConnected()) {
                    clientsQueue.add(new Player(newSocket, nextPlayerID()));
                }
                //If there are at least 2 players in queue, start a game (checking their connection before starting)
                if (clientsQueue.size() >= 2) {
                    try {
                        //Check if the first player in queue still connected
                        if (!testConnection(clientsQueue.get(0))) {
                            //if the player isn't connected, remove it from the queue
                            clientsQueue.remove(0);
                        }
                        //Check if the second player in queue still connected
                        else if (!testConnection(clientsQueue.get(1))) {
                            clientsQueue.remove(1);
                        }
                        //Both players are still connected --> a new Game instance is created
                        else {
                            System.out.println("Starting a new game...");
                            //ArrayList.remove(index) method returns the Player object in that position
                            Game game = new Game(clientsQueue.remove(0), clientsQueue.remove(0));
                            pool.execute(game);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function that manages the arguments values
     * Possible args are:
     *  -p <portNumber>
     *  -l <gridLength>
     *  -p <portNumber> -l <gridLength>
     *  -l <gridLength> -p <portNumber>
     * @param args main args
     */
    private static void checkArgs(String[] args) {
        try {
            if (args.length == 2 && args[0] != null && args[1] != null) {
                if (args[0].equals("-p") //change default port
                        && ConnectionSyntaxChecker.validPort(args[1])) {
                    port = Integer.parseInt(args[1]);
                } else if (args[1].equals("-l")    //change grid length
                        && (Integer.parseInt(args[1]) > 0
                        && Integer.parseInt(args[1]) < 100)) { //max grid value is 100
                    gridLength = Integer.parseInt(args[1]);
                }
            } else if (args.length == 4 && args[0] != null && args[1] != null && args[2] != null && args[3] != null) {
                if (args[0].equals("-p")
                        && ConnectionSyntaxChecker.validPort(args[1])
                        && args[2].equals("-l")
                        && (Integer.parseInt(args[3]) > 0
                        && Integer.parseInt(args[3]) < 100)) {   //max grid value is 100
                    port = Integer.parseInt(args[1]);
                    gridLength = Integer.parseInt(args[3]);
                } else if (args[0].equals("-l")
                        && (Integer.parseInt(args[1]) > 0
                        && Integer.parseInt(args[1]) < 100)  //max grid value is 100
                        && args[2].equals("-p")
                        && ConnectionSyntaxChecker.validPort(args[3])) {
                    port = Integer.parseInt(args[1]);
                    gridLength = Integer.parseInt(args[3]);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param player is the Player whose connection has to be tested
     * @return true if client is still connected, false otherwise
     */
    public synchronized static boolean testConnection(Player player) {
        //to test client connection, we just send a PING command to it.
        //if the message is sent correctly, client is still connected
        //if there's an Exception thrown, client is disconnected
        //else, there's a connection problem
        boolean isAlive = false;

        try {
            //player.send("{ \"cmd\" : \"PING\"" + ", \"msg\" : \"Testing the connection of player " + player.getName() + "\"}");
            player.send("PING");

            isAlive = true;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }

        return isAlive;
    }

    /**
     * @return a String representing the next playerID ('C' + clientCount)
     */
    private static String nextPlayerID() {
        String nextCode = "C" + clientCount;
        clientCount++;
        return nextCode;
    }

    public static int getGridLength() {
        return gridLength;
    }
}

