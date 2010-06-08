/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2002, Ithaqua Enterprises Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package gistoolkit.datasources;

import gistoolkit.features.*;
import gistoolkit.projection.*;
/**
 * Class for checking and manipulating the objects.  Probably only usefull intermediatly, most of these routines should be handled by the features themselves.
 */
public class ShapeUtils {
    /**
     * ShapeUtils constructor comment.
     */
    public ShapeUtils() {
        super();
    }
    
    /**
     * Checkorient tests the clockwise or counter-clockwise orientation of a polygon.
     * <p>
     * returned values:
     * <br>
     *	>0 clockwise
     *</br>
     *<br>
     *	=0 if points are in a straight line
     *</br>
     *<br>
     *  <0 counter-clockwise
     * </br>
     * </p>
     */
    public static double checkOrientation(double[] chkarrayx, double[] chkarrayy, int numpts) {
        int j, index, n1, n2;
        double z = 0;
        
        // Find the index of the point with the largest X value
        index = 0;
        for (j = 1; j < numpts; j++) {
            if (chkarrayx[j] > chkarrayx[index]) {
                index = j;
            }
        }
        
        // Check the polygon path orientation (clockwise or counter-countwise)
        n1 = index - 1;
        if (n1 < 0)
            n1 += numpts;
        for (j = 1; j < numpts - 2; j++) {
            n2 = (index + j) % numpts;
            z = (chkarrayx[n1] - chkarrayx[index]) * (chkarrayy[n2] - chkarrayy[index]);
            z = z - (chkarrayx[n2] - chkarrayx[index]) * (chkarrayy[n1] - chkarrayy[index]);
            if (Math.abs(z) > 10E-15)
                break;
        }
        return z;
    }
    
    /**
     * checkorient tests the clockwise or counter-clockwise orientation of a polygon.
     * <p>
     * returned values:
     * <br>
     *  >0 clockwise
     *</br>
     *<br>
     *  =0 if points are in a straight line
     *</br>
     *<br>
     *  <0 counter-clockwise
     * </br>
     * </p>
     */
    public static double checkOrientation(Point[] inPoints) {
        
        // if an invalid polygon is sent in, return invalid polygon.
        if (inPoints == null) return 0;
        if (inPoints.length <3 ) return 0;
        
        int j, index, n1, n2;
        double z = 0;
        
        // Find the index of the point with the largest X value
        index = 0;
        for (j = 1; j < inPoints.length; j++) {
            if (inPoints[j].x > inPoints[index].x) {
                index = j;
            }
        }
        
        // Check the polygon path orientation (clockwise or counter-countwise)
        if (index == 0){
            n1 = inPoints.length-1;
        }
        else {
            n1 = index - 1;
        }
        
        // loop around the polygon checking each node.
        for (j = 1; j < inPoints.length - 1; j++) {
            
            // Start the second point at the point after the largest point,
            // if the index of the point is beyond the length of the polygon,
            // then loop back to 0 (The beginning) and increment from there.
            n2 = (index + j) % inPoints.length;
            
            // perform the dot product to determine the direction of the polygon
            z = (inPoints[n1].x - inPoints[index].x) * (inPoints[n2].y - inPoints[index].y);
            z = z - (inPoints[n2].x - inPoints[index].x) * (inPoints[n1].y - inPoints[index].y);
            if (Math.abs(z) > 10E-15)
                break;
            
            
            // Check for duplicate points
            if (Math.abs(Math.abs(inPoints[n1].x - inPoints[index].x) + Math.abs(inPoints[n1].y -inPoints[index].y)) < 10E-15){
                n1 = n1-1;
                if (n1 < 0) n1 = inPoints.length-1;
                
                // check for triangles with duplicate points
                if (n1 == n2) break;
                j--;
            }
        }
        return z;
    }
       
    /**
     * determines if the second shape is within the first shape.
     */
    public static boolean isInside(Point[] inOuterPoints, Point[] inInnerPoints) {
        
        // on the boundary value
        boolean tempOnBoundary = true;
        
        // number of times the polygon crosses the ray
        int crossings = 0;
        double x = inInnerPoints[0].getX();
        double y = inInnerPoints[0].getY();
        for (int i = 0; i < inOuterPoints.length-1; i++) {
            if (((inOuterPoints[i].getX() < x) && (x < inOuterPoints[i + 1].getX())) || ((inOuterPoints[i].getX() > x) && (x > inOuterPoints[i + 1].getX()))) {
                double t = (x - inOuterPoints[i + 1].getX()) / (inOuterPoints[i].getX() - inOuterPoints[i + 1].getX());
                double cy = t * inOuterPoints[i].getY() + (1 - t) * inOuterPoints[i + 1].getY();
                if (y == cy)
                    return (tempOnBoundary); // on the boundary is considered inside
                else
                    if (y > cy)
                        crossings++;
            }
            if ((inOuterPoints[i].getX() == x && inOuterPoints[i].getY() <= y)) {
                if (inOuterPoints[i].getY() == y)
                    return (tempOnBoundary); // on the boundary
                if (inOuterPoints[i + 1].getX() == x) {
                    if ((inOuterPoints[i].getY() <= y && y <= inOuterPoints[i + 1].getY()) || (inOuterPoints[i].getY() >= y && y >= inOuterPoints[i + 1].getY())) {
                        return (tempOnBoundary);
                    }
                }
                else
                    if (inOuterPoints[i + 1].getX() > x)
                        crossings++;
                if (inOuterPoints[i - 1].getX() > x)
                    crossings++;
            }
        }
        if (Math.IEEEremainder(crossings, 2.0) == 0)
            return false;
        return true;
    }
    
    /** Projects the entire dataset <b>foreward</b> using the given projection.*/
    public static void projectForward(GISDataset inDataset, Projection inProjection) throws Exception{
        if (inProjection == null) return;
        if (inDataset != null){
            for (int i=0; i<inDataset.size(); i++){
                Record tempRecord = inDataset.getRecord(i);
                if (tempRecord != null){
                    ShapeProjector.projectForward(inProjection, tempRecord.getShape());
                }
            }
        }
    }
    
    /** Projects the entire dataset <b>backward</b> using the given projection.*/
    public static void projectBackward(GISDataset inDataset, Projection inProjection) throws Exception{
        if (inProjection == null) return;
        if (inDataset != null){
            for (int i=0; i<inDataset.size(); i++){
                Record tempRecord = inDataset.getRecord(i);
                if (tempRecord != null){
                    ShapeProjector.projectBackward(inProjection, tempRecord.getShape());
                }
            }
        }
    }
    
    /** Reproject the entire dataset, backward from the old projection, forward to the new projection */
    public static void reproject(GISDataset inDataset, Projection inOldProjection, Projection inNewProjection) throws Exception{
        projectBackward(inDataset, inOldProjection);
        projectForward(inDataset, inNewProjection);
    }
}