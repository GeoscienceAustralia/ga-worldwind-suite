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

/**
 * This is a convenience class used by ShapeFileReader and ShapeFileWriter
 * that captures methods used by both.
 * @author head
 */
public abstract class ShapeFileStream {
    // the name of the shapefile and associated files/extensions being read
    protected String myFilename = null;
    protected String myShpExt = null;
    protected String myShxExt = null;
    protected String myDbfExt = null;
    protected String myGzipExt = null;

   // header
    protected ShapeFileHeader myHeader = null;
    // this will be a Reader or Writer.  The reason we keep this in the base
    // class is so we can use it in the utility function equiv
    protected DbaseFileStream myDbaseStream = null;

    protected ShapeFileStream(String inFilename)
    {
        setFile(inFilename);
    }

    /**
     * Set the file name for this shape file.
     * The case-sensitive logic is needed for filesystems where
     * case is relevant.
     */
    protected void setFile(String inFilename) 
    {
                
        // initialize in case bogus name is sent in
        myFilename = null;
        myShpExt = null;
        myShxExt = null;
        myDbfExt = null;
        myGzipExt = null;
        
        // parse the filename and figure out the components
        if (inFilename != null){
            if ((inFilename.endsWith(".SHP")) ||
                (inFilename.endsWith(".SHX")) ||
                (inFilename.endsWith(".DBF"))) {
                myFilename = inFilename.substring(0, inFilename.length()-4);
                myShpExt = ".SHP";
                myShxExt = ".SHX";
                myDbfExt = ".DBF";
                myGzipExt = null;
            }
            else if ((inFilename.endsWith(".shp")) ||
                     (inFilename.endsWith(".shx")) ||
                     (inFilename.endsWith(".dbf"))) {
                myFilename = inFilename.substring(0, inFilename.length()-4);
                myShpExt = ".shp";
                myShxExt = ".shx";
                myDbfExt = ".dbf";
                myGzipExt = null;
            }
            else if (inFilename.endsWith(".GZ")) {
                // call self recursively to pick up the SHP/shp extension
                setFile(inFilename.substring(0, inFilename.length() - 3));
                myGzipExt = ".GZ";
            }
            else if (inFilename.endsWith(".gz")) {
                // call self recursively to pick up the SHP/shp extension
                setFile(inFilename.substring(0, inFilename.length() - 3));
                myGzipExt = ".gz";
            }
            else {
               // extension not specified
               myFilename = inFilename;
               myShpExt = ".shp";
               myShxExt = ".shx";
               myDbfExt = ".dbf";
               myGzipExt = null;
            }
        }
    }

    /**
     * checks readability of the specified file
     */
    private final boolean isReadable(String inFileName) {
        File inFile = new File(inFileName);
        return inFile.canRead();
    }

    /**
     * Return the shapefile header associated with this reader
     */
    public final ShapeFileHeader getHeader() 
    {
        return myHeader;
    }

    /**
     * Returns equivalency between this "stream" and another.   Equivalency
     * means that the headers of the files being read (or written) are
     * consistent (same types of shapes and attributes in each).
     */
    public boolean equiv(ShapeFileStream that) 
    {
        return (this.myHeader != null &&
                that.myHeader != null &&
                this.myDbaseStream != null &&
                that.myDbaseStream != null &&
                this.myHeader.myShapeType == that.myHeader.myShapeType &&
                this.myDbaseStream.equiv(that.myDbaseStream));
    }
}
