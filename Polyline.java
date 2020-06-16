import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 *
 * @author Saksham Arora, Winter 2020
 * @author Egemen Sahin, Winter 2020
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
	private ArrayList<Point> points;				// list of Point objects
	private Color color;							// color variable

	public Polyline(Color color){
		this.color = color;
		this.points = new ArrayList<Point>();		// initializes the list of Points to be empty
	}

	public Polyline(ArrayList<Point> points, Color color){
		this.points = points;
		this.color = color;
	}

	public void addPoint(Integer x, Integer y){
		this.points.add( new Point(x, y));			// adds the given point to the list of Points
	}

	public ArrayList<Point> getPoints() { return points;}

	// method that moves every point in the Points list by dx and dy
	@Override
	public void moveBy(int dx, int dy) {
		for (Point p : this.points){
			p.x += dx;
			p.y += dy;
		}
	}

	// an abstract definition of setCorners
	@Override
	public void setCorners(int x1, int y1, int x2, int y2) {}

	// returns the color of the Polyline
	@Override
	public Color getColor() {
		return this.color;
	}

	// sets the color of the Polyline
	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	// checks if the point x, y is contained within the Polyline, returns a true or false value
	@Override
	public boolean contains(int x, int y) {
		for (int i = 1; i < points.size(); i++){
			if (Segment.pointToSegmentDistance(x, y, (int) points.get(i - 1).getX(),
					(int) points.get(i - 1).getY(), (int) points.get(i).getX(),
					(int) points.get(i).getY()) <= 3) return true;
		}

		return false;
	}

	// draws the Polyline by drawing a line between every point in the Points list
	@Override
	public void draw(Graphics g) {
		g.setColor(this.color);
		for(int i = 1; i < points.size(); i++){
			g.drawLine((int) points.get(i - 1).getX(), (int) points.get(i - 1).getY(),
					(int) points.get(i).getX(), (int) points.get(i).getY());
		}
	}

	// toString method for the Polyline, returns all necessary information about the Polyline
	@Override
	public String toString() {
		String res = "polyline ";
		for (int i = 0; i < points.size(); i++) {
			res += points.get(i).x + " " + points.get(i).y + " ";
		}
		res += color.getRGB();
		return res;
	}
}
