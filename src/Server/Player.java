package Server;

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
    private boolean readyToPlay;

    /**
     * Contains the number of ships available for each length
     * i.e.
     * shipList[0] contains the number of ships of size 1
     * shipList[1] contains the number of ships of size 2
     * shipList[2] contains the number of ships of size 3
     * shipList[3] contains the number of ships of size 4
     * shipList[4] contains the number of ships of size 5
     */
    private static final int[] startingShipList = {
            3, 3, 2, 1, 1
    };
    // Current player ship list (contains the remanining number of ships)
    private int[] shipList;
    private int shipsAlive; //number of ships that are left to the player

    /**
     * Constructor
     *
     * @param socket socket object connected to Server
     * @param name   Player name
     */
    public Player(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
        gameGrid = new Tile[Server.GRID_LENGTH][Server.GRID_LENGTH];

        resetGrid();
        readyToPlay = false;

        System.out.println("New Client connected: " + this.name);

        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new PlayerGridSetter(this)).start();
    }

    /**
     * Function used to write on the player's output stream
     * It must be synchronized because multiple threads have to write on the socket one at the time
     *
     * @param message message to be sent
     */
    public synchronized void send(String message) {
        System.out.println("SENDING: " + message);

        //check if message is null
        if (message != null) {
            //write the message
            output.printf(message + "\n");

            //flush the stream, in that way the message is sent and the output is ready for new messages
            output.flush();
        }
    }

    /**
     * Function used to read from the player's input stream
     *
     * @return the String in input
     */
    public String receive() {
        String message = input.nextLine();

        System.out.println("RECEIVED (" + name + "): " + message);

        return message;
    }

    /**
     * Function that resets the ships grid and the available ships
     */
    public void resetGrid() {
        // Initialize tiles
        for (int i = 0; i < Server.GRID_LENGTH; i++) {
            for (int j = 0; j < Server.GRID_LENGTH; j++) {
                gameGrid[i][j] = new Tile();
            }
        }

        shipList = startingShipList.clone();

        shipsAlive = 0;
        for (int number : startingShipList) {
            shipsAlive += number;
        }
    }


    /**
     * Function which fires the own tile
     *
     * @param col column
     * @param row row
     * @return true if the player hit a ship, false otherwise
     */
    public boolean fire(int col, int row) {
        String message;

        if (gameGrid[row][col].hit()) {
            message = "{\"cmd\" : \"HIT\"," +
                    "\"msg\" : {\"row\" : " + row + ", \"col\" : " + col + " } }";

            //send HIT to both players
            send(message);
            opponent.send(message);

            if (gameGrid[row][col].getShip().isSunk()) {
                shipsAlive--;

                message = "{\"cmd\" : \"SUNK\"," +
                        "\"msg\" : {" +
                        "\"row\" : " + gameGrid[row][col].getShip().getX() +
                        ", \"col\" : " + gameGrid[row][col].getShip().getY() +
                        ", \"length\" : " + gameGrid[row][col].getShip().getLength() +
                        ", \"orientation\" :  '" + gameGrid[row][col].getShip().getOrientation() + "'" +
                        " } }";

                //sent SUNK to both players
                send(message);
                opponent.send(message);


                if (shipsAlive == 0) {    //if the ships left are 0, the opponent WIN (the function fire() works on the player who's been hit, so Opponent is the player who sent FIRE
                    opponent.send("{\"cmd\" : \"WON\"}");
                    send("{\"cmd\" : \"LOST\"}");

                    //Disconnect both Clients when match is finished
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        opponent.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        } else {
            message = "{\"cmd\" : \"MISS\"," +
                    "\"msg\" : {\"row\" : " + row + ", \"col\" : " + col + " } }";
            send(message);
            opponent.send(message);
            return false;
        }
    }

    /**
     * Function which sets the ship in the gameGrid
     *
     * @param col         Columns
     * @param row         Row
     * @param length      Ship Length
     * @param orientation Ship Orientation
     */
    public void set(int col, int row, int length, char orientation) throws IllegalArgumentException {
        // Check if selected ship is available
        if (shipList[length - 1] > 0) {
            // Check if involved Tiles are available
            if (getAvailability(col, row, length, orientation)) {
                gameGrid[row][col].insertShip(new Ship(length, orientation, col, row));
                shipList[length - 1]--;
                setAvailability(col, row, length, orientation, false);
            }
            //At least one Tile's not available
            else {
                throw new IllegalArgumentException("{ \"cmd\" : \"ERROR\"" +
                        ", \"msg\" : { " +
                        "\"cod\" : \"100\"" +
                        ",\"msg\" : \"Invalid ship position\" } }");
            }
        }
        // No more Ships of that size available
        else {
            throw new IllegalArgumentException("{ \"cmd\" : \"ERROR\"" +
                    ", \"msg\" : { " +
                    "\"cod\" : \"101\"" +
                    ",\"msg\" : \"Selected ship size is not available\" } }"
            );
        }
    }

    /**
     * Function which deletes the Ship (if exists) that is in the selected tile
     *
     * @param col Column
     * @param row Row
     * @throws IllegalArgumentException if the tile doesn't contain a ship
     */
    public void delete(int col, int row) throws IllegalArgumentException {
        //No Ship in the selected tile
        if (gameGrid[col][row] == null) throw new IllegalArgumentException("{ \"cmd\" : \"ERROR\"" +
                ", \"msg\" : { " +
                "\"cod\" : \"102\"" +
                ",\"msg\" : \"Selected tile doesn't contain a ship\" }}"
        );
        else {
            //Retrieving specific properties of the Ship
            int xInit = gameGrid[row][col].getShip().getX();
            int yInit = gameGrid[row][col].getShip().getY();
            int length = gameGrid[row][col].getShip().getLength();
            char orientation = gameGrid[row][col].getShip().getOrientation();

            //Deleting Ship from each Tile involved
            for (int i = 0; i < length; i++) {
                if (orientation == 'H') {
                    gameGrid[xInit + i][yInit].deleteShip();
                } else {
                    gameGrid[xInit][yInit + i].deleteShip();
                }
            }

            //Reset of the availability of the Tiles
            setAvailability(xInit, yInit, length, orientation, true);

            //Increase of the number of "boat size" available
            shipList[length - 1]++;
        }
    }


    /**
     * Function which checks the availability of the Tiles selected to insert the Ship into them
     *
     * @param col         Column
     * @param row         Row
     * @param length      Ship Length
     * @param orientation Ship Orientation
     * @return the availability of the Tiles
     */
    private boolean getAvailability(int col, int row, int length, char orientation) {
        //Check if the ship "overflows" from the grid
        //If the ship is Horizontally placed and its length goes outside the grid
        if (orientation == 'H' && (col + length - 1) >= Server.GRID_LENGTH) return false;

        //If the ship is Vertically placed and its length goes outside the grid
        if (orientation == 'V' && (row + length - 1) >= Server.GRID_LENGTH) return false;

        for (int i = 0; i < length; i++) {
            if (orientation == 'H') {
                if (!gameGrid[row][i + col].isAvailable()) return false;
            } else {
                if (!gameGrid[i + row][col].isAvailable()) return false;
            }
        }
        return true;
    }

    /**
     * Function which sets the availability (or unavailability) of the tiles near to the ships, including the tiles where the Ship is placed
     *
     * @param col         Column
     * @param row         Row
     * @param length      Ship Length
     * @param orientation Ship Orientation
     * @param available   true --> AVAILABLE false --> UNAVAILABLE
     */
    private void setAvailability(int col, int row, int length, char orientation, boolean available) {

        //Loops that marks the tiles near the ship as available (or not available, depending if the received parameter is true or false)
        for (int i = 0; i < length; i++) {
            //If the orientation is Horizontal
            if (orientation == 'H') {
                //If the Ship isn't placed at the first or at the last column
                if (((col + i) != 0) && ((col + i) != Server.GRID_LENGTH - 1)) {
                    gameGrid[row][col + i - 1].setAvailable(available);
                    gameGrid[row][col + i + 1].setAvailable(available);
                }
                //If the Ship is placed at the first row
                if (row == 0) gameGrid[row + 1][col + i].setAvailable(available);
                    //If the Ship is placed at the last column
                else if (row == Server.GRID_LENGTH - 1) gameGrid[row - 1][col + i].setAvailable(available);
                else {
                    gameGrid[row + 1][col + i].setAvailable(available);
                    gameGrid[row - 1][col + i].setAvailable(available);
                }
            }
            //If the orientation is Vertical
            else {
                //If the Ship isn't placed at the first or at the last row
                if (((row + i) != 0) && ((row + i) != Server.GRID_LENGTH - 1)) {
                    gameGrid[row + i - 1][col].setAvailable(available);
                    gameGrid[row + i + 1][col].setAvailable(available);
                }
                //If the Ship is placed at the first column
                if (col == 0) gameGrid[row + i][col + 1].setAvailable(available);
                    //If the Ship is placed at the last row
                else if (col == Server.GRID_LENGTH - 1) gameGrid[row + i][col - 1].setAvailable(available);
                else {
                    gameGrid[row + i][col + 1].setAvailable(available);
                    gameGrid[row + i][col - 1].setAvailable(available);
                }
            }
        }
    }

    /**
     * Function that checks if all the Player ships are positioned
     *
     * @return true if all the ships are set, false otherwise
     */
    public boolean isGridReady() {
        for (int shipsToBeSet : shipList) {
            if (shipsToBeSet != 0) return false;  //if there are still ships to be set, return false
        }
        //if all ships have been set, return true
        return true;
    }

    /* ------ Getters and Setters ------- */

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

    public static int[] getStartingShipList() {
        return startingShipList;
    }

    public int[] getShipList() {
        return shipList;
    }

    public Player getOpponent() {
        return opponent;
    }

    public int getShipsAlive() {
        return shipsAlive;
    }

    public boolean isReadyToPlay() {
        return readyToPlay;
    }

    public void isReadyToPlay(boolean readyToPlay) {
        this.readyToPlay = readyToPlay;
    }


}