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

package gistoolkit.datasources.db2spatialextender;

import java.sql.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.datasources.*;

/**
 *
 * Super class for the Spatial Extender DataSources.  Handles connecting to the database
 * retrieving the valid range of x and y values, and serialization of the connection parameters common to
 * SpatialExtenderDataSources.
 *
 */
public abstract class SpatialExtenderDataSource extends SimpleJDBCDataSource{
        
    /**DB2 supports Schemas, and this is the schema under which the data will reside.*/
    private String myDatabaseSchema = "Schemaname";
    /** Returns the name of the schema within the DB2 database where the data resides. */
    public String getDatabaseSchema() {return myDatabaseSchema;}
    /** Sets the name of the schema within the DB2 database where the data resides. */
    public void setDatabaseSchema(String inDatabaseSchema){myDatabaseSchema = inDatabaseSchema;}
        
    /** Creates new SpatialExtenderDataSource */
    public SpatialExtenderDataSource() {
        super();
        setName("DB2");
        setDatabaseDriver("COM.ibm.db2.jdbc.net.DB2Driver");
        setDatabaseURLBase("jdbc:db2");
        setDatabasePort(6789);
    }
    
    /**
     * DB2 stores all of these features as posative integers between 0 and the MAX_DB2_INT.
     * DB2 throws a nasty exception if the value is out of range, so this number is used
     * to move the bounds such that they can be represented within this range.
     * The maximum is an signed posative 32 bit integer = 2,147,483,647
     */
    public static long MAX_DB2_INT = Long.parseLong("2147483647");
    
    /** Only find the X and Y maximums once. */
    private boolean myFoundMaxMins = false;
    /** The maximum X value that DB2 can represent */
    private double myXMax = 1000000;
    /** The minimum X value that DB2 can represent */
    private double myXMin = 0;
    /** The maximum Y value that DB2 can represent */
    private double myYMax = 1000000;
    /** the minimum Y value that DB2 can represent */
    private double myYMin = 0;    
    
    /** Function to ensure that the X and Y of the point are within the allowable limits */
    protected Point checkMaxPoint(Point inPoint){
        if (inPoint.getX() > myXMax) inPoint.setX(myXMax);
        if (inPoint.getY() > myYMax) inPoint.setY(myYMax);
        if (inPoint.getX() < myXMin) inPoint.setX(myXMin);
        if (inPoint.getY() < myYMin) inPoint.setY(myYMin);
        return inPoint;
    }
    
    /**The Envelope of the dataset.*/
    protected Envelope myEnvelope = null;
            
    /** Returns the correctly formated URL for connecting to a db2 database. */    
    public String getJDBCURL(){return  getDatabaseURLBase()+"://"+getDatabaseServername()+":"+getDatabasePort()+"/"+getDatabaseName();}
    
    /**
     * Initializes the connection to the database.
     */
    public Connection connect() throws Exception{
        Connection tempConnection = null;
        if (!isConnected()){
            
            // check connection parameters
            if (getDatabaseSchema() == null) throw new Exception("No Database Schema defined");
                
            // Connect to the database
            tempConnection = super.connect();
            if (tempConnection != null){
                if (!myFoundMaxMins){
                    Statement tempStatement = tempConnection.createStatement();
                    try{                    
                        // try using the 8.0 view.
                        String tempQuery = "SELECT x_offset, y_offset, x_scale, y_scale FROM db2gse.ST_spatial_reference_systems WHERE srs_id "+getDatabaseSpatialReferenceID();
                        ResultSet rset = tempStatement.executeQuery(tempQuery);
                        rset.next();
                        double tempXOffset = rset.getDouble(1);
                        double tempYOffset = rset.getDouble(2);
                        double tempXScale = rset.getDouble(3);
                        double tempYScale = rset.getDouble(4);
                        myXMax = (tempXOffset + MAX_DB2_INT/tempXScale)-1;
                        myXMin = (tempXOffset);
                        myYMax = (tempYOffset + MAX_DB2_INT/tempYScale)-1;
                        myYMin = (tempYOffset);
                        myFoundMaxMins = true;
                        rset.close();
                        tempStatement.close();
                        tempStatement = null;
                    }
                    catch (Exception e){
                        // try using the 7.2 undocumented table.
                        String tempQuery = "SELECT falsex, falsey, xyunits FROM db2gse.spatial_ref_sys WHERE srid = "+getDatabaseSpatialReferenceID();
                        ResultSet rset = tempStatement.executeQuery(tempQuery);
                        rset.next();
                        double tempXOffset = rset.getDouble(1);
                        double tempYOffset = rset.getDouble(2);
                        double tempScale = rset.getDouble(3);
                        myXMax = (tempXOffset + MAX_DB2_INT/tempScale)-1;
                        myXMin = (tempXOffset);
                        myYMax = (tempYOffset + MAX_DB2_INT/tempScale)-1;
                        myYMin = (tempYOffset);
                        myFoundMaxMins = true;
                        rset.close();
                        tempStatement.close();
                        tempStatement = null;
                    }
                }            
            }
        }
        else{
            tempConnection = super.connect();
        }
        return tempConnection;
    }
    
    /**Returns the bounding rectangle of all the shapes in the Data Source.*/
    public Envelope readEnvelope() throws Exception {
        readDataset();
        return getCacheEnvelope();
    }        
    
    /** Get the where clause based on the Envelope */
    protected String getWhereString(Envelope inEnvelope){
        // if the Envelope form a point, then construct the point.
        if (inEnvelope.getMinX() == inEnvelope.getMaxX()){
            Point tempPoint = new Point(inEnvelope.getMinX(), inEnvelope.getMaxY());
            return "db2gse.ST_Intersects("+getDatabaseShapeColumn()+", db2gse.ST_PointFromText('"+tempPoint.getWKT()+"', db2gse.coordref()..srid("+getDatabaseSpatialReferenceID()+"))) = 1";
        }
        else{
            Point tempTopLeft = checkMaxPoint(new Point(inEnvelope.getMinX(), inEnvelope.getMaxY()));
            Point tempTopRight = checkMaxPoint(new Point(inEnvelope.getMaxX(), inEnvelope.getMaxY()));
            Point tempBottomLeft = checkMaxPoint(new Point(inEnvelope.getMinX(), inEnvelope.getMinY()));
            Point tempBottomRight = checkMaxPoint(new Point(inEnvelope.getMaxX(), inEnvelope.getMinY()));
            
            String tempWhereString = "\tdb2gse.ST_Intersects("+getDatabaseShapeColumn()+", "
            +"db2gse.ST_PolyFromText('polygon(("
            +tempTopLeft.getX()+" "+tempTopLeft.getY()+", "
            +tempTopRight.getX()+" "+tempTopRight.getY()+", "
            +tempBottomRight.getX()+" "+tempBottomRight.getY()+", "
            +tempBottomLeft.getX()+" "+tempBottomLeft.getY()+", "
            +tempTopLeft.getX()+" "+tempTopLeft.getY()+""
            +"))', db2gse.coordref()..srid("+getDatabaseSpatialReferenceID()+"))) = 1";
            return tempWhereString;
        }
    }
    
    /** Constant for saving and retrieving configuration information for these data sources. */
    private static final String SCHEMA = "Schema";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("DB2SpatialExtenderDataSource");
        
        // connection parameters
        tempRoot.addAttribute(SCHEMA, getDatabaseSchema());
        return tempRoot;
    }            
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception("Can not set up DB2 Datasource configuration information is null");
        super.setNode(inNode);
        String tempName = null;
        String tempValue = null;
        try{
            // connection parameters
            tempName = SCHEMA;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseSchema(tempValue);
        }
        catch (Exception e){
            throw new Exception("Can not read value for "+tempName+" to configure "+getName()+ " Arc SDE Datasource");
        }
    }    
    /** Returns the converter for this Database. */
    public SQLConverter getSQLConverter(){return new DB2SQLConverter();}  
}
