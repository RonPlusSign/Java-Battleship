import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private Scanner in;
    private PrintWriter out;

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
     * The first message will be a "WELCOME" message in which we receive our
     * mark. Then we go into a loop listening for any of the other messages,
     * and handling each message appropriately. The "VICTORY", "DEFEAT", "TIE",
     *  and "OTHER_PLAYER_LEFT" messages will ask the user whether or not to
     * play another game. If the answer is no, the loop is exited and the server
     * is sent a "QUIT" message.
     */
    public void play() throws Exception {
        System.out.println("PLAY");

        while(true){
            String message = "";
            while(!message.contains("Your move")){  //messages
                if(in.hasNextLine()){
                    message = in.nextLine();
                    System.out.println(message);
                }
            }

            try {
                Scanner inputTastiera = new Scanner(System.in);
                message = inputTastiera.nextLine();
                System.out.println(message);
                out.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client("127.0.0.1");
        System.out.println("Client created");

        client.play();

        //when game is finished:
        client.socket.close();

        //frame.dispose(); // <--- to close the UI
    }
}