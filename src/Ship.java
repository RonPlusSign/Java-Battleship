/**
 * Ship
 * Each ship will be put into the Game Grid
 */
public class Ship {

    char x;
    char y;
    int length;
    char orientation;

    //Constructor
    public Ship(int length, char orientation) {
        this.length = length;
        this.orientation = orientation;
    }

    //Called when placed into the Game Grid
    public void setCoordinates(char x, char y) {
        this.x = x;
        this.y = y;
    }
}
