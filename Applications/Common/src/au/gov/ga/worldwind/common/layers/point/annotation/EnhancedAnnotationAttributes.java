package au.gov.ga.worldwind.common.layers.point.annotation;

import gov.nasa.worldwind.render.AnnotationAttributes;

/**
 * Enhanced annotation attributes that contains additional attributes
 * for use with the {@link EnhancedAnnotation} class.
 */
public class EnhancedAnnotationAttributes extends AnnotationAttributes
{
	private static final Double DEFAULT_FADE_DISTANCE = 4E4;

	/**
	 * Create a new instance initialised with the values from the provided {@link AnnotationAttributes}.
	 */
	public EnhancedAnnotationAttributes(AnnotationAttributes attributes)
	{
		super.setDefaults(attributes);
	}
	
	/**
	 * Create a new instance initialised default values
	 */
	public EnhancedAnnotationAttributes()
	{
	}
	
	// Thresholds to control the annotation visibility based on distance from the camera eye to the marker
	private Double minEyeDistance = null;
	private Double maxEyeDistance = null;
	
	// The distance it takes to fade from 0% to 100% opacity
	private Double fadeDistance = DEFAULT_FADE_DISTANCE;
	
	public Double getMinEyeDistance()
	{
		return minEyeDistance;
	}
	
	public void setMinEyeDistance(Double minEyeDistance)
	{
		this.minEyeDistance = minEyeDistance;
	}
	
	public Double getMaxEyeDistance()
	{
		return maxEyeDistance;
	}

	public void setMaxEyeDistance(Double maxEyeDistance)
	{
		this.maxEyeDistance = maxEyeDistance;
	}
	
	public Double getFadeDistance()
	{
		return fadeDistance == null ? 1d : fadeDistance;
	}
	
	public void setFadeDistance(Double fadeDistance)
	{
		this.fadeDistance = fadeDistance;
	}
}
