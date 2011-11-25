package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.geom.Position;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;

/**
 * An {@link AbstractCameraPositionPath} that draws the current animation's eye position along with nodes representing key frames.
 */
class EyePositionPath extends AbstractCameraPositionPath
{
	
	public EyePositionPath(Animation animation)
	{
		super(animation);
	}

	@Override
	protected Position[] getPathPositions(int startFrame, int endFrame)
	{
		return getAnimation().getCamera().getEyePositionsBetweenFrames(startFrame, endFrame);
	}
	
	@Override
	protected boolean isPathFrame(KeyFrame keyFrame)
	{
		return keyFrame.hasValueForParameter(getAnimation().getCamera().getEyeLat()) || 
			   keyFrame.hasValueForParameter(getAnimation().getCamera().getEyeLon()) || 
			   keyFrame.hasValueForParameter(getAnimation().getCamera().getEyeElevation());
	}
	
}
