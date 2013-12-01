package server;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	private final String name;
	private final List<Integer> bg; // final for now, will change if we
									// decide to do the
	// drop to change background color thing

	private final List<Integer> actions;

	private final List<int[]> unusedColors = new ArrayList<int[]>();
	private final Color[] colors = new Color[] { Color.BLACK, Color.DARK_GRAY,
			Color.GRAY, Color.LIGHT_GRAY, Color.WHITE, Color.RED,
			Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE,
			Color.MAGENTA, new Color(163, 31, 52), Color.MAGENTA,
			Color.PINK, Color.CYAN };

	public Whiteboard(String name, List<Integer> bg) {
		this.name = name;
		this.bg = Collections.synchronizedList(new ArrayList<Integer>(bg));
		actions = Collections.synchronizedList(new ArrayList<Integer>());
		this.makeColorList();

	}

	public void makeColorList() {


		for (Color i : colors) {
			unusedColors
					.add(new int[] { i.getRed(), i.getGreen(), i.getBlue() });
		}

	}

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
	public synchronized void addAction(int x1, int y1, int x2, int y2,
			int stroke, int red, int green, int blue) {
		actions.addAll(Arrays.asList(x1, y1, x2, y2, stroke, red, green, blue));

		List<Integer> used = new ArrayList<Integer>(Arrays.asList(red, green, blue));
		
		// Won't change unusedColors if not in the list
		
		unusedColors.remove(used);

	}

	/**
	 * Gets the background color as a string
	 * 
	 * @return red green blue values of the background
	 */
	public String getBackgroundColorString() {
		return bg.get(0) + " " + bg.get(1) + " " + bg.get(2);
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
	 * Converts the board's actions to a string (X1 Y1 X2 Y2 STROKE R G B)
	 * separated by spaces
	 * 
	 * @return string of actions of the board, separated by spaces
	 */

	public String createStringOfActions() {
		StringBuilder action = new StringBuilder();

		// loop through and add to the string
		for (Integer i : actions) {
			// x1, y1, x2, y2, stroke, red, green, blue
			action.append(i + " ");
		}

		return action.toString();
	}

	public int calculateArtsy() {
		return (int) Math.round( 100 * unusedColors.size() / (colors.length + 0.0));
	}

}
