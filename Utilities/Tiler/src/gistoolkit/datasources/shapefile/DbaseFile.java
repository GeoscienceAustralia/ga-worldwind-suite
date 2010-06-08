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

import cmp.LEDataStream.*;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Class to read and write data to a dbase III format file.
 * Creation date: (5/15/2001 5:15:13 PM)
 */
public class DbaseFile {
    
    // Header information for the DBase File
    private DbaseFileHeader myHeader;
    
    // Record Information.
    private Vector myRecordVect = new Vector();
    
    // The file name to read or write.
    private String myFileName;
    
    // Convenient place to store the field names.
    private String[] myFieldNames;
    
    // verbose output flag
    private static boolean ourVerbose = false;
    
    /**
     * DbaseFile constructor comment.
     */
    public DbaseFile() {
        super();
    }
    
    /**
     * DbaseFile constructor comment.
     */
    public DbaseFile(String inFileName) {
        super();
        setFileName(inFileName);
    }
    
    /**
     * Adds the column to the file.
     */
    public void addColumn(String inName, char inType, int inLength, int inDecimalPlace) throws Exception{
        // update the header.
        if (myHeader == null) myHeader = new DbaseFileHeader();
        myHeader.addColumn(inName, inType, inLength, inDecimalPlace);
    }
    
    /**
     * Removes the named column from the file
     */
    public void removeColumn(String inName) {
        if (myHeader != null) {
            String trimName = inName.trim();
            int rmCol = myHeader.removeColumn(trimName);
            
            if (rmCol >= 0) {
                // now remove the data for this column from each record
                for (int rec = 0; rec < myHeader.getNumRecords(); rec++) {
                    // copy the fields before and after the deleted column
                    Object[] tempRec = new Object[myHeader.getNumFields()];
                    System.arraycopy(getRecord(rec), 0,
                    tempRec, 0, rmCol);
                    System.arraycopy(getRecord(rec), rmCol+1,
                    tempRec, rmCol,getRecord(rec).length-rmCol-1);
                    setRecord(tempRec, rec);
                }
                
                // re-build the list of field names
                myFieldNames = new String[myHeader.getNumFields()];
                for (int i=0; i<myHeader.getNumFields(); i++){
                    myFieldNames[i] = myHeader.getFieldName(i).trim();
                }
            }
        }
    }
    
    /**
     * Adds the record to the dataset.
     */
    protected void addRecord(Object[] inRecord){
        myRecordVect.addElement(inRecord);
    }
    
    /**
     * Returns the array of field names.
     */
    public String[] getFieldNames(){
        return myFieldNames;
    }
    
    /**
     * Retrieves the file name to read or write.
     */
    public String getFileName(){
        return myFileName;
    }
    
    // Retrieve number of records in the DbaseFile
    public int getNumRecords(){
        if (myRecordVect == null) return 0;
        else return myRecordVect.size();
    }
    
    // Retrieve the record at the given index
    public Object[] getRecord(int inIndex){
        return (Object[]) myRecordVect.elementAt(inIndex);
    }
    
    /** Retrieve the name of the given column. */
    public String getFieldName(int inIndex){
        return myHeader.getFieldName(inIndex);
    }
    
    /** Retrieve the type of the given column.*/
    public char getFieldType(int inIndex){
        return myHeader.getFieldType(inIndex);
    }
    
    /** Retrieve the length of the given column. */
    public int getFieldLength(int inIndex){
        return myHeader.getFieldLength(inIndex);
    }
    
    /** Retrieve the location of the decimal point. */
    public int getFieldDecimalLength(int inIndex){
        return myHeader.getFieldDecimalCount(inIndex);
    }
    
    /**
     * read the DBF file into memory.
     */
    public void read() throws Exception{
        // create an input stream from the file
        if (myFileName == null) throw new Exception("No file name specified");
        
        InputStream fin = null;
        // check to see if it is a compressed file
        if (myFileName.toUpperCase().endsWith(".GZ")) {
            FileInputStream fileIn =
            new FileInputStream(myFileName);
            fin = (InputStream) new GZIPInputStream(fileIn);
        }
        else {
            fin = (InputStream) new FileInputStream(myFileName);
        }
        LEDataInputStream in = new LEDataInputStream(fin);
        
        // create the header to contain the header information.
        myHeader = new DbaseFileHeader();
        myHeader.readHeader(in);
        myFieldNames = new String[myHeader.getNumFields()];
        for (int i=0; i<myHeader.getNumFields(); i++){
            myFieldNames[i] = myHeader.getFieldName(i).trim();
        }
        
        // flag to indicate to the user that there is extra space in this DBF file.
        boolean tempTelling = true;
        
        // read the data.
        char tempDeleted;
        for (int i=0; i<myHeader.getNumRecords(); i++){
            
            // retrieve the record length
            int tempNumFields = myHeader.getNumFields();
            
            // storage for the actual values
            Object[] tempRow = new Object[tempNumFields];
            
            // read the deleted flag
            tempDeleted = (char) in.readByte();
            
            // read the record length
            int tempRecordLength = 1; // for the deleted character just read.
            
            // read the Fields
            for (int j=0; j<tempNumFields; j++){
                
                // find the length of the field.
                int tempFieldLength = myHeader.getFieldLength(j);
                tempRecordLength = tempRecordLength + tempFieldLength;
                
                // find the field type
                char tempFieldType = myHeader.getFieldType(j);
                //System.out.print("Reading Name="+myHeader.getFieldName(j)+" Type="+tempFieldType +" Length="+tempFieldLength);
                
                // read the data.
                Object tempObject = null;
                switch (tempFieldType){
                    case 'L': // logical data type, one character (T,t,F,f,Y,y,N,n)
                        char tempChar = (char) in.readByte();
                        if ((tempChar == 'T') || (tempChar == 't') || (tempChar == 'Y') || (tempChar == 'y')){
                            tempObject = new Boolean(true);
                        }
                        else {
                            tempObject = new Boolean(false);
                        }
                        break;
                    case 'C': // character record.
                        byte[] sbuffer = new byte[tempFieldLength];
                        in.readFully(sbuffer);
                        tempObject = new String(sbuffer, "ISO-8859-1").trim();
                        break;
                    case 'D': // date data type.
                        byte[] dbuffer = new byte[8];
                        in.readFully(dbuffer);
                        String tempString = new String(dbuffer, 0, 4);
                        try{
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
                        }
                        
                        break;
                    case 'M': // memo field.
                        byte[] mbuffer = new byte[10];
                        in.readFully(mbuffer);
                        break;
                    case 'N': // number
                    case 'F': // floating point number
                        byte[] fbuffer = new byte[tempFieldLength];
                        in.readFully(fbuffer);
                        try{
                            tempString = new String(fbuffer);
                            tempObject = Double.valueOf(tempString.trim());
                        }
                        catch (NumberFormatException e){
                        }
                        break;
                    default:
                        byte[] defbuffer = new byte[tempFieldLength];
                        in.readFully(defbuffer);
                        System.out.println("Do not know how to parse Field type "+tempFieldType);
                }
                tempRow[j] = tempObject;
                //				System.out.println(" Data="+tempObject);
            }
            
            // ensure that the full record has been read.
            if (tempRecordLength < myHeader.getRecordLength()){
                byte[] tempbuff = new byte[myHeader.getRecordLength()-tempRecordLength];
                in.readFully(tempbuff);
                if (tempTelling){
                    System.out.println("DBF File has "+(myHeader.getRecordLength()-tempRecordLength)+" extra bytes per record");
                    tempTelling = false;
                }
            }
            
            // add the row if it is not deleted.
            if (tempDeleted != '*'){
                myRecordVect.addElement(tempRow);
            }
        }
        fin.close();
    }
    
    /**
     * Removes all data from the dataset
     */
    protected void removeAllRecords(){
        myRecordVect.removeAllElements();
    }
    
    /**
     * Set the file name to read or write.
     */
    public void setFileName(String inFileName){
        if (inFileName == null) return;
        if (inFileName.length() == 0) return;
        
        // check if the file exists
        File tempFile = new File(inFileName);
        if (!tempFile.exists()){
            if (!inFileName.toUpperCase().endsWith(".DBF")) {
                myFileName = inFileName+".dbf";
            }
        }
        else{
            myFileName = inFileName;
        }
    }
    
    // notify about warnings.
    private boolean myWarning = true;
    /** Print warnings to system.out. */
    public void setWarnings(boolean inWarning){
        myWarning = inWarning;
        if (myHeader == null) myHeader = new DbaseFileHeader();
        myHeader.setWarnings(inWarning);
    }
    
    // Set the record at the given index
    public void setRecord(Object[] inRecord, int inIndex){
        myRecordVect.setElementAt(inRecord, inIndex);
    }
    
    // Set the verbose mode for output from this class
    public static void setVerbose(boolean inVerbose) {
        ourVerbose = inVerbose;
    }
    
    /**
     * write the DBF file to disk.
     */
    public void write() throws Exception{
        
        // create an output stream from the file
        if (myFileName == null) throw new Exception("No file name specified");
        FileOutputStream fout = new FileOutputStream(myFileName);
        LEDataOutputStream out = new LEDataOutputStream(fout);
        
        // write the header information
        if (myHeader == null) throw new Exception("No header available to write");
        myHeader.setNumRecords(myRecordVect.size());
        myHeader.writeHeader(out);
        int tempRecordLength = myHeader.getRecordLength();
        
        // write the data.
        for (int i=0; i<myHeader.getNumRecords(); i++){
            // keep track of how many bytes are written.
            int templength = 0;
            
            // storage for the actual values
            Object[] tempRow = getRecord(i);
            
            // write the deleted value
            out.writeByte(' ');
            templength++;
            for (int j=0; j<tempRow.length; j++){
                
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
                        if (tempRow[j] instanceof Boolean){
                            if ( ((Boolean) tempRow[j]).booleanValue() == true){
                                out.writeByte('T');
                                tempBytesWritten++;
                            }
                            else{
                                out.writeByte('F');
                                tempBytesWritten++;
                            }
                        }
                        else{
                            out.writeByte('F');
                            tempBytesWritten++;
                        }
                        templength = templength + 1;
                        break;
                    case 'C': // character record.
                    {
                        String tempString = "";
                        if (tempRow[j] != null) tempString = tempRow[j].toString();
                        byte[] tempBytes = tempString.getBytes("ISO-8859-1");
                        for (int k=0; k<tempFieldLength; k++){
                            if (tempBytes.length > k){
                                out.writeByte(tempBytes[k]);
                                tempBytesWritten++;
                            }
                            else{
                                out.writeByte(' ');
                                tempBytesWritten++;
                            }
                        }
                        templength = templength + tempFieldLength;
                    }
                    break;
                    case 'D': // date data type.
                        if (tempRow[j] instanceof Date){
                            Calendar c = Calendar.getInstance();
                            c.setTime((Date) tempRow[j]);
                            String tempString = ""+c.get(Calendar.YEAR);
                            for (int k=0; k<4; k++){
                                if (tempString.length()>k){
                                    out.writeByte(tempString.charAt(k));
                                    tempBytesWritten++;
                                }
                                else {
                                    out.writeByte('0');
                                    tempBytesWritten++;
                                }
                            }
                            tempString = ""+(c.get(Calendar.MONTH)+1);
                            if (tempString.length() == 0) {out.writeByte('0'); out.writeByte('0');}
                            if (tempString.length() == 1) {out.writeByte('0'); out.writeByte(tempString.charAt(0));}
                            if (tempString.length() > 1) {out.writeByte(tempString.charAt(0)); out.writeByte(tempString.charAt(1));}
                            tempBytesWritten +=2;
                            tempString = ""+c.get(Calendar.DAY_OF_MONTH);
                            if (tempString.length() == 0) {out.writeByte('0'); out.writeByte('0');}
                            if (tempString.length() == 1) {out.writeByte('0'); out.writeByte(tempString.charAt(0));}
                            if (tempString.length() > 1) {out.writeByte(tempString.charAt(0)); out.writeByte(tempString.charAt(1));}
                            tempBytesWritten +=2;
                        }
                        else{
                            // write 8 blanks
                            for (int k=0; k<8; k++){
                                out.writeByte(0);
                                tempBytesWritten++;
                            }
                        }
                        templength = templength + 8;
                        break;
                    case 'N': // number
                        if (tempFieldName.equals("SE_ROW_ID")){
                            System.out.println("");
                        }
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
                        
                        if (tempRow[j] instanceof Number){
                            String tempString = ((Number) tempRow[j]).toString();
                            
                            
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
                                    out.writeByte('0');
                                    tempBytesWritten++;
                                }
                                else{
                                    out.writeByte(tempWorkingString.charAt(k-(tempLeadingLength-tempWorkingString.length())));
                                    tempBytesWritten++;
                                }
                            }
                            
                            
                            // write the trailer.
                            if (tempDecimals >0){
                                out.writeByte('.');
                                tempBytesWritten++;
                            }
                            for (int k=0; k<tempDecimals; k++){
                                if (tempIndex == 0){
                                    out.writeByte('0');
                                    tempBytesWritten++;
                                }
                                else{
                                    if((tempIndex + k + 1) >= tempString.length()){
                                        out.writeByte('0');
                                        tempBytesWritten++;
                                    }
                                    else{
                                        out.writeByte(tempString.charAt(tempIndex+k+1));
                                        tempBytesWritten++;
                                    }
                                }
                            }
                        }
                        else{
                            for (int k=0; k<tempLeadingLength; k++){
                                out.writeByte(0);
                                tempBytesWritten++;
                            }
                            if (tempDecimals >0){
                                out.writeByte('.');
                                tempBytesWritten++;
                                for (int k=0; k<tempDecimals; k++){
                                    out.writeByte(0);
                                    tempBytesWritten++;
                                }
                            }
                        }
                        templength = templength + tempFieldLength;
                        break;
                    case 'F': // floating point number
                        // The decimal can be in any location for these types of numbers.
                        if (tempRow[j] instanceof Number){
                            String tempString = ((Number) tempRow[j]).toString();
                            int tempIndex = tempString.indexOf(".");
                            if (tempIndex > tempFieldLength){
                                for (int k=0; k<tempFieldLength; k++){
                                    out.writeByte('9');
                                    tempBytesWritten++;
                                }
                            }
                            else if (tempIndex == -1){
                                // write the string
                                for (int k=0; k<tempFieldLength; k++){
                                    if (k<tempString.length()){
                                        out.writeByte(tempString.charAt(k));
                                        tempBytesWritten++;
                                    }
                                    else{
                                        out.writeByte(' ');
                                        tempBytesWritten++;
                                    }
                                }
                            }
                        }
                        // write the string value.
                        else{
                            if (tempRow[j] == null){
                                for (int k=0; k<tempFieldLength; k++){
                                    out.writeByte(0);
                                    tempBytesWritten++;
                                }
                            }
                            else{
                                String tempString = tempRow[j].toString();
                                // write the string
                                for (int k=0; k<tempFieldLength; k++){
                                    if (k<tempString.length()){
                                        out.writeByte(tempString.charAt(k));
                                        tempBytesWritten++;
                                    }
                                    else{
                                        out.writeByte(0);
                                        tempBytesWritten++;
                                    }
                                }
                            }
                        }
                        
                    default:
                        System.out.println("Do not know how to parse/write field type "+tempFieldType);
                }
                if (ourVerbose) {
                    System.out.println("Wrote "+tempBytesWritten+" byte for "+tempFieldName+" Field Length is "+tempFieldLength+" Length="+templength);
                }
            }
            
            // if we have not yet finished writing the record, then finish it off.
            if (ourVerbose) {
                System.out.println("Writing "+(tempRecordLength-templength)+" Bytes at the end");
            }
            for (int j=0; j<(tempRecordLength-templength); j++){
                out.writeByte(0);
            }
        }
    }
}
