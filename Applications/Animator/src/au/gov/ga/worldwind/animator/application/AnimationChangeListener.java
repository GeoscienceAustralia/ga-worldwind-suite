package au.gov.ga.worldwind.animator.application;

import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * An interface for listeners that want to be notified
 * when the current animation of the Animator application
 * changes.
 */
public interface AnimationChangeListener
{
	/**
	 * Notified when the current animation is changed 
	 */
	void updateAnimation(Animation newAnimation);
	
}
