/* CS 10 Problem Set 6
Egemen Sahin and Saksham Tobeyou
Communication for Editor in Cooperative Drawing Program
Single shared Code
 */

import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * Handles communication to/from the server for the editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {

			// Handle messages
			// TODO: YOUR CODE HERE

			String line; // The message from the client

			// If the message exists,
			while((line = in.readLine())!= null){
				// Make sense of the message and repaint the canvas after the edits have been done.
				ParseString.parse(line, editor.getSketch());
				editor.repaint();
			}

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}

	// Send editor requests to the server
	// TODO: YOUR CODE HERE

	// A message to create a new shape
	public synchronized void requestNew(Shape shape){ send("new" + " " + shape.toString()); }

	// A message to delete the shape with Id id
	public synchronized void delete(Integer id) { send("delete" + " " + id); }

	// A message to move the shape with Id id by dx and dy
	public synchronized void move(Integer id, Integer dx, Integer dy) { send("move" + " " + id + " " + dx + " " + dy); }

	// A message to recolor the shape with Id id to Color color
	public synchronized void recolor(Integer id, Color color) { send("recolor" + " " + id + " " + color.getRGB()); }

	// A message to start dragging the shape with Id id to Color color
	public synchronized void reqDrag(Integer id) { send("drag" + " " + id);
	}
}







