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
    private Tile[][] gameGrid;

    /**
     * Contains the number of ships available for each length
     * i.e.
     * shipList[0] contains the number of ships of size 2
     * shipList[1] contains the number of ships of size 3
     * shipList[2] contains the number of ships of size 4
     * shipList[3] contains the number of ships of size 5
     */
    private static final int[] startingShipList = new int[]{
            3, 2, 1, 1
    };
    // Current player ship list (contains the remanining number of ships)
    private int[] shipList;

    /**
     * Constructor
     * @param socket socket object connected to Server
     * @param name Player name
     * @param size grid length
     */
    public Player(Socket socket, String name, int size) {
        this.socket = socket;
        this.name = name;
        gameGrid = new Tile[size][size];

        // Initialize tiles
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function which fires the own tile
     *
     * @param x X Axys
     * @param y Y Axys
     */
    public void fire(int x, int y) {
        if (gameGrid[x][y].hit()) {

            output.print("HIT " + String.format("%02d", x) + String.format("%02d", y));

            if (gameGrid[x][y].getShip().isSunk()) {

                output.println("SUNK " + String.format("%02d", gameGrid[x][y].getShip().getX()) +
                        String.format("%02d", gameGrid[x][y].getShip().getY()) +
                        gameGrid[x][y].getShip().getLength() +
                        gameGrid[x][y].getShip().getOrientation());
            } else output.println();
        } else {
            output.println("MISS " + String.format("%02d", x) + String.format("%02d", y));
        }
    }

    /**
     * Function which sets the ship in the gameGrid
     *
     * @param x           Axys
     * @param y           Axys
     * @param length      Ship Length
     * @param orientation Ship Orientation
     */
    public void set(int x, int y, int length, char orientation) throws IllegalArgumentException {
        // Check if selected ship is available
        if (shipList[length - 2] > 0) {

            if (getAvailability(x, y, length, orientation)) {
                gameGrid[x][y].insertShip(new Ship(length, orientation, x, y));
                shipList[length - 2]--;
                setAvailability(x, y, length, orientation, false);
            } else {
                throw new IllegalArgumentException("ERROR 1 Invalid boat position");
            }
        } else {
            throw new IllegalArgumentException("ERROR 2 Selected boat size not available");
        }
    }

    public void delete(int x, int y) throws IllegalArgumentException {
        if (gameGrid[x][y] == null) throw new IllegalArgumentException("ERROR 6 Selected tile doesn't contain a boat");
        else {
            int xInit = gameGrid[x][y].getShip().getX();
            int yInit = gameGrid[x][y].getShip().getY();
            int length = gameGrid[x][y].getShip().getLength();
            char orientation = gameGrid[x][y].getShip().getOrientation();

            for (int i = 0; i < length; i++) {
                if (orientation == 'H') {
                    gameGrid[xInit+i][yInit].deleteShip();
                } else{
                    gameGrid[xInit][yInit+i].deleteShip();
                }
            }

            setAvailability(xInit, yInit, length, orientation, true);
        }
    }


    /**
     * Function which checks the availability of the Tiles selected to insert the Ship into them
     *
     * @param x           Axys
     * @param y           Axys
     * @param length      Ship Length
     * @param orientation Ship Orientation
     * @return the availability of the Tiles
     */
    private boolean getAvailability(int x, int y, int length, char orientation) {
        //Check if the ship "overflows" from the grid
        //If the ship is Horizontally placed and its length goes outside the grid (21 columns)
        if (orientation == 'H' && (x + length - 1) >= gameGrid[0].length) return false;
        //If the ship is Vertically placed and its length goes outside the grid (21 rows)
        if (orientation == 'V' && (y + length - 1) >= gameGrid.length) return false;

        for (int i = 0; i < length; i++) {
            if (orientation == 'H') {
                if (!gameGrid[i + x][y].isAvailable()) return false;
            } else {
                if (!gameGrid[x][i + y].isAvailable()) return false;
            }
        }
        return true;
    }

    /**
     * Function which sets the availability (or unavailability) of the tiles near to the ships, including the tiles where the Ship is placed
     *
     * @param x           X Axys
     * @param y           Y Axys
     * @param length      Ship Length
     * @param orientation Ship Orientation
     * @param available   true --> AVAILABLE false --> UNAVAILABLE
     */
    private void setAvailability(int x, int y, int length, char orientation, boolean available) {

        //Loops that marks the tiles near the ship as not available (available = false;)
        for (int i = 0; i < length; i++) {
            //If the orientation is Horizontal
            if (orientation == 'H') {
                //If the Ship isn't placed at the first or at the last column
                if (((x + i) != 0) && ((x + i) != 20)) {
                    gameGrid[x + i - 1][y].setAvailable(available);
                    gameGrid[x + i + 1][y].setAvailable(available);
                }
                //If the Ship is placed at the first column
                if (y == 0) gameGrid[x + i][y + 1].setAvailable(available);
                    //If the Ship is placed at the last column
                else if (y == 20) gameGrid[x + i][y - 1].setAvailable(available);
                else {
                    gameGrid[x + i][y + 1].setAvailable(available);
                    gameGrid[x + i][y - 1].setAvailable(available);
                }
            }
            //If the orientation is Vertical
            else {
                //If the Ship isn't placed at the first or at the last row
                if (((y + i) != 0) && ((y + i) != 20)) {
                    gameGrid[x][y + i - 1].setAvailable(available);
                    gameGrid[x][y + i + 1].setAvailable(available);
                }
                //If the Ship is placed at the first row
                if (x == 0) gameGrid[x + 1][y + i].setAvailable(available);
                    //If the Ship is placed at the last row
                else if (x == 20) gameGrid[x - 1][y + i].setAvailable(available);
                else {
                    gameGrid[x + 1][y + i].setAvailable(available);
                    gameGrid[x - 1][y + i].setAvailable(available);
                }
            }
        }
    }

    /**
     * Function that checks if all the Player ships are positioned
     * @return true if all the ships are set, false otherwise
     */
    public boolean isGridReady() {
        for (int boatsToBeSet : shipList) {
            if (boatsToBeSet != 0) return false;  //if there are still boats to be set, return false
        }
        //if all boats have been set, return true
        return true;
    }

    /* getters & setters */

    public PrintWriter getOutput() { return output; }

    public Scanner getInput() { return input; }

    public String getName() { return name; }

    public Socket getSocket() { return socket; }

    public void setOpponent(Player opponent) { this.opponent = opponent; }

    public static int[] getStartingShipList() { return startingShipList; }

    public int[] getShipList() { return shipList; }
}