package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Possible outputs: (1) initial connect message ("HELLO"), (2) select
 * whiteboard ("SELECT" WB_ID USER_NAME), (3) make new whiteboard and select it
 * ("NEW" WB_NAME COLOR_R COLOR_G COLOR_B USER_NAME), (4) new draw actions
 * ("DRAW" WB_ID COLOR_R COLOR_G COLOR_B STROKE X1 Y1 X2 Y2...), (5) change
 * whiteboard bg color ("BG" WB_ID COLOR_R COLOR_G COLOR_B), (6) disconnect
 * message ("BYE" WB_ID USER_NAME)
 * 
 * Possible inputs: (1) whiteboard names and ids (WB_NAME WB_ID WB_NAME
 * WB_ID...), (2)-(3) whiteboard specs ("USERS" USER_NAME USER_NAME... "ARTS"
 * DRAW_ACTIONS) to new client, ("NEWUSER" USER_NAME) to others, (4) new draw
 * actions by others ("DRAW" ARTSY_METER COLOR_R COLOR_G COLOR_B STROKE X1 Y1 X2
 * Y2...), (5) change whiteboard bg color ("BG" COLOR_R COLOR_G COLOR_B), (6)
 * user leaves ("BYEUSER" USER_NAME)
 */
public class Artist {
    private final Socket socket;
    private final int port = 4444;
    // always send the server the whiteboardID, not its name
    private final int whiteboardID;

    public Artist() throws UnknownHostException, IOException {
        String ip = "localhost";
        socket = new Socket(ip, port);

        // try with multiple resources! this is so hot
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // send initial hello to get whiteboards
            out.println("HELLO");
            System.out.println("wrote hello!");

            // retrieve the list of whiteboards
            Map<Integer, String> whiteboardList = new HashMap<Integer, String>();
            String[] input = in.readLine().split(" ");
            System.out.println("read something in!");
            for (int i = 1; i < input.length; i += 2) {
                System.out.println(input[i - 1] + " " + input[i]);
                whiteboardList.put(new Integer(input[i - 1]), input[i]);
            }

            // select the whiteboard (name) we want, give the server the id of
            // it (look it up) if we're selecting (not creating)
            whiteboardID = 0;

        }
    }

    public static void main(String[] args) {
        try {
            Artist artist = new Artist();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
