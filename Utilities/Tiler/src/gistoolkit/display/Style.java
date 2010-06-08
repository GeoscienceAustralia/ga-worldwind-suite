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

package gistoolkit.display;

import gistoolkit.features.*;
import java.awt.*;
import java.util.*;
import gistoolkit.common.*;
import gistoolkit.display.shader.*;
import gistoolkit.display.renderer.*;
import gistoolkit.display.labeler.*;
/**
 * Class to represent the style of a layer.  This style contains the information about how an object
 * should draw itself.
 */
public class Style {
    /** Name for this style. */
    private String myStyleName = "Style";
    /** Set the name of this style. */
    public void setStyleName(String inStyleName){myStyleName = inStyleName;}
    /** Get the name of this style. */
    public String getStyleName(){return myStyleName;}
    
    /** Title for this style. */
    private String myStyleTitle = "Style";
    /** Set the title of this style. */
    public void setStyleTitle(String inStyleTitle){myStyleTitle = inStyleTitle;}
    /** Get the title of this style. */
    public String getStyleTitle(){return myStyleTitle;}

    /** There may be several renderers for different layers.  Typically though, there may only be one.*/
    private Vector myRenderers = new Vector();
    /** Add a renderer to this shape.*/
    public void add(Renderer inRenderer){myRenderers.insertElementAt(inRenderer, 0);}
    /** Remove a renderer from this shape*/
    public void remove(Renderer inRenderer) {myRenderers.removeElement(inRenderer);}
    /** Return the number of renderers */
    public int getNumRenderers(){return myRenderers.size();}
    /** Set the Renderer*/
    public void setRenderer(int inIndex, Renderer inRenderer){myRenderers.setElementAt(inRenderer, inIndex);}
    /** Retrieve the renderer at the given index */
    public Renderer getRenderer(int inIndex){return (Renderer) myRenderers.elementAt(inIndex);}
    /** Remove all the renderers from this Style. */
    public void removeAllRenderers(){myRenderers.removeAllElements();}
    
    /** There may be serveral labelers for different layers, Typically though, there may only be one.*/
    private Vector myLabelers = new Vector();
    /** Add a labeler to this layer.*/
    public void add(Labeler inLabeler){ myLabelers.insertElementAt(inLabeler, 0);}
    /** Remove a labeler from this shape*/
    public void remove(Labeler inLabeler){ myLabelers.removeElement(inLabeler);}
    /** Return the number of labelers */
    public int getNumLabelers(){return myLabelers.size();}
    /** Set the labeler*/
    public void setLabeler(int inIndex, Labeler inLabeler){ myLabelers.setElementAt(inLabeler, inIndex);}
    /** Retrieve the labeler at the given index */
    public Labeler getLabeler(int inIndex){ return (Labeler) myLabelers.elementAt(inIndex);}
    /** Remove all the labelers from this Style. */
    public void removeAllLabelers(){ myLabelers.removeAllElements();}
    
    /** Shader to determine what color to draw the features and what pen shape to use. */
    private Shader myShader = new MonoShader();
    /** Get the shader to use with this style.  The shader will determine the colors to use, and the pen to use. */
    public Shader getShader() {return myShader;}
    /** Set the shader to use with this style.  The shader will determine the colors to use, and the pen to use. */            
    public void setShader(Shader newShader) {myShader = newShader;}
    
    /** Shader to determine what color to draw the features and what pen shape to use, draws over the regular shader. */
    private Shader myTopShader = null;
    /** Get the shader to use with this style.  The shader will determine the colors to use, and the pen to use, draws over the regular shader. */
    public Shader getTopShader() {return myTopShader;}
    /** Set the shader to use with this style.  The shader will determine the colors to use, and the pen to use, draws over the regular shader. */            
    public void setTopShader(Shader newTopShader) {myTopShader = newTopShader;}

    /**
     * Constructs a new style with a single Feature Renderer, and a MonoShader with the default color scheme for Shaders.
     */
    public Style() {
        super();
        setShader(new MonoShader());
        add(new FeatureRenderer());
        add(new FeatureLabeler());
    }
        
    /**
     * Function called when a shape is to be drawn.
     */
    public void drawHighlight(Record inRecord, Graphics inGraphics, Converter inConverter){
        
        // find a renderer for this shape
        boolean tempDrawn = false;
        for (int i=0; i<myRenderers.size(); i++){
            
            tempDrawn = ((Renderer) myRenderers.elementAt(i)).drawShapeHighlight(inRecord, inGraphics, inConverter, myShader);
            if (tempDrawn)break;
        }
        
        // find a labeler for this shape
        tempDrawn = false;
        for (int i=0; i<myLabelers.size(); i++){
            
            tempDrawn = ((Labeler) myLabelers.elementAt(i)).drawLabelHighlight(inRecord, inGraphics, inConverter, myShader);
            if (tempDrawn)break;
        }
    }

    /**
     * Function called when a shape is to be drawn.
     */
    public void drawLabel(Record inRecord, Graphics inGraphics, Converter inConverter){
                
        // find a labeler for this shape
        boolean tempDrawn = false;
        for (int i=0; i<myLabelers.size(); i++){
            
            tempDrawn = ((Labeler) myLabelers.elementAt(i)).drawLabel(inRecord, inGraphics, inConverter, myShader);
            if (tempDrawn)break;
        }
    }

    
    /**
     * Function called when a shape is to be drawn.
     */
    public void drawShape(Record inRecord, Graphics inGraphics, Converter inConverter){
        drawShape(inRecord, inGraphics, inConverter, false);
    }
    
    /**
     * Function called when a shape is to be drawn, boolean if the top shader should be used.
     */
    public void drawShape(Record inRecord, Graphics inGraphics, Converter inConverter, boolean inTop){
        
        // find a renderer for this shape
        boolean tempDrawn = false;
        Shader tempShader = myShader;
        if (inTop) tempShader = myTopShader;
        if (tempShader == null) return;
        for (int i=0; i<myRenderers.size(); i++){
            
            tempDrawn = ((Renderer) myRenderers.elementAt(i)).drawShape(inRecord, inGraphics, inConverter, tempShader);
            if (tempDrawn)break;
        }
    }
        
    /**
     * Constructs a new style with the given Renderer (if null is specified, then no renderer is used), and a MonoShader with the default color scheme for Shaders.
     */
    public Style(Renderer inRenderer) {
        super();
        if (inRenderer != null){
            add(inRenderer);
        }
    }
    
    /**
     * Constructs a new style with the given Renderer (if null is specified, then no renderer is used), and the given shader (if null is specified then a mono shader is used with the default color scheme for mono shaders).
     */
    public Style(Renderer inRenderer, Shader inShader) {
        super();
        if (inRenderer != null){
            add(inRenderer);
        }
        if (inShader != null){
            myShader = inShader;
        }
    }
    
    private static final String STYLE_NAME = "StyleName";
    private static final String STYLE_TITLE = "StyleTitle";
    private static final String SHADER_NODE = "Shader";
    private static final String TOP_SHADER_NODE = "TopShader";
    private static final String SHADER_CLASS = "ShaderClass";    
    private static final String RENDERER_NODE = "Renderer";
    private static final String RENDERER_CLASS = "RendererClass";
    private static final String LABELER_NODE = "Labeler";
    private static final String LABELER_CLASS = "LabelerClass";
    
    /** Retrieve the configuration information for this style */
    public Node getNode(){
        // Save the style information
        Node tempRoot = new Node("Style");
        tempRoot.addAttribute(STYLE_NAME, getStyleName());
        tempRoot.addAttribute(STYLE_TITLE, getStyleTitle());
        
        // retrieve the Shader information
        Node tempShader = new Node(SHADER_NODE);
        tempShader.addAttribute(SHADER_CLASS, myShader.getClass().getName());
        tempShader.addChild(myShader.getNode());
        tempRoot.addChild(tempShader);
        
        // retrieve the top shader information
        if (myTopShader != null){
            Node tempTopShader = new Node(TOP_SHADER_NODE);
            tempRoot.addChild(tempTopShader);
            tempTopShader.addAttribute(SHADER_CLASS, myTopShader.getClass().getName());
            tempTopShader.addChild(myTopShader.getNode());
        }
        
        // retrieve the Renderer Information
        for (int i=0; i<myRenderers.size(); i++){
            Renderer tempRenderer = (Renderer) myRenderers.elementAt(i);
            if (tempRenderer != null){
                Node tempRendererNode = new Node(RENDERER_NODE);
                tempRendererNode.addAttribute(RENDERER_CLASS, tempRenderer.getClass().getName());
                tempRendererNode.addChild(tempRenderer.getNode());
                tempRoot.addChild(tempRendererNode);
            }
        }

        // retrieve the Labeler Information
        for (int i=0; i<myLabelers.size(); i++){
            Labeler tempLabeler = (Labeler) myLabelers.elementAt(i);
            if (tempLabeler != null){
                Node tempLabelerNode = new Node(LABELER_NODE);
                tempLabelerNode.addAttribute(LABELER_CLASS, tempLabeler.getClass().getName());
                tempLabelerNode.addChild(tempLabeler.getNode());
                tempRoot.addChild(tempLabelerNode);
            }
        }
        
        return tempRoot;
    }
    
    /** Set the configuration information for this style */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception("No configuration information for style ");
        // Style Information
        setStyleName(inNode.getAttribute(STYLE_NAME));
        setStyleTitle(inNode.getAttribute(STYLE_TITLE));
        
        // Shader information
        Node tempShaderNode = inNode.getChild(SHADER_NODE);
        if (tempShaderNode != null) {
            String tempShaderClass = tempShaderNode.getAttribute(SHADER_CLASS);
            if (tempShaderClass != null){
                Shader tempShader = (Shader) Class.forName(tempShaderClass).newInstance();
                Node[] tempNodes = tempShaderNode.getChildren();
                if (tempNodes.length > 0){
                    tempShader.setNode(tempNodes[0]);
                }
                setShader(tempShader);
            }
        }
        
        // TopShader information
        Node tempTopShaderNode = inNode.getChild(TOP_SHADER_NODE);
        if (tempTopShaderNode != null) {
            String tempShaderClass = tempTopShaderNode.getAttribute(SHADER_CLASS);
            if (tempShaderClass != null){
                Shader tempShader = (Shader) Class.forName(tempShaderClass).newInstance();
                Node[] tempNodes = tempTopShaderNode.getChildren();
                if (tempNodes.length > 0){
                    tempShader.setNode(tempNodes[0]);
                }
                setTopShader(tempShader);
            }
        }

        // Renderer INformation
        Node[] tempNodes = inNode.getChildren(RENDERER_NODE);
        if (tempNodes.length > 0) myRenderers.removeAllElements();
        for (int i=tempNodes.length-1; i>=0; i--){
            String tempRendererString = tempNodes[i].getAttribute(RENDERER_CLASS);
            if (tempRendererString != null){
                Renderer tempRenderer = (Renderer) Class.forName(tempRendererString).newInstance();
                Node[] tempConfNodes = tempNodes[i].getChildren();
                if (tempConfNodes.length > 0){
                    tempRenderer.setNode(tempConfNodes[0]);
                }
                add(tempRenderer);
            }
        }
        
        // Labeler Information
        tempNodes = inNode.getChildren(LABELER_NODE);
        if (tempNodes.length > 0) myLabelers.removeAllElements();
        for (int i=0; i<tempNodes.length; i++){
            String tempLabelerString = tempNodes[i].getAttribute(LABELER_CLASS);
            if (tempLabelerString != null){
                if (tempLabelerString.equals("gistoolkit.display.labeler.SimpleLabeler")) tempLabelerString = "gistoolkit.display.labeler.FeatureLabeler";
                Labeler tempLabeler = (Labeler) Class.forName(tempLabelerString).newInstance();
                Node[] tempConfNodes = tempNodes[i].getChildren();
                if (tempConfNodes.length > 0){
                   tempLabeler.setNode(tempConfNodes[0]);
                }
                add(tempLabeler);
            }
        }
    }
    
    /**
     * Calls the beginDraw() method on the renderers.
     */
    public void beginDraw(){
        for (int i=0; i<myRenderers.size(); i++){
            Renderer tempRenderer = (Renderer) myRenderers.get(i);
            if (tempRenderer != null) tempRenderer.beginDraw();
        }
    }
    /**
     * Calls the endDraw() method on the renderers.
     */
    public void endDraw(){
        for (int i=0; i<myRenderers.size(); i++){
            Renderer tempRenderer = (Renderer) myRenderers.get(i);
            if (tempRenderer != null) tempRenderer.endDraw();
        }
    }
    /**
     * Calls the beginLabel() method on the labelers.
     */
    public void beginLabel(){
        for (int i=0; i<myLabelers.size(); i++){
            Labeler tempLabeler = (Labeler) myLabelers.get(i);
            if (tempLabeler != null) tempLabeler.beginLabel();
        }
    }
    /**
     * Calls the endLabel() method on the labelers.
     */
    public void endLabel(){
        for (int i=0; i<myLabelers.size(); i++){
            Labeler tempLabeler = (Labeler) myLabelers.get(i);
            if (tempLabeler != null) tempLabeler.endLabel();
        }
    }
}