import java.io.IOException;

class SyntaxChecker {
    private int maxGrid;

    public SyntaxChecker(int maxGrid) {
        this.maxGrid = maxGrid;
    }

    /**
     * Function which check the validity of the FIRE message
     * @param x X Axys
     * @param y Y Axys
     * @throws IllegalArgumentException Invalid Coordinates
     */
    public void checkCorrectMessage(int x, int y) throws IllegalArgumentException {
        if (!(x>0 && x<maxGrid && y>0 && y<maxGrid)) throw new IllegalArgumentException("ERROR Invalid Coordinates");
    }

    /**
     * Function which check the validity of the SET message
     * @param x X Axys
     * @param y Y Axys
     * @param length Ship Length
     * @param orientation Ship Orientation
     * @throws IllegalArgumentException Invalid Length or Invalid Orientation
     */
    public void checkCorrectMessage(int x, int y, int length, char orientation) throws IllegalArgumentException{
        checkCorrectMessage(x,y);
        if (length < 2 || length > 5) throw new IllegalArgumentException("ERROR Invalid Length");
        if (orientation != 'H' && orientation != 'V') throw new IllegalArgumentException("ERROR Invalid Orientation. Select H or V");
    }

    /**
     * Function which checks the correct message length
     * @param nameMessage FIRE or SET
     * @param message message
     * @throws IllegalArgumentException Invalid Message Format
     */
    public void checkCorrectMessageFormat(String nameMessage, String message) throws IllegalArgumentException {
        //DELETE message
        if(nameMessage.equals("DELETE")){
            if (!(String.valueOf(message.charAt(6)).equals(" ") && message.length() == 11))
                throw new IllegalArgumentException("ERROR Invalid Message Format");
        }
        //FIRE message
        else if (nameMessage.equals("FIRE")){
            if (!(String.valueOf(message.charAt(4)).equals(" ") && message.length() == 9))
                throw new IllegalArgumentException("ERROR Invalid Message Format");
        }
        //SET message
        else if (!(String.valueOf(message.charAt(3)).equals(" ") && message.length() == 10))
            throw new IllegalArgumentException("ERROR Invalid Message Format");
    }


}