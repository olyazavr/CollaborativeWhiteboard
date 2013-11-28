package client;

import java.awt.BorderLayout;
import java.awt.Container;
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
	private final JComboBox newBoard;

	private final JLabel IPprompt;
	private final JTextField enterIP;
	private final JButton localhost;

	private final JButton GO;

	private final GroupLayout layout;
	private final Group row1;
	private final Group row2;
	private final Group row3;
	private final Group row4;
	private final Group horizontal;

	private final Group ver1;
	private final Group ver2;
	private final Group ver3;
	private final Group ver4;
	private final Group vertical;

	private final JFrame window;

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
			Map<Integer, String> whiteboardList = new HashMap<Integer, String>();
			String[] input = in.readLine().split(" ");
			System.out.println("read something in!");

			for (int i = 1; i < input.length; i += 2) {
				System.out.println(input[i - 1] + " " + input[i]);
				whiteboardList.put(new Integer(input[i - 1]), input[i]);
			}

			// select the whiteboard (name) we want, give the server the id of
			// it (look it up) if we're selecting (not creating)
			whiteboardID = 0;

		}

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

		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement("New whiteboard");
		newBoard = new JComboBox(model);

		// TODO: parse whiteboard name shits from the server and do a loop and
		// add
		// them here

		IPprompt = new JLabel();
		IPprompt.setText("Enter IP address: ");

		enterIP = new JTextField(10);

		localhost = new JButton();
		localhost.setText("Use localhost");

		localhost.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				enterIP.setText("localhost");

			}

		});

		GO = new JButton();
		GO.setText("Go!");

		layout = new GroupLayout(window.getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		this.window.setLayout(layout);

		row1 = layout.createSequentialGroup().addComponent(usernamePrompt)
				.addComponent(enterUsername);
		row2 = layout.createSequentialGroup().addComponent(board)
				.addComponent(newBoard);
		row3 = layout.createSequentialGroup().addComponent(IPprompt)
				.addComponent(enterIP).addComponent(localhost);
		row4 = layout.createSequentialGroup().addComponent(GO);

		horizontal = layout.createSequentialGroup();
		horizontal.addGroup(layout.createParallelGroup().addGroup(row1)
				.addGroup(row2).addGroup(row3).addGroup(row4));
		layout.setHorizontalGroup(horizontal);

		ver1 = layout.createParallelGroup().addComponent(usernamePrompt)
				.addComponent(enterUsername);
		ver2 = layout.createParallelGroup().addComponent(board)
				.addComponent(newBoard);
		ver3 = layout.createParallelGroup().addComponent(IPprompt)
				.addComponent(enterIP).addComponent(localhost);
		ver4 = layout.createParallelGroup().addComponent(GO);

		vertical = layout.createSequentialGroup();
		vertical.addGroup(ver1).addGroup(ver2).addGroup(ver3).addGroup(ver4);
		layout.setVerticalGroup(vertical);

		this.window.pack();

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
