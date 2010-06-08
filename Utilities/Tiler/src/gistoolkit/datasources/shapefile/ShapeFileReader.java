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

package gistoolkit.datasources.shapefile;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import gistoolkit.features.*;
import cmp.LEDataStream.*;

/**
 * A ShapeFileReader is used to pull shapes out of a shapefile one at a
 * time. 
 * @author head
 */
public class ShapeFileReader extends ShapeFileStream
{
    // the InputStreams being read
    protected LEDataInputStream myShpStream = null;
    protected LEDataInputStream myShxStream = null;

    private int myReadPosition = 0;
   
   /**
    * Create a reader on the named file
    */
   public ShapeFileReader(String inFilename) 
       throws FileNotFoundException, IOException 
    {
        super(inFilename);

         // open the files for reading
        if (myGzipExt != null) {
            FileInputStream fin =
                new FileInputStream(myFilename+myShpExt+myGzipExt);
            GZIPInputStream gzin = new GZIPInputStream(fin);
            myShpStream = new LEDataInputStream(gzin);
            myDbaseStream = new DbaseFileReader(myFilename+myDbfExt+myGzipExt);
        }
        else {
            FileInputStream fin = new FileInputStream(myFilename+myShpExt);
            myShpStream = new LEDataInputStream(fin);
            myDbaseStream = new DbaseFileReader(myFilename+myDbfExt);
        }

        // open the file and read the header
        readHeader();
    }
    
    /**
     * Reads the shapefile header record
     */
   protected void readHeader() {
      // create a new header.
      myHeader = new ShapeFileHeader();

      try {
         // read the header
         myHeader.readHeader(myShpStream);
      }
      catch (IOException ioe) {
         System.out.println("Couldn't read shapefile header for " + myFilename);
      }

      // how many records remain
      myReadPosition = myHeader.getHeaderLength();
   }

   /**
    * Read a single shapefile record
    * returns the read shapefile record or null if there are no more records
    */
    public ShapeFileRecord read() 
        throws IOException
    {
       ShapeFileRecord tempRecord = null;
       if (myReadPosition < myHeader.getFileLength()) {
            // read the dbf to get the attribute values
            Record dbRecord = ((DbaseFileReader)myDbaseStream).read();

            // there ought to be a way to construct a ShapeFileRecord from a
            // Record but for now we'll do this
            tempRecord = new ShapeFileRecord();
            tempRecord.setAttributeNames(dbRecord.getAttributeNames());
            tempRecord.setAttributeTypes(dbRecord.getAttributeTypes());
            tempRecord.setAttributes(dbRecord.getAttributes());

            // Bytes 0 to 4 represent the record number in the file, these may
            // be out of order.
            myShpStream.setLittleEndianMode(false);
            tempRecord.setIndex(myShpStream.readInt());

            // read the content length of this record in 16 bit words,
            // excluding the index, and content length.
            myShpStream.setLittleEndianMode(false);
            int tempContentLength = myShpStream.readInt();

            // read the Type of shape
            myShpStream.setLittleEndianMode(true);
            int tempShapeType = myShpStream.readInt();

            // retrieve that shape.
            tempRecord.setShape(readShape(tempShapeType,
                                          tempContentLength, myShpStream));
            myReadPosition += (4 + tempContentLength);
        }

       return tempRecord;
   }

    /**
     * Reads the shape from the shape file.
     */
    private final Shape readShape(int inShapeType,
                            int inContentLength,
                            LEDataInputStream in)
        throws IOException
    {
        Shape rShape = null;

        switch (inShapeType) {
        case ShapeFile.SHAPE_NULL:
            rShape = readNull(in);
            break;
                
        case ShapeFile.SHAPE_POINT:
            rShape = readPoint(in);
            break;
        
        case ShapeFile.SHAPE_POLYLINE:
            rShape = readPolyLine(in);
            break;
        
        case ShapeFile.SHAPE_POLYGON:
            // Polygon -> converts to MultiPolygon
            rShape = readPolygon(in);
            break;
        
        case ShapeFile.SHAPE_MULTIPOINT:
            rShape = readMultiPoint(in);
            break;
        
        //11 PointZ
        //13 PolyLineZ
        //15 PolygonZ
        //18 MultiPointZ
        
        case ShapeFile.SHAPE_POINTM:
            rShape = readPointM(in);
            break;
        
        case ShapeFile.SHAPE_POLYLINEM:
            rShape = readPolyLineM(in);
            break;
            
        //25 PolygonM
        
        case ShapeFile.SHAPE_MULTIPOINTM:
            rShape = readMultiPointM(in);
            break;
        
        case ShapeFile.SHAPE_MULTIPATCH:
            // MultiPatch
            System.err.println("Multi Patches are not currently supported");
            break;
            
        default:
            System.err.println("Unknown shape type of "+inShapeType);
            break;
        }
        
        return rShape;
    }
    
    /**
     * Reads the null shape from the Shape File.
     */
    private final Shape readNull(LEDataInputStream in) throws IOException{
        System.err.println("Null Type Found");
        return null;
    }

    /**
     * Reads the Point from the shape file.
     */
    private final Point readPoint(LEDataInputStream in)
        throws IOException
    {
        // create a new point
        Point tempPoint = new Point(0,0);

        readPoint(tempPoint, in);

        // return the new Point
        return tempPoint;
    }

    /**
     * Reads the Point from the shape file.
     */
    private final Point readPoint(Point inPoint,
                            LEDataInputStream in)
        throws IOException
    {
        // bytes 1 to 4 are the type and have already been read.

        // bytes 4 to 12 are the X coordinate
        in.setLittleEndianMode(true);
        inPoint.setX(in.readDouble());

        // bytes 12 to 20 are the Y coordinate
        in.setLittleEndianMode(true);
        inPoint.setY(in.readDouble());

        // return the new Point
        return inPoint;
    }

    /**
     * Reads the Envelope from the shape file.
     */
    private final Envelope readEnvelope(LEDataInputStream in) throws IOException{
                
        // read the Coordinates
        in.setLittleEndianMode(true);
        double tempXmin = in.readDouble();
        double tempYmin = in.readDouble();
        double tempXmax = in.readDouble();
        double tempYmax = in.readDouble();
        
        // return the new envelope
        return new Envelope(tempXmin, tempYmin, tempXmax, tempYmax);
    }
    
    /**
     * Reads the MultiPoint from the shape file.
     */
    private final MultiPoint readMultiPoint(LEDataInputStream in) throws IOException{
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 36 are the bounding box of the multi points
        // the MultiPoint will calculate the envelope on creation.
        readEnvelope(in);
        
        // number of points in the MultiPoint
        in.setLittleEndianMode(true);
        int tempNum = in.readInt();
        
        // bytes 40 and up contain the points
        Point[] tempPoints = new Point[tempNum];
        for (int i=0; i<tempNum; i++){
            
            // read the shape type
            in.setLittleEndianMode(true);
            int tempType = in.readInt();
            
            // read the Point
            tempPoints[i] = readPoint(in);
            
        }
        
        // return the new MultiPoint
        return new MultiPoint(tempPoints);
        
    }
    
    /**
     * Reads the MultiPoint from the shape file.
     */
    private final MultiPointM readMultiPointM(LEDataInputStream in) throws IOException{
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 36 are the bounding box of the multi points
        // the MultiPoint will calculate the envelope on creation.
        readEnvelope(in);
        
        // number of points in the MultiPoint
        in.setLittleEndianMode(true);
        int tempNum = in.readInt();
        
        // bytes 40 and up contain the points
        PointM[] tempPoints = new PointM[tempNum];
        for (int i=0; i<tempNum; i++){
            
            // read the shape type
            in.setLittleEndianMode(true);
            int tempType = in.readInt();
            
            // read the Point
            tempPoints[i] = (PointM) readPoint(new PointM(0,0), in);
        }
        
        // read the minimum
        double tempMinM = in.readDouble();
        double tempMaxM = in.readDouble();
        for (int i=0; i<tempPoints.length; i++){
            tempPoints[i].setM(in.readDouble());
        }
        
        // return the new MultiPoint
        return new MultiPointM(tempPoints);
        
    }
    
    /**
     * Reads the MultiPoint from the shape file.
     */
    private final MultiPointZ readMultiPointZ(LEDataInputStream in) throws IOException{
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 36 are the bounding box of the multi points
        // the MultiPoint will calculate the envelope on creation.
        readEnvelope(in);
        
        // number of points in the MultiPoint
        in.setLittleEndianMode(true);
        int tempNum = in.readInt();
        
        // bytes 40 and up contain the points
        PointZ[] tempPoints = new PointZ[tempNum];
        for (int i=0; i<tempNum; i++){
            
            // read the shape type
            in.setLittleEndianMode(true);
            int tempType = in.readInt();
            
            // read the Point
            tempPoints[i] = (PointZ) readPoint(new PointZ(0,0), in);
        }
        
        // read the M coordinates
        double tempMinM = in.readDouble();
        double tempMaxM = in.readDouble();
        for (int i=0; i<tempPoints.length; i++){
            tempPoints[i].setM(in.readDouble());
        }
        
        // read the Z coordinates
        double tempMinZ = in.readDouble();
        double tempMaxZ = in.readDouble();
        for (int i=0; i<tempPoints.length; i++){
            tempPoints[i].setZ(in.readDouble());
        }
        
        // return the new MultiPoint
        return new MultiPointZ(tempPoints);
        
    }
    
    /**
     * Reads the Point from the shape file.
     */
    private final PointM readPointM(LEDataInputStream in) throws IOException{
        
        // create a new point
        PointM tempPoint = new PointM(0,0);
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 12 are the X coordinate
        in.setLittleEndianMode(true);
        tempPoint.setX(in.readDouble());
        
        // bytes 12 to 20 are the Y coordinate
        in.setLittleEndianMode(true);
        tempPoint.setY(in.readDouble());
        
        // bytes 20 to 28 are the M value
        in.setLittleEndianMode(true);
        tempPoint.setM(in.readDouble());
        
        // return the new Point
        return tempPoint;
        
    }
    
    /**
     * Reads the Point from the shape file.
     */
    private final PointZ readPointZ(LEDataInputStream in) throws IOException{
        
        // create a new point
        PointZ tempPoint = new PointZ(0,0);
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 12 are the X coordinate
        in.setLittleEndianMode(true);
        tempPoint.setX(in.readDouble());
        
        // bytes 12 to 20 are the Y coordinate
        in.setLittleEndianMode(true);
        tempPoint.setY(in.readDouble());
        
        // bytes 20 to 28 are the Z value
        in.setLittleEndianMode(true);
        tempPoint.setZ(in.readDouble());
        
        // bytes 28 to 36 are the M value
        in.setLittleEndianMode(true);
        tempPoint.setM(in.readDouble());
        
        // return the new Point
        return tempPoint;
        
    }
    
    /**
     * Reads the Polygon from the shape file.
     */
    private final MultiPolygon readPolygon(LEDataInputStream in) throws IOException{
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 36 are the bounding box of the multi points
        // the Polygon will calculate the envelope on creation.
        readEnvelope(in);
        
        // number of parts in the Polygon
        in.setLittleEndianMode(true);
        int tempNumParts = in.readInt();
        
        // number of points in the Polygon
        in.setLittleEndianMode(true);
        int tempNumPoints = in.readInt();
        
        // part indexes.
        in.setLittleEndianMode(true);
        int[] tempParts = new int[tempNumParts];
        for (int i=0; i<tempParts.length; i++){
            tempParts[i] = in.readInt();
        }
        
        // place to store the lines.
        Vector tempRingVect = new Vector();        
        
        // loop through the parts reading each one.
        for (int i=1; i<=tempParts.length; i++){
            int tempNumPartPoints = 0;
            if (i < tempParts.length) tempNumPartPoints = tempParts[i]-tempParts[i-1];
            else tempNumPartPoints = tempNumPoints-tempParts[i-1];
            
            // read the points.
            double[] tempXPoints = new double[tempNumPartPoints];
            double[] tempYPoints = new double[tempNumPartPoints];
            for (int j=0; j<tempNumPartPoints; j++){
                
                // The X coordinate
                in.setLittleEndianMode(true);
                tempXPoints[j] = in.readDouble();

                // The Y coordinate
                in.setLittleEndianMode(true);
                tempYPoints[j] = in.readDouble();
            }
            
            // create the Line
            LinearRing tempLinearRing = new LinearRing(tempXPoints, tempYPoints);
            tempRingVect.add(tempLinearRing);
        } 
       
        // At this point, there is a bunch of rings in the RingVect.
        // Find the largest ring,
        
        // Vectors for determining holding the shapes and holes.
        Vector tempPosative = new Vector();
        Vector tempNegative = new Vector();
        for (int i = 0; i < tempRingVect.size(); i++) {
            
            // check the orientation of the ring
            LinearRing tempRing = (LinearRing) tempRingVect.elementAt(i);
            
            // if this is posative
            if (tempRing.isClockwise()){
                // add it to the list of shapes
                tempPosative.addElement(tempRing);
            }
            else {
                // add it to the list of holes.
                tempNegative.addElement(tempRing);
            }
        }
        
        // if there are no posative shapes, then check for negative shapes
        if (tempPosative.size() == 0){
            if (tempNegative.size() == 1){
                tempPosative.addElement(tempNegative.elementAt(0));
                tempNegative.removeAllElements();
            }
        }
        
        // if there are no posative shapes, then enter null
        if (tempPosative.size() == 0) {
            return null;
        }
        else {
            
            // order the posative shapes from Inside out.
            // this is to ensure that the negative shapes are in the correct order
            for (int j = 0; j < tempPosative.size() - 1; j++) {
                for (int k = j + 1; k < tempPosative.size(); k++) {
                    LinearRing tempA = (LinearRing) tempPosative.elementAt(j);
                    LinearRing tempB = (LinearRing) tempPosative.elementAt(k);
                    if (tempB.contains(tempA)) {
                        tempPosative.setElementAt(tempB, j);
                        tempPosative.setElementAt(tempA, k);
                    }
                }
            }
            
            // loop through all the posative shapes creating polygons from them.
            Vector tempPolygonVect = new Vector();
            for (int j = 0; j < tempPosative.size(); j++) {
                
                LinearRing tempPosRing = (LinearRing) tempPosative.elementAt(j);
                
                // loop through the negative shapes
                Vector tempNegVect = new Vector();
                for (int k = 0; k < tempNegative.size(); k++) {
                    LinearRing tempNegRing = (LinearRing) tempNegative.elementAt(k);
                    if (tempPosRing.contains(tempNegRing)){
                        tempNegVect.addElement(tempNegRing);
                        
                        // if this is a hole in this polygon, do not add it to larger polygons.
                        tempNegative.removeElement(tempNegRing);
                    }
                }
                
                // create the polygon
                LinearRing[] tempNegRings = new LinearRing[tempNegVect.size()];
                tempNegVect.copyInto(tempNegRings);
                Polygon tempPolygon = new Polygon(tempPosRing, tempNegRings);
                tempPolygonVect.addElement(tempPolygon);
            }
            
            // create the Multi Polygon
            Polygon[] tempPolygons = new Polygon[tempPolygonVect.size()];
            tempPolygonVect.copyInto(tempPolygons);
            
            MultiPolygon tempMultiPolygon = new MultiPolygon(tempPolygons);
            return tempMultiPolygon;
        }
    }
    
    /**
     * Reads the Polygon from the shape file.
     */
    private final MultiPolygonM readPolygonM(LEDataInputStream in) throws IOException{
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 36 are the bounding box of the multi points
        // the Polygon will calculate the envelope on creation.
        readEnvelope(in);
        
        // number of parts in the Polygon
        in.setLittleEndianMode(true);
        int tempNumParts = in.readInt();
        
        // number of points in the Polygon
        in.setLittleEndianMode(true);
        int tempNumPoints = in.readInt();
        
        // part indexes.
        in.setLittleEndianMode(true);
        int[] tempParts = new int[tempNumParts];
        for (int i=0; i<tempParts.length; i++){
            tempParts[i] = in.readInt();
        }
        
        // Points
        Vector tempRingVect = new Vector();
        Vector tempPointVect = new Vector();
        int tempPartNum = 0;
        for (int i=0; i<tempNumPoints; i++){
            
            // read the shape type
            in.setLittleEndianMode(true);
            int tempType = in.readInt();
            
            // read the Point
            PointM tempPoint = (PointM) readPoint(new PointM(0,0), in);
            tempPointVect.addElement(tempPoint);
            
            // if there is another part, then check it
            if ((tempPartNum + 1) < tempNumParts){
                if (i == tempParts[tempPartNum+1]){
                    
                    // create a point array from the points
                    PointM[] tempPointArray = new PointM[tempPointVect.size()];
                    tempPointVect.copyInto(tempPointArray);
                    
                    // create a Ring from the points
                    LinearRingM tempRing = new LinearRingM(tempPointArray);
                    
                    // add the Ring to the line vect
                    tempRingVect.add(tempRing);
                    
                    // clear the point vector for further processing
                    tempPointVect.removeAllElements();
                    
                    // increment the part pointer
                    tempPartNum++;
                }
            }
        }
        
        // add the last Ring
        // create a point array from the points
        PointM[] tempPointArray = new PointM[tempPointVect.size()];
        tempPointVect.copyInto(tempPointArray);
        
        // create a Ring from the points
        LinearRingM tempRing = new LinearRingM(tempPointArray);
        
        // add the Ring to the Ring vect
        tempRingVect.add(tempRing);
        
        // read the M values
        double tempMinM = in.readDouble();
        double tempMaxM = in.readDouble();
        for (int i=0; i<tempRingVect.size(); i++){
            tempRing = (LinearRingM) tempRingVect.elementAt(i);
            PointM[] tempPoints = (PointM[]) tempRing.getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempPoints[i].setM(in.readDouble());
            }
        }
        
        // At this point, there is a bunch of rings in the RingVect.
        // Find the largest ring,
        
        // Vectors for determining holding the shapes and holes.
        Vector tempPosative = new Vector();
        Vector tempNegative = new Vector();
        for (int i = 0; i < tempRingVect.size(); i++) {
            
            // check the orientation of the ring
            tempRing = (LinearRingM) tempRingVect.elementAt(i);
            
            // if this is posative
            if (tempRing.isClockwise()){
                // add it to the list of shapes
                tempPosative.addElement(tempRing);
            }
            else {
                // add it to the list of holes.
                tempNegative.addElement(tempRing);
            }
        }
        
        // if there are no posative shapes, then check for negative shapes
        if (tempPosative.size() == 0){
            if (tempPosative.size() == 1){
                tempPosative.addElement(tempNegative.elementAt(0));
                tempNegative.removeAllElements();
            }
        }
        
        // if there are no posative shapes, then enter null
        if (tempPosative.size() == 0) {
            return null;
        }
        else {
            
            // order the posative shapes from Inside out.
            // this is to ensure that the negative shapes are in the correct order
            for (int j = 0; j < tempPosative.size() - 1; j++) {
                for (int k = j + 1; k < tempPosative.size(); k++) {
                    LinearRing tempA = (LinearRing) tempPosative.elementAt(j);
                    LinearRing tempB = (LinearRing) tempPosative.elementAt(k);
                    if (tempB.contains(tempA)) {
                        tempPosative.setElementAt(tempB, j);
                        tempPosative.setElementAt(tempA, k);
                    }
                }
            }
            
            // loop through all the posative shapes creating polygons from them.
            Vector tempPolygonVect = new Vector();
            for (int j = 0; j < tempPosative.size(); j++) {
                
                LinearRing tempPosRing = (LinearRing) tempPosative.elementAt(j);
                
                // loop through the negative shapes
                Vector tempNegVect = new Vector();
                for (int k = 0; k < tempNegative.size(); k++) {
                    LinearRing tempNegRing = (LinearRing) tempNegative.elementAt(k);
                    if (tempPosRing.contains(tempNegRing)){
                        tempNegVect.addElement(tempNegRing);
                        
                        // if this is a hole in this polygon, do not add it to larger polygons.
                        tempNegative.removeElement(tempNegRing);
                    }
                }
                
                // create the polygon
                LinearRingM[] tempNegRings = new LinearRingM[tempNegVect.size()];
                tempNegVect.copyInto(tempNegRings);
                Polygon tempPolygon = new PolygonM(tempPosRing, tempNegRings);
                tempPolygonVect.addElement(tempPolygon);
            }
            
            // construct the array.
            PolygonM[] tempPolygons = new PolygonM[tempPolygonVect.size()];
            tempPolygonVect.copyInto(tempPolygons);
            
            // create the Multi Polygon
            MultiPolygonM tempMultiPolygon = new MultiPolygonM(tempPolygons);
            return tempMultiPolygon;
        }
    }
    
    /**
     * Reads the Polygon from the shape file.
     */
    private final MultiPolygonZ readPolygonZ(LEDataInputStream in) throws IOException{
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 36 are the bounding box of the multi points
        // the Polygon will calculate the envelope on creation.
        readEnvelope(in);
        
        // number of parts in the Polygon
        in.setLittleEndianMode(true);
        int tempNumParts = in.readInt();
        
        // number of points in the Polygon
        in.setLittleEndianMode(true);
        int tempNumPoints = in.readInt();
        
        // part indexes.
        in.setLittleEndianMode(true);
        int[] tempParts = new int[tempNumParts];
        for (int i=0; i<tempParts.length; i++){
            tempParts[i] = in.readInt();
        }
        
        // Points
        Vector tempRingVect = new Vector();
        Vector tempPointVect = new Vector();
        int tempPartNum = 0;
        for (int i=0; i<tempNumPoints; i++){
            
            // read the shape type
            in.setLittleEndianMode(true);
            int tempType = in.readInt();
            
            // read the Point
            PointZ tempPoint = (PointZ) readPoint(new PointZ(0,0), in);
            tempPointVect.addElement(tempPoint);
            
            // if there is another part, then check it
            if ((tempPartNum + 1) < tempNumParts){
                if (i == tempParts[tempPartNum+1]){
                    
                    // create a point array from the points
                    PointZ[] tempPointArray = new PointZ[tempPointVect.size()];
                    tempPointVect.copyInto(tempPointArray);
                    
                    // create a Ring from the points
                    LinearRingZ tempRing = new LinearRingZ(tempPointArray);
                    
                    // add the Ring to the line vect
                    tempRingVect.add(tempRing);
                    
                    // clear the point vector for further processing
                    tempPointVect.removeAllElements();
                    
                    // increment the part pointer
                    tempPartNum++;
                }
            }
        }
        
        // add the last Ring
        // create a point array from the points
        PointZ[] tempPointArray = new PointZ[tempPointVect.size()];
        tempPointVect.copyInto(tempPointArray);
        
        // create a Ring from the points
        LinearRingZ tempRing = new LinearRingZ(tempPointArray);
        
        // add the Ring to the Ring vect
        tempRingVect.add(tempRing);
        
        // read the M values
        double tempMinM = in.readDouble();
        double tempMaxM = in.readDouble();
        for (int i=0; i<tempRingVect.size(); i++){
            tempRing = (LinearRingZ) tempRingVect.elementAt(i);
            PointZ[] tempPoints = (PointZ[]) tempRing.getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempPoints[i].setM(in.readDouble());
            }
        }
        
        // At this point, there is a bunch of rings in the RingVect.
        // Find the largest ring,
        
        // Vectors for determining holding the shapes and holes.
        Vector tempPosative = new Vector();
        Vector tempNegative = new Vector();
        for (int i = 0; i < tempRingVect.size(); i++) {
            
            // check the orientation of the ring
            tempRing = (LinearRingZ) tempRingVect.elementAt(i);
            
            // if this is posative
            if (tempRing.isClockwise()){
                // add it to the list of shapes
                tempPosative.addElement(tempRing);
            }
            else {
                // add it to the list of holes.
                tempNegative.addElement(tempRing);
            }
        }
        
        // if there are no posative shapes, then check for negative shapes
        if (tempPosative.size() == 0){
            if (tempPosative.size() == 1){
                tempPosative.addElement(tempNegative.elementAt(0));
                tempNegative.removeAllElements();
            }
        }
        
        // if there are no posative shapes, then enter null
        if (tempPosative.size() == 0) {
            return null;
        }
        else {
            
            // order the posative shapes from Inside out.
            // this is to ensure that the negative shapes are in the correct order
            for (int j = 0; j < tempPosative.size() - 1; j++) {
                for (int k = j + 1; k < tempPosative.size(); k++) {
                    LinearRing tempA = (LinearRing) tempPosative.elementAt(j);
                    LinearRing tempB = (LinearRing) tempPosative.elementAt(k);
                    if (tempB.contains(tempA)) {
                        tempPosative.setElementAt(tempB, j);
                        tempPosative.setElementAt(tempA, k);
                    }
                }
            }
            
            // loop through all the posative shapes creating polygons from them.
            Vector tempPolygonVect = new Vector();
            for (int j = 0; j < tempPosative.size(); j++) {
                
                LinearRing tempPosRing = (LinearRing) tempPosative.elementAt(j);
                
                // loop through the negative shapes
                Vector tempNegVect = new Vector();
                for (int k = 0; k < tempNegative.size(); k++) {
                    LinearRing tempNegRing = (LinearRing) tempNegative.elementAt(k);
                    if (tempPosRing.contains(tempNegRing)){
                        tempNegVect.addElement(tempNegRing);
                        
                        // if this is a hole in this polygon, do not add it to larger polygons.
                        tempNegative.removeElement(tempNegRing);
                    }
                }
                
                // create the polygon
                LinearRingZ[] tempNegRings = new LinearRingZ[tempNegVect.size()];
                tempNegVect.copyInto(tempNegRings);
                PolygonZ tempPolygon = new PolygonZ(tempPosRing, tempNegRings);
                tempPolygonVect.addElement(tempPolygon);
            }
            
            // construct the array.
            PolygonZ[] tempPolygons = new PolygonZ[tempPolygonVect.size()];
            tempPolygonVect.copyInto(tempPolygons);
            
            // create the Multi Polygon
            MultiPolygonZ tempMultiPolygon = new MultiPolygonZ(tempPolygons);
            return tempMultiPolygon;
        }
    }
    
    /**
     * Reads the MultiLineString from the shape file.
     */
    private final MultiLineString readPolyLine(LEDataInputStream in) throws IOException{
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 36 are the bounding box of the multi points
        // the LineString will calculate the envelope on creation.
        readEnvelope(in);
        
        // number of parts in the LineString
        in.setLittleEndianMode(true);
        int tempNumParts = in.readInt();
        
        // number of points in the PolyLine
        in.setLittleEndianMode(true);
        int tempNumPoints = in.readInt();
        
        // part indexes.
        in.setLittleEndianMode(true);
        int[] tempParts = new int[tempNumParts];
        for (int i=0; i<tempParts.length; i++){
            tempParts[i] = in.readInt();
        }
        
        // place to store the lines.
        Vector tempLineVect = new Vector();        
        
        // loop through the parts reading each one.
        for (int i=1; i<=tempParts.length; i++){
            int tempNumPartPoints = 0;
            if (i < tempParts.length) tempNumPartPoints = tempParts[i]-tempParts[i-1];
            else tempNumPartPoints = tempNumPoints-tempParts[i-1];
            
            // read the points.
            double[] tempXPoints = new double[tempNumPartPoints];
            double[] tempYPoints = new double[tempNumPartPoints];
            for (int j=0; j<tempNumPartPoints; j++){
                
                // The X coordinate
                in.setLittleEndianMode(true);
                tempXPoints[j] = in.readDouble();

                // The Y coordinate
                in.setLittleEndianMode(true);
                tempYPoints[j] = in.readDouble();
            }
            
            // create the Line
            LineString tempLineString = new LineString(tempXPoints, tempYPoints);
            tempLineVect.add(tempLineString);
        }
        
        // create the MultiLineString
        LineString[] tempLineStrings = new LineString[tempLineVect.size()];
        tempLineVect.copyInto(tempLineStrings);
        MultiLineString tempMultiLineString = new MultiLineString(tempLineStrings);
        
        // return the new MultiLine String
        return tempMultiLineString;
        
    }
    
    /**
     * Reads the MultiLineStringM from the shape file.
     */
    private final MultiLineStringM readPolyLineM(LEDataInputStream in) throws IOException{
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 36 are the bounding box of the multi points
        // the LineString will calculate the envelope on creation.
        readEnvelope(in);
        
        // number of parts in the LineString
        in.setLittleEndianMode(true);
        int tempNumParts = in.readInt();
        
        // number of points in the PolyLine
        in.setLittleEndianMode(true);
        int tempNumPoints = in.readInt();
        
        // part indexes.
        in.setLittleEndianMode(true);
        int[] tempParts = new int[tempNumParts];
        for (int i=0; i<tempParts.length; i++){
            tempParts[i] = in.readInt();
        }
        
        // Points
        Vector tempLineVect = new Vector();
        Vector tempPointVect = new Vector();
        int tempPartNum = 0;
        for (int i=0; i<tempNumPoints; i++){
            
            // read the Point
            PointM tempPoint = (PointM) readPoint(new PointM(0,0), in);
            tempPointVect.addElement(tempPoint);
            
            // if there is another part, then check it
            if ((tempPartNum + 1) < tempNumParts){
                if (i == tempParts[tempPartNum+1]){
                    
                    // create a point array from the points
                    PointM[] tempPointArray = new PointM[tempPointVect.size()];
                    tempPointVect.copyInto(tempPointArray);
                    
                    // create a LineString from the points
                    LineStringM tempLineString = new LineStringM(tempPointArray);
                    
                    // add the line string to the line vect
                    tempLineVect.add(tempLineString);
                    
                    // clear the point vector for further processing
                    tempPointVect.removeAllElements();
                    
                    // increment the part pointer
                    tempPartNum++;
                }
            }
        }
        
        // add the last line string
        // create a point array from the points
        PointM[] tempPointArray = new PointM[tempPointVect.size()];
        tempPointVect.copyInto(tempPointArray);
        
        // create a LineString from the points
        LineStringM tempLineString = new LineStringM(tempPointArray);
        
        // add the line string to the line vect
        tempLineVect.add(tempLineString);
        
        // read the M coordinates
        double tempMmin = in.readDouble();
        double tempMmax = in.readDouble();
        
        for (int i=0; i<tempLineVect.size(); i++){
            PointM[] tempPoints = (PointM[]) tempLineString.getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempPoints[j].setM(in.readDouble());
            }
        }
        
        // create the MultiLineString
        LineStringM[] tempLineStrings = new LineStringM[tempLineVect.size()];
        tempLineVect.copyInto(tempLineStrings);
        MultiLineStringM tempMultiLineString = new MultiLineStringM(tempLineStrings);
        
        // return the new MultiLine String
        return tempMultiLineString;
        
    }
    
    /**
     * Reads the MultiLineStringZ from the shape file.
     */
    private final MultiLineStringZ readPolyLineZ(LEDataInputStream in) throws IOException{
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 36 are the bounding box of the multi points
        // the LineString will calculate the envelope on creation.
        readEnvelope(in);
        
        // number of parts in the LineString
        in.setLittleEndianMode(true);
        int tempNumParts = in.readInt();
        
        // number of points in the PolyLine
        in.setLittleEndianMode(true);
        int tempNumPoints = in.readInt();
        
        // part indexes.
        in.setLittleEndianMode(true);
        int[] tempParts = new int[tempNumParts];
        for (int i=0; i<tempParts.length; i++){
            tempParts[i] = in.readInt();
        }
        
        // Points
        Vector tempLineVect = new Vector();
        Vector tempPointVect = new Vector();
        int tempPartNum = 0;
        for (int i=0; i<tempNumPoints; i++){
            
            // read the Point
            PointZ tempPoint = (PointZ) readPoint(new PointZ(0,0), in);
            tempPointVect.addElement(tempPoint);
            
            // if there is another part, then check it
            if ((tempPartNum + 1) < tempNumParts){
                if (i == tempParts[tempPartNum+1]){
                    
                    // create a point array from the points
                    PointZ[] tempPointArray = new PointZ[tempPointVect.size()];
                    tempPointVect.copyInto(tempPointArray);
                    
                    // create a LineString from the points
                    LineStringZ tempLineString = new LineStringZ(tempPointArray);
                    
                    // add the line string to the line vect
                    tempLineVect.add(tempLineString);
                    
                    // clear the point vector for further processing
                    tempPointVect.removeAllElements();
                    
                    // increment the part pointer
                    tempPartNum++;
                }
            }
        }
        
        // add the last line string
        // create a point array from the points
        PointZ[] tempPointArray = new PointZ[tempPointVect.size()];
        tempPointVect.copyInto(tempPointArray);
        
        // create a LineString from the points
        LineStringZ tempLineString = new LineStringZ(tempPointArray);
        
        // add the line string to the line vect
        tempLineVect.add(tempLineString);
        
        // read the Z coordinates
        double tempZmin = in.readDouble();
        double tempZmax = in.readDouble();
        
        for (int i=0; i<tempLineVect.size(); i++){
            PointZ[] tempPoints = (PointZ[]) tempLineString.getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempPoints[j].setZ(in.readDouble());
            }
        }
        
        // read the M coordinates
        double tempMmin = in.readDouble();
        double tempMmax = in.readDouble();
        
        for (int i=0; i<tempLineVect.size(); i++){
            PointZ[] tempPoints = (PointZ[]) tempLineString.getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempPoints[j].setM(in.readDouble());
            }
        }
        
        // create the MultiLineString
        LineStringZ[] tempLineStrings = new LineStringZ[tempLineVect.size()];
        tempLineVect.copyInto(tempLineStrings);
        MultiLineStringZ tempMultiLineString = new MultiLineStringZ(tempLineStrings);
        
        // return the new MultiLine String
        return tempMultiLineString;
    }
}
