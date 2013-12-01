package server;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The ADT representing a Whiteboard. It holds its name, background color, and
 * history of actions. It can also calculate its artsy meter.
 * 
 * This is threadsafe because all of its instance variables are private, final,
 * and threadsafe (except unused colors, which are mutable, but threadsafe). All
 * modifications done to actions or bg (or things that depends on actions or bg)
 * are locked on actions or bg. Nothing mutable is returned from any method and
 * nothing mutable is shared between instances. A Whiteboard doesn't know
 * anything about the server or view, it only responds to additions to actions
 * or change in bg, and returns strings of its current state.
 * 
 */

public class Whiteboard {
    private final String name;
    private final List<Integer> bg; // final for now, will change if we
                                    // decide to do the
    // drop to change background color thing

    private final List<Integer> actions;

    private List<int[]> unusedColors;
    private final static Color[] colors = new Color[] { Color.BLACK, Color.DARK_GRAY,
            Color.GRAY, Color.LIGHT_GRAY, Color.WHITE, Color.RED, Color.ORANGE,
            Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA,
            new Color(163, 31, 52), Color.MAGENTA, Color.PINK, Color.CYAN };

    public Whiteboard(String name, List<Integer> bg) {
        this.name = name;
        this.bg = Collections.synchronizedList(new ArrayList<Integer>(bg));
        actions = Collections.synchronizedList(new ArrayList<Integer>());
        unusedColors = Collections.synchronizedList(makeColorList());

    }

    /**
     * Makes the list of unused colors (currently, all of the colors) to be used
     * in calculation of artsy meter.
     * 
     * @return a list of int arrays of unused colors (r, g, b)
     */
    private List<int[]> makeColorList() {
        List<int[]> uColors = new ArrayList<int[]>();

        for (Color i : colors) {
            uColors.add(new int[] { i.getRed(), i.getGreen(), i.getBlue() });
        }

        return uColors;
    }

    /**
     * Returns the whiteboard's name
     * 
     * @return name of whiteboard
     */
    public String getName() {
        return this.name;
    }

    /**
     * Adds a draw action to the action list.
     * 
     * @param x1
     *            starting x
     * @param y1
     *            starting y
     * @param x2
     *            ending x
     * @param y2
     *            ending y
     * @param stroke
     *            stroke size
     * @param red
     *            amount of red (0-255)
     * @param green
     *            amount of green (0-255)
     * @param blue
     *            amount of blue (0-255)
     */
    public void addAction(int x1, int y1, int x2, int y2, int stroke, int red, int green, int blue) {
        // locked so that we can't print out history as its being changed
        synchronized (actions) {
            actions.addAll(Arrays.asList(x1, y1, x2, y2, stroke, red, green, blue));

            int[] used = new int[] { red, green, blue };

            // Won't change unusedColors if not in the list
            unusedColors.remove(used);
        }
    }

    /**
     * Gets the background color as a string
     * 
     * @return red green blue values of the background
     */
    public String getBackgroundColorString() {
        // because 3 gets can be interleaved
        synchronized (bg) {
            return bg.get(0) + " " + bg.get(1) + " " + bg.get(2);
        }
    }

    /**
     * Change the background color of the board.
     * 
     * @param red
     *            amount of red
     * @param green
     *            amount of green
     * @param blue
     *            amount of blue
     */
    public synchronized void setBackgroundColor(int red, int green, int blue) {
        // because 3 sets can be interleaved
        synchronized (bg) {
            bg.set(0, red);
            bg.set(1, green);
            bg.set(2, blue);
        }
    }

    /**
     * Converts the board's actions to a string (X1 Y1 X2 Y2 STROKE R G B)
     * separated by spaces
     * 
     * @return string of actions of the board, separated by spaces
     */
    public String createStringOfActions() {
        StringBuilder action = new StringBuilder();

        // so that we can't modify actions as we access it
        synchronized (actions) {
            // loop through and add to the string
            for (Integer i : actions) {
                // x1, y1, x2, y2, stroke, red, green, blue
                action.append(i + " ");
            }
        }
        return action.toString();
    }

    /**
     * Calculates the artsy meter
     * 
     * @return 0-100 value of the artsy meter
     */
    public int calculateArtsy() {
        // this changes with actions
        synchronized (actions) {
            // int representation of percent used
            return (int) (100 * (colors.length - unusedColors.size()) / (colors.length + 0.0));
        }
    }

}
