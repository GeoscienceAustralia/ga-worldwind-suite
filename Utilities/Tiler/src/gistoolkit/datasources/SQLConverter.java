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

import java.sql.*;
import gistoolkit.features.*;

/**
 * Class to convert java types and AttributeTypes to SQL for a particular database.
 */
public interface SQLConverter {
    
    /** Convert the given ODBC type of data to the GISToolkit type of data. */
    public AttributeType getAttributeType(ResultSetMetaData inrmet, int inIndex)throws SQLException;
    
    /**
     * Take the given type, of the given value, and convert it to the sql string needed for insertion into the database.
     * In the case of strings, this routine will add the appostraphies as needed.
     */
    public String toSQL(Object inObject, AttributeType inAttributeType, int inJDBCType);
    
    /** Convert the object to a JDBC Character representation for the database.  It will add any delimiting characters like the beginning and ending appostraphies, as well as converting the embedded appostraphies to duplicate appostraphies. */
    public String toSQLString(Object inObject, AttributeType inAttributeType);
    
    /** Convert the object to the JDBC Date representaion for the database.  It will add any delimited characters, or functions to the database like, to_Date(format, date string) stuff. */
    public String toSQLDate(Object inObject, AttributeType inAttributeType);
    
    /** Convert a java date into the JDBC date representaion for this database. */
    public String toSQLDate(java.util.Date inDate, AttributeType inAttributeType);
    
    /** Convert a string into the JDBC date representaion for this database. */
    public String toSQLDate(String inDate, AttributeType inAttributeType);
    
    /** Convert an object into the JDBC Integer representaion for this database. */
    public String toSQLInteger(Object inInteger, AttributeType inAttributeType);
    
    /** Convert an object into the JDBC decimal representaion for this database. */
    public String toSQLDecimal(Object inDecimal, AttributeType inAttributeType);
}
