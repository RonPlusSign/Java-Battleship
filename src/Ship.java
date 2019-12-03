/**
 * Ship
 * Each ship will be put into the Game Grid
 */
public class Ship {

    private int x;
    private int y;
    private int length;
    private char orientation;
    private int lifePoints;

    //Constructor
    public Ship(int length, char orientation, int x, int y) {
        this.length = length;
        this.orientation = orientation;
        this.x = x;
        this.y = y;

        lifePoints = length;
    }

    /*
        Checks if the Ship is positioned into the grid
    */
    public boolean isPositioned() {
        return (x > 0 && y > 0);
    }

    /**
        Removes one 'live' to the ship
    */
    public void removeLifePoint() {
        if (lifePoints - 1 >= 0) --lifePoints;
    }

    
    /**
     * Check if the ship is sunk
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
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }
}
