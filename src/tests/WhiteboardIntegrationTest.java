package tests;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.Test;

import server.WhiteboardServer;

/**
 * Testing strategy: test all of the possible things the client and server can
 * send to each other, ensuring that the right message gets sent to the right
 * clients and in the right order. Partition on who should get the message (ie.
 * all Artists, all clients connected to a whiteboard, all clients but the one
 * who send it, just the client who sent it), and the number of clients attached
 * to the server.
 */
public class WhiteboardIntegrationTest {
    private final String local = "localhost";
    private final String localIP = "127.0.0.1";
    private final int port = 4444;

    /**
     * test that a "HELLO" message merits a response with the list of
     * whiteboards ("LIST" WB_NAME..)
     */
    @Test(timeout = 10000)
    public void helloTest() {
        try {
            // start up the server
            new WhiteboardServer();

            // we pretend to be the client
            final Socket socket = new Socket(local, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // say hello
            out.println("HELLO");

            // expect the list of whiteboards (just default right now)
            // TODO: this doesn't work )):
            assertEquals("LIST Default", in.readLine());

            socket.close();

            // we pretend to be another client!
            final Socket socket2 = new Socket(localIP, port);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

            // say hello
            out2.println("HELLO");

            // expect the list of whiteboards to be unchanged
            assertEquals("LIST Default", in2.readLine());

            socket2.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * test that "DRAW" messages get sent to all connected clients (but only
     * those associated with the whiteboard), and in the correct order
     */
    @Test(timeout = 10000)
    public void drawTest() {

    }

    /**
     * test that "CLEAR" messages get sent to all connected clients (but only
     * those associated with the whiteboard), and in the correct order
     */
    @Test(timeout = 10000)
    public void clearTest() {

    }

    /**
     * test that "BG" messages get sent to all connected clients (but only those
     * associated with the whiteboard), and in the correct order
     */
    @Test(timeout = 10000)
    public void bgTest() {

    }

    /**
     * test that "BYE" messages send "BYEUSER" messages to all but the client
     * that sent the message
     */
    @Test(timeout = 10000)
    public void byeTest() {

    }

    /**
     * test that "SELECT" messages return a response with all of the board's
     * history (empty or not) to the sender, and "NEWUSER" messages to everyone
     * else
     * */
    @Test(timeout = 10000)
    public void selectTest() {

    }

    /**
     * test that "NEW" messages return a response of "NEWNAME" to original
     * client, and "NEWBOARD" to all Artist clients
     * */
    @Test(timeout = 10000)
    public void newTest() {

    }

    /**
     * 
     * "BYEARTIST" should cause no new messages to be send
     */
    @Test(timeout = 10000)
    public void byeArtistTest() {

    }

}
