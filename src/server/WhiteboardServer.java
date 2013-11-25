package server;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Run this shit to make a server.
 * 
 */
public class WhiteboardServer {
    // stores all the whiteboards, maps ID number to Whiteboard for fast lookup
    private final Map<Integer, Whiteboard> whiteboards = Collections
            .synchronizedMap(new HashMap<Integer, Whiteboard>());
    // helps in making a unique ID for every new Whiteboard created
    private final AtomicInteger whiteBoardIDCounter = new AtomicInteger(0);

    /**
     * Make a new Whiteboard, add it to the whiteboards list, and switch to it
     * 
     * @param name
     *            name of whiteboard
     * @param color
     *            background color
     * @param userID
     *            ID of user that called this
     */
    private void createWhiteboard(String name, Color color, int userID) {
        int whiteboardID = whiteBoardIDCounter.getAndIncrement();
        whiteboards.put(whiteboardID, new Whiteboard(whiteboardID, name, color));
        selectWhiteboard(whiteboardID, userID);
    }

    /**
     * User has selected this Whiteboard, so add them to the user list and send
     * them info
     * 
     * @param whiteboardID
     *            id of whiteboard
     * @param userID
     *            id of user
     */
    private void selectWhiteboard(int whiteboardID, int userID) {

    }

    /**
     * Starts a server with fresh memory (no whiteboards)
     */
    public static void main(String[] args) {

    }

}