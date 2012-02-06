package au.gov.ga.worldwind.animator.util;

/**
 * An interface for objects that can be named
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface Nameable
{

	/**
	 * @return The name of this object
	 */
	String getName();
	
	/**
	 * Set the name of this object
	 * 
	 * @param name The name to set
	 */
	void setName(String name);
	
}
