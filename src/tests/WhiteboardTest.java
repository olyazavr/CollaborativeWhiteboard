package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import server.Whiteboard;

public class WhiteboardTest {

	private final List<Integer> blackrgb = new ArrayList<Integer>();
	private final List<Integer> whitergb = new ArrayList<Integer>();
	private final Color MIT = new Color(163, 31, 52);
	private final List<Integer> MITcolor = new ArrayList<Integer>();

	@Test
	public void initializeBackgroundTest() {

		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

		final Whiteboard board1 = new Whiteboard("board1", blackrgb);

		assertEquals(board1.getColor(0, 0), blackrgb);
		assertEquals(board1.getColor(799, 599), blackrgb);
		assertEquals(board1.getColor(400, 300), blackrgb);

	}

	@Test
	public void basicNameTest() {

		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

		final Whiteboard board1 = new Whiteboard("board1", blackrgb);

		assertEquals(board1.getName(), "board1");

	}

	@Test
	public void changeColorTest() {

		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

		whitergb.add(255);
		whitergb.add(255);
		whitergb.add(255);

		final Whiteboard board1 = new Whiteboard("board1", blackrgb);

		board1.setColor(799, 599, 255, 255, 255);
		assertEquals(board1.getColor(0, 0), blackrgb);
		assertEquals(board1.getColor(799, 599), whitergb);

		board1.setColor(0, 0, 255, 255, 255);

		assertEquals(board1.getColor(0, 0), whitergb);
		assertEquals(board1.getColor(799, 599), whitergb);

	}

	@Test
	public void toStringTestBlack() {

		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

		final Whiteboard board1 = new Whiteboard("board1", blackrgb);

		String[] boardArray = board1.createListOfPixels().split(" ");

		// Hashing is not in order, but should have correct spacing and r b g
		// should all be 0

		assertTrue(board1.createListOfPixels().charAt(1) == ' ');

		assertEquals(boardArray[2], "0");
		assertEquals(boardArray[3], "0");
		assertEquals(boardArray[4], "0");

		assertEquals(boardArray[7], "0");
		assertEquals(boardArray[8], "0");
		assertEquals(boardArray[9], "0");

	}

	@Test
	public void MITColorTest1() {
		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

		MITcolor.add(MIT.getRed());
		MITcolor.add(MIT.getGreen());
		MITcolor.add(MIT.getBlue());

		final Whiteboard MITboard = new Whiteboard("MIT", blackrgb);
		String[] boardArray = MITboard.createListOfPixels().split(" ");
		assertTrue(MITboard.createListOfPixels().charAt(1) == ' ');
		assertEquals(boardArray[7], "0");

	}
	@Test
	public void MITColorTest2() {
		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);
		
		MITcolor.add(MIT.getRed());
		MITcolor.add(MIT.getGreen());
		MITcolor.add(MIT.getBlue());
		
		final Whiteboard MITboard = new Whiteboard("MIT", blackrgb);
		MITboard.setBackgroundColor(MIT.getRed(), MIT.getGreen(), MIT.getBlue());

		assertEquals(MITboard.getColor(0, 0), MITcolor);
		assertEquals(MITboard.getColor(57, 82), MITcolor); // arbitrary

		String[] MITarray = MITboard.createListOfPixels().split(" ");

		assertEquals(MITarray[2], "163");
		assertEquals(MITarray[3], "31");
		assertEquals(MITarray[4], "52");

		assertEquals(MITarray[12], "163");
		assertEquals(MITarray[13], "31");
		assertEquals(MITarray[14], "52");
		
		
	}
}
