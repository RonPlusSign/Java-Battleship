package server;

/**
 * Ship
 * Each ship will be put into the Game Grid
 */
public class Ship {

    private int row;
    private int column;
    private int length;
    private char orientation;
    private int lifePoints;

    //Constructor
    public Ship(int length, char orientation, int column, int row) {
        this.length = length;
        this.orientation = orientation;
        this.column = column;
        this.row = row;

        lifePoints = length;
    }

    /*
        Checks if the Ship is positioned into the grid
    */
    public boolean isPositioned() {
        return (row > 0 && column > 0);
    }

    /**
     * Removes one 'live' to the ship
     */
    public void removeLifePoint() {
        if (lifePoints - 1 >= 0) --lifePoints;
    }


    /**
     * Check if the ship is sunk
     *
     * @return value
     */
    public boolean isSunk() {
        return lifePoints == 0;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @return the orientation
     */
    public char getOrientation() {
        return orientation;
    }

    /**
     * @return the column
     */
    public int getColumn() {
        return column;
    }

    /**
     * @return the row
     */
    public int getRow() {
        return row;
    }
}
