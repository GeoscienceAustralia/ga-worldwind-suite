package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.Animatable;

/**
 * An {@link AnimationEvent} that is linked to an animatable object.
 * <p/>
 * {@link AnimatableObjectEvent}s may be generated when parameters are added or removed from an {@link Animatable} object.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public interface AnimatableObjectEvent extends AnimationEvent
{

	Animatable getAnimatableObject();
	
}
