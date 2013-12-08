package tests;

import java.awt.Color;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import server.WhiteboardServer;
import client.Artist;
import client.Canvas;

/**
 * To test the Artist and Canvas classes, we can only start it up to see if
 * there are exceptions.
 * 
 * To test the server/client interactions, as well as concurrency and UI, we did
 * the following:
 * 
 * 1. first make sure we can connect via IP address (and localhost) and that
 * populates the dropdown of whiteboard names.
 * 
 * 2. make sure creating a new whiteboard updates the dropdowns of other Artists
 * 
 * 3. make sure we can both create and select existing whiteboard with different
 * names and bg colors.
 * 
 * 4. make sure the Canvas UI works as planned (artsy meter increases with more
 * colors, all colors work, erasing works, doge button works, clear works).
 * 
 * 5. test multiple users sharing one whiteboard (ensure both see the same
 * thing).
 * 
 * 6. test multiple whiteboard support (ensure different whiteboards don't send
 * actions to each other).
 * 
 * 7. make sure behavior is as expected when one user draws and another user
 * draws/erases common pixels (in equilibrium, the same image must be on both).
 * 
 * 8. check to see that whiteboard state is saved during disconnect/reconnect.
 * 
 * 9. test switching whiteboards (Artist dialogue should appear, filled out)
 * 
 * 10. test saving an image
 * 
 * 11. test posting to Facebook
 * 
 * UI breaks didit.
 * 
 * @category no_didit
 */
public class UITest {

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

    @Test
    public void testArtistConstructor() {
        // we can't really test the Artist in any other way
        // just make sure nothing blows up
        Artist.main(new String[1]);
    }

    @Test
    public void testCanvasConstructor() {
        // we can't really test the Canvas in any other way
        // just make sure nothing blows up
        try {
            new Canvas("board1", "localhost", Color.WHITE, "user1");
        } catch (IOException e) {
            // this will make the test fail
            throw new RuntimeException(e);
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
