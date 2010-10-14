package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;

import java.awt.Color;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

/**
 * A key frame marker used to mark the position of the camera eye at a specific key frame.
 */
public class EyeKeyFrameMarker extends KeyFrameMarker
{
	private static final BasicMarkerAttributes DEFAULT_UNHIGHLIGHTED_ATTRIBUTES;
	static
	{
		DEFAULT_UNHIGHLIGHTED_ATTRIBUTES = new BasicMarkerAttributes();
		DEFAULT_UNHIGHLIGHTED_ATTRIBUTES.setMaxMarkerSize(2000d);
		DEFAULT_UNHIGHLIGHTED_ATTRIBUTES.setMaterial(new Material(Color.BLUE));
	}
	
	public EyeKeyFrameMarker(Animation animation, int frame)
	{
		super(animation, frame, animation.getCamera().getEyePositionAtFrame(new AnimationContextImpl(animation), frame));
	}

	@Override
	public void applyPositionChangeToAnimation()
	{
		KeyFrame currentKeyFrame = getAnimation().getKeyFrame(getFrame());
		
		ParameterValue eyeLatValue = currentKeyFrame.getValueForParameter(getAnimation().getCamera().getEyeLat());
		ParameterValue eyeLonValue = currentKeyFrame.getValueForParameter(getAnimation().getCamera().getEyeLon());
		ParameterValue eyeElevationValue = currentKeyFrame.getValueForParameter(getAnimation().getCamera().getEyeElevation());
		
		eyeLatValue.setValue(getPosition().latitude.degrees);
		eyeLonValue.setValue(getPosition().longitude.degrees);
		eyeElevationValue.setValue(getAnimation().applyZoomScaling(getPosition().elevation));
		
		// Smooth this key frame, and the ones either side of it
		eyeLatValue.smooth();
		eyeLonValue.smooth();
		eyeElevationValue.smooth();
		
		
	}
	
	@Override
	public BasicMarkerAttributes getUnhighlightedAttributes()
	{
		return DEFAULT_UNHIGHLIGHTED_ATTRIBUTES;
	}

}
