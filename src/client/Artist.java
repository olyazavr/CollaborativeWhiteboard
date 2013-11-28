package client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.GroupLayout.Group;
import javax.swing.SwingUtilities;

/**
 * Possible outputs: (1) initial connect message ("HELLO"), (2) select
 * whiteboard ("SELECT" WB_ID USER_NAME), (3) make new whiteboard and select it
 * ("NEW" WB_NAME COLOR_R COLOR_G COLOR_B USER_NAME), (4) new draw actions
 * ("DRAW" WB_ID STROKE X Y COLOR_R COLOR_G COLOR_B), (5) change whiteboard bg
 * color ("BG" WB_ID COLOR_R COLOR_G COLOR_B), (6) disconnect message ("BYE"
 * WB_ID USER_NAME)
 * 
 * Possible inputs: (1) whiteboard names and ids (WB_ID WB_NAME WB_ID
 * WB_NAME...), (2)-(3) whiteboard specs ("USERS" USER_NAME USER_NAME...
 * "PIXELS" X1 Y1 COLOR_R1 COLOR_G1 COLOR_B1 X2 Y2 COLOR_R2 COLOR_G2
 * COLOR_B2...) to new client, ("NEWUSER" USER_NAME) to others, (4) new draw
 * actions by others ("DRAW" ARTSY_METER STROKE X Y COLOR_R COLOR_G COLOR_B),
 * (5) change whiteboard bg color ("BG" COLOR_R COLOR_G COLOR_B), (6) user
 * leaves ("BYEUSER" USER_NAME)
 */
public class Artist {
	private final Socket socket;
	private final int port = 4444;
	// always send the server the whiteboardID, not its name
	private final int whiteboardID;

	private final JLabel usernamePrompt;
	private final JTextField enterUsername;
	private final JLabel board;
	private final JComboBox<String> newBoard;

	private final JLabel IPprompt;
	private final JTextField enterIP;
	private final JButton localhost;

	private final JLabel whiteboardPrompt;
	private final JTextField whiteboardNamer;
	private final JLabel bgColorPrompt;
	private final JComboBox<String> bgColorPicker;

	private Color color;
	private final JButton GO;

	private final GroupLayout layout;
	private final Group row1;
	private final Group row2;
	private final Group row3;
	private final Group row4;
	private final Group row5;
	private final Group horizontal;

	private final Group ver1;
	private final Group ver2;
	private final Group ver3;
	private final Group ver4;
	private final Group ver5;
	private final Group vertical;

	private final JFrame window;

	private String username;
	private String IP;

	public Artist() throws UnknownHostException, IOException {
		String ip = "localhost";
		socket = new Socket(ip, port);

		// try with multiple resources! this is so hot
		try (BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(),
						true)) {

			// send initial hello to get whiteboards
			out.println("HELLO");
			System.out.println("wrote hello!");

			// retrieve the list of whiteboards
			final Map<Integer, String> whiteboardList = new HashMap<Integer, String>();
			String[] input = in.readLine().split(" ");
			System.out.println("read something in!");

			for (int i = 1; i < input.length; i += 2) {
				System.out.println(input[i - 1] + " " + input[i]);
				whiteboardList.put(new Integer(input[i - 1]), input[i]);
			}

			// select the whiteboard (name) we want, give the server the id of
			// it (look it up) if we're selecting (not creating)
			whiteboardID = 0;

			// Create a log-in screen
			this.window = new JFrame("Login");
			this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// Initiating labels, textfields, etc
			usernamePrompt = new JLabel();
			usernamePrompt.setName("usernamePrompt");
			usernamePrompt.setText("Pick a username: ");

			enterUsername = new JTextField();

			board = new JLabel();
			board.setText("Choose/create a board: ");

			// The dropdown list to choose a whiteboard is a combobox
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();

			// default option (first thing) is newboard
			model.addElement("New whiteboard");

			model.addElement("existing board"); // have this for testing now

			// TODO: parse whiteboard name shits from the server and do a loop
			// and
			// add them here

			newBoard = new JComboBox<String>(model);

			// If they want a new board, prompt them
			// Pick name and background color
			whiteboardPrompt = new JLabel();
			whiteboardPrompt.setText("New board name:");
			whiteboardNamer = new JTextField(10);

			bgColorPrompt = new JLabel();
			bgColorPrompt.setText("with background: ");

			DefaultComboBoxModel<String> colors = new DefaultComboBoxModel<String>();

			colors.addElement("White");
			colors.addElement("Black");
			colors.addElement("Gray");
			colors.addElement("Light gray");
			colors.addElement("Red");
			colors.addElement("Orange");
			colors.addElement("Yellow");
			colors.addElement("Green");
			colors.addElement("Blue");
			colors.addElement("MIT Special");
			colors.addElement("Magenta");
			colors.addElement("Pink");
			colors.addElement("Cyan");
			colors.addElement("Custom...");

			final Map<String, Color> colorMap = new HashMap<String, Color>();
			colorMap.put("White", Color.WHITE);
			colorMap.put("Black", Color.BLACK);
			colorMap.put("Gray", Color.GRAY);
			colorMap.put("Light gray", Color.LIGHT_GRAY);
			colorMap.put("Red", Color.RED);
			colorMap.put("Orange", Color.ORANGE);
			colorMap.put("Yellow", Color.YELLOW);
			colorMap.put("Green", Color.GREEN);
			colorMap.put("Blue", Color.BLUE);
			colorMap.put("MIT Special", new Color(163, 31, 52));
			colorMap.put("Magenta", Color.MAGENTA);
			colorMap.put("Pink", Color.PINK);
			colorMap.put("Cyan", Color.CYAN);

			bgColorPicker = new JComboBox<String>(colors);

			IPprompt = new JLabel();
			IPprompt.setText("Enter IP address: ");

			// default width so it doesn't get shat on by the fatass button
			enterIP = new JTextField(10);

			localhost = new JButton();
			localhost.setText("Use localhost");

			GO = new JButton();
			GO.setText("Go!");

			// use GroupLayout
			layout = new GroupLayout(window.getContentPane());
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			this.window.setLayout(layout);

			// draw all the shits
			row1 = layout.createSequentialGroup().addComponent(usernamePrompt)
					.addComponent(enterUsername);
			row2 = layout.createSequentialGroup().addComponent(board)
					.addComponent(newBoard);
			row3 = layout.createSequentialGroup()
					.addComponent(whiteboardPrompt)
					.addComponent(whiteboardNamer).addComponent(bgColorPrompt)
					.addComponent(bgColorPicker);
			row4 = layout.createSequentialGroup().addComponent(IPprompt)
					.addComponent(enterIP).addComponent(localhost);
			row5 = layout.createSequentialGroup().addComponent(GO);

			horizontal = layout.createSequentialGroup();
			horizontal.addGroup(layout.createParallelGroup().addGroup(row1)
					.addGroup(row2).addGroup(row3).addGroup(row4)
					.addGroup(row5));

			layout.setHorizontalGroup(horizontal);

			ver1 = layout.createParallelGroup().addComponent(usernamePrompt)
					.addComponent(enterUsername);
			ver2 = layout.createParallelGroup().addComponent(board)
					.addComponent(newBoard);
			ver3 = layout.createParallelGroup().addComponent(whiteboardPrompt)
					.addComponent(whiteboardNamer).addComponent(bgColorPrompt)
					.addComponent(bgColorPicker);
			ver4 = layout.createParallelGroup().addComponent(IPprompt)
					.addComponent(enterIP).addComponent(localhost);
			ver5 = layout.createParallelGroup().addComponent(GO);

			vertical = layout.createSequentialGroup();
			vertical.addGroup(ver1).addGroup(ver2).addGroup(ver3)
					.addGroup(ver4).addGroup(ver4).addGroup(ver5);
			layout.setVerticalGroup(vertical);

			this.window.pack();

			// LISTENERS BITCH
			// the localhost button sets the text to local host
			// parse later
			localhost.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					enterIP.setText("localhost");
				}

			});

			newBoard.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String choice = (String) newBoard.getSelectedItem();
					if (!choice.equals("New whiteboard")) {
						// TODO: find someway to hide the shits without making
						// all the objects realign
						whiteboardPrompt.setVisible(false);
						whiteboardNamer.setVisible(false);
						bgColorPrompt.setVisible(false);
						bgColorPicker.setVisible(false);
					} else {
						// if they are a dumbass and cant make up their mind
						whiteboardPrompt.setVisible(true);
						whiteboardNamer.setVisible(true);
						bgColorPrompt.setVisible(true);
						bgColorPicker.setVisible(true);
					}
				}

			});

			bgColorPicker.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String bgColor = (String) bgColorPicker.getSelectedItem();
					if (bgColor.equals("Custom...")) {
						color = Color.WHITE;
						JColorChooser.showDialog(new JPanel(),
								"Choose a color", color);

					} else {

						// get the chosen color object from the map
						color = colorMap.get(bgColor);
					}
				}

			});

			// clicking go makes new canvas and gets rid of the log in screen
			// finalize choices and sends dat shit to server

			GO.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					// make sure the client doesn't try to do stupid stuff like
					// empty usernames lolz
					if (enterUsername.getText().replaceAll("\\s+", "").length() != 0
							& enterIP.getText().replaceAll("\\s+", "").length() != 0) {

						// let the user make a map
						boolean boardNameTaken = false;

						if (!boardNameTaken) {

							// TODO: do usernames need to be unique??

							username = enterUsername.getText();
							IP = enterIP.getText();

							// see what they chose
							String choice = (String) newBoard.getSelectedItem();

							try {
								if (choice.equals("New whiteboard")) {

									// loop through the map to see if their
									// choice of whiteboard name is taken
									for (String value : whiteboardList.values()) {
										if (value.equals(whiteboardNamer
												.getText())) {
											boardNameTaken = true;
											break;
										}
									}
									if (boardNameTaken) {
										JOptionPane
												.showMessageDialog(window,
														"That whiteboard name is taken. Please choose a different one!");

									} else {
										out.println("NEW "
												+ whiteboardNamer.getText()
												+ " " + color.getRed() + " "
												+ color.getGreen() + " "
												+ color.getBlue() + username);
										// TODO: initialize background color
										// stuffs

									}

								} else {

									// if the client chose an existing
									// whiteboard
									// make a canvas with that name

									// TODO: this doesn't actually work lolz

									Canvas canvas = new Canvas(whiteboardNamer
											.getText());
									// TODO: fix the map to either a BiMap or name --> ID (for whiteboards)
									
									out.println("SELECT" + whiteboardNamer.getText());

								}

								// Catch invalid inputs
								// dayum dis is dumb-bitch-proof
							} catch (Exception badConnection) {
								JOptionPane.showMessageDialog(window,
										"Invalid input!");
							}

						}

						// when log in is done close everythannggg
						window.dispose();
					}

				}
			});
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Artist main;
				try {
					main = new Artist();
					main.window.setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});
	}

}
