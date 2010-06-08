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
import cmp.LEDataStream.*;

/**
 * Class to represent the header in the shape file.
 */
public class ShapeFileHeader{
    
    /**File Code, must be the value 9994*/
    int myFileCode = 9994;
    /** Return the file code. */
    public int getFileCode(){return myFileCode;}
    
    /**
     * Unused 1;
     */
    int myUnused1 = 0;
    
    /**
     * Unused 2;
     */
    int myUnused2 = 0;
    
    /**
     * Unused 3;
     */
    int myUnused3 = 0;
    
    /**
     * Unused 4;
     */
    int myUnused4 = 0;
    
    /**
     * Unused 5;
     */
    int myUnused5 = 0;
    
    /**File Length;*/
    int myFileLength = 0;
    
    /**Version of the file.*/
    int myVersion = 1000;
    /** Return the version of the file. */
    public int getVersion(){return myVersion;}
    
    /**
     * Shape Type
     * Value Shape Type
     * 0 Null Shape
     * 1 Point
     * 3 PolyLine
     * 5 Polygon
     * 8 MultiPoint
     * 11 PointZ
     * 13 PolyLineZ
     * 15 PolygonZ
     * 18 MultiPointZ
     * 21 PointM
     * 23 PolyLineM
     * 25 PolygonM
     * 28 MultiPointM
     * 31 MultiPatch
     */
    /* The null shape type, there is no shape for this record. */
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
    
    int myShapeType = 0;
    
    /**
     * BoundingBox Xmin
     */
    double myXmin = 0;
    
    /**
     * BoundingBox Ymin
     */
    double myYmin = 0;
    
    /**
     * BoundingBox Xmax
     */
    double myXmax = 0;
    
    /**
     * BoundingBox Ymax
     */
    double myYmax = 0;
    
    /** Get the envelope of the shape file. */
    public gistoolkit.features.Envelope getFileEnvelope(){return new gistoolkit.features.Envelope(myXmin, myYmin, myXmax, myYmax);}
    
    /**
     * BoundingBox Zmin
     */
    double myZmin = 0;
    
    /**
     * BoundingBox Zmax
     */
    double myZmax = 0;
    
    /**
     * BoundingBox Zmin
     */
    double myMmin = 0;
    
    /**
     * BoundingBox Zmax
     */
    double myMmax = 0;
    
    // notify about warnings.
    private boolean myWarning = true;
    /** Print warnings to system.out. */
    public void setWarnings(boolean inWarning){myWarning = inWarning;}
    
    /**
     * ShapeFileHeader constructor comment.
     */
    public ShapeFileHeader() {
        super();
    }
    /**
     * Return the length of the header in 16 bit words..
     */
    public int getHeaderLength(){
        return 50;
    }
    
    /**
     * Return the number of 16 bit words in the shape file as recorded in the header
     */
    public int getFileLength() {
        return myFileLength;
    }
    
    
    /**
     * Read the header from the shape file.
     */
    public void readHeader(LEDataInputStream in) throws IOException{
        
        // the first four bytes are integers
        in.setLittleEndianMode(false);
        myFileCode = in.readInt();
        if (myFileCode != 9994) warn("File Code = "+myFileCode+" Not equal to 9994");
        
        // From 4 to 8 are unused.
        in.setLittleEndianMode(false);
        myUnused1 = in.readInt();
        
        // From 8 to 12 are unused.
        in.setLittleEndianMode(false);
        myUnused2 = in.readInt();
        
        // From 12 to 16 are unused.
        in.setLittleEndianMode(false);
        myUnused3 = in.readInt();
        
        // From 16 to 20 are unused.
        in.setLittleEndianMode(false);
        myUnused4 = in.readInt();
        
        // From 20 to 24 are unused.
        in.setLittleEndianMode(false);
        myUnused5 = in.readInt();
        
        // From 24 to 28 are the file length.
        in.setLittleEndianMode(false);
        myFileLength = in.readInt();
        
        // From 28 to 32 are the File Version.
        in.setLittleEndianMode(true);
        myVersion = in.readInt();
        
        // From 32 to 36 are the Shape Type.
        in.setLittleEndianMode(true);
        myShapeType = in.readInt();
        
        // From 36 to 44 are Xmin.
        in.setLittleEndianMode(true);
        myXmin = in.readDouble();
        
        // From 44 to 52 are Ymin.
        in.setLittleEndianMode(true);
        myYmin = in.readDouble();
        
        // From 52 to 60 are Xmax.
        in.setLittleEndianMode(true);
        myXmax = in.readDouble();
        
        // From 60 to 68 are Ymax.
        in.setLittleEndianMode(true);
        myYmax = in.readDouble();
        
        // From 68 to 76 are Zmin.
        in.setLittleEndianMode(true);
        myZmin = in.readDouble();
        
        // From 76 to 84 are Zmax.
        in.setLittleEndianMode(true);
        myZmax = in.readDouble();
        
        // From 84 to 92 are Mmin.
        in.setLittleEndianMode(true);
        myMmin = in.readDouble();
        
        // From 92 to 100 are Mmax.
        in.setLittleEndianMode(true);
        myMmax = in.readDouble();
        
        // that is all 100 bytes of the header.
    }
    private void warn(String inWarn){
        if (myWarning){
            System.out.print("WARNING: ");
            System.out.println(inWarn);
        }
    }
    
}
