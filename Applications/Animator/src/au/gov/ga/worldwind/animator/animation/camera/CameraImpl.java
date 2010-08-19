/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.camera;

import java.util.Collection;

import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

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
	private Parameter eyeLat;
	private Parameter eyeLon;
	private Parameter eyeElevation;
	
	private Parameter lookAtLat;
	private Parameter lookAtLon;
	private Parameter lookAtElevation;
	
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
	public Parameter getEyeLat()
	{
		return eyeLat;
	}

	@Override
	public Parameter getEyeLon()
	{
		return eyeLon;
	}

	@Override
	public Parameter getEyeElevation()
	{
		return eyeElevation;
	}

	@Override
	public Parameter getLookAtLat()
	{
		return lookAtLat;
	}

	@Override
	public Parameter getLookAtLon()
	{
		return lookAtLon;
	}

	@Override
	public Parameter getLookAtElevation()
	{
		return lookAtElevation;
	}
	
	@Override
	public Collection<Parameter> getParameters()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Collection<Parameter> getEnabledParameters()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
