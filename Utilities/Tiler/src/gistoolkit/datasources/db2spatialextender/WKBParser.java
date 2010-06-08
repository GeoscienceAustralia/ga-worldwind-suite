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

package gistoolkit.datasources.db2spatialextender;

import java.util.*;
import java.io.*;
import cmp.LEDataStream.LEDataInputStream;
import cmp.LEDataStream.LEDataOutputStream;
import gistoolkit.features.*;

/**
 * Class used for parsing WKB format objects into features, and vice versa.
 *
 */
public class WKBParser extends Object {
    private static int myByteOrder = 49;
    
    /** Creates new WKBParser */
    public WKBParser() {
    }
    
    /**
     * Retrieves a binary input stream from the database, and parses it into a Point.
     * <p>
     * 	Point   {
     *   double x;
     *   double y;
     * 	};
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.Point
     */
    private static Point parsePoint(LEDataInputStream in) {
        try{
            
            // read the Xcoordinate
            double tempX = in.readDouble();
            double tempY = in.readDouble();
            
            // create the point
            Point tempPoint = new Point(tempX, tempY);
            return tempPoint;
            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println(t);
            t.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a binary input stream from the database, and parses it into a Linear Ring.
     * <p>
     * 	LinearRing   {
     * 	  uint32  numPoints;
     * 	  Point   points[numPoints];
     * 	};
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.LinearRing
     */
    private static LinearRing parseLinearRing(LEDataInputStream in) {
        try{
            // read the number of points.
            int tempNumPoints = in.readInt();
            
            // read the points
            Vector tempPointVect = new Vector();
            for (int i=0; i<tempNumPoints; i++){
                Point tempPoint = parsePoint(in);
                tempPointVect.addElement(tempPoint);
            }
                        
            // create the Ring
            Point[] tempPoints = new Point[tempPointVect.size()];
            tempPointVect.copyInto(tempPoints);
            LinearRing tempLinearRing = new LinearRing(tempPoints);
            return tempLinearRing;
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println(t);
            t.printStackTrace();
        }
        return null;
    }
    
    /**
     * Takes a binary input stream from the database, and parses it into a shape.
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.Shape
     * @param in java.io.InputStream
     */
    public static Shape parseShape(java.io.InputStream in) {
        if (in == null) return null;
        Shape tempShape=null;
        try{
            // set the input stream
            LEDataInputStream tempIn = new LEDataInputStream(in);
            
            // read the endianness of the input stream
            byte[] tempEndian = new byte[1];
            int tempLength = in.read(tempEndian);
            if (((int)tempEndian[0]) == 0)tempIn.setLittleEndianMode(false);
            else tempIn.setLittleEndianMode(true);
            
            // read the datatype of the incoming data
            int tempType = tempIn.readInt();
            
            // select the correct parsing routine
            switch (tempType){
                case 1: tempShape = parseWKBPoint(tempIn); break;
                case 2: tempShape = parseWKBLineString(tempIn); break;
                case 3: tempShape = parseWKBPolygon(tempIn); break;
                case 4: tempShape = parseWKBMultiPoint(tempIn); break;
                case 5: tempShape = parseWKBMultiLineString(tempIn); break;
                case 6: tempShape = parseWKBMultiPolygon(tempIn); break;
                default: System.out.println("Unsupported Type "+tempType);
            }
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println(t);
            t.printStackTrace();
        }
        return tempShape;
    }
    
    /**
     * Retrieves a binary input stream from the database, and parses it into a shape.
     * <p>
     * <pre>
     * WKBLineString {
     *  byte     byteOrder;
     *  uint32   wkbType;                                         // 2
     *  uint32   numPoints;
     *  Point    points[numPoints];
     * }
     * </pre>
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.LinearRing
     */
    private static LineString parseWKBLineString(LEDataInputStream in) {
        try{
            
            // read the number of points
            int tempNumPoints = in.readInt();
            
            // read the points
            Point[] tempPoints = new Point[tempNumPoints];
            for (int i=0; i<tempNumPoints; i++){
                tempPoints[i] = parsePoint(in);
            }
            
            // create the LineString
            LineString tempLineString = new LineString(tempPoints);
            return tempLineString;
            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println(t);
            t.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves a binary input stream from the database, and parses it into a shape.
     * <p>
     * <pre>
     * WKBMultiLineString    {
     *  byte              byteOrder;
     *  uint32            wkbType;      // 5 for multiLineString
     *  uint32            num_wkbLineStrings;
     *  WKBLineString     WKBLineStrings[num_wkbLineStrings];
     * }
     * </pre>
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.LinearRing
     */
    private static MultiLineString parseWKBMultiLineString(LEDataInputStream in) {
        try{
            
            // read the number of points
            int tempNumLineStrings = in.readInt();
            
            // read the points
            LineString[] tempLineStrings = new LineString[tempNumLineStrings];
            for (int i=0; i<tempNumLineStrings; i++){
                
                // read the byte order
                // read the endianness of the input stream
                byte tempEndian = in.readByte();
                if (((int)tempEndian) == 0)in.setLittleEndianMode(false);
                else in.setLittleEndianMode(true);
                
                // read the type
                int tempType = in.readInt();
                
                // parse the LineString
                tempLineStrings[i] = parseWKBLineString(in);
            }
            
            // create the LineString
            MultiLineString tempMultiLineString = new MultiLineString(tempLineStrings);
            return tempMultiLineString;
            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println(t);
            t.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves a binary input stream from the database, and parses it into a shape.
     * <p>
     * <pre>
     * WKBMultiPoint    {
     *  byte              byteOrder;
     *  uint32            wkbType;    // 4 for multi points
     *  uint32            num_wkbPoints;
     *  WKBPoint          WKBPoints[num_wkbPoints];
     * }
     * </pre>
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.MultiPoint
     */
    private static MultiPoint parseWKBMultiPoint(LEDataInputStream in) {
        try{
            
            // read the number of points
            int tempNumPoints = in.readInt();
            
            // read the points
            Point[] tempPoints = new Point[tempNumPoints];
            for (int i=0; i<tempNumPoints; i++){
                tempPoints[i] = parsePoint(in);
            }
            
            // create the LineString
            MultiPoint tempMultiPoint = new MultiPoint(tempPoints);
            return tempMultiPoint;
            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println(t);
            t.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves a binary input stream from the database, and parses it into a shape.
     * <p>
     * <pre>
     * wkbMultiPolygon {
     *  byte              byteOrder;
     *  uint32            wkbType;    // 6 for multi polygons
     *  uint32            num_wkbPolygons;
     *  WKBPolygon        wkbPolygons[num_wkbPolygons];
     *
     * WKBPolygon    {
     *   byte              byteOrder; // 1 for littleendian, 0 for bigendian
     *   uint32            wkbType;   // 3 for polygons
     *   uint32            numRings;
     *   LinearRing        rings[numRings];
     * }
     * </pre>
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.Shape
     * @param in java.io.InputStream
     */
    private static MultiPolygon parseWKBMultiPolygon(LEDataInputStream in) {
        try{
            // read the number of Polygons.
            int tempNumPolygons = in.readInt();
            
            // read the polygons
            Vector tempPolyVect = new Vector();
            for (int i=0; i<tempNumPolygons; i++){
                
                // read the byte order
                byte tempByteOrder = in.readByte();  // there is a bug in DB2, this constantly returns 49
                if (tempByteOrder == 0){
                    in.setLittleEndianMode(false);
                }
                else in.setLittleEndianMode(true);
                
                // read the type
                int tempType = in.readInt();
                
                // add the polygon to the vector
                Polygon tempPolygon = parseWKBPolygon(in);
                if (tempPolygon != null) tempPolyVect.addElement(tempPolygon);
            }
            
            // create the multi Polygon
            Polygon[] tempPolygon = new Polygon[tempPolyVect.size()];
            tempPolyVect.copyInto(tempPolygon);
            
            MultiPolygon tempMultiPolygon = new MultiPolygon(tempPolygon);
            return tempMultiPolygon;
            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println(t);
            t.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves a binary input stream from the database, and parses it into a shape.
     * <p>
     * WKBPoint {
     *  byte     byteOrder;
     *  uint32   wkbType; // 1 for points
     *  Point    point;
     * };
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.LinearRing
     */
    private static Point parseWKBPoint(LEDataInputStream in) {
        try{
            
            // read the Xcoordinate
            double tempX = in.readDouble();
            double tempY = in.readDouble();
            
            // create the point
            Point tempPoint = new Point(tempX, tempY);
            return tempPoint;
            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println(t);
            t.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves a binary input stream from the database, and parses it into a shape.
     * <p>
     * <pre>
     * WKBPolygon    {
     *   byte              byteOrder; // 1 for littleendian, 0 for bigendian
     *   uint32            wkbType;   // 3 for polygons
     *   uint32            numRings;
     *   LinearRing        rings[numRings];
     * }
     * </pre>
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.Shape
     * @param in java.io.InputStream
     */
    private static Polygon parseWKBPolygon(LEDataInputStream in) {
        try{
            // read the number of rings.
            int tempNumRings = in.readInt();
            
            // read the rings
            Vector tempRingVect = new Vector();
            LinearRing tempPosativeRing = null;
            for (int i=0; i<tempNumRings; i++){
                LinearRing tempRing = parseLinearRing(in);
                if (i==0) tempPosativeRing = tempRing;
                else tempRingVect.addElement(tempRing);
            }
            if (tempPosativeRing == null) return null;
            
            
            // create the polygon
            LinearRing[] tempHoles = new LinearRing[tempRingVect.size()];
            tempRingVect.copyInto(tempHoles);
            
            Polygon tempPolygon = new Polygon(tempPosativeRing, tempHoles);
            return tempPolygon;
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println(t);
            t.printStackTrace();
        }
        return null;
    }
    
    /**
     * Takes a shape and produces it's Well Known Binary (WKB) form.
     */
    public static byte[] writeWKB(Shape inShape){
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        LEDataOutputStream out = new LEDataOutputStream(bout);
        
        try{
            if (inShape instanceof Point) writeWKBPoint(out, (Point) inShape);
            if (inShape instanceof LineString) writeWKBLineString(out, (LineString) inShape);
            if (inShape instanceof Polygon) writeWKBPolygon(out, (Polygon) inShape);
            if (inShape instanceof MultiPoint) writeWKBMultiPoint(out, (MultiPoint) inShape);
            if (inShape instanceof MultiLineString) writeWKBMultiLineString(out, (MultiLineString) inShape);
            if (inShape instanceof MultiPolygon) writeWKBMultiPolygon(out, (MultiPolygon) inShape);
            out.flush();
        }
        catch (Exception e){
            System.out.println(e);
        }
        return bout.toByteArray();
    }
    
    /**
     * Returns the base Sql for updating a particular shape type.
     * <p>
     * For a point it would be
     * db2gse.ST_PointFromWKB(cast(? as blob(1m)), db2gse.coordref()..srid(0)))
     * </p>
     * <p>
     * For a MultiPoint it would be
     * db2gse.ST_>PointFromWKB(cast(? as blob(1m)), db2gse.coordref()..srid(0)))
     * </p>
     */
    public static String getSQL(String inShapeType, int inCoordinateReference)throws Exception{
        if (inShapeType == null) return null;
        if (inShapeType.equalsIgnoreCase("ST_Point")) return "db2gse.ST_PointFromWKB(cast(? as blob(1m)), db2gse.coordref()..srid("+inCoordinateReference+"))";
        if (inShapeType.equalsIgnoreCase("ST_Polygon")) return "db2gse.ST_PolyFromWKB(cast(? as blob(1m)), db2gse.coordref()..srid("+inCoordinateReference+"))";
        if (inShapeType.equalsIgnoreCase("ST_LineString")) return "db2gse.ST_LineFromWKB(cast(? as blob(1m)), db2gse.coordref()..srid("+inCoordinateReference+"))";
        if (inShapeType.equalsIgnoreCase("ST_MultiPoint")) return "db2gse.ST_MPointFromWKB(cast(? as blob(1m)), db2gse.coordref()..srid("+inCoordinateReference+"))";
        if (inShapeType.equalsIgnoreCase("ST_MultiLineString")) return "db2gse.ST_MLineFromWKB(cast(? as blob(1m)), db2gse.coordref()..srid("+inCoordinateReference+"))";
        if (inShapeType.equalsIgnoreCase("ST_MultiPolygon")) return "db2gse.ST_MPolyFromWKB(cast(? as blob(1m)), db2gse.coordref()..srid("+inCoordinateReference+"))";
        
        throw new Exception("Unknown Shape Type");
    }
    
    /**
     * Converts the type of shape sent in, to therequired shape.
     */
    public static Shape convert(Shape inShape, String inType)throws Exception{
        if (inType == null) return inShape;
        String tempType = inType.trim();

        // Points
        if (tempType.equalsIgnoreCase("ST_Point")){
            if (inShape instanceof Point) return inShape;
            if (inShape instanceof MultiPoint) {
                MultiPoint tempMPoint = (MultiPoint) inShape;
                if (tempMPoint.getPoints().length > 0){
                    return tempMPoint.getPoints()[0];
                }
            }
        }
        
        // Lines
        if (tempType.equalsIgnoreCase("ST_LineString")){
            if (inShape instanceof LineString) return inShape;
            if (inShape instanceof MultiLineString) {
                MultiLineString tempMLine = (MultiLineString) inShape;
                if (tempMLine.getLines().length > 0){
                    return tempMLine.getLines()[0];
                }
            }
        }
        
        // Polygons
        if (tempType.equalsIgnoreCase("ST_Polygon")){
            if (inShape instanceof Polygon) return inShape;
            if (inShape instanceof MultiPolygon) {
                MultiPolygon tempMPolygon = (MultiPolygon) inShape;
                if (tempMPolygon.getPolygons().length > 0){
                    return tempMPolygon.getPolygons()[0];
                }
            }
        }
        
        // MultiPoints
        if (tempType.equalsIgnoreCase("ST_MultiPoint")){
            if (inShape instanceof MultiPoint) return inShape;
            if (inShape instanceof Point) {
                Point[] tempPoints = new Point[1];
                tempPoints[0] = (Point) inShape;
                return new MultiPoint(tempPoints);
            }
        }

        // MultiLineString
        if (tempType.equalsIgnoreCase("ST_MultiLineString")){
            if (inShape instanceof MultiLineString) return inShape;
            if (inShape instanceof LineString) {
                LineString[] tempLineStrings = new LineString[1];
                tempLineStrings[0] = (LineString) inShape;
                return new MultiLineString(tempLineStrings);
            }
        }

        // MultiPolygon
        if (tempType.equalsIgnoreCase("ST_MultiPolygon")){
            if (inShape instanceof MultiPolygon) return inShape;
            if (inShape instanceof Polygon) {
                Polygon[] tempPolygons = new Polygon[1];
                tempPolygons[0] = (Polygon) inShape;
                return new MultiPolygon(tempPolygons);
            }
        }

        throw new Exception("Can not convert Shape to "+inType);
    }

    /**
     * Writes the Well Known Binary (WKB) representation of the point to the output stream.
     * <p>
     * <pre>
     * 	Point   {
     *   double x;
     *   double y;
     * 	};
     * </pre>
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     */
    private static void writePoint(LEDataOutputStream out, Point inPoint) throws Exception{
        out.writeDouble(inPoint.x);
        out.writeDouble(inPoint.y);            
        return;
    }
    
    /**
     * Writes the Well Known Binary (WKB) representation of the Linear Ring to the output stream.
     * <p>
     * <pre>
     * 	LinearRing   {
     * 	  uint32  numPoints;
     * 	  Point   points[numPoints];
     * 	};
     * </pre>
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     * @return features.LinearRing
     */
    private static void writeLinearRing(LEDataOutputStream out, LinearRing inRing) throws Exception{
        Point[] tempPoints = inRing.getRingPoints();
        
        // Number of points
        int tempNumPoints = tempPoints.length ;
        Point tempEndPoint = null;

        // write the number of points.
        out.writeInt(tempNumPoints);
        
        // write the points
        for (int i=0; i<tempPoints.length; i++){
            writePoint(out, tempPoints[i]);
        }
        
        // our job here is done.
        return;
    }    

    /**
     * Writes the Well Known Binary (WKB) representation of the point to the output stream.
     * <p>
     * <pre>
     * WKBPoint {
     *  byte     byteOrder;
     *  uint32   wkbType; // 1 for points
     *  Point    point;
     * };
     * </pre>
     * </p>
     * Creation date: (4/17/2001 2:59:59 PM)
     */
    private static void writeWKBPoint(LEDataOutputStream out, Point inPoint) throws Exception{
        // write the endianness of the point
        out.writeByte(myByteOrder); // little endian
        
        // write the type of shape
        out.writeInt(1); // 1 is a shape
        
        // write the x coordinate
        out.writeDouble(inPoint.x);
        
        // write the y coordinate
        out.writeDouble(inPoint.y);            
        return;
    }
    
    /**
     * Writes the Well Known Binary (WKB) representation of the LineString to the output stream.
     * <p>
     * <pre>
     * WKBLineString {
     *  byte     byteOrder;
     *  uint32   wkbType;                                         // 2
     *  uint32   numPoints;
     *  Point    points[numPoints];
     * }
     * </pre>
     * </p>
     */
    private static void writeWKBLineString(LEDataOutputStream out, LineString inLineString)throws Exception{
        // write the endianness of the point
        out.writeByte(myByteOrder); // little endian
        
        // write the type of shape
        out.writeInt(2); // 2 is a LineString
        
        // write the number of points.
        Point[] tempPoints = inLineString.getPoints();
        out.writeInt(tempPoints.length);
        
        // write the points
        for (int i=0; i<tempPoints.length; i++){
            writePoint(out, tempPoints[i]);
        }
        return;
    }

    /**
     * Writes the Well Known Binary (WKB) representation of the Polygon to the output stream.
     * <p>
     * <pre>
     * WKBPolygon    {
     *   byte              byteOrder; // 1 for littleendian, 0 for bigendian
     *   uint32            wkbType;   // 3 for polygons
     *   uint32            numRings;
     *   LinearRing        rings[numRings];
     * }
     * </pre>
     * </p>
     * </p>
     */
    private static void writeWKBPolygon(LEDataOutputStream out, Polygon inPolygon)throws Exception{
        // write the endianness of the point
        out.writeByte(myByteOrder); // little endian
        
        // write the type of shape
        out.writeInt(3); // 3 is a Polygon
        
        // write the number of rings.
        LinearRing[] tempHoles = inPolygon.getHoles();
        out.writeInt(tempHoles.length + 1);
        
        // write the rings
        writeLinearRing(out, inPolygon.getPosativeRing());
        
        for (int i=0; i<tempHoles.length; i++){
            writeLinearRing(out, tempHoles[i]);
        }
        return;
    }

    /**
     * Writes the Well Known Binary (WKB) representation of the MultiPolygon to the output stream.
     * <p>
     * <pre>
     * wkbMultiPoint {
     *  byte              byteOrder;
     *  uint32            wkbType;    // 4 for multiPoints
     *  uint32            num_Points;
     *  Points            Points[num_Points];
     * </pre>
     * </p>
     */
    private static void writeWKBMultiPoint(LEDataOutputStream out, MultiPoint inMPoint)throws Exception{
        // write the endianness of the point
        out.writeByte(myByteOrder); // little endian
        
        // write the type of shape
        out.writeInt(4); // 4 is a MultiPoint
        
        // write the number of points.
        Point[] tempPoints = inMPoint.getPoints();
        out.writeInt(tempPoints.length);
        
        // write the points
        for (int i=0; i<tempPoints.length; i++){
            writePoint(out, tempPoints[i]);
        }
        return;
    }

    /**
     * Writes the Well Known Binary (WKB) representation of the MultiLineString to the output stream.
     * <p>
     * <pre>
     * WKBMultiLineString    {
     *  byte              byteOrder;
     *  uint32            wkbType;      // 5 for multiLineString
     *  uint32            num_wkbLineStrings;
     *  WKBLineString     WKBLineStrings[num_wkbLineStrings];
     * }
     * </pre>
     * </p>
     */
    private static void writeWKBMultiLineString(LEDataOutputStream out, MultiLineString inMultiLineString)throws Exception{
        // write the endianness of the point
        out.writeByte(myByteOrder); // little endian
        
        // write the type of shape
        out.writeInt(5); // 5 is a MultiLineString
        
        // write the number of LineStrings.
        LineString[] tempLineStrings = inMultiLineString.getLines();
        out.writeInt(tempLineStrings.length);
        
        // write the points
        for (int i=0; i<tempLineStrings.length; i++){
            writeWKBLineString(out, tempLineStrings[i]);
        }
        return;
    }

    /**
     * Writes the Well Known Binary (WKB) representation of the MultiPolygon to the output stream.
     * <p>
     * <pre>
     * wkbMultiPolygon {
     *  byte              byteOrder;
     *  uint32            wkbType;    // 6 for multi polygons
     *  uint32            num_wkbPolygons;
     *  WKBPolygon        wkbPolygons[num_wkbPolygons];
     *
     * WKBPolygon    {
     *   byte              byteOrder; // 1 for littleendian, 0 for bigendian
     *   uint32            wkbType;   // 3 for polygons
     *   uint32            numRings;
     *   LinearRing        rings[numRings];
     * }
     * </pre>
     * </p>
     */
    private static void writeWKBMultiPolygon(LEDataOutputStream out, MultiPolygon inMultiPolygon)throws Exception{
        // write the endianness of the point
        out.writeByte(myByteOrder); // little endian
        
        // write the type of shape
        out.writeInt(6); // 6 is a MultiPolygon
        
        // write the number of Polygons.
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        out.writeInt(tempPolygons.length);
        
        // write the points
        for (int i=0; i<tempPolygons.length; i++){
            writeWKBPolygon(out, tempPolygons[i]);
        }
        return;
    }
}
