package client;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.junit.Rule;

/**
 * Canvas represents a drawing surface that allows the user to draw
 * on it freehand, with the mouse.
 */
public class Canvas extends JPanel {
    // image where the user's drawing is stored
    private Image drawingBuffer;
    private static Color color = Color.BLACK;
    private static int stroke = 2;
    private final static int ERASER_STROKE = 10;
    private final static int BUTTON_WIDTH = 150;
    private final static int BUTTON_HEIGHT = 100;
    private final static int TABLE_WIDTH = 180;
    private final static int TABLE_HEIGHT = 330;
    private final static Color PURPLE = new Color(160, 32, 240);
    private final static Color MIT = new Color(163, 31, 52);
    
    
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
                Label sliderLabel = new Label("Stroke Size:");
                JSlider strokeSlider = new JSlider(); //TODO: Add JSlider listener
                Button paintButton = new Button("Draw!");
                Button eraserButton = new Button("Erase!");
                Label tableLabel = new Label("List of Artists:"); //TODO: Change the font from Annie's trove
                JTable playerList = new JTable(0, 1); //TODO: Add table-server comm
                JScrollPane scrollList = new JScrollPane(playerList);
                
                //Color pallet buttons
                Button button1 = new Button(); //TODO: Add a shitload of listeners possible listener class
                Button button2 = new Button();
                Button button3 = new Button();
                Button button4 = new Button();
                Button button5 = new Button();
                Button button6 = new Button();
                Button button7 = new Button();
                Button button8 = new Button();
                Button button9 = new Button();
                Button button10 = new Button();
                Button button11 = new Button();
                Button button12 = new Button();
                Button button13 = new Button();
                Button button14 = new Button();
                Button button15 = new Button();
                
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
                button12.setBackground(PURPLE);
                button13.setBackground(Color.MAGENTA);
                button14.setBackground(Color.PINK);
                button15.setBackground(Color.CYAN);
                
                
                paintButton.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent arg0) {
                        color = Color.BLACK;
                        stroke = 2;
                    }
                    
                    public void mouseReleased(MouseEvent arg0) { }
                    public void mousePressed(MouseEvent arg0) { }
                    public void mouseExited(MouseEvent arg0) { }
                    public void mouseEntered(MouseEvent arg0) { }
                });

                eraserButton.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent arg0) {
                        color = Color.WHITE;
                        stroke = ERASER_STROKE;
                    }
                    
                    public void mouseReleased(MouseEvent arg0) { }
                    public void mousePressed(MouseEvent arg0) { }
                    public void mouseExited(MouseEvent arg0) { }
                    public void mouseEntered(MouseEvent arg0) { }
                });
                
                // creating layouts
                GridLayout palletLayout = new GridLayout(3, 5);
                BoxLayout panelLayout = new BoxLayout(sidePanel, BoxLayout.Y_AXIS);
                BoxLayout pButtonLayout = new BoxLayout(paintButtonContainer, BoxLayout.Y_AXIS);
                BoxLayout eButtonLayout = new BoxLayout(eraserButtonContainer, BoxLayout.Y_AXIS);
                window.add(canvas, BorderLayout.WEST);
                window.add(sidePanel, BorderLayout.EAST);
                playerList.setFillsViewportHeight(true);
                playerList.setTableHeader(null);
                
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
                window.setSize(1010, 600);
                canvas.setSize(800,  600);
                sidePanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 20, 5));
                paintButtonContainer.setBorder(BorderFactory.createEmptyBorder(25, 0, 12, 0));
                eraserButtonContainer.setBorder(BorderFactory.createEmptyBorder(13, 0, 25, 0));
                Dimension buttonDimension = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
                Dimension tableDimension = new Dimension(TABLE_WIDTH, TABLE_HEIGHT);
                paintButton.setMaximumSize(buttonDimension);
                eraserButton.setMaximumSize(buttonDimension);
                scrollList.setPreferredSize(tableDimension);
                
                // set fonts
                sliderLabel.setFont(new Font("Helvetica", Font.TRUETYPE_FONT, 20));
                paintButton.setFont(new Font("Helvetica", Font.TRUETYPE_FONT, 35));
                eraserButton.setFont(new Font("Helvetica", Font.TRUETYPE_FONT, 35));
                tableLabel.setFont(new Font("Helvetica", Font.TRUETYPE_FONT, 20));
                
                //window.pack();
                window.setResizable(false);
                window.setVisible(true);
            }
        });
    }
}
