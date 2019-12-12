/**
 * Class that manages the listening for one Player SET and DELETE functions (Server-side)
 */
class GridSetter implements Runnable {
    private Player player;
    private SyntaxChecker syntaxChecker;

    /**
     * main Constructor
     * @param player player which has to insert the ships
     * @param syntaxChecker syntaxChecker object to control message format
     */
    public GridSetter(Player player, SyntaxChecker syntaxChecker) {
        this.player = player;
        this.syntaxChecker = syntaxChecker;
    }

    /**
     * Main gridSetter function that contains all ships set logic and waiting for opponent
     */
    @Override
    public void run() {
        String command;
            while(!(player.isGridReady())){
                try {
                    if(player.getInput().hasNextLine()){
                        command = player.getInput().nextLine();

                        if (command != null && command.startsWith("SET")) {
                            System.out.println("SET command received from " + player.getName() + ": " + command);
                            set(command);
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }

        //when grid positioning is finished, alert user that it
        try{
            player.getOutput().println("READY");
        } catch (Exception e) {
            e.printStackTrace();
        }
        }

    /**
     * Function that sets the ship of player based on the received event
     * @param event SET event that has to be managed
     */
    private void set(String event) {
        syntaxChecker.checkCorrectMessageFormat("SET", event);

        int x = Integer.parseInt(String.valueOf(event.charAt(4)).concat(String.valueOf(event.charAt(5))));
        int y = Integer.parseInt(String.valueOf(event.charAt(6)).concat(String.valueOf(event.charAt(7))));
        int length = Integer.parseInt(String.valueOf(event.charAt(8)));
        char orientation = event.charAt(9);

        syntaxChecker.checkCorrectMessage(x, y, length, orientation);
        try{
            //if an exception is thrown by player.set, it means that its parameters are invalid
            player.set(x, y, length, orientation);

            player.getOutput().println("OK Added new ship of length" + length);
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
