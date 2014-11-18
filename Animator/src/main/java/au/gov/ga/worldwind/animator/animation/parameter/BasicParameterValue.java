/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.animation.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.event.ChangeableBase;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.common.util.Validate;

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

	private String name;
	
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
		setValue(value, false);
	}
	
	protected boolean setValue(double value, boolean inhibitEvent)
	{
		boolean changed = this.value != value;
		if (!changed)
		{
			return false;
		}
		
		this.value = value;
		
		if (!inhibitEvent)
		{
			fireChangeEvent(value);
		}
		
		return true;
	}

	@Override
	public void translate(double delta)
	{
		if (delta == 0.0)
		{
			return;
		}
		
		value += delta;
		fireChangeEvent(value);
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
		if (!changed)
		{
			return;
		}
		
		this.frame = frame;
		fireChangeEvent(frame);
	}
	
	@Override
	public void smooth()
	{
		// No smoothing applied to basic parameter values
	}

	@Override
	public String getName()
	{
		if (name == null)
		{
			return getOwner().getName() + "Value" + getFrame();
		}
		return name;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
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
	public ParameterValue clone()
	{
		BasicParameterValue result = new BasicParameterValue(value, frame, owner);
		result.setName(name);
		return result;
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
				result.setValue(Double.parseDouble(element.getAttribute(constants.getParameterValueAttributeValue())));
				result.setFrame(Integer.parseInt(element.getAttribute(constants.getParameterValueAttributeFrame())));
				
				result.setOwner((Parameter)context.getValue(constants.getParameterValueOwnerKey()));
				
				Validate.notNull(result.owner, "No owner found in the context. Expected type Parameter under key " + constants.getParameterValueOwnerKey());

				return result;
			}
			default:
			{
				return null;
			}
		}
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[Owner: " + getOwner().getName() + ", Frame: " + getFrame() + ", Value: " + getValue() + "]";
	}
}
