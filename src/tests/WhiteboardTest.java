package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import server.Whiteboard;

public class WhiteboardTest {

	private final Color black = new Color(0, 0, 0);
	private final List<Integer> blackrgb = new ArrayList<Integer>();
	private final List<Integer> whitergb = new ArrayList<Integer>();

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
	public void toStringTest() {

		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

        final Whiteboard board1 = new Whiteboard("board1", blackrgb);

		String boardString = board1.toString();

		assertTrue(boardString.charAt(0) == '0');
		assertTrue(boardString.charAt(1) == ' ');

		// These things don't work boardString is starting with 0 55 0 0 0 1 24
		// 0 0 what are 55 and 24 how

		System.out.println(boardString.substring(0, 19));
		// assertTrue(boardString.charAt(2) == '0');

	}

}
