import java.io.IOException;

class Game implements Runnable {
    private Player currentPlayer, opponent;
    private final int maxGrid = 15;
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
                System.out.println("ERROR 5 Connection error");
            } else {
                //make users position its grid
                String shipsConcat = "";
                positionShips();

                manageGame();   //main function
            }
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
    }

    /**
     * Function handling the game logic
     * See README.md for more info about the protocol
     *
     * @param event command from the user
     */

    private void elaborateUserMove(String event) throws Exception {

        if (event.startsWith("DELETE")) {
            //If the message has the correct Format
            syntaxChecker.checkCorrectMessageFormat("DELETE", event);

            int x = Integer.parseInt(String.valueOf(event.charAt(7)).concat(String.valueOf(event.charAt(8))));
            int y = Integer.parseInt(String.valueOf(event.charAt(9)).concat(String.valueOf(event.charAt(10))));

            syntaxChecker.checkCorrectMessage(x, y);
            currentPlayer.delete(x, y);
        }
        //FIRE action
        if (event.startsWith("FIRE")) {
            //If the message has the correct Format
            syntaxChecker.checkCorrectMessageFormat("FIRE", event);

            int x = Integer.parseInt(String.valueOf(event.charAt(5)).concat(String.valueOf(event.charAt(6))));
            int y = Integer.parseInt(String.valueOf(event.charAt(7)).concat(String.valueOf(event.charAt(8))));

            syntaxChecker.checkCorrectMessage(x, y);
            opponent.fire(x, y);
        }
        //SET action
        else if (event.startsWith("SET")) {     //WARNING: THIS MIGHT BE DELETED BECAUSE SET IS DONE ONLY BEFORE GAME STARTS
            //If the message has the correct Format
            set(event, currentPlayer);
        }
        //Neither "DELETE nor ""FIRE" nor "SET"
        else {
            throw new IllegalArgumentException("ERROR 4 Command not valid");
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

    private void positionShips(){
        String shipsConcat = "", command;

        for (int n: Player.getStartingShipList()) { shipsConcat += String.valueOf(n); }

        //tell clients to position their ships
        try {
            currentPlayer.getOutput().println("GRID " + shipsConcat);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            opponent.getOutput().println("GRID " + shipsConcat);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Starting positioning players ships");

        //listen for set messages
        while(!currentPlayer.isGridReady() && !opponent.isGridReady()){ //&& clientsConnected()

            while (!currentPlayer.getInput().hasNextLine() || !opponent.getInput().hasNextLine()){} // && clientsConnected()) {}

            try {
                if(currentPlayer.getInput().hasNextLine()){
                    command = currentPlayer.getInput().nextLine();

                    if (command != null && command.startsWith("SET")) {
                        System.out.println("SET command received from " + currentPlayer.getName() + ": " + command);
                        set(command, currentPlayer);
                    }
                }
                else if(opponent.getInput().hasNextLine()){
                    command = opponent.getInput().nextLine();
                    if (command != null && command.startsWith("SET")) {
                        System.out.println("SET command received from " + opponent.getName() + ": " + command);
                        set(command, opponent);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

        System.out.println("Grid positioning finished");

        try{
            currentPlayer.getOutput().println("READY");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            opponent.getOutput().println("READY");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void set(String event, Player player) {
        syntaxChecker.checkCorrectMessageFormat("SET", event);

        int x = Integer.parseInt(String.valueOf(event.charAt(4)).concat(String.valueOf(event.charAt(5))));
        int y = Integer.parseInt(String.valueOf(event.charAt(6)).concat(String.valueOf(event.charAt(7))));
        int length = Integer.parseInt(String.valueOf(event.charAt(8)));
        char orientation = event.charAt(9);

        syntaxChecker.checkCorrectMessage(x, y, length, orientation);
        try{
            //if an exception is thrown by player.set, it means that its parameters are invalid
            player.set(x, y, length, orientation);

            player.getOutput().println("OK");
        } catch (IllegalArgumentException e){
            if(e.getMessage().startsWith("ERROR 1")){
                player.getOutput().println(e.getMessage());
            }
            else if(e.getMessage().startsWith("ERROR 2")){
                player.getOutput().println(e.getMessage());
            }
            else e.printStackTrace();
        }
    }
}