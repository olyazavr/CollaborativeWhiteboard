package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import server.Whiteboard;

public class WhiteboardTest {

	private final List<Integer> blackrgb = new ArrayList<Integer>();
	private final List<Integer> whitergb = new ArrayList<Integer>();
	private final Color MIT = new Color(163, 31, 52);
	private final List<Integer> MITcolor = Arrays.asList(163, 31, 52);

	@Test
	public void basicNameBGcolorTest() {

		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

		final Whiteboard board1 = new Whiteboard("board1", blackrgb);

		assertEquals(board1.getBackgroundColorString(), "0 0 0");

		assertEquals(board1.getName(), "board1");
		
		// Shouldn't have any actions
		assertTrue(board1.createStringOfActions().length() == 0);
		board1.setBackgroundColor(255, 255, 255);
		
		// changing background color is not an action
		assertTrue(board1.createStringOfActions().length() == 0);

		assertEquals(board1.getBackgroundColorString(), "255 255 255");

	}

	@Test
	public void MITinitializationTest() {
		

		final Whiteboard MITboard = new Whiteboard("MIT", MITcolor);

		assertEquals(MITboard.getName(), "MIT");
		
		System.out.println(MITboard.getBackgroundColorString());
		assertEquals(MITboard.getBackgroundColorString(), "163, 31, 52");

		MITboard.setBackgroundColor(0, 0, 0);
		assertTrue(MITboard.createStringOfActions().length() == 0);
		assertEquals(MITboard.getBackgroundColorString(), "0 0 0");

	}

	@Test
	public void newActionTest() {

		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

		whitergb.add(255);
		whitergb.add(255);
		whitergb.add(255);

		final Whiteboard board1 = new Whiteboard("board1", blackrgb);

		String bgString = board1.getBackgroundColorString();

		assertEquals(bgString, "0 0 0");

		assertEquals(board1.getName(), "board1");

		board1.addAction(0, 7, 2, 9, 10, 157, 33, 56);

		assertTrue(board1.createStringOfActions().length() == 21);
		assertTrue(board1.createStringOfActions().charAt(4) == '2');
		assertTrue(board1.createStringOfActions().charAt(5) == ' ');
		assertTrue(board1.createStringOfActions().charAt(6) == '9');

		board1.addAction(88, 96, 72, 30, 3, 33, 56, 0);

		assertTrue(board1.createStringOfActions().length() == 43);
		assertTrue(board1.createStringOfActions().charAt(4) == '2');
		assertTrue(board1.createStringOfActions().charAt(5) == ' ');
		assertTrue(board1.createStringOfActions().charAt(6) == '9');

		assertTrue(board1.calculateArtsy() == 0);

		board1.addAction(0, 24, 73, 19, 10, 255, 255, 255);

		assertEquals(board1.createStringOfActions(),
				"0 7 2 9 10 157 33 56 88 96 72 30 3 33 56 0 0 24 73 19 10 255 255 255 ");

		board1.addAction(3, 41, 96, 5, 5, 255, 255, 255);

		assertEquals(
				board1.createStringOfActions(),
				"0 7 2 9 10 157 33 56 88 96 72 30 3 33 56 0 0 24 73 19 10 255 255 255 3 41 96 5 5 255 255 255 ");

		board1.addAction(0, 0, 99, 99, 6, Color.MAGENTA.getRed(),
				Color.MAGENTA.getGreen(), Color.MAGENTA.getBlue());

		// Check that clear makes board start over
		board1.clear();

		assertTrue(board1.createStringOfActions().length() == 0);

	}

	@Test
	public void testOfArtsiness() {

		final Whiteboard MITboard = new Whiteboard("MIT", MITcolor);

		// Using color not on pallet does not increase artsy
		MITboard.addAction(0, 7, 2, 9, 10, 157, 33, 56);
		assertTrue(MITboard.calculateArtsy() == 0);

		MITboard.addAction(0, 24, 73, 19, 10, 255, 255, 255);
		assertTrue(MITboard.calculateArtsy() == 7);

		// Using same color again does not increase artsy
		MITboard.addAction(3, 41, 96, 5, 5, 255, 255, 255);
		assertTrue(MITboard.calculateArtsy() == 7);

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
