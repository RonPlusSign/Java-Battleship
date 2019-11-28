import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(58901)) {
            System.out.println("Server is Running...");
            var pool = Executors.newFixedThreadPool(2);
            while (true) {
                Game game = new Game();
                pool.execute(game.new Player(listener.accept(), 'X'));
                pool.execute(game.new Player(listener.accept(), 'O'));
            }
        }
    }
}

class Game {
    Player currentPlayer;

    /**
     * A Player is identified by a character mark which is either 'X' or 'O'.
     * For communication with the client the player has a socket and associated
     * Scanner and PrintWriter.
     */
    class Player implements Runnable {
        char mark;
        Player opponent;
        Socket socket;
        Scanner input;
        PrintWriter output;

        public Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
        }

        @Override
        public void run() {
            try {
                setup();
                System.out.println("New Client connected: " + mark);
                processCommands();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (opponent != null && opponent.output != null) {
                    opponent.output.println("OTHER_PLAYER_LEFT");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private void setup() throws IOException {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + mark);
            if (mark == 'X') {  //X because it's the first symbol to be assigned (= first player)
                currentPlayer = this;
                output.println("MESSAGE Waiting for opponent to connect");
            } else {
                opponent = currentPlayer;
                opponent.opponent = this;
                opponent.output.println("MESSAGE Your move");
            }
        }

        private void processCommands() {
            while (true) {
                while(input.hasNextLine()){
                    String command = input.nextLine();
                    System.out.print("Command received ---> ");
                    System.out.println(mark + ": " + command);
                    //TODO: elaborate client input

                    elaborateUserMove(command, this);

                    output.println("You made your move: " + command);
                    opponent.output.println("Your opponent made his move: " + command);

                    //swap players and activate opponent turns
                    Player temp = currentPlayer;
                    currentPlayer = opponent;
                    opponent = temp;

                    currentPlayer.output.println("MESSAGE Your move");
                }
            }
        }

        public synchronized void elaborateUserMove(String event, Player player) {
            if (player != currentPlayer) {
                throw new IllegalStateException("Not your turn");
            } else if (player.opponent == null) {
                throw new IllegalStateException("You don't have an opponent yet");
            } else {
                //TODO: add here game logic
                // interpret event, check table, update table, send result to users...
                System.out.println("EVENT: " + event);
            }
        }
    }
}