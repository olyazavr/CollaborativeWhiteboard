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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * * Possible outputs:
 * 
 * (1) initial connect message ("HELLO"),
 * 
 * (2) select whiteboard ("SELECT" WB_NAME USER_NAME),
 * 
 * (3) make new whiteboard and select it ("NEW" WB_NAME COLOR_R COLOR_G COLOR_B
 * USER_NAME),
 * 
 * (4) new draw actions ("DRAW" WB_NAME STROKE X Y COLOR_R COLOR_G COLOR_B),
 * 
 * (5) change whiteboard bg color ("BG" WB_NAME COLOR_R COLOR_G COLOR_B),
 * 
 * (6) disconnect message ("BYE" WB_NAME USER_NAME)
 * 
 * Possible inputs:
 * 
 * (1) whiteboard names (WB_NAME WB_NAME...),
 * 
 * (2)-(3) whiteboard specs ("USERS" USER_NAME USER_NAME... "PIXELS" X1 Y1
 * COLOR_R1 COLOR_G1 COLOR_B1 X2 Y2 COLOR_R2 COLOR_G2 COLOR_B2...) to new
 * client, ("NEWUSER" USER_NAME) to others,
 * 
 * (4) new draw actions by others ("DRAW" ARTSY_METER STROKE X Y COLOR_R COLOR_G
 * COLOR_B),
 * 
 * (5) change whiteboard bg color ("BG" COLOR_R COLOR_G COLOR_B),
 * 
 * (6) user leaves ("BYEUSER" USER_NAME)
 */
public class Artist {
    private final Socket socket;
    private final int port = 4444;
    private final List<String> whiteboards;
    // always send the server the whiteboard name
    // private final String whiteboardName;
    private String username;
    private String IP;

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
    private final Map<String, Color> colorMap;

    private Color color;
    private final JButton GO;

    private final JFrame window;

    public Artist() throws UnknownHostException, IOException {
        String ip = "localhost";
        socket = new Socket(ip, port);

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
        DefaultComboBoxModel<String> whiteboardCombo = new DefaultComboBoxModel<String>();

        // get whiteboard names from server
        whiteboards = getWhiteboards();

        // default option (first thing) is a new board
        whiteboardCombo.addElement("New whiteboard");

        // add all whiteboard names!
        for (String board : whiteboards) {
            whiteboardCombo.addElement(board);
        }

        newBoard = new JComboBox<String>(whiteboardCombo);

        // If they want a new board, prompt them to pick name and background
        // color
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

        IPprompt = new JLabel("Enter IP address: ");
        localhost = new JButton("Use localhost");

        // default width so it doesn't get shat on by the fatass button
        enterIP = new JTextField(10);

        GO = new JButton();
        GO.setText("Go!");

        // use GroupLayout
        GroupLayout layout = new GroupLayout(window.getContentPane());
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.window.setLayout(layout);

        // draw all the shits
        Group row1 = layout.createSequentialGroup().addComponent(usernamePrompt)
                .addComponent(enterUsername);
        Group row2 = layout.createSequentialGroup().addComponent(board)
                .addComponent(newBoard);
        Group row3 = layout.createSequentialGroup()
                .addComponent(whiteboardPrompt)
                .addComponent(whiteboardNamer).addComponent(bgColorPrompt)
                .addComponent(bgColorPicker);
        Group row4 = layout.createSequentialGroup().addComponent(IPprompt)
                .addComponent(enterIP).addComponent(localhost);
        Group row5 = layout.createSequentialGroup().addComponent(GO);

        Group horizontal = layout.createSequentialGroup();
        horizontal.addGroup(layout.createParallelGroup().addGroup(row1)
                .addGroup(row2).addGroup(row3).addGroup(row4)
                .addGroup(row5));

        layout.setHorizontalGroup(horizontal);

        Group ver1 = layout.createParallelGroup().addComponent(usernamePrompt)
                .addComponent(enterUsername);
        Group ver2 = layout.createParallelGroup().addComponent(board)
                .addComponent(newBoard);
        Group ver3 = layout.createParallelGroup().addComponent(whiteboardPrompt)
                .addComponent(whiteboardNamer).addComponent(bgColorPrompt)
                .addComponent(bgColorPicker);
        Group ver4 = layout.createParallelGroup().addComponent(IPprompt)
                .addComponent(enterIP).addComponent(localhost);
        Group ver5 = layout.createParallelGroup().addComponent(GO);

        Group vertical = layout.createSequentialGroup();
        vertical.addGroup(ver1).addGroup(ver2).addGroup(ver3)
                .addGroup(ver4).addGroup(ver4).addGroup(ver5);
        layout.setVerticalGroup(vertical);

        this.window.pack();

        addListeners();

    }

    /**
     * Gets the whiteboard names (all unique) from the server to populate the
     * drop-down menu
     * 
     * @return a list of names of the whiteboards
     * @throws IOException
     */
    private List<String> getWhiteboards() throws IOException {
        List<String> whiteboards = new ArrayList<String>();
        // try with multiple resources! this is so hot
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // send initial hello to get whiteboards
            out.println("HELLO");

            // retrieve the list of whiteboard names
            String[] input = in.readLine().split(" ");

            for (int i = 0; i < input.length; ++i) {
                whiteboards.add(input[i]);
            }
        }
        return whiteboards;
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
     * Adds all the listeners to UI objects
     */
    private void addListeners() {
        // the localhost button sets the text to local host, parse later
        localhost.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                enterIP.setText("localhost");
            }

        });

        // select new whiteboard or create whiteboard
        newBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String choice = (String) newBoard.getSelectedItem();
                if (!choice.equals("New whiteboard")) {
                    // TODO: find some way to hide the shits without making
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

        // select background color for new whiteboard
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
                if (!enterUsername.getText().isEmpty() && !enterIP.getText().isEmpty()) {
                    username = enterUsername.getText();
                    IP = enterIP.getText();

                    // see what they chose
                    String choice = (String) newBoard.getSelectedItem();

                    try {
                        if (choice.equals("New whiteboard")) {
                            // if the name has already been taken
                            if (whiteboards.contains(whiteboardNamer.getText())) {
                                JOptionPane.showMessageDialog(window,
                                        "That whiteboard name is taken. Please choose a different one!");

                            } else {
                                // make a new whiteboard!
                                System.out.println("NEW "
                                        + whiteboardNamer.getText()
                                        + " " + color.getRed() + " "
                                        + color.getGreen() + " "
                                        + color.getBlue() + username);
                                // TODO: initialize background color
                                // stuffs

                            }

                        } else {
                            // if the client chose an existing whiteboard make a
                            // canvas with that name
                            System.out.println("SELECT" + whiteboardNamer.getText());
                        }

                        // TODO: this doesn't actually work lolz
                        // Canvas canvas = new Canvas(whiteboardNamer.getText(),
                        // socket);

                        // Catch invalid inputs
                        // dayum dis is dumb-bitch-proof
                    } catch (Exception badConnection) {
                        JOptionPane.showMessageDialog(window, "Invalid input!");
                    }

                    // when log in is done close everythannggg
                    window.dispose();
                }

            }
        });

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
