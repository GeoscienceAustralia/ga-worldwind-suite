package au.gov.ga.worldwind.animator.util;

import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * A super-interface for objects that can be 'enabled' or 'disabled'.
 * <p/>
 * When making an object 'enabled' or 'disabled', this status is propagated to any child {@link Enableable}s. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Enableable
{

	/**
	 * Return whether this object is currently enabled for the current {@link Animation}.
	 * <p/>
	 * An 
	 * @return whether this object is currently enabled
	 */
	boolean isEnabled();
	
	/**
	 * @return Whether all of this objects {@link Enableable} children are 'enabled'. If this object has no {@link Enableable} children, should return <code>true</code>.
	 */
	boolean isAllChildrenEnabled();
	
	/**
	 * Set whether this object is currently enabled for the current {@link Animation}.
	 * <p/>
	 * The 'enabled' status will be propagated to child objects (if applicable)
	 * 
	 * @param enabled Whether this object is currently enabled
	 */
	void setEnabled(boolean enabled);
	
}
