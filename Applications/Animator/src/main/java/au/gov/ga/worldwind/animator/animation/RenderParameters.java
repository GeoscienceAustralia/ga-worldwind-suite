package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Dimension;
import java.io.File;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.io.XmlSerializable;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * A class that holds the render parameters associated with an animation 
 * (e.g. frame rate, dimensions etc.)
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RenderParameters implements AnimationObject, XmlSerializable<RenderParameters>, Cloneable
{
	public static final Dimension DEFAULT_DIMENSIONS = new Dimension(1024, 576);
	private static final int DEFAULT_IMAGE_SCALE_PERCENT = 100; 
	private static final int DEFAULT_FRAME_RATE = 25;
	private static final double DEFAULT_DETAIL_LEVEL = 1.0d;
	private static final boolean DEFAULT_LOCKED_DIMENSIONS = true;
	private static final boolean DEFAULT_RENDER_ALPHA = true;
	
	/** 
	 * The dimension of the output image (in pixels)
	 * <p/>
	 * Defaults to 1024x576
	 */
	private Dimension imageDimension = DEFAULT_DIMENSIONS;
	
	/** A percentage scaler applied to the base image dimensions to obtain the render dimensions */
	private int imageScalePercent = DEFAULT_IMAGE_SCALE_PERCENT;
	
	/** 
	 * The frame rate of the output animation (in frames per second)
	 * <p/>
	 * Defaults to {@value #DEFAULT_FRAME_RATE} 
	 */
	private int frameRate = DEFAULT_FRAME_RATE;
	
	/** Whether or not to lock the aspect ratio of the image dimensions */
	private boolean lockedDimensions = DEFAULT_LOCKED_DIMENSIONS;
	
	/** The base target file for rendered frames */
	private File renderDestination = null;
	
	/** The frame to begin rendering from */
	private Integer startFrame = null;
	
	/** The frame to finish rendering on */
	private Integer endFrame = null;
	
	/** 
	 * The detail level to use when rendering.
	 * <p/>
	 * May be used to control things like the terrain mesh density etc.
	 */
	private double detailLevel = DEFAULT_DETAIL_LEVEL;
	
	/**
	 * Whether or not to support alpha channels when rendering.
	 * <p/>
	 * Defaults to <code>true</code>.
	 */
	private boolean renderAlpha = DEFAULT_RENDER_ALPHA;
	
	private String name = "RenderParameters";
	
	/**
	 * @return The base {@link #imageDimension}, before scaling.
	 */
	public Dimension getImageDimension()
	{
		return imageDimension;
	}
	
	public void setImageDimension(Dimension imageDimension)
	{
		this.imageDimension = imageDimension;
	}
	
	/**
	 * @return The dimensions of the output render (after {@link #imageScalePercent} is applied to the base {@link #imageDimension})
	 */
	public Dimension getRenderDimension()
	{
		double scaler = imageScalePercent / 100d;
		return new Dimension((int)(imageDimension.width * scaler), (int)(imageDimension.height * scaler));
	}
	
	public boolean isRenderDestinationSet()
	{
		return getRenderDestination() != null;
	}
	
	public int getFrameRate()
	{
		return frameRate;
	}
	
	public void setFrameRate(int frameRate)
	{
		this.frameRate = frameRate;
	}

	public double getImageAspectRatio()
	{
		return imageDimension.getWidth() / imageDimension.getHeight();
	}
	
	public boolean isDimensionsLocked()
	{
		return lockedDimensions;
	}
	
	public void setDimensionsLocked(boolean locked)
	{
		this.lockedDimensions = locked;
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
	
	public File getRenderDestination()
	{
		return renderDestination;
	}
	
	public void setRenderDestination(File destination)
	{
		this.renderDestination = destination;
	}
	
	public File getRenderDirectory()
	{
		if (renderDestination == null)
		{
			return null;
		}
		return renderDestination.getParentFile();
	}
	
	public String getFrameName()
	{
		if (renderDestination == null)
		{
			return null;
		}
		return renderDestination.getName();
	}
	
	public Integer getStartFrame()
	{
		return startFrame;
	}

	public void setStartFrame(Integer startFrame)
	{
		this.startFrame = startFrame;
	}

	public Integer getEndFrame()
	{
		return endFrame;
	}

	public void setEndFrame(Integer endFrame)
	{
		this.endFrame = endFrame;
	}
	
	public void setFrameRange(int startFrame, int endFrame)
	{
		setStartFrame(startFrame);
		setEndFrame(endFrame);
	}
	
	public boolean isFrameRangeSet()
	{
		return startFrame != null && endFrame != null;
	}
	
	public double getDetailLevel()
	{
		return detailLevel;
	}

	public void setDetailLevel(double detailHint)
	{
		this.detailLevel = detailHint;
	}

	public boolean isRenderAlpha()
	{
		return renderAlpha;
	}

	public void setRenderAlpha(boolean renderAlpha)
	{
		this.renderAlpha = renderAlpha;
	}

	public int getImageScalePercent()
	{
		return imageScalePercent;
	}

	public void setImageScalePercent(int imageScalePercent)
	{
		this.imageScalePercent = imageScalePercent;
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
		WWXML.appendBoolean(result, constants.getLockedDimensionsElementName(), lockedDimensions);
		if (renderDestination != null)
		{
			XMLUtil.appendText(result, constants.getRenderDestinationElementName(), renderDestination.toURI().toString());
		}
		if (startFrame != null)
		{
			WWXML.appendInteger(result, constants.getFrameStartElementName(), startFrame);
		}
		if (endFrame != null)
		{
			WWXML.appendInteger(result, constants.getFrameEndElementName(), endFrame);
		}
		WWXML.appendDouble(result, constants.getDetailLevelElementName(), detailLevel);
		WWXML.appendBoolean(result, constants.getRenderAlphaElementName(), renderAlpha);
		WWXML.appendInteger(result, constants.getImageScalePercentElementName(), imageScalePercent);
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
				result.setFrameRate(XMLUtil.getInteger(element, constants.getFrameRateElementName(), DEFAULT_FRAME_RATE, null));
				
				int width = XMLUtil.getInteger(element, constants.getWidthElementName(), DEFAULT_DIMENSIONS.width, null);
				int height = XMLUtil.getInteger(element, constants.getHeightElementName(), DEFAULT_DIMENSIONS.height, null);
				result.setImageDimension(new Dimension(width, height));
				
				result.setDimensionsLocked(XMLUtil.getBoolean(element, constants.getLockedDimensionsElementName(), DEFAULT_LOCKED_DIMENSIONS));
				
				result.setRenderDestination(XMLUtil.getFile(element, constants.getRenderDestinationElementName()));
				
				result.setStartFrame(XMLUtil.getInteger(element, constants.getFrameStartElementName(), null));
				result.setEndFrame(XMLUtil.getInteger(element, constants.getFrameEndElementName(), null));
				
				result.setDetailLevel(XMLUtil.getDouble(element, constants.getDetailLevelElementName(), DEFAULT_DETAIL_LEVEL));
				
				result.setRenderAlpha(XMLUtil.getBoolean(element, constants.getRenderAlphaElementName(), DEFAULT_RENDER_ALPHA));
				
				result.setImageScalePercent(XMLUtil.getInteger(element, constants.getImageScalePercentElementName(), DEFAULT_IMAGE_SCALE_PERCENT));
				
				return result;
			}
		}
		
		return null;
	}

	public RenderParameters clone()
	{
		RenderParameters result = new RenderParameters();
		result.setDetailLevel(detailLevel);
		result.setDimensionsLocked(lockedDimensions);
		result.setEndFrame(endFrame);
		result.setStartFrame(startFrame);
		result.setFrameRate(frameRate);
		result.setImageDimension(imageDimension);
		result.setRenderAlpha(renderAlpha);
		result.setRenderDestination(renderDestination);
		return result;
	}
}
