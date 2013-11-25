package server;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Run this shit to make a server.
 * 
 */
public class WhiteboardServer {
    private final ServerSocket serverSocket;
    private final int port = 4444;
    // stores all the whiteboards, maps ID number to Whiteboard for fast lookup
    private final Map<Integer, Whiteboard> whiteboards;

    // stores all the users, maps ID number to Person for fast lookup
    // private final Map<Integer, Person> users;

    // helps in making a unique ID for every new Whiteboard created
    private final AtomicInteger whiteBoardIDCounter;

    /**
     * Creates a new server, with no whiteboards or users, and a reset
     * whiteboard ID counter
     * 
     * @throws IOException
     *             if invalid port
     */
    public WhiteboardServer() throws IOException {
        serverSocket = new ServerSocket(port);
        whiteboards = Collections.synchronizedMap(new HashMap<Integer, Whiteboard>());
        // users = Collections.synchronizedMap(new HashMap<Integer, Person>());
        whiteBoardIDCounter = new AtomicInteger(0);
    }

    /**
     * This gets called when the client initially makes contact with the server.
     * This makes a new Person with a unique ID number and stores them
     * 
     * @param userID
     *            unique ID for the user
     */
    private void newUser(int userID) {
        // users.put(userID, new Person(userID));
    }

    /**
     * Make a new Whiteboard, add it to the whiteboards list, and switch to it
     * 
     * @param boardName
     *            name of whiteboard, can't be empty
     * @param color
     *            background color
     * @param userID
     *            ID of user that called this
     * @param userName
     *            name of user, can't be empty
     * @return list of all actions of whiteboard (none so far)
     */
    private String createWhiteboard(String boardName, Color color, int userID, String userName) {
        int whiteboardID = whiteBoardIDCounter.getAndIncrement();
        whiteboards.put(whiteboardID, new Whiteboard(whiteboardID, boardName, color));
        selectWhiteboard(whiteboardID, userID, userName);

        return listAllActions(whiteboardID);
    }

    /**
     * User has selected this Whiteboard, so add them to the user list and send
     * them info. Also must have already chosen user name, so set that too.
     * 
     * @param whiteboardID
     *            id of whiteboard
     * @param userID
     *            id of user
     * @param userName
     *            name of user, can't be empty
     * @return list of all actions of the whiteboard
     */
    private String selectWhiteboard(int whiteboardID, int userID, String userName) {
        // Person newArtist = users.get(userID);
        // newArtist.setName(userName)
        // whiteboards.get(whiteboardID).addUser(newArtist)

        return listAllActions(whiteboardID);
    }

    /**
     * Lists all of the whiteboard names
     * 
     * @return a string of all whiteboard names, separated by spaces
     */
    private String listWhiteboards() {
        StringBuilder boards = new StringBuilder();
        for (Whiteboard w : whiteboards.values()) {
            boards.append(w.getName() + " ");
        }
        return boards.toString().trim();
    }

    /**
     * Lists all of the user names in a particular whiteboard
     * 
     * @return a string of all user names, separated by spaces
     */
    private String listUsers(int boardID) {
        StringBuilder users = new StringBuilder();
        Whiteboard board = whiteboards.get(boardID);
        // for (User u : board.users()) {
        // users.append(u.getName() + " ");
        // }
        return users.toString().trim();
    }

    /**
     * This lists all actions that have been performed on a particular board.
     * Format: "USERS" USER_NAME USER_NAME... "ARTS" "DRAW" ARTSY_METER COLOR_R
     * COLOR_G COLOR_B STROKE X1 Y1 X2 Y2... "DRAW" ARTSY_METER COLOR_R COLOR_G
     * COLOR_B STROKE X1 Y1 X2 Y2...
     * 
     * @param boardID
     *            id of the board in question
     * @return string that lists all actions and users in a whiteboard
     */
    private String listAllActions(int boardID) {
        StringBuilder actions = new StringBuilder();

        // add users
        actions.append("USERS " + listUsers(boardID));

        // add actions
        actions.append(" ARTS ");
        Whiteboard board = whiteboards.get(boardID);
        // for (Art a : board.arts()) {
        // actions.append(a + " ");
        // }
        return actions.toString().trim();
    }

    /**
     * Converts a string array of commands to draw to an Art, and adds that to
     * both the Whiteboard and the Person
     * 
     * @param input
     *            "DRAW" WB_ID USER_ID COLOR_R COLOR_G COLOR_B STROKE X1 Y1 X2
     *            Y2...
     */
    private void draw(String[] input) {
        Whiteboard board = whiteboards.get(new Integer(input[1]));
        // Person person = users.get(new Integer(input[2]));
        Color color = new Color(new Integer(input[3]), new Integer(input[4]), new Integer(input[5]));

        // TODO:...make an Art
        // person.addArt(art);
        // board.addArt(art);
    }

    /**
     * Change the background color of the board
     * 
     * @param boardID
     *            board in question
     * @param color
     *            color to change background to
     */
    private void changeBackgroundColor(int boardID, Color color) {
        Whiteboard board = whiteboards.get(boardID);
        // board.setBackgroundColor(color);
    }

    /**
     * Run the server, listening for client connections and handling them. Never
     * returns unless an exception is thrown. This was stolen from Minesweeper!
     * 
     * @throws IOException
     *             if the main server socket is broken (IOExceptions from
     *             individual clients do *not* terminate serve())
     */
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();

            // start a new thread every time!
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    handleConnection(socket);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        }
    }

    /**
     * Handle a single client connection. Returns when client disconnects. This
     * was stolen from Minesweeper!
     * 
     * @param socket
     *            socket where the client is connected
     */
    private void handleConnection(Socket socket) {
        // try with multiple resources! this is so hot
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String response = handleRequest(line);

                // disconnect!
                if (response.equals("BYE")) {
                    return;
                }

                if (!response.isEmpty()) {
                    out.write(response);
                    out.flush();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Respond to the client's request appropriately, and send information back.
     * 
     * Possible inputs: (1) initial connect message ("HELLO" USER_ID), (2)
     * select whiteboard ("SELECT" WB_ID USER_ID USER_NAME), (3) make new
     * whiteboard and select it ("NEW" WB_NAME COLOR_R COLOR_G COLOR_B USER_ID
     * USER_NAME), (4) new draw actions ("DRAW" WB_ID USER_ID COLOR_R COLOR_G
     * COLOR_B STROKE X1 Y1 X2 Y2...), (5) change whiteboard bg color ("BG"
     * WB_ID COLOR_R COLOR_G COLOR_B), (6) disconnect message ("BYE" USER_ID)
     * 
     * Possible outputs: (1) whiteboard names (WB_NAME WB_NAME...) to new
     * client, ("NEWUSER" USER_NAME) to others, (2)-(3) whiteboard specs
     * ("USERS" USER_NAME USER_NAME... "ARTS" DRAW_ACTIONS), (4) new draw
     * actions by others ("DRAW" ARTSY_METER COLOR_R COLOR_G COLOR_B STROKE X1
     * Y1 X2 Y2...), (5) change whiteboard bg color ("BG" COLOR_R COLOR_G
     * COLOR_B), (6) user leaves ("BYEUSER" USER_NAME)
     * 
     * @param input
     *            the client's request
     * @return response to give to the client
     */
    private String handleRequest(String input) {
        System.out.println(input);
        String[] inputSplit = input.split(" ");

        // initial connect message
        // "HELLO" USER_ID
        if (inputSplit[0].equals("HELLO")) {
            newUser(new Integer(inputSplit[1]));
            // TODO: alert all other clients
            return listWhiteboards();
        }

        // select a whiteboard
        // "SELECT" WB_ID USER_ID USER_NAME
        else if (inputSplit[0].equals("SELECT")) {
            return selectWhiteboard(new Integer(inputSplit[1]), new Integer(inputSplit[2]), inputSplit[3]);
        }

        // make new whiteboard and select it
        // "NEW" WB_NAME COLOR_R COLOR_G COLOR_B USER_ID USER_NAME
        else if (inputSplit[0].equals("NEW")) {
            // make a color from RGB values
            Color color = new Color(new Integer(inputSplit[2]), new Integer(inputSplit[3]), new Integer(inputSplit[4]));
            return createWhiteboard(inputSplit[1], color, new Integer(inputSplit[5]), inputSplit[6]);
        }

        // new draw actions
        // "DRAW" WB_ID USER_ID COLOR_R COLOR_G COLOR_B STROKE X1 Y1 X2 Y2...
        else if (inputSplit[0].equals("DRAW")) {
            draw(inputSplit);
            // TODO: announce to others
        }

        // change whiteboard bg color
        // "BG" WB_ID COLOR_R COLOR_G COLOR_B
        else if (inputSplit[0].equals("BG")) {
            // make a color from RGB values
            Color color = new Color(new Integer(inputSplit[2]), new Integer(inputSplit[3]), new Integer(inputSplit[4]));
            changeBackgroundColor(new Integer(inputSplit[1]), color);
            // TODO: announce to others
        }

        // disconnect message
        // "BYE" USER_ID
        else if (inputSplit[0].equals("BYE")) {
            // TODO: announce to others
            return "BYE"; // poison pill
        }

        // What the fuck did you put in here
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