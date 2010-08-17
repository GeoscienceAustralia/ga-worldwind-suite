/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import au.gov.ga.worldwind.animator.math.vector.Vector;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A basic implementation of the {@link ParameterValue} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicParameterValue<V extends Vector<V>> implements ParameterValue<V>
{

	/** The owner of this value */
	private Parameter<V> owner;
	
	/** The value of this {@link ParameterValue}*/
	private V value;
	
	/**
	 * Constructor. 
	 * <p/>
	 * Initialises the mandatory {@link #owner} and {@link #value} fields
	 */
	public BasicParameterValue(V value, Parameter<V> owner) 
	{
		Validate.notNull(value, "A value is required");
		Validate.notNull(owner, "An owner is required");
		this.value = value;
		this.owner = owner;
	}
	
	@Override
	public String getRestorableState()
	{
		// TODO Implement me!
		return null;
	}

	@Override
	public void restoreState(String stateInXml)
	{
		// TODO Implement me!
	}

	@Override
	public V getValue()
	{
		return value;
	}

	@Override
	public void setValue(V value)
	{
		this.value = value;
	}

	@Override
	public Parameter<V> getOwner()
	{
		return owner;
	}

}
