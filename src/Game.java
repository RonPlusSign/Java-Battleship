import java.io.IOException;

class Game implements Runnable {
    private Player currentPlayer, opponent;
    private final int maxGrid = 21;

    public Game(Player currentPlayer, Player opponent) {
        this.currentPlayer = currentPlayer;
        this.opponent = opponent;

        this.currentPlayer.setOpponent(opponent);
        this.opponent.setOpponent(currentPlayer);
    }

    @Override
    public void run() {

        try {
            if (!clientsConnected()) {
                System.out.println("[ERROR] Connection error");
            } else manageGame();   //main function
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
        return;
    }

    /**
     * Function handling the game logic
     * See README.md for more info about the protocol
     * @param event command from the user
     */

    private void elaborateUserMove(String event) throws Exception{

        if(event.startsWith("DELETE")){
            //If the message has the correct Format
            checkCorrectMessageFormat("DELETE", event);

            int x= Integer.parseInt(String.valueOf(event.charAt(7)+event.charAt(8)));
            int y= Integer.parseInt(String.valueOf(event.charAt(9)+event.charAt(10)));

            checkCorrectMessage(x, y);
            currentPlayer.delete(x,y);
        }
        //FIRE action
        if (event.startsWith("FIRE")){
            //If the message has the correct Format
            checkCorrectMessageFormat("FIRE", event);

            int x= Integer.parseInt(String.valueOf(event.charAt(5)+event.charAt(6)));
            int y= Integer.parseInt(String.valueOf(event.charAt(7)+event.charAt(8)));

            checkCorrectMessage(x, y);
            opponent.fire(x, y);
        }
        //SET action
        else if (event.startsWith("SET")){
            //If the message has the correct Format
            checkCorrectMessageFormat("FIRE", event);

            int x= Integer.parseInt(String.valueOf(event.charAt(4)+event.charAt(5)));
            int y= Integer.parseInt(String.valueOf(event.charAt(6)+event.charAt(7)));
            int length= Integer.parseInt(String.valueOf(event.charAt(8)));
            char orientation = event.charAt(9);

            checkCorrectMessage(x, y, length, orientation);
            currentPlayer.set(x, y, length, orientation);
        }
        //Neither "DELETE nor ""FIRE" nor "SET"
        else {
            throw new Exception();
        }

        System.out.println("EVENT: " + event);
    }

    /**
     * Function which check the validity of the FIRE message
     * @param x X Axys
     * @param y Y Axys
     * @throws Exception Invalid Coordinates
     */
    private void checkCorrectMessage(int x, int y) throws Exception{
        if (x>0 && x<maxGrid && y>0 && y<maxGrid) throw new Exception("Invalid Coordinates");
    }

    /**
     * Function which check the validity of the SET message
     * @param x X Axys
     * @param y Y Axys
     * @param length Ship Length
     * @param orientation Ship Orientation
     * @throws Exception Invalid Length or Invalid Orientation
     */
    private void checkCorrectMessage(int x, int y, int length, char orientation) throws Exception{
        checkCorrectMessage(x,y);
        if (length < 2 || length > 5) throw new Exception("Invalid Length");
        if (orientation != 'H' && orientation != 'V') throw new Exception("Invalid Orientation. Select H or V");
    }

    /**
     * Function which checks the correct message length
     * @param nameMessage FIRE or SET
     * @param message message
     * @throws Exception Invalid Message Format
     */
    private void checkCorrectMessageFormat(String nameMessage, String message) throws Exception{
        //DELETE message
        if(nameMessage.equals("DELETE")){
            if (!(String.valueOf(message.charAt(6)).equals(" ") && message.length() == 11))
                throw new Exception("Invalid Message Format");
        }
        //FIRE message
        else if (nameMessage.equals("FIRE")){
            if (!(String.valueOf(message.charAt(4)).equals(" ") && message.length() == 9))
                throw new Exception("Invalid Message Format");
        }
        //SET message
        else if (String.valueOf(message.charAt(3)).equals(" ") && message.length() == 10)
            throw new Exception("Invalid Message Format");
    }


    private void manageGame() {
        while (clientsConnected()) {
            try {
                //Loops until the move is valid
                while (true) {

                    currentPlayer.getOutput().println("MESSAGE Your move");

                    while (!currentPlayer.getInput().hasNextLine() && clientsConnected()) {}

                    String command = currentPlayer.getInput().nextLine();
                    System.out.print("Command received ---> ");
                    System.out.println(currentPlayer.getName() + ": " + command);

                    //If the move is valid then continue, otherwise repeat the command request
                    try {
                        elaborateUserMove(command);
                        currentPlayer.getOutput().println("You made your move: " + command);
                        opponent.getOutput().println("Your opponent made his move");
                        break;
                    } catch (Exception e) {
                        currentPlayer.getOutput().println("The move" + e.getMessage() + "wasn't valid. You have to type another command...");
                        opponent.getOutput().println("The move wasn't valid. Your opponent is typing another command...");
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
                    System.out.println("[ERROR] Connection error");
                    break;
                }
            }
        }
    }

    private boolean clientsConnected() {
        if (!Server.testConnection(currentPlayer)) {
            opponent.getOutput().println("WIN Opponent left the game.");
            System.out.println("Client " + currentPlayer.getName() + " left the game.");
            try {
                opponent.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[ERROR] Player 1 disconnected.");
            return false;
        } else if (!Server.testConnection(opponent)) {
            currentPlayer.getOutput().println("WIN Opponent left the game.");
            System.out.println("Client " + opponent.getName() + " left the game.");
            try {
                currentPlayer.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[ERROR] Player 2 disconnected.");
            return false;
        }

        System.out.println("[MESSAGE] Players connections are OK.");
        return true;
    }
}