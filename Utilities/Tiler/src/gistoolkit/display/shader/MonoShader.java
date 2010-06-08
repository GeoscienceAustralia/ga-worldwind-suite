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
import java.awt.image.*;
import gistoolkit.common.*;
import gistoolkit.display.*;
import gistoolkit.display.shader.stroke.*;
/**
 * A shader that will always return the same color regardless of what values are sent in.
 */
public class MonoShader implements Shader, EditableShader {
    /** Default line color is black;*/
    private Color myLineColor = Color.black;
    /** Color to use when drawing lines*/
    public Color getLineColor() {return myLineColor;}
    /** Set the line color to be used when drawing lines*/
    public void setLineColor(Color newLineColor) {myLineColor = newLineColor;}
    
    /** Default fill color is null, it will not be drawn.*/
    private Color myFillColor = null;
    /** Color to use when filling a shape*/
    public Color getFillColor() {return myFillColor;}
    /** Sets the color to use when filling a shape*/
    public void setFillColor(Color inFillColor) {myFillColor = inFillColor;}
    
    /** Default fill pattern is null, it will not be drawn.*/
    private BufferedImage myFillPattern = null;
    /** Get the pattern to use when filling a polygon.*/
    public BufferedImage getFillPattern() {return myFillPattern;}
    /** Sets the color to use when filling a shape*/
    public void setFillPattern(BufferedImage inFillPattern) {myFillPattern = inFillPattern; myFillPatternFileName = null;}
    
    /** The file name of the Buffered Image.  This is used in order to store the pattern in the configuration file and retrieve it again. */
    private String myFillPatternFileName = null;
    /** Get the file name of the fill pattern.  This returns the location of the file. */
    public String getFillPatternFileName(){return myFillPatternFileName;}
    /** Set the file name of the fill pattern.  This will load the file and use it.  An exception will be thrown if the file is the wrong format, or not available. */
    public void setFillPatternFileName(String inFileName) throws Exception{
        Image tempImage = Toolkit.getDefaultToolkit().createImage(inFileName);
        
        // wait for the damn image to load.
        Panel tempWaitPanel = new Panel();
        MediaTracker mt = new MediaTracker(tempWaitPanel);
        mt.addImage(tempImage, 0);
        mt.waitForAll();
        
        int tempWidth = tempImage.getWidth(tempWaitPanel);
        int tempHeight = tempImage.getHeight(tempWaitPanel);
        
        // retrieve the pixels
        BufferedImage tempBImage = new BufferedImage(tempWidth, tempHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempGraphics = (Graphics2D) tempBImage.getGraphics();
        tempGraphics.drawImage(tempImage, 0, 0, tempWaitPanel);
        myFillPatternFileName = inFileName;
        myFillPattern = tempBImage;
    }
    
    /** Default highlight color is red because I like red.*/
    private Color myHighlightColor = Color.red;
    /** Color to use when highlighting a selected shape*/
    public Color getHighlightColor() {return myHighlightColor;}
    /** Color to use when highlighting a selected shape*/
    public void setHighlightColor(Color inHighlightColor) {myHighlightColor = inHighlightColor;}
    
    /** Default the label color to null.*/
    private Color myLabelColor = null;
    /** Returns the color this MonoShader will use for displaying a label.*/
    public Color getLabelColor(){return myLabelColor;}
    /** Sets the color this MonoShader will use for displaying a label.*/
    public void setLabelColor(Color inLabelColor){myLabelColor = inLabelColor;}
    
    /** Default the label font to some standard type of Font.*/
    private Font myLabelFont = new Font("Serif",Font.PLAIN, 12);
    /** Return the font to use when displaying the label. */
    public Font getLabelFont(){return myLabelFont;}
    /** Set the font to use when displaying the label. */
    public void setLabelFont(Font inFont){myLabelFont = inFont;}
    
    /**Default Line width is a single pixel width. The default stroke is a round cap and a round join.*/
    private Stroke myStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    /** Return the stroke for this shader.*/
    public Stroke getStroke(){return myStroke;}
    /** Sets the stroke for this shader, will not allow null.*/
    public void setStroke(Stroke inStroke){if (inStroke != null) myStroke = inStroke;}
    
    /**Name to appear on the legend */
    private String myName = new String();
    /** Retrieve the name of the shader */
    public String getName(){return myName;}
    /** Set the name of the shader */
    public void setName(String inName){myName = inName;}
    
    /**Default Alpha is 100% opaque*/
    private float myAlpha = 1;
    /** Composite for drawing shapes.*/
    private AlphaComposite myAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
    /** Retrieve the alpha of the shape 0 is clear, 1 is opaque.*/
    public float getAlpha(){return myAlpha;}
    /** Set the alpha of the shape 0 means clear, 1 means opaque.*/
    public void setAlpha(float inAlpha){
        myAlpha = inAlpha;
        myAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, inAlpha);
    }
    
    /**
     * Create a new Mono shader with the default values for everything.
     */
    public MonoShader() {
        super();
    }
    
    /**
     * MonoShader constructor comment.
     */
    public MonoShader(Color inColor) {
        super();
        myFillColor = inColor;
        myHighlightColor = inColor;
        myLineColor = inColor;
        myLabelColor = inColor;
    }
    
    /**
     * Create a new Mono Shader with the given Line and Fill colors, the label color is the same as the line color, and the highlight color is the default.
     */
    public MonoShader(Color inLineColor, Color inFillColor) {
        super();
        myFillColor = inFillColor;
        myLineColor = inLineColor;
        myLabelColor = inLineColor;
    }
    
    /**
     * Create a new Mono Shader with the given line, fill and highlight colors, the label color is the same as the line color.
     */
    public MonoShader(Color inLineColor, Color inFillColor, Color inHighlightColor) {
        super();
        myFillColor = inFillColor;
        myLineColor = inLineColor;
        myHighlightColor = inHighlightColor;
        myLabelColor = inLineColor;
    }
    
    /**
     * Create a new Mono Shader with the given line, fill highlight and Label colors.
     */
    public MonoShader(Color inLineColor, Color inFillColor, Color inHighlightColor, Color inLabelColor) {
        super();
        myFillColor = inFillColor;
        myLineColor = inLineColor;
        myHighlightColor = inHighlightColor;
        myLabelColor = inLabelColor;
    }
    
    /**
     * Returns a new edit panel for editing this type of shader.
     */
    public ShaderPanel getEditPanel(){
        MonoShaderPanel tempShaderPanel = new MonoShaderPanel();
        tempShaderPanel.setShader(this);
        return tempShaderPanel;
    }
    
    /**
     * Read the properties for the initialization of the shader from the properties sent in.
     */
    public void load(Properties inProperties) throws Exception {
        if (inProperties == null) throw new Exception("Properties are null");
        String tempClass = this.getClass().getName();
        
        // read the colors.
        String tempString = inProperties.getProperty(tempClass+".FillColor");
        if (tempString != null) myFillColor = Color.decode(tempString);
        tempString = inProperties.getProperty(tempClass+".LineColor");
        if (tempString != null) myLineColor = Color.decode(tempString);
        tempString = inProperties.getProperty(tempClass+".LabelColor");
        if (tempString != null) myLabelColor = Color.decode(tempString);
        tempString = inProperties.getProperty(tempClass+".HighlightColor");
        if (tempString != null) myHighlightColor = Color.decode(tempString);
        tempString = inProperties.getProperty(tempClass+".AlphaComposite");
        if (tempString != null) setAlpha(Float.parseFloat(tempString));
        tempString = inProperties.getProperty(tempClass+".LineWidth");
        if (tempString != null){
            myStroke = new BasicStroke(Float.parseFloat(tempString), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
    }
    
    /** Generate the legend for this shader.  The MonoShader only creates one entry in the legend. */
    public BufferedImage getLegend(){
        int tempSpace = 3;
        int height = 20;
        int width = 20;
        int ascent = 0;
        int squareWidth = 16;
        BufferedImage tempImage = new BufferedImage(40,20,BufferedImage.TYPE_INT_ARGB);
        Graphics tempGraphics = tempImage.getGraphics();
        Graphics2D g2d = (Graphics2D) tempGraphics;
        
        // change the width of the image to match the label.
        g2d.setFont(myLabelFont);
        FontMetrics tempMetrics = g2d.getFontMetrics();
        height = tempMetrics.getHeight();
        width = tempMetrics.stringWidth(myName);
        if (height < 20) height = 20;
        ascent = tempMetrics.getAscent();
        tempImage = new BufferedImage(width+20+tempSpace, height, BufferedImage.TYPE_INT_ARGB);
        
        tempGraphics = tempImage.getGraphics();
        g2d = (Graphics2D) tempGraphics;
        g2d.setBackground(Color.white);
        g2d.clearRect(0,0,tempImage.getWidth(), tempImage.getHeight());
        
        // draw the label
        g2d.setFont(myLabelFont);
        if (myLabelColor == null) g2d.setColor(Color.black);
        else g2d.setColor(myLabelColor);
        g2d.drawString(myName,2+squareWidth+tempSpace,ascent+2);
        
        // draw the fill
        g2d = (Graphics2D) getFillGraphics(g2d, null, null);
        g2d.fillRect(2,height/2-squareWidth/2,squareWidth, squareWidth);
        
        // draw the line
        g2d = (Graphics2D) getLineGraphics(g2d, null, null);
        g2d.drawRect(2,height/2-squareWidth/2,squareWidth, squareWidth);

        return tempImage;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for drawing of lines.
     */
    public Graphics getLineGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (myLineColor == null) return null;
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        g2d.setColor(myLineColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myLabelFont);
        return g2d;
    }
    
    /**
     * Set up the graphics context for the highlighting of lines.
     * Always return the graphics context sent in after modifying it for highlighting of lines.
     */
    public Graphics getLineHighlightGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (myHighlightColor == null) return null;
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        g2d.setColor(myHighlightColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myLabelFont);
        return g2d;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for filling of polygons.
     */
    public Graphics getFillGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (myFillColor == null) return null;
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        g2d.setColor(myFillColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myLabelFont);
        if (myFillPattern != null){
            g2d.setPaint(new TexturePaint(myFillPattern, new Rectangle(0,0,myFillPattern.getWidth(), myFillPattern.getHeight())));
        }
        return g2d;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for highlighting of polygons.
     */
    public Graphics getFillHighlightGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (myFillColor == null) return null;
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        g2d.setColor(myFillColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myLabelFont);
        return g2d;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for labeling of features.
     */
    public Graphics getLabelGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (myLabelColor == null) return null;
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        g2d.setColor(myLabelColor);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1.0));
        g2d.setStroke(myStroke);
        g2d.setFont(myLabelFont);
        return g2d;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for highlighting of features.
     */
    public Graphics getLabelHighlightGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (myHighlightColor == null) return null;
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        g2d.setColor(myHighlightColor);
        g2d.setStroke(myStroke);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myLabelFont);
        return g2d;
    }
    
    private static final String SHADER_NAME = "Name";
    private static final String FILL_COLOR = "FillColor";
    private static final String LINE_COLOR = "LineColor";
    private static final String LINE_WIDTH = "LineWidth";
    private static final String HIGHLIGHT_COLOR = "HighlightColor";
    private static final String LABEL_COLOR = "LabelColor";
    private static final String LABEL_FONT = "LabelFont";
    private static final String LABEL_FONT_STYLE = "LabelFontStyle";
    private static final String LABEL_FONT_SIZE = "LabelFontSize";
    private static final String ALPHA = "Alpha";
    private static final String DASH_ARRAY = "DashArray";
    private static final String FILL_IMAGE_FILE_NAME = "FillImageFileName";
    private static final String STROKE_TYPE = "StrokeType";
    private static final String STROKE_TYPE_BASIC = "Basic";
    private static final String STROKE_TYPE_RAILROAD = "RailRoad";
    private static final String TIE_WIDTH = "TieWidth";
    private static final String TWO_LINE = "TwoLine";
    
    /** Get the configuration information for this shader  */
    public Node getNode() {
        Node tempRoot = new Node("MonoShader");
        tempRoot.addAttribute(SHADER_NAME, getName());
        if (myFillColor != null) tempRoot.addAttribute(FILL_COLOR, ""+myFillColor.getRGB());
        if (myLineColor != null) tempRoot.addAttribute(LINE_COLOR, ""+myLineColor.getRGB());
        if (myHighlightColor != null) tempRoot.addAttribute(HIGHLIGHT_COLOR, ""+myHighlightColor.getRGB());
        if (myLabelColor != null) tempRoot.addAttribute(LABEL_COLOR, ""+myLabelColor.getRGB());
        tempRoot.addAttribute(LABEL_FONT, myLabelFont.getFontName());
        tempRoot.addAttribute(LABEL_FONT_STYLE, ""+myLabelFont.getStyle());
        tempRoot.addAttribute(LABEL_FONT_SIZE, ""+myLabelFont.getSize());
        tempRoot.addAttribute(ALPHA, ""+getAlpha());
        if (myFillPatternFileName != null) tempRoot.addAttribute(FILL_IMAGE_FILE_NAME, myFillPatternFileName);
        if (myStroke instanceof BasicStroke){
            tempRoot.addAttribute(STROKE_TYPE, STROKE_TYPE_BASIC);
            BasicStroke tempStroke = (BasicStroke) myStroke;
            tempRoot.addAttribute(LINE_WIDTH, ""+tempStroke.getLineWidth());
            float[] tempDashArray = tempStroke.getDashArray();
            if (tempDashArray != null){
                StringBuffer sb = new StringBuffer();
                for (int i=0; i<tempDashArray.length; i++){
                    if (i>0) sb.append(",");
                    sb.append(Float.toString(tempDashArray[i]));
                }
                tempRoot.addAttribute(DASH_ARRAY, sb.toString());
            }
        }
        else if (myStroke instanceof RailRoadStroke){
            tempRoot.addAttribute(STROKE_TYPE, STROKE_TYPE_RAILROAD);
            RailRoadStroke tempStroke = (RailRoadStroke) myStroke;
            tempRoot.addAttribute(LINE_WIDTH, ""+tempStroke.getLineWidth());
            tempRoot.addAttribute(TIE_WIDTH, ""+tempStroke.getTieWidth());
            tempRoot.addAttribute(TWO_LINE, ""+tempStroke.getTwoLine());
            float[] tempDashArray = tempStroke.getDashArray();
            if (tempDashArray != null){
                StringBuffer sb = new StringBuffer();
                for (int i=0; i<tempDashArray.length; i++){
                    if (i>0) sb.append(",");
                    sb.append(Float.toString(tempDashArray[i]));
                }
                tempRoot.addAttribute(DASH_ARRAY, sb.toString());
            }
        }
        
        return tempRoot;
        
    }
    
    /** Set the configuration information for this shader  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) return;
        String tempName = SHADER_NAME;
        String tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            myName = tempValue;
        }
        
        // fill color
        tempName = FILL_COLOR;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                myFillColor = new Color(Integer.parseInt(tempValue));
            }
            catch (NumberFormatException e){
                System.out.println("Error parsing value for "+tempName+" Value = "+tempValue+" in MonoShader Using Default");
            }
        }
        else myFillColor = null;
        
        // line color
        tempName = LINE_COLOR;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                myLineColor = new Color(Integer.parseInt(tempValue));
            }
            catch (NumberFormatException e){
                System.out.println("Error parsing value for "+tempName+" Value = "+tempValue+" in MonoShader Using Default");
            }
        }
        else myLineColor = null;
        
        // Label Color
        tempName = LABEL_COLOR;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                myLabelColor = new Color(Integer.parseInt(tempValue));
            }
            catch (NumberFormatException e){
                System.out.println("Error parsing value for "+tempName+" Value = "+tempValue+" in MonoShader Using Default");
            }
        }
        else myHighlightColor = null;
        
        // Highlight Color
        tempName = HIGHLIGHT_COLOR;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                myHighlightColor = new Color(Integer.parseInt(tempValue));
            }
            catch (NumberFormatException e){
                System.out.println("Error parsing value for "+tempName+" Value = "+tempValue+" in MonoShader Using Default");
            }
        }
        else myHighlightColor = null;
        
        // Line Width
        tempValue = inNode.getAttribute(LINE_WIDTH);
        if (tempValue != null){
            try{
                float tempLineWidth = Float.parseFloat(tempValue);
                String tempTypeString = inNode.getAttribute(STROKE_TYPE);
                if (tempTypeString == null) tempTypeString = STROKE_TYPE_BASIC;
                
                // dash array.
                String tempDashArrayString = inNode.getAttribute(DASH_ARRAY);
                if (tempDashArrayString != null){
                    ArrayList tempList = new ArrayList();
                    StringTokenizer st = new StringTokenizer(tempDashArrayString, ",");
                    while (st.hasMoreElements()){
                        tempList.add(st.nextElement());
                    }
                    float[] tempDashArray = new float[tempList.size()];
                    for (int i=0; i<tempList.size(); i++){
                        tempDashArray[i] = Float.parseFloat((String) tempList.get(i));
                    }
                    
                    // set the stroke.
                    if (tempTypeString.equals(STROKE_TYPE_RAILROAD)){
                        float tempTieWidth = tempLineWidth * 2;
                        boolean tempTwoLine = false;
                        tempValue = inNode.getAttribute(TIE_WIDTH);
                        if (tempValue != null) tempTieWidth = Float.parseFloat(tempValue);
                        tempValue = inNode.getAttribute(TWO_LINE);
                        if (tempValue != null) tempTwoLine = tempValue.toUpperCase().trim().startsWith("T");
                        setStroke(new RailRoadStroke(tempLineWidth, tempTieWidth, tempDashArray, tempTwoLine));
                    }
                    else {
                        setStroke(new BasicStroke(tempLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, tempDashArray, (float) 0 ));
                    }                        
                }
                else{
                    // set the stroke.
                    if (tempTypeString.equals(STROKE_TYPE_RAILROAD)){
                        float tempTieWidth = tempLineWidth * 2;
                        boolean tempTwoLine = false;
                        tempValue = inNode.getAttribute(TIE_WIDTH);
                        if (tempValue != null) tempTieWidth = Float.parseFloat(tempValue);
                        tempValue = inNode.getAttribute(TWO_LINE);
                        if (tempValue != null) tempTwoLine = tempValue.toUpperCase().trim().startsWith("T");
                        setStroke(new RailRoadStroke(tempLineWidth, tempTieWidth, tempTwoLine));
                    }
                    else {
                        setStroke(new BasicStroke(tempLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
                    }
                }
            }
            catch (NumberFormatException e){
                System.out.println("Error parsing value for "+tempName+" Value = "+tempValue+" in MonoShader Using Default");
            }
        }
   
        /** The Fill Image. */
        tempName = FILL_IMAGE_FILE_NAME;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                setFillPatternFileName(tempValue);
            }
            catch (Exception e){
                System.out.println(e);
                e.printStackTrace();
            }
        }
        
        // alpha value
        tempName = ALPHA;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                setAlpha(Float.parseFloat(tempValue));
            }
            catch (NumberFormatException e){
                System.out.println("Error parsing value for "+tempName+" Value = "+tempValue+" in MonoShader Using Default");
            }
        }
        
        // Font Name
        String tempFontName = "Serif";
        tempName = LABEL_FONT;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            tempFontName = tempValue;
        }
        // Font Style
        int tempFontStyle = Font.PLAIN;
        tempName = LABEL_FONT_STYLE;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                tempFontStyle = Integer.parseInt(tempValue);
            }
            catch (NumberFormatException e){
                System.out.println("Error parsing value for "+tempName+" Value = "+tempValue+" in MonoShader Using Default");
            }
        }
        // Font Size
        int tempFontSize = 10;
        tempName = LABEL_FONT_SIZE;
        tempValue = inNode.getAttribute(tempName);
        if (tempValue != null){
            try{
                tempFontSize = Integer.parseInt(tempValue);
            }
            catch (NumberFormatException e){
                System.out.println("Error parsing value for "+tempName+" Value = "+tempValue+" in MonoShader Using Default");
            }
        }
        
        setLabelFont(new Font(tempFontName, tempFontStyle, tempFontSize));
        
    }
    
}