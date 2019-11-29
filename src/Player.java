import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * A Player is identified by a character mark which is either 'X' or 'O'.
 * For communication with the client the player has a socket and associated
 * Scanner and PrintWriter.
 */
class Player {
    char mark;
    Player opponent;
    Socket socket;
    Scanner input;
    PrintWriter output;

    public Player(Socket socket, char mark) {
        this.socket = socket;
        this.mark = mark;

        System.out.println("New Client connected: " + this.mark);

        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + mark);
            if (mark == 'X') {
                output.println("Waiting for opponent");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}