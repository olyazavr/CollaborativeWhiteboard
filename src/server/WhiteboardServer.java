package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Threadsafe, mutable server for collaborative whiteboard-ing.
 * 
 * This is threadsafe because all of the shared variables are private, final,
 * thread-safe, and locked during iteration. Information to be sent back to the
 * client is put on blocking queues, and is processed in an orderly manner.
 * Every client has two threads- one for input and one for output, and the only
 * shared information is a final socket and an atomic integer. The server knows
 * nothing about the GUI or the client, except for the messages the client
 * sends.
 * 
 * Port is set to 4444 by default, and every whiteboard and client are given
 * unique ID numbers.
 * 
 */
public class WhiteboardServer {
    private final ServerSocket serverSocket;
    private final int port = 4444;

    private final AtomicInteger clientIDCounter;

    // whiteboard name -> whiteboard
    private final Map<String, Whiteboard> whiteboards;
    // whiteboard name -> client IDs
    private final Map<String, List<Integer>> whiteboardClients;
    // client ID -> name
    private final Map<Integer, String> names;
    // client ID -> queue
    private final Map<Integer, BlockingQueue<String>> queues;

    /**
     * Creates a new server, with no whiteboards or users
     * 
     * @throws IOException
     *             if invalid port
     */
    public WhiteboardServer() throws IOException {
        serverSocket = new ServerSocket(port);
        whiteboards = Collections.synchronizedMap(new HashMap<String, Whiteboard>());

        clientIDCounter = new AtomicInteger(0);
        names = Collections.synchronizedMap(new HashMap<Integer, String>());
        queues = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<String>>());
        whiteboardClients = Collections.synchronizedMap(new HashMap<String, List<Integer>>());
    }

    /**
     * Make a new Whiteboard, add it to the whiteboards list
     * 
     * @param boardName
     *            name of whiteboard, can't be empty, must be unique
     * @param red
     *            amount of red in bg (0-255)
     * @param green
     *            amount of green in bg (0-255)
     * @param blue
     *            amount of blue in bg (0-255)
     */
    private void createWhiteboard(String boardName, int red, int green, int blue) {
        whiteboards.put(boardName, new Whiteboard(boardName, Arrays.asList(red, green, blue)));
        whiteboardClients.put(boardName, new ArrayList<Integer>());
    }

    /**
     * User has selected this Whiteboard, so add them to the list and send them
     * info. Also must have already chosen user name, so set that too.
     * 
     * @param boardName
     *            name of whiteboard
     * @param userName
     *            name of user, can't be empty
     * @param clientID
     *            id of client
     * @return "USERS" list of users "PIXELS " list of pixels and their colors
     */
    private String selectWhiteboard(String boardName, String userName, int clientID) {
        names.put(clientID, userName);

        // subscribe the client to whiteboard events
        whiteboardClients.get(boardName).add(clientID);

        return "USERS " + listUsers(boardName) + " PIXELS " + createListOfPixels(boardName);
    }

    /**
     * Converts the board's array of positions and colors to a string of all of
     * the positions and colors (X Y R G B) of the board's pixels separated by
     * spaces
     * 
     * @param boardName
     *            name of the board in question
     * @return string of positions and colors of the pixels of the board
     */
    private String createListOfPixels(String boardName) {
        Whiteboard board = whiteboards.get(boardName);
        return board.createListOfPixels();
    }

    /**
     * Lists all of the whiteboard names
     * 
     * @return a string of all whiteboard names separated by spaces
     */
    private String listWhiteboards() {
        StringBuilder boards = new StringBuilder();

        synchronized (whiteboards) {
            // make a default board
            if (whiteboards.isEmpty()) {
                // bg is all white
                createWhiteboard("Default", 255, 255, 255);
            }

            // get all whiteboard names
            for (String w : whiteboards.keySet()) {
                boards.append(w + " ");
            }
        }

        return boards.toString().trim();
    }

    /**
     * Lists all of the user names in a particular whiteboard
     * 
     * @param boardName
     *            name of whiteboard in question
     * @return a string of all user names, separated by spaces
     */
    private String listUsers(String boardName) {
        StringBuilder users = new StringBuilder();
        List<Integer> clients = whiteboardClients.get(boardName);

        // get all of the names associated with this whiteboard
        for (Integer id : clients) {
            users.append(names.get(id) + " ");
        }

        return users.toString().trim();
    }

    /**
     * Change the background color of the board
     * 
     * @param boardName
     *            board in question
     * @param red
     *            amount of red (0-255)
     * @param green
     *            amount of green (0-255)
     * @param blue
     *            amount of blue (0-255)
     */
    private void changeBackgroundColor(String boardName, int red, int green, int blue) {
        Whiteboard board = whiteboards.get(boardName);
        board.setBackgroundColor(red, green, blue);
    }

    /**
     * Saves the drawing action to the whiteboard and returns the message to
     * send to all other clients (with the artsy meter)
     * 
     * @param boardName
     *            name of board in question
     * @param stroke
     *            >1, MUST BE ODD, thickness of the draw action
     * @param x
     *            x-coordinate
     * @param y
     *            y-coordinate
     * @param red
     *            amount of red (0-255)
     * @param green
     *            amount of green (0-255)
     * @param blue
     *            amount of blue (0-255)
     * @return "DRAW" ARTSY_METER STROKE X Y COLOR_R COLOR_G COLOR_B
     */
    private String draw(String boardName, int stroke, int x, int y, int red, int green, int blue) {
        Whiteboard board = whiteboards.get(boardName);
        int width = stroke / 2; // integer division

        // search for width around the x,y center
        for (int j = y - width; j <= y + width; ++j) {
            for (int i = x - width; i <= x + width; ++i) {
                // if within board
                if (i > 0 && j > 0 && i <= 800 && j <= 600) {
                    board.setColor(i, j, red, green, blue);
                }
            }
        }

        // int artsy = board.getArtsy();
        int artsy = 5;

        return "DRAW " + artsy + " " + stroke + " " + x + " " + y + " " + red + " " + blue;
    }

    /**
     * Put the message on all of the queues of clients that are in the
     * particular whiteboard except the specified client.
     * 
     * @param clientID
     *            id of client not to recieve message
     * @param boardName
     *            name of whiteboard in question
     * @param message
     *            message to put on the queues
     */
    private void putOnAllQueuesBut(int clientID, String boardName, String message) {
        List<Integer> clients = whiteboardClients.get(boardName);
        for (int id : clients) {
            if (clientID != id) {
                try {
                    queues.get(id).put(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Run the server, listening for client connections and handling them. Never
     * returns unless an exception is thrown.
     * 
     * @throws IOException
     *             if the main server socket is broken (IOExceptions from
     *             individual clients do *not* terminate serve())
     */
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            final int clientID = clientIDCounter.getAndIncrement();
            queues.put(clientID, new LinkedBlockingQueue<String>());

            // start a new thread for input
            Thread inputThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // outputThread closes this socket
                    handleInput(socket, clientID);
                }
            });

            inputThread.start();

            // start a new thread for output
            Thread outputThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    handleOutput(socket, clientID);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            outputThread.start();
        }
    }

    /**
     * Handle input from a single client connection
     * 
     * @param socket
     *            socket where the client is connected
     * @param clientID
     *            id of client
     */
    private void handleInput(Socket socket, int clientID) {
        // try with resources!
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                System.out.println("read in " + line);
                handleRequest(line, clientID);
            }

        } catch (SocketException e) {
            // this is ok, the other thread closed the socket
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delivers output to the particular socket connection. Returns when client
     * disconnects.
     * 
     * @param socket
     *            socket where the client is connected
     * @param clientID
     *            id of client
     */
    private void handleOutput(Socket socket, int clientID) {
        // try with resources!
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String response = "";

            while (!"BYE".equals(response)) {
                // take the latest output, deliver it
                response = queues.get(clientID).take();
                System.out.println("got response " + response);

                if (!response.isEmpty()) {
                    out.println(response);
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Respond to the client's request appropriately, and send information back
     * (via queues).
     * 
     * Possible inputs:
     * 
     * (1) initial connect message ("HELLO"),
     * 
     * (2) select whiteboard ("SELECT" WB_NAME USER_NAME),
     * 
     * (3) make new whiteboard and select it ("NEW" WB_NAME COLOR_R COLOR_G
     * COLOR_B USER_NAME),
     * 
     * (4) new draw actions ("DRAW" WB_NAME STROKE X Y COLOR_R COLOR_G COLOR_B),
     * 
     * (5) change whiteboard bg color ("BG" WB_NAME COLOR_R COLOR_G COLOR_B),
     * 
     * (6) disconnect message ("BYE" WB_NAME USER_NAME)
     * 
     * Possible outputs:
     * 
     * (1) whiteboard names (WB_NAME WB_NAME...),
     * 
     * (2)-(3) whiteboard specs ("USERS" USER_NAME USER_NAME... "PIXELS" X1 Y1
     * COLOR_R1 COLOR_G1 COLOR_B1 X2 Y2 COLOR_R2 COLOR_G2 COLOR_B2...) to new
     * client, ("NEWUSER" USER_NAME) to others,
     * 
     * (4) new draw actions by others ("DRAW" ARTSY_METER STROKE X Y COLOR_R
     * COLOR_G COLOR_B),
     * 
     * (5) change whiteboard bg color ("BG" COLOR_R COLOR_G COLOR_B),
     * 
     * (6) user leaves ("BYEUSER" USER_NAME)
     * 
     * @param input
     *            the client's request
     * @param clientID
     *            id of client making the request
     */
    private void handleRequest(String input, int clientID) {
        String[] inputSplit = input.split(" ");
        BlockingQueue<String> clientQueue = queues.get(clientID);

        // try to put on the queues
        try {
            // initial connect message
            // "HELLO"
            if (inputSplit[0].equals("HELLO")) {
                clientQueue.put(listWhiteboards());
                return;
            }

            // select a whiteboard
            // "SELECT" WB_NAME USER_NAME
            if (inputSplit[0].equals("SELECT")) {
                String boardName = inputSplit[1];
                String userName = inputSplit[2];

                // select whiteboard, tell others that there's a new user
                clientQueue.put(selectWhiteboard(boardName, userName, clientID));
                putOnAllQueuesBut(clientID, boardName, "NEWUSER " + userName);
                return;
            }

            // make new whiteboard and select it
            // "NEW" WB_NAME COLOR_R COLOR_G COLOR_B USER_NAME
            if (inputSplit[0].equals("NEW")) {
                String boardName = inputSplit[1];
                int red = new Integer(inputSplit[2]);
                int green = new Integer(inputSplit[3]);
                int blue = new Integer(inputSplit[4]);
                String userName = inputSplit[5];

                // make a new whiteboard
                createWhiteboard(boardName, red, green, blue);
                // we don't care about the result of select, because the board
                // is brand new
                selectWhiteboard(boardName, userName, clientID);
                return;
            }

            // new draw actions
            // "DRAW" WB_NAME STROKE X Y COLOR_R COLOR_G COLOR_B
            if (inputSplit[0].equals("DRAW")) {
                String boardName = inputSplit[1];
                int stroke = new Integer(inputSplit[2]);
                int x = new Integer(inputSplit[3]);
                int y = new Integer(inputSplit[4]);
                int red = new Integer(inputSplit[5]);
                int green = new Integer(inputSplit[6]);
                int blue = new Integer(inputSplit[7]);

                // draw has artsy meter on it "DRAW" ARTSY_METER STROKE X Y
                // COLOR_R COLOR_G COLOR_B
                String draw = draw(boardName, stroke, x, y, red, green, blue);
                putOnAllQueuesBut(clientID, boardName, draw);
                return;
            }

            // change whiteboard bg color
            // "BG" WB_NAME COLOR_R COLOR_G COLOR_B
            if (inputSplit[0].equals("BG")) {
                String boardName = inputSplit[1];
                int red = new Integer(inputSplit[2]);
                int green = new Integer(inputSplit[3]);
                int blue = new Integer(inputSplit[4]);

                // change color, inform others
                changeBackgroundColor(boardName, red, green, blue);
                putOnAllQueuesBut(clientID, boardName, "BG " + red + " " + green + " " + blue);
                return;
            }

            // disconnect message
            // "BYE" WB_NAME USER_NAME
            if (inputSplit[0].equals("BYE")) {
                String boardName = inputSplit[1];
                String userName = inputSplit[2];

                // un-subscribe the client from whiteboard events
                List<Integer> unsubList = whiteboardClients.get(boardName);
                unsubList.remove(unsubList.indexOf(clientID));
                names.remove(clientID);
                queues.remove(clientID);

                // tell others the user is gone
                putOnAllQueuesBut(clientID, boardName, "BYEUSER " + userName);
                clientQueue.put("BYE"); // poison pill
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // things that don't adhere to the grammar were put in here, muy bad
        throw new UnsupportedOperationException();
    }

    /**
     * Starts a server with fresh memory (no whiteboards)
     */
    public static void main(String[] args) {
        // try to initialize the server with port 4444, this should def work,
        // try/catch just to soothe java's nerves
        try {
            WhiteboardServer server = new WhiteboardServer();
            server.serve();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}