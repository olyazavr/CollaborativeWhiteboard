package client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * This is the login screen that the user must navigate through to get a canvas.
 * First, they must enter a valid IP to connect to, then they can make a new
 * whiteboard or select an existing one. To make a new whiteboard, the user must
 * supply a username, unique board (if not unique, (1) and then (2) and so on
 * will be appended to the name), and background color. To select a whiteboard,
 * the user only needs to provide a username. The drop-down of whiteboards gets
 * updated every time another client adds a new whiteboard.
 * 
 * This is threadsafe because no information is shared except for the arguments
 * passed to the new Canvas. However, this is ok because as soon as that
 * happens, the Artist is destroyed and thus no longer holds references to those
 * objects. The threads created only share a socket, which is only used to get
 * the output or input stream. To exchange information to and from the server,
 * blocking queues are used, so that information is all processed in an orderly
 * manner. All UI updates are handled in Swing's thread. Moreover, to ensure
 * whiteboard names are unique, they are sent to the server first, and the name
 * that returns is unique and the server will have already made a whiteboard
 * with that name (so no race conditions).
 * 
 * Default port is 4444.
 */
public class Artist {

    private Socket socket;
    private final int port = 4444;
    private List<String> whiteboards;
    private String username;

    private String IP;
    private boolean connected = true;
    private final BlockingQueue<String> inQueue;
    private final BlockingQueue<String> outQueue;

    private final JRadioButton localhost;
    private final JRadioButton otherIP;
    private final JTextField enterIP;
    private final JButton connect;

    private final JLabel usernamePrompt;
    private final JTextField enterUsername;
    private final JLabel board;
    private final JComboBox<String> newBoard;
    private DefaultComboBoxModel<String> whiteboardCombo;

    private final JLabel whiteboardPrompt;
    private final JTextField whiteboardNamer;
    private final JLabel bgColorPrompt;
    private final JComboBox<String> bgColorPicker;
    private final Map<String, Color> colorMap;

    private Color color = Color.WHITE;
    private final JButton GO;
    private final JFrame window;

    /**
     * Creates a new Artist login screen to create/select whiteboards and open
     * Canvases. Default port is 4444.
     * 
     * @param IP
     *            optional IP address if reconnecting, null otherwise
     * 
     * @throws UnknownHostException
     * @throws IOException
     */
    public Artist(String IP) throws UnknownHostException, IOException {
        // Create a log-in screen
        this.window = new JFrame("Login");
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // make it pretty!!
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.IP = IP;
        inQueue = new LinkedBlockingQueue<String>();
        outQueue = new LinkedBlockingQueue<String>();

        JLabel IPprompt = new JLabel("Enter IP address: ");
        localhost = new JRadioButton("localhost", true);
        otherIP = new JRadioButton();
        ButtonGroup ipButtonGroup = new ButtonGroup();
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
        bgColorPicker.setSelectedIndex(1); // white is default

        GO = new JButton("Go!");

        // use GroupLayout
        GroupLayout layout = new GroupLayout(window.getContentPane());
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.window.setLayout(layout);

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

        addListeners();

        // can't enter anything until IP is selected
        toggleWhiteboardSelection(false);

        // if we have an IP, set that IP and get whiteboard names
        if (IP != null) {
            if (!IP.equals("localhost")) {
                otherIP.setEnabled(true);
                enterIP.setText(IP);
            }
            socket = new Socket(IP, port);
            startConnection();
        }

        this.window.pack();
        this.window.setVisible(true);
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
     * Starts the threads to read in and print out information to/from the
     * server. Also sets up the whiteboards. This should only be called whenever
     * the socket has been started.
     */
    private void startConnection() {
        // Thread that reads in from the server, mainly keeping track of new
        // whiteboards
        Thread inCommunication = new Thread(new Runnable() {
            public void run() {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    while (connected) {
                        handleInput(in.readLine());
                    }

                } catch (Exception e) {
                    // socket has been closed by other thread, this is ok
                }
            }
        });

        inCommunication.start();

        // Thread that prints out to the server, mainly informing it of HELLO
        // and BYE messages
        Thread outCommunication = new Thread(new Runnable() {
            public void run() {
                try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    // send initial hello to get whiteboards
                    outQueue.put("HELLO");

                    while (connected) {
                        String message = outQueue.take();
                        out.println(message);

                        // we disconnect!
                        if (message.equals("BYEARTIST")) {
                            connected = false;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        outCommunication.start();

        setupWhiteboards();
    }

    /**
     * Initialize all the whiteboard stuff and set up the new whiteboard option
     */
    private void setupWhiteboards() {
        whiteboards = new ArrayList<String>();
        whiteboardCombo = new DefaultComboBoxModel<String>();
        newBoard.setModel(whiteboardCombo);

        // make a new whiteboard option
        addWhiteboard("New whiteboard");
    }

    /**
     * Adds a whiteboard to the list and the combobox model
     * 
     * @param boardName
     *            name of the whiteboard to add
     */
    private void addWhiteboard(final String boardName) {
        whiteboards.add(boardName);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                whiteboardCombo.addElement(boardName);
            }
        });
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

                // try to start connection with server, enable everything else
                // if succeed
                try {
                    socket = new Socket(IP, port);
                    startConnection();

                } catch (IOException notValidIP) {
                    JOptionPane.showMessageDialog(window, "Please enter a valid IP address and try again");
                }
            }
        });

        // select new whiteboard or create whiteboard
        newBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String choice = (String) newBoard.getSelectedItem();

                // make sure the board is actually selectable atm
                if (newBoard.isVisible() && choice.equals("New whiteboard")) {
                    toggleNewWhiteboard(true);

                } else {
                    toggleNewWhiteboard(false);
                }
            }

        });

        // on close, make sure we tell the server
        window.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                try {
                    outQueue.put("BYEARTIST");
                } catch (Exception e1) {
                    e1.printStackTrace();
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
        // finalize choices and sends info to server
        GO.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                username = enterUsername.getText();
                String whiteboardName = whiteboardNamer.getText();

                // usernames can't be empty and can't contain spaces
                if (username.isEmpty() || containsSpace(username)) {
                    JOptionPane.showMessageDialog(window, "Username cannot be empty and cannot contain spaces.");
                    return;
                }
                // see what they chose
                String choice = (String) newBoard.getSelectedItem();

                // try to make a new whiteboard!
                if (choice.equals("New whiteboard")) {
                    makeNewWhiteboard(username, whiteboardName);

                } else {
                    // if the client chose an existing whiteboard make a
                    // canvas with that name, and close Artist
                    try {
                        new Canvas(choice, IP, color, username);
                        window.dispose();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

    }

    /**
     * Attempts to make a new whiteboard. Name may be changed (a (1) will be
     * appended, then a (2), and so on) if there are duplicates. On success,
     * will close the Artist.
     * 
     * @param userName
     *            VALID username
     * @param whiteboardName
     *            desired name, not necessary valid
     */
    private void makeNewWhiteboard(String userName, String whiteboardName) {
        // sanitize the boardName
        if (containsSpace(whiteboardName) || whiteboardName.isEmpty()) {
            JOptionPane.showMessageDialog(window,
                    "Whiteboard name cannot be empty and cannot contain spaces.");
            return;
        }

        // tell server that we're making a new whiteboard, get the name back in
        // case there were conflicts (ie. if it's taken, "(1)" is appended at
        // the end, and then "(2)" and so on)
        try {
            // "NEW" WB_NAME COLOR_R COLOR_G COLOR_B
            outQueue.put("NEW " + whiteboardName + " " + color.getRed() + " " + color.getGreen() + " "
                    + color.getBlue());
            String name = inQueue.take();

            // make a new whiteboard! Then close Artist
            new Canvas(name, IP, color, username);
            window.dispose();

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Respond to the servers's requests appropriately
     * 
     * Possible requests:
     * 
     * (1) new whiteboard is made ("NEWBOARD" WB_NAME)
     * 
     * (2) the list of whiteboard names ("LIST" WB_NAME WB_NAME)
     * 
     * (3) the new whiteboard name ("NEWNAME" NAME)
     * 
     * @param input
     *            the input to analyze
     */
    private void handleInput(String input) {
        // new whiteboard name
        // "NEWBOARD" WB_NAME
        if (input.startsWith("NEWBOARD")) {
            // (everything but "NEWBOARD ")
            addWhiteboard(input.substring(9));
            return;
        }

        // this is the list of whiteboards
        // "LIST" WB_NAME WB_NAME...
        if (input.startsWith("LIST")) {
            String[] inputSplit = input.split(" ");
            for (int i = 1; i < inputSplit.length; ++i) {
                addWhiteboard(inputSplit[i]);
            }

            // now that we've received whiteboards, make sure we can select them
            toggleWhiteboardSelection(true);
            return;
        }
        // the new whiteboard name to avoid duplicates if that is the case)
        // "NEWNAME" NAME
        if (input.startsWith("NEWNAME")) {
            try {
                // get the name, without the "NEWNAME "
                inQueue.put(input.substring(8));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // things that don't adhere to the grammar were put in here, muy bad
        throw new UnsupportedOperationException();
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
    private boolean containsSpace(String name) {
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // we have no IP to give yet
                    new Artist(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}