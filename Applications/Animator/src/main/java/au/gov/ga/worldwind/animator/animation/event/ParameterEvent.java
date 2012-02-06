package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An {@link AnimationEvent} that is linked to a {@link Parameter}.
 * <p/>
 * {@link ParameterValueEvent}s may be generated when parameters are removed from,
 * or added to, animatable objects.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface ParameterEvent extends AnimationEvent
{

	Parameter getParameter();
	
}
