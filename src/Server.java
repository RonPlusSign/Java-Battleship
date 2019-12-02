import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private static ArrayList<Player> clientsQueue = new ArrayList();
    private static int clientCount = 0;

    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(58901)) {
            System.out.println("Server is Running...");
            ExecutorService pool = Executors.newFixedThreadPool(1);
            while (true) {
                Socket newSocket = listener.accept();
                if(newSocket.isConnected())
                    clientsQueue.add(new Player(newSocket, nextPlayerID()));

                if (clientsQueue.size() >= 2) { //if there are at least 2 players in queue, start a game (checking their connection before starting)
                    try {
                        if (!testConnection(clientsQueue.get(0))) {  //check if the first player in queue still connected
                            clientsQueue.remove(0); //if the player isn't connected, remove it from the queue
                        } else if (!testConnection(clientsQueue.get(1))) {   //check if the first player in queue still connected
                            clientsQueue.remove(1); //if the player isn't connected, remove it from the queue
                        } else {    //both players are still connected
                            System.out.println("Starting the game");
                            Game game = new Game(clientsQueue.remove(0), clientsQueue.remove(0));   //ArrayList.remove(index) method returns the Player object in that position
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
     * */
    public static boolean testConnection(Player player) {
        //to test client connection, we just send a PING command to it.
        //if it answers correctly, client is still connected
        //if there's an Exception thrown, client is disconnected
        //else, there's a connection problem
        boolean isAlive = false;

        try {
            player.output.println("PING Test connessione di " + player.name);
            if(player.input.hasNextLine()){
                if(player.input.nextLine().contains("PONG"))
                    isAlive = true;
            }

        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        System.out.println("Socket isAlive: " + isAlive);
        return isAlive;
    }

    /**
    * @return a String representing the next playerID ('C' + clientCount)
    * */
    private static String nextPlayerID() {
        String nextCode = "C" + String.valueOf(clientCount);
        clientCount++;
        return nextCode;
    }
}
