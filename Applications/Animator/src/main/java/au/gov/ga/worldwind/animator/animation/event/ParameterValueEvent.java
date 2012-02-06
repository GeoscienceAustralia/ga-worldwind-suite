package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

/**
 * An {@link AnimationEvent} that is linked to a {@link ParameterValue}.
 * <p/>
 * {@link ParameterValueEvent}s may be generated when parameter values are changed.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface ParameterValueEvent extends AnimationEvent
{
	
	ParameterValue getParameterValue();
	
}
