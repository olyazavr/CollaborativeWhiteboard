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
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Canvas represents a drawing surface that allows the user to draw
 * on it freehand, with the mouse.
 */
public class Canvas extends JPanel {
    // image where the user's drawing is stored
    private Image drawingBuffer;
    private static Color color = Color.BLACK;
    private static Color userColor = Color.BLACK;
    private static int stroke = 3;
    private static int userStroke = 3;
    private static boolean erasing = false;
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
    
    
    /**
     * Make a canvas.
     * @param width width in pixels
     * @param height height in pixels
     */
    public Canvas(int width, int height) {
        this.setPreferredSize(new Dimension(width, height));
        addDrawingController();
        // note: we can't call makeDrawingBuffer here, because it only
        // works *after* this canvas has been added to a window.  Have to
        // wait until paintComponent() is first called.
    }
    
    public Canvas() {
        // Main Window creation
        JFrame window = new JFrame("Whiteboard #"); //TODO: Query server for whiteboard number
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout windowLayout = new BorderLayout();
        window.setLayout(windowLayout);
        
        // Container and Canvas creation
        Canvas canvas = new Canvas(800, 600);
        JPanel sidePanel = new JPanel();
        JPanel paintButtonContainer = new JPanel();
        JPanel eraserButtonContainer = new JPanel();
        JPanel colorPallet = new JPanel();
        
        // components of the side panel
        final Label sliderLabel = new Label("Stroke Size:");
        final JSlider strokeSlider = new JSlider(SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
        final Button paintButton = new Button("Draw!");
        final Button eraserButton = new Button("Erase!");
        final Label tableLabel = new Label("List of Artists:");
        final JTable playerList = new JTable(0, 1); //TODO: Add table-server comm
        final JScrollPane scrollList = new JScrollPane(playerList);
        
        //Color pallet buttons
        JButton button1 = new JButton();
        JButton button2 = new JButton();
        JButton button3 = new JButton();
        JButton button4 = new JButton();
        JButton button5 = new JButton();
        JButton button6 = new JButton();
        JButton button7 = new JButton();
        JButton button8 = new JButton();
        JButton button9 = new JButton();
        JButton button10 = new JButton();
        JButton button11 = new JButton();
        JButton button12 = new JButton();
        JButton button13 = new JButton();
        JButton button14 = new JButton();
        JButton button15 = new JButton("...");
        
        // set colors to buttons
        button1.setBackground(Color.BLACK);
        button2.setBackground(Color.DARK_GRAY);
        button3.setBackground(Color.GRAY);
        button4.setBackground(Color.LIGHT_GRAY);
        button5.setBackground(Color.WHITE);
        button6.setBackground(Color.RED);
        button7.setBackground(Color.ORANGE);
        button8.setBackground(Color.YELLOW);
        button9.setBackground(Color.GREEN);
        button10.setBackground(Color.BLUE);
        button11.setBackground(MIT);
        button12.setBackground(Color.MAGENTA);
        button13.setBackground(Color.PINK);
        button14.setBackground(Color.CYAN);
        
        // This removes the default buttons
        button1.setBorderPainted(false);
        button2.setBorderPainted(false);
        button3.setBorderPainted(false);
        button4.setBorderPainted(false);
        button5.setBorderPainted(false);
        button6.setBorderPainted(false);
        button7.setBorderPainted(false);
        button8.setBorderPainted(false);
        button9.setBorderPainted(false);
        button10.setBorderPainted(false);
        button11.setBorderPainted(false);
        button12.setBorderPainted(false);
        button13.setBorderPainted(false);
        button14.setBorderPainted(false);
        button15.setBorderPainted(false);

        // This makes the background visible
        button1.setOpaque(true);
        button2.setOpaque(true);
        button3.setOpaque(true);
        button4.setOpaque(true);
        button5.setOpaque(true);
        button6.setOpaque(true);
        button7.setOpaque(true);
        button8.setOpaque(true);
        button9.setOpaque(true);
        button10.setOpaque(true);
        button11.setOpaque(true);
        button12.setOpaque(true);
        button13.setOpaque(true);
        button14.setOpaque(true);
        
        // Adding action listeners to the buttons and sliders
        paintButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                erasing = false;
                color = userColor;
                stroke = userStroke;
            }
        });
        
        eraserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                erasing = true;
                userColor = color;
                userStroke = stroke;
                color = Color.WHITE;
                stroke = userStroke * 5;
            }
        });
        
        strokeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                if(!erasing) stroke = strokeSlider.getValue() * 2 - 1;
            }
        });
        
        // Color Buttons
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.BLACK;}
        });
        
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.DARK_GRAY;}
        });
        
        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.GRAY;}
        });
        
        button4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.LIGHT_GRAY;}
        });
        
        button5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.WHITE;}
        });
        
        button6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.RED;}
        });
        
        button7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.ORANGE;}
        });
        
        button8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.YELLOW;}
        });
        
        button9.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.GREEN;}
        });
        
        button10.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.BLUE;}
        });
        
        button11.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = MIT;}
        });
        
        button12.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.MAGENTA;}
        });
        
        button13.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.PINK;}
        });
        
        button14.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {if(!erasing) color = Color.CYAN;}
        });
        
        button15.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!erasing) color = JColorChooser.showDialog(new JPanel(), "Choose a color", color);
            }
        });
        
        // creating layouts and customizing components
        GridLayout palletLayout = new GridLayout(3, 5);
        BoxLayout panelLayout = new BoxLayout(sidePanel, BoxLayout.Y_AXIS);
        BoxLayout pButtonLayout = new BoxLayout(paintButtonContainer, BoxLayout.Y_AXIS);
        BoxLayout eButtonLayout = new BoxLayout(eraserButtonContainer, BoxLayout.Y_AXIS);
        window.add(canvas, BorderLayout.WEST);
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
        
        // adding components to the pallet
        colorPallet.add(button1);
        colorPallet.add(button2);
        colorPallet.add(button3);
        colorPallet.add(button4);
        colorPallet.add(button5);
        colorPallet.add(button6);
        colorPallet.add(button7);
        colorPallet.add(button8);
        colorPallet.add(button9);
        colorPallet.add(button10);
        colorPallet.add(button11);
        colorPallet.add(button12);
        colorPallet.add(button13);
        colorPallet.add(button14);
        colorPallet.add(button15);
        
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
        canvas.setSize(CANVAS_WIDTH,  CANVAS_HEIGHT);
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
        fillWithWhite();
        drawSmile();
    }
    
    /*
     * Make the drawing buffer entirely white.
     */
    private void fillWithWhite() {
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0,  0,  getWidth(), getHeight());
        
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
        
        g.drawImage(img, (800 - 550) / 2 , (600 - 550) / 2, null);
        
        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }
    
    /*
     * Draw a happy smile on the drawing buffer.
     */
    private void drawSmile() {
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        // all positions and sizes below are in pixels
        final Rectangle smileBox = new Rectangle(20, 20, 100, 100); // x, y, width, height
        final Point smileCenter = new Point(smileBox.x + smileBox.width/2, smileBox.y + smileBox.height/2);
        final int smileStrokeWidth = 3;
        final Dimension eyeSize = new Dimension(9, 9);
        final Dimension eyeOffset = new Dimension(smileBox.width/6, smileBox.height/6);
        
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(smileStrokeWidth));
        
        // draw the smile -- an arc inscribed in smileBox, starting at -30 degrees (southeast)
        // and covering 120 degrees
        g.drawArc(smileBox.x, smileBox.y, smileBox.width, smileBox.height, -30, -120);
        
        // draw some eyes to make it look like a smile rather than an arc
        for (int side: new int[] { -1, 1 }) {
            g.fillOval(smileCenter.x + side * eyeOffset.width - eyeSize.width/2,
                       smileCenter.y - eyeOffset.height - eyeSize.width/2,
                       eyeSize.width,
                       eyeSize.height);
        }
        
        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }
    
    /*
     * Draw a line between two points (x1, y1) and (x2, y2), specified in
     * pixels relative to the upper-left corner of the drawing buffer.
     */
    private void drawLineSegment(int x1, int y1, int x2, int y2) {
        Graphics2D graphics = (Graphics2D) drawingBuffer.getGraphics();

        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(stroke));
        graphics.drawLine(x1, y1, x2, y2);
        

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
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
        // draw a line segment from that last point to the point of the next mouse event.
        private int lastX, lastY; 

        /*
         * When mouse button is pressed down, start drawing.
         */
        public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
        }

        /*
         * When mouse moves while a button is pressed down,
         * draw a line segment.
         */
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            drawLineSegment(lastX, lastY, x, y);
            lastX = x;
            lastY = y;
        }

        // Ignore all these other mouse events.
        public void mouseMoved(MouseEvent e) { }
        public void mouseClicked(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
    }
    
    
    /*
     * Main program. Make a window containing a Canvas.
     */
    public static void main(String[] args) {
        // set up the UI (on the event-handling thread)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Canvas canvas = new Canvas();
            }
        });
    }
}
