package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;

import java.awt.Color;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * A key frame marker used to mark the position of the camera eye at a specific key frame.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class EyeKeyFrameMarker extends KeyFrameMarker
{
	private static final BasicMarkerAttributes DEFAULT_UNHIGHLIGHTED_ATTRIBUTES;
	static
	{
		DEFAULT_UNHIGHLIGHTED_ATTRIBUTES = new BasicMarkerAttributes();
		DEFAULT_UNHIGHLIGHTED_ATTRIBUTES.setMaterial(new Material(Color.BLUE));
	}
	
	public EyeKeyFrameMarker(Animation animation, int frame)
	{
		super(animation, frame, animation.getCamera().getEyePositionAtFrame(frame));
	}

	@Override
	public BasicMarkerAttributes getUnhighlightedAttributes()
	{
		return DEFAULT_UNHIGHLIGHTED_ATTRIBUTES;
	}

	@Override
	protected Parameter getLatParameter()
	{
		return getAnimation().getCamera().getEyeLat();
	}

	@Override
	protected Parameter getLonParameter()
	{
		return getAnimation().getCamera().getEyeLon();
	}

	@Override
	protected Parameter getElevationParameter()
	{
		return getAnimation().getCamera().getEyeElevation();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (!(obj instanceof EyeKeyFrameMarker))
		{
			return false;
		}
		
		return ((EyeKeyFrameMarker)obj).getFrame() == this.getFrame();
	}
	
	@Override
	public int hashCode()
	{
		return this.getFrame();
	}
	
}
