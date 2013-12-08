package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;

import server.WhiteboardServer;

/**
 * Testing strategy: test all of the possible things the client and server can
 * send to each other, ensuring that the right message gets sent to the right
 * clients. Partition on who should get the message (ie. all Artists, all
 * clients connected to a whiteboard, all clients but the one who send it, just
 * the client who sent it)
 */
public class WhiteboardIntegrationTest {
    private final String local = "localhost";
    private final String localIP = "127.0.0.1";
    private final int port = 4444;

    @Before
    public void setUp() {
        try {
            // start up the server
            startServer();

            // Avoid race where we try to connect to server too early
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * test that a "HELLO" message merits a response with the list of
     * whiteboards ("LIST" WB_NAME..)
     */
    @Test(timeout = 10000)
    public void helloTest() {
        try {
            // we pretend to be the client
            final Socket socket = new Socket(local, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // say hello
            out.println("HELLO");

            // expect the list of whiteboards (just default right now)
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
     * those associated with the whiteboard)
     */
    @Test(timeout = 10000)
    public void drawTest() {
        try {
            // we pretend to be the client
            final Socket socket = new Socket(local, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // we pretend to be another client!
            final Socket socket2 = new Socket(localIP, port);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

            // say hello
            out.println("HELLO");
            out2.println("HELLO");

            // expect the list of whiteboards (just default right now)
            assertEquals("LIST Default", in.readLine());
            assertEquals("LIST Default", in2.readLine());

            // select Default board with username user1
            out.println("SELECT Default user1");

            // expect bg color (white), artsy meter of zero, and only user1
            // attached
            assertEquals("255 255 255 0 USERS user1 ACTIONS ", in.readLine());

            // Draw from (0,0) to (5,5) with stroke 7 in black
            out.println("DRAW Default 0 0 5 5 7 0 0 0");

            // should get a draw action with artsy of 7%
            assertEquals("DRAW 7 0 0 5 5 7 0 0 0", in.readLine());

            // select Default board with username user2
            out2.println("SELECT Default user2");

            // expect bg color (white), artsy meter of 7, and user 1 and user2,
            // as well as the draw action from before
            assertEquals("255 255 255 7 USERS user1 user2 ACTIONS 0 0 5 5 7 0 0 0", in2.readLine());

            // expect first client to be notified of another user joining
            assertEquals("NEWUSER user2", in.readLine());

            // Now two draws from (1,7) to (20,25) with stroke 7 in red
            out2.println("DRAW Default 1 7 20 25 7 255 0 0");

            // both should get a draw action with artsy of 14%
            assertEquals("DRAW 14 1 7 20 25 7 255 0 0", in.readLine());
            assertEquals("DRAW 14 1 7 20 25 7 255 0 0", in2.readLine());

            socket.close();
            socket2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * test that "CLEAR" messages get sent to all connected clients (but only
     * those associated with the whiteboard)
     */
    @Test(timeout = 10000)
    public void clearTest() {
        try {
            // we pretend to be the client
            final Socket socket = new Socket(local, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // we pretend to be another client!
            final Socket socket2 = new Socket(localIP, port);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

            // say hello
            out.println("HELLO");
            out2.println("HELLO");

            // expect the list of whiteboards (just default right now)
            assertEquals("LIST Default", in.readLine());
            assertEquals("LIST Default", in2.readLine());

            // select Default board with username user1
            out.println("SELECT Default user1");

            // expect bg color (white), artsy meter of zero, and only user1
            // attached
            assertEquals("255 255 255 0 USERS user1 ACTIONS ", in.readLine());

            // Draw from (0,0) to (5,5) with stroke 7 in black
            out.println("DRAW Default 0 0 5 5 7 0 0 0");

            // should get a draw action with artsy of 7%
            assertEquals("DRAW 7 0 0 5 5 7 0 0 0", in.readLine());

            // select Default board with username user2
            out2.println("SELECT Default user2");

            // expect bg color (white), artsy meter of 7, and user 1 and user2,
            // as well as the draw action from before
            assertEquals("255 255 255 7 USERS user1 user2 ACTIONS 0 0 5 5 7 0 0 0", in2.readLine());

            // expect first client to be notified of another user joining
            assertEquals("NEWUSER user2", in.readLine());

            // Now two clears everything
            out2.println("CLEAR Default");

            // both should get a clear action
            assertEquals("CLEAR", in.readLine());
            assertEquals("CLEAR", in2.readLine());

            socket.close();
            socket2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * test that "BG" messages get sent to all connected clients (but only those
     * associated with the whiteboard)
     */
    @Test(timeout = 10000)
    public void bgTest() {
        try {
            // we pretend to be the client
            final Socket socket = new Socket(local, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // we pretend to be another client!
            final Socket socket2 = new Socket(localIP, port);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

            // say hello
            out.println("HELLO");
            out2.println("HELLO");

            // expect the list of whiteboards (just default right now)
            assertEquals("LIST Default", in.readLine());
            assertEquals("LIST Default", in2.readLine());

            // select Default board with username user1
            out.println("SELECT Default user1");

            // expect bg color (white), artsy meter of zero, and only user1
            // attached
            assertEquals("255 255 255 0 USERS user1 ACTIONS ", in.readLine());

            // change the bg color to red
            out.println("BG Default 255 0 0");

            // should get a bg change message
            assertEquals("BG 255 0 0", in.readLine());

            // select Default board with username user2
            out2.println("SELECT Default user2");

            // expect bg color (red), artsy meter of 0, and user 1 and user2,
            // and no draw actions
            assertEquals("255 0 0 0 USERS user1 user2 ACTIONS ", in2.readLine());

            // expect first client to be notified of another user joining
            assertEquals("NEWUSER user2", in.readLine());

            // now change the bg color to black
            out2.println("BG Default 0 0 0");

            // both should get a bg change message
            assertEquals("BG 0 0 0", in.readLine());
            assertEquals("BG 0 0 0", in2.readLine());

            socket.close();
            socket2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * test that "BYE" messages send "BYEUSER" messages to all but the client
     * that sent the message (the client's connection should be terminated by
     * server)
     */
    @Test(timeout = 10000)
    public void byeTest() {
        try {
            // we pretend to be the client
            final Socket socket = new Socket(local, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // we pretend to be another client!
            final Socket socket2 = new Socket(localIP, port);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

            // say hello
            out.println("HELLO");
            out2.println("HELLO");

            // expect the list of whiteboards (just default right now)
            assertEquals("LIST Default", in.readLine());
            assertEquals("LIST Default", in2.readLine());

            // select Default board with username user1
            out.println("SELECT Default user1");

            // expect bg color (white), artsy meter of zero, and only user1
            // attached
            assertEquals("255 255 255 0 USERS user1 ACTIONS ", in.readLine());

            // select Default board with username user2
            out2.println("SELECT Default user2");

            // expect bg color (white), artsy meter of 0, and user 1 and user2,
            // and no draw actions
            assertEquals("255 255 255 0 USERS user1 user2 ACTIONS ", in2.readLine());

            // expect first client to be notified of another user joining
            assertEquals("NEWUSER user2", in.readLine());

            // now one leaves
            out.println("BYE Default user1");

            // two should get the message
            assertEquals("BYEUSER user1", in2.readLine());

            // one's connection should be terminated
            assertNull(in.readLine());

            socket.close();
            socket2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * test that "SELECT" messages return a response with all of the board's
     * history (empty or not) to the sender, and "NEWUSER" messages to everyone
     * else
     * */
    @Test(timeout = 10000)
    public void selectTest() {
        try {
            // we pretend to be the client
            final Socket socket = new Socket(local, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // say hello
            out.println("HELLO");

            // expect the list of whiteboards (just default right now)
            assertEquals("LIST Default", in.readLine());

            // select Default board with username user1
            out.println("SELECT Default user1");

            // expect bg color (white), artsy meter of zero, and only user1
            // attached
            assertEquals("255 255 255 0 USERS user1 ACTIONS ", in.readLine());

            // we pretend to be another client!
            final Socket socket2 = new Socket(localIP, port);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

            // say hello
            out2.println("HELLO");

            // expect the list of whiteboards to be unchanged
            assertEquals("LIST Default", in2.readLine());

            // select Default board with username user1
            out2.println("SELECT Default user2");

            // expect bg color (white), artsy meter of zero, and both user1 and
            // user2
            assertEquals("255 255 255 0 USERS user1 user2 ACTIONS ", in2.readLine());

            // expect first client to be notified of another user joining
            assertEquals("NEWUSER user2", in.readLine());

            socket.close();
            socket2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * test that "NEW" messages return a response of "NEWNAME" to original
     * client, and "NEWBOARD" to all Artist clients (also ensure duplicate names
     * do not happen)
     * */
    @Test(timeout = 10000)
    public void newTest() {
        try {
            // we pretend to be the client
            final Socket socket = new Socket(local, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // we pretend to be another client!
            final Socket socket2 = new Socket(localIP, port);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

            // say hello
            out.println("HELLO");
            out2.println("HELLO");

            // expect the list of whiteboards (just default right now)
            assertEquals("LIST Default", in.readLine());
            assertEquals("LIST Default", in2.readLine());

            // one makes a new black board
            out.println("NEW catBoard 0 0 0");

            // that name isn't taken, so the same name should be returned
            assertEquals("NEWNAME catBoard", in.readLine());

            // two should get a notification of a new board
            assertEquals("NEWBOARD catBoard", in2.readLine());

            // two tries to make a board with the same name
            out2.println("NEW catBoard 255 255 255");

            // name is taken, so we get catBoard(1)
            assertEquals("NEWNAME catBoard(1)", in2.readLine());

            socket.close();
            socket2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * "BYEARTIST" should cause the server to close the connection.
     */
    @Test(timeout = 10000)
    public void byeArtistTest() {
        try {
            // we pretend to be the client
            final Socket socket = new Socket(local, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // say hello
            out.println("HELLO");

            // expect the list of whiteboards (just default right now)
            assertEquals("LIST Default", in.readLine());

            // suddenly, the Artist leaves
            out.println("BYEARTIST");

            // server should shut down the connection
            assertNull(in.readLine());

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Runs the server in another thread
     */
    private void startServer() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    WhiteboardServer.main(new String[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }

}
