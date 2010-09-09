package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.Animatable;

/**
 * The default implementation of the {@link AnimatableObjectEvent} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class AnimatableObjectEventImpl extends AnimationEventImpl implements AnimatableObjectEvent
{
	public AnimatableObjectEventImpl(Animatable owner, Type eventType, AnimationEvent cause, Object value)
	{
		super(owner, eventType, cause, value);
	}

	@Override
	public Animatable getAnimatableObject()
	{
		return (Animatable)getOwner();
	}

}
