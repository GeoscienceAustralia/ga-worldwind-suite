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

import java.util.*;
import gistoolkit.features.*;

/**
 * Class for converting Well Known Text(WKT) representations of a shape into their Shape counterparts.
 * @author  bitterstorm
 */
public class WKTFactory {
    
    /** Creates new WKTFactory */
    public WKTFactory() {
    }
    
    /** Retrieve the shape from the text string */
    public static Shape parseShape(String inWKT) throws Exception{
        if (inWKT == null) return null;
        if (inWKT.startsWith("POINT")) return parsePoint(inWKT);
        if (inWKT.startsWith("LINESTRING")) return parseLineString(inWKT);
        if (inWKT.startsWith("POLYGON")) return parsePolygon(inWKT);
        if (inWKT.startsWith("MULTIPOINT")) return parseMultiPoint(inWKT);
        if (inWKT.startsWith("MULTILINESTRING")) return parseMultiLineString(inWKT);
        if (inWKT.startsWith("MULTIPOLYGON")) return parseMultiPolygon(inWKT);
        throw new Exception("Geometry Type not recognized for "+inWKT);
    }
    
    /** Convert the Well Know Text (WKT) representation of a point into a Point. */
    public static Point parsePoint(String inWKT) throws Exception{
        // find the X coordinate
        int tempStartIndex = inWKT.indexOf('(');
        if (tempStartIndex == -1) throw new Exception("Can Not find Start of X coordinate in Point "+inWKT);
        int tempEndIndex = inWKT.indexOf(')',tempStartIndex);
        if (tempEndIndex == -1) throw new Exception("Can Not find End of X coordinate in Point "+inWKT);
        
        // return the point
        Point tempPoint = parsePoint(inWKT, tempStartIndex+1, tempEndIndex-1);
        return tempPoint;
    }
    
    /** Convert the Well Know Text (WKT) representation of a line string into a LineString. */
    public static LineString parseLineString(String inWKT) throws Exception{
        // find the first Parenthesis
        int tempStartIndex = inWKT.indexOf('(');
        if (tempStartIndex == -1) throw new Exception("Can not find start of LineString in "+inWKT);
        int tempEndIndex = inWKT.indexOf(')',tempStartIndex);
        if (tempEndIndex == -1) throw new Exception("Can not find end of LineString in "+inWKT);
        
        // get the points from the middle part
        Point[] tempPoints = parsePoints(inWKT, tempStartIndex+1, tempEndIndex-1);
        LineString tempLineString = new LineString(tempPoints);
        return tempLineString;
    }
    
    /** Convert the Well Know Text (WKT) representation of a polygon into a Polygon. */
    public static Polygon parsePolygon(String inWKT) throws Exception{
        // find the first parenthisis
        int tempStartIndex = inWKT.indexOf('(');
        if (tempStartIndex == -1) throw new Exception("Can not find start of Polygon in "+inWKT);
        
        // find the last parenthisis
        int tempEndIndex = inWKT.lastIndexOf(')');
        if (tempEndIndex == -1) throw new Exception("Can not find end of Polygon in "+inWKT);
        
        // parse the groups of points between the parenthesis
        Point[][] tempPointArray = parsePointGroups(inWKT, tempStartIndex+1, tempEndIndex-1);
        
        // create the posative ring
        Polygon tempPolygon = null;
        if (tempPointArray.length >0){
            LinearRing tempPosativeRing = new LinearRing(tempPointArray[0]);
            ArrayList tempList = new ArrayList();
            for (int i=1; i<tempPointArray.length; i++){
                tempList.add(new LinearRing(tempPointArray[i]));
            }
            LinearRing[] tempHoles = new LinearRing[tempList.size()];
            tempList.toArray(tempHoles);
            tempPolygon = new Polygon(tempPosativeRing, tempHoles);
        }
        return tempPolygon;
    }
    
    /** Convert the Well Known Text (WKT) representation of a multi point into a MultiPoint. */
    public static MultiPoint parseMultiPoint(String inWKT) throws Exception{
        // find the first Parenthesis
        int tempStartIndex = inWKT.indexOf('(');
        if (tempStartIndex == -1) throw new Exception("Can not find start of MultiPoint in "+inWKT);
        int tempEndIndex = inWKT.indexOf(')',tempStartIndex);
        if (tempEndIndex == -1) throw new Exception("Can not find end of MultiPoint in "+inWKT);
        
        // get the points from the middle part
        Point[] tempPoints = parsePoints(inWKT, tempStartIndex+1, tempEndIndex-1);
        MultiPoint tempMultiPoint = new MultiPoint(tempPoints);
        return tempMultiPoint;
    }
    
    /** Convert the Well Known Text (WKT) representaion of a multi line string into a MultiLineString. */
    public static MultiLineString parseMultiLineString(String inWKT) throws Exception{
        // find the first parenthisis
        int tempStartIndex = inWKT.indexOf('(');
        if (tempStartIndex == -1) throw new Exception("Can not find start of MultiLineString in "+inWKT);
        
        // find the last parenthisis
        int tempEndIndex = inWKT.lastIndexOf(')');
        if (tempEndIndex == -1) throw new Exception("Can not find end of MultiLineString in "+inWKT);
        
        // parse the groups of points between the parenthesis
        Point[][] tempPointArray = parsePointGroups(inWKT, tempStartIndex+1, tempEndIndex-1);
        
        // create the posative ring
        MultiLineString tempMultiLineString = null;
        if (tempPointArray.length >0){
            ArrayList tempList = new ArrayList();
            for (int i=0; i<tempPointArray.length; i++){
                tempList.add(new LineString(tempPointArray[i]));
            }
            LineString[] tempLines = new LineString[tempList.size()];
            tempList.toArray(tempLines);
            tempMultiLineString = new MultiLineString(tempLines);
        }
        return tempMultiLineString;
    }
    
    /** Convert the Well Known Text (WKT) representation of  multi polygon into a MultiPolygon. */
    public static MultiPolygon parseMultiPolygon(String inWKT) throws Exception {
        // find the first parenthisis
        int tempStartIndex = inWKT.indexOf('(');
        if (tempStartIndex == -1) throw new Exception("Can not find start of MultiPolygon in "+ inWKT);
        
        // find the matching parenthisis
        int tempEndIndex = getMatchingParenthesis(inWKT, tempStartIndex + 1);
        if (tempEndIndex == -1) throw new Exception("Can not find end of MultiPolygon in "+inWKT);
        
        // find the Polygons
        ArrayList tempPolygonList = new ArrayList();
        int tempPolygonStartIndex = inWKT.indexOf('(', tempStartIndex + 1);
        int tempPolygonEndIndex = getMatchingParenthesis(inWKT, tempPolygonStartIndex + 1);
        while (tempPolygonStartIndex != -1){
            // parse the groups of points between the parenthesis
            Point[][] tempPointArray = parsePointGroups(inWKT, tempPolygonStartIndex+1, tempPolygonEndIndex-1);
            
            // create the polygon
            Polygon tempPolygon = null;
            if (tempPointArray.length >0){
                LinearRing tempPosativeRing = new LinearRing(tempPointArray[0]);
                ArrayList tempList = new ArrayList();
                for (int i=1; i<tempPointArray.length; i++){
                    tempList.add(new LinearRing(tempPointArray[i]));
                }
                LinearRing[] tempHoles = new LinearRing[tempList.size()];
                tempList.toArray(tempHoles);
                tempPolygon = new Polygon(tempPosativeRing, tempHoles);
                tempPolygonList.add(tempPolygon);
            }
            
            // calculate the next value of the start and end indexes.
            tempPolygonStartIndex = inWKT.indexOf('(', tempPolygonEndIndex + 1);
            tempPolygonEndIndex = getMatchingParenthesis(inWKT, tempPolygonStartIndex + 1);
        }
        
        // create the multipolygon
        Polygon[] tempPolygons = new Polygon[tempPolygonList.size()];
        tempPolygonList.toArray(tempPolygons);
        return new MultiPolygon(tempPolygons);
    }
    
    /** parse a point from a space separated set of numbers.  An example of input may be "1.2 2.3 3.4". */
    protected static Point parsePoint(String inWKT, int inStartIndex, int inEndIndex) throws Exception{
        // find the X coordinate
        int tempStartIndex = inStartIndex;
        int tempEndIndex = inWKT.indexOf(' ',tempStartIndex);
        if ((tempEndIndex == -1) || (tempEndIndex > inEndIndex)) throw new Exception("Can Not find End of X coordinate in Point "+inWKT+ " Between "+inStartIndex+" and "+inEndIndex);
        double x = Double.parseDouble(inWKT.substring(tempStartIndex, tempEndIndex+1).trim());
        
        // find the Y Coordinate
        tempStartIndex = tempEndIndex + 1;
        if (tempStartIndex > inEndIndex) throw new Exception("Can Not find Start of Y coordinate in Point "+inWKT);
        tempEndIndex = inWKT.indexOf(' ',tempStartIndex);
        if ((tempEndIndex == -1) || (tempEndIndex > inEndIndex)) tempEndIndex = inEndIndex;
        double y = Double.parseDouble(inWKT.substring(tempStartIndex, tempEndIndex+1).trim());
        
        // could find a z coordinate in the future.
        return new Point(x,y);
    }
    
    /** parse a set of points from a space separated coma delimited set of numbers.  An example of input may be "1.2 2.3 3.4, 100 200.2, 25 35 45". */
    protected static Point[] parsePoints(String inWKT, int inStartIndex, int inEndIndex) throws Exception{
        // loop through the set separating out the commas
        ArrayList tempArrayList = new ArrayList();
        int tempStartIndex = inStartIndex;
        while ((tempStartIndex != -1) && (tempStartIndex < inEndIndex)){
            int tempEndIndex = inWKT.indexOf(',',tempStartIndex);
            if ((tempEndIndex == -1) || (tempEndIndex > inEndIndex)) tempEndIndex = inEndIndex;
            else tempEndIndex = tempEndIndex -1;
            tempArrayList.add(parsePoint(inWKT, tempStartIndex, tempEndIndex));
            tempStartIndex = tempEndIndex + 2; // to step over the comma
        }
        
        Point[] tempPoints = new Point[tempArrayList.size()];
        tempArrayList.toArray(tempPoints);
        return tempPoints;
    }
    
    /**
     * Parse muliple sets of points from a space separated comma delimited, and parenthetically grouped set of numbers.
     * An example of the type of string that should be sent in is the following. (1.2 2.3 3.4, 1.3 3.4 4.5, 1.4 2.5 3.6),(9.8 8.7,3.4 4.3,1.2 1.3, 1.5 2.3)
     */
    protected static Point[][] parsePointGroups(String inWKT, int inStartIndex, int inEndIndex) throws Exception{
        int tempStartIndex = inWKT.indexOf('(', inStartIndex);
        if ((tempStartIndex == -1) || (tempStartIndex > inEndIndex)) return new Point[0][0];
        int tempEndIndex = inWKT.indexOf(')', tempStartIndex);
        if (tempEndIndex > inEndIndex) tempEndIndex = inEndIndex;
        else tempEndIndex = tempEndIndex -1;
        
        ArrayList tempArrayList = new ArrayList();
        while ((tempStartIndex != -1) && (tempStartIndex < inEndIndex)){
            tempArrayList.add(parsePoints(inWKT, tempStartIndex+1, tempEndIndex));
            tempStartIndex = inWKT.indexOf('(', tempEndIndex);
            tempEndIndex = inWKT.indexOf(')', tempStartIndex);
            if (tempEndIndex > inEndIndex) tempEndIndex = inEndIndex;
            else tempEndIndex = tempEndIndex -1;
        }
        
        // convert the points to an array
        Point[][] tempPointArray = new Point[tempArrayList.size()][0];
        for (int i=0; i<tempArrayList.size(); i++){
            tempPointArray[i] = (Point[]) tempArrayList.get(i);
        }
        return tempPointArray;
    }
    
    /** Given a string and a starting position.  Find the matching parenthesis for the position sent in. */
    private static int getMatchingParenthesis(String inWKT, int inStartLocation){
        int numOpen = 0;
        for (int i=inStartLocation; i<inWKT.length(); i++){
            if (inWKT.charAt(i) == '(') numOpen++;
            else {
                if (inWKT.charAt(i) == ')'){
                    if (numOpen == 0) return i;
                    else numOpen--;
                }
            }
        }
        return -1;
    }
    
    /** Test the Parser */
    public static void main(String [] inArgs){
        try{
            // parse a point
            System.out.println("Point 1");
            Point[] tempPoints = {parsePoint("POINT(1 2 3)")};
            writePoints(tempPoints);
            // parse a point
            System.out.println("Point 2");
            Point[] tempPoints2 = {parsePoint("POINT(1.234 12.34 123.4)")};
            writePoints(tempPoints2);
            // parse a LineString
            System.out.println("LineString 1");
            LineString tempLineString = parseLineString("LINESTRING(0 0,1 1,1 2)");
            writePoints(tempLineString.getPoints());
            // parse a LineString
            System.out.println("LineString 2");
            tempLineString = parseLineString("LINESTRING(1.234 1.234 123.4,12.34 12.34 123.4,123.4 123.4)");
            writePoints(tempLineString.getPoints());
            // parse a Polygon
            System.out.println("Polygon 1");
            Polygon tempPolygon = parsePolygon("POLYGON((0 0 0,4 0 0,4 4 0,0 4 0,0 0 0),(1 1 0,2 1 0,2 2 0,1 2 0,1 1 0))");
            writePoints(tempPolygon.getPoints());
            // parse a Polygon
            System.out.println("Polygon 2");
            tempPolygon = parsePolygon("POLYGON((1.2345 1.2345,12.345 12.345 0,123.45 123.45 0,1234.5 1234.5 0,12345 12345),(1.2345 1.2345 0,12.345 12.345,123.45 123.45 0,1234.5 1234.5 0,12345 12345 0))");
            writePoints(tempPolygon.getPoints());
            // parse a MultiPoint
            System.out.println("MultiPoint 1");
            MultiPoint tempMultiPoint = parseMultiPoint("MULTIPOINT(0 0 0,1 2 1)");
            writePoints(tempMultiPoint.getPoints());
            // parse a MultiPoint
            System.out.println("MultiPoint 2");
            tempMultiPoint = parseMultiPoint("MULTIPOINT(12.34 12.34 0,43.21 43.21)");
            writePoints(tempMultiPoint.getPoints());
            // parse a MultiLineString
            System.out.println("MultiLineString 1");
            MultiLineString tempMultiLineString = parseMultiLineString("MULTILINESTRING((0 0 0,1 1 0,1 2 1),(2 3 1,3 2 1,5 4 1))");
            writePoints(tempMultiLineString.getPoints());
            // parse a MultiLineString
            System.out.println("MultiLineString 2");
            tempMultiLineString = parseMultiLineString("MULTILINESTRING((1.2345 1.2345 0,12.345 12.345 0,123.45 123.45),(1234.5 1234.5 1,5432.1 5432.1 1,543.21 543.21))");
            writePoints(tempMultiLineString.getPoints());
            // parse a MultiPolygon
            System.out.println("MultiPolygon 1");
            MultiPolygon tempMultiPolygon = parseMultiPolygon("MULTIPOLYGON(((0 0 0,4 0 0,4 4 0,0 4 0,0 0 0),(1 1 0,2 1 0,2 2 0,1 2 0,1 1 0)),((-1 -1 0,-1 -2 0,-2 -2 0,-2 -1 0,-1 -1 0)))");
            writePoints(tempMultiPolygon.getPoints());
        }
        catch (Exception e){
            System.out.println(""+e);
        }
        
    }
    
    /** Write out the point Array */
    public static void writePoints(Point[] inPoints){
        for (int i=0; i<inPoints.length; i++){
            System.out.println(""+inPoints[i].getX()+" \t "+inPoints[i].getY());
        }
    }
}
