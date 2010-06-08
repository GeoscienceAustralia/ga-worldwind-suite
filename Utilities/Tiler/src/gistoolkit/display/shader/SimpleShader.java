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

package gistoolkit.display.shader;

import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import gistoolkit.common.*;
import gistoolkit.display.*;
import gistoolkit.display.shader.stroke.*;

/**
 * A simple shader to handle the simplest case.  Usefull for extending.
 */
public class SimpleShader implements Shader{
    /**
     * A description to be displayed to the user that indicates what this scale bar does.
     * A good thing to indicates is that it takes a projection in meters and displays a scale in metric.
     * Or that it takes a projection in feed and displays a scale in in,ft,yd,and miles.
     */
    public String getDescription(){return "From a meter projection, displays km,m,cm,mm";}
    
    /** Creates new SimpleShader */
    public SimpleShader() {
    }
    
    /** Default Color: null by default indicating that the shape will not be filled.*/
    private Color myDefaultFillColor = null;
    /** Retrieve the default fill color */
    public Color getDefaultFillColor(){return myDefaultFillColor;}
    /** Set the default fill color */
    public void setDefaultFillColor(Color inColor){myDefaultFillColor = inColor;}
    
    /** Default Color: null by default indicating that the shape will not be drawn.*/
    private Color myDefaultLineColor = null;
    /** Retrieve the default fill color */
    public Color getDefaultLineColor(){return myDefaultLineColor;}
    /** Set the default fill color */
    public void setDefaultLineColor(Color inColor){myDefaultLineColor = inColor;}
    
    /** Default Color: null by default indicating that the shape will not be filled.*/
    private Color myDefaultLabelColor = null;
    /** Retrieve the default fill color */
    public Color getDefaultLabelColor(){return myDefaultLabelColor;}
    /** Set the default fill color */
    public void setDefaultLabelColor(Color inColor){myDefaultLabelColor = inColor;}
    
    /** Default Color: red by default indicating that the shape will not be Highlighed.*/
    private Color myDefaultHighlightColor = Color.red;
    /** Retrieve the default Highligh color */
    public Color getDefaultHighlightColor(){return myDefaultHighlightColor;}
    /** Set the default Highligh color */
    public void setDefaultHighlightColor(Color inColor){myDefaultHighlightColor = inColor;}
    
    /** The name of the shader */
    private String myName = "";
    /** Get the name of the shader */
    public String getName(){return myName;}
    /** Set the name of the shader */
    public void setName(String inName){myName = inName;}
    
    /** The name of the column to shade by */
    private String myColumnName = null;
    /** Get the name of the column to shade by */
    public String getColumnName(){return myColumnName;}
    /** Set the name of the column to shade by */
    public void setColumnName(String inColumnName){myColumnName = inColumnName;}
    
    /** The font to use when labeling */
    private Font myDefaultFont = new Font("serif",Font.PLAIN, 12);
    /** Get the font to be used when writing labels */
    public Font getDefaultFont(){return myDefaultFont;}
    /** Set the font to be used when writing labels */
    public void setDefaultFont(Font inFont){myDefaultFont = inFont;}
    
    /** Composite for drawing shapes.*/
    private AlphaComposite myAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
    /** Return the default alpha composite */
    protected AlphaComposite getDefaultAlphaComposite(){return myAlphaComposite;}
    /** Hold a reference to the float alpha for returning to interested parties.*/
    private float myAlpha = 1;
    /** Retrieve the alpha of the shape 0 is clear, 1 is opaque.*/
    public float getAlpha(){return myAlpha;}
    /** Set the alpha of the shape 0 means clear, 1 means opaque.*/
    public void setAlpha(float inAlpha){
        myAlpha = inAlpha;
        myAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, inAlpha);
    }
    
    /**Default Line width is a single pixel width. The default stroke is a round cap and a round join.*/
    private Stroke myStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    /** Return the stroke for this shader.*/
    public Stroke getStroke(){return myStroke;}
    /** Sets the stroke for this shader, will not allow null.*/
    public void setStroke(Stroke inStroke){if (inStroke != null) myStroke = inStroke;}
    
    /**
     * Each shader should have a dialog to display to edit the shader.
     * This method will return that dialog.
     */
    public ShaderEditDialog getEditDialog() {
        return null;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for drawing of lines.
     */
    public Graphics getLineGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        if (myDefaultLineColor == null) return null;
        g2d.setColor(myDefaultLineColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myDefaultFont);
        return g2d;
    }
    
    /**
     * Set up the graphics context for the highlighting of lines.
     * Always return the graphics context sent in after modifying it for highlighting of lines.
     */
    public Graphics getLineHighlightGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        if (myDefaultHighlightColor == null) return null;
        g2d.setColor(myDefaultHighlightColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myDefaultFont);
        return g2d;
    }
    
    /**
     * Set up the graphics context for the filling of shapes.
     * Always return the graphics context sent in after modifying it for filling of polygons.
     */
    public Graphics getFillGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        if (myDefaultFillColor == null) return null;
        g2d.setColor(myDefaultFillColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myDefaultFont);
        return g2d;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for highlighting of polygons.
     */
    public Graphics getFillHighlightGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        if (myDefaultHighlightColor == null) return null;
        g2d.setColor(myDefaultFillColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myDefaultFont);
        return g2d;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for labeling of features.
     */
    public Graphics getLabelGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        if (myDefaultLabelColor == null) return null;
        g2d.setColor(myDefaultLabelColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myDefaultFont);
        return g2d;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for highlighting of features.
     */
    public Graphics getLabelHighlightGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        if (myDefaultHighlightColor == null) return null;
        g2d.setColor(myDefaultHighlightColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myDefaultFont);
        return g2d;
    }
    
    /**
     * Read the properties for the initialization of the rendere from the properties sent in.
     */
    public void load(Properties inProperties) throws Exception {
    }
    
    /**
     * Return the Legend for this shader
     */
    public BufferedImage getLegend(){
        return null;
    }
    
    private static final String DEFAULT_STROKE_NODE = "DefaultStroke";
    private static final String DEFAULT_STROKE_DASH_ARRAY = "DefaultStrokeDashArray";
    private static final String DEFAULT_STROKE_DASH_PHASE = "DefaultStrokeDashPhase";
    private static final String DEFAULT_STROKE_END_CAP = "DefaultStrokeEndCap";
    private static final String DEFAULT_STROKE_LINE_JOIN = "DefaultStrokeLineJoin";
    private static final String DEFAULT_STROKE_LINE_WIDTH = "DefaultStrokeLineWidth";
    private static final String DEFAULT_STROKE_MITER_LIMIT = "DefaultStrokeMiterLimit";
    private static final String DEFAULT_STROKE_TYPE = "DefaultStrokeType";
    private static final String STROKE_TYPE_BASIC = "Basic";
    private static final String STROKE_TYPE_RAILROAD = "RailRoad";
    private static final String DEFAULT_TIE_WIDTH = "DefaultTieWidth";
    private static final String DEFAULT_TWO_LINE = "DefaultTwoLine";
    
    protected Node getNodeFromStroke(Stroke inStroke){
        Node tempRoot = new Node(DEFAULT_STROKE_NODE);
        if (inStroke instanceof BasicStroke){
            BasicStroke tempStroke = (BasicStroke) inStroke;
            tempRoot.addAttribute(DEFAULT_STROKE_TYPE, STROKE_TYPE_BASIC);
            tempRoot.addAttribute(DEFAULT_STROKE_DASH_ARRAY, getStringFromDashArray(tempStroke.getDashArray()));
            tempRoot.addAttribute(DEFAULT_STROKE_DASH_PHASE, ""+tempStroke.getDashPhase());
            tempRoot.addAttribute(DEFAULT_STROKE_END_CAP, ""+tempStroke.getEndCap());
            tempRoot.addAttribute(DEFAULT_STROKE_LINE_JOIN, ""+tempStroke.getLineJoin());
            tempRoot.addAttribute(DEFAULT_STROKE_LINE_WIDTH, ""+tempStroke.getLineWidth());
            tempRoot.addAttribute(DEFAULT_STROKE_MITER_LIMIT, ""+tempStroke.getMiterLimit());
        }
        if (inStroke instanceof RailRoadStroke){
            RailRoadStroke tempStroke = (RailRoadStroke) inStroke;
            tempRoot.addAttribute(DEFAULT_STROKE_TYPE, STROKE_TYPE_RAILROAD);
            tempRoot.addAttribute(DEFAULT_STROKE_DASH_ARRAY, getStringFromDashArray(tempStroke.getDashArray()));
            tempRoot.addAttribute(DEFAULT_STROKE_LINE_WIDTH, ""+tempStroke.getLineWidth());
            tempRoot.addAttribute(DEFAULT_TIE_WIDTH, ""+tempStroke.getTieWidth());
            tempRoot.addAttribute(DEFAULT_TWO_LINE, ""+tempStroke.getTwoLine());
        }
        return tempRoot;
    }
    
    /** Construct the string for saving the dash array from the array sent in. */
    protected String getStringFromDashArray(float[] inDashArray){
        StringBuffer sb = new StringBuffer();
        if (inDashArray != null){
            for (int i=0; i<inDashArray.length; i++){
                if (i>0) sb.append("|");
                sb.append(inDashArray[i]);
            }
        }
        return sb.toString();
    }
    
    /** Construct the dash array from the string that saves it to configuration.
     * The string is a pipe | delimited list of float values that are used to determine which
     * segments of a line are drawn and which ones are not.
     */
    protected float[] getDashArrayFromString(String inString){
        if (inString == null) return null;
        StringTokenizer st = new StringTokenizer(inString);
        Vector tempVect = new Vector();
        float[] tempDashArray = null;
        while (st.hasMoreElements()){
            String tempNum = st.nextToken("|");
            try{
                Float tempFloat = new Float(tempNum);
                tempVect.addElement(tempFloat);
            }
            catch (NumberFormatException e){
                System.out.println("Number Format Exception parsing DashArray in Simple Shader Value="+inString);
            }
        }
        if (tempVect.size() > 0){
            tempDashArray = new float[tempVect.size()];
            for (int i=0; i<tempVect.size(); i++){
                tempDashArray[i] = ((Float) tempVect.elementAt(i)).floatValue();
            }
        }
        return tempDashArray;
    }
    
    
    protected Stroke getStrokeFromNode(Node inNode){
        if (inNode == null) return new BasicStroke((float)1.0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        Stroke tempStroke = new BasicStroke((float)1.0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        
        // the line width
        float tempLineWidth = 1;
        String tempName = DEFAULT_STROKE_LINE_WIDTH;
        String tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                tempLineWidth = Float.parseFloat(tempValue);
            }
            catch (NumberFormatException e){
                System.out.println("Number Format Exception parsing "+tempName+" in Simple Shader Value="+tempValue);
            }
        }
        
        // the dash array
        float[] tempDashArray = new float[0];
        tempName = DEFAULT_STROKE_DASH_ARRAY;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            tempDashArray = getDashArrayFromString(tempValue);
        }
        
        // the type
        String tempTypeString = inNode.getAttribute(DEFAULT_STROKE_TYPE);
        if (tempTypeString == null) tempTypeString = STROKE_TYPE_BASIC;
        
        // set the stroke.
        if (tempTypeString.equals(STROKE_TYPE_RAILROAD)){
            float tempTieWidth = tempLineWidth * 2;
            boolean tempTwoLine = false;
            tempValue = inNode.getAttribute(DEFAULT_TIE_WIDTH);
            if (tempValue != null) tempTieWidth = Float.parseFloat(tempValue);
            tempValue = inNode.getAttribute(DEFAULT_TWO_LINE);
            if (tempValue != null) tempTwoLine = tempValue.toUpperCase().trim().startsWith("T");
            if ((tempDashArray == null) || (tempDashArray.length == 0))
                tempStroke = new RailRoadStroke(tempLineWidth, tempTieWidth, tempTwoLine);
            else tempStroke = new RailRoadStroke(tempLineWidth, tempTieWidth, tempDashArray, tempTwoLine);
        }
        else{
            // parameters specific to the Basic stroke
            // the Dash Phase
            float tempDashPhase = 0;
            tempName = DEFAULT_STROKE_DASH_PHASE;
            tempValue = inNode.getAttribute(tempName);
            if (tempValue != null){
                try{
                    tempDashPhase = Float.parseFloat(tempValue);
                }
                catch (NumberFormatException e){
                    System.out.println("Number Format Exception parsing "+tempName+" in Simple Shader Value="+tempValue);
                }
            }
            
            // the end cap style
            int tempEndCap = BasicStroke.CAP_ROUND;
            tempName = DEFAULT_STROKE_END_CAP;
            tempValue = inNode.getAttribute(tempName);
            if (tempValue != null){
                try{
                    tempEndCap = Integer.parseInt(tempValue);
                }
                catch (NumberFormatException e){
                    System.out.println("Number Format Exception parsing "+tempName+" in Simple Shader Value="+tempValue);
                }
            }
            
            // the way the lines are joined
            int tempLineJoin = BasicStroke.JOIN_ROUND;
            tempName = DEFAULT_STROKE_LINE_JOIN;
            tempValue = inNode.getAttribute(tempName);
            if (tempValue != null){
                try{
                    tempLineJoin = Integer.parseInt(tempValue);
                }
                catch (NumberFormatException e){
                    System.out.println("Number Format Exception parsing "+tempName+" in Simple Shader Value="+tempValue);
                }
            }
            
            // Miter Limit
            float tempMiterLimit = 1;
            tempName = DEFAULT_STROKE_MITER_LIMIT;
            tempValue = inNode.getAttribute(tempName);
            if (tempValue != null){
                try{
                    tempMiterLimit = Float.parseFloat(tempValue);
                }
                catch (NumberFormatException e){
                    System.out.println("Number Format Exception parsing "+tempName+" in Simple Shader Value="+tempValue);
                }
            }
            
            // return the new basic stroke
            if ((tempDashArray == null) || (tempDashArray.length == 0)){
                tempStroke = new BasicStroke(tempLineWidth, tempEndCap, tempLineJoin, tempMiterLimit);
            }
            else tempStroke = new BasicStroke(tempLineWidth, tempEndCap, tempLineJoin, tempMiterLimit, tempDashArray, tempDashPhase);
        }
        return tempStroke;
    }
    
    private static final String SHADER_NAME = "DefaultName";
    private static final String DEFAULT_FILL_COLOR = "DefaultFillColor";
    private static final String DEFAULT_LINE_COLOR = "DefaultLineColor";
    private static final String DEFAULT_HIGHLIGHT_COLOR = "DefaultHighlightColor";
    private static final String DEFAULT_LABEL_COLOR = "DefaultLabelColor";
    private static final String DEFAULT_LABEL_FONT = "DefaultLabelFont";
    private static final String DEFAULT_LABEL_FONT_STYLE = "DefaultLabelFontStyle";
    private static final String DEFAULT_LABEL_FONT_SIZE = "DefaultLabelFontSize";
    private static final String DEFAULT_ALPHA = "DefaultAlpha";
    private static final String DEFAULT_ALPHA_RULE = "DefaultAlphaRule";
    
    /** Get the configuration information for this shader  */
    public Node getNode() {
        Node tempRoot = new Node("SimpleShader");
        
        // name
        tempRoot.addAttribute(SHADER_NAME, myName);
        
        // Fill color
        if (myDefaultFillColor != null) tempRoot.addAttribute(DEFAULT_FILL_COLOR, ""+myDefaultFillColor.getRGB());
        else tempRoot.addAttribute(DEFAULT_FILL_COLOR, "NONE");
        
        // LineColor
        if (myDefaultLineColor != null) tempRoot.addAttribute(DEFAULT_LINE_COLOR, ""+myDefaultLineColor.getRGB());
        else tempRoot.addAttribute(DEFAULT_LINE_COLOR, "NONE");
        
        // LabelColor
        if (myDefaultLabelColor != null) tempRoot.addAttribute(DEFAULT_LABEL_COLOR, ""+myDefaultLabelColor.getRGB());
        else tempRoot.addAttribute(DEFAULT_LABEL_COLOR, "NONE");
        
        // HighlightColor
        if (myDefaultHighlightColor != null) tempRoot.addAttribute(DEFAULT_HIGHLIGHT_COLOR, ""+myDefaultHighlightColor.getRGB());
        else tempRoot.addAttribute(DEFAULT_HIGHLIGHT_COLOR, "NONE");
        
        // LabelFont
        tempRoot.addAttribute(DEFAULT_LABEL_FONT, myDefaultFont.getName());
        
        // LabelFontStyle
        tempRoot.addAttribute(DEFAULT_LABEL_FONT_STYLE, ""+myDefaultFont.getStyle());
        
        // LabelFontSize
        tempRoot.addAttribute(DEFAULT_LABEL_FONT_SIZE, ""+myDefaultFont.getSize());
        
        // Alpha Value
        tempRoot.addAttribute(DEFAULT_ALPHA, ""+myAlphaComposite.getAlpha());
        
        // Alpha Rule
        tempRoot.addAttribute(DEFAULT_ALPHA_RULE, ""+myAlphaComposite.getRule());
        
        // the Stroke
        tempRoot.addChild(getNodeFromStroke(myStroke));
        
        return tempRoot;
    }
    
    /** Set the configuration information for this shader  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) return;
        
        // name
        String tempName = SHADER_NAME;
        String tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            myName = tempValue;
        }
        
        // Fill color
        tempName = DEFAULT_FILL_COLOR;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                if (!tempValue.equalsIgnoreCase("NONE")){
                    myDefaultFillColor = new Color(Integer.parseInt(tempValue));
                }
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing "+tempName+" in SimpleShader Value = "+tempValue);
            }
        }
        else myDefaultFillColor = null;
        
        // Line color
        tempName = DEFAULT_LINE_COLOR;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                if (!tempValue.equalsIgnoreCase("NONE")){
                    myDefaultLineColor = new Color(Integer.parseInt(tempValue));
                }
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing "+tempName+" in SimpleShader Value = "+tempValue);
            }
        }
        else myDefaultLineColor = null;
        
        // Label color
        tempName = DEFAULT_LABEL_COLOR;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                if (!tempValue.equalsIgnoreCase("NONE")){
                    myDefaultLabelColor = new Color(Integer.parseInt(tempValue));
                }
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing "+tempName+" in SimpleShader Value = "+tempValue);
            }
        }
        else myDefaultLabelColor = null;
        
        // Highlight color
        tempName = DEFAULT_HIGHLIGHT_COLOR;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                if (!tempValue.equalsIgnoreCase("NONE")){
                    myDefaultHighlightColor = new Color(Integer.parseInt(tempValue));
                }
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing "+tempName+" in SimpleShader Value = "+tempValue);
            }
        }
        else myDefaultHighlightColor = null;
        
        // the Label Font
        // the Label Font Name
        String tempFontName = "Helvetica";
        tempName = DEFAULT_LABEL_FONT;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null) tempFontName = tempValue;
        
        // the Label Font Style
        int tempFontStyle = Font.PLAIN;
        tempName = DEFAULT_LABEL_FONT_STYLE;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                tempFontStyle = Integer.parseInt(tempValue);
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing "+tempName+" in SimpleShader Value = "+tempValue);
            }
        }
        
        // the Label Font size
        int tempFontSize = 10;
        tempName = DEFAULT_LABEL_FONT_SIZE;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                tempFontSize = Integer.parseInt(tempValue);
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing "+tempName+" in SimpleShader Value = "+tempValue);
            }
        }
        myDefaultFont = new Font(tempFontName, tempFontStyle, tempFontSize);
        
        // Alpha Value
        float tempAlphaValue = 1;
        tempName = DEFAULT_ALPHA;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                tempAlphaValue = Float.parseFloat(tempValue);
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing "+tempName+" in SimpleShader Value = "+tempValue);
            }
        }
        myAlpha = tempAlphaValue;
        
        // the Alpha Rule
        int tempAlphaRule = AlphaComposite.DST_OUT;
        tempName = DEFAULT_ALPHA_RULE;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                tempAlphaRule = Integer.parseInt(tempValue);
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing "+tempName+" in SimpleShader Value = "+tempValue);
            }
        }
        myAlphaComposite = AlphaComposite.getInstance(tempAlphaRule, tempAlphaValue);
        
        // the Stroke
        Node tempNode = inNode.getChild(DEFAULT_STROKE_NODE);
        if (tempNode != null){
            myStroke = getStrokeFromNode(tempNode);
        }
        
    }
    
}
