/*
 * ReadOnlyShapeFileDataSource.java
 *
 */

package gistoolkit.datasources.shapefile;

import java.io.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.datasources.*;


/**
 * Reads a single shape file.
 */
public class ReadOnlyShapeFileDataSource extends SimpleDataSource{
    /** The fully qualified path to the shape file.*/
    private String myFileName;
    
    /** The last time the file was accessed. */
    private long myFileDate = 0;
    
    /** The envelope of the file. */
    private Envelope myEnvelope = null;
    
    /** Creates a new instance of ReadOnlyShapeFileDataSource */
    public ReadOnlyShapeFileDataSource() {
    }
    
    /** Creates a new instance of the ReadOnlyShapeFileDataSource with this file as the input. */
    public  ReadOnlyShapeFileDataSource(String inFileName)throws FileNotFoundException{
        // check that the file is there
        File tempFile = new File(inFileName);
        if (!tempFile.exists()){
            throw new FileNotFoundException(inFileName);
        }
        
        // set this as the active file name
        myFileName = inFileName;
    }
    
    /**
     * Reads only the objects from the data source that intersect this envelope.
     */
    public synchronized GISDataset readDataset(Envelope inEnvelope) throws Exception {
        if (!checkDate()) clearCache();
        return super.readDataset(inEnvelope);
    }

    /** Checks if the file date has changed since the last time we read it, returns true if it is the same.*/
    public boolean checkDate(){
        File tempFile = new File(myFileName);
        long tempLastModified = tempFile.lastModified();
        return myFileDate == tempLastModified;
    }
        
    
    /** Set the file name for the data source do not include the extension.*/
    public void setFileName(String inName) throws Exception{
        
        // String indicating the location of the shape file
        String tempName = inName;
        
        // check for the shp ending
        if ((inName.endsWith(".shp")) || (inName.endsWith(".SHP"))) {
            tempName = inName.substring(0, tempName.length() - 4);
        }
        // check for the shx ending
        if ((inName.endsWith(".shx")) || (inName.endsWith(".SHX"))) {
            tempName = inName.substring(0, tempName.length() - 4);
        }
        // check for the dbf ending
        if ((inName.endsWith(".dbf")) || (inName.endsWith(".DBF"))) {
            tempName = inName.substring(0, tempName.length() - 4);
        }
        
        // save the name
        myFileName = tempName;
    }

    /** Constant to use in the configuration information file for the file name. */
    private static final String FILE_NAME = "FileName";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("ShapeFileDatasource");
        tempRoot.addAttribute(FILE_NAME, myFileName);
        return tempRoot;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception("No Configuration information for ShapeFileDataSource");
        super.setNode(inNode);
        String tempString = inNode.getAttribute(FILE_NAME);
        if (tempString == null) throw new Exception("No File specified for ShapeFileDataSource");
        myFileName = tempString;
        setFileName(tempString);
    }

    /** Returns the bounding rectangle of all the shapes in the Data Source. */
    public Envelope readEnvelope() throws Exception {
        if ((myEnvelope == null)||(!checkDate())){
            ShapeFileReader tempReader = new ShapeFileReader(myFileName);
            myEnvelope = tempReader.getHeader().getFileEnvelope();
            clearCache();
        }
        return myEnvelope;
    }
    
    /** This method should return the shapes from the data source  */
    protected GISDataset readShapes(Envelope inEnvelope) throws Exception {
        GISDataset tempDataset = new GISDataset();
        if (inEnvelope == null){
            ShapeFileReader tempReader = new ShapeFileReader(myFileName);
            Record tempRecord = tempReader.read();
            while (tempRecord != null){
                tempDataset.add(tempRecord);
                tempRecord = tempReader.read();
            }
        }
        else{
            EnvelopeShapeFileReader tempReader = new EnvelopeShapeFileReader(myFileName);
            Record tempRecord = tempReader.read(inEnvelope);
            Polygon tempPolygon = inEnvelope.getPolygon();
            while (tempRecord != null){
                if (tempRecord.getShape() != null){
                    Shape tempShape = tempRecord.getShape();
                    if (inEnvelope.contains(tempShape.getEnvelope())){
                        tempDataset.add(tempRecord);
                    }
                    else{
                        if (tempPolygon.intersects(tempShape)){
                            tempDataset.add(tempRecord);
                        }
                    }
                }
                tempRecord = tempReader.read(inEnvelope);
            }
        }
        File tempFile = new File(myFileName);
        myFileDate = tempFile.lastModified();
        return tempDataset;
    }    
}
