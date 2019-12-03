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
        } catch (IOException e) {
        }
        try {
            opponent.socket.close();
        } catch (IOException e) {
        }
        return;
    }

    /**
     * Function handling the game logic
     * See README.md for more info about the protocol
     * @param event command from the user
     */

    private void elaborateUserMove(String event) throws Exception{
        if (event.startsWith("FIRE")){
            if (String.valueOf(event.charAt(4)) == " " && event.length() == 9){
                int x= Integer.parseInt(String.valueOf(event.charAt(5)+event.charAt(6)));
                int y= Integer.parseInt(String.valueOf(event.charAt(7)+event.charAt(8)));
                currentPlayer.fire(x, y);
            } else throw new Exception();
        }
        else if (event.startsWith("SET")){
            if (String.valueOf(event.charAt(3)) == " " && event.length() == 8){
                int x= Integer.parseInt(String.valueOf(event.charAt(4)+event.charAt(5)));
                int y= Integer.parseInt(String.valueOf(event.charAt(6)+event.charAt(7)));
                currentPlayer.set(x, y);
            } else throw new Exception();
        }
        else {
            throw new Exception();
        }

        System.out.println("EVENT: " + event);
    }

    private void manageGame() {
        while (clientsConnected()) {
            try {
                //Loops until the move is valid
                while (true) {

                    currentPlayer.output.println("MESSAGE Your move");

                    while (!currentPlayer.input.hasNextLine() && clientsConnected()) {}

                    String command = currentPlayer.input.nextLine();
                    System.out.print("Command received ---> ");
                    System.out.println(currentPlayer.name + ": " + command);

                    //If the move is valid then continue, otherwise repeat the command request
                    try {
                        elaborateUserMove(command);
                        currentPlayer.output.println("You made your move: " + command);
                        opponent.output.println("Your opponent made his move: " + command);
                        break;
                    } catch (Exception e) {
                        currentPlayer.output.println("The move wasn't valid. You have to type another command...");
                        opponent.output.println("The move wasn't valid. Your opponent is typing another command...");
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
                    break;
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