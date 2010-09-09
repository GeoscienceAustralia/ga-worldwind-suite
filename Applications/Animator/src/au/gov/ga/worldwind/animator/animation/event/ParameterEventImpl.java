package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * The default implementation of the {@link ParameterEvent} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class ParameterEventImpl extends AnimationEventImpl implements ParameterEvent
{

	public ParameterEventImpl(Parameter owner, Type eventType, AnimationEvent cause, Object value)
	{
		super(owner, eventType, cause, value);
	}

	@Override
	public Parameter getParameter()
	{
		return (Parameter)getOwner();
	}

}
