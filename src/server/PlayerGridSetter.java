package server;

public class PlayerGridSetter implements Runnable {
    Player player;

    public PlayerGridSetter(Player player) {
        this.player = player;
    }

    /**
     * Main Player function
     */
    @Override
    public void run() {
        positionShips();
    }

    /**
     * Function that manages clients ships grid setup logic
     */
    private void positionShips() {
        String command;

        System.out.println("Starting positioning player " + player.getName() + " ships");

        while (true) {
            try {
                command = player.receive();
            } catch (Exception e) {
                break;  //if there's a connection error, finish the positioning
            }

            try {
                if (command.startsWith("GRID")) {
                    StringBuilder msg = new StringBuilder();
                    for (int n : player.getShipList()) {
                        msg.append(n);
                    }

                    player.send("{ " +
                            " \"cmd\" : \"GRID\"" +
                            ", \"msg\" : {" +
                            "\"length\" : " + Server.getGridLength() +
                            ", \"ships\" : \"" + msg
                            + "\"} }");

                } else if (command.startsWith("SET")) {
                    set(command);
                    //player.printGrid();

                } else if (command.startsWith("DELETE")) {
                    delete(command);
                    //player.printGrid();

                } else if (command.startsWith("RESET")) {
                    player.resetGrid();

                    //send GRID as response
                    StringBuilder msg = new StringBuilder();
                    for (int n : player.getShipList()) {
                        msg.append(n);
                    }

                    player.send("{ " +
                            " \"cmd\" : \"GRID\"" +
                            ", \"msg\" : {" +
                            "\"length\" : " + Server.getGridLength() +
                            ", \"ships\" : \"" + msg
                            + "\"} }");

                } else if (command.startsWith("READY")) {
                    if (!player.isGridReady())  //if the player hasn't finished to position its ships
                        player.send("{ \"cmd\" : \"ERROR\"" +
                                ", \"msg\" : { " +
                                "\"cod\" : \"103\"" +
                                ",\"msg\" : \"You still have ships left to position\" } }");
                    else {
                        player.isReadyToPlay(true);  //set that the player is ready

                        if (player.getOpponent() == null) {    //if the player doesn't have an opponent yet
                            player.send("{\"cmd\" : \"WAIT\"}");
                            break;
                        } else if (!player.getOpponent().isReadyToPlay()) {   //if the opponent's grid isn't ready
                            player.send("{\"cmd\" : \"WAIT\"}");
                            break;
                        } else  //if both players are ready, exit from the grid setting (Game is going to warn Clients to start the game (PLAY command))
                            break;
                    }
                } else {
                    player.send("{ \"cmd\" : \"ERROR\"" +
                                ", \"msg\" : { " +
                                "\"cod\" : \"900\"" +
                                ",\"msg\" : \"Invalid Message\" } }");
                }

            } catch (Exception e) {
                if (e.getMessage().contains("ERROR")) player.send(e.getMessage());
                else {
                    System.out.println("Client " + player.getName() + " disconnected.");
                    break;
                }
            }
        }
        //print the message only if the player sent READY, not if it has disconnected
        if (player.isReadyToPlay()) { 
            System.out.println("Grid positioning of " + player.getName() + " finished.");

            player.printGrid();
        } else {
            System.out.println("Client " + player.getName() + " left.");

            //TODO: replace "player.getOpponent() != null"
            //TODO: with "!Server.testConnection(player.getOpponent())"
            //TODO: once the function works
            if (player.getOpponent() != null) {
                player.getOpponent().send("{\"cmd\" : \"WON\"," +
                        "\"msg\": \"Your opponent left the game. \" }");
                player.getOpponent().disconnect();
            }
        }
    }

    /**
     * Function that sets the ship of player based on the received event
     *
     * @param event SET event that has to be managed
     */
    private void set(String event) {

        GameSyntaxChecker.checkCorrectMessageFormat("SET", event);

        int row = Integer.parseInt(String.valueOf(event.charAt(4)).concat(String.valueOf(event.charAt(5))));
        int col = Integer.parseInt(String.valueOf(event.charAt(6)).concat(String.valueOf(event.charAt(7))));
        int length = Integer.parseInt(String.valueOf(event.charAt(8)));
        char orientation = event.charAt(9);

        GameSyntaxChecker.checkCorrectMessage(col, row, length, orientation);

        try {
            //if an exception is thrown by player.set(), it means that its parameters are invalid
            player.set(col, row, length, orientation);
            player.send("{\"cmd\" : \"OK\"," +
                    "\"msg\": \"Added new ship of length " + length + "\" }");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ERROR")) {
                System.out.println("SET Error: " + e.getMessage());
                player.send(e.getMessage());
            } else e.printStackTrace();
        }
    }

    /**
     * Function that deletes the ship of player based on the received event
     *
     * @param event DELETE event that has to be managed
     */
    private void delete(String event) {

        GameSyntaxChecker.checkCorrectMessageFormat("DELETE", event);

        int row = Integer.parseInt(String.valueOf(event.charAt(7)).concat(String.valueOf(event.charAt(8))));
        int col = Integer.parseInt(String.valueOf(event.charAt(9)).concat(String.valueOf(event.charAt(10))));

        GameSyntaxChecker.checkCorrectMessage(col, row);

        try {
            //if an exception is thrown by player.delete(), it means that its parameters are invalid
            player.delete(col, row);
            player.send("{\"cmd\" : \"OK\"" +
                    ", \"msg\": \"Removed the ship\" }");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ERROR")) {
                System.out.println("DELETE Error: " + e.getMessage());
                player.send(e.getMessage());
            } else e.printStackTrace();
        }
    }
}
