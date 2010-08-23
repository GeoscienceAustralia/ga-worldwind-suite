package au.gov.ga.worldwind.animator.animation;

import java.awt.Dimension;

/**
 * A class that holds the render parameters associated with an animation 
 * (e.g. frame rate, dimensions etc.)
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RenderParameters
{
	private static final Dimension DEFAULT_DIMENSIONS = new Dimension(1024, 576);
	
	private static final int DEFAULT_FRAME_RATE = 25;
	
	/** 
	 * The dimension of the output image (in pixels)
	 * <p/>
	 * Defaults to 
	 */
	private Dimension imageDimension = DEFAULT_DIMENSIONS;
	
	/** 
	 * The frame rate of the output animation (in frames per second)
	 * <p/>
	 * Defaults to {@value #DEFAULT_FRAME_RATE} 
	 */
	private int frameRate = DEFAULT_FRAME_RATE;
	
	
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
	
}
