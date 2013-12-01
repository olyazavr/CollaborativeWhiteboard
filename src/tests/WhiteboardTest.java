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
	public void basicNameTest() {

		blackrgb.add(0);
		blackrgb.add(0);
		blackrgb.add(0);

		final Whiteboard board1 = new Whiteboard("board1", blackrgb);

		assertEquals(board1.getName(), "board1");
		assertTrue(board1.createStringOfActions().length() == 0);
		board1.setBackgroundColor(255, 255, 255);

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
		
	}

}
