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

package gistoolkit.display.scalebar;

import java.awt.*;
import gistoolkit.common.*;
import gistoolkit.display.*;

/**
 * Class to display a scale bar on the map.
 */
public abstract class SimpleScaleBar implements ScaleBar{
    
    /** 
     * A description to be displayed to the user that indicates what this scale bar does.
     * A good thing to indicates is that it takes a projection in meters and displays a scale in metric.
     * Or that it takes a projection in feed and displays a scale in in,ft,yd,and miles.
     * <p> This one returns kilometers, meters, centimters, milimeters, and micrometers </p>
     */
    public String getDescription(){return "From a meter projection, displays km,m,cm,mm";}

    /** Default Color: null by default indicating that the shape will not be drawn.*/
    private Color myDefaultLineColor = Color.black;
    /** Retrieve the default fill color */
    public Color getDefaultLineColor(){return myDefaultLineColor;}
    /** Set the default fill color */
    public void setDefaultLineColor(Color inColor){myDefaultLineColor = inColor;}
    
    /** Default Color: null by default indicating that the shape will not be filled.*/
    private Color myDefaultLabelColor = Color.black;
    /** Retrieve the default fill color */
    public Color getDefaultLabelColor(){return myDefaultLabelColor;}
    /** Set the default fill color */
    public void setDefaultLabelColor(Color inColor){myDefaultLabelColor = inColor;}
    
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
        
    /** Creates a new instance of ScaleBar */
    public SimpleScaleBar() {
    }
    
    /**Indicates what percent of the screen width this bar should use. */
    private float myScreenPercent = 100;
    /**set what percent of the screen width this bar should use. Should be 1 to 100.*/
    public void setScreenPercent(float inPercent){myScreenPercent = inPercent;}
    /**get what percent of the screen width this bar should use. */
    public float getScreenPercent(){return myScreenPercent;}
    
    /** The offset from the left or right edge of the screen. */
    private int myHorizontalOffset = 0;
    /** Set the offset from the left or right edge of the screen in pixels. */
    public void setHorizontalOffset(int inOffset){myHorizontalOffset = inOffset;}
    /** Set the offset from the uleft or rightr edge of the screen in pixels. */
    public int getHorizontalOffset(){return myHorizontalOffset;}

    /** The offset from the upper or lower edge of the screen. */
    private int myVerticalOffset = 0;
    /** Set the offset from the upper or lower edge of the screen in pixels. */
    public void setVerticalOffset(int inOffset){myVerticalOffset = inOffset;}
    /** Set the offset from the upper or lower edge of the screen in pixels. */
    public int getVerticalOffset(){return myVerticalOffset;}
    
    /** The upper left quadrant of the screen. */
    public static final int UPPER_LEFT = 0;
    
    /** The upper right quadrant of the screen. */
    public static final int UPPER_RIGHT = 1;
    
    /** The lower left quadrant of the screen. */
    public static final int LOWER_LEFT = 3;
    
    /** the lower right quadrant of the screen. */
    public static final int LOWER_RIGHT = 4;
    
    /** Indicates which quadrant of the screen the scale bar should be displayed within.*/
    private int myQuadrant = 0;
    /** Sets the quadrent UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT of the screen where the scale bar should be placed. */
    public void setQuadrant(int inQuadrant){myQuadrant = inQuadrant;}
    /** gets the quadrent UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT of the screen where the scale bar should be placed. */
    public int getQuadrant(){return myQuadrant;}
    
    /** Get the available width of the Scale bar */
    public Rectangle getAvailableSize(int inScreenWidth, int inScreenHeight){
        int width = (int) (inScreenWidth * (myScreenPercent/100.0));
        int height = (int) (inScreenHeight * (myScreenPercent/100.0));
        return new Rectangle(width, height);
    }
    
    /** Return the start point given the width and height. */
    public Point getStart(int inImageWidth, int inImageHeight, int inWidth, int inHeight){
        switch(myQuadrant){
            case UPPER_LEFT:
                int x = myHorizontalOffset;
                int y = myVerticalOffset;
                return new Point(x,y);
            case UPPER_RIGHT:
                x = (inImageWidth-inWidth)-myHorizontalOffset;
                y = myVerticalOffset;
                return new Point(x,y);
            case LOWER_LEFT:
                x = myHorizontalOffset;
                y = (inImageHeight-inHeight)-myVerticalOffset;
                return new Point(x,y);
            case LOWER_RIGHT: 
                x = (inImageWidth-inWidth)-myHorizontalOffset;
                y = (inImageHeight-inHeight)-myVerticalOffset;
                return new Point(x,y);
                
        }
        return new Point(myHorizontalOffset, myVerticalOffset);
    }    
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for drawing of lines.
     */
    public Graphics getLineGraphics(Graphics inGraphics) {
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
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for labeling of features.
     */
    public Graphics getLabelGraphics(Graphics inGraphics) {
        if (inGraphics == null) return inGraphics;
        Graphics2D g2d = (Graphics2D) inGraphics;
        if (myDefaultLabelColor == null) return null;
        g2d.setColor(myDefaultLabelColor);
        g2d.setComposite(myAlphaComposite);
        g2d.setFont(myDefaultFont);
        return g2d;
    }

    /** Method to draw a scale bar on the graphics context. */
    public abstract void drawScale(Graphics inGraphics, Converter inConverter, int inWidth, int inHeight);
         
    private static final String DEFAULT_LINE_COLOR = "DefaultLineColor";
    private static final String DEFAULT_LABEL_COLOR = "DefaultLabelColor";
    private static final String DEFAULT_LABEL_FONT = "DefaultLabelFont";
    private static final String DEFAULT_LABEL_FONT_STYLE = "DefaultLabelFontStyle";
    private static final String DEFAULT_LABEL_FONT_SIZE = "DefaultLabelFontSize";
    private static final String DEFAULT_ALPHA = "DefaultAlpha";
    private static final String DEFAULT_ALPHA_RULE = "DefaultAlphaRule";
    private static final String DEFAULT_LINE_WIDTH = "DefaultLineWidth";
    private static final String QUADRANT = "Quadrant";
    private static final String SCREEN_PERCENT = "ScreenPercent";
    private static final String HORIZONTAL_OFFSET = "HorizontalOffset";
    private static final String VERTICAL_OFFSET = "VerticalOffset";
    
    
    /** Get the configuration information for this ScaleBar  */
    public Node getNode() {
        Node tempRoot = new Node("SimpleScaleBarShader");
        
        // LineColor
        if (myDefaultLineColor != null) tempRoot.addAttribute(DEFAULT_LINE_COLOR, ""+myDefaultLineColor.getRGB());
        else tempRoot.addAttribute(DEFAULT_LINE_COLOR, "NONE");
        
        // LabelColor
        if (myDefaultLabelColor != null) tempRoot.addAttribute(DEFAULT_LABEL_COLOR, ""+myDefaultLabelColor.getRGB());
        else tempRoot.addAttribute(DEFAULT_LABEL_COLOR, "NONE");
    
        // LineWidth
        if (myStroke instanceof BasicStroke){
            tempRoot.addAttribute(DEFAULT_LINE_WIDTH, ""+((BasicStroke) myStroke).getLineWidth());
        }
        
        // Quadrant.
        tempRoot.addAttribute(QUADRANT, ""+myQuadrant);
        
        // Screen Percent
        tempRoot.addAttribute(SCREEN_PERCENT, ""+myScreenPercent);
        
        // Horizontan Offset
        tempRoot.addAttribute(HORIZONTAL_OFFSET, ""+myHorizontalOffset);
        
        // Vertical Offset
        tempRoot.addAttribute(VERTICAL_OFFSET, ""+myVerticalOffset);
        
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

        return tempRoot;
    }
    
    /** Set the configuration information for this ScaleBar  */
    public void setNode(Node inNode) throws Exception {
        
        // Line color
        String tempName = DEFAULT_LINE_COLOR;
        String tempValue = inNode.getAttribute(tempName);
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
        
        // Line Width
        String tempString = inNode.getAttribute(DEFAULT_LINE_WIDTH);
        if (tempString != null){
            try{
                float tempLineWidth = Float.parseFloat(tempString);
                setStroke(new BasicStroke(tempLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            catch (NumberFormatException e){
            }
        }
        
        // Quadrant.
        tempString = inNode.getAttribute(QUADRANT);
        if (tempString != null){
            try{
                int tempQuadrant = Integer.parseInt(tempString);
                setQuadrant(tempQuadrant);
            }
            catch (NumberFormatException e){
            }
        }
        
        // Screen Percent
        tempString = inNode.getAttribute(SCREEN_PERCENT);
        if (tempString != null){
            try{
                float tempPercent = Float.parseFloat(tempString);
                setScreenPercent((int) tempPercent);
            }
            catch (NumberFormatException e){
            }
        }
        
        // Horizontan Offset
        tempString = inNode.getAttribute(HORIZONTAL_OFFSET);
        if (tempString != null){
            try{
                int tempOffset = Integer.parseInt(tempString);
                setHorizontalOffset(tempOffset);
        
            }
            catch (Exception e){
            }
        }
        
        // Vertical Offset
        tempString = inNode.getAttribute(VERTICAL_OFFSET);
        if (tempString != null){
            try{
                int tempOffset = Integer.parseInt(tempString);
                setVerticalOffset(tempOffset);
        
            }
            catch (Exception e){
            }
        }        
        
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
                
        // the Label Font
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
    }
}
