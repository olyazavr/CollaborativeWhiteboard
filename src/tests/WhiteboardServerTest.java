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
 * 1. First make sure we can connect via IP address (and localhost) and receive
 * whiteboard names.
 * 
 * 2. Make sure we can both create and select existing whiteboard with different
 * names and bg colors.
 * 
 * 3. Make sure the Canvas UI works as planned (artsy meter increases with more
 * colors, all colors work, erasing works, doge button works, clear works).
 * 
 * 4. Test multiple users sharing one whiteboard (ensure both see the same
 * thing).
 * 
 * 5. Test multiple whiteboard support (ensure different whiteboards don't send
 * actions to each other).
 * 
 * 6. Make sure behavior is as expected when one user draws and another user
 * draws/erases common pixels (in equilibrium, the same image must be on both)
 * 
 * 7. Check to see that whiteboard state is saved during disconnect/reconnect.
 * 
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
