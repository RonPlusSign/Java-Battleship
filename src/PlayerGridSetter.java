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

                if (command.startsWith("GRID")) {
                    StringBuilder msg = new StringBuilder();
                    for (int n : Player.getStartingShipList()) {
                        msg.append(n);
                    }

                    player.send("{ " +
                            " \"cmd\" : \"GRID\"" +
                            ", \"msg\" : {" +
                            "\"length\" : " + Server.GRID_LENGTH +
                            ", \"ships\" : \"" + msg
                            + "\"} }");

                } else if (command.startsWith("SET")) {
                    set(command);

                } else if (command.startsWith("DELETE")) {
                    delete(command);

                } else if (command.startsWith("RESET")) {
                    player.resetGrid();

                    //send GRID as response
                    StringBuilder msg = new StringBuilder();
                    for (int n : Player.getStartingShipList()) {
                        msg.append(n);
                    }

                    player.send("{ " +
                            " \"cmd\" : \"GRID\"" +
                            ", \"msg\" : {" +
                            "\"length\" : " + Server.GRID_LENGTH +
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
                }
            } catch (Exception e) {
                System.out.println("Exception thrown: " + e.getMessage());
                player.send(e.getMessage());
            }
        }
        System.out.println("Grid positioning of " + player.getName() + " finished");
    }

    /**
     * Function that sets the ship of player based on the received event
     *
     * @param event SET event that has to be managed
     */
    private void set(String event) {
        SyntaxChecker syntaxChecker = new SyntaxChecker();
        syntaxChecker.checkCorrectMessageFormat("SET", event);

        int x = Integer.parseInt(String.valueOf(event.charAt(6)).concat(String.valueOf(event.charAt(7))));
        int y = Integer.parseInt(String.valueOf(event.charAt(4)).concat(String.valueOf(event.charAt(5))));
        int length = Integer.parseInt(String.valueOf(event.charAt(8)));
        char orientation = event.charAt(9);

        syntaxChecker.checkCorrectMessage(x, y, length, orientation);

        try {
            //if an exception is thrown by player.set(), it means that its parameters are invalid
            player.set(x, y, length, orientation);
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
        SyntaxChecker syntaxChecker = new SyntaxChecker();
        syntaxChecker.checkCorrectMessageFormat("DELETE", event);

        int x = Integer.parseInt(String.valueOf(event.charAt(9)).concat(String.valueOf(event.charAt(10))));
        int y = Integer.parseInt(String.valueOf(event.charAt(7)).concat(String.valueOf(event.charAt(8))));

        syntaxChecker.checkCorrectMessage(x, y);

        try {
            //if an exception is thrown by player.delete(), it means that its parameters are invalid
            player.delete(x, y);
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
