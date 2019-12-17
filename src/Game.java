import java.io.IOException;

/**
 * Class that manages a game between 2 Players
 */
class Game implements Runnable {
    private Player currentPlayer, opponent; //players
    private final int maxGrid = 15; //grid length
    private SyntaxChecker syntaxChecker;

    /**
     * Constructor
     *
     * @param currentPlayer first player
     * @param opponent      second player
     */
    public Game(Player currentPlayer, Player opponent) {
        this.currentPlayer = currentPlayer;
        this.opponent = opponent;

        this.currentPlayer.setOpponent(opponent);
        this.opponent.setOpponent(currentPlayer);

        this.syntaxChecker = new SyntaxChecker();
    }

    /**
     * Main function that runs all the main functions in order
     */
    @Override
    public void run() {
        try {
            if (!clientsConnected()) {
                System.out.println("ERROR 05 Connection error");
            } else {
                while (!(currentPlayer.isGridReady() && opponent.isGridReady())) {
                }    //wait for clients to set their grid layout

                currentPlayer.getOutput().println("PLAY");
                opponent.getOutput().println("PLAY");
                manageGame();   //main function
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {   //when game finishes
            currentPlayer.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            opponent.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function that handles the event logic
     * See README.md for more info about the protocol
     *
     * @param event command from the user
     */
    private void elaborateUserMove(String event) throws IllegalArgumentException {

        //FIRE action
        if (event.startsWith("FIRE")) {
            //If the message has the correct Format
            syntaxChecker.checkCorrectMessageFormat("FIRE", event);

            int x = Integer.parseInt(String.valueOf(event.charAt(5)).concat(String.valueOf(event.charAt(6))));
            int y = Integer.parseInt(String.valueOf(event.charAt(7)).concat(String.valueOf(event.charAt(8))));

            syntaxChecker.checkCorrectMessage(x, y);
            opponent.fire(x, y);
        }
        //Neither "DELETE" nor "FIRE"
        else {
            throw new IllegalArgumentException("ERROR 900 Unknown command");
        }

        System.out.println("EVENT: " + event);
    }

    /**
     * Function that handles the game turnament logic
     */
    private void manageGame() {
        while (clientsConnected()) {
            try {
                //Loops until the move is valid
                while (true) {
                    // Is your turn
                    currentPlayer.getOutput().println("{\"msg\" : \"TURN\"}");

                    try {
                        String command = currentPlayer.getInput().nextLine();
                        System.out.println("Command received ---> " + currentPlayer.getName() + ": " + command);

                        elaborateUserMove(command);
                        currentPlayer.getOutput().println("OK " + command);

                        //If the move is valid then exit the loop, otherwise repeat the command request
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
                    System.out.println("ERROR 5 Connection error");
                    break;
                }
            }
        }
    }

    /**
     * Checks periodically that the two players are still in the game
     *
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