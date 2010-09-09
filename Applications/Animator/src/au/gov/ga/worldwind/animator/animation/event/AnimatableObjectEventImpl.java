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

	public AnimatableObjectEventImpl(Type eventType, Animatable source, AnimationEvent cause)
	{
		super(eventType, source, cause);
	}

	@Override
	public Animatable getAnimatableObject()
	{
		return (Animatable)getSource();
	}

}
