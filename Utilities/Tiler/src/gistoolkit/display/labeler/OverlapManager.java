/*
 * OverlapManager.java
 *
 * Created on January 8, 2003, 4:33 PM
 */

package gistoolkit.display.labeler;

import java.util.Vector;
import java.awt.*;


/**
 * Class to handle if overlaps are to be allowed are not.
 */
public class OverlapManager {
    
    /** Creates a new instance of OverlapManager */
    public OverlapManager() {
    }
    
    /** vector to hold the list of already written labels. */
    private Vector myOverLapLabels= new Vector();

    /** Checks if this is an overlaping value. Always returns false if overlaps are allowed.*/
    public boolean isOverLaps(int inLocX, int inLocY, int inWidth, int inHeight){
        
        Rectangle tempRect = new Rectangle(inLocX - inWidth/2, inLocY-inHeight/2, inWidth, inHeight);
        for(int i=0; i<myOverLapLabels.size();i++){
            java.awt.Shape myShape = (java.awt.Shape)myOverLapLabels.elementAt(i);
            if(myShape.intersects(tempRect)){
                return true;
            }
        }
        myOverLapLabels.add(tempRect);
        return false;
    }
    /** Checks if this is an overlaping value. Always returns false if overlaps are allowed.*/
    public boolean isOverLaps(java.awt.Shape inShape){
        
        for(int i=0; i<myOverLapLabels.size();i++){
            java.awt.Shape tempShape = (java.awt.Shape) myOverLapLabels.elementAt(i);
            if(tempShape.intersects(inShape.getBounds2D())){
                return true;
            }
        }
        myOverLapLabels.add(inShape);
        return false;
    }
    /** Clear the contents of the overlap manager. */
    public void clear(){
        myOverLapLabels.removeAllElements();
    }
}
