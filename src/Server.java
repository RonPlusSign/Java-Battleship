import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(58901)) {
            System.out.println("Server is Running...");
            ExecutorService pool = Executors.newFixedThreadPool(1);
//            while (true) {
            System.out.println("Waiting for player 1");
            Player player1 = new Player(listener.accept(), 'X');

            System.out.println("Waiting for player 2");
            Player player2 = new Player(listener.accept(), 'O');

            if (player1.socket.isConnected() && player2.socket.isConnected()) {
                System.out.println("Starting the game");
                Game game = new Game(player1, player2);
                pool.execute(game);
            } else {
                System.out.println("One of the players left before the match started.");
                try {
                    player1.socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    player2.socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

