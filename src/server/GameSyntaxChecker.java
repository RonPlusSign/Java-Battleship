package server;

/**
 * Class used to check if the syntax/content of the messages is valid
 */
public class GameSyntaxChecker {

    /**
     * Function which checks the validity of the FIRE and DELETE message
     *
     * @param col column
     * @param row row
     * @throws IllegalArgumentException Invalid Coordinates
     */
    public static void checkCorrectMessage(int col, int row) throws IllegalArgumentException {
        if (!(col >= 0 && col < Server.getGridLength() && row >= 0 && row < Server.getGridLength()))
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
     * @param col         column
     * @param row         row
     * @param length      Ship Length
     * @param orientation Ship Orientation
     * @throws IllegalArgumentException Invalid Length or Invalid Orientation
     */
    public static void checkCorrectMessage(int col, int row, int length, char orientation) throws IllegalArgumentException {
        checkCorrectMessage(col, row);
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
    public static void checkCorrectMessageFormat(String nameMessage, String message) throws IllegalArgumentException {
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