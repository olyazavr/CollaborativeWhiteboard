package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import server.Whiteboard;

/**
 * Testing strategy: Make sure on initialization, the whiteboard has an artsy
 * meter of zero, and no actions. Test the name getter. Test the background
 * color setter and getter. Test adding new actions and getting them in string
 * format. Test clearing all actions. Test the artsy meter increasing when new
 * colors are in actions, but not when custom or repeated colors are added. Test
 * that the artsy meter returns to 0 when the board is cleared. We can partition
 * on different names and colors, and different actions sent (different pixels,
 * colors, or doge code, which is all -1s). We allow no empty strings in names.
 * 
 */
public class WhiteboardTest {

    private final List<Integer> blackrgb = new ArrayList<Integer>();
    private final List<Integer> whitergb = new ArrayList<Integer>();
    private final List<Integer> MITcolor = Arrays.asList(163, 31, 52);

    @Test
    public void initBoardTest() {
        // test to make sure actions and artsy meter are empty
        blackrgb.add(0);
        blackrgb.add(0);
        blackrgb.add(0);

        final Whiteboard board1 = new Whiteboard("board1", blackrgb);

        // actions should be empty
        assertEquals("", board1.createStringOfActions());

        // artsy should be zero
        assertEquals(0, board1.calculateArtsy());
    }

    @Test
    public void nameTest() {
        // test name getter
        blackrgb.add(0);
        blackrgb.add(0);
        blackrgb.add(0);

        final Whiteboard board1 = new Whiteboard("board1", blackrgb);

        assertEquals(board1.getName(), "board1");
    }

    @Test
    public void backgroundColorTest() {
        // test bgcolor getters/setters
        blackrgb.add(0);
        blackrgb.add(0);
        blackrgb.add(0);

        final Whiteboard board1 = new Whiteboard("board1", blackrgb);
        assertEquals(board1.getBackgroundColorString(), "0 0 0");

        board1.setBackgroundColor(255, 255, 255);

        // changing background color is not an action
        assertEquals("", board1.createStringOfActions());
        assertEquals(board1.getBackgroundColorString(), "255 255 255");
    }

    @Test
    public void MITinitializationTest() {
        // test a board with a custom color
        final Whiteboard MITboard = new Whiteboard("MIT", MITcolor);

        assertEquals(MITboard.getName(), "MIT");
        assertEquals(MITboard.getBackgroundColorString(), "163 31 52");

        MITboard.setBackgroundColor(0, 0, 0);

        // changing background color is not an action
        assertTrue(MITboard.createStringOfActions().length() == 0);
        assertEquals(MITboard.getBackgroundColorString(), "0 0 0");
    }

    @Test
    public void newActionTest() {
        // test adding and getting an action
        blackrgb.add(0);
        blackrgb.add(0);
        blackrgb.add(0);

        whitergb.add(255);
        whitergb.add(255);
        whitergb.add(255);

        final Whiteboard board1 = new Whiteboard("board1", blackrgb);

        board1.addAction(0, 7, 2, 9, 10, 157, 33, 56);
        assertEquals(board1.createStringOfActions(),
                "0 7 2 9 10 157 33 56");

        board1.addAction(88, 96, 72, 30, 3, 33, 56, 0);
        assertEquals(board1.createStringOfActions(),
                "0 7 2 9 10 157 33 56 88 96 72 30 3 33 56 0");

        board1.addAction(0, 0, 99, 99, 6, Color.MAGENTA.getRed(),
                Color.MAGENTA.getGreen(), Color.MAGENTA.getBlue());
        assertEquals(board1.createStringOfActions(),
                "0 7 2 9 10 157 33 56 88 96 72 30 3 33 56 0 0 0 99 99 6 255 0 255");

        // adding a doge
        board1.addAction(-1, -1, -1, -1, -1, -1, -1, -1);
        assertEquals(
                board1.createStringOfActions(),
                "0 7 2 9 10 157 33 56 88 96 72 30 3 33 56 0 0 0 99 99 6 255 0 255 -1 -1 -1 -1 -1 -1 -1 -1");
    }

    @Test
    public void clearActionsTest() {
        // test that clearing clears all actions
        blackrgb.add(0);
        blackrgb.add(0);
        blackrgb.add(0);

        final Whiteboard board1 = new Whiteboard("board1", blackrgb);
        board1.addAction(0, 7, 2, 9, 10, 157, 33, 56);
        assertEquals(board1.createStringOfActions(),
                "0 7 2 9 10 157 33 56");

        // Check that clear makes board start over
        board1.clear();
        assertEquals("", board1.createStringOfActions());
    }

    @Test
    public void testOfArtsiness() {
        // test things that should change the artsy meter
        final Whiteboard MITboard = new Whiteboard("MIT", MITcolor);

        // Using color not on pallet does not increase artsy
        MITboard.addAction(0, 7, 2, 9, 10, 157, 33, 56);
        assertTrue(MITboard.calculateArtsy() == 0);

        // This should increase artsy
        MITboard.addAction(0, 24, 73, 19, 10, 255, 255, 255);
        assertTrue(MITboard.calculateArtsy() == 7);

        // Using same color again does not increase artsy
        MITboard.addAction(3, 41, 96, 5, 5, 255, 255, 255);
        assertTrue(MITboard.calculateArtsy() == 7);

        // This should increase artsy
        MITboard.addAction(0, 0, 99, 99, 6, Color.MAGENTA.getRed(),
                Color.MAGENTA.getGreen(), Color.MAGENTA.getBlue());
        assertTrue(MITboard.calculateArtsy() == 14);

        // Check that clear makes board start over
        MITboard.clear();
        assertTrue(MITboard.calculateArtsy() == 0);

        // Drawing the same stuff should still increase artsy
        MITboard.addAction(0, 24, 73, 19, 10, 255, 255, 255);
        assertTrue(MITboard.calculateArtsy() == 7);

        MITboard.addAction(0, 0, 99, 99, 6, Color.MAGENTA.getRed(),
                Color.MAGENTA.getGreen(), Color.MAGENTA.getBlue());
        assertTrue(MITboard.calculateArtsy() == 14);
    }
}
