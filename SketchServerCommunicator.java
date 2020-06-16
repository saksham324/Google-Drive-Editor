import java.io.*;
import java.net.Socket;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 *
 * @author Saksham Arora, Winter 2020
 * @author Egemen Sahin, Winter 2020
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE

			for ( Integer id: server.getSketch().getShapesMap().navigableKeySet()){  	//tells the client the curr state of the shapesMap sorted from topMost
				send("new " + server.getSketch().getShapesMap().get(id));
			}


			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			String line;										// reads in the message from the client
			while((line = in.readLine())!= null){
				ParseString.parse(line, server.getSketch());	// parses the message and calls the appropriate function on the master sketch
				server.broadcast(line);							// broadcast the message to all clients
			}

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
