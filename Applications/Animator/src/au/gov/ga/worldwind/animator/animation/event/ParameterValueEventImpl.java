package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

/**
 * The default implementation of the {@link ParameterValueEvent} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ParameterValueEventImpl extends AnimationEventBase implements ParameterValueEvent
{

	public ParameterValueEventImpl(Type eventType, ParameterValue source, AnimationEvent cause)
	{
		super(eventType, source, cause);
	}

	@Override
	public ParameterValue getParameterValue()
	{
		return (ParameterValue)getSource();
	}

	

}
