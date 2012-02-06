package au.gov.ga.worldwind.animator.util;

/**
 * A super-interface for anything that can be 'armed' or 'disarmed'
 * <p/>
 * When making an object 'armed' or 'disarmed', this status is propagated to any
 * child {@link Armable}s.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Armable
{

	/**
	 * Returns whether this object is 'armed' (i.e. whether it should be
	 * included in the creation of key frames or not).
	 * <p/>
	 * Different from 'enabled' in that a 'disarmed' object is still included in
	 * the animation, just not in the creation of key frames.
	 */
	boolean isArmed();

	/**
	 * Set whether or not this object is 'armed'.
	 * 
	 * @see #isArmed()
	 */
	void setArmed(boolean armed);

	/**
	 * @return Whether <em>all</em> of this objects {@link Armable} children are
	 *         'armed'. If this object has no {@link Armable} children, should
	 *         return <code>true</code>.
	 */
	boolean isAllChildrenArmed();

	/**
	 * @return Whether <em>any</em> of this objects {@link Armable} children are
	 *         'armed'. If this object has no {@link Armable} children, should
	 *         return <code>false</code>.
	 */
	boolean hasArmedChildren();

	/**
	 * Add an {@link Armable} which is codependant on this {@link Armable} (ie,
	 * whenever this is armed, armable is armed, and vice versa). Must also call
	 * {@link #connectCodependantArmable(Armable)} on the armable provided (and
	 * check that the armable has not already been added to prevent a stack
	 * overflow).
	 */
	void connectCodependantArmable(Armable armable);
}
