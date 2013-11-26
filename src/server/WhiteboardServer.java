package server;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

    private final AtomicInteger whiteBoardIDCounter;
    private final AtomicInteger clientIDCounter;

    // whiteboard id -> whiteboard
    private final Map<Integer, Whiteboard> whiteboards;
    // whiteboard ID -> client IDs
    private final Map<Integer, List<Integer>> whiteboardClients;
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
        whiteboards = Collections.synchronizedMap(new HashMap<Integer, Whiteboard>());

        whiteBoardIDCounter = new AtomicInteger(0);
        clientIDCounter = new AtomicInteger(0);
        names = Collections.synchronizedMap(new HashMap<Integer, String>());
        queues = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<String>>());
        whiteboardClients = Collections.synchronizedMap(new HashMap<Integer, List<Integer>>());
    }

    /**
     * Make a new Whiteboard, add it to the whiteboards list, and switch to it
     * 
     * @param boardName
     *            name of whiteboard, can't be empty
     * @param color
     *            background color
     * @param userName
     *            name of user, can't be empty
     * @param clientID
     *            id of client
     * @return list of all actions of whiteboard (none so far)
     */
    private void createWhiteboard(String boardName, Color color, String userName, int clientID) {
        int boardID = whiteBoardIDCounter.getAndIncrement();
        whiteboards.put(boardID, new Whiteboard(boardID, boardName, color));
        whiteboardClients.put(boardID, new ArrayList<Integer>());
        selectWhiteboard(boardID, userName, clientID);
    }

    /**
     * User has selected this Whiteboard, so add them to the list and send them
     * info. Also must have already chosen user name, so set that too.
     * 
     * @param boardID
     *            id of whiteboard
     * @param userName
     *            name of user, can't be empty
     * @param clientID
     *            id of client
     * @return "USERS" list of users "ARTS" list of pixels and their colors
     */
    private String selectWhiteboard(int boardID, String userName, int clientID) {
        names.put(clientID, userName);

        // subscribe the client to whiteboard events
        whiteboardClients.get(boardID).add(clientID);

        return "USERS " + listUsers(boardID) + " ARTS " + createListOfPixels(boardID);
    }

    /**
     * Converts the board's array of positions and colors to a string of all of
     * the positions and colors (X Y R G B) of the board's pixels separated by
     * spaces
     * 
     * @param boardID
     *            id of the board in question
     * @return string of positions and colors of the pixels of the board
     */
    private String createListOfPixels(int boardID) {
        // Map<List<Integer>, Color> pointsArray =
        // whiteboards.get(boardID);
        Map<List<Integer>, List<Integer>> pointsArray = new HashMap<List<Integer>, List<Integer>>();
        StringBuilder points = new StringBuilder();

        // loop through and add to the string
        for (Entry<List<Integer>, List<Integer>> point : pointsArray.entrySet()) {
            // X Y R G B
            points.append(point.getKey().get(0) + " " + point.getKey().get(1) + " " + point.getValue().get(0) + " "
                    + point.getValue().get(1) + " " + point.getValue().get(2) + " ");
        }

        return points.toString();
    }

    /**
     * Lists all of the whiteboard names and ids
     * 
     * @return a string of all whiteboard names and ids (name followed by
     *         corresponding id), separated by spaces
     */
    private String listWhiteboards() {
        StringBuilder boards = new StringBuilder();

        synchronized (whiteboards) {
            for (Entry<Integer, Whiteboard> w : whiteboards.entrySet()) {
                boards.append(w.getKey() + " " + w.getValue().getName() + " ");
            }
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
        List<Integer> clients = whiteboardClients.get(boardID);

        for (Integer id : clients) {
            users.append(names.get(id) + " ");
        }
        return users.toString().trim();
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
     * Converts
     * 
     * @param input
     *            "DRAW" WB_ID STROKE X Y COLOR_R COLOR_G COLOR_B
     * @return "DRAW" ARTSY_METER STROKE X Y COLOR_R COLOR_G COLOR_B
     */
    private String draw(int boardID, int stroke, int x, int y, int red, int green, int blue) {
        Whiteboard board = whiteboards.get(boardID);
        Color color = new Color(red, green, blue);
        int width = stroke / 2; // integer division

        // board.addPixels(what);
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
     * @param boardID
     *            id of whiteboard in question
     * @param message
     *            message to put on the queues
     */
    private void putOnAllQueuesBut(int clientID, int boardID, String message) {
        List<Integer> clients = whiteboardClients.get(boardID);
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
            System.out.println("waiting");
            final Socket socket = serverSocket.accept();
            System.out.println("got one!");
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
            System.out.println("trying to read in");
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
                    out.write(response);
                    out.flush();
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
     * Possible inputs: (1) initial connect message ("HELLO"), (2) select
     * whiteboard ("SELECT" WB_ID USER_NAME), (3) make new whiteboard and select
     * it ("NEW" WB_NAME COLOR_R COLOR_G COLOR_B USER_NAME), (4) new draw
     * actions ("DRAW" WB_ID STROKE X Y COLOR_R COLOR_G COLOR_B), (5) change
     * whiteboard bg color ("BG" WB_ID COLOR_R COLOR_G COLOR_B), (6) disconnect
     * message ("BYE" WB_ID USER_NAME)
     * 
     * Possible outputs: (1) whiteboard names and ids (WB_NAME WB_ID WB_NAME
     * WB_ID...), (2)-(3) whiteboard specs ("USERS" USER_NAME USER_NAME...
     * "ARTS" DRAW_ACTIONS) to new client, ("NEWUSER" USER_NAME) to others, (4)
     * new draw actions by others ("DRAW" ARTSY_METER STROKE X Y COLOR_R COLOR_G
     * COLOR_B), (5) change whiteboard bg color ("BG" COLOR_R COLOR_G COLOR_B),
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
            // "SELECT" WB_ID USER_NAME
            if (inputSplit[0].equals("SELECT")) {
                clientQueue.put(selectWhiteboard(new Integer(inputSplit[1]), inputSplit[2], clientID));
                putOnAllQueuesBut(clientID, new Integer(inputSplit[1]), "NEWUSER " + inputSplit[2]);
                return;
            }

            // make new whiteboard and select it
            // "NEW" WB_NAME COLOR_R COLOR_G COLOR_B USER_NAME
            if (inputSplit[0].equals("NEW")) {
                // make a color from RGB values
                Color color = new Color(new Integer(inputSplit[2]), new Integer(inputSplit[3]), new Integer(
                        inputSplit[4]));
                createWhiteboard(inputSplit[1], color, inputSplit[5], clientID);
                return;
            }

            // new draw actions
            // "DRAW" WB_ID STROKE X1 Y1 COLOR_R COLOR_G COLOR_B
            if (inputSplit[0].equals("DRAW")) {
                String draw = draw(new Integer(inputSplit[1]), new Integer(inputSplit[2]), new Integer(inputSplit[3]),
                        new Integer(inputSplit[4]), new Integer(inputSplit[5]), new Integer(inputSplit[6]),
                        new Integer(inputSplit[7]));
                putOnAllQueuesBut(clientID, new Integer(inputSplit[1]), draw);
                return;
            }

            // change whiteboard bg color
            // "BG" WB_ID COLOR_R COLOR_G COLOR_B
            if (inputSplit[0].equals("BG")) {
                // make a color from RGB values
                Color color = new Color(new Integer(inputSplit[2]), new Integer(inputSplit[3]), new Integer(
                        inputSplit[4]));
                changeBackgroundColor(new Integer(inputSplit[1]), color);
                putOnAllQueuesBut(clientID, new Integer(inputSplit[1]), "BG " + inputSplit[2] + " " + inputSplit[3]
                        + " " + inputSplit[4]);
                return;
            }

            // disconnect message
            // "BYE" WB_ID USER_NAME
            if (inputSplit[0].equals("BYE")) {
                // un-subscribe the client from whiteboard events
                List<Integer> unsubList = whiteboardClients.get(new Integer(inputSplit[1]));
                unsubList.remove(unsubList.indexOf(clientID));

                putOnAllQueuesBut(clientID, new Integer(inputSplit[1]), "BYEUSER " + inputSplit[2]);
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