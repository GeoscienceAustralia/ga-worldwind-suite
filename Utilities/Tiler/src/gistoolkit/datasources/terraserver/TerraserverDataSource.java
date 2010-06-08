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

package gistoolkit.datasources.terraserver;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.projection.*;
import gistoolkit.datasources.*;
import gistoolkit.datasources.filter.*;

/**
 * Allows the retrieval of image data from terraserver.
 * @author  ithaqua
 */
public class TerraserverDataSource implements DataSource, RasterDatasource{
    // Output stream for keeping track of performance metrics.
    private PrintStream myPerformanceStream = null;
    public void setPerformanceOutputStream(PrintStream inOutputStream){myPerformanceStream = inOutputStream;}
    private void logPerformance(String inString){
        myPerformanceStream.print(new Date().toString());
        myPerformanceStream.print("-");
        myPerformanceStream.println(inString);
    }
    
    /** Creates new TerraserverDataSource */
    public TerraserverDataSource() {
    }
    public static final int TILE_WIDTH=200;
    public static final int TILE_HEIGHT=200;
    /**
     * Terraserver only has a set number of resolutions available
     * these are associated with scale values, and must increase in the array.
     * this datasource will automatically chose the closest one
     */
    private static int[] myDOQResolutions = {1,2,4,8,16,32,64};
    /** Retrieve the list of valid resolutions */
    public static int[] getDOQResolutions(){return myDOQResolutions;}
    
    /**
     * Terraserver only has a set number of resolutions available,
     * these are the associated scale values, and must correspond with the
     * items in the resolutions array.
     */
    private static int[] myDOQScaleValues = {10,11,12,13,14,15,16};
    
    /**
     * Terraserver only has a set number of resolutions available
     * these are associated with scale values, and must increase in the array.
     * this datasource will automatically chose the closest one
     */
    private static int[] myDRGResolutions = {2,4,8,16,32,64,128,256,512};
    /** Retrieve the list of valid resolutions */
    public static int[] getDRGResolutions(){return myDRGResolutions;}
    
    /**
     * Terraserver only has a set number of resolutions available,
     * these are the associated scale values, and must correspond with the
     * items in the resolutions array.
     */
    private static int[] myDRGScaleValues = {11,12,13,14,15,16,17,18,19};
    
    /** Tye type of image to retrieve, DOQ, or DRG */
    private int myImageType = 1;
    public static final int DOQ = 1;
    public static final int DRG = 2;
    /** Return the type of image to retrieve, choices are DOQ, or DRG */
    public int getImageType(){return myImageType;}
    /** Set the type of image to retrieve, choices are DOQ, or DRG */
    public void setImageType(int inImageType){myImageType = inImageType;}
    
    /** The resolution to use for this image */
    private int myResolution = 0; // zero indicates that I should guess.
    /** Set the resolution to use.  Zero indicates that I should use the best guess. */
    public void setResolution(int inResolution) throws Exception{
        if (inResolution == 0) {
            myResolution = 0;
            return;
        }
        String tempString = "";
        String tempType = "";
        int[] tempResolutions = null;
        int[] tempScaleValues = null;
        if (myImageType == DOQ){
            tempType = "DOQ";
            tempResolutions = myDOQResolutions;
            tempScaleValues = myDOQScaleValues;
        }
        else{
            tempType = "DRG";
            tempResolutions = myDRGResolutions;
            tempScaleValues = myDRGScaleValues;
        }
        for (int i=0; i<tempResolutions.length; i++){
            if (i>0) tempString = tempString + ", ";
            tempString = tempString + ""+tempResolutions[i];
            if (inResolution == tempResolutions[i]){
                myResolution = inResolution;
                return;
            }
        }
        throw new Exception("Imagery at "+inResolution+" meters is not available for "+tempType+", valid resolutions are "+tempString);
    }
    
    /**
     * Maximum retry time.
     * Occasionally, Terraserver will fail with a communications error of some time.  This parameter specifies
     * The maximum amount of time the applicaiton will spend retrying types while generating an image.
     */
    private int myMaxRetrySeconds = 0;
    /**
     * Set the Maximum retry time.
     * Occasionally, Terraserver will fail with a communications error of some time.  This parameter specifies
     * The maximum amount of time the applicaiton will spend retrying types while generating an image.
     */
    public void setMaxRetrySeconds(int inSeconds){myMaxRetrySeconds = inSeconds;}
    /**
     * Get the Maximum retry time.
     * Occasionally, Terraserver will fail with a communications error of some time.  This parameter specifies
     * The maximum amount of time the applicaiton will spend retrying types while generating an image.
     */
    public int getMaxRetrySeconds(){return myMaxRetrySeconds;}
    
    /** Keeps track of the number of miliseconds spent trying to retrieve tiles more than once.*/
    private long myCurrentRetryMiliseconds = 0;
    
    /**Name of this datasource.*/
    private String myName = "None";
    /** Return the name of this datasource for display to the user. */
    public String getName(){return myName;}
    /** Set the name of this datasource for display purposes. */
    public void setName(String inName){myName = inName;}
    
    /**
     * Sets the projection to use to convert from the storage media, source projection.
     * It is expected that this projection will be run in reverse, to reverse project already projected data,
     * and that it will not change often so it is OK for this to be done just once.
     * This can not be set for this projection because it uses the same from projection.
     */
    public void setFromProjection(Projection inProjection) throws Exception{
        // Terraserver always uses the same from projection.  It is UTM zones.
    }
    public Projection getFromProjection(){
        // Terraserver always uses the same from projection.  It is UTM zones.
        return null;
    }
    
    /** Projection to use to convert from the datasource projection or a basis generated by the From Projection to the projection used by the map. */
    private Projection myToProjection = new NoProjection();
    
    /**
     * Allows another projection to be used to convert to the screen projection.  The CacheProjected flag indicates to the
     * Data source that the to projection will not be changing often, and it is OK to project once and cache it.  Setting this
     * flag to false indicates to the DataSource that the toProjection will be changing often.*/
    public void setToProjection(Projection inProjection, boolean inCacheProjected) throws Exception{
        myEnvelope = null;
        myDataset = null;
        myToProjection = inProjection;
    }
    /** Return the projection this data source is to project to. */
    public Projection getToProjection(){return myToProjection;}
    
    /** Number of pixels in the width of the image */
    private int myImageWidth = 0;
    private int myCacheImageWidth = 0;
    /** Set the width of the image.  Images with a width of 0 or less will be optimized differently */
    public void setImageWidth(int inWidth) {myImageWidth = inWidth;}
    /** Return the width of the image to generate */
    public int getImageWidth() {return myImageWidth;}
    /** Number of pixels in the height of the image */
    private int myImageHeight = 0;
    private int myCacheImageHeight = 0;
    /** Set the height of the image.  Images with a height of 0 or less will be optimized differently */
    public void setImageHeight(int inHeight) {myImageHeight = inHeight;}
    /** Return the height of the image to generate */
    public int getImageHeight() {return myImageHeight;}
    
    /** Set the optimization level for this image.  If the images are relatively close in projection, then a large number can be used.  If not, use zero.*/
    private int myOptimize = 200;
    /** Set the optimization level for this image */
    public void setOptimization(int inOptimize){myOptimize = inOptimize;}
    /** Retrieve the optimization level for this image */
    public int getOptimization(){return myOptimize;}
    
    /** Envelope to use when caching data */
    private Envelope myEnvelope = null;
    
    /** Cached Dataset */
    private GISDataset myDataset = null;
    
    /**
     * Returns the bounding rectangle of all the shapes in the shape file.
     * This is more or less the entire USA, I am going to have to punt on this one.
     */
    public Envelope getEnvelope() throws Exception {
        return null;
    }
    
    /**
     * Update Terraserver, .... Ya Right
     * Update the data source with the changed record.
     */
    public void update(Record inRecord) throws Exception {
    }
    
    /**
     * Update Terraserver, .... Ya Right
     * Commit all changes since the last commit.
     */
    public void commit() throws Exception {
    }
    
    /**
     * Delete data from Terraserver, .... Ya Right
     * Delete this record from the database.
     */
    public void delete(Record inRecord) throws Exception {
    }
    
    /**
     * Update Terraserver, .... Ya Right
     * Inserts the given record into the datasource.
     */
    public void insert(Record inRecord) throws Exception {
    }
    
    /**
     * Update Terraserver, .... Ya Right  Rollbacks are completely out of the question
     * Rollback any changes to this datasource since the last commit.
     */
    public void rollback() throws Exception {
    }
    
    /**
     * Determines if this datasource is updateable.
     * Uh, NO, Like I am so shure.
     */
    public boolean isUpdateable() {
        return false;
    }
    
    /**
     * Reading all data from terraserver is not an option, nothing is returned.
     */
    public GISDataset readDataset() throws Exception {
        return new GISDataset();
    }
    
    /**
     * Reads only the items from the datasource that are within the envelope.
     */
    public GISDataset readDataset(Envelope inEnvelope) throws Exception {
        myCurrentRetryMiliseconds=0;
        TerraserverPerformanceLog tempLog = null;
        long tempStartTime = new Date().getTime();
        if (myPerformanceStream != null) tempLog = new TerraserverPerformanceLog(new StringBuffer());

        // the envelope sent in are in real world coordinates in my current projection.
        // ensure that we don't already have these envelope
        if (myEnvelope != null){
            if (myEnvelope.isEqual(myEnvelope)){
                if ((myImageWidth == myCacheImageWidth) && (myImageHeight == myCacheImageHeight)) return myDataset;
            }
        }
        
        // convert the envelope to UTM
        Point tempTopLeftPoint = new Point(inEnvelope.getMinX(), inEnvelope.getMaxY());
        Point tempTopRightPoint = new Point(inEnvelope.getMaxX(), inEnvelope.getMaxY());
        Point tempBottomLeftPoint = new Point(inEnvelope.getMinX(), inEnvelope.getMinY());
        Point tempBottomRightPoint = new Point(inEnvelope.getMaxX(), inEnvelope.getMinY());
        if (myToProjection != null){
            myToProjection.projectBackward(tempTopLeftPoint);
            myToProjection.projectBackward(tempTopRightPoint);
            myToProjection.projectBackward(tempBottomLeftPoint);
            myToProjection.projectBackward(tempBottomRightPoint);
        }
        
        // figure out how many tiles it is going to take to cover this area
        UniversalTransverseMercatorProjection tempUTMProjection = new UniversalTransverseMercatorProjection();
        
        // find the center of the map
        Point tempCenter = new Point((tempTopLeftPoint.getX() + tempBottomRightPoint.getX())/2, (tempTopLeftPoint.getY()+tempBottomRightPoint.getY())/2);
        
        // set the zone to the center of the map. The getUTMZone takes parameters (Latitude, and Longitude);
        int tempZone = tempUTMProjection.getUTMZone(tempCenter.getY(), tempCenter.getX());
        
        // determine if the zone changes within the map
        boolean tempZoneChange = false; {
            int tempZone1 = tempUTMProjection.getUTMZone(tempTopLeftPoint.getY(), tempTopLeftPoint.getX());
            int tempZone2 = tempUTMProjection.getUTMZone(tempTopRightPoint.getY(), tempTopRightPoint.getX());
            int tempZone3 = tempUTMProjection.getUTMZone(tempBottomLeftPoint.getY(), tempBottomLeftPoint.getX());
            int tempZone4 = tempUTMProjection.getUTMZone(tempBottomRightPoint.getY(), tempBottomRightPoint.getX());
            if ((tempZone1 == tempZone2) && (tempZone2 == tempZone3) && (tempZone3 == tempZone4)) tempZoneChange = false;
            else {
                tempZoneChange = true;
            }
        }
        tempUTMProjection.setZone(tempZone);
        int tempOptimize = myOptimize;
        if (tempZoneChange) {
            tempOptimize = 0; // if the zones change, eliminate the optimization.
            if (tempLog != null) tempLog.log("Zone Change Detected, no Optimization");
        }
        
        // project forward
        tempUTMProjection.projectForward(tempTopLeftPoint);
        tempUTMProjection.projectForward(tempTopRightPoint);
        tempUTMProjection.projectForward(tempBottomLeftPoint);
        tempUTMProjection.projectForward(tempBottomRightPoint);
        double tempArea = (Math.abs(tempTopLeftPoint.getX() - tempBottomRightPoint.getX()) * (Math.abs(tempTopLeftPoint.getY()-tempBottomRightPoint.getY())));
        
        int tempMeterAcuracy = -1;
        int tempScaleValue = -1;
        int[] tempResolutions = null;
        int[] tempScaleValues = null;
        if (myImageType == DOQ){
            tempResolutions = myDOQResolutions;
            tempScaleValues = myDOQScaleValues;
        }
        else{
            tempResolutions = myDRGResolutions;
            tempScaleValues = myDRGScaleValues;
        }
        
        // the geometry of the image
        int tempWidth = 0;
        int tempHeight = 0;
        double tempIncXDistance = 0;
        double tempIncYDistance = 0;
        
        if ((myImageWidth <= 0) || (myImageHeight <=0)){
            System.out.println("No Image Height or Width, Using Resolution");
            // the user has specified a resolution to use.
            if (myResolution > 0){
                System.out.println("Using Supplied Resolution = "+myResolution);
                for (int i=0; i<tempResolutions.length; i++){
                    if (tempResolutions[i] == myResolution){
                        tempMeterAcuracy = tempResolutions[i];
                        tempScaleValue = tempScaleValues[i];
                        break;
                    }
                }
            }
            else{
                // the images from terraserver are in 200 pixel squares, so I want to use no more than 36 squares.
                int tempAcuracy = (int) tempArea / (36*TILE_WIDTH*TILE_HEIGHT);
                
                // find the resolution and scale value
                for (int i=0; i<tempResolutions.length; i++){
                    tempMeterAcuracy = tempResolutions[i];
                    tempScaleValue = tempScaleValues[i];
                    if (tempAcuracy <= tempResolutions[i]){
                        break;
                    }
                }
                
                System.out.println("Using Calculated Resolution of "+tempMeterAcuracy);
            }
            // find the number of increments
            int tempNumInc = (int) Math.abs(tempTopLeftPoint.getX()-tempTopRightPoint.getX()) / tempMeterAcuracy;
            
            // find the increment distance.
            tempIncXDistance = Math.abs(inEnvelope.getMinX() - inEnvelope.getMaxX())/tempNumInc;
            tempIncYDistance = Math.abs(inEnvelope.getMaxY() - inEnvelope.getMinY())/tempNumInc;
            
            // Calculate the width and height of the image
            tempWidth = (int) (Math.abs(inEnvelope.getMinX()-inEnvelope.getMaxX())/tempIncXDistance);
            tempHeight = (int) (Math.abs(inEnvelope.getMaxY()-inEnvelope.getMinY())/tempIncYDistance);
        }
        else {
            System.out.println("Using Supplied Width = "+myImageWidth+" and Height = "+myImageHeight);
            
            // the width and height are given.
            tempWidth = myImageWidth;
            tempHeight = myImageHeight;
            
            // find the increment distance.
            tempIncXDistance = Math.abs(inEnvelope.getMinX()-inEnvelope.getMaxX())/tempWidth;
            tempIncYDistance = Math.abs(inEnvelope.getMaxY()-inEnvelope.getMinY())/tempHeight;
            
            // find the meter acuracy and scale value
            double myIdealResolution =  (Math.abs(tempTopLeftPoint.getX()-tempBottomRightPoint.getX())/tempWidth) * 0.833;  // fudge factor, works for ~6 degreese of rotaiton between the projections.
            int tempIndex = 0;
            for (int i=0; i<tempResolutions.length; i++){
                tempMeterAcuracy = tempResolutions[i];
                tempScaleValue = tempScaleValues[i];
                if (i+1 <tempResolutions.length){
                    if (myIdealResolution <= tempResolutions[i+1]){
                        break;
                    }
                }
            }
        }

        if (tempLog != null) tempLog.log("Width="+tempWidth+" Height="+tempHeight);
        int[] tempImageArray = new int[tempWidth*tempHeight];
        
        // double loop through the image getting the pixels
        Point tempPoint = new Point(0,0);
        Point tempNextPoint = new Point(0,0);
        Date tempDate = new Date();
        System.out.println("Time = "+ tempDate +" Optimizing at "+tempOptimize+" Pixels");
        for (int i=0; i<tempHeight; i++){
            for (int j=0; j<tempWidth; j++){
                tempPoint.setX(inEnvelope.getMinX()+j*tempIncXDistance+tempIncXDistance/2);
                tempPoint.setY(inEnvelope.getMaxY()-i*tempIncYDistance-tempIncYDistance/2);
                if (myToProjection != null) myToProjection.projectBackward(tempPoint);
                if (tempZoneChange){
                    int tempTestZone = tempUTMProjection.getUTMZone(tempPoint.getY(), tempPoint.getX());
                    if (tempTestZone != tempZone){
                        tempZone = tempTestZone;
                        tempUTMProjection.setZone(tempZone);
                    }
                }
                tempUTMProjection.projectForward(tempPoint);
                
                // don't do all the projections
                if (tempOptimize > 1){
                    int tempNextProject = j+myOptimize;
                    if (tempNextProject > tempWidth) tempNextProject = tempWidth;
                    tempNextPoint.setX(inEnvelope.getMinX()+tempNextProject*tempIncXDistance+tempIncXDistance/2);
                    tempNextPoint.setY(inEnvelope.getMaxY()-i*tempIncYDistance-tempIncYDistance/2);
                    if (myToProjection != null) myToProjection.projectBackward(tempNextPoint);
                    tempUTMProjection.projectForward(tempNextPoint);
                    double tempXInc = (tempNextPoint.getX()-tempPoint.getX())/(tempNextProject-j);
                    double tempYInc = (tempNextPoint.getY()-tempPoint.getY())/(tempNextProject-j);
                    int end = tempNextProject-j;
                    for (int k=0; k<end; k++){
                        double tempX = (tempPoint.getX()+tempXInc*k);
                        double tempY = (tempPoint.getY()+tempYInc*k);
                        int tempColor = getColor(tempX, tempY, tempZone, tempMeterAcuracy, tempScaleValue, tempLog);
                        tempImageArray[(i*tempWidth)+(j+k)] = tempColor;
                    }
                    j = j+end-1;
                }
                // perform each projection.
                else{
                    int tempColor = getColor(tempPoint.getX(), tempPoint.getY(), tempZone, tempMeterAcuracy, tempScaleValue, tempLog);
                    tempImageArray[(i*tempWidth)+(j)] = tempColor;
                }
            }
        }
        Date endDate = new Date();
        long tempEndTime = endDate.getTime();
        if (tempLog != null) tempLog.end();        
        System.out.println("Time = "+endDate+" Finished Projecting time="+(tempDate.getTime()-endDate.getTime()));
        if (myPerformanceStream != null) myPerformanceStream.println(tempLog.toString());
        // return the graphics object
        BufferedImage tempBufferedImage = new BufferedImage(tempWidth, tempHeight, BufferedImage.TYPE_INT_ARGB);
        tempBufferedImage.setRGB(0,0,tempWidth, tempHeight, tempImageArray, 0, tempWidth);
        RasterShape tempShape = new RasterShape(inEnvelope, tempBufferedImage);
        Record tempRecord = new Record();
        
        String[] tempAttributeNames = {"Terraserver"};
        tempRecord.setAttributeNames(tempAttributeNames);
        Object[][] tempAttributeValues = new Object[1][1];
        tempAttributeValues[0][0] = "Terraserver";
        tempRecord.setAttributes(tempAttributeValues);
        tempRecord.setShape(tempShape);
        GISDataset tempDataset = new GISDataset();
        tempDataset.add(tempRecord);
        myDataset = tempDataset;
        inEnvelope = (Envelope) inEnvelope.clone();
        myCacheImageWidth = myImageWidth;
        myCacheImageHeight = myImageHeight;
        return tempDataset;
    }
    
    /**
     * Adds a datasource listener to this datasource.
     */
    public void addDataSourceListener(DataSourceListener inDataSourceListener) {
    }
    
    /**
     * Removes the datasource lisener from this datasource.
     */
    public void removeDataSourceListener(DataSourceListener inDataSourceListener) {
    }
    
    /**
     * Initialize the data source from the properties.
     */
    public void load(Properties inProperties) {
    }
    
    /** The location of terraserver */
    private String myURLBase = "http://terraserver-usa.com/tile.ashx";
    /** Returns the URL location of the tile.asp script on terraserver */
    public String getURLBase(){return myURLBase;}
    /** Sets the URL location of the tile.asp script on terraserver */
    public void setURLBase(String inURLBase){myURLBase = inURLBase;}
    
    private int myLastX=0;
    private int myLastY=0;
    private int myLastZ=0;
    private int[] myLastPixels=null;
    /**
     * returns the color of the pixel at the given location in UTM coordinates.
     */
    public int getColor(double inX, double inY, int inUTMZone, int inResolution, int inScaleFactor, TerraserverPerformanceLog inLog) throws MalformedURLException{
        int tilewidth = TILE_WIDTH; // width of a tile in pixels
        int tileheight = TILE_HEIGHT; // height of a tile in pixels
        
        // convert the UTM coordinates to tile coordinates
        int x = (int) inX/(tilewidth*inResolution);
        int y = (int) inY/(tileheight*inResolution);
        int z = inUTMZone;
        int[] pixels = null;
        
        // Is this the last tile used, if yes then use it again
        if ((x == myLastX)&&(y == myLastY)&&(z == myLastZ)&&(myLastPixels!=null)){
            pixels = myLastPixels;
        }
        else {
            // must cache these tiles, to prevent retrieving the same one 40,000 times.
            // Construct the URL to search for it in the cache.
            pixels = getCache(myImageType, z, inScaleFactor, x, y, inLog);
            
            if (pixels == null){
                long tempStart = new Date().getTime();
                String tempString = "S="+inScaleFactor+"&T="+myImageType+"&X="+x+"&Y="+y+"&Z="+z;
                String tempURL = myURLBase+"?"+tempString;
                pixels = addCache(tilewidth, tileheight, tempURL, myImageType, z, inScaleFactor, x, y);
                long tempEnd = new Date().getTime();
                if (inLog != null) {
                    inLog.readFromServer(tempEnd - tempStart);
                    inLog.log("Requesting Image "+ tempURL);
                }
            }
            myLastX = x;
            myLastY = y;
            myLastZ = z;
            myLastPixels = pixels;
            
        }
        // UTM location of the upper lefthand courner.
        double tempX = x*(inResolution*tilewidth);
        double tempY = y*(inResolution*tileheight);
        
        // the X coordinate of the pixel is
        int pixelX = (int) (inX-tempX)/inResolution;
        int pixelY = (int) (inY-tempY)/inResolution;
        pixelY = tileheight-pixelY-1;
        pixelX = Math.abs(pixelX);
        
        // could and probably will do some fancy averaging here.
        int tempIndex = pixelY*tileheight + pixelX;
        if ((tempIndex >= pixels.length)||(tempIndex < 0)){
            return Color.white.getRGB();
        }
        return pixels[tempIndex];
    }
    
    // the memory cache.
    private Hashtable myCache = new Hashtable(9);
    
    /** This vector acts as a queue allowing the removal of the oldest tiles */
    private Vector myVectCache = new Vector();
    
    /** The number of tiles to keep in memory */
    private int myMemoryCache = 200;
    /** Set the number of tiles to keep in memory.  Each tile is 200x200 integers, so the amount of memory in bytes is 200x200x2 or 80,000 bytes for each tile.*/
    public void setMemoryCache(int inMemoryCache){myMemoryCache = inMemoryCache;}
    /** Return the number of tiles to keep in memory. */
    public int getMemoryCache(){return myMemoryCache;}
    
    /** Location of the disk cache.  Since downloading imagery several times can be burdensom on the network and computing resources involved, the disk cache may assist */
    public String myDiskCache = null;
    /** Set the location on the disk where the caching of tiles should take place */
    public void setDiskCache(String inDirectory){
        myDiskCache = null;
        if (inDirectory == null) return;
        File tempFile = new File(inDirectory);
        if (tempFile.isDirectory() && tempFile.canWrite()) myDiskCache = inDirectory;
    }
    /** Return the location on the disk where the caching of tiles should take place */
    public String getDiskCache(){return myDiskCache;}
    
    /** Construct the file name */
    private String getFileName(int inTheme, int inZone, int inScale, int inX, int inY){
        int tempScale = inScale;
        String tempString = "T="+inTheme+File.separatorChar+"Z="+inZone+File.separatorChar+"S="+inScale+File.separatorChar+"X="+inX+"Y="+inY;
        return tempString;
    }
    
    /** Add the contents of the URL to the cache directory */
    private int[] addCache(int intilewidth, int intileheight, String inURL, int inTheme, int inZone, int inScale, int inX, int inY){
        
        // create the pixels
        int[] pixels = new int[intilewidth * intileheight];
        String tempName = getFileName(inTheme, inZone, inScale, inX, inY);
        try {
            Image tempImage = null;
            if (myDiskCache != null){
                // construct the file name
                String tempFileName = myDiskCache + File.separatorChar + tempName;
                // ensure that the directories exist
                int tempIndex = tempFileName.lastIndexOf(File.separatorChar);
                if (tempIndex != -1){
                    File tempFile = new File(tempFileName.substring(0,tempIndex));
                    if (!tempFile.exists()){
                        tempFile.mkdirs();
                    }
                }
                
                // write the image to the file.
                byte[] tempBuffer = new byte[1000];
                System.out.print("Requesting Image "+inURL);
                long tempStartTime = new Date().getTime();
                boolean myTryOnce = true;                
                byte[] tempImageData = null;
                while ((myCurrentRetryMiliseconds <= myMaxRetrySeconds*1000) || (myTryOnce)){
                    try{
			myTryOnce = false;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			URL tempURL = new URL(inURL);
			URLConnection tempConnection = tempURL.openConnection();
			tempConnection.setUseCaches(false);
			InputStream in = tempConnection.getInputStream();
			int length = in.read(tempBuffer);
			while (length > -1){
				bout.write(tempBuffer, 0, length);
				length = in.read(tempBuffer);
			}
			bout.close();
			in.close();
			tempImageData = bout.toByteArray();
			tempImage = java.awt.Toolkit.getDefaultToolkit().createImage(tempImageData);
			
			// try to write the data to disk.
			File tempDirectory = new File(myDiskCache);
			if (tempDirectory.exists() && tempDirectory.isDirectory()){
				try{
					FileOutputStream fout = new FileOutputStream(tempFileName);
					fout.write(tempImageData);
					fout.close();
				}
				catch (IOException e){
					// fail silently
				}
			}
			break;
                    }
                    catch (Exception e){
                    	System.out.println("Error "+e);
                        myCurrentRetryMiliseconds = myCurrentRetryMiliseconds + (new Date().getTime() - tempStartTime);
                        File tempFile = new File(tempFileName);
                        tempFile.delete();
                    }
                }
            }
            else tempImage = java.awt.Toolkit.getDefaultToolkit().createImage(inURL);
            
            // Retrieve the pixels from the image.
            for (int i=0; i<pixels.length; i++) pixels[i] = 0;
            if (tempImage != null){
                PixelGrabber pg = new PixelGrabber(tempImage, 0, 0, intilewidth, intileheight, pixels, 0, intilewidth);
                try {
                    pg.grabPixels();
                    if (!pg.grabPixels()) {
                        System.err.println("Terraserver Exception image fetch aborted or errored on "+inURL);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Terraserver Exception interrupted waiting for pixels from "+inURL);
                }
                System.out.println(" Recieved " + pixels.length + " Pixels");
            }
        }
        catch (Exception e){
            System.out.println("Exception = "+e);
            e.printStackTrace();
            int tempInt = Color.white.getRGB();
            for (int i=0 ;i<pixels.length; i++){
                pixels[i] = tempInt;
            }
        }
        addMemoryCache(tempName, pixels);
        return pixels;
    }
    
    private void addMemoryCache(String inName, int[] inPixels){
        // add the tile to the memory cache.
        myCache.put(inName, inPixels);
        myVectCache.addElement(inName);
        
        // if the cache is too large, remove the oldest ones.
        if (myVectCache.size() > myMemoryCache){
            while(myVectCache.size() > myMemoryCache){
                Object tempKey = myVectCache.remove(0);
                myCache.remove(tempKey);
            }
        }
    }
    
    private int[] getCache(int inTheme, int inZone, int inScale, int inX, int inY, TerraserverPerformanceLog inLog){
        
        // search for it in the memory cache
        String tempName = getFileName(inTheme, inZone, inScale, inX, inY);
        Enumeration e = myCache.keys();
        while (e.hasMoreElements()){
            String tempString = (String) e.nextElement();
            if (tempString != null){
                if (tempString.equals(tempName)){
                    return (int[]) myCache.get(tempString);
                }
            }
        }
        
        // search for it in the disk cache
        if (myDiskCache != null){
            long tempStart = new Date().getTime();
            File tempFile = new File(myDiskCache + File.separatorChar + tempName);
            if (tempFile.exists()){
                try{
                    Image tempImage = java.awt.Toolkit.getDefaultToolkit().createImage(tempFile.getAbsolutePath());
                    
                    // find the color at this location.
                    int[] pixels = new int[TILE_WIDTH * TILE_HEIGHT];
                    PixelGrabber pg = new PixelGrabber(tempImage, 0, 0, TILE_WIDTH, TILE_HEIGHT, pixels, 0, TILE_WIDTH);
                    pg.grabPixels();
                    if (pg.grabPixels()) {
                        addMemoryCache(tempName, pixels);
                        System.out.println("Read from disk "+tempFile);

                        long tempEnd = new Date().getTime();
                        if (inLog != null){
                            inLog.readFromDisk(tempEnd-tempStart);
                            inLog.log("Read from disk "+tempFile);
                        }
                        return pixels;
                    }                    
                }
                catch (Exception ex){
                }
                
            }
            
        }
        return null;
    }
    
    private static final String OPTIMIZATION = "Optimization";
    private static final String RESOLUTION = "Resolution";
    private static final String URL_BASE = "URLBase";
    private static final String DISK_CACHE = "DiskCache";
    private static final String DATA_SOURCE_NAME = "DataSourceName";
    private static final String THEME = "Theme";
    
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = new Node("TerraserverDataSource");
        tempRoot.addAttribute(OPTIMIZATION, ""+myOptimize);
        tempRoot.addAttribute(RESOLUTION, ""+myResolution);
        tempRoot.addAttribute(URL_BASE, myURLBase);
        tempRoot.addAttribute(DISK_CACHE, myDiskCache);
        tempRoot.addAttribute(DATA_SOURCE_NAME, myName);
        tempRoot.addAttribute(THEME, ""+myImageType);
        return tempRoot;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception("No Configuration INformation for TerraserverDataSource");
        String tempName = DATA_SOURCE_NAME;
        String tempValue = inNode.getAttribute(tempName);
        if (tempValue != null) myName = tempValue;
        try{
            tempName = THEME;
            tempValue = inNode.getAttribute(tempName);
            if (tempValue != null){
                myImageType = Integer.parseInt(tempValue);
            }
            tempName = OPTIMIZATION;
            tempValue = inNode.getAttribute(tempName);
            if (tempValue != null){
                myOptimize = Integer.parseInt(tempValue);
            }
            tempName = RESOLUTION;
            tempValue = inNode.getAttribute(tempName);
            if (tempValue != null){
                myResolution = Integer.parseInt(tempValue);
            }
            tempName = URL_BASE;
            tempValue = inNode.getAttribute(tempName);
            if (tempValue != null){
                myURLBase = tempValue;
            }
            tempName = DISK_CACHE;
            tempValue = inNode.getAttribute(tempName);
            if (tempValue != null){
                myDiskCache = tempValue;
            }
        }
        catch (Exception e){
            throw new Exception("Error reading value for "+tempName+" for TerraserverDataSource");
        }
    }
    
    /** Terraserver datasource does not use filters, nothing is done with these.  */
    public void setFilter(Filter inFilter) {}
    
    /** Terraserver datasource does not use filters, always returns null.  */
    public Filter getFilter() {return null;}
    
    /** Get the style to use with this datasource.  */
    public gistoolkit.display.Style getStyle() {
        return null;
    }    
}
