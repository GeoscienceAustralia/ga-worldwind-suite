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
import cmp.LEDataStream.*;

/**
 * A DbaseFileReader is used to write dbase III records out one at a
 * time.
 * @author head
 */
public class DbaseFileWriter extends DbaseFileStream {
    // the OutputStreams being written
    protected LEDataOutputStream myDbfStream = null;
    
    // index and word position holders
    private int myNumRecords = 0;
    
    /** Indicates if the header has been written yet. */
    private boolean myHeaderWritten = false;
    
    // DbaseFileHeader is temporary... need way to specify field info
    public DbaseFileWriter(String filename)throws FileNotFoundException, IOException {
        super(filename);
        
        // open the file for writing (don't write to gzip files because we
        // need to go back and modify the headers later... perhaps we could
        // compress it afterwards)
        myDbfStream = new LEDataOutputStream(new FileOutputStream(myFilename+myDbfExt));
    }
    // DbaseFileHeader is temporary... need way to specify field info
    public DbaseFileWriter(String filename, DbaseFileHeader inHeader)throws FileNotFoundException, IOException {
        super(filename);
        
        // open the file for writing (don't write to gzip files because we
        // need to go back and modify the headers later... perhaps we could
        // compress it afterwards)
        myHeader = inHeader;
        myDbfStream = new LEDataOutputStream(new FileOutputStream(myFilename+myDbfExt));
    }
    
    protected void finalize() throws IOException {
        // close the stream in case it wasn't done already
        if (myDbfStream != null) {
            close();
        }
    }
    
    public void close()
    throws IOException {
        // close the current output streams so we can re-write headers
        myDbfStream.close();
        
        // setting the streams to null is an indicator that close() has been called
        myDbfStream = null;
        
        // write the corrected header to a byte array stream
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        LEDataOutputStream lout = new LEDataOutputStream(bout);
        myHeader.setNumRecords(myNumRecords);
        try {
            myHeader.writeHeader(lout);
        }
        catch (Exception e) {
            throw new IOException(e.toString());
        }
        
        // open a RandomAccessFile on the dbf and write the updated header
        RandomAccessFile dbfFile =
        new RandomAccessFile(myFilename + myDbfExt, "rw");
        dbfFile.write(bout.toByteArray());
        dbfFile.close();
    }
    
    /**
     * Appends the passed record to the dbase file.
     */
    public void write(Record inRec) throws IOException {
        if (myHeader == null) {
            // create a header based on the first input record
            // create the header from the attributes of the first record
            myHeader = new DbaseFileHeader();
            
            try {
                // Loop over the record fields and build the header
                for (int i = 0; i < inRec.getAttributes().length; i++) {
                    char type = '\0';
                    if (inRec.getAttributeTypes()[i].getType() == AttributeType.BOOLEAN) {
                        type = 'L';
                    }
                    else if (inRec.getAttributeTypes()[i].getType() == AttributeType.STRING) {
                        type = 'C';
                    }
                    else if (inRec.getAttributeTypes()[i].getType() == AttributeType.TIMESTAMP) {
                        type = 'D';
                    }
                    else if (inRec.getAttributeTypes()[i].getType() == AttributeType.INTEGER) {
                        type = 'N';
                    }
                    else if (inRec.getAttributeTypes()[i].getType() == AttributeType.FLOAT) {
                        // a negative length is used to indicate it's a true float
                        if (inRec.getAttributeTypes()[i].getAuxLength() < 0) {
                            type = 'F';
                        }
                        else {
                            type = 'N';
                        }
                    }
                    
                    myHeader.addColumn(inRec.getAttributeNames()[i],
                    type,
                    inRec.getAttributeTypes()[i].getLength(),
                    inRec.getAttributeTypes()[i].getAuxLength());
                }
            }
            catch (Exception e) {
                throw new IOException(e.toString());
            }
        }
        else {
            // TODO:
            // verify the field attributes are either the same or equivalent
            // if equivalent, re-reference to the input record (chances
            // are that we processing a new file so changing the reference
            // will speed up future comparisons)
            
            //             myHeader = new DbaseFileHeader(inRec);
        }
        
        if (!myHeaderWritten){
            try {
                myHeader.writeHeader(myDbfStream);
                myHeaderWritten = true;
            }
            catch (Exception e) {
                throw new IOException(e.toString());
            }
        }
        
        
        
        int tempRecordLength = myHeader.getRecordLength();
        
        // keep track of how many bytes are written.
        int templength = 0;
        
        // write the deleted value
        myDbfStream.writeByte(' ');
        templength++;
        for (int j=0; j<inRec.getAttributes().length; j++) {
            // field name for reference
            String tempFieldName = myHeader.getFieldName(j);
            
            // find the length of the field.
            int tempFieldLength = myHeader.getFieldLength(j);
            
            // find the field type
            char tempFieldType = myHeader.getFieldType(j);
            
            // write the data.
            int tempBytesWritten = 0;
            switch (tempFieldType){
                case 'L': // logical data type, one character (T,t,F,f,Y,y,N,n)
                    if (inRec.getAttributes()[j] instanceof Boolean){
                        if ( ((Boolean) inRec.getAttributes()[j]).booleanValue() == true){
                            myDbfStream.writeByte('T');
                            tempBytesWritten++;
                        }
                        else{
                            myDbfStream.writeByte('F');
                            tempBytesWritten++;
                        }
                    }
                    else{
                        myDbfStream.writeByte('F');
                        tempBytesWritten++;
                    }
                    templength = templength + 1;
                    break;
                case 'C': // character record.
                {
                    String tempString = "";
                    if (inRec.getAttributes()[j] != null) tempString = inRec.getAttributes()[j].toString();
                    // make sure 8-bit characters are written in the same way they were read
                    byte[] tempBytes = tempString.getBytes("ISO-8859-1");
                    for (int k=0; k<tempFieldLength; k++){
                        if (tempBytes.length > k){
                            myDbfStream.writeByte(tempBytes[k]);
                            tempBytesWritten++;
                        }
                        else{
                            myDbfStream.writeByte(' ');
                            tempBytesWritten++;
                        }
                    }
                    templength = templength + tempFieldLength;
                }
                break;
                case 'D': // date data type.
                    if (inRec.getAttributes()[j] instanceof Date){
                        Calendar c = Calendar.getInstance();
                        c.setTime((Date) inRec.getAttributes()[j]);
                        String tempString = ""+c.get(Calendar.YEAR);
                        for (int k=0; k<4; k++){
                            if (tempString.length()>k){
                                myDbfStream.writeByte(tempString.charAt(k));
                                tempBytesWritten++;
                            }
                            else {
                                myDbfStream.writeByte('0');
                                tempBytesWritten++;
                            }
                        }
                        tempString = ""+(c.get(Calendar.MONTH)+1);
                        if (tempString.length() == 0) {
                            myDbfStream.writeByte('0');
                            myDbfStream.writeByte('0');
                        }
                        if (tempString.length() == 1) {
                            myDbfStream.writeByte('0');
                            myDbfStream.writeByte(tempString.charAt(0));
                        }
                        if (tempString.length() > 1) {
                            myDbfStream.writeByte(tempString.charAt(0));
                            myDbfStream.writeByte(tempString.charAt(1));
                        }
                        tempBytesWritten +=2;
                        tempString = ""+c.get(Calendar.DAY_OF_MONTH);
                        if (tempString.length() == 0) {
                            myDbfStream.writeByte('0');
                            myDbfStream.writeByte('0');
                        }
                        if (tempString.length() == 1) {
                            myDbfStream.writeByte('0');
                            myDbfStream.writeByte(tempString.charAt(0));
                        }
                        if (tempString.length() > 1) {
                            myDbfStream.writeByte(tempString.charAt(0));
                            myDbfStream.writeByte(tempString.charAt(1));
                        }
                        tempBytesWritten +=2;
                    }
                    else{
                        // write 8 blanks
                        for (int k=0; k<8; k++){
                            myDbfStream.writeByte(0);
                            tempBytesWritten++;
                        }
                    }
                    templength = templength + 8;
                    break;
                case 'N': // number
                    // retrieve the decimal position of this column
                    int tempDecimals = myHeader.getFieldDescription(j).myDecimalCount;
                    
                    // if there are no decimal points, then just add the first part.
                    int tempLeadingLength = 0;
                    if (tempDecimals == 0){
                        tempLeadingLength = tempFieldLength;
                    }
                    else{
                        tempLeadingLength = tempFieldLength-tempDecimals-1; // the 1 is for the period or decimal point
                    }
                    
                    if (inRec.getAttributes()[j] instanceof Number) {
                        String tempString = ((Number) inRec.getAttributes()[j]).toString();
                        
                        // find the decimal in the String
                        int tempIndex = tempString.indexOf('.');
                        
                        // if is a decimal in the string then trim it off.
                        String tempWorkingString = tempString;
                        if (tempIndex != -1){
                            tempWorkingString = tempString.substring(0,tempIndex);
                        }
                        
                        // write the String
                        for (int k=0; k<tempLeadingLength; k++){
                            if (tempLeadingLength - k > tempWorkingString.length()){
                                myDbfStream.writeByte('0');
                                tempBytesWritten++;
                            }
                            else{
                                myDbfStream.writeByte(tempWorkingString.charAt(k-(tempLeadingLength-tempWorkingString.length())));
                                tempBytesWritten++;
                            }
                        }
                        
                        
                        // write the trailer.
                        if (tempDecimals >0){
                            myDbfStream.writeByte('.');
                            tempBytesWritten++;
                        }
                        for (int k=0; k<tempDecimals; k++){
                            if (tempIndex == 0){
                                myDbfStream.writeByte('0');
                                tempBytesWritten++;
                            }
                            else{
                                if((tempIndex + k + 1) >= tempString.length()){
                                    myDbfStream.writeByte('0');
                                    tempBytesWritten++;
                                }
                                else{
                                    myDbfStream.writeByte(tempString.charAt(tempIndex+k+1));
                                    tempBytesWritten++;
                                }
                            }
                        }
                    }
                    // write the string value.
                    else {
                        if (inRec.getAttributes()[j] == null){
                            for (int k=0; k<tempFieldLength; k++){
                                myDbfStream.writeByte(0);
                                tempBytesWritten++;
                            }
                        }
                        else{
                            String tempString = inRec.getAttributes()[j].toString();
                            // write the string
                            for (int k=0; k<tempFieldLength; k++){
                                if (k<tempString.length()){
                                    myDbfStream.writeByte(tempString.charAt(k));
                                    tempBytesWritten++;
                                }
                                else{
                                    myDbfStream.writeByte(0);
                                    tempBytesWritten++;
                                }
                            }
                        }
                    }
                    templength = templength + tempFieldLength;
                    break;
                case 'F': // floating point number
                    // The decimal can be in any location for these types of numbers.
                    if (inRec.getAttributes()[j] instanceof Number) {
                        String tempString = ((Number) inRec.getAttributes()[j]).toString();
                        int tempIndex = tempString.indexOf(".");
                        if (tempIndex > tempFieldLength){
                            for (int k=0; k<tempFieldLength; k++){
                                myDbfStream.writeByte('9');
                                tempBytesWritten++;
                            }
                        }
                        else if (tempIndex == -1) {
                            // write the string
                            for (int k=0; k<tempFieldLength; k++){
                                if (k<tempString.length()){
                                    myDbfStream.writeByte(tempString.charAt(k));
                                    tempBytesWritten++;
                                }
                                else{
                                    myDbfStream.writeByte(' ');
                                    tempBytesWritten++;
                                }
                            }
                        }
                    }
                    // write the string value.
                    else {
                        if (inRec.getAttributes()[j] == null){
                            for (int k=0; k<tempFieldLength; k++){
                                myDbfStream.writeByte(0);
                                tempBytesWritten++;
                            }
                        }
                        else{
                            String tempString = inRec.getAttributes()[j].toString();
                            // write the string
                            for (int k=0; k<tempFieldLength; k++){
                                if (k<tempString.length()){
                                    myDbfStream.writeByte(tempString.charAt(k));
                                    tempBytesWritten++;
                                }
                                else{
                                    myDbfStream.writeByte(0);
                                    tempBytesWritten++;
                                }
                            }
                        }
                    }
                    
                default:
                    System.err.println("Do not know how to parse/write field type " + tempFieldType);
            }
        }
        
        // if we have not yet finished writing the record, then finish it off.
        for (int j=0; j<(tempRecordLength-templength); j++){
            myDbfStream.writeByte(0);
        }
        
        // keep track of number for when we re-write the header
        myNumRecords++;
    }
}
