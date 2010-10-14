package au.gov.ga.worldwind.animator.layers.camerapath;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * A key frame marker used to mark the position of the camera look-at at a specific key frame.
 */
public class LookatKeyFrameMarker extends KeyFrameMarker
{

	public LookatKeyFrameMarker(Animation animation, int frame)
	{
		super(animation, frame, animation.getCamera().getLookatPositionAtFrame(new AnimationContextImpl(animation), frame));
	}

	@Override
	protected Parameter getLatParameter()
	{
		return getAnimation().getCamera().getLookAtLat();
	}

	@Override
	protected Parameter getLonParameter()
	{
		return getAnimation().getCamera().getLookAtLon();
	}

	@Override
	protected Parameter getElevationParameter()
	{
		return getAnimation().getCamera().getLookAtElevation();
	}

}
