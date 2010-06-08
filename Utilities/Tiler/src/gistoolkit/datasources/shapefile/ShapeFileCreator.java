/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2003, Ithaqua Enterprises Inc.
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
import gistoolkit.features.*;

/**
 * Class for saving a data set as a shape file.
 */
public class ShapeFileCreator {
    
    /** Creates new ShapeFileCreator */
    public ShapeFileCreator() {
    }
    
    /** Save the shape file as a file. */
    public static void save(Record[] inRecords, String inFileName) throws Exception{
        ArrayList tempNameList = new ArrayList();
        ArrayList tempTypeList = new ArrayList();
        
        // retrieve the full list of names.
        String[] tempLastNames = null;
        for (int i=0; i<inRecords.length; i++){
            String[] tempNames = inRecords[i].getAttributeNames();
            if (tempNames == tempLastNames) continue;
            AttributeType[] tempTypes = inRecords[i].getAttributeTypes();
            for (int j=0; j<tempNames.length; j++){
                boolean found = false;
                for (int k=0; k<tempTypeList.size(); k++){
                    if (tempNameList.get(k) == tempNames[j]){
                        found = true;
                        break;
                    }
                    else{
                        String tempName = (String) tempNameList.get(i);
                        if (tempName.equalsIgnoreCase(tempNames[j])){
                            found = true;
                            break;
                        }
                    }
                }
                if (!found){
                    tempNameList.add(tempNames[j]);
                    if (j >= tempTypes.length){
                        System.out.println("TempTypes are Too short");
                        tempTypeList.add(new AttributeType(AttributeType.STRING, 20));
                    }
                    else tempTypeList.add(tempTypes[j]);
                }
            }
            tempLastNames = tempNames;
        }
        
        // Now we have the complete list of attributes, and attribute types.
        String[] tempAttributeNames = new String[tempNameList.size()];
        tempNameList.toArray(tempAttributeNames);
        AttributeType[] tempAttributeTypes = new AttributeType[tempTypeList.size()];
        tempTypeList.toArray(tempAttributeTypes);
        
        // determine how many different types of shapes we have.
        int[] tempTypes = new int[20];
        ArrayList[] tempLists = new ArrayList[20];
        
        int length = 0;
        ArrayList[] tempTypesList = new ArrayList[20];
        ArrayList tempListNull = new ArrayList();
        
        for (int i=0; i<inRecords.length; i++){
            if (inRecords[i].getShape() == null) tempListNull.add(inRecords[i]);
            else{
                try{
                    int tempType = ShapeFile.getShapeType(inRecords[i].getShape());
                    boolean found = false;
                    for (int j=0; j<length; j++){
                        if (tempType == tempTypes[j]){
                            found = true;
                            tempTypesList[j].add(inRecords[i]);
                            break;
                        }
                    }
                    
                    // if the shape was not found add a new type for it
                    if (!found){
                        if (length < 20){
                            tempTypesList[length] = new ArrayList();
                            tempTypes[length] = tempType;
                            tempTypesList[length].add(inRecords[i]);
                            length++;
                        }
                    }
                }
                catch (Exception e){
                    System.out.println(""+e); //unsupported shape type
                }
            }
        }
        
        // if the length is == 0, then there are no shapes.
        if (length == 0) return;
        
        // if there is only one type of record (the 90% case I would expect), then copy the null shapes into this set, and save the lot as one shape file.
        if (length == 1){
            for (int i=0; i<tempListNull.size(); i++){
                tempTypesList[0].add(tempListNull.get(i));
            }
            Record[] tempRecords = new Record[tempTypesList[0].size()];
            tempTypesList[0].toArray(tempRecords);
            saveRecords(inFileName, tempRecords, tempAttributeNames, tempAttributeTypes);
        }
        
        // if there are more than one type of shape, then record them in separate shape files.
        else{
            for (int i=0; i<length; i++){
                String tempFileName = inFileName + ShapeFile.getShapeName(tempTypes[i]);
                Record[] tempRecords = new Record[tempTypesList[i].size()];
                tempTypesList[i].toArray(tempRecords);
                saveRecords(tempFileName, tempRecords, tempAttributeNames, tempAttributeTypes);
            }
            
            if (tempListNull.size() > 0){
                String tempFileName = inFileName + "Nulls";
                Record[] tempRecords = new Record[tempListNull.size()];
                tempListNull.toArray(tempRecords);
                saveRecords(tempFileName, tempRecords, tempAttributeNames, tempAttributeTypes);
            }
        }
    }
    
    
    /**
     * At this point, I know that the records are all the same type.
     */
    private static void saveRecords(String inFileName, Record[] inRecords, String[] inAttributeNames, AttributeType[] inAttributeTypes) throws Exception{
        
        // construct the Attributes file.
        ShapeFile tempShapeFile = new ShapeFile(inFileName);
        
        // ensure that the records are the correct length.
        for (int i=0; i<inAttributeTypes.length; i++){
            if (inAttributeTypes[i].getLength() <= 0){
                if (inAttributeTypes[i].getType() == AttributeType.STRING){
                    int tempMax = 10;
                    for (int j=0; j<inRecords.length; j++){
                        if (inRecords[j].getAttributes()[i] instanceof String){
                            String tempString = (String) (inRecords[j].getAttributes()[i]);
                            if (tempMax < tempString.length()) tempMax = tempString.length();
                        }
                    }
                    inAttributeTypes[i] = new AttributeType(AttributeType.STRING, tempMax, -1);
                }
            }
        }
        
        // add the columns to the file.
        for (int i=0; i<inAttributeNames.length; i++){
            AttributeType tempType = inAttributeTypes[i];
            String tempName =(String) inAttributeNames[i];
            tempShapeFile.addColumn(tempName, ShapeFile.getFieldType(tempType), tempType.getLength(), tempType.getAuxLength());
        }
        
        ArrayList tempListRecords = new ArrayList();
        
        // add the rows to the files.
        for (int i=0; i<inRecords.length; i++){
            Object[] tempAttributeValues = new Object[inAttributeNames.length];
            String[] tempRecordAttributeNames = inRecords[i].getAttributeNames();
            Object[] tempRecordAttributeValues = inRecords[i].getAttributes();
            for (int j=0; j<inAttributeNames.length; j++){
                for (int k=0; k<tempRecordAttributeNames.length; k++){
                    if (inAttributeNames[j].equals(tempRecordAttributeNames[k])){
                        tempAttributeValues[j] = tempRecordAttributeValues[k];
                        break;
                    }
                    else if (inAttributeNames[j].equalsIgnoreCase(tempRecordAttributeNames[k])){
                        tempAttributeValues[j] = tempRecordAttributeValues[k];
                    }
                }
            }
            
            // save the record.
            tempListRecords.add(new ShapeFileRecord(i, inRecords[i].getShape(), tempAttributeValues, inAttributeNames, inAttributeTypes));
        }
        
        // save the records
        ShapeFileRecord[] tempRecords = new ShapeFileRecord[tempListRecords.size()];
        tempListRecords.toArray(tempRecords);
        tempShapeFile.setRecords(tempRecords);
        tempShapeFile.writeRecords();
    }
}
