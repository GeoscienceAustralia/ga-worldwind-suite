package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.KeyFrame;

/**
 * An {@link AnimationEvent} that is linked to a Key Frame.
 * <p/>
 * {@link KeyFrameEvent}s may be generated when key frames are added, removed, moved or changed.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface KeyFrameEvent extends AnimationEvent
{
	
	KeyFrame getKeyFrame();
	
}
