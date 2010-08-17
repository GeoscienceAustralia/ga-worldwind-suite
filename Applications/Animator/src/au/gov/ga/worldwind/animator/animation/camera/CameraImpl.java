/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.camera;

import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.math.interpolation.Interpolator;
import au.gov.ga.worldwind.animator.math.vector.Vector1;

/**
 * A default implementation of the {@link Camera} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class CameraImpl implements Camera
{
	
	// Note: Camera properties are stored individually so that more fine-grained control
	// can be achieved
	private Parameter<Vector1> eyeLat;
	private Parameter<Vector1> eyeLon;
	private Parameter<Vector1> eyeElevation;
	
	private Parameter<Vector1> lookAtLat;
	private Parameter<Vector1> lookAtLon;
	private Parameter<Vector1> lookAtElevation;
	
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
	public Parameter<Vector1> getEyeLat()
	{
		return eyeLat;
	}

	@Override
	public Parameter<Vector1> getEyeLon()
	{
		return eyeLon;
	}

	@Override
	public Parameter<Vector1> getEyeElevation()
	{
		return eyeElevation;
	}

	@Override
	public Parameter<Vector1> getLookAtLat()
	{
		return lookAtLat;
	}

	@Override
	public Parameter<Vector1> getLookAtLon()
	{
		return lookAtLon;
	}

	@Override
	public Parameter<Vector1> getLookAtElevation()
	{
		return lookAtElevation;
	}
}
