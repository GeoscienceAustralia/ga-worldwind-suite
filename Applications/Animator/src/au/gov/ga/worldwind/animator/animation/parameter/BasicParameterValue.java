/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
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
	
	/**
	 * Constructor. Required for de-serialisation. Not for general use.
	 */
	protected BasicParameterValue()	{}

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
	public void setFrame(int frame)
	{
		this.frame = frame;
	}
	
	@Override
	public void smooth()
	{
		// No smoothing applied to basic parameter values
	}

	@Override
	public Element toXml(Element parent)
	{
		Element result = WWXML.appendElement(parent, "parameterValue");
		
		WWXML.setTextAttribute(result, "type", getType().name());
		WWXML.setIntegerAttribute(result, "frame", getFrame());
		WWXML.setDoubleAttribute(result, "value", getValue());
		
		return result;
	}

	@Override
	public ParameterValue fromXml(Element element, AnimationFileVersion versionId, AVList context)
	{
		return null;
	}
	
	
}
