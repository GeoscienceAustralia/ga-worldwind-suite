/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.camera;

import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.math.vector.Vector3;

/**
 * A default implementation of the {@link Camera} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class CameraImpl implements Camera
{

	@Override
	public void apply(AnimationContext animationContext, int frame)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getRestorableState()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restoreState(String stateInXml)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Parameter<Vector3> getEyePosition()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Parameter<Vector3> getLookAtPosition()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
