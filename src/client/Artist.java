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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Artist {

	private Socket socket;
	private final int port = 4444;
	private List<String> whiteboards;
	// always send the server the whiteboard name
	// private final String whiteboardName;
	private String username;
	private String IP;

	private final JLabel IPprompt;
	private final JRadioButton localhost;
	private final JRadioButton otherIP;
	private final JTextField enterIP;
	private final ButtonGroup ipButtonGroup;
	private final JButton connect;

	private final JLabel usernamePrompt;
	private final JTextField enterUsername;
	private final JLabel board;
	private final JComboBox<String> newBoard;

	private final JLabel whiteboardPrompt;
	private final JTextField whiteboardNamer;
	private final JLabel bgColorPrompt;
	private final JComboBox<String> bgColorPicker;
	private final Map<String, Color> colorMap;

    private Color color = Color.WHITE;
	private final JButton GO;
	private final JFrame window;

	public Artist() throws UnknownHostException, IOException {
		// Create a log-in screen
		this.window = new JFrame("Login");
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		IPprompt = new JLabel("Enter IP address: ");
		localhost = new JRadioButton("localhost", true);
		otherIP = new JRadioButton();
		ipButtonGroup = new ButtonGroup();
		ipButtonGroup.add(localhost);
		ipButtonGroup.add(otherIP);
		
		// default width so it doesn't get shat on by the fatass button
		enterIP = new JTextField(10);
		connect = new JButton("Connect!");

		usernamePrompt = new JLabel("Pick a username:");
		enterUsername = new JTextField();
		board = new JLabel("Choose/create a board: ");

		// The dropdown list to choose a whiteboard is a combobox
		newBoard = new JComboBox<String>();

		// If they want a new board, prompt them to pick name and background color
		whiteboardPrompt = new JLabel("New board name:");
		whiteboardNamer = new JTextField(10);
		bgColorPrompt = new JLabel("with background: ");

		colorMap = makeColors();

		// add all the colors, as well as a custom option
		DefaultComboBoxModel<String> colors = new DefaultComboBoxModel<String>();
		for (String s : colorMap.keySet()) {
			colors.addElement(s);
		}
		colors.addElement("Custom...");

		bgColorPicker = new JComboBox<String>(colors);
		bgColorPicker.setSelectedIndex(1); // white is default

		GO = new JButton("Go!");

		// use GroupLayout
		GroupLayout layout = new GroupLayout(window.getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		this.window.setLayout(layout);

		// can't enter anything until IP is selected
		toggleWhiteboardSelection(false);

		// draw all the shits
		Group row1 = layout.createSequentialGroup()
		        .addComponent(IPprompt)
				.addComponent(localhost)
				.addComponent(otherIP)
				.addComponent(enterIP)
				.addComponent(connect);
		
		Group row2 = layout.createSequentialGroup()
				.addComponent(usernamePrompt)
				.addComponent(enterUsername);
		
		Group row3 = layout.createSequentialGroup()
		        .addComponent(board)
				.addComponent(newBoard);
		
		Group row4 = layout.createSequentialGroup()
				.addComponent(whiteboardPrompt)
				.addComponent(whiteboardNamer)
				.addComponent(bgColorPrompt)
				.addComponent(bgColorPicker);
		
		Group row5 = layout.createSequentialGroup()
		        .addComponent(GO);

		Group horizontal = layout.createSequentialGroup();
		
		horizontal.addGroup(layout
		        .createParallelGroup()
		        .addGroup(row1)
				.addGroup(row2)
				.addGroup(row3)
				.addGroup(row4)
				.addGroup(row5));

		layout.setHorizontalGroup(horizontal);

		Group ver1 = layout.createParallelGroup()
		        .addComponent(IPprompt)
				.addComponent(localhost)
				.addComponent(otherIP)
				.addComponent(enterIP)
				.addComponent(connect);
		
		Group ver2 = layout.createParallelGroup()
				.addComponent(usernamePrompt, 0, 25, Integer.MAX_VALUE)
				.addComponent(enterUsername);
		
		Group ver3 = layout.createParallelGroup()
		        .addComponent(board)
				.addComponent(newBoard);
		
		Group ver4 = layout.createParallelGroup()
				.addComponent(whiteboardPrompt)
				.addComponent(whiteboardNamer)
				.addComponent(bgColorPrompt)
				.addComponent(bgColorPicker);
		
		Group ver5 = layout.createParallelGroup()
		        .addComponent(GO);

		Group vertical = layout
		        .createSequentialGroup();
		
		vertical.addGroup(ver1)
		        .addGroup(ver2)
		        .addGroup(ver3)
		        .addGroup(ver4)
				.addGroup(ver4)
				.addGroup(ver5);
		
		layout.setVerticalGroup(vertical);

		this.window.pack();

		addListeners();
	}

	/**
	 * Toggles whether or not the rest of the whiteboard selection elements
	 * (besides the IP) should be visible. All of these should be invisible
	 * until a valid IP is entered.
	 * 
	 * @param visible
	 *            whether or not the whiteboard selection elements should be
	 *            visible
	 */
	private void toggleWhiteboardSelection(boolean visible) {
		usernamePrompt.setVisible(visible);
		enterUsername.setVisible(visible);
		board.setVisible(visible);
		newBoard.setVisible(visible);
		whiteboardPrompt.setVisible(visible);
		whiteboardNamer.setVisible(visible);
		bgColorPicker.setVisible(visible);
		bgColorPrompt.setVisible(visible);
		GO.setVisible(visible);
		this.window.pack();
	}

	/**
	 * Gets the whiteboard names (all unique) from the server to populate the
	 * drop-down menu, also add the names to the combo box model, setting that
	 * model for the drop down menu
	 * 
	 * @throws IOException
	 */
	private void getWhiteboards() throws IOException {
		whiteboards = new ArrayList<String>();
		// try with multiple resources! this is so hot
		try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(),true)) {

			DefaultComboBoxModel<String> whiteboardCombo = new DefaultComboBoxModel<String>();
			whiteboardCombo.addElement("New whiteboard");

			// send initial hello to get whiteboards
			out.println("HELLO");

			// retrieve the list of whiteboard names
			String[] input = in.readLine().split(" ");

			// add to the list and combobox
			for (int i = 0; i < input.length; ++i) {
				whiteboards.add(input[i]);
				whiteboardCombo.addElement(input[i]);
			}

			newBoard.setModel(whiteboardCombo);
		}
	}

	/**
	 * Makes the color map with all of the standard colors in Color, as well as
	 * MIT's color ;)
	 * 
	 * @return the map of string to color
	 */
	private Map<String, Color> makeColors() {
		Map<String, Color> colorMap = new HashMap<String, Color>();
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

		return colorMap;
	}

	/**
	 * Toggle whether or not the user can see new whiteboard UI elements (if the
	 * user is creating a whiteboard, these should be visible, otherwise they
	 * should not)
	 * 
	 * @param visible
	 *            whether or not the new whiteboard UI stuff should be visible
	 */
	private void toggleNewWhiteboard(boolean visible) {
		whiteboardPrompt.setVisible(visible);
		whiteboardNamer.setVisible(visible);
		bgColorPrompt.setVisible(visible);
		bgColorPicker.setVisible(visible);
		window.pack();
	}

	/**
	 * Adds all the listeners to UI objects
	 */
	private void addListeners() {
		// connect with the IP given, get whiteboards
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// get the IP
				if (localhost.isSelected()) {
					IP = "localhost";
					
				} else {
					IP = enterIP.getText();
				}

				// try to get whiteboard names from server, enable everything
				// else if succeed
				try {
					socket = new Socket(IP, port);
					getWhiteboards();
					toggleWhiteboardSelection(true);
					
				} catch (IOException notValidIP) {
					JOptionPane.showMessageDialog(window,
							"Please enter a valid IP address and try again");
				}
			}
		});

		// select new whiteboard or create whiteboard
		newBoard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String choice = (String) newBoard.getSelectedItem();
				
				if (choice.equals("New whiteboard")) {
					toggleNewWhiteboard(true);
					
				} else {
					toggleNewWhiteboard(false);
				}
			}

		});

		// select background color for new whiteboard
		bgColorPicker.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String bgColor = (String) bgColorPicker.getSelectedItem();
				if (bgColor.equals("Custom...")) {
					color = Color.WHITE;
					JColorChooser.showDialog(new JPanel(), "Choose a color",
							color);

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
				username = enterUsername.getText();
				String whiteboardName = whiteboardNamer.getText();

				// Usernames can't be empty and can't contain space
				if (!username.isEmpty() && !containsSpace(username)) {
					// see what they chose
					String choice = (String) newBoard.getSelectedItem();

					if (choice.equals("New whiteboard")) {

						// if the name has already been taken
						if (whiteboards.contains(whiteboardName)) {
							JOptionPane
									.showMessageDialog(window, "That whiteboard name is taken. Please choose a different one!");

							// if the board name contains space or is empty
						} else if (containsSpace(whiteboardName)
								|| whiteboardName.isEmpty()) {
							JOptionPane
									.showMessageDialog(window, "Whiteboard name cannot be empty and cannot contain spaces.");

						} else {
							// make a new whiteboard!
							try {
                                System.out.println("making new board " + whiteboardName);
                                new Canvas(whiteboardName, IP, color, username, true);
								window.dispose();

							} catch (Exception badConnection) {
                                JOptionPane.showMessageDialog(window, "Invalid IP input!");
							}
						}

					} else {

						// if the client chose an existing whiteboard make a
						// canvas with that name
						System.out.println("selecting board " + choice);
						
						try {
                            new Canvas(choice, IP, color, username, false);
							window.dispose();
							
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}

					// Catch invalid inputs
					// dayum dis is dumb-bitch-proof

					// when log in is done close everythannggg

				} else {
					JOptionPane.showMessageDialog(window,"Username cannot be empty and cannot contain spaces.");
				}

			}
		});

	}

	/**
	 * Checks to see if a string contains whitespace. Used here to make sure
	 * usernames and whiteboard names don't have whitespace in them
	 * 
	 * @param name
	 *            a string
	 * 
	 * @return boolean, true if it contains a space
	 */
	public static boolean containsSpace(String name) {
		Pattern pattern = Pattern.compile("\\s");
		Matcher matcher = pattern.matcher(name);
		return matcher.find();
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