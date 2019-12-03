import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * A Player is identified by a character name.
 * For communication with the client the player has a socket and associated
 * Scanner and PrintWriter.
 */
class Player {
    private String name;
    private Player opponent;
    private Socket socket;
    private Scanner input;
    private PrintWriter output;
    private Tile gameGrid[][];

    /**
     * Contains the number of ships available for each length
     * i.e.
     * shipList[0] contains the number of ships of size 1
     * shipList[1] contains the number of ships of size 2
     * ecc.
     */
    private static final int startingShipList[] = new int[] {
        5, 4, 6, 8
    };
    // Current player ship list (contains the remanining number of ships)
    private int shipList[];

    public Player(Socket socket, String name, int size) {
        this.socket = socket;
        this.name = name;
        gameGrid = new Tile[size][size];
        
        // Initalize tiles
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                gameGrid[i][j] = new Tile();
            }
        }

        shipList = startingShipList.clone();

        // Inform clients
        System.out.println("New Client connected: " + this.name);

        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + name);
            //if (opponent == null) {
            //    output.println("Waiting for opponent");
            //}

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void fire(int x, int y) {
        if (gameGrid[x][y].hit()) {
            output.println("HIT");
            
            if (gameGrid[x][y].getShip().isSunk()) {
                output.println("SUNK");
            }
        }
        else {
            output.println("MISS");
        }
    }

    public void set(int x, int y, int length, char orientation) {
        // Check if selected ship is available
        if (shipList[length - 1] > 0) {

            if (getAvailability(x, y, length, orientation)){
                gameGrid[x][y].insertShip(new Ship(length, orientation, x, y));
                shipList[length - 1]--;
                setUnavailability(x,y,length,orientation);
                output.println("OK Added new ship of length" + length);
            } else {
                output.println("ERROR 1 Invalid boat position");
                return;
            }
        }
        else {
            output.println("ERROR 2 Selected boat size not available");
        }
    }

    /**
     * Function which checks the availability of the Tiles selected to insert the Ship into them
     * @param x
     * @param y
     * @param length
     * @param orientation
     * @return
     */
    private boolean getAvailability(int x, int y, int length, char orientation){

        //Check if the ship "overflows" from the grid
        //If the ship is Horizontally placed and its length goes outside the grid (21 columns)
        if (String.valueOf(orientation) == "H" && (x+length-1) >= gameGrid[0].length) return false;
        //If the ship is Vertically placed and its length goes outside the grid (21 rows)
        if (String.valueOf(orientation) == "V" && (y+length-1) >= gameGrid.length) return false;

        for (int i=0; i < length; i++){
            if (String.valueOf(orientation) == "H"){
                if (gameGrid[i+x][y].isAvailable() == false) return false;
            } else {
                if (gameGrid[x][i+y].isAvailable() == false) return false;
            }
        }
        return true;
    }

    /**
     * Function which sets the unavailability of the tiles near to the ships, without excluding the tiles where the Ship is placed as well
     * @param x
     * @param y
     * @param length
     * @param orientation
     */
    private void setUnavailability(int x, int y, int length, char orientation){

        //Loops that marks the tiles near the ship as non available (available = false;)
        for (int i=0; i < length; i++){
           //If the orientation is Horizontal
            if (String.valueOf(orientation) == "H"){
                //If the Ship isn't placed at the first or at the last column
                if(((x+i) != 0) && ((x+i)!=20)) {
                    gameGrid[x+i-1][y].setAvailable(false);
                    gameGrid[x+i+1][y].setAvailable(false);
                }
                //If the Ship is placed at the first column
                if(y == 0) gameGrid[x+i][y+1].setAvailable(false);
                //If the Ship is placed at the last column
                else if(y == 20) gameGrid[x+i][y-1].setAvailable(false);
                else {
                    gameGrid[x+i][y+1].setAvailable(false);
                    gameGrid[x+i][y-1].setAvailable(false);
                }
            }
            //If the orientation is Vertical
            else {
                //If the Ship isn't placed at the first or at the last row
                if(((y+i) != 0) && ((y+i)!=20)) {
                    gameGrid[x][y+i-1].setAvailable(false);
                    gameGrid[x][y+i+1].setAvailable(false);
                }
                //If the Ship is placed at the first row
                if(x == 0) gameGrid[x+1][y+i].setAvailable(false);
                //If the Ship is placed at the last row
                else if(x == 20) gameGrid[x-1][y+i].setAvailable(false);
                else {
                    gameGrid[x+1][y+i].setAvailable(false);
                    gameGrid[x-1][y+i].setAvailable(false);
                }
            }
        }

    }


    public PrintWriter getOutput() {
        return output;
    }

    public Scanner getInput() {
        return input;
    }

    public String getName() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }
}