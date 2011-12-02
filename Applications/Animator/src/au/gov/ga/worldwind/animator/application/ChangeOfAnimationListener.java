package au.gov.ga.worldwind.animator.application;

import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * An interface for listeners that want to be notified
 * when the current animation of the Animator application
 * changes.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ChangeOfAnimationListener
{
	/**
	 * Notified when the current animation object is changed 
	 */
	void updateAnimation(Animation newAnimation);
	
}
