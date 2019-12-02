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
            if (!clientsConnected()) {
                System.out.println("[ERROR] Connection error");
            } else manageGame();   //main function
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {   //when game finishes
            currentPlayer.socket.close();
        } catch (IOException e) {}
        try{
            opponent.socket.close();
        } catch (IOException e){}
        return;
    }

    public void elaborateUserMove(String event, Player player) {
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
        return 0;
    }

    private void manageGame() {
        while (true) {
            try {

                //Loops until the move isn't invalid
                while (true){

                    currentPlayer.output.println("MESSAGE Your move");

                    if (currentPlayer.input.hasNextLine()) {
                        String command = currentPlayer.input.nextLine();
                        System.out.print("Command received ---> ");
                        System.out.println(currentPlayer.mark + ": " + command);

                        //TODO: elaborate client input
                        //If the move is not invalid then continue, otherwise repeat the command request
                        if (!(elaborateUserMove(command, currentPlayer) == -1)) {
                            currentPlayer.output.println("You made your move: " + command);
                            opponent.output.println("Your opponent made his move: " + command);
                            break;
                        }
                    }
                }
                //swap players and activate opponent turns
                Player temp = currentPlayer;
                currentPlayer = opponent;
                opponent = temp;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (!clientsConnected()) {
                    System.out.println("[ERROR] Connection error");
                }
            }
        }
    }

    private boolean clientsConnected() {
        if (!Server.testConnection(currentPlayer)) {
            opponent.output.println("WIN Opponent left the game.");
            System.out.println("Client " + currentPlayer.name + " left the game.");
            try {
                opponent.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[ERROR] Player 1 disconnected.");
            return false;
        } else if (!Server.testConnection(opponent)) {
            currentPlayer.output.println("WIN Opponent left the game.");
            System.out.println("Client " + opponent.name + " left the game.");
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