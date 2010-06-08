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

package gistoolkit.display.labeler;
import java.util.Vector;
import java.awt.*;
import java.awt.geom.*;
import java.text.*; //number format
import gistoolkit.common.*;
import gistoolkit.display.*;
import gistoolkit.features.*;

/**
 * Class for labeling an object with multiple attribute columns.
 */
public class MultipleLabeler extends SimpleLabeler{
    
    /** The string to place in between the labels. */
    private String myDelimiter = "";
    /** Get the string to place between the labels. */
    public String getDelimiter(){return myDelimiter;}
    /** Set the string to place between the labels. */
    public void setDelimiter(String inString){myDelimiter = inString;}
    
    /** Creates a new instance of MultipleLabeler */
    public MultipleLabeler() {
    }
    
    /** A name for this labeler, returns "Simple Labeler". */
    public String getLabelerName(){ return "Multiple Labeler";}
    
    /** Array of objects to label by. */
    private Vector myColumnAttributes = new Vector();
    
    
    /** Add the number of the column to label by.*/
    public void addLabelColumn(int inLabelColumn){
        addLabelColumn( inLabelColumn, ColumnAttributes.FORMAT_NONE, "","");
    }
    
    /** Add the number of the column, and it's attributes. */
    public void addLabelColumn(int inColumnNum, int inColumnFormat, String inColumnPreString, String inColumnPostString){
        ColumnAttributes tempColumnDesc = new ColumnAttributes(inColumnNum, inColumnFormat, inColumnPreString, inColumnPostString);
        myColumnAttributes.add(tempColumnDesc);
    }
    
    /** Get the number of label columns. */
    public int getCountLabelColumns(){return myColumnAttributes.size();}
    
    /** Return the information for the label columns.*/
    public ColumnAttributes getLabelAttributes(int inIndex){
        return (ColumnAttributes) myColumnAttributes.get(inIndex);
    }
    
    /** Clear all the label columns. */
    public void clearLabelColumns(){
        myColumnAttributes.clear();
    }
    
    /** reference to a polygon labeler. */
    private PolygonLabeler myPolygonLabeler = null;
    /** get a polygon labeler bassed on this labeler. */
    private PolygonLabeler getPolygonLabeler(){
        if (myPolygonLabeler != null) return myPolygonLabeler;
        myPolygonLabeler = new PolygonLabeler();
        myPolygonLabeler.setLabelOffset(getLabelOffset());
        myPolygonLabeler.setLabelOrientation(getLabelOrientation());
        return myPolygonLabeler;
    }
    /** reference to a line labeler. */
    private LineLabeler myLineLabeler = null;
    /** get a line labeler bassed on this labeler. */
    private LineLabeler getLineLabeler(){
        if (myLineLabeler != null) return myLineLabeler;
        myLineLabeler = new LineLabeler();
        myLineLabeler.setLabelOffset(getLabelOffset());
        myLineLabeler.setLabelOrientation(getLabelOrientation());
        return myLineLabeler;
    }
    private void reset(){
        myPolygonLabeler = null;
        myLineLabeler = null;
    }
    
    /**
     * Draw the label for the record on the graphics context
     */
    protected boolean drawLabel(Record inRecord,Graphics inGraphics,Converter inConverter) {
        
        String[] tempAttributeNames = inRecord.getAttributeNames();
        if (inRecord.getAttributes()[getLabelColumn()] != null){
            gistoolkit.features.Shape tempShape = inRecord.getShape();
            if (tempShape != null){
                // construct the String
                if (myColumnAttributes.size() > 0){
                    StringBuffer sb = new StringBuffer();
                    for (int i=0; i<myColumnAttributes.size(); i++){
                        if (myDelimiter != null){
                            if (i > 0){
                                sb.append(myDelimiter);
                            }
                        }
                        ColumnAttributes tempDescription = (ColumnAttributes) myColumnAttributes.elementAt(i);
                        
                        Object tempObject = inRecord.getAttributes()[tempDescription.getColumnNum()];
                        if (tempObject != null){
                            // attempt to convert to a number
                            if (tempDescription.getColumnFormat() == ColumnAttributes.FORMAT_CURRENCY){
                                NumberFormat tempNumberFormat = NumberFormat.getCurrencyInstance();
                                try{
                                    tempObject = tempNumberFormat.format(tempObject);
                                }
                                catch (Exception e){
                                    try{
                                        double tempDouble = Double.parseDouble(tempObject.toString());
                                        tempObject = tempNumberFormat.format(tempDouble);
                                    }
                                    catch (Exception ex){
                                    }
                                }
                            }
                        }
                        else{
                            sb.delete(0, sb.length());
                            break;
                        }
                        sb.append(tempDescription.getColumnPreString());
                        sb.append(tempObject);
                        sb.append(tempDescription.getColumnPostString());
                    }
                    String tempString = sb.toString();
                    
                    if (!isDuplicate(tempString)){
                        // draw polygons
                        if ((tempShape instanceof gistoolkit.features.Polygon) || (tempShape instanceof gistoolkit.features.MultiPolygon)){
                            PolygonLabeler tempPolygonLabeler = getPolygonLabeler();
                            boolean tempDraw = tempPolygonLabeler.drawLabel(tempString, tempShape, inGraphics, inConverter, getOverlapManager());
                            if (!tempDraw) removeDuplicate(tempString);
                            return tempDraw;
                        }
                        if ((tempShape instanceof gistoolkit.features.LineString) || (tempShape instanceof gistoolkit.features.MultiLineString)){
                            LineLabeler tempLineLabeler = getLineLabeler();
                            boolean tempDraw = tempLineLabeler.drawLabel(tempString, tempShape, inGraphics, inConverter, getOverlapManager());
                            if (!tempDraw) removeDuplicate(tempString);
                            return tempDraw;
                        }
                        // draw everything else.
                        else{
                            FontMetrics fm = inGraphics.getFontMetrics();
                            Rectangle2D r2d = fm.getStringBounds(tempString, inGraphics);
                            Graphics2D g2d = (Graphics2D) inGraphics;
                            int tempWidth = (int) r2d.getWidth();
                            int tempHeight = (int) r2d.getHeight();
                            Envelope e = inRecord.getShape().getEnvelope();
                            double x = (e.getMinX() + e.getMaxX())/2;
                            double y = (e.getMinY() + e.getMaxY())/2;
                            int tempLocX = inConverter.toScreenX(x);
                            int tempLocY = inConverter.toScreenY(y);
                            java.awt.Point p = getLabelPosition(tempLocX, tempLocY, tempWidth, tempHeight);
                            if (!isOverLaps(tempLocX, tempLocY, tempWidth, tempHeight)){
                                // draw the string
                                g2d.drawString(tempString, p.x-tempWidth/2, p.y+tempHeight/2);
                                return true;
                            }
                            else{
                                removeDuplicate(tempString);
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    /** Return the edit panel used to edit this labeler  */
    public LabelerPanel getEditPanel() {
        SimpleLabelerPanel tempPanel = new SimpleLabelerPanel();
        tempPanel.setLabeler(this);
        return tempPanel;
    }
    /** Name for the Feature Renderer for the configuration file. */
    private static final String MULTIPLE_LABELER_NODE = "MultipleLabeler";
    private static final String MULTIPLE_LABELER_DELIMITER = "Delimiter";
    private static final String MULTIPLE_LABELER_NUM_COLUMNS = "NumColumns";
    private static final String MULTIPLE_LABELER_COLUMN_NODE = "Column";
    private static final String MULTIPLE_LABELER_COLUMN_NUMBER = "Index";
    private static final String MULTIPLE_LABELER_COLUMN_FORMAT = "Format";
    private static final String MULTIPLE_LABELER_COLUMN_PRESTRING = "PreString";
    private static final String MULTIPLE_LABELER_COLUMN_POSTSTRING = "PostString";
    
    
    /** get the configuration information for this labeler  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName(MULTIPLE_LABELER_NODE);
        
        // add the delimiter attribute.
        tempRoot.addAttribute(MULTIPLE_LABELER_DELIMITER, encodeString(myDelimiter));
        
        // add the number of columns to label by.
        tempRoot.addAttribute(MULTIPLE_LABELER_NUM_COLUMNS, ""+myColumnAttributes.size());
        
        // add the column attributes
        for (int i=0; i<myColumnAttributes.size(); i++){
            Node tempColumnNode = new Node(MULTIPLE_LABELER_COLUMN_NODE+i);
            ColumnAttributes tempAttributes = (ColumnAttributes)myColumnAttributes.elementAt(i);
            tempColumnNode.addAttribute(MULTIPLE_LABELER_COLUMN_NUMBER, ""+tempAttributes.getColumnNum());
            tempColumnNode.addAttribute(MULTIPLE_LABELER_COLUMN_FORMAT, ""+tempAttributes.getColumnFormat());
            tempColumnNode.addAttribute(MULTIPLE_LABELER_COLUMN_PRESTRING, ""+tempAttributes.getColumnPreString());
            tempColumnNode.addAttribute(MULTIPLE_LABELER_COLUMN_POSTSTRING, ""+tempAttributes.getColumnPostString());
            tempRoot.addChild(tempColumnNode);
        }
        
        return tempRoot;
    }
    
    /** Set the configuration information for this labeler  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) return;
        super.setNode(inNode);
        
        // get the delimiter attribute
        String tempString = inNode.getAttribute(MULTIPLE_LABELER_DELIMITER);
        if (tempString != null){
            myDelimiter = decodeString(tempString);
        }
        
        // get the columns to label by
        tempString = inNode.getAttribute(MULTIPLE_LABELER_NUM_COLUMNS);
        if (tempString != null){
            try{
                int tempNumColumns = Integer.parseInt(tempString);
                for (int i=0; i<tempNumColumns; i++){
                    Node tempChild = inNode.getChild(MULTIPLE_LABELER_COLUMN_NODE+i);
                    if (tempChild != null){
                        
                        // create the attributes
                        try{
                            tempString = tempChild.getAttribute(MULTIPLE_LABELER_COLUMN_NUMBER);
                            int tempColumnNum = Integer.parseInt(tempString);
                            tempString = tempChild.getAttribute(MULTIPLE_LABELER_COLUMN_FORMAT);
                            int tempColumnFormat = Integer.parseInt(tempString);
                            String tempPreString = tempChild.getAttribute(MULTIPLE_LABELER_COLUMN_PRESTRING);
                            String tempPostString = tempChild.getAttribute(MULTIPLE_LABELER_COLUMN_POSTSTRING);
                            
                            addLabelColumn(tempColumnNum, tempColumnFormat, tempPreString, tempPostString);
                        }
                        catch (Exception e){
                            System.out.println("Error reading label column "+MULTIPLE_LABELER_COLUMN_NODE+i);
                        }
                    }
                }
            }
            catch (NumberFormatException e){
            }
        }
    }
    
    /**
     * Called before the layer is initially labeled to allow the labeler to prepare for labeling.
     */
    public void beginLabel(){
        super.beginLabel();
        getLineLabeler().beginLabel();
        getPolygonLabeler().beginLabel();
    }
    
    /**
     * Called after the layer has completed labeling.
     */
    public void endLabel() {
        super.beginLabel();
        getLineLabeler().endLabel();
        getPolygonLabeler().endLabel();
    }
    
    /**
     * encode the carage returns in a different string form for storage.
     */
    public static String encodeString(String inString){
        StringBuffer sb = new StringBuffer(inString);
        int tempIndex = sb.indexOf("\n");
        while (tempIndex >= 0){
            sb.replace(tempIndex, tempIndex+1, "[CR]");
            tempIndex = sb.indexOf("\n");
        }
        return sb.toString();
    }
    
    /**
     * Decode the carage returns so they can be displayed.
     */
    public static String decodeString(String inString){
        StringBuffer sb = new StringBuffer(inString);
        int tempIndex = sb.indexOf("[CR]");
        while (tempIndex >= 0){
            sb.replace(tempIndex, tempIndex + 4, "\n");
            tempIndex = sb.indexOf("[CR]");
        }
        return sb.toString();
    }
}
