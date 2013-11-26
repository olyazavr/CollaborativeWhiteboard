package tests;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.ArrayList;

import org.junit.Test;

import server.Whiteboard;

import org.junit.Assert;

public class WhiteboardTest {

	private final Color black = new Color(0, 0, 0);
	private final ArrayList<Integer> blackrgb = new ArrayList<Integer>();
	private final ArrayList<Integer> whitergb = new ArrayList<Integer>();
	private final Whiteboard board1 = new Whiteboard(1, "board1", black);

	@Test
	public void initializeBackgroundTest() {

		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

		assertEquals(board1.getColor(0, 0), blackrgb);
		assertEquals(board1.getColor(799, 599), blackrgb);
		assertEquals(board1.getColor(400, 300), blackrgb);

	}

	@Test
	public void basicNameIdTest() {

		assertTrue(board1.getID() == 1);
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

		board1.setColor(799, 599, 255, 255, 255);
		assertEquals(board1.getColor(0, 0), blackrgb);
		assertEquals(board1.getColor(799, 599), whitergb);

		board1.setColor(0, 0, 255, 255, 255);

		assertEquals(board1.getColor(0, 0), whitergb);
		assertEquals(board1.getColor(799, 599), whitergb);

	}
	
	@Test
	public void toStringTest() {
		
		String boardString = board1.toString();
		
		assertTrue(boardString.charAt(0) == '0');
		assertTrue(boardString.charAt(1) == ' ');
		//System.out.println(boardString.substring(0, 19));
		//assertTrue(boardString.charAt(2) == '0');
		
	}

}
