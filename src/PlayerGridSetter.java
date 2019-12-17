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

        while (!(player.isGridReady())) {
            try {
                if (player.getInput().hasNextLine()) {
                    command = player.getInput().nextLine();

                    if (command != null) {
                        if (command.startsWith("GRID")) {
                            StringBuilder shipsConcat = new StringBuilder();

                            for (int n : Player.getStartingShipList()) {
                                shipsConcat.append(n);
                            }

                            player.getOutput().println("{" +
                                    " \"cmd\" : \"GRID\"," +
                                    " \"msg\" : \"" +
                                    shipsConcat
                                    + "\"" +
                                    "}");
                        } else if (command.startsWith("SET")) {
                            System.out.println("SET command received from " + player.getName() + ": " + command);
                            set(command);
                        } else if (command.startsWith("DELETE")) {

                        } else if (command.startsWith("READY")) {
                            if (!player.isGridReady())
                                player.getOutput().println("ERROR 03");
                        } else if (player.getOpponent() == null)
                            player.getOutput().println("WAIT");
                        else if (player.getOpponent().isGridReady())
                            player.getOutput().println("READY");
                        else
                            player.getOutput().println("WAIT");
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
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
            //if an exception is thrown by player.set, it means that its parameters are invalid
            player.set(x, y, length, orientation);
            player.getOutput().println("OK Added new ship of length " + length);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("ERROR")) {
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
            //if an exception is thrown by player.set, it means that its parameters are invalid
            player.delete(x, y);
            player.getOutput().println("OK Removed the ship");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("ERROR")) {
                player.getOutput().println("ERROR");
            } else e.printStackTrace();
        }
    }
}
