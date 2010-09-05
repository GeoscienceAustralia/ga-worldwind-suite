/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.util.ChangeableBase;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A basic implementation of the {@link ParameterValue} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicParameterValue extends ChangeableBase implements ParameterValue
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
		addChangeListener(owner);
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
		boolean changed = this.value != value;
		
		this.value = value;
		
		if (changed)
		{
			notifyChange();
		}
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
		boolean changed = this.frame != frame;
		
		this.frame = frame;
		
		if (changed)
		{
			notifyChange();
		}
	}
	
	@Override
	public void smooth()
	{
		// No smoothing applied to basic parameter values
	}

	/**
	 * Set the owner of this value. Should only be invoked during de-serialisation.
	 */
	protected void setOwner(Parameter p)
	{
		removeChangeListener(this.owner);
		addChangeListener(p);
		this.owner = p;
	}
	
	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();
		
		Element result = WWXML.appendElement(parent, constants.getParameterValueElementName());
		
		WWXML.setTextAttribute(result, constants.getParameterValueAttributeType(), getType().name());
		WWXML.setIntegerAttribute(result, constants.getParameterValueAttributeFrame(), getFrame());
		WWXML.setDoubleAttribute(result, constants.getParameterValueAttributeValue(), getValue());
		
		return result;
	}

	@Override
	public ParameterValue fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version ID is required");
		Validate.notNull(context, "A context is required");
		
		AnimationIOConstants constants = version.getConstants();
		
		switch (version)
		{
			case VERSION020:
			{
				BasicParameterValue result = new BasicParameterValue();
				result.setValue(WWXML.getDouble(element, ATTRIBUTE_PATH_PREFIX + constants.getParameterValueAttributeValue(), null));
				result.setFrame(WWXML.getInteger(element, ATTRIBUTE_PATH_PREFIX + constants.getParameterValueAttributeFrame(), null));
				
				result.owner = (Parameter)context.getValue(constants.getParameterValueOwnerKey());
				
				Validate.notNull(result.owner, "No owner found in the context. Expected type Parameter under key " + constants.getParameterValueOwnerKey());

				return result;
			}
			default:
			{
				return null;
			}
		}
	}
	
}
