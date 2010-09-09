package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

/**
 * The default implementation of the {@link ParameterValueEvent} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ParameterValueEventImpl extends AnimationEventImpl implements ParameterValueEvent
{
	public ParameterValueEventImpl(Object owner, Type eventType, AnimationEvent cause, Object value)
	{
		super(owner, eventType, cause, value);
	}

	@Override
	public ParameterValue getParameterValue()
	{
		return (ParameterValue)getOwner();
	}

	

}
