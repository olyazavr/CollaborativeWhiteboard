package tests;

import java.io.IOException;

import org.junit.Test;

import server.WhiteboardServer;

/**
 * To test the server, we only make sure it can start without any exceptions.
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
 * 6. test multiple whiteboard support (ensure different whiteboards don’t send
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
 */
public class WhiteboardServerTest {

    @Test
    public void testServerConstructor() {
        // we can't really test the server in any other way
        // just make sure nothing blows up
        try {
            new WhiteboardServer();
        } catch (IOException e) {
            // this will make the test fail
            throw new RuntimeException(e);
        }
    }
    
    
}
