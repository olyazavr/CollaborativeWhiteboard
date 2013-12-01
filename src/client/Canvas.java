package client;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Canvas represents a drawing surface that allows the user to draw on it
 * freehand, with the mouse.
 */
public class Canvas extends JPanel {
    private static final long serialVersionUID = 1L;
    private final int port = 4444; // default port
    private Socket socket;

    // image where the user's drawing is stored
    private Image drawingBuffer;
    private Color color = Color.BLACK;
    private Color bgColor;
    private int stroke = 3;
    private boolean erasing = false;
    private final String name;
    private final String user;
    private final boolean newWhiteboard;
    private final DefaultTableModel playersModel;

    private final int BUTTON_WIDTH = 100;
    private final int BUTTON_HEIGHT = 50;
    private final int TABLE_WIDTH = 180;
    private final int TABLE_HEIGHT = 330;
    private final int SLIDER_MIN = 1;
    private final int SLIDER_MAX = 5;
    private final int SLIDER_INIT = 2;
    private final int WINDOW_WIDTH = 1010;
    private final int WINDOW_HEIGHT = 600;
    private final int CANVAS_WIDTH = 800;
    private final int CANVAS_HEIGHT = 600;
    private final int SIDE_PANEL_WIDTH = 200;
    private final int SIDE_PANEL_HEIGHT = 600;
    private final Color MIT = new Color(163, 31, 52);

    private final JPanel colorPallet;
    private final JButton buttonBlack;
    private final JButton buttonDarkGray;
    private final JButton buttonGray;
    private final JButton buttonLightGray;
    private final JButton buttonWhite;
    private final JButton buttonRed;
    private final JButton buttonOrange;
    private final JButton buttonYellow;
    private final JButton buttonGreen;
    private final JButton buttonBlue;
    private final JButton buttonMITcolor;
    private final JButton buttonMagenta;
    private final JButton buttonPink;
    private final JButton buttonCyan;
    private final JButton buttonMore;

    private final BlockingQueue<String> inQueue;
    private final BlockingQueue<String> outQueue;
    private boolean connected = true;

    public Canvas(String boardName, String IP, Color bgColor, String userName, boolean newWhiteboard)
            throws UnknownHostException, IOException {
        socket = new Socket(IP, port);
        inQueue = new LinkedBlockingQueue<String>();
        outQueue = new LinkedBlockingQueue<String>();
        playersModel = new DefaultTableModel();
        this.name = boardName;
        this.bgColor = bgColor;
        this.user = userName;
        this.newWhiteboard = newWhiteboard;

        // Thread that reads in from the server, mainly keeping track of new
        // draw events
        Thread inCommunication = new Thread(new Runnable() {
            public void run() {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    while (connected) {
                        String input = in.readLine();
                        System.out.println("canvas has read in " + input + " for user " + user);

                        // this is on init
                        if (input.contains("ACTIONS")) {
                            inQueue.put(input);
                        } else {
                            handleRequest(input);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        inCommunication.start();

        // Thread that prints out to the server, mainly informing it of new draw
        // events
        Thread outCommunication = new Thread(new Runnable() {
            public void run() {
                try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    while (connected) {
                        out.println(outQueue.take());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        outCommunication.start();

        // Main Window creation
        JFrame window = new JFrame("Whiteboard: " + boardName);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout windowLayout = new BorderLayout();
        window.setLayout(windowLayout);

        // Container and Canvas creation
        this.setPreferredSize(new Dimension(800, 600));
        addDrawingController();
        JPanel sidePanel = new JPanel();
        JPanel paintButtonContainer = new JPanel();
        JPanel eraserButtonContainer = new JPanel();
        colorPallet = new JPanel();

        // components of the side panel
        final Label sliderLabel = new Label("Stroke Size:");
        final JSlider strokeSlider = new JSlider(SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
        final Button paintButton = new Button("Draw!");
        final Button eraserButton = new Button("Erase!");
        final Label tableLabel = new Label("List of Artists:");
        final JTable playerList = new JTable(playersModel);

        final JScrollPane scrollList = new JScrollPane(playerList);

        // Color pallet buttons
        buttonBlack = new JButton();
        buttonDarkGray = new JButton();
        buttonGray = new JButton();
        buttonLightGray = new JButton();
        buttonWhite = new JButton();
        buttonRed = new JButton();
        buttonOrange = new JButton();
        buttonYellow = new JButton();
        buttonGreen = new JButton();
        buttonBlue = new JButton();
        buttonMITcolor = new JButton();
        buttonMagenta = new JButton();
        buttonPink = new JButton();
        buttonCyan = new JButton();
        buttonMore = new JButton("...");

        // setup their properties and listeners
        setupButtons();

        // creating layouts and customizing components
        GridLayout palletLayout = new GridLayout(3, 5);
        BoxLayout panelLayout = new BoxLayout(sidePanel, BoxLayout.Y_AXIS);
        BoxLayout pButtonLayout = new BoxLayout(paintButtonContainer, BoxLayout.Y_AXIS);
        BoxLayout eButtonLayout = new BoxLayout(eraserButtonContainer, BoxLayout.Y_AXIS);
        window.add(this, BorderLayout.WEST);
        window.add(sidePanel, BorderLayout.EAST);
        playerList.setFillsViewportHeight(true);
        playerList.setTableHeader(null);
        strokeSlider.setMajorTickSpacing(1);
        strokeSlider.setPaintTicks(true);
        strokeSlider.setPaintLabels(true);

        // apply layouts to containers
        paintButtonContainer.setLayout(pButtonLayout);
        eraserButtonContainer.setLayout(eButtonLayout);
        colorPallet.setLayout(palletLayout);
        sidePanel.setLayout(panelLayout);

        // adding components to the button container
        paintButtonContainer.add(paintButton);
        eraserButtonContainer.add(eraserButton);

        // adding components to the side panel
        sidePanel.add(sliderLabel);
        sidePanel.add(strokeSlider);
        sidePanel.add(paintButtonContainer);
        sidePanel.add(eraserButtonContainer);
        sidePanel.add(colorPallet);
        sidePanel.add(tableLabel);
        sidePanel.add(scrollList);

        // borders and dimensions
        Dimension buttonDimension = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        Dimension tableDimension = new Dimension(TABLE_WIDTH, TABLE_HEIGHT);
        Dimension sidePanelDimension = new Dimension(SIDE_PANEL_WIDTH, SIDE_PANEL_HEIGHT);
        window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        sidePanel.setPreferredSize(sidePanelDimension);
        sidePanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 20, 5));
        colorPallet.setMaximumSize(new Dimension(200, 100));
        paintButtonContainer.setBorder(BorderFactory.createEmptyBorder(25, 0, 12, 0));
        eraserButtonContainer.setBorder(BorderFactory.createEmptyBorder(13, 0, 25, 0));
        paintButton.setPreferredSize(buttonDimension);
        eraserButton.setPreferredSize(buttonDimension);
        scrollList.setPreferredSize(tableDimension);

        // Create segoe font from the font file
        Font segoe;

        try {
            segoe = Font.createFont(Font.TRUETYPE_FONT, new File("files/SEGOEUI.TTF"));
        } catch (FontFormatException | IOException e1) {
            throw new RuntimeException("files/SEGOEUI.TTF has been either tampered or removed");
        }

        // set fonts
        sliderLabel.setFont(segoe.deriveFont(20f));
        paintButton.setFont(segoe.deriveFont(35f));
        eraserButton.setFont(segoe.deriveFont(35f));
        tableLabel.setFont(segoe.deriveFont(20f));

        window.setResizable(false);
        window.setVisible(true);

    }

    /**
     * Sets up the buttons, makes them look what they should look like and adds
     * listeners to them
     */
    private void setupButtons() {
        // set colors to buttons
        buttonBlack.setBackground(Color.BLACK);
        buttonDarkGray.setBackground(Color.DARK_GRAY);
        buttonGray.setBackground(Color.GRAY);
        buttonLightGray.setBackground(Color.LIGHT_GRAY);
        buttonWhite.setBackground(Color.WHITE);
        buttonRed.setBackground(Color.RED);
        buttonOrange.setBackground(Color.ORANGE);
        buttonYellow.setBackground(Color.YELLOW);
        buttonGreen.setBackground(Color.GREEN);
        buttonBlue.setBackground(Color.BLUE);
        buttonMITcolor.setBackground(MIT);
        buttonMagenta.setBackground(Color.MAGENTA);
        buttonPink.setBackground(Color.PINK);
        buttonCyan.setBackground(Color.CYAN);

        // set up the map
        Map<JButton, Color> colorButtons = new HashMap<JButton, Color>();
        colorButtons.put(buttonBlack, Color.BLACK);
        colorButtons.put(buttonDarkGray, Color.DARK_GRAY);
        colorButtons.put(buttonGray, Color.GRAY);
        colorButtons.put(buttonLightGray, Color.LIGHT_GRAY);
        colorButtons.put(buttonRed, Color.RED);
        colorButtons.put(buttonOrange, Color.ORANGE);
        colorButtons.put(buttonYellow, Color.YELLOW);
        colorButtons.put(buttonGreen, Color.GREEN);
        colorButtons.put(buttonBlue, Color.BLUE);
        colorButtons.put(buttonMITcolor, MIT);
        colorButtons.put(buttonMagenta, Color.MAGENTA);
        colorButtons.put(buttonPink, Color.PINK);
        colorButtons.put(buttonCyan, Color.CYAN);

        // set properties of all buttons
        for (final Entry<JButton, Color> button : colorButtons.entrySet()) {
            // This removes the default buttons
            button.getKey().setBorderPainted(false);
            // This makes the background visible
            button.getKey().setOpaque(true);

            // adding components to the pallet
            colorPallet.add(button.getKey());

            button.getKey().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!erasing)
                        color = button.getValue();
                }
            });
        }

        // taking care of buttonMore
        colorPallet.add(buttonMore);
        buttonMore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!erasing)
                    color = JColorChooser.showDialog(new JPanel(), "Choose a color", color);
            }
        });
    }

    /**
     * Send the server the message to create/select a whiteboard, if it's a new
     * whiteboard, nothing is returned, if it's an existing one, set the bg
     * color and users list and draw the actions.
     */
    private void setupWhiteboard() {
        try {
            if (newWhiteboard) {
                // "NEW" WB_NAME COLOR_R COLOR_G COLOR_B USER_NAME
                outQueue.put("NEW " + name + " " + bgColor.getRed() + " " + bgColor.getGreen() + " "
                        + bgColor.getBlue() + " " + user);
                fillWithChoice();
            } else {
                // "SELECT" WB_NAME USER_NAME
                outQueue.put("SELECT " + name + " " + user);

                // BG_RED BG_GREEN BG_BLUE ARTSY_METER "USERS" USER_NAME
                // USER_NAME... "ACTIONS" X1 Y1 X2 Y2 STROKE COLOR_R COLOR_G
                // COLOR_B X1 Y1 X2 Y2 STROKE COLOR_R COLOR_G COLOR_B...
                String[] totalInput = inQueue.take().split(" ACTIONS ");
                String[] usersInput = totalInput[0].split(" ");
                List<String> usersList = new ArrayList<String>();

                int red = new Integer(usersInput[0]);
                int green = new Integer(usersInput[1]);
                int blue = new Integer(usersInput[2]);
                int artsy = new Integer(usersInput[3]);
                // TODO: artsy meter??
                bgColor = new Color(red, green, blue);
                fillWithChoice();

                // draw the actions if there are actions to draw
                if (totalInput.length > 1) {
                    parseActions(totalInput[1], false);
                }

                // add users to the playersModel
                for (int i = 5; i < usersInput.length; ++i) {
                    String[] row = new String[1];
                    row[0] = usersInput[i];
                    playersModel.addRow(row);

                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes in a stream of actions and draws them. This needs to be called on
     * init if this is an existing board, or every time a new draw action is
     * recieved
     * 
     * @param input
     *            X1 Y1 X2 Y2 STROKE COLOR_R COLOR_G COLOR_B X1 Y1 X2 Y2 STROKE
     *            COLOR_R COLOR_G COLOR_B...
     * @param withArtsy
     *            whether or not we expect an artsy meter in the beginning
     */
    private void parseActions(String input, boolean withArtsy) {
        // one more thing if we have artsy
        int numOfItems = withArtsy ? 8 : 7;

        String[] pixelsInput = input.split(" ");
        for (int i = numOfItems; i < pixelsInput.length; i += numOfItems + 1) {
            int artsy = 0;
            if (withArtsy) {
                artsy = new Integer(pixelsInput[i - 8]);
            }
            int x1 = new Integer(pixelsInput[i - 7]);
            int y1 = new Integer(pixelsInput[i - 6]);
            int x2 = new Integer(pixelsInput[i - 5]);
            int y2 = new Integer(pixelsInput[i - 4]);
            int stroke = new Integer(pixelsInput[i - 3]);
            int red = new Integer(pixelsInput[i - 2]);
            int green = new Integer(pixelsInput[i - 1]);
            int blue = new Integer(pixelsInput[i]);

            if (withArtsy) {
                // TODO: artsy??
            }

            // draw it!
            drawLineSegment(x1, y1, x2, y2, new Color(red, green, blue), stroke);
        }
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        // If this is the first time paintComponent() is being called,
        // make our drawing buffer.
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }

        // Copy the drawing buffer to the screen.
        g.drawImage(drawingBuffer, 0, 0, null);
    }

    /*
     * Make the drawing buffer and draw some starting content for it.
     */
    private void makeDrawingBuffer() {
        drawingBuffer = createImage(getWidth(), getHeight());
        // set up the whiteboard before we paint!
        setupWhiteboard();
        drawSmile();
    }

    /*
     * Make the drawing buffer entirely white.
     */
    private void fillWithChoice() {
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        g.setColor(bgColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        // so color
        // much pixel
        // many image
        // wow.
        Image img = null;
        try {
            img = ImageIO.read(new File("files/DOGE.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        g.drawImage(img, (800 - 550) / 2, (600 - 550) / 2, null);

        // IMPORTANT! every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }

    /*
     * Draw a happy smile on the drawing buffer.
     */
    private void drawSmile() {
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        // all positions and sizes below are in pixels
        final Rectangle smileBox = new Rectangle(20, 20, 100, 100); // x, y,
                                                                    // width,
                                                                    // height
        final Point smileCenter = new Point(smileBox.x + smileBox.width / 2, smileBox.y + smileBox.height / 2);
        final int smileStrokeWidth = 3;
        final Dimension eyeSize = new Dimension(9, 9);
        final Dimension eyeOffset = new Dimension(smileBox.width / 6, smileBox.height / 6);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(smileStrokeWidth));

        // draw the smile -- an arc inscribed in smileBox, starting at -30
        // degrees (southeast)
        // and covering 120 degrees
        g.drawArc(smileBox.x, smileBox.y, smileBox.width, smileBox.height, -30, -120);

        // draw some eyes to make it look like a smile rather than an arc
        for (int side : new int[] { -1, 1 }) {
            g.fillOval(smileCenter.x + side * eyeOffset.width - eyeSize.width / 2,
                    smileCenter.y - eyeOffset.height - eyeSize.width / 2,
                    eyeSize.width,
                    eyeSize.height);
        }

        // IMPORTANT! every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }

    /*
     * Draw a line between two points (x1, y1) and (x2, y2), specified in pixels
     * relative to the upper-left corner of the drawing buffer.
     */
    private void drawLineSegment(int x1, int y1, int x2, int y2, Color color, int stroke) {
        Graphics2D graphics = (Graphics2D) drawingBuffer.getGraphics();

        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(stroke));
        graphics.drawLine(x1, y1, x2, y2);

        // IMPORTANT! every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }

    /*
     * Add the mouse listener that supports the user's freehand drawing.
     */
    private void addDrawingController() {
        DrawingController controller = new DrawingController();
        addMouseListener(controller);
        addMouseMotionListener(controller);
    }

    /*
     * DrawingController handles the user's freehand drawing.
     */
    private class DrawingController implements MouseListener, MouseMotionListener {
        // store the coordinates of the last mouse event, so we can
        // draw a line segment from that last point to the point of the next
        // mouse event.
        private int lastX, lastY;

        /*
         * When mouse button is pressed down, start drawing.
         */
        public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
        }

        /*
         * When mouse moves while a button is pressed down, draw a line segment.
         */
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            // put the draw action on the queue!
            try {
                // "DRAW" WB_NAME X1 Y1 X2 Y2 STROKE COLOR_R COLOR_G COLOR_B
                outQueue.put("DRAW " + name + " " + lastX + " " + lastY + " " + x + " " + y + " " + stroke + " "
                        + color.getRed() + " " + color.getGreen() + " " + color.getBlue());
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            lastX = x;
            lastY = y;
        }

        // Ignore all these other mouse events.
        public void mouseMoved(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    /**
     * Respond to the servers's requests appropriately
     * 
     * Possible requests:
     * 
     * (1) new draw actions ("DRAW" ARTSY_METER X1 Y1 X1 Y2 STROKE COLOR_R
     * COLOR_G COLOR_B),
     * 
     * (2) change whiteboard bg color ("BG" COLOR_R COLOR_G COLOR_B),
     * 
     * (3) user leaves ("BYEUSER" USER_NAME)
     * 
     * @param input
     *            the server's request
     */
    private void handleRequest(String input) {
        String[] inputSplit = input.split(" ");

        // new draw action
        // "DRAW" ARTSY_METER X1 Y1 X1 Y2 STROKE COLOR_R COLOR_G COLOR_B
        if (inputSplit[0].equals("DRAW")) {
            // everything but "DRAW ", also we have artsy!!
            parseActions(input.substring(5), true);
            return;
        }

        // change background color
        // "BG" COLOR_R COLOR_G COLOR_B
        if (inputSplit[0].equals("BG")) {
            int red = new Integer(inputSplit[1]);
            int green = new Integer(inputSplit[2]);
            int blue = new Integer(inputSplit[3]);

            bgColor = new Color(red, green, blue);
            return;
        }

        // user leaves
        // "BYEUSER" USER_NAME
        if (inputSplit[0].equals("BYEUSER")) {
            String userName = inputSplit[1];

            // find the user, remove them
            for (int i = 0; i < playersModel.getRowCount(); ++i) {
                if (userName.equals(playersModel.getValueAt(i, 0))) {
                    playersModel.removeRow(i);
                    break;
                }
            }
            return;
        }

        // things that don't adhere to the grammar were put in here, muy bad
        throw new UnsupportedOperationException();
    }
}
