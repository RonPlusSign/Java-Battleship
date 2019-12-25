/**
 * Class used to check if the syntax/content of the messages is valid
 */
class SyntaxChecker {
    private int maxGrid;

    public SyntaxChecker() {
        this.maxGrid = Server.GRID_LENGTH;
    }

    /**
     * Function which checks the validity of the FIRE and DELETE message
     *
     * @param x X Axys
     * @param y Y Axys
     * @throws IllegalArgumentException Invalid Coordinates
     */
    public void checkCorrectMessage(int x, int y) throws IllegalArgumentException {
        if (!(x >= 0 && x < maxGrid && y >= 0 && y < maxGrid))
            throw new IllegalArgumentException(
                    "{ \"cmd\" : \"ERROR\"" +
                            ", \"msg\" : { " +
                            "\"cod\" : \"901\"" +
                            ",\"msg\" : \"Invalid coordinates\" }}"
            );
    }

    /**
     * Function which checks the validity of the SET message
     *
     * @param x           X Axys
     * @param y           Y Axys
     * @param length      Ship Length
     * @param orientation Ship Orientation
     * @throws IllegalArgumentException Invalid Length or Invalid Orientation
     */
    public void checkCorrectMessage(int x, int y, int length, char orientation) throws IllegalArgumentException {
        checkCorrectMessage(x, y);
        if (length < 1 || length > 5) throw new IllegalArgumentException(   //ships can have a length of 1, 2, 3, 4 or 5
                "{ \"cmd\" : \"ERROR\"" +
                        ", \"msg\" : { " +
                        "\"cod\" : \"101\"" +
                        ",\"msg\" : \"Selected ship length is not available\" }}"
        );
        if (orientation != 'H' && orientation != 'V')
            throw new IllegalArgumentException(
                    "{ \"cmd\" : \"ERROR\"" +
                            ", \"msg\" : { " +
                            "\"cod\" : \"104\"" +
                            ",\"msg\" : \"Invalid Orientation (Select H or V)\" }}"
            );
    }

    /**
     * Function which checks the correct message length
     *
     * @param nameMessage FIRE or SET
     * @param message     message
     * @throws IllegalArgumentException Invalid Message Format
     */
    public void checkCorrectMessageFormat(String nameMessage, String message) throws IllegalArgumentException {
        //DELETE message
        if (nameMessage.equals("DELETE")) {
            if (!(String.valueOf(message.charAt(6)).equals(" ") && message.length() == 11))
                throw new IllegalArgumentException(
                        "{ \"cmd\" : \"ERROR\"" +
                                ", \"msg\" : { " +
                                "\"cod\" : \"900\"" +
                                ",\"msg\" : \"Invalid message format\" }}"
                );
        }
        //FIRE message
        else if (nameMessage.equals("FIRE")) {
            if (!(String.valueOf(message.charAt(4)).equals(" ") && message.length() == 9))
                throw new IllegalArgumentException(
                        "{ \"cmd\" : \"ERROR\"" +
                                ", \"msg\" : { " +
                                "\"cod\" : \"900\"" +
                                ",\"msg\" : \"Invalid message format\" }}"
                );
        }
        //SET message
        else if (nameMessage.equals("SET")) {
            if (!(String.valueOf(message.charAt(3)).equals(" ") && message.length() == 10))
                throw new IllegalArgumentException(
                        "{ \"cmd\" : \"ERROR\"" +
                                ", \"msg\" : { " +
                                "\"cod\" : \"900\"" +
                                ",\"msg\" : \"Invalid message format\" }}"
                );
        }
    }
}