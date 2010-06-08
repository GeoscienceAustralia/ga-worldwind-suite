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
 * A Polygon is a group of rings, the first represents the external polygin, there may be zero or more holes as well.
 */
public class Polygon extends Shape {
    /**
     * List of points in the linear Ring.
     */
    private LinearRing myPolygon;
    
    /**
     * An array of linear rings which represent the holes.
     */
    private LinearRing[] myHoles;
    
    /**
     * Polygon constructor comment.
     */
    public Polygon() {
        super();
    }
    
    /**
     * Create a new linear ring from the points.
     */
    public Polygon(LinearRing inLinearRing) {
        myPolygon = inLinearRing;
        calculateEnvelope();
    }
    
    /**
     * Create a new linear ring from the points.
     */
    public Polygon(LinearRing inLinearRing, LinearRing[] inHoles) {
        myPolygon = inLinearRing;
        myHoles = inHoles;
        calculateEnvelope();
    }
    
    /** Return the type of shape this is */
    public String getShapeType(){return POLYGON;}
    
    /** Return the number of points in this shape. */
    public int getNumPoints(){
        int tempNumPoints = 0;
        if (myPolygon != null){
            tempNumPoints = myPolygon.getNumPoints();
            
            LinearRing[] tempHoles = getHoles();
            for (int i=0; i<tempHoles.length; i++){
                tempNumPoints = tempNumPoints + tempHoles[i].getNumPoints();
            }
        }
        return tempNumPoints;
    }
    
    /** Get the point at the given index. */
    public Point getPoint(int inIndex){
        Point tempPoint = null;
        
        // find the point for this index.
        int tempIndex = inIndex;
        if (tempIndex > -1){
            
            // check the Posative Ring
            LinearRing tempRing = getPosativeRing();
            if (tempRing.getNumPoints() <= tempIndex){
                tempIndex = tempIndex - tempRing.getNumPoints();
                
                // check the Negative Rings.
                LinearRing[] tempHoles = getHoles();
                for (int i=0; i<tempHoles.length; i++){
                    if (tempHoles[i].getNumPoints() <= tempIndex){
                        tempIndex = tempIndex - tempHoles[i].getNumPoints();
                    }
                    else{
                        tempPoint = tempHoles[i].getPoint(tempIndex);
                        break;
                    }
                }
            }
            else{
                tempPoint = tempRing.getPoint(tempIndex);
            }
        }
        return tempPoint;
    }
    
    /** Set the point at the given index. */
    public void setPoint(int inIndex, double inXCoordinate, double inYCoordinate){
        // find the point closes to this click location.
        int tempIndex = inIndex;
        if (tempIndex > -1){
            
            // check the Posative Ring
            LinearRing tempRing = getPosativeRing();
            if (tempRing.getNumPoints() <= tempIndex){
                tempIndex = tempIndex - tempRing.getNumPoints();
                
                // check the Negative Rings.
                LinearRing[] tempHoles = getHoles();
                for (int i=0; i<tempHoles.length; i++){
                    if (tempHoles[i].getNumPoints() <= tempIndex){
                        tempIndex = tempIndex - tempHoles[i].getNumPoints();
                    }
                    else{
                        tempHoles[i].setPoint(tempIndex, inXCoordinate, inYCoordinate);
                        break;
                    }
                }
            }
            else{
                tempRing.setPoint(tempIndex, inXCoordinate, inYCoordinate);
            }
        }
    }
    /** Add the a point to this shape.  Returns the location where the point was added.  Returns -1 if it could not be added. */
    public int add(double inX, double inY) {
        int tempIndex = getClosestIndex(inX, inY);
        if (add(tempIndex+1, inX, inY)) return tempIndex+1;
        return -1;
    }
    
    /** Adds the point to this shape at the given index. */
    public boolean add(int inIndex, double inX, double inY){
        int tempIndex = inIndex;
        boolean tempReturn = false;
        if (tempIndex > -1){
            
            // check the Posative Ring
            LinearRing tempRing = getPosativeRing();
            if (tempRing.getNumPoints() < tempIndex){
                tempIndex = tempIndex - tempRing.getNumPoints();
                
                // check the Negative Rings.
                LinearRing[] tempHoles = getHoles();
                for (int i=0; i<tempHoles.length; i++){
                    if (tempHoles[i].getNumPoints() < tempIndex){
                        tempIndex = tempIndex - tempHoles[i].getNumPoints();
                    }
                    else{
                        tempReturn = tempHoles[i].add(tempIndex, inX, inY);
                        break;
                    }
                }
            }
            else{
                tempReturn = tempRing.add(tempIndex, inX, inY);
            }
        }
        return tempReturn;
    }
    
    /** Delete the given point from the polygon. */
    public boolean remove(int inIndex){
        // find the point closes to this click location.
        int tempIndex = inIndex;
        boolean tempReturn = false;
        if (tempIndex > -1){
            
            // check the Posative Ring
            LinearRing tempRing = getPosativeRing();
            if (tempRing.getNumPoints() <= tempIndex){
                tempIndex = tempIndex - tempRing.getNumPoints();
                
                // check the Negative Rings.
                LinearRing[] tempHoles = getHoles();
                for (int i=0; i<tempHoles.length; i++){
                    if (tempHoles[i].getNumPoints() <= tempIndex){
                        tempIndex = tempIndex - tempHoles[i].getNumPoints();
                    }
                    else{
                        tempReturn = tempHoles[i].remove(tempIndex);
                        break;
                    }
                }
            }
            else{
                tempReturn = tempRing.remove(tempIndex);
            }
        }
        return tempReturn;
    }
    
    /**
     * Calculates the envelope based on the data in the polygon.
     */
    public void calculateEnvelope() {
        myEnvelope = null;
    }
    public Envelope getEnvelope(){
        if (myEnvelope == null){
            if (myPolygon != null){
                myEnvelope = myPolygon.getEnvelope();
            }
            // The holes are not an issue because the holse can not be outside the primary polygon anyway.
        }
        return myEnvelope;
    }
    
    /**
     * Creates a copy of the Polygon
     */
    public Object clone(){
        if (myPolygon != null){
            
            // create a copy of the posative area
            LinearRing tempPolygon = (LinearRing) myPolygon.clone();
            
            // create copies of the negative areas
            LinearRing[] tempHoles = null;
            if (myHoles != null){
                tempHoles = new LinearRing[myHoles.length];
                for (int i=0; i<myHoles.length; i++){
                    tempHoles[i] = (LinearRing) myHoles[i].clone();
                }
            }
            
            // create the new Polygon
            return new Polygon(tempPolygon, tempHoles);
        }
        else return new Polygon();
    }
    
    /**
     * returns true if any of the contained polygons contain this shape.
     */
    public boolean contains(Shape inShape) {
        if (inShape == null) return false;
        try{
            // check the minimum bounding rectangle
            if (!this.getEnvelope().contains(inShape.getEnvelope())) return false;
            
            // if the inshape is a point, then perform that logic.
            if (inShape instanceof Point) {
                return isPointInPolygon(this, (Point) inShape);
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        
        // all other shapes
        return false;
    }
    /**
     * Return the negative rings.
     */
    public LinearRing[] getHoles(){
        return myHoles;
    }
    
    /**
     *  Add a hole at the given index.
     */
    public void addHole(int inIndex, LinearRing inLinearRing){
        // check for valid data and special cases.
        if (inLinearRing == null) return;
        if (myHoles == null){
            myHoles = new LinearRing[1];
            myHoles[0] = inLinearRing;
            return;
        }
        
        // create the new points array.
        LinearRing[] tempLinearRings = new LinearRing[myHoles.length+1];
        for (int i=0; i<myHoles.length; i++){
            if (i < inIndex){
                tempLinearRings[i] = myHoles[i];
            }
            if (i == inIndex){
                tempLinearRings[i] = inLinearRing;
            }
            if (i >= inIndex){
                tempLinearRings[i+1] = myHoles[i];
            }
        }
        if (inIndex >= myHoles.length) tempLinearRings[myHoles.length] = inLinearRing;
        myHoles = tempLinearRings;
        // calculate the new envelope
        calculateEnvelope();
        
    }
    
    /**
     *  Remove a hole at the given index.
     */
    public void removeHole(int inIndex){
        // check for valid data and special cases.
        if (myHoles == null) return;
        if ((inIndex < 0) || (inIndex >= myHoles.length)) return;
        
        // create the new Hole array.
        LinearRing[] tempLinearRings = new LinearRing[myHoles.length-1];
        for (int i=0; i<tempLinearRings.length; i++){
            if (i < inIndex){
                tempLinearRings[i] = myHoles[i];
            }
            if (i >= inIndex){
                tempLinearRings[i-1] = myHoles[i];
            }
        }
        myHoles = tempLinearRings;
        // calculate the new envelope
        calculateEnvelope();
        
    }
    /**
     * Return the posative Polygon from the shape.
     */
    public LinearRing getPosativeRing(){
        return myPolygon;
    }
    
    /**
     * Performs a point in polygon calculation.
     */
    public static boolean isPointInPolygon(Polygon inPolygon, Point inPoint) {
        
        // error prevention
        if (inPolygon == null) return false;
        if (inPoint == null) return false;
        
        // do a point in ring calculation on the primary shape
        if (isPointInRing(inPolygon.getPosativeRing(), inPoint)){
            
            // If the point is within the ring, then check the holes
            LinearRing[] tempRings = inPolygon.getHoles();
            if (tempRings != null){
                for (int i=0; i<tempRings.length; i++){
                    if (isPointInRing(tempRings[i], inPoint)) return false;
                }
            }
            
            // point is within the posative ring, and not within any of the holes
            // this is the one case where the pit is within the polygon
            return true;
        }
        return false;
    }
    
    /**
     * Performs a point in polygon calculation.
     */
    public static boolean isPointInRing(LinearRing inRing, Point inPoint) {
        if (inRing == null) return false;
        if (inPoint == null) return false;
        
        
        // on the boundary value
        boolean tempOnBoundary = true;
        Point[] tempRingPoints = inRing.getPoints();
        if (tempRingPoints == null) return false;
        
        // number of times the polygon crosses the ray
        int crossings = 0;
        double x = inPoint.getX();
        double y = inPoint.getY();
        
        // loop through all the points check for crossings.
        for (int i = 0; i < tempRingPoints.length - 1; i++) {
            
            // An intersection exists.
            if (((tempRingPoints[i].getX() < x) && (x < tempRingPoints[i + 1].getX()))
            || ((tempRingPoints[i].getX() > x) && (x > tempRingPoints[i + 1].getX()))) {
                double t =
                (x - tempRingPoints[i + 1].getX())
                / (tempRingPoints[i].getX() - tempRingPoints[i + 1].getX());
                double cy =
                t * tempRingPoints[i].getY() + (1 - t) * tempRingPoints[i + 1].getY();
                if (y == cy)
                    return (tempOnBoundary); // on the boundary is considered inside
                else
                    if (y > cy)
                        crossings++;
            }
            
            // line intersects exactly.
            if ((tempRingPoints[i].getX() == x && tempRingPoints[i].getY() <= y)) {
                if (tempRingPoints[i].getY() == y)
                    return (tempOnBoundary); // on the boundary
                if (tempRingPoints[i + 1].getX() == x) {
                    if ((tempRingPoints[i].getY() <= y && y <= tempRingPoints[i + 1].getY())
                    || (tempRingPoints[i].getY() >= y && y >= tempRingPoints[i + 1].getY())) {
                        return (tempOnBoundary);
                    }
                }
                else
                    if (tempRingPoints[i + 1].getX() > x)
                        crossings++;
                if (tempRingPoints[i - 1].getX() > x)
                    crossings++;
            }
        }
        
        // if the number of intersections is even, then the point is outside.
        if (Math.IEEEremainder(crossings, 2.0) == 0)
            return false;
        // if it is not even, then it is inside.
        return true;
    }
    
    /** Return the points that comprise the object */
    public Point[] getPoints(){
        if (myPolygon == null) return new Point[0];
        
        // calculate how many points I need
        int tempNumPoints = 0;
        if (myPolygon != null) tempNumPoints =   myPolygon.getPoints().length;
        if (myHoles != null){
            for (int i=0; i<myHoles.length; i++){
                tempNumPoints = tempNumPoints + myHoles[i].getPoints().length;
            }
        }
        
        // create the array of points
        Point[] tempPoints = new Point[tempNumPoints];
        int tempIndex = 0;
        if (myPolygon != null){
            Point[] tempPoints2 = myPolygon.getPoints();
            for (int i=0; i<tempPoints2.length; i++){
                tempPoints[tempIndex] = tempPoints2[i];
                tempIndex++;
            }
        }
        if (myHoles != null){
            for (int i=0; i<myHoles.length; i++){
                Point[] tempPoints2 = myHoles[i].getPoints();
                for (int j=0; j<tempPoints2.length; j++){
                    tempPoints[tempIndex] = tempPoints2[j];
                    tempIndex++;
                }
            }
        }
        return tempPoints;
    }
    
    /** Returns the OGIS Well Know Text Representation of this shape */
    public String getWKT(){
        myPolygon.ensureClosed();
        StringBuffer sb = new StringBuffer("polygon(");
        if (myPolygon != null){
            sb.append("(");
            Point[] tempPoints = myPolygon.getRingPoints();
            for (int i=0; i<tempPoints.length; i++){
                if (i>0) sb.append(",");
                sb.append(tempPoints[i].getX());
                sb.append(" ");
                sb.append(tempPoints[i].getY());
            }
            sb.append(")");
        }
        if (myHoles!= null){
            for (int i=0; i<myHoles.length; i++){
                sb.append(",(");
                myHoles[i].ensureClosed();
                Point[] tempPoints = myHoles[i].getRingPoints();
                for (int j=0; j<tempPoints.length; j++){
                    if (j>0) sb.append(",");
                    sb.append(tempPoints[j].getX());
                    sb.append(" ");
                    sb.append(tempPoints[j].getY());
                }
                sb.append(")");
            }
        }
        sb.append(")");
        return sb.toString();
    }
    
    /** Translate the shape the given distance in the X and Y directions  */
    public void translate(double inXDistance, double inYDistance) {
        if (myPolygon != null) myPolygon.translate(inXDistance, inYDistance);
        if (myHoles != null){
            for (int i=0; i<myHoles.length; i++){
                myHoles[i].translate(inXDistance, inYDistance);
            }
        }
        calculateEnvelope();
    }
    
    /**
     * Return the point in the polygon that is the closest to this point.
     */
    public Point getClosestPoint(double inX, double inY) {
        Point tempPoint = null;
        
        // find the point closes to this click location.
        int tempIndex = getClosestIndex(inX, inY);
        tempPoint = getPoint(tempIndex);
        return tempPoint;
    }
    
    /** Return the index of the point in the polygon that is the closest to this point.*/
    public int getClosestIndex(double inX, double inY) {
        Point tempPoint = null;
        int tempRingIndex = -1;
        int tempPointIndex = -1;
        int tempReturnIndex = -1;
        double tempMinDistance = Double.MAX_VALUE;
        
        // find the point closes to this click location.
        LinearRing tempPosativeRing = getPosativeRing();
        if (tempPosativeRing != null){
            int tempIndex = tempPosativeRing.getClosestIndex(inX, inY);
            if (tempIndex > -1){
                tempPoint = tempPosativeRing.getPoint(tempIndex);
                double tempDistance = getDistance(tempPoint.getX(), tempPoint.getY(), inX, inY);
                if (tempDistance < tempMinDistance){
                    tempMinDistance = tempDistance;
                    tempRingIndex = 0;
                    tempPointIndex = tempIndex;
                }
            }
            
            LinearRing[] tempHoles = getHoles();
            if (tempHoles != null){
                
                // loop through the holes finding the minimum distance.
                for (int i=0; i<tempHoles.length; i++){
                    if (tempHoles[i] != null){
                        tempIndex = tempHoles[i].getClosestIndex(inX, inY);
                        tempPoint = tempHoles[i].getPoint(tempIndex);
                        double tempDistance = getDistance(tempPoint.getX(), tempPoint.getY(), inX, inY);
                        
                        // find the minimum distance.
                        if (tempDistance < tempMinDistance){
                            tempMinDistance = tempDistance;
                            tempRingIndex = i+1;
                            tempPointIndex = tempIndex;
                        }
                    }
                }
            }
            
            // if a point was found
            if (tempRingIndex != -1){
                // if the point was in one of the holes, then add the points to get to it.
                tempReturnIndex = 0;
                if (tempRingIndex > 0) {
                    tempReturnIndex = tempReturnIndex + tempPosativeRing.getNumPoints();
                }
                for (int i=0; i<tempRingIndex-1; i++){
                    LinearRing tempRing = tempHoles[i];
                    tempReturnIndex  = tempReturnIndex + tempRing.getNumPoints();
                }
                tempReturnIndex = tempReturnIndex + tempPointIndex;
            }
        }
        return tempReturnIndex;
    }
    
    /** Get the distance from this shape to the given point */
    public double getDistanceToPoint(double inX, double inY){
        if (myPolygon == null) return Double.NaN;
        double tempMinDistance = myPolygon.getDistanceToPoint(inX, inY);
        
        if (myHoles != null){
            for (int i=0; i<myHoles.length; i++){
                double tempDistance = myHoles[i].getDistanceToPoint(inX, inY);
                if (tempDistance < tempMinDistance) tempMinDistance = tempDistance;
            }
        }
        return tempMinDistance;
    }
    
    /** Determines if the two shapes intersect  */
    public boolean intersects(Shape inShape) {
        // if the shape sent in is null, then return false
        if (inShape == null) return false;
        
        // if the envelope do not overlap, then the shapes cannot
        if (!getEnvelope().intersects(inShape.getEnvelope())) return false;
        
        // Points
        if (inShape instanceof Point) return contains((Point)inShape);
        
        // MultiPoints
        if (inShape instanceof MultiPoint) return ((MultiPoint) inShape).intersectsPolygon(this);
        
        // LineStrings
        if (inShape instanceof LineString) return ((LineString) inShape).intersectsPolygon(this);
        
        // MultiLineStrings
        if (inShape instanceof MultiLineString) return ((MultiLineString) inShape).intersectsPolygon(this);
        
        // LinearRings
        if (inShape instanceof LinearRing) return ((LinearRing) inShape).intersectsPolygon(this);
        
        // Polygons
        if (inShape instanceof Polygon) return intersectsPolygon((Polygon) inShape);
        
        // MultiPolygons
        if (inShape instanceof MultiPolygon) return intersectsMultiPolygon((MultiPolygon) inShape);
        
        // RasterShapes
        if (inShape instanceof RasterShape) return intersectsRasterShape((RasterShape) inShape);
        
        // did not find the shape so return false
        return false;
    }
    
    /** A polygon intersects another polygon if any of the rings of the polygon intersect, or one polygon is contained within the other. */
    public boolean intersectsPolygon(Polygon inPolygon){
        if (contains(inPolygon)) return true;
        if (inPolygon.contains(this)) return true;
        
        // check the posative rings
        if (myPolygon == null) return false;
        LinearRing tempPosativeRing = inPolygon.getPosativeRing();
        if (tempPosativeRing == null) return false;
        
        LinearRing[] tempHoles = inPolygon.getHoles();
        if (tempHoles != null){
            for (int i=0; i<tempHoles.length; i++){
                if (tempHoles[i].contains(myPolygon)) return false;
                if (tempHoles[i].intersectsLinearRing(myPolygon)) return true;
            }
        }
        
        if (tempPosativeRing.intersectsLinearRing(myPolygon)) return true;
        
        if (myHoles != null){
            for (int i=0; i<myHoles.length; i++){
                if (myHoles[i].intersectsLinearRing(tempPosativeRing)) return true;
            }
        }
        
        if ((myHoles != null) && (tempHoles != null)){
            for (int i=0; i<myHoles.length; i++){
                for (int j=0; j<tempHoles.length; j++){
                    if (myHoles[i].intersectsLinearRing(tempHoles[j])) return true;
                }
            }
        }
        return false;
    }
    
    /** A Polygon intersects a MultiPolygon if it intersects any of the MultiPolygon's constituent Polygons. */
    public boolean intersectsMultiPolygon(MultiPolygon inMultiPolygon){
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        if (tempPolygons != null){
            for (int i=0; i<tempPolygons.length; i++){
                if (intersectsPolygon(tempPolygons[i])) return true;
            }
        }
        return false;
    }
    
    /** A Polygon intersects a RasterShape if it intersects any of the envelope, or if it is contained within the envelope. */
    public boolean intersectsRasterShape(RasterShape inRasterShape){
        Polygon tempPolygon = inRasterShape.getEnvelope().getPolygon();
        if (intersectsPolygon(tempPolygon)) return true;
        return false;
    }
    
    /** Find a point within the polygon . */
    public Point getCentroid(){
        Envelope e = getEnvelope();
        // find the center of the envelope
        double centerX = (e.getMinX() + e.getMaxX())/2;
        double centerY = (e.getMinY() + e.getMaxY())/2;
        double distance = 0.0;
        
        Point[] tempPoints = getPosativeRing().getRingPoints();
        
        // find the intersections on the center X line
        Point tempLeftPoint = new Point(e.getMinX()-1, centerY);
        Point tempRightPoint = new Point(e.getMaxX()+1, centerY);
        Point tempPoint1 = null;
        Point tempPoint2 = null;
        for (int i=0; i<tempPoints.length-1; i++){
            Point tempPoint = getLinesIntersect( tempLeftPoint, tempRightPoint, tempPoints[i], tempPoints[i+1]);
            if (tempPoint != null){
                if (tempPoint1 == null){
                    tempPoint1 = tempPoint;
                }
                else {
                    tempPoint2 = tempPoint;
                    // is the point between these points in the shape.
                    if (tempPoint2.equals(tempPoint1)){
                        tempPoint2 = null;
                    }
                    else{
                        tempPoint = new Point((tempPoint1.getX() + tempPoint2.getX()) /2, centerY);
                        if (contains(tempPoint)){
                            double tempDistance = Math.abs(tempPoint1.getX() - tempPoint2.getX());
                            if (tempDistance > distance){
                                distance = tempDistance;
                                centerX = tempPoint.getX();
                                centerY = tempPoint.getY();
                            }
                        }
                    }
                }
            }
        }
        
        // find the intersections on the center Y line
        tempLeftPoint = new Point(centerX, e.getMaxY()+1);
        tempRightPoint = new Point(centerX, e.getMinY()-1);
        tempPoint1 = null;
        tempPoint2 = null;
        distance = 0;
        for (int i=0; i<tempPoints.length-1; i++){
            Point tempPoint = getLinesIntersect( tempLeftPoint, tempRightPoint, tempPoints[i], tempPoints[i+1]);
            if (tempPoint != null){
                if (tempPoint1 == null){
                    tempPoint1 = tempPoint;
                }
                else {
                    tempPoint2 = tempPoint;
                    // is the point between these points in the shape.
                    if (tempPoint2.equals(tempPoint1)){
                        tempPoint2 = null;
                    }
                    else{
                        tempPoint = new Point(centerX, (tempPoint1.getY() + tempPoint2.getY()) /2);
                        if (contains(tempPoint)){
                            double tempDistance = Math.abs(tempPoint1.getY() - tempPoint2.getY());
                            if (tempDistance > distance){
                                distance = tempDistance;
                                centerX = tempPoint.getX();
                                centerY = tempPoint.getY();
                            }
                        }
                    }
                }
            }
        }
        // return the bext point
        return new Point(centerX, centerY);
    }
}
