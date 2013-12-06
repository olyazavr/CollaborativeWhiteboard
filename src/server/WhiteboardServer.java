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
 * sends. All modifications to queues or Whiteboards is locked on that queue or
 * whiteboard.
 * 
 * Port is set to 4444 by default, and every client is given unique ID numbers.
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
    // all the clients that are Artists
    private final List<Integer> artistClients;

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
        artistClients = Collections.synchronizedList(new ArrayList<Integer>());
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
     * @return
     */
    private void createWhiteboard(String boardName, int red, int green, int blue) {
        Whiteboard board = new Whiteboard(boardName, Arrays.asList(red, green, blue));
        synchronized (board) {
            whiteboards.put(boardName, board);
            whiteboardClients.put(boardName, new ArrayList<Integer>());
        }
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
     * @return BG_RED BG_GREEN BG_BLUE ARTSY_METER "USERS" list of users
     *         "ACTIONS " list of pixels and their colors
     */
    private String selectWhiteboard(String boardName, String userName, int clientID) {
        names.put(clientID, userName);
        Whiteboard board = whiteboards.get(boardName);
        int artsy;
        String bg;
        String users;
        String actions;

        // subscribe the client to whiteboard events
        whiteboardClients.get(boardName).add(clientID);
        synchronized (board) {
            artsy = board.calculateArtsy();
            bg = board.getBackgroundColorString();
            users = listUsers(boardName);
            actions = createListOfActions(boardName);
        }

        return bg + " " + artsy + " USERS " + users + " ACTIONS " + actions;
    }

    /**
     * Converts the board's actions to a string (X1 Y1 X2 Y2 STROKE R G B)
     * separated by spaces
     * 
     * 
     * @param boardName
     *            name of the board in question
     * @return string of actions of the board, separated by spaces
     */
    private String createListOfActions(String boardName) {
        Whiteboard board = whiteboards.get(boardName);
        return board.createStringOfActions();
    }

    /**
     * Lists all of the whiteboard names
     * 
     * @return "LIST" and a string of all whiteboard names separated by spaces
     */
    private String listWhiteboards() {
        StringBuilder boards = new StringBuilder();

        // iterating is not safe
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

        return "LIST " + boards.toString().trim();
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

        // iterating is not safe
        synchronized (clients) {
            // get all of the names associated with this whiteboard
            for (Integer id : clients) {
                users.append(names.get(id) + " ");
            }
        }
        return users.toString().trim();
    }

    /**
     * Change the background color of the board
     * 
     * @param boardName
     *            name of board in question
     * @param red
     *            amount of red (0-255)
     * @param green
     *            amount of green (0-255)
     * @param blue
     *            amount of blue (0-255)
     */
    private void changeBackgroundColor(String boardName, int red, int green, int blue) {
        Whiteboard board = whiteboards.get(boardName);
        synchronized (board) {
            board.setBackgroundColor(red, green, blue);
        }
    }

    /**
     * Clears everything from the board, leaving only the background color
     * 
     * @param boardName
     *            name of board in question
     */
    private void clearBoard(String boardName) {
        Whiteboard board = whiteboards.get(boardName);
        synchronized (board) {
            board.clear();
        }
    }

    /**
     * Saves the drawing action to the whiteboard and returns the message to
     * send to all other clients (with the artsy meter)
     * 
     * @param boardName
     *            name of board in question
     * @param x1
     *            starting x
     * @param y1
     *            starting y
     * @param x2
     *            ending x
     * @param y2
     *            ending y
     * @param stroke
     *            stroke size
     * @param red
     *            amount of red (0-255)
     * @param green
     *            amount of green (0-255)
     * @param blue
     *            amount of blue (0-255)
     * @return "DRAW" ARTSY_METER X1 Y1 X2 Y2 STROKE COLOR_R COLOR_G COLOR_B
     */
    private String draw(String boardName, int x1, int y1, int x2, int y2, int stroke, int red, int green, int blue) {
        Whiteboard board = whiteboards.get(boardName);
        int artsy;

        synchronized (board) {
            board.addAction(x1, y1, x2, y2, stroke, red, green, blue);
            artsy = board.calculateArtsy();
        }

        return "DRAW " + artsy + " " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + stroke + " " + red + " " + green
                + " " + blue;
    }

    /**
     * Fixes duplicate names by appending a (1) at the end, and then, if there
     * is already a (1), change it to (2), and so on.
     * 
     * @param name
     *            name to fix
     * @return fixed name (with a (1) or (2), etc) at end
     */
    private String fixDuplicate(String name) {
        if (whiteboards.containsKey(name)) {
            // does this even have parens (have we fixed this before)
            if (name.contains("(") && name.contains(")")) {
                // make sure we get the last ones
                int start = name.lastIndexOf('(');
                int end = name.lastIndexOf(')');

                // check to make sure the thing in parens is a number (so, we've
                // already made a duplicate copy)
                String possibleNum = name.substring(start + 1, end);
                if (possibleNum.matches("[0-9]+")) {
                    // increment!
                    int nextNum = new Integer(possibleNum) + 1;

                    // recurse because we may already have "name(1)"
                    // much recursive. such 006. wow.
                    return fixDuplicate(name.substring(0, start) + "(" + nextNum + ")");
                }
            }
            // nope, we haven't fixed this before, just add a (1)
            return fixDuplicate(name + "(1)");
        }
        return name;
    }

    /**
     * Put the message on all of the queues of clients that are in the
     * particular whiteboard except the specified client. If we are sending to
     * Artists, leave boardName null.
     * 
     * @param clientID
     *            id of client not to receive message
     * @param boardName
     *            name of whiteboard in question, or null if we are looking for
     *            Artists
     * @param message
     *            message to put on the queues
     */
    private void putOnAllQueuesBut(int clientID, String boardName, String message) {
        List<Integer> clients;
        if (boardName != null) {
            // we are looking for Canvases
            clients = whiteboardClients.get(boardName);
        } else {
            // we are looking for Artists
            clients = artistClients;
        }

        // iterating is not safe
        synchronized (clients) {
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
            String response;
            while (!(response = queues.get(clientID).take()).equals("BYE")) {
                // take the latest output, deliver it
                if (!response.isEmpty()) {
                    out.println(response);
                }
            }

            // remove the queue when we're done
            if (artistClients.contains(clientID)) { // remove if artist
                artistClients.remove(artistClients.indexOf(clientID));
            } else { // remove if canvas
                names.remove(clientID);
            }
            queues.remove(clientID);

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
     * (3) make new whiteboard ("NEW" WB_NAME COLOR_R COLOR_G COLOR_B),
     * 
     * (4) new draw actions ("DRAW" WB_NAME X1 Y1 X1 Y2 STROKE COLOR_R COLOR_G
     * COLOR_B),
     * 
     * (5) change whiteboard bg color and clear ("BG" WB_NAME COLOR_R COLOR_G
     * COLOR_B),
     * 
     * (6) clear everything from board ("CLEAR" WB_NAME),
     * 
     * (7) disconnect message ("BYE" WB_NAME USER_NAME)
     * 
     * (7) disconnect message for Artist ("BYEARTIST")
     * 
     * Possible outputs:
     * 
     * (1) whiteboard names (WB_NAME WB_NAME...),
     * 
     * (2) whiteboard specs (BG_RED BG_GREEN BG_BLUE ARTSY_METER "USERS"
     * USER_NAME USER_NAME... "ACTIONS" X1 Y1 X1 Y2 STROKE COLOR_R COLOR_G
     * COLOR_B X1 Y1 X1 Y2 STROKE COLOR_R COLOR_G COLOR_B...) to new client,
     * ("NEWUSER" USER_NAME) to others,
     * 
     * (3) announce a new whiteboard to everyone ("NEWBOARD" WB_NAME), send back
     * the possibly new name if there were duplicates (ie. a 1 may be added,
     * then a 2, etc) ("NEWNAME" name)
     * 
     * (4) new draw actions ("DRAW" ARTSY_METER X1 Y1 X1 Y2 STROKE COLOR_R
     * COLOR_G COLOR_B),
     * 
     * (5) change whiteboard bg color ("BG" COLOR_R COLOR_G COLOR_B),
     * 
     * (6) clear everything from board ("CLEAR"),
     * 
     * (7) user leaves ("BYEUSER" USER_NAME) to everyone but the user
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
                queues.put(clientID, new LinkedBlockingQueue<String>());
                artistClients.add(clientID);
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
            // "NEW" WB_NAME COLOR_R COLOR_G COLOR_B
            if (inputSplit[0].equals("NEW")) {
                String boardName = inputSplit[1];
                int red = new Integer(inputSplit[2]);
                int green = new Integer(inputSplit[3]);
                int blue = new Integer(inputSplit[4]);

                // ensure name is unique
                synchronized (whiteboards) {
                    // if name taken, fix it
                    boardName = fixDuplicate(boardName);

                    // make a new whiteboard
                    createWhiteboard(boardName, red, green, blue);
                    // tell all Artists there's a new board and tell the
                    // origin what the new name is (it may have been changed)
                    clientQueue.put("NEWNAME " + boardName);
                    putOnAllQueuesBut(clientID, null, "NEWBOARD " + boardName);

                    // the Artist is leaving, so un-subscribe the client
                    // from new whiteboard events
                    clientQueue.put("BYE"); // poison pill
                    return;
                }
            }

            // new draw actions
            // "DRAW" WB_NAME X1 Y1 X2 Y2 STROKE COLOR_R COLOR_G COLOR_B
            if (inputSplit[0].equals("DRAW")) {
                String boardName = inputSplit[1];
                int x1 = new Integer(inputSplit[2]);
                int y1 = new Integer(inputSplit[3]);
                int x2 = new Integer(inputSplit[4]);
                int y2 = new Integer(inputSplit[5]);
                int stroke = new Integer(inputSplit[6]);
                int red = new Integer(inputSplit[7]);
                int green = new Integer(inputSplit[8]);
                int blue = new Integer(inputSplit[9]);

                // draw has artsy meter on it "DRAW" ARTSY_METER X1 Y1 X2 Y2
                // STROKE COLOR_R COLOR_G COLOR_B
                String draw = draw(boardName, x1, y1, x2, y2, stroke, red, green, blue);
                putOnAllQueuesBut(-1, boardName, draw); // put on all queues
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
                // clear the board, but this doesn't need to be announced
                clearBoard(boardName);
                // put on all queues
                putOnAllQueuesBut(-1, boardName, "BG " + red + " " + green + " " + blue);
                return;
            }

            // clear everything from board
            // "CLEAR" WB_NAME
            if (inputSplit[0].equals("CLEAR")) {
                String boardName = inputSplit[1];

                // change color, inform others
                clearBoard(boardName);
                putOnAllQueuesBut(-1, boardName, "CLEAR"); // put on all queues
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

                // tell others the user is gone
                putOnAllQueuesBut(clientID, boardName, "BYEUSER " + userName);
                clientQueue.put("BYE"); // poison pill
                return;
            }

            // disconnect message for Artist
            // "BYEARTIST"
            if (inputSplit[0].equals("BYEARTIST")) {
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