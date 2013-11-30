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
import java.util.Arrays;
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

        // Thread that reads in from the server, mainly keeping track of new
        // draw events
        Thread inCommunication = new Thread(new Runnable() {
            public void run() {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    while (connected) {
                        String input = in.readLine();
                        System.out.println(input);
                        inQueue.put(input);
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

        // set up the whiteboard before we paint!
        if (newWhiteboard) {
            setupWhiteboard(true);
        } else {
            // inits is bgColor, usersList, pixelsMap
            List<Object> inits = setupWhiteboard(false);
            bgColor = (Color) inits.get(0);

            // add all names to the model
            for (String name : (List<String>) inits.get(1)) {
                String[] row = new String[1];
                row[0] = name;
                playersModel.addRow(row);
            }

            drawPixels((Map<List<Integer>, Color>) inits.get(2));
        }

        socket = new Socket(IP, port);

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
     * whiteboard, nothing is returned, if it's an existing one, get the bg
     * color, users list, and pixels
     * 
     * @param newWB
     *            whether or not the whiteboard is newly created, or already
     *            existing
     * 
     * @return null if new board, list of: bg color, list of users, and map of
     *         (x, y) to Color (ie. pixel colors)
     */
    private List<Object> setupWhiteboard(boolean newWB) {
        try {
            if (newWB) {
                // "NEW" WB_NAME COLOR_R COLOR_G COLOR_B USER_NAME
                outQueue.put("NEW " + name + " " + bgColor.getRed() + " " + bgColor.getGreen() + " "
                        + bgColor.getBlue() + " " + user);
            } else {
                // "SELECT" WB_NAME USER_NAME

                outQueue.put("SELECT " + name + " " + user);

                // BG_RED BG_GREEN BG_BLUE "USERS" USER_NAME USER_NAME...
                // "PIXELS" X1 Y1 COLOR_R1 COLOR_G1 COLOR_B1 X2 Y2 COLOR_R2
                // COLOR_G2 COLOR_B2...
                String[] totalInput = inQueue.take().split(" PIXELS ");
                String[] usersInput = totalInput[0].split(" ");
                List<String> usersList = new ArrayList<String>();
                Map<List<Integer>, Color> pixels = parseActions(totalInput[1]);
                for (int i = 4; i < usersInput.length; ++i) {
                    usersList.add(usersInput[i]);
                }

                int red = new Integer(usersInput[0]);
                int green = new Integer(usersInput[1]);
                int blue = new Integer(usersInput[2]);
                Color bg = new Color(red, green, blue);

                return Arrays.asList(bg, usersList, pixels);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Converts a string of x1 y1 x2 y2 stroke r g b... to
     * 
     * @param input
     *            X1 Y1 COLOR_R1 COLOR_G1 COLOR_B1 X2 Y2 COLOR_R2 COLOR_G2
     *            COLOR_B2
     * @return a map of (x,y) to Color
     */
    private Map<List<Integer>, Color> parseActions(String input) {
        String[] pixelsInput = input.split(" ");
        Map<List<Integer>, Color> pixels = new HashMap<List<Integer>, Color>();
        for (int i = 4; i < pixelsInput.length; i += 5) {
            int x = new Integer(pixelsInput[i - 4]);
            int y = new Integer(pixelsInput[i - 3]);
            int red = new Integer(pixelsInput[i - 2]);
            int green = new Integer(pixelsInput[i - 1]);
            int blue = new Integer(pixelsInput[i]);
            pixels.put(Arrays.asList(x, y), new Color(red, green, blue));
        }

        return pixels;
    }

    /**
     * Draws pixels on the board (used when an existing board is selected)
     * 
     * @param pixels
     *            map of (x,y) to Color
     */
    private void drawPixels(Map<List<Integer>, Color> pixels) {
        // TODO: do this shit
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
        fillWithChoice();
        drawSmile();
    }

    /*
     * Make the drawing buffer entirely white.
     * 
     * TODO: this still needs to talk to server and get the starting color of
     * all the pixels
     */
    private void fillWithChoice() {
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        g.setColor(Color.WHITE);
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
    private void drawLineSegment(int x1, int y1, int x2, int y2) {
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
            drawLineSegment(lastX, lastY, x, y);
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
}
