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
package gistoolkit.datasources;

import java.util.*;
import java.sql.*;
import gistoolkit.features.*;

/**
 * Class to convert java types and AttributeTypes to SQL for a particular database.
 */
public class SimpleSQLConverter implements SQLConverter{
    
    /** Creates a new instance of SimpleSQLConverter */
    public SimpleSQLConverter() {
    }
    /** Convert the given ODBC type of data to the GISToolkit type of data. */
    public AttributeType getAttributeType(ResultSetMetaData inrmet, int inIndex)throws SQLException{
        int tempType = inrmet.getColumnType(inIndex+1);
        int tempSize = inrmet.getColumnDisplaySize(inIndex+1);
        int tempSize2 = inrmet.getPrecision(inIndex+1);
        if (tempSize == 0) tempSize = -1;
        AttributeType tempAttributeType = null;
        switch( tempType ) {
            case Types.CHAR:
                tempAttributeType = new AttributeType(AttributeType.STRING, tempSize);
                break;
            case Types.DATE:
                tempAttributeType = new AttributeType(AttributeType.TIMESTAMP, tempSize);
                break;
            case Types.DECIMAL:
                tempAttributeType = new AttributeType(AttributeType.FLOAT, tempSize, tempSize2);
                break;
            case Types.FLOAT:
                tempAttributeType = new AttributeType(AttributeType.FLOAT, tempSize, tempSize2);
                break;
            case Types.DOUBLE:
                tempAttributeType = new AttributeType(AttributeType.FLOAT, tempSize, tempSize2);
                break;
            case Types.INTEGER:
                tempAttributeType = new AttributeType(AttributeType.INTEGER, tempSize);
                break;
            case Types.NUMERIC:
                tempAttributeType = new AttributeType(AttributeType.FLOAT, tempSize, tempSize2);
                break;
            case Types.TIMESTAMP:
                tempAttributeType = new AttributeType(AttributeType.TIMESTAMP, tempSize);
                break;
            case Types.VARCHAR:
                tempAttributeType = new AttributeType(AttributeType.STRING, tempSize);
                break;
            default:
                tempAttributeType = new AttributeType(AttributeType.UNKNOWN, tempSize);
                System.out.println("Unknown JDBC Attribute Type "+tempType);
        } // End switch
        return tempAttributeType;
    }
    
    /**
     * Take the given type, of the given value, and convert it to the sql string needed for insertion into the database.
     * In the case of strings, this routine will add the appostraphies as needed.
     */
    public String toSQL(Object inObject, AttributeType inAttributeType, int inJDBCType){
        // boolean values.
        if (inJDBCType == Types.CHAR){
            return toSQLString(inObject, inAttributeType);
        }
        if (inJDBCType == Types.DATE){
            return toSQLDate(inObject, inAttributeType);
        }
        if (inJDBCType == Types.DECIMAL){
            return toSQLDecimal(inObject, inAttributeType);
        }
        if (inJDBCType == Types.DOUBLE){
            return toSQLDecimal(inObject, inAttributeType);
        }
        if (inJDBCType == Types.FLOAT){
            return toSQLDecimal(inObject, inAttributeType);
        }
        if (inJDBCType == Types.NUMERIC){
            return toSQLDecimal(inObject, inAttributeType);
        }
        if (inJDBCType == Types.INTEGER){
            return toSQLInteger(inObject, inAttributeType);
        }
        if (inJDBCType == Types.TIMESTAMP){
            return toSQLDate(inObject, inAttributeType);
        }
        if (inJDBCType == Types.VARCHAR){
            return toSQLString(inObject, inAttributeType);
        }
        return"";
    }
    
    /** Convert the object to a JDBC Character representation for the database.  It will add any delimiting characters like the beginning and ending appostraphies, as well as converting the embedded appostraphies to duplicate appostraphies. */
    public String toSQLString(Object inObject, AttributeType inAttributeType){
        if (inObject == null) return "''";
        String tempString = inObject.toString();
        int tempLength = inAttributeType.getLength();
        if (tempLength > 0){
            if (tempString.length() > tempLength) tempString = tempString.substring(0, tempLength);
        }
        // if there are no embedded appostraphies, then return the string.
        if (tempString.indexOf('\'') == -1) return "'"+tempString+"'";
        
        // duplicate the embedded appostraphies, then return the string.
        StringBuffer sb = new StringBuffer();
        sb.append("'");
        for (int i=0; i<tempString.length(); i++){
            sb.append(tempString.charAt(i));
            if (tempString.charAt(i) == '\'') sb.append('\'');
        }
        sb.append("'");
        return sb.toString();
    }
    
    /** Convert the object to the JDBC Date representaion for the database.  It will add any delimited characters, or functions to the database like, to_Date(format, date string) stuff. */
    public String toSQLDate(Object inObject, AttributeType inAttributeType){
        if (inObject == null) return "";
        if (inObject instanceof java.util.Date){
            return toSQLDate( (java.util.Date) inObject, inAttributeType);
        }
        if (inObject instanceof String){
            return toSQLDate( (String) inObject, inAttributeType);
        }
        return toSQLDate(inObject.toString(), inAttributeType);
    }
    
    /** Convert a java date into the JDBC date representaion for this database. */
    public String toSQLDate(java.util.Date inDate, AttributeType inAttributeType){
        // Create the String representation of the date.
        Calendar c = Calendar.getInstance();
        int Year = c.get(Calendar.YEAR);
        int Month = c.get(Calendar.MONTH)+1;
        int Day = c.get(Calendar.DAY_OF_MONTH);
        int Hour = c.get(Calendar.HOUR_OF_DAY);
        int Min = c.get(Calendar.MINUTE);
        int Sec = c.get(Calendar.SECOND);
        return "TIMESTAMP("+Year+"-"+Month+"-"+Day+","+Hour+":"+Min+":"+Sec+")";
    }
    
    /** Convert a string into the JDBC date representaion for this database. */
    public String toSQLDate(String inDate, AttributeType inAttributeType){
        return "'"+inDate+"'";
    }
    
    /** Convert an object into the JDBC Integer representaion for this database. */
    public String toSQLInteger(Object inInteger, AttributeType inAttributeType){
        if (inInteger == null) return "";
        int tempLength = inAttributeType.getLength();        
        if (inInteger instanceof Integer){
            String tempString = ""+((Integer) inInteger).intValue();
            if (tempString.length() > tempLength) {
                StringBuffer sb = new StringBuffer();
                for (int i=0; i<tempLength; i++){
                    sb.append('9');
                }
                tempString = sb.toString();
            }
            else return tempString;
        }
        if (inInteger instanceof Double){
            String tempString = ""+((Double) inInteger).intValue();
            if (tempString.length() > tempLength) {
                StringBuffer sb = new StringBuffer();
                for (int i=0; i<tempLength; i++){
                    sb.append('9');
                }
                tempString = sb.toString();
            }
            else return tempString;
        }
        
        if (inInteger instanceof String){
            try{
                int tempInt = Integer.parseInt((String) inInteger);
                String tempString = ""+tempInt;
                if (tempString.length() > tempLength) {
                    StringBuffer sb = new StringBuffer();
                    for (int i=0; i<tempLength; i++){
                        sb.append('9');
                    }
                    tempString = sb.toString();
                }
                else return tempString;
            }
            catch (NumberFormatException e){
                return "0";
            }
        }
        return "0";
    }
    /** Convert an object into the JDBC decimal representaion for this database. */
    public String toSQLDecimal(Object inDecimal, AttributeType inAttributeType){
        if (inDecimal == null) return "";
        int tempLength = inAttributeType.getLength();
        String tempString = inDecimal.toString();
        if (tempLength > 0){
            int tempPrecision = inAttributeType.getAuxLength();
            int tempScale = tempLength-tempPrecision;

            int tempIndex = tempString.indexOf('.');
            if (tempIndex != -1){
                if (tempIndex > tempLength){
                    StringBuffer sb = new StringBuffer();
                    for (int i=0; i<tempLength; i++){
                        sb.append('9');
                    }
                    tempString = sb.toString();
                }
                if (tempString.length() > tempLength){
                    tempString = tempString.substring(0, tempLength);
                }
            }
            else{
                if (tempString.length() > tempLength){
                    StringBuffer sb = new StringBuffer();
                    for (int i=0; i<tempLength; i++){
                        sb.append('9');
                    }
                    tempString = sb.toString();
                }
            }
        }
        return tempString;
    }
}
