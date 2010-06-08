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

package gistoolkit.datasources.seamless;

import java.io.*;
import java.util.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.datasources.*;
import gistoolkit.datasources.shapefile.*;

/**
 * Seamless data sources are just a way of indexing a lot of different shapes in an efficient way.
 * The seamless table contains a series of polygons that are then used to retrieve data from the other
 * shape files.
 * The seamless shape file is a shape file that contains two columns.  The name column is the location of the shapes that actually exist in that location.  The
 */
public class SeamlessDataSource extends SimpleDataSource{
    
    /** Name to display to the user */
    private String myName = "";
    
    /** Index of where to find these shapes */
    private Envelope[] myIndexEnvelope = null;
    private Shape[] myIndexShapes = null;
    private String[] myTableNames = null;
    private String[] myTablePaths = null;
    
    /** Envelope of the index. */
    private Envelope myDataEnvelope = null;
    
    /** Location of the seamless table */
    private String myFileName = "";
    
    /** Creates new SeamlessDataSource */
    public SeamlessDataSource() {
    }
    
    /** Creates new SeamlessDataSource */
    public SeamlessDataSource(String inFileName) {
        setFileName(inFileName);
    }

    /** Set the file name of the index file */
    public void setFileName(String inFileName){
        if ((myName == null) || (myName.length() == 0)) myName = new File(inFileName).getName();
        myFileName = inFileName;
    }
    
    /** Read the index of the shape files and assemble them */
    private void readIndex() throws Exception{
        if (myFileName != null){
            ShapeFileDataSource tempDataSource = new ShapeFileDataSource(myFileName);
            GISDataset tempIndexDataset = tempDataSource.readDataset();
            myDataEnvelope = tempIndexDataset.getEnvelope();
            
            // distill out what we need
            ArrayList tempListEnvelope = new ArrayList();
            ArrayList tempListShapes = new ArrayList();
            ArrayList tempListTablenames = new ArrayList();
            ArrayList tempListPaths = new ArrayList();
            for (int i=0; i<tempIndexDataset.size(); i++){
                Record tempRecord = tempIndexDataset.getRecord(i);
                if (tempRecord != null){
                    Shape tempShape = tempRecord.getShape();
                    if (tempShape != null){
                        String tempTable = null;
                        String tempLocation = null;
                        String[] tempNames = tempRecord.getAttributeNames();
                        Object[] tempValues = tempRecord.getAttributes();
                        for(int j=0; j<tempNames.length; j++){
                            if (tempNames[j].equalsIgnoreCase("Table")){
                                tempTable = (String) tempValues[j];
                            }
                            if (tempNames[j].equalsIgnoreCase("Path")){
                                tempLocation = (String) tempValues[j];
                            }
                        }
                        tempListEnvelope.add(tempShape.getEnvelope());
                        tempListShapes.add(tempShape);
                        tempListTablenames.add(tempTable);
                        tempListPaths.add(tempLocation);
                    }
                }
            }
            
            // construct the arrays
            myIndexEnvelope = new Envelope[tempListShapes.size()];
            myIndexShapes = new Shape[tempListShapes.size()];
            myTableNames = new String[tempListShapes.size()];
            myTablePaths = new String[tempListShapes.size()];
            for (int i=0; i<tempListShapes.size(); i++){
                myIndexEnvelope[i] = (Envelope) tempListEnvelope.get(i);
                myIndexShapes[i] = (Shape) tempListShapes.get(i);
                myTableNames[i] = (String) tempListTablenames.get(i);
                myTablePaths[i] = (String) tempListPaths.get(i);
            }
        }
    }
            
    
    /**
     * Initialize the data source from the properties.
     */
    public void load(Properties inProperties) {
    }
    
    /**
     * Returns the identifier string for the datasource.
     */
    public String getName() {
        return myName;
    }
    
        
    /**Returns the bounding rectangle of all the shapes in the Data Source.*/
    public Envelope readEnvelope() throws Exception {
        // returns the Envelope of this layer.
        if (myIndexEnvelope == null) readIndex();
        if (myIndexEnvelope != null) return (Envelope) myDataEnvelope.clone();
        return null;
    }
    
    /**Sets an identifier string for the datasource.*/
    public void setName(String inName) {
        myName = inName;
    }
    
    /**Determines if this datasource is updateable.*/
    public boolean isUpdateable() {
        return false;
    }
    
    /** name of the node attribute for the file name */
    private static final String FILE_NAME = "FileName";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = new Node("SeamlessFileDatasource");
        tempRoot.addAttribute(FILE_NAME, myFileName);
        return tempRoot;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception ("No Configuration information for ShapeFileDataSource");
        String tempString = inNode.getAttribute(FILE_NAME);
        if (tempString == null) throw new Exception("No File specified for ShapeFileDataSource");
        myFileName = tempString;
        setFileName(tempString);
    }    
    
    /**
     * Reads only the objects from the data source that intersect these Envelope.
     */
    protected GISDataset readShapes(Envelope inEnvelope) throws Exception {
        if (inEnvelope == null) return new GISDataset();
        if (myIndexEnvelope == null) readIndex();
        if (myIndexEnvelope != null){
            
            ArrayList tempArrayListTables = new ArrayList();
            ArrayList tempArrayListPaths = new ArrayList();
            
            // check for null Envelope
            if (inEnvelope == null){
                for (int i=0; i<myIndexEnvelope.length; i++){
                    tempArrayListTables.add(myTableNames[i]);
                    tempArrayListPaths.add(myTablePaths[i]);
                }
            }
            else{            
                // loop through the dataset retrieving the required shapes.
                for (int i = 0; i < myIndexEnvelope.length; i++) {

                    // determine if the shape belongs in the dataset
                    if (inEnvelope.overlaps(myIndexEnvelope[i])) {
                        tempArrayListTables.add(myTableNames[i]);
                        tempArrayListPaths.add(myTablePaths[i]);
                    }
                }
            }
            
            // loop through the Index files and retrieve the actual data
            GISDataset tempReturnDataset = new GISDataset();            
            for (int i = 0; i < tempArrayListTables.size(); i++) {
                String tempTable = (String) tempArrayListTables.get(i);
                String tempLocation = (String) tempArrayListPaths.get(i);
                
                // locate the file
                String tempFileName = null;
                if ((tempTable != null) && (tempLocation != null)){
                                        
                    // correct the table should it need it.
                    if (tempTable.toUpperCase().endsWith(".SHP")) tempTable = tempTable.substring(tempTable.length()-4, tempTable.length());
                    
                    // correct the location should it need it.
                    if (tempLocation.indexOf("_") != -1) tempLocation = tempLocation.substring(tempLocation.indexOf("_")+1);
                    if (tempLocation.indexOf('\\') != -1) tempLocation = tempLocation.replace('\\',File.separatorChar);
                    if (!tempLocation.endsWith(""+File.separatorChar)) tempLocation += File.separatorChar;
                    
                    // try just appending the table and the name.
                    File tempFile = new File(tempLocation + File.separatorChar+ tempTable+".shp");
                    if (tempFile.exists()) tempFileName = tempFile.getAbsolutePath();
                    else{
                        //System.out.println("File did not exist");
                        // try using the index file as a local path
                        tempFile = new File(myFileName);
                        tempFile = tempFile.getParentFile();
                        if (tempFile.isDirectory()){
                            String tempPath = tempFile.getAbsolutePath() + File.separatorChar + tempLocation + tempTable + ".shp";
                            tempFile = new File(tempPath);
                            if (tempFile.exists()) tempFileName = tempFile.getAbsolutePath();
                        }
                    }
                }
                
                // read the file
                if (tempFileName != null){
                    System.out.println("Reading File :"+tempFileName);
                    EnvelopeShapeFileReader tempShapeFileReader = new EnvelopeShapeFileReader(tempFileName);
                    Record tempRecord = tempShapeFileReader.read(inEnvelope);
                    while (tempRecord != null){
                        tempReturnDataset.add(tempRecord);
                        tempRecord = tempShapeFileReader.read(inEnvelope);
                    }
                }
            }            
            return tempReturnDataset;
        }
        
        return new GISDataset();
    }    
    /** Get the style to use with this datasource.  */
    public gistoolkit.display.Style getStyle() {
        return null;
    }  
}
