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

import gistoolkit.features.*;
import gistoolkit.features.featureutils.*;
import java.util.Vector;
import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Use this very usefull stream for reading from the file and handling endian ness.
 */
import cmp.LEDataStream.*;

/**
 * Represents a shape file on the disk.
 */
public class ShapeFile {
    /**
     * String to represent the base name of the shape file.
     */
    private String myFilename;
    
    /**
     * For some operating systems, the case of the extension matters
     * so we need to save them.
     * If not gzipped, then myGzipExt will be null.
     */
    private String myShpExt;
    private String myShxExt;
    private String myDbfExt;
    private String myGzipExt;
    
    /** Constants for accessing the various shape types. */
    public static final int SHAPE_NULL = 0;
    public static final int SHAPE_POINT = 1;
    public static final int SHAPE_POLYLINE = 3;
    public static final int SHAPE_POLYGON = 5;
    public static final int SHAPE_MULTIPOINT = 8;
    public static final int SHAPE_POINTZ = 11;
    public static final int SHAPE_POLYLINEZ = 13;
    public static final int SHAPE_POLYGONZ = 15;
    public static final int SHAPE_MULTIPOINTZ = 18;
    public static final int SHAPE_POINTM = 21;
    public static final int SHAPE_POLYLINEM = 23;
    public static final int SHAPE_POLYGONM = 25;
    public static final int SHAPE_MULTIPOINTM = 28;
    public static final int SHAPE_MULTIPATCH = 31;
    
    
    /**
     * File for accessing the attribute data.
     */
    private DbaseFile myDBFile;
    
    /**
     * Class to represent the contents of the shape file header.
     */
    private ShapeFileHeader myHeader = new ShapeFileHeader();
    
    /**
     * Array to hold the record information.
     */
    private ShapeFileRecord[] myRecords;
    
    /**
     * Create a new blank shape file, not too usefull
     */
    public ShapeFile() {
        super();
    }
    
    /**
     * Constructor to create a new Shape File with the given file name.
     */
    public ShapeFile(String inFileName){
        setFile(inFileName);
    }
    
    /**
     * Set the file name for this shape file.
     * The case-sensitive logic is needed for filesystems where
     * case is relevant.
     */
    public void setFile(String inFileName){
        
        // initialize in case bogus name is sent in
        myFilename = null;
        myShpExt = null;
        myDbfExt = null;
        myGzipExt = null;
        
        if (inFileName != null){
            if ((inFileName.endsWith(".SHP")) ||
            (inFileName.endsWith(".SHX")) ||
            (inFileName.endsWith(".DBF"))) {
                myFilename = inFileName.substring(0, inFileName.length()-4);
                myShpExt = ".SHP";
                myDbfExt = ".DBF";
                myGzipExt = null;
            }
            else if ((inFileName.endsWith(".shp")) ||
            (inFileName.endsWith(".shx")) ||
            (inFileName.endsWith(".dbf"))) {
                myFilename = inFileName.substring(0, inFileName.length()-4);
                myShpExt = ".shp";
                myDbfExt = ".dbf";
                myGzipExt = null;
            }
            else if (inFileName.endsWith(".GZ")) {
                // call self recursively to pick up the SHP/shp extension
                setFile(inFileName.substring(0, inFileName.length() - 3));
                myGzipExt = ".GZ";
            }
            else if (inFileName.endsWith(".gz")) {
                // call self recursively to pick up the SHP/shp extension
                setFile(inFileName.substring(0, inFileName.length() - 3));
                myGzipExt = ".gz";
            }
            else {
                // extension not specified
                myFilename = inFileName;
                // look for uncomressed first, then compressed
                // only look for lowercase (on OS where that matters)
                // since that's the "correct" case
                if (isReadable(myFilename + ".shp")) {
                    myShpExt = ".shp";
                    myDbfExt = ".dbf";
                    myGzipExt = null;
                } else if (isReadable(myFilename + ".shp.gz")) {
                    myShpExt = ".shp";
                    myDbfExt = ".dbf";
                    myGzipExt = ".gz";
                }
            }
            
            // initialize the DbaseFile.
            if (myDBFile != null){
                myDBFile.setFileName(myFilename+myDbfExt);
            }
            else{
                myDBFile = new DbaseFile(myFilename+myDbfExt);
            }
            
        }
    }
    
    /**
     * Adds a column to the shape file.
     */
    public void addColumn(String inName, char inType, int inLength, int inDecimalPosition) throws Exception{
        myDBFile.addColumn(inName, inType, inLength, inDecimalPosition);
    }
    
    /**
     * Removes a named column from the shape file
     */
    public void removeColumn(String inName) {
        myDBFile.removeColumn(inName);
        // reset the record attriutes
        setAttributes();
    }
    
    /**
     * checks readability of the specified file
     */
    private boolean isReadable(String inFileName) {
        File inFile = new File(inFileName);
        return inFile.canRead();
    }
    
    /**
     * Retrieves the records from the shape file.
     */
    public ShapeFileRecord[] getRecords(){
        return myRecords;
    }
    
    /**
     * Reads the Envelope from the shape file.
     */
    private Envelope readEnvelope(LEDataInputStream in) throws IOException{
                
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
    private MultiPoint readMultiPoint(LEDataInputStream in) throws IOException{
        
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
    private MultiPointM readMultiPointM(LEDataInputStream in) throws IOException{
        
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
    private MultiPointZ readMultiPointZ(LEDataInputStream in) throws IOException{
        
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
     * Reads the null shape from the Shape File.
     */
    private Shape readNull(LEDataInputStream in) throws IOException{
        System.out.println("Null Type Found");
        return null;
    }
    
    /**
     * Reads the Point from the shape file.
     */
    private Point readPoint(LEDataInputStream in) throws IOException{
        
        // create a new point
        Point tempPoint = new Point(0,0);
        
        // bytes 1 to 4 are the type and have already been read.
        
        // bytes 4 to 12 are the X coordinate
        in.setLittleEndianMode(true);
        tempPoint.setX(in.readDouble());
        
        // bytes 12 to 20 are the Y coordinate
        in.setLittleEndianMode(true);
        tempPoint.setY(in.readDouble());
        
        // return the new Point
        return tempPoint;
    }
    
    /**
     * Reads the Point from the shape file.
     */
    private Point readPoint(Point inPoint, LEDataInputStream in) throws IOException{
        
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
     * Reads the Point from the shape file.
     */
    private PointM readPointM(LEDataInputStream in) throws IOException{
        
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
    private PointZ readPointZ(LEDataInputStream in) throws IOException{
        
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
    private MultiPolygon readPolygon(LEDataInputStream in) throws IOException{
        
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
    private MultiPolygonM readPolygonM(LEDataInputStream in) throws IOException{
        
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
    private MultiPolygonZ readPolygonZ(LEDataInputStream in) throws IOException{
        
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
    private MultiLineString readPolyLine(LEDataInputStream in) throws IOException{
        
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
    private MultiLineStringM readPolyLineM(LEDataInputStream in) throws IOException{
        
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
    private MultiLineStringZ readPolyLineZ(LEDataInputStream in) throws IOException{
        
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
    /**
     * Read the records from a shape file.
     */
    public void readRecords() throws Exception{
        LEDataInputStream in = null;
        if (myGzipExt != null) {
            FileInputStream fin =
            new FileInputStream(myFilename+myShpExt+myGzipExt);
            GZIPInputStream gzin = new GZIPInputStream(fin);
            in = new LEDataInputStream(gzin);
        }
        else {
            FileInputStream fin = new FileInputStream(myFilename+myShpExt);
            in = new LEDataInputStream(fin);
        }
        readRecords(in);
        in.close();
    }
    /**
     * Read the records from a shape file.
     */
    public void readRecords(LEDataInputStream in) throws Exception{
        // create a new header.
        myHeader = new ShapeFileHeader();
        
        // read the header
        myHeader.readHeader(in);
        
        // read the records.
        int tempCurrentLength = myHeader.getHeaderLength();
        Vector tempRecordVect = new Vector();
        
        while (tempCurrentLength < myHeader.myFileLength){
            // read the record header
            ShapeFileRecord tempRecord = new ShapeFileRecord();
            
            // Bytes 0 to 4 represent the record number in the file, these may be out of order.
            in.setLittleEndianMode(false);
            tempRecord.setIndex(in.readInt());
            
            // read the content length of this record in 16 bit words, excluding the index.
            in.setLittleEndianMode(false);
            int tempContentLength = in.readInt();
            
            // read the Type of shape
            in.setLittleEndianMode(true);
            int tempShapeType = in.readInt();
            
            // retrieve that shape.
            tempRecord.setShape(readShape(tempShapeType, tempContentLength, in));
            
            // sort as we go.  if the records are stored correctly
            // (in record index order) then this will be quick.
            for (int i=tempRecordVect.size()-1; i >= 0; i--) {
                ShapeFileRecord compareRec =
                (ShapeFileRecord)tempRecordVect.get(i);
                if (tempRecord.getIndex() > compareRec.getIndex()) {
                    // this is where it goes, add it, mark it null and get out
                    tempRecordVect.add(i+1, tempRecord);
                    tempRecord = null;
                    break;
                }
            }
            // if it wasn't added yet, it goes in front
            if (tempRecord != null) {
                tempRecordVect.add(0, tempRecord);
            }
            
            // update the current length the 4 is for the index, and content length.
            tempCurrentLength = tempCurrentLength + 4 + tempContentLength;
        }
        
        // create the shape records.
        myRecords = new ShapeFileRecord[tempRecordVect.size()];
        tempRecordVect.copyInto(myRecords);
        
        // read the DBF records
        if (myGzipExt != null) {
            myDBFile = new DbaseFile(myFilename+myDbfExt+myGzipExt);
        } else {
            myDBFile = new DbaseFile(myFilename+myDbfExt);
        }
        myDBFile.read();
        int tempLength = myDBFile.getNumRecords();
        if (tempLength != myRecords.length){
            System.out.println("Differernt numbers of records in shape and DBF File");
            System.out.println("DBF File Record Number = "+tempLength);
            System.out.println("Shape File Record Number = "+myRecords.length);
            
            if (tempLength < myRecords.length){
                System.out.println("Using NULL for missing attribute data");
            }
            else{
                System.out.println("Ignoring excess Attribute Data");
            }
        }
        
        setAttributes();
    }
    
    /**
     * Sets the record attributes from the DBF file
     */
    private void setAttributes() {
        // construct the attribute types
        AttributeType[] tempAttributeTypes = new AttributeType[myDBFile.getFieldNames().length];
        for (int i=0; i<tempAttributeTypes.length; i++){
            char tempCType = myDBFile.getFieldType(i);
            String tempType = AttributeType.STRING;
            
            if (tempCType == 'L') tempType=AttributeType.BOOLEAN;
            if (tempCType == 'C') tempType=AttributeType.STRING;
            if (tempCType == 'D') tempType=AttributeType.TIMESTAMP;
            if (tempCType == 'N') tempType=AttributeType.FLOAT;
            if (tempCType == 'F') tempType=AttributeType.FLOAT;
            
            int tempFieldLength = myDBFile.getFieldLength(i);
            int tempDecimalLength = myDBFile.getFieldDecimalLength(i);
            tempAttributeTypes[i] = new AttributeType(tempType, tempFieldLength, tempDecimalLength);
        }
        
        for (int i=0; i<myRecords.length; i++){
            myRecords[i].setAttributeNames(myDBFile.getFieldNames());
            myRecords[i].setAttributes(myDBFile.getRecord(i));
            myRecords[i].setAttributeTypes(tempAttributeTypes);
        }
    }
    /**
     * Reads the shape from the shape file.
     */
    private Shape readShape(int inShapeType, int inContentLength, LEDataInputStream in)throws IOException{
        
        // 0 Null Shape
        if (inShapeType == 0) return readNull(in);
        
        // 1 Point
        if (inShapeType == 1) return readPoint(in);
        
        // 3 PolyLine
        if (inShapeType == 3) return readPolyLine(in);
        
        // 5 Polygon -> converts to MultiPolygon
        if (inShapeType == 5) return readPolygon(in);
        
        // 8 MultiPoint
        if (inShapeType == 8) return readMultiPoint(in);
        
        //11 PointZ
        //13 PolyLineZ
        //15 PolygonZ
        //18 MultiPointZ
        
        //21 PointM
        if (inShapeType == 21) return readPointM(in);
        
        //23 PolyLineM
        if (inShapeType == 23) return readPolyLineM(in);
        
        //25 PolygonM
        
        //28 MultiPointM
        if (inShapeType == 28) return readMultiPointM(in);
        
        //31 MultiPatch
        if (inShapeType == 31){
            System.out.println("Multi Patches are not currently supported");
            return null;
        }
        System.out.println("Unknown shape type of "+inShapeType);
        
        return null;
        
    }
    
    /**
     * Sets the records for the shape file.
     */
    public void setRecords(ShapeFileRecord[] inRecords)throws Exception{
        for (int i=0; i<inRecords.length; i++){
            if (inRecords[i] != null){
                int tempShapeType = getShapeType(inRecords[i].getShape());
                myHeader.myShapeType = tempShapeType;
                break;
            }
        }
        myRecords=inRecords;
    }
    
    /**
     * Writes the Envelope to the shape file.
     * returns the number of 16 bit words written
     */
    private int writeEnvelope(Envelope inEnvelope, LEDataOutputStream out) throws IOException{
        
        // read the Coordinates
        out.setLittleEndianMode(true);
        out.writeDouble(inEnvelope.getMinX());	//min X
        out.writeDouble(inEnvelope.getMinY());	//min Y
        out.writeDouble(inEnvelope.getMaxX());	//max X
        out.writeDouble(inEnvelope.getMaxY());	//max Y
        
        // return the number of 16 bit words written
        return 16;
    }
    /**
     * Read the header from the shape file.
     */
    private void writeHeader(LEDataOutputStream out) throws IOException{
        if (myHeader != null){
            // the first four bytes are the file code.
            out.setLittleEndianMode(false);
            out.writeInt(myHeader.myFileCode);
            
            // From 4 to 8 are unused.
            out.setLittleEndianMode(false);
            out.writeInt(myHeader.myUnused1);
            
            // From 8 to 12 are unused.
            out.setLittleEndianMode(false);
            out.writeInt(myHeader.myUnused2);
            
            // From 12 to 16 are unused.
            out.setLittleEndianMode(false);
            out.writeInt(myHeader.myUnused3);
            
            // From 16 to 20 are unused.
            out.setLittleEndianMode(false);
            out.writeInt(myHeader.myUnused4);
            
            // From 20 to 24 are unused.
            out.setLittleEndianMode(false);
            out.writeInt(myHeader.myUnused5);
            
            // From 24 to 28 are the file length.
            out.setLittleEndianMode(false);
            out.writeInt(myHeader.myFileLength);
            
            // From 28 to 32 are the File Version.
            out.setLittleEndianMode(true);
            out.writeInt(myHeader.myVersion);
            
            // From 32 to 36 are the Shape Type.
            out.setLittleEndianMode(true);
            out.writeInt(myHeader.myShapeType);
            
            // From 36 to 44 are Xmin.
            out.setLittleEndianMode(true);
            out.writeDouble(myHeader.myXmin);
            
            // From 44 to 52 are Ymin.
            out.setLittleEndianMode(true);
            out.writeDouble(myHeader.myYmin);
            
            // From 52 to 60 are Xmax.
            out.setLittleEndianMode(true);
            out.writeDouble(myHeader.myXmax);
            
            // From 60 to 68 are Ymax.
            out.setLittleEndianMode(true);
            out.writeDouble(myHeader.myYmax);
            
            // From 68 to 76 are Zmin.
            out.setLittleEndianMode(true);
            out.writeDouble(myHeader.myZmin);
            
            // From 76 to 84 are Zmax.
            out.setLittleEndianMode(true);
            out.writeDouble(myHeader.myZmax);
            
            // From 84 to 92 are Mmin.
            out.setLittleEndianMode(true);
            out.writeDouble(myHeader.myMmin);
            
            // From 92 to 100 are Mmax.
            out.setLittleEndianMode(true);
            out.writeDouble(myHeader.myMmax);
            
            // that is all 100 bytes of the header.
        }
    }
    /**
     * Writes the MultiPoint to the shape file.
     * returns the number of 16 bit words written.
     */
    private int writeMultiPoint(MultiPointM inMultiPointM, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(28);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inMultiPointM.getEnvelope(), out);
        
        // number of points in the MultiPoint
        out.setLittleEndianMode(true);
        PointM[] tempPoints = (PointM[]) inMultiPointM.getPoints();
        out.writeInt(tempPoints.length);
        tempWords += 2;
        
        // write the points
        for (int i=0; i<tempPoints.length; i++){
            tempWords += writePoint(tempPoints[i], out);
        }
        
        // write M values
        out.writeDouble(inMultiPointM.getMinM());
        tempWords += 4;
        out.writeDouble(inMultiPointM.getMaxM());
        tempWords += 4;
        for (int j=0; j<tempPoints.length; j++){
            out.writeDouble(tempPoints[j].getM());
            tempWords += 4;
        }
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiPoint to the shape file.
     * returns the number of 16 bit words written.
     */
    private int writeMultiPoint(MultiPointZ inMultiPointZ, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(18);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inMultiPointZ.getEnvelope(), out);
        
        // number of points in the MultiPoint
        out.setLittleEndianMode(true);
        PointZ[] tempPoints = (PointZ[]) inMultiPointZ.getPoints();
        out.writeInt(tempPoints.length);
        tempWords += 2;
        
        // write the points
        for (int i=0; i<tempPoints.length; i++){
            tempWords += writePoint(tempPoints[i], out);
        }
        
        // write Z values
        out.writeDouble(inMultiPointZ.getMinZ());
        tempWords += 4;
        out.writeDouble(inMultiPointZ.getMaxZ());
        tempWords += 4;
        for (int j=0; j<tempPoints.length; j++){
            out.writeDouble(tempPoints[j].getZ());
            tempWords += 4;
        }
        
        // write M values
        out.writeDouble(inMultiPointZ.getMinM());
        tempWords += 4;
        out.writeDouble(inMultiPointZ.getMaxM());
        tempWords += 4;
        for (int j=0; j<tempPoints.length; j++){
            out.writeDouble(tempPoints[j].getM());
            tempWords += 4;
        }
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiPoint to the shape file.
     * returns the number of 16 bit words written.
     */
    private int writeMultiPoint(MultiPoint inMultiPoint, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(8);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inMultiPoint.getEnvelope(), out);
        
        // number of points in the MultiPoint
        out.setLittleEndianMode(true);
        Point[] tempPoints = inMultiPoint.getPoints();
        out.writeInt(tempPoints.length);
        tempWords += 2;
        
        // write the points
        for (int i=0; i<tempPoints.length; i++){
            tempWords += writePoint(tempPoints[i], out);
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the null shape to the Shape File.
     * Returns the number of 16 bit words written to the Shape File.
     */
    private int writeNull(LEDataOutputStream out) throws IOException{
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(0);
        
        return 2;
        
    }
    
    /**
     * writes the point to the output stream returns the length written.
     */
    private int writePoint(Point inPoint, LEDataOutputStream out) throws IOException{
        
        // write the X value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getX());
        
        // write the Y value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getY());
        
        // return the number of words written
        return 8;
        
    }
    
    /**
     * writes the point to the output stream returns the length written.
     */
    private int writePointM(PointM inPoint, LEDataOutputStream out) throws IOException{
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(21);
        
        // write the X value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getX());
        
        // write the Y value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getY());
        
        // write the M value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getM());
        
        // return the number of words written
        return 14;
        
    }
    
    /**
     * writes the point to the output stream returns the length written.
     */
    private int writePointType(Point inPoint, LEDataOutputStream out) throws IOException{
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(1);
        
        // write the X value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getX());
        
        // write the Y value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getY());
        
        // return the number of words written
        return 10;
        
    }
    
    /**
     * writes the point to the output stream returns the length written.
     */
    private int writePointZ(PointZ inPoint, LEDataOutputStream out) throws IOException{
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(21);
        
        // write the X value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getX());
        
        // write the Y value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getY());
        
        // write the Z value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getZ());
        
        // write the M value
        out.setLittleEndianMode(true);
        out.writeDouble(inPoint.getM());
        
        // return the number of words written
        return 18;
        
    }
    
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolygon(MultiPolygonM inMultiPolygon, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(5);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inMultiPolygon.getEnvelope(), out);
        
        // create a part vect
        Vector tempParts = new Vector();
        PolygonM[] tempPolygons = (PolygonM[]) inMultiPolygon.getPolygons();
        for (int j=0; j<tempPolygons.length; j++){
            tempParts.addElement(tempPolygons[j].getPosativeRing());
            LinearRingM[] tempHoles = (LinearRingM[]) tempPolygons[j].getHoles();
            for (int i=0; i<tempHoles.length; i++){
                tempParts.addElement(tempHoles[i]);
            }
        }
        
        // number of parts in the Polygon
        out.setLittleEndianMode(true);
        out.writeInt(tempParts.size());
        tempWords += 2;
        
        // number of points in the polygon.
        LinearRingM[] tempRings = new LinearRingM[tempParts.size()];
        tempParts.copyInto(tempRings);
        int numPoints = 0;
        for (int i=0; i<tempRings.length; i++){
            numPoints += tempRings[i].getPoints().length;
        }
        out.writeInt(numPoints);
        tempWords += 2;
        
        
        // part indexes.
        int tempLocation = 0;
        for (int i=0; i<tempRings.length; i++){
            out.writeInt(tempLocation);
            tempWords += 2;
            tempLocation += tempRings[i].getPoints().length;
        }
        
        // Points
        for (int i=0; i<tempRings.length; i++){
            Point[] tempPoints = tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempWords += writePoint(tempPoints[j], out);
            }
        }
        
        // write the M values.
        out.writeDouble(inMultiPolygon.getMinM());
        tempWords += 4;
        out.writeDouble(inMultiPolygon.getMaxM());
        tempWords += 4;
        for (int i=0; i<tempRings.length; i++){
            PointM[] tempPoints = (PointM[]) tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                out.writeDouble(tempPoints[j].getM());
                tempWords += 4;
            }
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolygon(MultiPolygonZ inMultiPolygon, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(5);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inMultiPolygon.getEnvelope(), out);
        
        // create a part vect
        Vector tempParts = new Vector();
        PolygonZ[] tempPolygons = (PolygonZ[]) inMultiPolygon.getPolygons();
        for (int j=0; j<tempPolygons.length; j++){
            tempParts.addElement(tempPolygons[j].getPosativeRing());
            LinearRingZ[] tempHoles = (LinearRingZ[]) tempPolygons[j].getHoles();
            for (int i=0; i<tempHoles.length; i++){
                tempParts.addElement(tempHoles[i]);
            }
        }
        
        // number of parts in the Polygon
        out.setLittleEndianMode(true);
        out.writeInt(tempParts.size());
        tempWords += 2;
        
        // number of points in the polygon.
        LinearRingZ[] tempRings = new LinearRingZ[tempParts.size()];
        tempParts.copyInto(tempRings);
        int numPoints = 0;
        for (int i=0; i<tempRings.length; i++){
            numPoints += tempRings[i].getPoints().length;
        }
        out.writeInt(numPoints);
        tempWords += 2;
        
        
        // part indexes.
        int tempLocation = 0;
        for (int i=0; i<tempRings.length; i++){
            out.writeInt(tempLocation);
            tempWords += 2;
            tempLocation += tempRings[i].getPoints().length;
        }
        
        // Points
        for (int i=0; i<tempRings.length; i++){
            Point[] tempPoints = tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempWords += writePoint(tempPoints[j], out);
            }
        }
        
        // write the Z values.
        out.writeDouble(inMultiPolygon.getMinZ());
        tempWords += 4;
        out.writeDouble(inMultiPolygon.getMaxZ());
        tempWords += 4;
        for (int i=0; i<tempRings.length; i++){
            PointZ[] tempPoints = (PointZ[]) tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                out.writeDouble(tempPoints[j].getZ());
                tempWords += 4;
            }
        }
        
        // write the M values.
        out.writeDouble(inMultiPolygon.getMinM());
        tempWords += 4;
        out.writeDouble(inMultiPolygon.getMaxM());
        tempWords += 4;
        for (int i=0; i<tempRings.length; i++){
            PointM[] tempPoints = (PointM[]) tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                out.writeDouble(tempPoints[j].getM());
                tempWords += 4;
            }
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolygon(PolygonM inPolygon, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(25);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inPolygon.getEnvelope(), out);
        
        // create a part vect
        Vector tempParts = new Vector();
        tempParts.addElement(inPolygon.getPosativeRing());
        LinearRingM[] tempHoles = (LinearRingM[]) inPolygon.getHoles();
        for (int i=0; i<tempHoles.length; i++){
            tempParts.addElement(tempHoles[i]);
        }
        
        // number of parts in the Polygon
        out.setLittleEndianMode(true);
        out.writeInt(tempParts.size());
        tempWords += 2;
        
        // number of points in the polygon.
        LinearRingM[] tempRings = new LinearRingM[tempParts.size()];
        tempParts.copyInto(tempRings);
        int numPoints = 0;
        for (int i=0; i<tempRings.length; i++){
            numPoints += tempRings[i].getPoints().length;
        }
        out.writeInt(numPoints);
        tempWords += 2;
        
        
        // part indexes.
        int tempLocation = 0;
        for (int i=0; i<tempRings.length; i++){
            out.writeInt(tempLocation);
            tempWords += 2;
            tempLocation += tempRings[i].getPoints().length;
        }
        
        // Points
        for (int i=0; i<tempRings.length; i++){
            Point[] tempPoints = tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempWords += writePoint(tempPoints[j], out);
            }
        }
        
        // write the M values.
        out.writeDouble(inPolygon.getMinM());
        tempWords += 4;
        out.writeDouble(inPolygon.getMaxM());
        tempWords += 4;
        for (int i=0; i<tempRings.length; i++){
            PointM[] tempPoints = (PointM[]) tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                out.writeDouble(tempPoints[j].getM());
                tempWords += 4;
            }
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolygon(PolygonZ inPolygon, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(15);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inPolygon.getEnvelope(), out);
        
        // create a part vect
        Vector tempParts = new Vector();
        tempParts.addElement(inPolygon.getPosativeRing());
        LinearRingZ[] tempHoles = (LinearRingZ[]) inPolygon.getHoles();
        for (int i=0; i<tempHoles.length; i++){
            tempParts.addElement(tempHoles[i]);
        }
        
        // number of parts in the Polygon
        out.setLittleEndianMode(true);
        out.writeInt(tempParts.size());
        tempWords += 2;
        
        // number of points in the polygon.
        LinearRingZ[] tempRings = new LinearRingZ[tempParts.size()];
        tempParts.copyInto(tempRings);
        int numPoints = 0;
        for (int i=0; i<tempRings.length; i++){
            numPoints += tempRings[i].getPoints().length;
        }
        out.writeInt(numPoints);
        tempWords += 2;
        
        
        // part indexes.
        int tempLocation = 0;
        for (int i=0; i<tempRings.length; i++){
            out.writeInt(tempLocation);
            tempWords += 2;
            tempLocation += tempRings[i].getPoints().length;
        }
        
        // Points
        for (int i=0; i<tempRings.length; i++){
            Point[] tempPoints = tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempWords += writePoint(tempPoints[j], out);
            }
        }
        
        // write the Z values.
        out.writeDouble(inPolygon.getMinZ());
        tempWords += 4;
        out.writeDouble(inPolygon.getMaxZ());
        tempWords += 4;
        for (int i=0; i<tempRings.length; i++){
            PointZ[] tempPoints = (PointZ[]) tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                out.writeDouble(tempPoints[j].getZ());
                tempWords += 4;
            }
        }
        
        // write the M values.
        out.writeDouble(inPolygon.getMinM());
        tempWords += 4;
        out.writeDouble(inPolygon.getMaxM());
        tempWords += 4;
        for (int i=0; i<tempRings.length; i++){
            PointM[] tempPoints = (PointM[]) tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                out.writeDouble(tempPoints[j].getM());
                tempWords += 4;
            }
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolygon(MultiPolygon inMultiPolygon, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(5);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inMultiPolygon.getEnvelope(), out);
        
        // create a part vect
        Vector tempParts = new Vector();
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        for (int j=0; j<tempPolygons.length; j++){
            tempParts.addElement(tempPolygons[j].getPosativeRing());
            LinearRing[] tempHoles = tempPolygons[j].getHoles();
            for (int i=0; i<tempHoles.length; i++){
                tempParts.addElement(tempHoles[i]);
            }
        }
        
        // number of parts in the Polygon
        out.setLittleEndianMode(true);
        out.writeInt(tempParts.size());
        tempWords += 2;
        
        // number of points in the polygon.
        LinearRing[] tempRings = new LinearRing[tempParts.size()];
        tempParts.copyInto(tempRings);
        int numPoints = 0;
        for (int i=0; i<tempRings.length; i++){
            numPoints += tempRings[i].getPoints().length;
        }
        out.writeInt(numPoints);
        tempWords += 2;
        
        
        // part indexes.
        int tempLocation = 0;
        for (int i=0; i<tempRings.length; i++){
            out.writeInt(tempLocation);
            tempWords += 2;
            tempLocation += tempRings[i].getPoints().length;
        }
        
        // Points
        for (int i=0; i<tempRings.length; i++){
            Point[] tempPoints = tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempWords += writePoint(tempPoints[j], out);
            }
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    
    /**
     * Writes the Polygon to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolygon(Polygon inPolygon, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(5);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inPolygon.getEnvelope(), out);
        
        // create a part vect
        Vector tempParts = new Vector();
        tempParts.addElement(inPolygon.getPosativeRing());
        LinearRing tempPosativeRing = inPolygon.getPosativeRing();
        if (!tempPosativeRing.isClockwise()) tempPosativeRing.reorder();
        LinearRing[] tempHoles = inPolygon.getHoles();
        if (tempHoles != null){
            for (int i=0; i<tempHoles.length; i++){
                if (tempHoles[i] != null){
                    if (tempHoles[i].isClockwise()) tempHoles[i].reorder();
                    tempParts.addElement(tempHoles[i]);
                }
            }
        }
        
        // number of parts in the Polygon
        out.setLittleEndianMode(true);
        out.writeInt(tempParts.size());
        tempWords += 2;
        
        // number of points in the polygon.
        LinearRing[] tempRings = new LinearRing[tempParts.size()];
        tempParts.copyInto(tempRings);
        
        // ensure that the rings are closed
        int numPoints = 0;
        for (int i=0; i<tempRings.length; i++){
            tempRings[i].ensureClosed();
            numPoints += tempRings[i].getPoints().length;
        }
        out.writeInt(numPoints);
        tempWords += 2;
        
        
        // part indexes.
        int tempLocation = 0;
        for (int i=0; i<tempRings.length; i++){
            out.writeInt(tempLocation);
            tempWords += 2;
            tempLocation += tempRings[i].getPoints().length;
        }
        
        // Points
        for (int i=0; i<tempRings.length; i++){
            Point[] tempPoints = tempRings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempWords += writePoint(tempPoints[j], out);
            }
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolyLine(LineStringM inLineString, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(23);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inLineString.getEnvelope(), out);
        
        // number of parts in the MultiLineString
        out.setLittleEndianMode(true);
        out.writeInt(1);
        tempWords += 2;
        
        // number of points in the PolyLine
        out.setLittleEndianMode(true);
        PointM[] tempPoints = (PointM[]) inLineString.getPoints();
        out.writeInt(tempPoints.length);
        tempWords += 2;
        
        // part indexes.
        out.setLittleEndianMode(true);
        out.writeInt(0);
        tempWords += 2;
        
        // Points
        for (int j=0; j<tempPoints.length; j++){
            tempWords += writePoint(tempPoints[j], out);
        }
        
        // write M values
        out.writeDouble(inLineString.getMinM());
        tempWords += 4;
        out.writeDouble(inLineString.getMaxM());
        tempWords += 4;
        for (int j=0; j<tempPoints.length; j++){
            out.writeDouble(tempPoints[j].getM());
            tempWords += 4;
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolyLine(LineStringZ inLineString, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(13);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inLineString.getEnvelope(), out);
        
        // number of parts in the MultiLineString
        out.setLittleEndianMode(true);
        out.writeInt(1);
        tempWords += 2;
        
        // number of points in the PolyLine
        out.setLittleEndianMode(true);
        PointZ[] tempPoints = (PointZ[]) inLineString.getPoints();
        out.writeInt(tempPoints.length);
        tempWords += 2;
        
        // part indexes.
        out.setLittleEndianMode(true);
        out.writeInt(0);
        tempWords += 2;
        
        // Points
        for (int j=0; j<tempPoints.length; j++){
            tempWords += writePoint(tempPoints[j], out);
        }
        
        // write Z values
        out.writeDouble(inLineString.getMinZ());
        tempWords += 4;
        out.writeDouble(inLineString.getMaxZ());
        tempWords += 4;
        for (int j=0; j<tempPoints.length; j++){
            out.writeDouble(tempPoints[j].getZ());
            tempWords += 4;
        }
        
        // write M values
        out.writeDouble(inLineString.getMinM());
        tempWords += 4;
        out.writeDouble(inLineString.getMaxM());
        tempWords += 4;
        for (int j=0; j<tempPoints.length; j++){
            out.writeDouble(tempPoints[j].getM());
            tempWords += 4;
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolyLine(MultiLineStringM inMultiLineStringM, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(23);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inMultiLineStringM.getEnvelope(), out);
        
        // number of parts in the MultiLineString
        out.setLittleEndianMode(true);
        LineStringM[] tempLineStrings = (LineStringM[]) inMultiLineStringM.getLines();
        out.writeInt(tempLineStrings.length);
        tempWords += 2;
        
        // number of points in the PolyLine
        out.setLittleEndianMode(true);
        int tempNumPoints = 0;
        for (int i=0; i<tempLineStrings.length; i++){
            tempNumPoints = tempNumPoints + tempLineStrings[i].getPoints().length;
        }
        out.writeInt(tempNumPoints);
        tempWords += 2;
        
        // part indexes.
        out.setLittleEndianMode(true);
        int tempLocation = 0;
        for (int i=0; i<tempLineStrings.length; i++){
            out.writeInt(tempLocation);
            tempWords += 2;
            tempLocation += tempLineStrings[i].getPoints().length;
        }
        
        // Points
        for (int i=0; i<tempLineStrings.length; i++){
            PointM[] tempPoints = (PointM[]) tempLineStrings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempWords += writePoint(tempPoints[j], out);
            }
        }
        
        // write M values
        out.writeDouble(inMultiLineStringM.getMinM());
        tempWords += 4;
        out.writeDouble(inMultiLineStringM.getMaxM());
        tempWords += 4;
        LineStringM[] tempLines = (LineStringM[]) inMultiLineStringM.getLines();
        for (int i=0; i<tempLines.length; i++){
            PointM[] tempPoints = (PointM[]) tempLines[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                out.writeDouble(tempPoints[j].getM());
                tempWords += 4;
            }
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolyLine(MultiLineStringZ inMultiLineStringZ, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(13);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inMultiLineStringZ.getEnvelope(), out);
        
        // number of parts in the MultiLineString
        out.setLittleEndianMode(true);
        LineStringZ[] tempLineStrings = (LineStringZ[]) inMultiLineStringZ.getLines();
        out.writeInt(tempLineStrings.length);
        tempWords += 2;
        
        // number of points in the PolyLine
        out.setLittleEndianMode(true);
        int tempNumPoints = 0;
        for (int i=0; i<tempLineStrings.length; i++){
            tempNumPoints = tempNumPoints + tempLineStrings[i].getPoints().length;
        }
        out.writeInt(tempNumPoints);
        tempWords += 2;
        
        // part indexes.
        out.setLittleEndianMode(true);
        int tempLocation = 0;
        for (int i=0; i<tempLineStrings.length; i++){
            out.writeInt(tempLocation);
            tempWords += 2;
            tempLocation += tempLineStrings[i].getPoints().length;
        }
        
        // Points
        for (int i=0; i<tempLineStrings.length; i++){
            PointM[] tempPoints = (PointM[]) tempLineStrings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempWords += writePoint(tempPoints[j], out);
            }
        }
        
        // write Z values
        out.writeDouble(inMultiLineStringZ.getMinZ());
        tempWords += 4;
        out.writeDouble(inMultiLineStringZ.getMaxZ());
        tempWords += 4;
        LineStringZ[] tempLines = (LineStringZ[]) inMultiLineStringZ.getLines();
        for (int i=0; i<tempLines.length; i++){
            PointZ[] tempPoints = (PointZ[]) tempLines[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                out.writeDouble(tempPoints[j].getZ());
                tempWords += 4;
            }
        }
        
        // write M values
        out.writeDouble(inMultiLineStringZ.getMinM());
        tempWords += 4;
        out.writeDouble(inMultiLineStringZ.getMaxM());
        tempWords += 4;
        tempLines = (LineStringZ[]) inMultiLineStringZ.getLines();
        for (int i=0; i<tempLines.length; i++){
            PointZ[] tempPoints = (PointZ[]) tempLines[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                out.writeDouble(tempPoints[j].getM());
                tempWords += 4;
            }
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolyLine(LineString inLineString, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(3);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inLineString.getEnvelope(), out);
        
        // number of parts in the MultiLineString
        out.setLittleEndianMode(true);
        out.writeInt(1);
        tempWords += 2;
        
        // number of points in the PolyLine
        out.setLittleEndianMode(true);
        Point[] tempPoints = inLineString.getPoints();
        out.writeInt(tempPoints.length);
        tempWords += 2;
        
        // part indexes.
        out.setLittleEndianMode(true);
        out.writeInt(0);
        tempWords += 2;
        
        // Points
        for (int j=0; j<tempPoints.length; j++){
            tempWords += writePoint(tempPoints[j], out);
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the MultiLineString to the shape file.
     * returns the number of 16 bit words written to the file.
     */
    private int writePolyLine(MultiLineString inMultiLineString, LEDataOutputStream out) throws IOException{
        
        // number of 16 bit words written.
        int tempWords = 0;
        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(3);
        tempWords += 2;
        
        // write the envelope
        tempWords += writeEnvelope(inMultiLineString.getEnvelope(), out);
        
        // number of parts in the MultiLineString
        out.setLittleEndianMode(true);
        LineString[] tempLineStrings = inMultiLineString.getLines();
        out.writeInt(tempLineStrings.length);
        tempWords += 2;
        
        // number of points in the PolyLine
        out.setLittleEndianMode(true);
        int tempNumPoints = 0;
        for (int i=0; i<tempLineStrings.length; i++){
            tempNumPoints = tempNumPoints + tempLineStrings[i].getPoints().length;
        }
        out.writeInt(tempNumPoints);
        tempWords += 2;
        
        // part indexes.
        out.setLittleEndianMode(true);
        int tempLocation = 0;
        for (int i=0; i<tempLineStrings.length; i++){
            out.writeInt(tempLocation);
            tempWords += 2;
            tempLocation += tempLineStrings[i].getPoints().length;
        }
        
        // Points
        for (int i=0; i<tempLineStrings.length; i++){
            Point[] tempPoints = tempLineStrings[i].getPoints();
            for (int j=0; j<tempPoints.length; j++){
                tempWords += writePoint(tempPoints[j], out);
            }
        }
        
        // return the number of 16 bit words written.
        return tempWords;
        
    }
    /**
     * Writes the records to a shape file.
     */
    public void writeRecords() throws Exception{
        // write the shape file.
        FileOutputStream fout = new FileOutputStream(myFilename+".shp");
        LEDataOutputStream out = new LEDataOutputStream(fout);
        
        // write the index file.
        FileOutputStream fidxout = new FileOutputStream(myFilename+".shx");
        LEDataOutputStream idxout = new LEDataOutputStream(fidxout);
        
        // write the records.
        writeRecords(out, idxout);
        
        // close the streams.
        out.close();
        idxout.close();
        
        // write the database
        myDBFile.removeAllRecords();
        myDBFile.setFileName(myFilename+".dbf");
        for (int i=0; i<myRecords.length; i++){
            myDBFile.addRecord(myRecords[i].getAttributes());
        }
        myDBFile.write();
        
    }
    /**
     * Read the records from a shape file.
     */
    public void writeRecords(LEDataOutputStream out, LEDataOutputStream idxout) throws Exception{
        ByteArrayOutputStream tempbout = new ByteArrayOutputStream();
        LEDataOutputStream templout = new LEDataOutputStream(tempbout);
        
        // calculate the bounding box
        if (myRecords.length > 0){
            EnvelopeBuffer tempEnvelopeBuffer = new EnvelopeBuffer();
            for (int i=0; i<myRecords.length; i++){
                Shape tempShape = myRecords[i].getShape();
                if (tempShape != null){
                    tempEnvelopeBuffer.expandToInclude(tempShape.getEnvelope());
                }
            }
            
            myHeader.myXmax = tempEnvelopeBuffer.getMaxX();
            myHeader.myXmin = tempEnvelopeBuffer.getMinX();
            myHeader.myYmax = tempEnvelopeBuffer.getMaxY();
            myHeader.myYmin = tempEnvelopeBuffer.getMinY();
        }
        
        // write the records.
        int tempTotalLength = myHeader.getHeaderLength();
        
        // write the index header.
        myHeader.myFileLength = myHeader.getHeaderLength() + 4*myRecords.length;
        writeHeader(idxout);
        
        // write the shapes.
        for (int i=0; i<myRecords.length; i++){
            
            // create a very temporary byte array output stream to write things to.
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            LEDataOutputStream lout = new LEDataOutputStream(bout);
            
            // retrieve the length
            int tempRecordLength = writeShape(myRecords[i].getShape(), lout);
            
            // write the length and the shape to the byte array out put stream.
            templout.setLittleEndianMode(false);
            templout.writeInt(i+1);	// index
            templout.setLittleEndianMode(false);
            templout.writeInt(tempRecordLength); //record length
            templout.flush();
            templout.write(bout.toByteArray());
            
            // write the offset and contents to the index file
            idxout.setLittleEndianMode(false);
            idxout.writeInt(tempTotalLength);
            idxout.writeInt(tempRecordLength);
            
            // increment the total length, the 4 is for the index, and record length above
            tempTotalLength = tempTotalLength +4+ tempRecordLength;
        }
        
        // write the header information
        myHeader.myFileLength = tempTotalLength;
        writeHeader(out);
        
        
        // write the shape information
        templout.flush();
        out.write(tempbout.toByteArray());
    }
    /**
     * Writes the shape to the shape file.
     * Returns the number of 16 bit words written.
     */
    private int writeShape(Shape inShape, LEDataOutputStream out)throws IOException{
        
        // 0 Null Shape
        if (inShape == null) return writeNull(out);
        
        //11 PointZ
        if (inShape instanceof PointZ) return writePointZ((PointZ) inShape, out);
        
        //21 PointM
        if (inShape instanceof PointM) return writePointM((PointM) inShape, out);
        
        // 1 Point
        if (inShape instanceof Point) return writePointType((Point) inShape, out);
        
        // 3 PolyLine
        // 23 PolyLineM
        // 13 PolyLineZ
        if (inShape instanceof LineString) return writePolyLine((LineString) inShape, out);
        if (inShape instanceof MultiLineString) return writePolyLine((MultiLineString) inShape, out);
        
        //5 Polygon
        //25 PolygonM
        //15 PolygonZ
        if (inShape instanceof Polygon) return writePolygon((Polygon) inShape, out);
        if (inShape instanceof MultiPolygon) return writePolygon((MultiPolygon) inShape, out);
        
        // 8 MultiPoint
        //28 MultiPointM
        //18 MultiPointZ
        if (inShape instanceof MultiPoint) return writeMultiPoint((MultiPoint) inShape, out);
        
        //31 MultiPatch
        
        return 0;
    }
    
    /** Returns the numerical type for the given shape. */
    public static int getShapeType(Shape inShape) throws Exception{
        // Z coordinate shapes.
        if (inShape instanceof PointZ) return SHAPE_POINTZ;
        if (inShape instanceof MultiPointZ) return SHAPE_MULTIPOINTZ;
        if (inShape instanceof LineStringZ) return SHAPE_POLYLINEZ;
        if (inShape instanceof MultiLineStringZ) return SHAPE_POLYLINEZ;
        if (inShape instanceof PolygonZ) return SHAPE_POLYGONZ;
        if (inShape instanceof MultiPolygonZ) return SHAPE_POLYGONZ;
        
        // measuer coordinate shapes
        if (inShape instanceof PointM) return SHAPE_POINTM;
        if (inShape instanceof MultiPointM) return SHAPE_MULTIPOINTM;
        if (inShape instanceof LineStringM) return SHAPE_POLYLINEM;
        if (inShape instanceof MultiLineStringM) return SHAPE_POLYLINEM;
        if (inShape instanceof PolygonM) return SHAPE_POLYGONM;
        if (inShape instanceof MultiPolygonM) return SHAPE_POLYGONM;
        
        // standard coordinates.
        if (inShape instanceof Point) return SHAPE_POINT;
        if (inShape instanceof MultiPoint) return SHAPE_MULTIPOINT;
        if (inShape instanceof LineString) return SHAPE_POLYLINE;
        if (inShape instanceof MultiLineString) return SHAPE_POLYLINE;
        if (inShape instanceof Polygon) return SHAPE_POLYGON;
        if (inShape instanceof MultiPolygon) return SHAPE_POLYGON;
        
        throw new Exception("Unknown Shape Type "+inShape);
    }
    
    /** Returns a string representing the given shape type. */
    public static String getShapeName(int inShapeType){
        // Z coordinate shapes.
        if (inShapeType == SHAPE_POINTZ) return "PointZ";
        if (inShapeType == SHAPE_MULTIPOINTZ) return "MultiPointZ";
        if (inShapeType == SHAPE_POLYLINEZ) return "LineZ";
        if (inShapeType == SHAPE_POLYGONZ) return "PolygonZ";
        
        // measuer coordinate shapes
        if (inShapeType == SHAPE_POINTM) return "PointM";
        if (inShapeType == SHAPE_MULTIPOINTM) return "MultiPointM";
        if (inShapeType == SHAPE_POLYLINEM) return "LineM";
        if (inShapeType == SHAPE_POLYGONM) return "PolygonM";
        
        // standard coordinates.
        if (inShapeType == SHAPE_POINT) return "Point";
        if (inShapeType == SHAPE_MULTIPOINT) return "MultiPoint";
        if (inShapeType == SHAPE_POLYLINE) return "Line";
        if (inShapeType == SHAPE_POLYGON) return "Polygon";
        
        return "unknown";
    }
    
    /** returns the dbf file type for the attribute sent in. */
    public static char getFieldType(AttributeType inAttributeType){
        if (inAttributeType.getType() == AttributeType.BOOLEAN){
            return 'L';
        }
        if (inAttributeType.getType() == AttributeType.FLOAT){
            return 'N';
        }
        if (inAttributeType.getType() == AttributeType.INTEGER){
            return 'N';
        }
        if (inAttributeType.getType() == AttributeType.TIMESTAMP){
            return 'D';
        }
        return 'C';
    }
    
}
