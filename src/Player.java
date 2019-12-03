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
        if (shipList[length + 1] > 0) {

            // Checks if the selected 'spots' are free
            for (int i = -1; i < length + 1; i++) {
                // Check in different orientations
                if (orientation == "H") {
                    // Check for all selected spots, and every one of those needs to have a free
                    // spot near them
                    if (gameGrid[x + i][y].getShip() != null 
                            && gameGrid[x + i][y + 1].getShip() != null 
                            && gameGrid[x + i][y - 1].getShip() != null) {
                        output.println("ERROR 1 Invalid position");
                        return;
                    }
                }
                else {
                    if (gameGrid[x][y + i].getShip() != null 
                            && gameGrid[x + 1][y + i].getShip() != null 
                            && gameGrid[x - 1][y + i].getShip() != null) {
                        output.println("ERROR 1 Invalid boat position");
                        return;
                    }
                }
            }

            gameGrid[x][y].insertShip(new Ship(length, orientation, x, y));
            shipList[length + 1]--;
            output.println("OK Added new ship of length" + length);
        }
        else {
            output.println("ERROR 2 Selected boat size not available");
        }
    }

}