/**
 * Tile
 * Each Tile composes the Game Grid. It contains the Ship
 * (if it is placed) and the boolean isHit used to check if the Ship has been hit there
 */
public class Tile {

    private Ship ship;
    private boolean isHit;

    public Tile() {
        isHit = false;
    }

    public void insertShip(Ship ship){
        this.ship=ship;
    }

    /**
     * Hit the current tile
     * @return  if the shot is successfull (water -> false, hit -> true)
     */
    public boolean hit() {
        if (ship != null) {
            isHit = true;
            ship.removeLifePoint();
            return true;
        }
        else return false;
    }

    /**
     * Check if the current tile is hit
     * @return value
     */
    public boolean isHit(){
        return isHit;
    }

    /**
     * @return the ship
     */
    public Ship getShip() {
        return ship;
    }
}
