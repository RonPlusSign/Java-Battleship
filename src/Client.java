import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private Scanner in;
    private PrintWriter out;
    private String username;    //stored for gui purposes (it might be showed to the user)

    public Client(String serverAddress) throws Exception {
        socket = new Socket(serverAddress, 58901);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);

        /* yourTablePanel.setLayout(new GridLayout(21, 21, 2, 2)); //TODO: Control if this can work for our playing grid
        for (var i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    out.println("MOVE " + j);
                }
            });
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER); */
    }

    /**
     * The main thread of the client will listen for messages from the server.
     * The first message will be a "WELCOME" message.
     * Then we go into a loop listening for any of the other messages,
     * and handling each message appropriately.
     */
    public void play() throws Exception {   //manage here the commands
        System.out.println("PLAY");

        while (true) {
            String message = "";
            try {
                while (true) {  //manage messages
                    if (in.hasNextLine()) {
                        message = in.nextLine();
                        System.out.println(message);

                        if(message.contains("WELCOME")){    //welcome message is composed by "WELCOME " + userID, generated Server-side
                            username = message.substring(8);    //8 is the first char after "WELCOME "
                            //TODO: make the user position its ship grid
                        }
                        else if (message.contains("PING")) {
                            out.println("PONG");
                        }
                        else if(message.contains("WIN")){
                            System.out.println("YOU WIN!");
                            //instead of closing connection, we could ask the user if he wants to play again
                            //if yes, we could add the client to the server queue (using the Game class)
                            socket.close();
                        }
                        else if(message.contains("LOST")){

                        }
                        else if (message.contains("Your move")) {
                            try {
                                Scanner inputTastiera = new Scanner(System.in);
                                message = inputTastiera.nextLine();
                                out.println(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Connection to server lost.");
                socket.close();
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client("127.0.0.1");
        System.out.println("Client created");

        client.play();

        //frame.dispose(); // <--- to close the UI
    }
}