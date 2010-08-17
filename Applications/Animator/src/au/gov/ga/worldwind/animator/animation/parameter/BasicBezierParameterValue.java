/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import au.gov.ga.worldwind.animator.math.vector.Vector;

/**
 * A basic implementation of the {@link BezierParameterValue} interface.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicBezierParameterValue<V extends Vector<V>> extends BasicParameterValue<V> implements BezierParameterValue<V>
{

	/** 
	 * Whether or not this parameter is locked.
	 * 
	 * @see #isLocked()
	 */
	private boolean locked;
	
	/** The '<code>in</code>' value */
	private V in;
	
	/** The '<code>out</code>' value */
	private V out;
	
	/**
	 * @param value
	 * @param owner
	 * 
	 * TODO: Make this more applicable to beziers
	 */
	public BasicBezierParameterValue(V value, Parameter<V> owner)
	{
		super(value, owner);
	}
	
	@Override
	public void setInValue(V value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public V getInValue()
	{
		return in;
	}

	@Override
	public void setOutValue(V value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public V getOutValue()
	{
		return out;
	}

	@Override
	public boolean isLocked()
	{
		return locked;
	}

	@Override
	public void setLocked(boolean locked)
	{
		this.locked = locked;
	}

	

}
