package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.geom.Position;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;

/**
 * An {@link AbstractCameraPositionPath} that draws the current animation's lookat position along with nodes representing key frames.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LookatPositionPath extends AbstractCameraPositionPath
{

	public LookatPositionPath(Animation animation)
	{
		super(animation);
	}

	@Override
	protected Position[] getPathPositions(int startFrame, int endFrame)
	{
		return getAnimation().getCamera().getLookatPositionsBetweenFrames(startFrame, endFrame);
	}

	@Override
	protected boolean isPathFrame(KeyFrame keyFrame)
	{
		return keyFrame.hasValueForParameter(getAnimation().getCamera().getLookAtLat()) || 
		   	   keyFrame.hasValueForParameter(getAnimation().getCamera().getLookAtLon()) || 
		   	   keyFrame.hasValueForParameter(getAnimation().getCamera().getLookAtElevation());
	}

}
