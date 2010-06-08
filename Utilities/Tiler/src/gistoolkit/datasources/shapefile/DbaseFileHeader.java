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

import java.util.*;
import cmp.LEDataStream.*;
/**
 * Class to represent the header of a Dbase III file.
 * Creation date: (5/15/2001 5:15:30 PM)
 */
public class DbaseFileHeader {
    // Constant for the size of a record
    private int FILE_DESCRIPTOR_SIZE=32;
    
    // type of the file, must be 03h
    private int myFileType=0x03;
    
    // Date the file was last updated.
    private Date myUpdateDate = new Date();
    
    // Number of records in the datafile
    private int myNumRecords = 0;
    
    // Length of the header structure
    private int myHeaderLength;
    
    // Length of the records
    private int myRecordLength;
    
    // Number of fields in the record.
    private int myNumFields;
    
    // notify about warnings.
    private boolean myWarning = true;
    /** Print warnings to system.out. */
    public void setWarnings(boolean inWarning){myWarning = inWarning;}
    
    /**
     * Class for holding the information assicated with a record.
     */
    class DbaseFieldDescriptor{
        
        // Field Name
        String myFieldName;
        
        // Field Type (C N L D or M)
        char myFieldType;
        
        // Field Data Address offset from the start of the record.
        int myFieldDataAddress;
        
        // Length of the data in bytes
        int myFieldLength;
        
        // Field decimal count in Binary, indicating where the decimal is
        int myDecimalCount;
        
    }
    
    // collection of header records.
    private DbaseFieldDescriptor[] myFieldDescriptions;
    
    /**
     * DbaseFileHreader constructor comment.
     */
    public DbaseFileHeader() {
        super();
    }
    
    /**
     * Add a column to this DbaseFileHeader.
     * The type is one of (C N L or D) character, number, logical(true/false), or date.
     * The Field length is the total length in bytes reserved for this column.
     * The decimal count only applies to numbers(N), and floating point values (F), and refers to the number of characters to reserve after the decimal point.
     */
    public void addColumn (String inFieldName, char inFieldType, int inFieldLength, int inDecimalCount) throws Exception{
        if (inFieldLength <=0) inFieldLength = 1;
        if (myFieldDescriptions == null) myFieldDescriptions = new DbaseFieldDescriptor[0];
        int tempLength = 1;  // the length is used for the offset, and there is a * for deleted as the first byte
        DbaseFieldDescriptor[] tempFieldDescriptors = new DbaseFieldDescriptor[myFieldDescriptions.length+1];
        for (int i=0; i<myFieldDescriptions.length; i++){
            myFieldDescriptions[i].myFieldDataAddress = tempLength;
            tempLength = tempLength + myFieldDescriptions[i].myFieldLength;
            tempFieldDescriptors[i] = myFieldDescriptions[i];
        }
        tempFieldDescriptors[myFieldDescriptions.length] = new DbaseFieldDescriptor();
        tempFieldDescriptors[myFieldDescriptions.length].myFieldLength = inFieldLength;
        tempFieldDescriptors[myFieldDescriptions.length].myDecimalCount = inDecimalCount;
        tempFieldDescriptors[myFieldDescriptions.length].myFieldDataAddress = tempLength;
        
        // set the field name
        String tempFieldName = inFieldName;
        if (tempFieldName == null) tempFieldName = "NoName";
        if (tempFieldName.length() > 11) {
            tempFieldName = tempFieldName.substring(0,11);
            warn("FieldName "+inFieldName+" is longer than 11 characters, truncating to "+tempFieldName);
        }        
        tempFieldDescriptors[myFieldDescriptions.length].myFieldName = tempFieldName;
        
        // the field type
        if ((inFieldType == 'C') || (inFieldType == 'c')){
            tempFieldDescriptors[myFieldDescriptions.length].myFieldType = 'C';
            if (inFieldLength > 254) warn("Field Length for "+inFieldName+" set to "+inFieldLength+" Which is longer than 254, not consistent with dbase III");            
        }
        else if ((inFieldType == 'S') || (inFieldType == 's')){
            tempFieldDescriptors[myFieldDescriptions.length].myFieldType = 'C';
            warn("Field type for "+inFieldName+" set to S which is flat out wrong people!, I am setting this to C, in the hopes you meant character.");
            if (inFieldLength >254) warn("Field Length for "+inFieldName+" set to "+inFieldLength+" Which is longer than 254, not consistent with dbase III");
            tempFieldDescriptors[myFieldDescriptions.length].myFieldLength = 8;
        }
        else if ((inFieldType == 'D') || (inFieldType == 'd')){
            tempFieldDescriptors[myFieldDescriptions.length].myFieldType = 'D';
            if (inFieldLength != 8) warn("Field Length for "+inFieldName+" set to "+inFieldLength+" Setting to 8 digets YYYYMMDD");            
            tempFieldDescriptors[myFieldDescriptions.length].myFieldLength = 8;
        }
        else if ((inFieldType == 'F') || (inFieldType == 'f')){
            tempFieldDescriptors[myFieldDescriptions.length].myFieldType = 'F';
            if (inFieldLength > 20) warn("Field Length for "+inFieldName+" set to "+inFieldLength+" Preserving length, but should be set to Max of 20 not valid for dbase IV, and UP specification, not present in dbaseIII.");
        }
        else if ((inFieldType == 'N') || (inFieldType == 'n')){
            tempFieldDescriptors[myFieldDescriptions.length].myFieldType = 'N';
            if (inFieldLength > 18) warn("Field Length for "+inFieldName+" set to "+inFieldLength+" Preserving length, but should be set to Max of 18 for dbase III specification.");
            if (inDecimalCount < 0){
                warn("Field Decimal Position for "+inFieldName+" set to "+inDecimalCount+" Setting to 0 no decimal data will be saved.");
                tempFieldDescriptors[myFieldDescriptions.length].myDecimalCount = 0;                
            }
            if(inDecimalCount>inFieldLength-1){
                warn("Field Decimal Position for "+inFieldName+" set to "+inDecimalCount+" Setting to "+(inFieldLength-1)+" no non decimal data will be saved.");
                tempFieldDescriptors[myFieldDescriptions.length].myDecimalCount = inFieldLength-1;
            }
        }
        else if ((inFieldType == 'L') || (inFieldType == 'l')){
            tempFieldDescriptors[myFieldDescriptions.length].myFieldType = 'L';
            if (inFieldLength != 1) warn("Field Length for "+inFieldName+" set to "+inFieldLength+" Setting to length of 1 for logical fields.");
            tempFieldDescriptors[myFieldDescriptions.length].myFieldLength = 1;
        }
        else {
            throw new Exception("Undefined field type "+inFieldType + " For column "+inFieldName);
        }
        // the length of a record
        tempLength = tempLength + tempFieldDescriptors[myFieldDescriptions.length].myFieldLength;
        
        // set the new fields.
        myFieldDescriptions = tempFieldDescriptors;
        myHeaderLength = 33+32*myFieldDescriptions.length;
        myNumFields = myFieldDescriptions.length;
        myRecordLength=tempLength;
    }
    
    /**
     * Remove a column from this DbaseFileHeader.
     * @return index of the removed column, -1 if no found
     */
    public int removeColumn (String inFieldName) {
        
        int retCol = -1;
        int tempLength = 1;
        DbaseFieldDescriptor[] tempFieldDescriptors =
            new DbaseFieldDescriptor[myFieldDescriptions.length - 1];
        for (int i = 0, j = 0; i < myFieldDescriptions.length; i++) {
            if (!inFieldName.equalsIgnoreCase(myFieldDescriptions[i].myFieldName.trim())) {
                // if this is the last field and we still haven't found the
                // named field
                if (i == j &&
                    i == myFieldDescriptions.length - 1) {
                    System.err.println("Could not find a field named '" + 
                                       inFieldName + "' for removal");
                    return retCol;
                }
                tempFieldDescriptors[j] = myFieldDescriptions[i];
                tempFieldDescriptors[j].myFieldDataAddress = tempLength;
                tempLength += tempFieldDescriptors[j].myFieldLength;
                // only increment j on non-matching fields
                j++;
            }
            else {
                retCol = i;
            }
        }

        // set the new fields.
        myFieldDescriptions = tempFieldDescriptors;
        myHeaderLength = 33+32*myFieldDescriptions.length;
        myNumFields = myFieldDescriptions.length;
        myRecordLength = tempLength;

        return retCol;
    }

    private void warn(String inWarn){
        if (myWarning){
            System.out.print("WARNING: ");
            System.out.println(inWarn);
        }
    }
    
    /**
     * Return the Field Descriptor for the given field.
     */
    public DbaseFieldDescriptor getFieldDescription(int inIndex){
        return myFieldDescriptions[inIndex];
    }
    
    // Retrieve the length of the field at the given index
    public int getFieldLength(int inIndex){
        return myFieldDescriptions[inIndex].myFieldLength;
    }
    
    // Retrieve the location of the decimal point within the field.
    public int getFieldDecimalCount(int inIndex){
        return myFieldDescriptions[inIndex].myDecimalCount;
    }
    
    // Retrieve the Name of the field at the given index
    public String getFieldName(int inIndex){
        return myFieldDescriptions[inIndex].myFieldName;
    }
    
    // Retrieve the type of field at the given index
    public char getFieldType(int inIndex){
        return myFieldDescriptions[inIndex].myFieldType;
    }
    
    /**
     * Return the date this file was last updated.
     */
    public Date getLastUpdateDate(){
        return myUpdateDate;
    }
    
    /**
     * Return the number of fields in the records.
     */
    public int getNumFields(){
        return myNumFields;
    }
    
    /**
     * Return the number of records in the file
     */
    public int getNumRecords(){
        return myNumRecords;
    }
    
    /**
     * Return the length of the records in bytes.
     */
    public int getRecordLength(){
        return myRecordLength;
    }

    /**
     * Return the length of the header
     */
    public int getHeaderLength() 
    {
        return myHeaderLength;
    }
    
    /**
     * Read the header data from the DBF file.
     */
    public void readHeader(LEDataInputStream in)throws Exception{
        // type of file.
        myFileType = in.readUnsignedByte();
        if (myFileType != 0x03) {
            if (myWarning) System.out.println("Unsupported DBF file Type "+Integer.toHexString(myFileType));
            else throw new Exception("Unsupported DBF file Type "+Integer.toHexString(myFileType));
        }
        
        // parse the update date information.
        int tempUpdateYear = (int)in.readUnsignedByte();
        int tempUpdateMonth = (int)in.readUnsignedByte();
        int tempUpdateDay = (int)in.readUnsignedByte();
        tempUpdateYear = tempUpdateYear + 1900;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, tempUpdateYear);
        c.set(Calendar.MONTH, tempUpdateMonth-1);
        c.set(Calendar.DATE, tempUpdateDay);
        myUpdateDate = c.getTime();
        
        // read the number of records.
        myNumRecords = in.readInt();
        
        // read the length of the header structure.
        myHeaderLength = in.readShort();
        
        // read the length of a record
        myRecordLength = in.readShort();
        
        // skip the reserved bytes in the header.
        in.skipBytes(20);
        
        // calculate the number of Fields in the header
        myNumFields = (myHeaderLength - FILE_DESCRIPTOR_SIZE -1)/FILE_DESCRIPTOR_SIZE;
        
        // read all of the header records
        myFieldDescriptions = new DbaseFieldDescriptor[myNumFields];
        for (int i=0; i<myNumFields; i++){
            myFieldDescriptions[i] = new DbaseFieldDescriptor();
            
            // read the field name
            byte[] buffer = new byte[11];
            in.readFully(buffer);
            myFieldDescriptions[i].myFieldName = new String(buffer);
            
            // read the field type
            myFieldDescriptions[i].myFieldType = (char) in.readByte();
            
            // read the field data address, offset from the start of the record.
            myFieldDescriptions[i].myFieldDataAddress = in.readInt();
            
            // read the field length in bytes
            int tempLength = (int) in.readByte();
            if (tempLength < 0) tempLength = tempLength + 256;
            myFieldDescriptions[i].myFieldLength = tempLength;
            
            // read the field decimal count in bytes
            myFieldDescriptions[i].myDecimalCount = (int) in.readByte();
            
            // read the reserved bytes.
            in.skipBytes(14);
        }
        
        // Last byte is a marker for the end of the field definitions.
        in.skipBytes(1);
        
    }
    
    /**
     * Set the number of records in the file
     */
    protected void setNumRecords(int inNumRecords){
        myNumRecords = inNumRecords;
    }
    
    /**
     * Write the header data to the DBF file.
     */
    public void writeHeader(LEDataOutputStream out)throws Exception{
        // write the output file type.
        out.writeByte(myFileType);
        
        // write the date stuff
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        out.writeByte(c.get(Calendar.YEAR)-1900);
        out.writeByte(c.get(Calendar.MONTH)+1);
        out.writeByte(c.get(Calendar.DAY_OF_MONTH));
        
        // write the number of records in the datafile.
        out.writeInt(myNumRecords);
        
        // write the length of the header structure.
        out.writeShort(myHeaderLength);
        
        // write the length of a record
        out.writeShort(myRecordLength);
        
        // write the reserved bytes in the header
        for (int i=0; i<20; i++) out.writeByte(0);
        
        // write all of the header records
        int tempOffset = 0;
        for (int i=0; i<myFieldDescriptions.length; i++){
            
            // write the field name
            for (int j=0; j<11; j++){
                if (myFieldDescriptions[i].myFieldName.length() > j){
                    out.writeByte((int) myFieldDescriptions[i].myFieldName.charAt(j));
                }
                else{
                    out.writeByte(0);
                }
            }
            
            // write the field type
            out.writeByte(myFieldDescriptions[i].myFieldType);
            
            // write the field data address, offset from the start of the record.
            out.writeInt(tempOffset);
            tempOffset += myFieldDescriptions[i].myFieldLength;
            
            // write the length of the field.
            out.writeByte(myFieldDescriptions[i].myFieldLength);
            
            // write the decimal count.
            out.writeByte(myFieldDescriptions[i].myDecimalCount);
            
            // write the reserved bytes.
            for (int j=0; j<14; j++) out.writeByte(0);
        }
        
        // write the end of the field definitions marker
        out.writeByte(0x0D);
    }
}
