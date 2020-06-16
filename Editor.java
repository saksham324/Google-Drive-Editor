/* CS 10 Problem Set 6
Egemen Sahin and Saksham Tobeyou
Editor for Cooperative Drawing Program
 */

import java.lang.management.PlatformLoggingMXBean;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor extends JFrame {
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = 1;					// current shape id (if any; else 1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


	private Polyline tempPoly = new Polyline(color);	// The current polyline being drawn

	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};

		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});

		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "polyline", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() { return sketch; }

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE

		// If there are shapes in the sketch
		if (sketch.getIds().size() > 0) {
			// Get the id of each shape in the sketch
			for (Integer id : sketch.getIds()) {
				sketch.getShapesMap().get(id).draw(g);        // Draw each shape in the shapemap
			}
		}

		// Draw the current shape being drawn in the editor of the client drawing it.
		if (curr != null){
			curr.draw(g);
		}

		// Repaint the canvas
		repaint();
	}

	// Helpers for event handlers

	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE

		// If the client is drawing
		if (mode == Mode.DRAW) {

			// And wants to draw a rectangle, set the current shape to a new Rectangle, which will be a 1x1 "rectangle" at the pixel p.
			if (shapeType == "rectangle") {
				curr = new Rectangle((int) p.getX(), (int) p.getY(), color);
				drawFrom = p;
			}

			// And wants to draw an ellipse, set the current shape to a new Ellipse, which will be a 1x1 "ellipse" at the pixel p.
			if (shapeType == "ellipse") {
				curr = new Ellipse((int) p.getX(), (int) p.getY(), color);
				drawFrom = p;
			}

			// And wants to draw a segment, set the current shape to a new Segment which will be a 1x1 "segment" at the pixel p.
			if (shapeType == "segment") {
				curr = new Segment((int) p.getX(), (int) p.getY(), color);
				drawFrom = p;
			}

			// And wants to draw freehand/polyline, set the current shape to a new Polyline which will be a 1x1 "polyline" at the pixel p.
			// To do that, add the point to the temporary polyline's point list and then set curr to the temporary polyline.
			if (shapeType == "polyline"){
				tempPoly.addPoint((int) p.getX(), (int) p.getY());
				tempPoly.setColor(color);
				curr = tempPoly;
				drawFrom = p;
			}

			// Repaint the canvas.
			repaint();
		}

		Integer id = (getSketch().getId((int) p.getX(), (int) p.getY()));		// The id of the shape which is clicked

		// If the client is moving, recoloring or deleting, first check if the client is clicking on a shape.
		if (id != 1) {

			// If the client wants to move a shape
			if (mode == Mode.MOVE) {
				movingId = id;				// Set movingId to the current shape's id
				comm.reqDrag(movingId);		// Request an initialization of the drag

				moveFrom = p;				// Set the moveFrom point to the current point
			}

			// If the client wants to recolor a shape
			if (mode == Mode.RECOLOR) {
				comm.recolor(id, color);	// Request the server to recolor the shape with Id id to the Color color

			}

			// If the client wants to delete a shape
			if (mode == Mode.DELETE) {
				comm.delete(id);			// Request the server to delete the shape with Id id

			}

			// Repaint the canvas
			repaint();
		}
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE

		// If the client is drawing
		if (mode == Mode.DRAW) {

			// And the type of shape is a polyline which doesn't contain the point p
			if (shapeType.equals("polyline") && !tempPoly.getPoints().contains(new Point((int) p.getX(), (int) p.getY()))) {
				tempPoly.addPoint((int) p.getX(), (int) p.getY());			// Add the point p to tempPoly
				curr = tempPoly;		// Set curr to tempPoly
			}

			// If the type of shape isn't polyline
			else {
				// Set the current shape's corners to drawFrom which is static, and the current point.
				curr.setCorners((int) drawFrom.getX(), (int) drawFrom.getY(), (int) p.getX(), (int) p.getY());
			}
		}

		// If the client is moving the shape, the shape exists and the client is clicking on a shape
		if (mode == Mode.MOVE && movingId != 1 && moveFrom != null) {

			// Send a request to move the shape with the Id movingId
			comm.move(movingId, (int) (p.getX() - moveFrom.getX()), (int) (p.getY() - moveFrom.getY()));

			// Set moveFrom to the current point
			moveFrom = p;
		}

		// Repaint the canvas
		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE

		// If the client has drawn a shape
		if (mode == Mode.DRAW) {
			// Send a request to add the shape to the sketch
			comm.requestNew(curr);

			// Reset all temporary variables
			curr = null;
			drawFrom = null;
			tempPoly = new Polyline(color);
		}

		// If the client has moved a shape
		if (mode == Mode.MOVE){
			// Set moveFrom to null
			moveFrom = null;
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});
	}
}
