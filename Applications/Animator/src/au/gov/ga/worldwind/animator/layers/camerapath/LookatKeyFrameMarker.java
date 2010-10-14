package au.gov.ga.worldwind.animator.layers.camerapath;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;

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
	public void applyPositionChangeToAnimation()
	{
		// TODO Auto-generated method stub

	}

}
