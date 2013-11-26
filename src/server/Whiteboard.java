package server;

import java.awt.Color;
import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Server side hosts the whiteboard class. A map represents the canvas as an
 * ArrayList of coordinates (the keys of the map), representing pixels, and
 * their values are Color objects. A setColor method is there to allow the
 * server to change the color of a pixel, given it's coordinates and a new
 * color.
 * 
 */

public class Whiteboard {

	private final int ID; // can't change the ID of a whiteboard after it's made
	private final String name;
	private final Color bg; // final for now, will change if we decide to do the
							// drop to change background color thing

	private static Map<ArrayList<Integer>, Color> colormap = new HashMap<ArrayList<Integer>, Color>();

	public Whiteboard(int whiteboardID, String name, Color bgcolor) {

		this.ID = whiteboardID;
		this.name = name;
		this.bg = bgcolor;

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

		Whiteboard.colormap = colormap;

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

	public synchronized void setColor(int x, int y, Color color) { // we could
																	// change
																	// the
		// method to take
		// something more
		// usable, idk
		ArrayList<Integer> coords = new ArrayList<Integer>();
		coords.add(x);
		coords.add(y);
		Whiteboard.colormap.put(coords, color);

	}

	public Map getMap() {
		return Whiteboard.colormap; // this is not safe probs
	}

}
