package server;

import java.awt.Color;
import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
	private final ArrayList<Integer> bg; // final for now, will change if we
											// decide to do the
	// drop to change background color thing

	private static Map<ArrayList<Integer>, ArrayList<Integer>> colormap = new HashMap<ArrayList<Integer>, ArrayList<Integer>>();

	public Whiteboard(int whiteboardID, String name, Color color) {

		this.ID = whiteboardID;
		this.name = name;

		// Make array of background stuff

		// For now background shit can never ever be changed

		final int red = color.getRed();
		final int green = color.getGreen();
		final int blue = color.getBlue();

		this.bg = new ArrayList<Integer>();
		this.bg.add(red);
		this.bg.add(green);
		this.bg.add(blue);

		// loop through to make the map
		for (int y = 0; y < 600; y++) {
			for (int x = 0; x < 800; x++) {

				ArrayList<Integer> coords = new ArrayList<Integer>();

				coords.add(x);
				coords.add(y);
				colormap.put(coords, this.bg);

				// Use unmodifiablelist for coords (like convert them from
				// regular arraylist or some shit) at some point maybe i tried
				// but java kept yelling at me just pretend arraylist
				// is a list okay bitch its 2 am fuck it

			}
		}

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

	public synchronized String toString() {
		return this.createListOfPixels();
	}

	/**
	 * Creates a string representation of a map.
	 * 
	 * Same code as in server
	 * 
	 * @return
	 */

	private String createListOfPixels() {
		// Map<List<Integer>, Color> pointsArray =
		// whiteboards.get(boardID);

		StringBuilder points = new StringBuilder();

		// loop through and add to the string
		for (Entry<ArrayList<Integer>, ArrayList<Integer>> point : colormap
				.entrySet()) {

			// X Y R G B
			points.append(point.getKey().get(0) + " " + point.getKey().get(1)
					+ " " + point.getValue().get(0) + " "
					+ point.getValue().get(1) + " " + point.getValue().get(2)
					+ " ");
		}

		return points.toString();
	}

}
