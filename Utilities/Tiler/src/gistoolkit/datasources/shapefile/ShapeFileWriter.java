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
import gistoolkit.features.*;
import gistoolkit.features.featureutils.*;
import cmp.LEDataStream.*;

/**
 * A ShapeFileReader is used to write shapes out to a shapefile one at a
 * time.
 * @author head
 */
public class ShapeFileWriter extends ShapeFileStream {
    // the OutputStreams being written
    protected LEDataOutputStream myShpStream = null;
    protected LEDataOutputStream myShxStream = null;

    // index and word position holders
    private int myWriteIndex = 0;
    private int myWritePosition = 0;

    // extent of the written shapes
    private EnvelopeBuffer myEnvelopeBuffer = null;

    public ShapeFileWriter(String filename)
       throws FileNotFoundException, IOException 
    {
        super(filename);

         // open the files for reading (don't write to gzip files because we
         // need to go back and modify the headers later... perhaps we could
         // compress it afterwards)
        myShpStream =
            new LEDataOutputStream(new FileOutputStream(myFilename+myShpExt));
        myShxStream =
            new LEDataOutputStream(new FileOutputStream(myFilename+myShxExt));
        myDbaseStream = new DbaseFileWriter(myFilename + myDbfExt);

        // open the file and read the header

        // create a bogus header and write it out
        myEnvelopeBuffer = new EnvelopeBuffer();
        myHeader = new ShapeFileHeader();
        writeHeader(myShpStream);
        writeHeader(myShxStream);
   }

    protected void finalize() throws IOException
    {
        // close the stream in case it wasn't done already
        if (myShpStream != null) {
            close();
        }
    }
    
    /**
     * Closes the files for writing and re-writes the header with the
     * correct number of records that were written.
     */
    public void close()
        throws IOException
    {
        // close the current output streams so we can re-write headers
        myShpStream.close();
        myShxStream.close();

        // setting the streams to null is an indicator that close() has been called
        myShpStream = null;
        myShxStream = null;

        // write the corrected header to a byte array stream
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        LEDataOutputStream lout = new LEDataOutputStream(bout);
        myHeader.myFileLength = myWritePosition;
        writeHeader(lout);
        
        // open a RandomAccessFile on the shapefile and write the new header
        RandomAccessFile shpFile = new RandomAccessFile(myFilename + myShpExt,
                                                        "rw");
        shpFile.write(bout.toByteArray());
        shpFile.close();

        // the index file has the same header except for the file length
        myHeader.myFileLength = (50 + 4*myWriteIndex);
        bout.reset();
        writeHeader(lout);
        
        // open a RandomAccessFile on the index file and write the new header
        RandomAccessFile shxFile = new RandomAccessFile(myFilename + myShxExt,
                                                        "rw");
        shxFile.write(bout.toByteArray());
        shxFile.close();

        // close the dbase reader too so it can also re-write headers and such
        ((DbaseFileWriter)myDbaseStream).close();
    }
    

    /**
     * Append the shape to the output files
     */
    public void write(Record rec)
        throws IOException
    {
        // the shape goes in the shp file and the attributes in the dbf
        write(rec.getShape());
        ((DbaseFileWriter)myDbaseStream).write(rec);
    }

    /**
     * Writes the shape to the shape file.
     * Returns the number of 16 bit words written.
     */
    private final void write(Shape inShape)
        throws IOException
    {
        int numWords = 0;

        // create a temporary storage for the shape output
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        LEDataOutputStream lout = new LEDataOutputStream(bout);

        if (inShape == null) {
            numWords =  writeNull(lout);
        }
        
        if (inShape instanceof PointZ) {
            numWords =  writePointZ((PointZ) inShape, lout);
            myHeader.myShapeType = ShapeFile.SHAPE_POINTZ;
        }
        
        if (inShape instanceof PointM) {
            numWords =  writePointM((PointM) inShape, lout);
            myHeader.myShapeType = ShapeFile.SHAPE_POINTM;
        }
        
        if (inShape instanceof Point) {
            numWords =  writePointType((Point) inShape, lout);
            myHeader.myShapeType = ShapeFile.SHAPE_POINT;
        }
        
        if (inShape instanceof LineString) {
            numWords =  writePolyLine((LineString) inShape, lout);
            myHeader.myShapeType = ShapeFile.SHAPE_POLYLINE;
        }
        
        if (inShape instanceof MultiLineString) {
            numWords =  writePolyLine((MultiLineString) inShape, lout);
            myHeader.myShapeType = ShapeFile.SHAPE_POLYLINE;
        }

        if (inShape instanceof Polygon) {
            numWords =  writePolygon((Polygon) inShape, lout);
            myHeader.myShapeType = ShapeFile.SHAPE_POLYGON;
        }
        
        if (inShape instanceof MultiPolygon) {
            numWords =  writePolygon((MultiPolygon) inShape, lout);
            myHeader.myShapeType = ShapeFile.SHAPE_POLYGON;
        }
        
        if (inShape instanceof MultiPoint) {
            numWords =  writeMultiPoint((MultiPoint) inShape, lout);
            myHeader.myShapeType = ShapeFile.SHAPE_MULTIPOINT;
        }
        
        if (numWords > 0) {
            // write the length and the shape to the byte array out put stream.
            myShpStream.setLittleEndianMode(false);
            myShpStream.writeInt(++myWriteIndex);   // record index
            myShpStream.setLittleEndianMode(false);
            myShpStream.writeInt(numWords);         // record length
            myShpStream.flush();
            myShpStream.write(bout.toByteArray());

            // write the offset and contents to the index file
            myShxStream.setLittleEndianMode(false);
            myShxStream.writeInt(myWritePosition);
            myShxStream.writeInt(numWords);

            // point to the next position
            // the 4 is for the index, and record length above
            myWritePosition += (4 + numWords);

            myEnvelopeBuffer.expandToInclude(inShape.getEnvelope());                
        }
    }

    /**
     * Writes the Envelope to the shape file.
     * returns the number of 16 bit words written
     */
    private final int writeEnvelope(Envelope inEnvelope, LEDataOutputStream out)
        throws IOException
    {
        // read the Coordinates
        out.setLittleEndianMode(true);
        if (inEnvelope != null) {
            out.writeDouble(inEnvelope.getMinX());   //min X
            out.writeDouble(inEnvelope.getMinY());   //min Y
            out.writeDouble(inEnvelope.getMaxX());   //max X
            out.writeDouble(inEnvelope.getMaxY());   //max Y
        }
        else {
            out.writeDouble(0.0);
            out.writeDouble(0.0);
            out.writeDouble(0.0);
            out.writeDouble(0.0);
        }
        
        // return the number of 16 bit words written
        return 16;
    }

    /**
     * Write the header to the shape file.
     */
    private final void writeHeader(LEDataOutputStream out)
        throws IOException
    {
        if (myHeader != null) {
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
            
            // From 36 to 68 are the X-Y envelope
            writeEnvelope(myEnvelopeBuffer.getEnvelope(), out);
            
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

            // the write position is in 2-byte words
            myWritePosition = 50;
        }
    }

    /**
     * Writes the MultiPoint to the shape file.
     * returns the number of 16 bit words written.
     */
    private final int writeMultiPoint(MultiPointM inMultiPointM,
                                LEDataOutputStream out)
        throws IOException
    {
        
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
    private final int writeMultiPoint(MultiPointZ inMultiPointZ,
                                LEDataOutputStream out)
        throws IOException
    {
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
    private final int writeMultiPoint(MultiPoint inMultiPoint,
                                LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writeNull(LEDataOutputStream out)
        throws IOException
    {        
        // write the type
        out.setLittleEndianMode(true);
        out.writeInt(0);
        
        return 2;
    }
    
    /**
     * writes the point to the output stream returns the length written.
     */
    private final int writePoint(Point inPoint, LEDataOutputStream out)
        throws IOException
    {
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
    private final int writePointM(PointM inPoint, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePointType(Point inPoint, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePointZ(PointZ inPoint, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePolygon(MultiPolygonM inMultiPolygon,
                             LEDataOutputStream out)
        throws IOException
    {
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
    private final int writePolygon(MultiPolygonZ inMultiPolygon,
                             LEDataOutputStream out)
        throws IOException{
        
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
    private final int writePolygon(PolygonM inPolygon, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePolygon(PolygonZ inPolygon, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePolygon(MultiPolygon inMultiPolygon, LEDataOutputStream out)
        throws IOException
    {        
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
            LinearRing tempRing = tempPolygons[j].getPosativeRing();
            if (!tempRing.isClockwise()) tempRing.reorder();
            tempParts.addElement(tempRing);
            LinearRing[] tempHoles = tempPolygons[j].getHoles();
            for (int i=0; i<tempHoles.length; i++){
                if (tempHoles[i].isClockwise()) tempHoles[i].reorder();
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
    private final int writePolygon(Polygon inPolygon, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePolyLine(LineStringM inLineString, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePolyLine(LineStringZ inLineString, LEDataOutputStream out) 
        throws IOException
    {        
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
    private final int writePolyLine(MultiLineStringM inMultiLineStringM, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePolyLine(MultiLineStringZ inMultiLineStringZ, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePolyLine(LineString inLineString, LEDataOutputStream out)
        throws IOException
    {        
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
    private final int writePolyLine(MultiLineString inMultiLineString, LEDataOutputStream out)
        throws IOException
    {        
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

}
