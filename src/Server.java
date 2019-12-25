import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    //Array containing Players connected to this server
    private static ArrayList<Player> clientsQueue = new ArrayList<>();
    //Incremental ID representing each client connected
    private static int clientCount = 0;
    //Grid length (the Grid is square shaped)
    protected static final int GRID_LENGTH = 10;
    //Number of Game instances that can be managed
    private static final int MAX_GAMES_NUMBER = 10;  //number of same games that can be managed

    /**
     * Main Server function that manages Clients queue and starts the Games
     */
    public static void main(String[] args) {
        //Socket opening on port xxxx
        try (ServerSocket listener = new ServerSocket(1337)) {
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
                            System.out.println("Starting a game");
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
     * @param player is the Player whose connection has to be tested
     * @return true if client is still connected, false otherwise
     */
    public static boolean testConnection(Player player) {
        //to test client connection, we just send a PING command to it.
        //if it answers correctly, client is still connected
        //if there's an Exception thrown, client is disconnected
        //else, there's a connection problem
        boolean isAlive = false;

        try {
            player.send("{ \"cmd\" : \"PING\"" +
                    ", \"msg\" : \"Testing the connection of player " + player.getName() + "\"}");
            player.getOutput().flush();

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
}

