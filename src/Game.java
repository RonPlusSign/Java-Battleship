import java.io.IOException;

class Game implements Runnable {
    Player currentPlayer, opponent;

    public Game(Player currentPlayer, Player opponent) {
        this.currentPlayer = currentPlayer;
        this.opponent = opponent;

        this.currentPlayer.opponent = opponent;
        this.opponent.opponent = currentPlayer;
    }

    @Override
    public void run() {
        try {
//            currentPlayer.opponent = opponent;
//            opponent.opponent = currentPlayer;

            if (!clientsConnected()){
                System.out.println("[ERROR] Connection error");
            }
            else manageGame();   //main function
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {   //when game finishes
            currentPlayer.socket.close();
            opponent.socket.close();
        } catch (IOException e) {
        }
    }

    public synchronized void elaborateUserMove(String event, Player player) {
        //useless controls?
        /*if (player != currentPlayer) {
            throw new IllegalStateException("Not your turn");
        } else if (player.opponent == null) {
            throw new IllegalStateException("You don't have an opponent yet");
        } else {*/
            //TODO: add here game logic
            // interpret event, check table, update table, send result to users...
            System.out.println("EVENT: " + event);
//        }
    }

    private void manageGame() {
        while (true) {
            try {
                currentPlayer.output.println("MESSAGE Your move");
                if (currentPlayer.input.hasNextLine()) {
                    String command = currentPlayer.input.nextLine();
                    System.out.print("Command received ---> ");
                    System.out.println(currentPlayer.mark + ": " + command);

                    //TODO: elaborate client input
                    elaborateUserMove(command, currentPlayer);

                    currentPlayer.output.println("You made your move: " + command);
                    opponent.output.println("Your opponent made his move: " + command);

                    //swap players and activate opponent turns
                    Player temp = currentPlayer;
                    currentPlayer = opponent;
                    opponent = temp;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (!clientsConnected()){
                    System.out.println("[ERROR] Connection error");
                }
            }
        }
    }

    private boolean clientsConnected() {
        if (!currentPlayer.socket.isConnected()) {
            opponent.output.println("Opponent left.");
            System.out.println("Client " + currentPlayer.mark + " left the game.");
            try {
                opponent.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[ERROR] Player 1 disconnected.");
            return false;
        } else if (!opponent.socket.isConnected()) {
            currentPlayer.output.println("Opponent left.");
            System.out.println("Client " + opponent.mark + " left the game.");
            try {
                currentPlayer.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[ERROR] Player 2 disconnected.");
            return false;
        }
        System.out.println("[MESSAGE] Players connections are OK.");
        return true;
    }
}