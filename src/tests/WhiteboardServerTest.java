package tests;

import java.io.IOException;

import org.junit.Test;

import server.WhiteboardServer;

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
