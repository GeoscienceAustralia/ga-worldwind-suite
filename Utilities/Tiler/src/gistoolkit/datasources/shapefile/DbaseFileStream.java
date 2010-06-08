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
import gistoolkit.features.AttributeType;

/**
 * This is a convenience class used by DbaseFileReader and DbaseFileWriter
 * that captures methods used by both.
 * @author head
 */
abstract class DbaseFileStream {
    // the name of the shapefile and associated files/extensions being read
    protected String myFilename = null;
    protected String myDbfExt = null;
    protected String myGzipExt = null;

   // header
    protected DbaseFileHeader myHeader = null;

    // Convenient place to store the field names and types.
    protected String[] myFieldNames = null;
    protected AttributeType[] myFieldTypes = null;

    protected DbaseFileStream(String inFilename)
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
        myDbfExt = null;
        myGzipExt = null;
        
        // parse the filename and figure out the components
        if (inFilename != null){
            if ((inFilename.endsWith(".DBF"))) {
                myFilename = inFilename.substring(0, inFilename.length()-4);
                myDbfExt = ".DBF";
                myGzipExt = null;
            }
            else if ((inFilename.endsWith(".dbf"))) {
                myFilename = inFilename.substring(0, inFilename.length()-4);
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
               myDbfExt = ".dbf";
               myGzipExt = null;
            }
        }
    }

    /**
     * checks readability of the specified file
     */
    private boolean isReadable(String inFileName) {
        File inFile = new File(inFileName);
        return inFile.canRead();
    }

    /**
     * Return the shapefile header associated with this reader
     */
    public DbaseFileHeader getHeader() 
    {
        return myHeader;
    }

    /**
     * Returns the field names for the dbase file being read.
     */
    public String[] getFieldNames() 
    {
        if (myFieldNames == null) {
            // save off the field names
            myFieldNames = new String[myHeader.getNumFields()];
            for (int i=0; i<myHeader.getNumFields(); i++) {
                myFieldNames[i] = myHeader.getFieldName(i).trim();
            }
        }

        return myFieldNames;
    }

    /**
     * Returns the field names for the dbase file being read.
     */
    public AttributeType[] getFieldTypes() 
    {
        if (myFieldTypes == null) {
            // save off the field types
            myFieldTypes = new AttributeType[myHeader.getNumFields()];

            for (int i = 0; i < myHeader.getNumFields(); i++) {
                int len = myHeader.getFieldLength(i);
                int dec = myHeader.getFieldDecimalCount(i);
                // create the appropriate type based on the dbase type
                switch (myHeader.getFieldType(i)) {
                case 'L':
                    myFieldTypes[i] =
                        new AttributeType(AttributeType.BOOLEAN, len, dec);
                    break;
                case 'C':
                    myFieldTypes[i] =
                        new AttributeType(AttributeType.STRING, len, dec);
                    break;
                case 'D':
                    myFieldTypes[i] =
                        new AttributeType(AttributeType.TIMESTAMP, len, dec);
                    break;
                case 'N':
                    myFieldTypes[i] =
                        new AttributeType(AttributeType.FLOAT, len, dec);
                    break;
                case 'F':
                    // the -1 decimal length indicates that this is a 'F'
                    // field when writing the record in DbaseFileWriter
                    myFieldTypes[i] =
                        new AttributeType(AttributeType.FLOAT, len, -1);
                    break;
                default:
                    myFieldTypes[i] = null;
                    break;
                }
            }
        }

        return myFieldTypes;
    }

    /**
     * Returns equivalency between this reader and another.   Equivalency
     * means that the headers of the files being read are consistent (same
     * types of shapes and attributes in each).
     */
    public boolean equiv(DbaseFileStream that) 
    {
        // check that we can do the comparison
        if (that == null ||
            this.myHeader == null ||
            that.myHeader == null) {
            System.err.println("Failed dbase equivalency: NULL file or header");
            return false;
        }
        // make sure the number of fields is the same
        if (this.myHeader.getNumFields() != that.myHeader.getNumFields()) {
            System.err.println("Failed dbase equivalence: different number of fields");
            return false;
        }
        
        // loop over each attribute to see if they're all the same
        for (int i = 0; i < myHeader.getNumFields(); i++) {

            if (!this.getFieldNames()[i].
                equals(that.getFieldNames()[i])) {
                System.err.println("Failed DBF equivalence when comparing field names");
                System.err.println("Field named" + getFieldNames()[i] +
                                   " does not match field named " +
                                   that.getFieldNames()[i]);
                return false;
            }

            if (this.myHeader.getFieldType(i) != that.myHeader.getFieldType(i)) {
                System.err.println("Failed DBF equivalence when comparing field types");
                System.err.println("Field " + getFieldNames()[i] +
                                   " has types of " +
                                   this.getFieldTypes()[i].getType() + " and " +
                                   that.getFieldTypes()[i].getType());
                return false;
            }

            if (this.myHeader.getFieldLength(i) != 
                that.myHeader.getFieldLength(i)) {
                System.err.println("Failed DBF equivalence when comparing field lengths");
                System.err.println("Field " + getFieldNames()[i] +
                                   " has lengths of " +
                                   this.myHeader.getFieldLength(i) + " and " +
                                   that.myHeader.getFieldLength(i));
                return false;
            }

            if (this.myHeader.getFieldDecimalCount(i) != 
                that.myHeader.getFieldDecimalCount(i)) {
                System.err.println("Failed DBF equivalence when comparing field decimal count");
                System.err.println("Field " + getFieldNames()[i] +
                                   " has decimal counts of " +
                                   this.myHeader.getFieldDecimalCount(i) + " and " +
                                   that.myHeader.getFieldDecimalCount(i));
                return false;
            }
        }

        // if we got this far, they must all be the same
        return true;
    }
}
