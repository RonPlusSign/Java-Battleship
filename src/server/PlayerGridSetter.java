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
                System.out.println("Exception thrown: " + e.getMessage());
                break;
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

                } else if (command.startsWith("DELETE")) {
                    delete(command);

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
                }
            } catch (Exception e) {
                System.out.println("Exception thrown: " + e.getMessage());
                player.send(e.getMessage());
                break;
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
