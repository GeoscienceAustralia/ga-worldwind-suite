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

package gistoolkit.features;

/**
 * Represents the data associated with any shape.
 */
public abstract class Shape {
    /** There are set of valid shape types */
    public static final String NULLSHAPE = "NULL";
    public static final String POINT = "POINT";
    public static final String MULTIPOINT = "MULTIPOINT";
    public static final String LINEARRING = "LINEARRING";
    public static final String LINESTRING = "LINESTRING";
    public static final String MULTILINESTRING = "MULTILINESTRING";
    public static final String POLYGON = "POLYGON";
    public static final String MULTIPOLYGON = "MULTIPOLYGON";
    public static final String RASTER = "RASTER";
    
    /**
     * This is the number the shapes use to determine if one double is equal to another.
     * If the absolute value of the difference of the numbers is less than this value, then
     * the two numbers are equal.
     */
    public static final double EQUAL_LIMIT=1e-14;

    /**
     * stores the bounding rectangle of this shape.
     */
    protected Envelope myEnvelope=null;
    
    
    /** Return the type of shape this is */
    public abstract String getShapeType();
    
    /**
     * Recalculates the extents on the object, should be called if the object is changed.
     */
    public void calculateEnvelope(){}
    /**
     * return the bounding rectangle of this shape.
     */
    public abstract Envelope getEnvelope();
    
    /**
     * Creates a copy of the shape
     */
    public abstract Object clone() ;
    
    /** Returns the number of points in the shape. */
    public abstract int getNumPoints();
    
    /** Returns the point at the given index. */
    public abstract Point getPoint(int inIndex);
    
    /** Sets the point at the given index to the given value. */
    public abstract void setPoint(int inIndex, double inXCoordinate, double inYCoordinate);
    
    /** Add the a point to this shape.  Returns the location where the point was added.  Returns -1 if it could not be added.*/
    public abstract int add(double inX, double inY);

    /** Add the a point to this shape at this index.  Returns true if the point was added.  Returns false if it could not be added.*/
    public abstract boolean add(int inIndex, double inX, double inY);

    /** Remove this point from the Shape.  Returns true if it removed the point, returns false if it cannot.*/
    public abstract boolean remove(int inIndex);
    
    /**
     * Determines if this shape contains the shape sent in;
     * Since this is the super class, it always returns false.
     */
    public boolean contains(Shape inShape){
        Envelope tempEnvelope = getEnvelope();
        if (tempEnvelope != null){
            if (inShape != null){
                if(!tempEnvelope.overlaps(inShape.getEnvelope())){
                    return false;
                }
            }
        }
        return false;
    }
        
    /**
     * Determines if the Two Lines intersect. 
     * The parameters inL1a is an end point of line 1, inl1b is the other end point of line 1.
     * The parameters inL2a is an end point of line 2, inL2b is the other end point of line 2.
     * The return will return true if the two lines intersect, and false if they do not.
     */
    public static boolean linesIntersect (Point inL1A, Point inL1B, Point inL2A, Point inL2B){
        // degenerate conditions
        if (inL1A == null) return false;
        if (inL1B == null) return false;
        if (inL2A == null) return false;
        if (inL2B == null) return false;
        
        // if L1 completely above L2
        if ((inL1A.getX() > inL2A.getX()) && (inL1A.getX() > inL2B.getX())
        && (inL1B.getX() > inL2A.getX()) && (inL1B.getX() >inL2B.getX())) return false;
        
        // if L1 completely below L2
        if ((inL1A.getX() < inL2A.getX()) && (inL1A.getX() < inL2B.getX())
        && (inL1B.getX() < inL2A.getX()) && (inL1B.getX() < inL2B.getX())) return false;
        
        // if L1 completely right L2
        if ((inL1A.getY() > inL2A.getY()) && (inL1A.getY() > inL2B.getY())
        && (inL1B.getY() > inL2A.getY()) && (inL1B.getY() >inL2B.getY())) return false;
        
        // if L1 completely left L2
        if ((inL1A.getY() < inL2A.getY()) && (inL1A.getY() < inL2B.getY())
        && (inL1B.getY() < inL2A.getY()) && (inL1B.getY() < inL2B.getY())) return false;
        
        
        // Find the intersection.
        // calculate slope of the line one
        double slope1 = 0;
        double slope2 = 0;
        
        // Special Case Horizontal Line
        if (inL1A.getY() == inL1B.getY()) {
            slope1 = 0;
            
            // if the two X Coordinates are equal, then check point on line.
            if (inL1A.getX() == inL1B.getX()){
                return pointOnLine(inL1A, inL2A, inL2B);
            }
        }
        // Special Case Vertical Line
        else {
            if (inL1A.getX() == inL1B.getX()) {
                slope1 = Double.POSITIVE_INFINITY;
            }
            else{
                slope1 = (inL1A.getY()-inL1B.getY())/(inL1A.getX()-inL1B.getX());
            }
        }
        
        // Special Case Horizontal Line
        if (inL2A.getY() == inL2B.getY()) {
            slope2 = 0;
            
            // if the two X Coordinates are equal, then check point on line.
            if (inL2A.getX() == inL2B.getX()){
                return pointOnLine(inL2A, inL1A, inL1B);
            }
        }
        // Special Case Vertical Line
        else {
            if (inL2A.getX() == inL2B.getX()) {
                slope2 = Double.POSITIVE_INFINITY;
            }
            else{
                slope2 = (inL2A.getY()-inL2B.getY())/(inL2A.getX()-inL2B.getX());
            }
        }
        
        // calculate the Y intercepts
        double Intercept1 =0;
        if (slope1 == Double.POSITIVE_INFINITY){
            Intercept1 = Double.POSITIVE_INFINITY;
        }
        else {
            Intercept1 = inL1A.getY()-(slope1 * inL1A.getX());
        }
        double Intercept2 = 0;
        if (slope2 == Double.POSITIVE_INFINITY){
            Intercept2 = Double.POSITIVE_INFINITY;
        }
        else {
            Intercept2 = inL2A.getY()-(slope2 * inL2A.getX());
        }
        
        // if the two slopes are exactly equal, the lines are parallel
        if (slope1 == slope2){
            
            // if these two lines share the same intercept, then the Bounds check above
            // ensures that they are the same.
            if (Intercept1 == Intercept2) {
                return true;
            }
            
            // if they are not the same, then the lines do not intersect
            return false;
        }
        
        // General Case
        // calculate the point of intersection.
        double x = 0;
        double y = 0;
        
        // if line 1 is vertical
        if (slope1 == Double.POSITIVE_INFINITY){
            x = inL1A.getX();
            
            // I know slope 2 is not infinite from above.
            y = (slope2)*(x) + Intercept2;
        }
        else {
            if (slope2 == Double.POSITIVE_INFINITY){
                x = inL2A.getX();
                
                // I know slope 2 is not infinite from above.
                y = (slope1)*(x) + Intercept1;
            }
            else{
                x = (Intercept2-Intercept1)/(slope1-slope2);
                if (slope1 ==0) y = inL1A.getY();
                else if (slope2 == 0) y=inL2A.getY();
                else y = (slope1)*x + Intercept1;
            }
        }
        
        // ensure that X is on line 1 and line 2.
        double minL1X;
        double maxL1X;
        if (inL1A.getX() < inL1B.getX()){
            minL1X = inL1A.getX();
            maxL1X = inL1B.getX();
        }
        else {
            minL1X = inL1B.getX();
            maxL1X = inL1A.getX();
        }
        if (x < minL1X) return false;
        if (x > maxL1X) return false;
        
        double minL2X;
        double maxL2X;
        if (inL2A.getX() < inL2B.getX()){
            minL2X = inL2A.getX();
            maxL2X = inL2B.getX();
        }
        else {
            minL2X = inL2B.getX();
            maxL2X = inL2A.getX();
        }
        if (x < minL2X) return false;
        if (x > maxL2X) return false;
        
        
        // ensure that Y is on line 1 and line 2.
        double minL1Y;
        double maxL1Y;
        if (inL1A.getY() < inL1B.getY()){
            minL1Y = inL1A.getY();
            maxL1Y = inL1B.getY();
        }
        else {
            minL1Y = inL1B.getY();
            maxL1Y = inL1A.getY();
        }
        if (y < minL1Y) return false;
        if (y > maxL1Y) return false;
        
        double minL2Y;
        double maxL2Y;
        if (inL2A.getY() < inL2B.getY()){
            minL2Y = inL2A.getY();
            maxL2Y = inL2B.getY();
        }
        else {
            minL2Y = inL2B.getY();
            maxL2Y = inL2A.getY();
        }
        if (y < minL2Y) return false;
        if (y > maxL2Y) return false;
        
        // Yes, the lines do intersect.
        return true;
    }
    
    /**
     * Determines if the Two Lines intersect. 
     * The parameters inL1a is an end point of line 1, inl1b is the other end point of line 1.
     * The parameters inL2a is an end point of line 2, inL2b is the other end point of line 2.
     * The return will return true if the two lines intersect, and false if they do not.
     */
    public static boolean linesIntersect (double inL1AX, double inL1AY, double inL1BX, double inL1BY, double inL2AX, double inL2AY, double inL2BX, double inL2BY){
        
        // if L1 completely above L2
        if ((inL1AX > inL2AX) && (inL1AX > inL2BX)
        && (inL1BX > inL2AX) && (inL1BX >inL2BX)) return false;
        
        // if L1 completely below L2
        if ((inL1AX < inL2AX) && (inL1AX < inL2BX)
        && (inL1BX < inL2AX) && (inL1BX < inL2BX)) return false;
        
        // if L1 completely right L2
        if ((inL1AY > inL2AY) && (inL1AY > inL2BY)
        && (inL1BY > inL2AY) && (inL1BY >inL2BY)) return false;
        
        // if L1 completely left L2
        if ((inL1AY < inL2AY) && (inL1AY < inL2BY)
        && (inL1BY < inL2AY) && (inL1BY < inL2BY)) return false;
        
        
        // Find the intersection.
        // calculate slope of the line one
        double slope1 = 0;
        double slope2 = 0;
        
        // Special Case Horizontal Line
        if (inL1AY == inL1BY) {
            slope1 = 0;
            
            // if the two X Coordinates are equal, then check point on line.
            if (inL1AX == inL1BX){
                return pointOnLine(inL1AX, inL1AY, inL2AX, inL2AY, inL2BX, inL2BY);
            }
        }
        // Special Case Vertical Line
        else {
            if (inL1AX == inL1BX) {
                slope1 = Double.POSITIVE_INFINITY;
            }
            else{
                slope1 = (inL1AY-inL1BY)/(inL1AX-inL1BX);
            }
        }
        
        // Special Case Horizontal Line
        if (inL2AY == inL2BY) {
            slope2 = 0;
            
            // if the two X Coordinates are equal, then check point on line.
            if (inL2AX == inL2BX){
                return pointOnLine(inL2AX, inL2AY, inL1AX, inL1AY, inL1BX, inL1BY);
            }
        }
        // Special Case Vertical Line
        else {
            if (inL2AX == inL2BX) {
                slope2 = Double.POSITIVE_INFINITY;
            }
            else{
                slope2 = (inL2AY-inL2BY)/(inL2AX-inL2BX);
            }
        }
        
        // calculate the Y intercepts
        double Intercept1 =0;
        if (slope1 == Double.POSITIVE_INFINITY){
            Intercept1 = Double.POSITIVE_INFINITY;
        }
        else {
            Intercept1 = inL1AY-(slope1 * inL1AX);
        }
        double Intercept2 = 0;
        if (slope2 == Double.POSITIVE_INFINITY){
            Intercept2 = Double.POSITIVE_INFINITY;
        }
        else {
            Intercept2 = inL2AY-(slope2 * inL2AX);
        }
        
        // if the two slopes are exactly equal, the lines are parallel
        if (slope1 == slope2){
            
            // if these two lines share the same intercept, then the Bounds check above
            // ensures that they are the same.
            if (Intercept1 == Intercept2) {
                return true;
            }
            
            // if they are not the same, then the lines do not intersect
            return false;
        }
        
        // General Case
        // calculate the point of intersection.
        double x = 0;
        double y = 0;
        
        // if line 1 is vertical
        if (slope1 == Double.POSITIVE_INFINITY){
            x = inL1AX;
            
            // I know slope 2 is not infinite from above.
            y = (slope2)*(x) + Intercept2;
        }
        else {
            if (slope2 == Double.POSITIVE_INFINITY){
                x = inL2AX;
                
                // I know slope 2 is not infinite from above.
                y = (slope1)*(x) + Intercept1;
            }
            else{
                x = (Intercept2-Intercept1)/(slope1-slope2);
                if (slope1 ==0) y = inL1AY;
                else if (slope2 == 0) y=inL2AY;
                else y = (slope1)*x + Intercept1;
            }
        }
        
        // ensure that X is on line 1 and line 2.
        double minL1X;
        double maxL1X;
        if (inL1AX < inL1BX){
            minL1X = inL1AX;
            maxL1X = inL1BX;
        }
        else {
            minL1X = inL1BX;
            maxL1X = inL1AX;
        }
        if (x < minL1X) return false;
        if (x > maxL1X) return false;
        
        double minL2X;
        double maxL2X;
        if (inL2AX < inL2BX){
            minL2X = inL2AX;
            maxL2X = inL2BX;
        }
        else {
            minL2X = inL2BX;
            maxL2X = inL2AX;
        }
        if (x < minL2X) return false;
        if (x > maxL2X) return false;
        
        
        // ensure that Y is on line 1 and line 2.
        double minL1Y;
        double maxL1Y;
        if (inL1AY < inL1BY){
            minL1Y = inL1AY;
            maxL1Y = inL1BY;
        }
        else {
            minL1Y = inL1BY;
            maxL1Y = inL1AY;
        }
        if (y < minL1Y) return false;
        if (y > maxL1Y) return false;
        
        double minL2Y;
        double maxL2Y;
        if (inL2AY < inL2BY){
            minL2Y = inL2AY;
            maxL2Y = inL2BY;
        }
        else {
            minL2Y = inL2BY;
            maxL2Y = inL2AY;
        }
        if (y < minL2Y) return false;
        if (y > maxL2Y) return false;
        
        // Yes, the lines do intersect.
        return true;
    }

    /**
     * Determines if the Two Lines intersect. 
     * The parameters inL1a is an end point of line 1, inl1b is the other end point of line 1.
     * The parameters inL2a is an end point of line 2, inL2b is the other end point of line 2.
     * The return will return true if the two lines intersect, and false if they do not.
     */
    public static Point getLinesIntersect (Point inL1A, Point inL1B, Point inL2A, Point inL2B){
        // degenerate conditions
        if (inL1A == null) return null;
        if (inL1B == null) return null;
        if (inL2A == null) return null;
        if (inL2B == null) return null;
        
        // if L1 completely above L2
        if ((inL1A.getX() > inL2A.getX()) && (inL1A.getX() > inL2B.getX())
        && (inL1B.getX() > inL2A.getX()) && (inL1B.getX() >inL2B.getX())) return null;
        
        // if L1 completely below L2
        if ((inL1A.getX() < inL2A.getX()) && (inL1A.getX() < inL2B.getX())
        && (inL1B.getX() < inL2A.getX()) && (inL1B.getX() < inL2B.getX())) return null;
        
        // if L1 completely right L2
        if ((inL1A.getY() > inL2A.getY()) && (inL1A.getY() > inL2B.getY())
        && (inL1B.getY() > inL2A.getY()) && (inL1B.getY() >inL2B.getY())) return null;
        
        // if L1 completely left L2
        if ((inL1A.getY() < inL2A.getY()) && (inL1A.getY() < inL2B.getY())
        && (inL1B.getY() < inL2A.getY()) && (inL1B.getY() < inL2B.getY())) return null;
        
        
        // Find the intersection.
        // calculate slope of the line one
        double slope1 = 0;
        double slope2 = 0;
        
        
        // Special Case Horizontal Line
        if (inL1A.getY() == inL1B.getY()) {
            slope1 = 0;
            
            // if the two XCoordinates are equal, then check point on line.
            if (inL1A.getX() == inL1B.getX()){
                if (pointOnLine(inL1A, inL2A, inL2B)) return inL1A;
            }
        }
        // Special Case Vertical Line
        else {
            if (inL1A.getX() == inL1B.getX()) {
                slope1 = Double.POSITIVE_INFINITY;                
            }
            else{
                slope1 = (inL1A.getY()-inL1B.getY())/(inL1A.getX()-inL1B.getX());
            }
        }
        
        // Special Case Horizontal Line
        if (inL2A.getY() == inL2B.getY()) {
            slope2 = 0;
            
            // if the two XCoordinates are equal, then check point on line.
            if (inL2A.getX() == inL2B.getX()){
                if (pointOnLine(inL2A, inL1A, inL1B)) return inL2A;
            }
        }
        // Special Case Vertical Line
        else {
            if (inL2A.getX() == inL2B.getX()) {
                slope2 = Double.POSITIVE_INFINITY;
            }
            else{
                slope2 = (inL2A.getY()-inL2B.getY())/(inL2A.getX()-inL2B.getX());
            }
        }
        
        // calculate the Y intercepts
        double Intercept1 =0;
        if (slope1 == Double.POSITIVE_INFINITY){
            Intercept1 = Double.POSITIVE_INFINITY;
        }
        else {
            Intercept1 = inL1A.getY()-(slope1 * inL1A.getX());
        }
        double Intercept2 = 0;
        if (slope2 == Double.POSITIVE_INFINITY){
            Intercept2 = Double.POSITIVE_INFINITY;
        }
        else {
            Intercept2 = inL2A.getY()-(slope2 * inL2A.getX());
        }
        
        // if the two slopes are exactly equal, the lines are parallel
        if (slope1 == slope2){
            
            // if these two lines share the same intercept, then the Bounds check above
            // ensures that they are the same.
            if (Intercept1 == Intercept2) {
                return null;
            }
            
            // if they are not the same, then the lines do not intersect
            return null;
        }
                
        // General Case
        // calculate the point of intersection.
        double x = 0;
        double y = 0;
        
        // if line 1 is vertical
        if (slope1 == Double.POSITIVE_INFINITY){
            x = inL1A.getX();
            
            // I know slope 2 is not infinite from above.
            y = (slope2)*(x) + Intercept2;
        }
        else {
            if (slope2 == Double.POSITIVE_INFINITY){
                x = inL2A.getX();
                
                // I know slope 2 is not infinite from above.
                y = (slope1)*(x) + Intercept1;
            }
            else{
                x = (Intercept2-Intercept1)/(slope1-slope2);
                if (slope1 ==0) y = inL1A.getY();
                else if (slope2 == 0) y=inL2A.getY();
                else y = (slope1)*x + Intercept1;
            }
        }
        
        // ensure that X is on line 1 and line 2.
        double minL1X;
        double maxL1X;
        if (inL1A.getX() < inL1B.getX()){
            minL1X = inL1A.getX();
            maxL1X = inL1B.getX();
        }
        else {
            minL1X = inL1B.getX();
            maxL1X = inL1A.getX();
        }
        if (x < minL1X) return null;
        if (x > maxL1X) return null;
        
        double minL2X;
        double maxL2X;
        if (inL2A.getX() < inL2B.getX()){
            minL2X = inL2A.getX();
            maxL2X = inL2B.getX();
        }
        else {
            minL2X = inL2B.getX();
            maxL2X = inL2A.getX();
        }
        if (x < minL2X) return null;
        if (x > maxL2X) return null;
        
        
        // ensure that Y is on line 1 and line 2.
        double minL1Y;
        double maxL1Y;
        if (inL1A.getY() < inL1B.getY()){
            minL1Y = inL1A.getY();
            maxL1Y = inL1B.getY();
        }
        else {
            minL1Y = inL1B.getY();
            maxL1Y = inL1A.getY();
        }
        if (y < minL1Y) return null;
        if (y > maxL1Y) return null;
        
        double minL2Y;
        double maxL2Y;
        if (inL2A.getY() < inL2B.getY()){
            minL2Y = inL2A.getY();
            maxL2Y = inL2B.getY();
        }
        else {
            minL2Y = inL2B.getY();
            maxL2Y = inL2A.getY();
        }
        if (y < minL2Y) return null;
        if (y > maxL2Y) return null;
        
        // Yes, the lines do intersect.
        return new Point(x,y);
    }
    
   /**
    * Determines if a point is exactly on a line.  The parameter inPoint is the point in question.  The points inLA, and inLB are the end points
    * of the line.  If the point falls exactly on the line, then the method returns true.  If the point is not on the line, then the method returns false.
    */
    public static boolean pointOnLine(Point inPoint, Point inLA, Point inLB){
        return pointOnLine(inPoint.getX(), inPoint.getY(), inLA.getX(), inLA.getY(), inLB.getX(), inLB.getY());
    }
    
   /**
    * Determines if a point is exactly on a line.  The parameter inPoint is the point in question.  The points inLA, and inLB are the end points
    * of the line.  If the point falls exactly on the line, then the method returns true.  If the point is not on the line, then the method returns false.
    */
    public static boolean pointOnLine(double inPointX, double inPointY, double inLAX, double inLAY, double inLBX, double inLBY){
        
        // if the point is outside the bounds, then return
        if ((inPointX < inLAX) && (inPointX < inLBX)) return false;
        if ((inPointX > inLAX) && (inPointX > inLBX)) return false;
        if ((inPointY < inLAY) && (inPointY < inLBY)) return false;
        if ((inPointY > inLAY) && (inPointY > inLBY)) return false;
        
        // if the points are the same, then return true.
        if ((inPointX == inLAX) && ( inPointY == inLAY)) return true;
        if ((inPointX == inLBX) && ( inPointY == inLBY)) return true;
        
        // if the line is vertical
        if (inLAX == inLBX) {
            if (inPointX != inLAX) return false;
        }
        // if the line is horizontal.
        if (inLAY == inLBY) {
            if (inPointY != inLAY) return false;
        }
        
        // calculate slope of the line one
        double slope = (inLAY-inLBY)/(inLAX-inLBX);
        
        // calculate the intercept of the line
        double intercept = inLAY-(slope * inLAX);
        
        // calculate the Y coordinate of the point in question
        double ycandidate = slope*(inPointX) * intercept;
        
        // if this is the y coordinate in question, then return that point.
        if (Math.abs(ycandidate-inPointY) > EQUAL_LIMIT) return false;
        return true;
    }

    /** 
     * Returns the points that comprise the object.
     * This is an expensive operation as all the points must be generated to be returned.
     * As a result, the points returned are not owned by the shape.  They are copies of the data contained in the shape.
     */
    public abstract Point[] getPoints();
    
    /** Returns the OGIS Well Know Text Representation of this shape */
    public abstract String getWKT();
    
    /** Translate the shape the given distance in the X and Y directions */
    public abstract void translate(double inXDistance, double inYDistance);
    
    /** Distance from a point to another point.
     * Implements the standard distance formula x^2 + y^2=d^2.
     */
    public static double distance(Point inPointA, Point inPointB) {
        // classic distance formula
        return getDistance(inPointA.getX(), inPointA.getY(), inPointB.getX(), inPointB.getY());
    }
    
    /** Determine the distance between two points.
     * Implements the standard distance formula x^2 + y^2=d^2.
     */
    public static double getDistance( double inX1, double inY1, double inX2, double inY2) {
        
        // if the two x coordinates are identical then return dy.
        if (inX1 == inX2)
            return Math.abs(inY2 - inY1);
        
        // if the two y coordinates are identical, then return dx
        if (inY1 == inY2)
            return Math.abs(inX2 - inX1);
        
        // calculate the distance.
        return Math.sqrt((inX2-inX1)*(inX2-inX1)+(inY2-inY1)*(inY2-inY1));
        
    }
        
    /**Calculates the distance from the line defined by point 1 and 2 to the point defined by X and Y.*/
    public static double getDistanceToLine(Point inPoint1, Point inPoint2, double inX, double inY) {
        return getDistanceToLine(inPoint1.getX(), inPoint1.getY(), inPoint2.getX(), inPoint2.getY(), inX, inY);
    }
    
    /**Calculates the distance from the line defined by point 1 and 2 to the point defined by X and Y.*/
    public static double getDistanceToLine(double inP1X, double inP1Y, double inP2X, double inP2Y, double inX, double inY){
        
        // if the two points are identical, get the distance to the point.
        if ((inP1X == inP2X) && (inP1Y == inP2Y)){
            return getDistance(inP1X, inP1Y, inP2X, inP2Y);
        }
        
        // calculate slope of the line given by the two x points.
        double x = 0;
        double y = 0;
        
        // Special Case Horizontal Line
        if (inP1X == inP2X) {
            x = inP2X;
            y = inY;
        }
        // Special Case Vertical Line
        else
            if (inP1Y == inP2Y) {
                x = inX;
                y = inP1Y;
            }
        // General Case
            else {
                double slope = (inP2Y - inP1Y) / (inP2X - inP1X);
                
                // calculate the Intercepts
                double intercept1 = inP1Y - slope * inP1X;
                double intercept2 = (inY) + (1 / slope) * (inX);
                
                // calculate the coordinates of the Point on the line closest to this point
                x = (intercept1 - intercept2) / ((-1 / slope) - (slope));
                y = (-1 / slope) * x + intercept2;
            }
        
        // if the new point is between the end points of the line segment, then consider it.
        if (((x >= Math.min(inP1X, inP2X))
        && (x <= Math.max(inP1X, inP2X)))
        && (y >= Math.min(inP1Y, inP2Y))
        && (y <= Math.max(inP1Y, inP2Y))) {
            
            // calculate the distance between the two points
            return getDistance(x,y,inX, inY);
        }
        // calculate the distance to the end points
        Point tempPoint = new Point(x,y);
        return Math.min(getDistance(inX, inY, inP1X, inP1Y), getDistance(inX, inY, inP2X, inP2Y));
    }
    
    /**
     * Determines the distance from the given point to the extents of this shape.
     * This method is meant to be a fast way to find the distance to the shape when close enough is close enough.
     * The distance returned is the distance to any one of the lines, or the corners depending on which of the nine
     * quadrants the point happens to land within.  In the case of the point residing within the extents, a distance of zero 0 is returned.
     *
     */
    public double getDistanceToEnvelope(double inX, double inY){
        Envelope tempEnvelope = getEnvelope();
        if (tempEnvelope == null) return Double.NaN;

        // if the shape is to the left then compare to the Left points
        if (inX < tempEnvelope.getMinX()){
            // if the shape is above, then compare it to the point above
            if (inY> tempEnvelope.getMaxY()){
                return getDistance(inX, inY, tempEnvelope.getMinX(), tempEnvelope.getMaxY());
            }
            // if the shape is below, then compare it to the point below
            if (inY < tempEnvelope.getMaxY()){
                return getDistance(inX, inY, tempEnvelope.getMinX(), tempEnvelope.getMaxY());
            }
            // compare the shape to the line
            return getDistanceToLine(tempEnvelope.getMinX(), tempEnvelope.getMaxY(), tempEnvelope.getMinX(), tempEnvelope.getMinY(), inX, inY);
        }
        
        // if the shape is to the right, then compare to the right points.
        if (inX > tempEnvelope.getMaxX()){
            // if the shape is above, then compare it to the point above
            if (inY > tempEnvelope.getMaxY()){
                return Shape.getDistance(inX, inY, tempEnvelope.getMaxX(), tempEnvelope.getMaxY());
            }
            // if the shape is below, then compare it to the point below
            if (inY < tempEnvelope.getMaxY()){
                return Shape.getDistance(inX, inY, tempEnvelope.getMaxX(), tempEnvelope.getMaxY());
            }
            // compare the shape to the line on the right
            return Shape.getDistanceToLine(tempEnvelope.getMaxX(), tempEnvelope.getMaxY(), tempEnvelope.getMaxX(), tempEnvelope.getMinY(), inX, inY);
        }
        
        // if the shape is above, then compare to the top line.
        if (inY > tempEnvelope.getMaxY()){
            return Shape.getDistanceToLine(tempEnvelope.getMinX(), tempEnvelope.getMaxY(), tempEnvelope.getMaxX(), tempEnvelope.getMaxY(), inX, inY);
        }
        
        // if the shape is below, then compare to the bottom line.
        if (inY < tempEnvelope.getMinY()){
            return Shape.getDistanceToLine(tempEnvelope.getMinX(), tempEnvelope.getMaxY(), tempEnvelope.getMaxX(), tempEnvelope.getMinY(), inX, inY);
        }
        
        // the point is within the extents
        return 0;
    }
            
    /** Get the index of the point within the shape nearest this location.*/
    public abstract int getClosestIndex(double inX, double inY);//{return -1;}
    
    /** Get the distance from this shape to the given point */
    public abstract double getDistanceToPoint(double inX, double inY);
    
    /** Determines if the two shapes intersect */
    public abstract boolean intersects(Shape inShape);
    
}