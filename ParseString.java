/**
 * ParseString class which interprets the incoming messages from the server/client and calls the appropriate function
 * on local shapesMap for client or global shapesMap for server
 * @author Saksham Arora, Winter 2020
 * @author Egemen Sahin, Winter 2020
 */

import java.awt.*;
import java.util.ArrayList;

public class ParseString {
    private String input;          // input message
    private Sketch sketch;         // sketch passed in for functions to be performed on

    public ParseString(String input, Sketch sketch){
        this.input = input;
        this.sketch = sketch;
    }

    public static void parse(String input, Sketch sketch){
        String[] inputArray = input.split(" ");         // split the input message into an array of Strings
        String method = inputArray[0];                        // the method to be called is specified at the beginning

        if (method.equals("new")) {                           // to create a new shape
            Shape toAdd = null;
            String shapeType = inputArray[1];                 // shapeType is given by string at index 1

            if (shapeType.equals("rectangle")) {              // if shape is rectangle then set toAdd to be a rectangle with the provided values
                Integer x1 = Integer.parseInt(inputArray[2]);
                Integer y1 = Integer.parseInt(inputArray[3]);
                Integer x2 = Integer.parseInt(inputArray[4]);
                Integer y2 = Integer.parseInt(inputArray[5]);
                Color color = new Color(Integer.parseInt(inputArray[6]));
                toAdd = new Rectangle(x1, y1, x2, y2, color);
            }


            if (shapeType.equals("ellipse")) {                  // if shape is ellipse then set toAdd to be a ellipse with the provided values
                Integer x1 = Integer.parseInt(inputArray[2]);
                Integer y1 = Integer.parseInt(inputArray[3]);
                Integer x2 = Integer.parseInt(inputArray[4]);
                Integer y2 = Integer.parseInt(inputArray[5]);
                Color color = new Color(Integer.parseInt(inputArray[6]));
                toAdd = new Ellipse(x1, y1, x2, y2, color);
            }


            if (shapeType.equals("segment")) {                 // if shape is segment then set toAdd to be a segment with the provided values
                Integer x1 = Integer.parseInt(inputArray[2]);
                Integer y1 = Integer.parseInt(inputArray[3]);
                Integer x2 = Integer.parseInt(inputArray[4]);
                Integer y2 = Integer.parseInt(inputArray[5]);
                Color color = new Color(Integer.parseInt(inputArray[6]));
                toAdd = new Segment(x1, y1, x2, y2, color);
            }


            if (shapeType.equals("polyline")) {                 // if shape is polyline then add all the points attributed to the polyline
                ArrayList<Point> points = new ArrayList<Point>();
                for (int i = 2; i < inputArray.length-1; i+=2) {
                    Point point = new Point(Integer.parseInt(inputArray[i]), Integer.parseInt(inputArray[i+1]));
                    if (!points.contains(point)) { points.add(point); }
                }
                Color color = new Color(Integer.parseInt(inputArray[inputArray.length-1]));
                toAdd = new Polyline(points, color);
            }

            if (toAdd != null) { sketch.addShape(toAdd); }      // if toAdd is not null, add toAdd to the sketch
        }


        if (method.equals("delete")) {                          // if method is delete, remove it from the sketch using id provided
            sketch.delete(Integer.parseInt(inputArray[1]));
        }


        if (method.equals("move")) {                            // if method is move, move it in the sketch using dx and dy
            sketch.move(Integer.parseInt(inputArray[2]), Integer.parseInt(inputArray[3]));

        }

        if (method.equals("recolor")) {                         // if method is recolor, recolor it in the sketch using id and color value
            sketch.recolor(Integer.parseInt(inputArray[1]), new Color(Integer.parseInt(inputArray[2])));

        }


        if (method.equals("drag")) {                             // if method is drag, set movingId it in the sketch using movingId provided
            sketch.setMovingId(Integer.parseInt(inputArray[1]));

        }
    }
}
