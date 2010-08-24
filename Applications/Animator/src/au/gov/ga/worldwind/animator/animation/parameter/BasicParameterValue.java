/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A basic implementation of the {@link ParameterValue} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicParameterValue implements ParameterValue
{
	private static final long serialVersionUID = 20100823L;

	/** The owner of this value */
	private Parameter owner;
	
	/** The value of this {@link ParameterValue}*/
	private double value;
	
	/** The frame this {@link ParameterValue} is associated with */
	private int frame;
	
	/**
	 * Constructor. 
	 * <p/>
	 * Initialises the mandatory {@link #owner} and {@link #value} fields
	 */
	public BasicParameterValue(double value, int frame, Parameter owner) 
	{
		Validate.notNull(owner, "An owner is required");
		this.value = value;
		this.frame = frame;
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
	public double getValue()
	{
		return value;
	}

	@Override
	public void setValue(double value)
	{
		this.value = value;
	}

	@Override
	public Parameter getOwner()
	{
		return owner;
	}

	@Override
	public ParameterValueType getType()
	{
		return ParameterValueType.LINEAR;
	}
	
	@Override
	public int getFrame()
	{
		return frame;
	}
	
	@Override
	public void smooth()
	{
		// No smoothing applied to basic parameter values
	}
}
