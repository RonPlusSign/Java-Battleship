package server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Class that manages a game between 2 Players
 */
class Game implements Runnable {
    private Player currentPlayer, opponent; //Players
    private boolean isGameFinished;


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

        this.currentPlayer.setGame(this);
        this.opponent.setGame(this);

        isGameFinished = false;
    }

    /**
     * Main function that runs all the main functions in order
     */
    @Override
    public void run() {
        try {

            while (!(currentPlayer.isReadyToPlay() && opponent.isReadyToPlay())) {    //wait for clients to set their grid layout
                Thread.sleep(2000); //time is in milliseconds
            }

            currentPlayer.send("{\"cmd\" : \"PLAY\"}");
            opponent.send("{\"cmd\" : \"PLAY\"}");
            manageGame();   //main function

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Game finished.");
    }

    /**
     * Function that handles the game turnament logic
     * See README.md for more info about the protocol
     */
    private void manageGame() {
        while (!isGameFinished) {
            try {
                //Loops until the move is valid
                while (true) {
                    // Is your turn: TURN true
                    currentPlayer.send("{\"cmd\" : \"TURN\", " +
                            "\"msg\" : true }");
                    // Is not your turn: TURN false
                    opponent.send("{\"cmd\" : \"TURN\", " +
                            "\"msg\" : false }");

                    try {
                        String command = "";

                        try{
                            command = currentPlayer.receive();
                        } catch (Exception e){
                            opponent.send("{\"cmd\" : \"WON\"," +
                                    "\"msg\": \"Your opponent left the game. \" }");
                            isGameFinished = true;
                            break;
                        }

                        //during this part of the game, Client can only request for FIRE
                        if (command.startsWith("FIRE")) {
                            //If the message has the correct Format
                            GameSyntaxChecker.checkCorrectMessageFormat("FIRE", command);

                            int row = Integer.parseInt(String.valueOf(command.charAt(5)).concat(String.valueOf(command.charAt(6))));
                            int col = Integer.parseInt(String.valueOf(command.charAt(7)).concat(String.valueOf(command.charAt(8))));

                            GameSyntaxChecker.checkCorrectMessage(col, row);

                            //fire returns true if a ship is hit. If it's hit, the Client must fire again. Otherwise the turn is swapped
                            if (!opponent.fire(col, row)) {
                                //if miss, exit the loop and swap the players. Otherwise the player has to fire again
                                break;
                            }
                        }
                        //If the event wasn't FIRE
                        else {  //If the move is valid then exit the loop, otherwise an exception is thrown and Client must send a new FIRE request
                            throw new IllegalArgumentException("{\"cmd\": \"ERROR\"" +
                                    ", \"msg\" : {" +
                                    "\"cod\" : 900" +
                                    ", \"msg: \" : \"Invalid command, expected FIRE (you're in game)\"" +
                                    "} }");
                        }

                    } catch (Exception e) {
                        currentPlayer.send(e.getMessage());
                    }
                }

                //swap players and activate opponent turns
                Player temp = currentPlayer;
                currentPlayer = opponent;
                opponent = temp;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void setIsGameFinished(boolean b){
        this.isGameFinished = b;
    }

}