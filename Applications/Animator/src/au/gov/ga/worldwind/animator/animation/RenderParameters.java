package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Dimension;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.io.XmlSerializable;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A class that holds the render parameters associated with an animation 
 * (e.g. frame rate, dimensions etc.)
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RenderParameters implements AnimationObject, XmlSerializable<RenderParameters>
{
	public static final Dimension DEFAULT_DIMENSIONS = new Dimension(1024, 576);
	
	private static final int DEFAULT_FRAME_RATE = 25;
	
	/** 
	 * The dimension of the output image (in pixels)
	 * <p/>
	 * Defaults to 1024x576
	 */
	private Dimension imageDimension = DEFAULT_DIMENSIONS;
	
	/** 
	 * The frame rate of the output animation (in frames per second)
	 * <p/>
	 * Defaults to {@value #DEFAULT_FRAME_RATE} 
	 */
	private int frameRate = DEFAULT_FRAME_RATE;
	
	private String name = "RenderParameters";
	
	/**
	 * @return the {@link #imageDimension}
	 */
	public Dimension getImageDimension()
	{
		return imageDimension;
	}
	
	/**
	 * @param imageDimension the {@link #imageDimension} to set
	 */
	public void setImageDimension(Dimension imageDimension)
	{
		this.imageDimension = imageDimension;
	}
	
	/**
	 * @return the {@link #frameRate}
	 */
	public int getFrameRate()
	{
		return frameRate;
	}
	
	/**
	 * @param frameRate the {@link #frameRate} to set
	 */
	public void setFrameRate(int frameRate)
	{
		this.frameRate = frameRate;
	}

	/**
	 * @return The aspect ratio (<code>width:height</code>) of the image dimensions for the rendered output
	 */
	public double getImageAspectRatio()
	{
		return imageDimension.getWidth() / imageDimension.getHeight();
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		Validate.notNull(parent, "An XML element is required");
		Validate.notNull(version, "A version ID is required");
		
		AnimationIOConstants constants = version.getConstants();
		
		Element result = WWXML.appendElement(parent, constants.getRenderParametersElementName());
		
		WWXML.appendInteger(result, constants.getFrameRateElementName(), getFrameRate());
		WWXML.appendLong(result, constants.getWidthElementName(), Math.round(imageDimension.getWidth()));
		WWXML.appendLong(result, constants.getHeightElementName(), Math.round(imageDimension.getHeight()));
		
		return result;
	}

	@Override
	public RenderParameters fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version ID is required");
		Validate.notNull(context, "A context is required");
		
		AnimationIOConstants constants = version.getConstants();
		
		switch (version)
		{
			case VERSION020:
			{
				RenderParameters result = new RenderParameters();
				result.setFrameRate(WWXML.getInteger(element, constants.getFrameRateElementName(), null));
				
				int width = WWXML.getInteger(element, constants.getWidthElementName(), null);
				int height = WWXML.getInteger(element, constants.getHeightElementName(), null);
				result.setImageDimension(new Dimension(width, height));
				
				return result;
			}
		}
		
		return null;
	}

}
