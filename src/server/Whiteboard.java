package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Server side hosts the whiteboard class. A map represents the canvas as an
 * ArrayList of coordinates (the keys of the map), representing pixels, and
 * their values are Color objects. A setColor method is there to allow the
 * server to change the color of a pixel, given it's coordinates and a new
 * color.
 * 
 * The format of the white board entries: <x, y> --> <red, green, blue>
 * 
 * set color: x, y, r, g, b --> map entry
 * 
 */

public class Whiteboard {

    private final int ID; // can't change the ID of a whiteboard after it's made
    private final String name;
    private final List<Integer> bg; // final for now, will change if we
                                    // decide to do the
    // drop to change background color thing

    private static Map<List<Integer>, List<Integer>> colormap = Collections
            .synchronizedMap(new HashMap<List<Integer>, List<Integer>>());

    public Whiteboard(int whiteboardID, String name, List<Integer> bg) {

        this.ID = whiteboardID;
        this.name = name;
        this.bg = Collections.synchronizedList(new ArrayList<Integer>(bg));

        // loop through to make the map
        for (int y = 0; y < 600; y++) {
            for (int x = 0; x < 800; x++) {

                List<Integer> coords = new ArrayList<Integer>();

                coords.add(x);
                coords.add(y);
                colormap.put(coords, this.bg);

            }
        }

    }
    
    public List<Integer> getColor(int x, int y) {
    	List<Integer> key = new ArrayList<Integer>();
    	key.add(x);
    	key.add(y);
    	return colormap.get(key);
    	
    }

    public String getName() {
        return this.name;
    }

    public int getID() {
        return this.ID;
    }

    /**
     * 
     * Changes the color of an object on the whiteboard.
     * 
     * Synchronized for concurrency
     * 
     * @param x
     *            integer between 0 and 799 representing the x coordinate of the
     *            pixel
     * @param y
     *            between 0 and 599
     * @param color
     *            a Color object that you want that pixel's color to be changed
     *            to
     */

    public synchronized void setColor(int x, int y, int red, int green, int blue) {
        // we could change the method to take something more usable, idk
        // like maybe have the parser return a small list of points at once;
        // we'll what happens

        ArrayList<Integer> coords = new ArrayList<Integer>();
        coords.add(x);
        coords.add(y);

        ArrayList<Integer> color = new ArrayList<Integer>();
        color.add(red);
        color.add(green);
        color.add(blue);

        Whiteboard.colormap.put(coords, color);

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
        bg.set(0, red);
        bg.set(1, green);
        bg.set(2, blue);
    }
    
    /**
     * Converts the board's array of positions and colors to a string of all of
     * the positions and colors (X Y R G B) of the board's pixels separated by
     * spaces
     * 
     * 
     * @return string of positions and colors of the pixels of the board
     */

    public String createListOfPixels() {
        StringBuilder points = new StringBuilder();

        // loop through and add to the string
        for (Entry<List<Integer>, List<Integer>> point : colormap.entrySet()) {

            // X Y R G B
            points.append(point.getKey().get(0) + " " + point.getKey().get(1)
                    + " " + point.getValue().get(0) + " "
                    + point.getValue().get(1) + " " + point.getValue().get(2)
                    + " ");
        }

        return points.toString();
    }

}
