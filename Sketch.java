/**
 * Sketch class which holds the current shapes list as a Map

 * @author Saksham Arora, Winter 2020
 * @author Egemen Sahin, Winter 2020
 */


import java.awt.*;
import java.util.*;

public class Sketch {

    private static TreeMap<Integer, Shape> shapesMap;       // list of all the shapes as a Tree Map
    private static Integer nextId = 0;                      // Id of the next shape to be added
    private static Integer movingId = -1;                   // Id of the next shape to be moved

    public Sketch(){

        shapesMap = new TreeMap<Integer, Shape>();
    }

    // adds a shape to the shapes list
    public synchronized void addShape (Shape shape){
        shapesMap.put(nextId, shape);
        nextId -= 1;                                        // We decrement nextId because we believed it would be easier to have negative Ids to sort by most recent to least recent
    }

    // returns the ids of all the shapes sorted in descending order
    public synchronized Set<Integer> getIds (){
        return shapesMap.descendingKeySet();
    }

    // returns a reference to the shapesMap
    public synchronized TreeMap<Integer, Shape> getShapesMap () {
        return shapesMap;
    }

    // returns the id of the topmost shape which contains the point x, y
    public synchronized Integer getId (Integer x, Integer y) {
        for (Integer id: shapesMap.navigableKeySet()){
            if(shapesMap.get(id).contains(x, y)){
                return id;
            }
        }

        return 1;   // if there is no shape found, it returns 1
    }

    // recolors the shape with a given id
    public synchronized void recolor (Integer id, Color color){
        shapesMap.get(id).setColor(color);
    }

    // deletes the shape from the shapesMap with a given id
    public synchronized void delete (Integer id){
        shapesMap.remove(id);
    }

    // moves the shape with a given id by dx and dy
    public synchronized void move (Integer dx, Integer dy){
        shapesMap.get(movingId).moveBy(dx, dy);
    }

    // sets the movingId of a shape
    public synchronized void setMovingId(Integer id){ movingId = id; }


}
