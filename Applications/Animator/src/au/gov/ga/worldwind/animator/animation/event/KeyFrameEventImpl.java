package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.KeyFrame;

/**
 * The default implementation of the {@link KeyFrameEvent} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class KeyFrameEventImpl extends AnimationEventImpl implements KeyFrameEvent
{

	public KeyFrameEventImpl(KeyFrame owner, Type eventType, AnimationEvent cause, Object value)
	{
		super(owner, eventType, cause, value);
	}

	@Override
	public KeyFrame getKeyFrame()
	{
		return (KeyFrame)getOwner();
	}
}
