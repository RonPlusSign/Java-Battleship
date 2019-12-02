/**
 * Tile
 * Each Tile composes the Game Grid. It contains the Ship
 * (if it is placed) and the boolean isHit used to check if the Ship has been hit there
 */
public class Tile {

    Ship ship;
    boolean isHit;

    public Tile() {
        isHit=false;
    }

    public void insertShip(Ship ship){
        this.ship=ship;
    }

    public void setIsHit() {
        isHit = true;
    }

    public boolean getIsHit(){
        return isHit;
    }
}
