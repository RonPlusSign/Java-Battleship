import java.io.IOException;

class Game implements Runnable {
    private Player currentPlayer, opponent;
    private final int maxGrid = 21;
    private SyntaxChecker syntaxChecker;

    public Game(Player currentPlayer, Player opponent) {
        this.currentPlayer = currentPlayer;
        this.opponent = opponent;

        this.currentPlayer.setOpponent(opponent);
        this.opponent.setOpponent(currentPlayer);

        this.syntaxChecker = new SyntaxChecker(maxGrid);
    }

    @Override
    public void run() {

        try {
            if (!clientsConnected()) {
                System.out.println("ERROR Connection error");
            } else manageGame();   //main function
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {   //when game finishes
            currentPlayer.getSocket().close();
        } catch (IOException e) {
        }
        try {
            opponent.getSocket().close();
        } catch (IOException e) {
        }
        return;
    }

    /**
     * Function handling the game logic
     * See README.md for more info about the protocol
     * @param event command from the user
     */

    private void elaborateUserMove(String event) throws Exception {

        if(event.startsWith("DELETE")){
            //If the message has the correct Format
            syntaxChecker.checkCorrectMessageFormat("DELETE", event);

            int x= Integer.parseInt(String.valueOf(event.charAt(7)).concat(String.valueOf(event.charAt(8))));
            int y= Integer.parseInt(String.valueOf(event.charAt(9)).concat(String.valueOf(event.charAt(10))));

            syntaxChecker.checkCorrectMessage(x, y);
            currentPlayer.delete(x,y);
        }
        //FIRE action
        if (event.startsWith("FIRE")){
            //If the message has the correct Format
            syntaxChecker.checkCorrectMessageFormat("FIRE", event);

            int x= Integer.parseInt(String.valueOf(event.charAt(5)).concat(String.valueOf(event.charAt(6))));
            int y= Integer.parseInt(String.valueOf(event.charAt(7)).concat(String.valueOf(event.charAt(8))));

            syntaxChecker.checkCorrectMessage(x, y);
            opponent.fire(x, y);
        }
        //SET action
        else if (event.startsWith("SET")){
            //If the message has the correct Format
            syntaxChecker.checkCorrectMessageFormat("SET", event);

            int x= Integer.parseInt(String.valueOf(event.charAt(4)).concat(String.valueOf(event.charAt(5))));
            int y= Integer.parseInt(String.valueOf(event.charAt(6)).concat(String.valueOf(event.charAt(7))));
            int length= Integer.parseInt(String.valueOf(event.charAt(8)));
            char orientation = event.charAt(9);

            syntaxChecker.checkCorrectMessage(x, y, length, orientation);
            currentPlayer.set(x, y, length, orientation);
        }
        //Neither "DELETE nor ""FIRE" nor "SET"
        else {
            throw new IllegalArgumentException("ERROR Command not valid");
        }

        System.out.println("EVENT: " + event);
    }

    private void manageGame() {
        while (clientsConnected()) {
            try {
                //Loops until the move is valid
                while (true) {

                    // Is your turn
                    currentPlayer.getOutput().println("TURN");

                    while (!currentPlayer.getInput().hasNextLine() && clientsConnected()) {}

                    String command = currentPlayer.getInput().nextLine();
                    System.out.println("Command received ---> " + currentPlayer.getName() + ": " + command);

                    //If the move is valid then continue, otherwise repeat the command request
                    try {
                        elaborateUserMove(command);
                        currentPlayer.getOutput().println("OK " + command);
                        break;
                    } catch (Exception e) {

                        currentPlayer.getOutput().println(e.getMessage() + " (Type another command)");

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
                    System.out.println("ERROR Connection error");
                    break;
                }
            }
        }
    }

    /**
     * Checks periodically that the two players are still in the game
     * @return The status of the connection (TRUE = Ok)
     */
    private boolean clientsConnected() {
        if (!Server.testConnection(currentPlayer)) {

            opponent.getOutput().println("WIN Opponent left the game.");

            try {
                opponent.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            System.out.println(currentPlayer.getName() + " disconnected");
            return false;

        } else if (!Server.testConnection(opponent)) {

            currentPlayer.getOutput().println("WIN Opponent left the game");

            try {
                currentPlayer.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(opponent.getName() + " disconnected");
            return false;
        }

        System.out.println("OK Both players are still in the game");
        return true;
    }
}