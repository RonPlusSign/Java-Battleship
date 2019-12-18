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

                currentPlayer.getOutput().println("{\"cmd\" : \"PLAY\"}");
                opponent.getOutput().println("{\"cmd\" : \"PLAY\"}");
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
     * Function that handles the game turnament logic
     * See README.md for more info about the protocol
     */
    private void manageGame() {
        while (clientsConnected()) {
            try {
                //Loops until the move is valid
                while (true) {
                    // Is your turn: TURN true
                    currentPlayer.getOutput().println("{\"cmd\" : \"TURN\", " +
                            "\"msg\" : true }");
                    // Is not your turn: TURN false
                    opponent.getOutput().println("{\"cmd\" : \"TURN\", " +
                            "\"msg\" : false }");

                    try {
                        String command = currentPlayer.getInput().next();
                        System.out.println("Command received ---> " + currentPlayer.getName() + ": " + command);

                        //during this part of the game, Client can only request for FIRE
                        if (command.startsWith("FIRE")) {
                            //If the message has the correct Format
                            syntaxChecker.checkCorrectMessageFormat("FIRE", command); //TODO: add possibility to send ERROR <code> to Client every time an Exception could be thrown

                            int x = Integer.parseInt(String.valueOf(command.charAt(5)).concat(String.valueOf(command.charAt(6))));
                            int y = Integer.parseInt(String.valueOf(command.charAt(7)).concat(String.valueOf(command.charAt(8))));

                            syntaxChecker.checkCorrectMessage(x, y);

                            opponent.fire(x, y);
                            if (!opponent.fire(x, y)) {  //fire returns true if a boat is hit. If it's hit, the Client must fire again. Otherwise we swap the turn
                                //if miss, exit the loop and swap the players. Otherwise the player has to fire again
                                break;
                            } else { //player.fire(...) already checks if the ship is SUNK and warns the Clients


                            }
                        }
                        //If the event wasn't FIRE
                        else {  //If the move is valid then exit the loop, otherwise an exception is thrown and Client must send a new FIRE request
                            throw new IllegalArgumentException("ERROR Invalid command");    //TODO: might add a new type of error to manage this (Client sent a request that isn't a FIRE)
                        }

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

            opponent.getOutput().println("{\"cmd\" : \"WON\"," +
                    "\"msg\": \"Your opponent left the game. \" }");

            try {
                opponent.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(currentPlayer.getName() + " disconnected");
            return false;

        } else if (!Server.testConnection(opponent)) {

            currentPlayer.getOutput().println("{\"cmd\" : \"WON\"," +
                    "\"msg\": \"Your opponent left the game. \" }");

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