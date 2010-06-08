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
 * A DbaseFileReader is used to read a dbase III format file.
 * @author head
 */
public class DbaseFileReader extends DbaseFileStream {
    // the InputStreams being read
    protected LEDataInputStream myDbfStream = null;
    
    private int myReadPosition = 0;
    
    /**
     * Create a reader on the named file
     */
    public DbaseFileReader(String inFilename)
    throws FileNotFoundException, IOException {
        super(inFilename);
        
        InputStream fin = null;
        // check to see if it is a compressed file
        if (myGzipExt != null) {
            FileInputStream fileIn =
            new FileInputStream(myFilename+myDbfExt+myGzipExt);
            fin = (InputStream) new GZIPInputStream(fileIn);
        }
        else {
            fin = (InputStream) new FileInputStream(myFilename+myDbfExt);
        }
        myDbfStream = new LEDataInputStream(fin);
        
        // open the file and read the header
        readHeader();
    }
    
    /**
     * Reads the dbase header record
     */
    protected void readHeader() {
        // create the header
        myHeader = new DbaseFileHeader();
        
        try {
            // read the header
            myHeader.readHeader(myDbfStream);
            
            // reset field variables in case they need to be recalculated
            myFieldNames = null;
            myFieldTypes = null;
        }
        catch (Exception ioe) {
            System.err.println("Couldn't read dbase header for " +
            myFilename + ": " + ioe);
        }
        
        // how many records remain
        myReadPosition = myHeader.getHeaderLength();
    }
    
    /** Skip the next record. */
    public void skip() throws IOException{
        boolean foundRecord = false;
        while (!foundRecord) {
            // retrieve the record length
            int tempRecordLength = myHeader.getRecordLength();

            // read the deleted flag
            char tempDeleted = (char) myDbfStream.readByte();

            // skip the next bytes
            myDbfStream.skipBytes(tempRecordLength -1); //the 1 is for the deleted flag just read.
            
            // add the row if it is not deleted.
            if (tempDeleted != '*'){
                foundRecord = true;
            }
        }
    }
    /**
     * Read a single dbase record
     * @return the read shapefile record or null if there are no more records
     */
    public Record read() throws IOException {
        Object[] attrs = null;
        
        boolean foundRecord = false;
        while (!foundRecord) {
            
            // retrieve the record length
            int tempNumFields = myHeader.getNumFields();
            
            // storage for the actual values
            attrs = new Object[tempNumFields];
            
            // read the deleted flag
            char tempDeleted = (char) myDbfStream.readByte();
            
            // read the record length
            int tempRecordLength = 1; // for the deleted character just read.
            
            // read the Fields
            for (int j=0; j<tempNumFields; j++){
                
                // find the length of the field.
                int tempFieldLength = myHeader.getFieldLength(j);
                tempRecordLength = tempRecordLength + tempFieldLength;
                
                // find the field type
                char tempFieldType = myHeader.getFieldType(j);
                
                // read the data.
                Object tempObject = null;
                switch (tempFieldType){
                    case 'L': // logical data type, one character (T,t,F,f,Y,y,N,n)
                        char tempChar = (char) myDbfStream.readByte();
                        if ((tempChar == 'T') || (tempChar == 't') || (tempChar == 'Y') || (tempChar == 'y')){
                            tempObject = new Boolean(true);
                        }
                        else {
                            tempObject = new Boolean(false);
                        }
                        break;
                    case 'C': // character record.
                        byte[] sbuffer = new byte[tempFieldLength];
                        myDbfStream.readFully(sbuffer);
                        // use an encoding to ensure all 8 bits are loaded
                        tempObject = new String(sbuffer, "ISO-8859-1").trim();
                        break;
                        
                    case 'D': // date data type.
                        byte[] dbuffer = new byte[8];
                        myDbfStream.readFully(dbuffer);
                        try{
                            String tempString = new String(dbuffer, 0, 4);
                            int tempYear = Integer.parseInt(tempString);
                            tempString = new String(dbuffer, 4, 2);
                            int tempMonth = Integer.parseInt(tempString) - 1;
                            tempString = new String(dbuffer, 6, 2);
                            int tempDay = Integer.parseInt(tempString);
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.YEAR,tempYear);
                            c.set(Calendar.MONTH, tempMonth);
                            c.set(Calendar.DAY_OF_MONTH, tempDay);
                            tempObject = c.getTime();
                        }
                        catch (NumberFormatException e){
                            tempObject = null;
                        }
                        break;
                        
                    case 'N': // number
                    case 'F': // floating point number
                        byte[] fbuffer = new byte[tempFieldLength];
                        myDbfStream.readFully(fbuffer);
                        String tempString = new String(fbuffer);
                        try {
                            tempObject = Double.valueOf(tempString.trim());
                        }
                        catch (NumberFormatException nfe) {
                            // if we can't format the number, just save it as
                            // a string
                            tempObject = tempString;
                        }
                        break;
                        
                    default:
                        System.err.println("Do not know how to parse Field type "+tempFieldType);
                }
                attrs[j] = tempObject;
            }
            
            // ensure that the full record has been read.
            if (tempRecordLength < myHeader.getRecordLength()){
                byte[] tempbuff = new byte[myHeader.getRecordLength()-tempRecordLength];
                myDbfStream.readFully(tempbuff);
            }
            
            // add the row if it is not deleted.
            if (tempDeleted != '*'){
                foundRecord = true;
            }
        }
        
        // create the record that will be returned
        Record retRecord = new Record();
        retRecord.setAttributes(attrs);
        
        // set the attribute types and lengths
        retRecord.setAttributeNames(getFieldNames());
        retRecord.setAttributeTypes(getFieldTypes());
        
        return retRecord;
    }
}
