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
                if (player.getInput().hasNextLine()) {
                    command = player.getInput().nextLine();

                    if (command != null) {

                        System.out.println("COMMAND FROM CLIENT: " + command);

                        if (command.startsWith("GRID")) {

                            System.out.println("GRID command received from " + player.getName() + ": " + command);

                            StringBuilder msg = new StringBuilder();
                            for (int n : Player.getStartingShipList()) {
                                msg.append(n);
                            }

                            player.getOutput().println("{ " +
                                    " \"cmd\" : \"GRID\"" +
                                    ", \"msg\" : {" +
                                    "\"length\" : " + Server.GRID_LENGTH +
                                    ", \"ships\" : \"" + msg
                                    + "\"} }");

                        } else if (command.startsWith("SET")) {

                            System.out.println("SET command received from " + player.getName() + ": " + command);

                            set(command);
                        } else if (command.startsWith("DELETE")) {

                            System.out.println("DELETE command received from " + player.getName() + ": " + command);

                            delete(command);
                        } else if(command.startsWith("RESET")){

                            System.out.println("RESET command received from " + player.getName() + ": " + command);

                            player.resetGrid();
                        }
                        else if (command.startsWith("READY")) {

                            System.out.println("READY command received from " + player.getName() + ": " + command);

                            if (!player.isGridReady())  //if the player hasn't finished to position its ships
                                player.getOutput().println("{ \"cmd\" : \"ERROR\"" +
                                        ", \"msg\" : { " +
                                        "\"cod\" : \"103\"" +
                                        ",\"msg\" : \"You still have ships left to position\" } }");
                            else if (player.getOpponent() == null) {    //if the player doesn't have an opponent yet
                                player.getOutput().println("{\"cmd\" : \"WAIT\"}");
                                break;
                            } else if (!player.getOpponent().isGridReady()) {   //if the opponent's grid isn't ready
                                player.getOutput().println("{\"cmd\" : \"WAIT\"}");
                                break;
                            } else  //if both players are ready, exit from the grid setting (Game is going to warn Clients to start the game (PLAY command))
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception thrown: " + e.getMessage());
                player.getOutput().println(e.getMessage());
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

        int x = Integer.parseInt(String.valueOf(event.charAt(4)).concat(String.valueOf(event.charAt(5))));
        int y = Integer.parseInt(String.valueOf(event.charAt(6)).concat(String.valueOf(event.charAt(7))));
        int length = Integer.parseInt(String.valueOf(event.charAt(8)));
        char orientation = event.charAt(9);

        syntaxChecker.checkCorrectMessage(x, y, length, orientation);

        try {
            //if an exception is thrown by player.set(), it means that its parameters are invalid
            player.set(x, y, length, orientation);
            player.getOutput().println("{\"cmd\" : \"OK\"," +
                    "\"msg\": \"Added new ship of length " + length + "\" }");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ERROR")) {
                System.out.println("SET Error: " + e.getMessage());
                player.getOutput().println(e.getMessage());
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

        int x = Integer.parseInt(String.valueOf(event.charAt(7)).concat(String.valueOf(event.charAt(8))));
        int y = Integer.parseInt(String.valueOf(event.charAt(9)).concat(String.valueOf(event.charAt(10))));

        syntaxChecker.checkCorrectMessage(x, y);

        try {
            //if an exception is thrown by player.delete(), it means that its parameters are invalid
            player.delete(x, y);
            player.getOutput().println("{\"cmd\" : \"OK\"" +
                    ", \"msg\": \"Removed the ship\" }");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ERROR")) {
                System.out.println("DELETE Error: " + e.getMessage());
                player.getOutput().println(e.getMessage());
            } else e.printStackTrace();
        }
    }
}
