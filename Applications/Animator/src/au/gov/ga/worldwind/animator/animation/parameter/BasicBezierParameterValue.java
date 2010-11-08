package au.gov.ga.worldwind.animator.animation.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.util.Validate;


/**
 * A basic implementation of the {@link BezierParameterValue} interface.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicBezierParameterValue extends BasicParameterValue implements BezierParameterValue
{
	private static final long serialVersionUID = 20100819L;

	/** The default control point percentage to use */
	private static final double DEFAULT_CONTROL_POINT_PERCENTAGE = 0.4;
	
	/** 
	 * Whether or not this parameter is locked.
	 * 
	 * @see #isLocked()
	 */
	private boolean locked;
	
	/** The '<code>in</code>' control point value */
	private BezierContolPoint in = new BezierContolPoint();
	
	/** The '<code>out</code>' control point value */
	private BezierContolPoint out = new BezierContolPoint();
	
	/**
	 * Constructor. Required for de-serialisation. Not for general use.
	 */
	protected BasicBezierParameterValue(){
		super();
	}
	
	/**
	 * Construct a new bezier parameter value.
	 * <p/>
	 * Uses the {@link #smooth()} method to infer values for <code>in</code> and <code>out</code>.
	 * <p/>
	 * The bezier value will be locked.
	 * 
	 * @param value The value to store on this {@link ParameterValue}
	 * @param frame The frame at which this value applies
	 * @param owner The {@link Parameter} that 'owns' this parameter value
	 */
	public BasicBezierParameterValue(double value, int frame, Parameter owner)
	{
		super(value, frame, owner);
		this.in.setValue(value);
		this.in.setPercent(DEFAULT_CONTROL_POINT_PERCENTAGE);
		this.out.setValue(value);
		this.out.setPercent(DEFAULT_CONTROL_POINT_PERCENTAGE);
		this.locked = true;
	}
	
	/**
	 * Construct a new bezier parameter value.
	 * <p/>
	 * Allows <code>in</code> and <code>out</code> values to be specified.
	 * <p/>
	 * The resuling bezier value will be unlocked.
	 * 
	 * @param value The value to store on this {@link ParameterValue}
	 * @param frame The frame at which this value applies
	 * @param owner The {@link Parameter} that 'owns' this parameter value
	 * @param inValue The value to use for the <code>in</code> control point
	 * @param inPercent The relative time percentage to use for the <code>in</code> control point
	 * @param outValue The value to use for the <code>out</code> control point
	 * @param outPercent The relative time percentage to use for the <code>out</code> control point
	 * 
	 */
	public BasicBezierParameterValue(double value, int frame, Parameter owner, double inValue, double inPercent, double outValue, double outPercent)
	{
		super(value, frame, owner);
		this.locked = false;
		this.in.setValue(inValue);
		this.in.setPercent(inPercent);
		this.out.setValue(outValue);
		this.out.setPercent(outPercent);
	}
	
	/**
	 * Construct a new bezier parameter value.
	 * <p/>
	 * Allows <code>in</code> value to be specified, with the <code>out</code> value locked to the <code>in</code> value
	 * <p/>
	 * The resuling bezier value will be locked.
	 * 
	 * @param value The value to store on this {@link ParameterValue}
	 * @param frame The frame at which this value applies
	 * @param owner The {@link Parameter} that 'owns' this parameter value
	 * @param inValue The value to use for the <code>in</code> control point
	 * @param inPercent The relative time percentage to use for the <code>in</code> control point
	 * 
	 */
	public BasicBezierParameterValue(double value, int frame, Parameter owner, double inValue, double inPercent)
	{
		super(value, frame, owner);
		this.locked = true;
		this.in.setValue(inValue);
		this.in.setPercent(inPercent);
		this.out.setPercent(inPercent);
		lockOut();
	}
	
	
	@Override
	public ParameterValueType getType()
	{
		return ParameterValueType.BEZIER;
	}
	
	@Override
	public void setInValue(double value)
	{
		boolean changed = !this.in.hasValue() || this.in.getValue() != value;
		if (!changed)
		{
			return;
		}
		
		this.in.setValue(value);
		if (isLocked() && in.hasValue()) 
		{
			lockOut();
		}
		fireChangeEvent(value);
	}

	@Override
	public double getInValue()
	{
		return in.getValue();
	}
	
	@Override
	public void setInPercent(double percent)
	{
		boolean changed = this.in.getPercent() != percent;
		if (!changed)
		{
			return;
		}
		
		this.in.setPercent(percent);
		fireChangeEvent(percent);
	}
	
	@Override
	public double getInPercent()
	{
		return this.in.getPercent();
	}

	@Override
	public void setOutValue(double value)
	{
		boolean changed = !this.out.hasValue() || this.out.getValue() != value;
		if (!changed)
		{
			return;
		}
		
		this.out.setValue(value);
		if (isLocked() && out.hasValue())
		{
			lockIn();
		}
		fireChangeEvent(value);
	}

	@Override
	public double getOutValue()
	{
		return out.getValue();
	}

	@Override
	public void setOutPercent(double percent)
	{
		boolean changed = this.out.getPercent() != percent;
		if (!changed)
		{
			return;
		}
		
		this.out.setPercent(percent);
		fireChangeEvent(percent);
	}
	
	@Override
	public double getOutPercent()
	{
		return this.out.getPercent();
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

	/**
	 * Lock the '<code>in</code>' value to the '<code>out</code>' value.
	 * <p/>
	 * This will adjust <code>in</code> so that <code>in</code>, <code>value</code> and <code>out</code> are colinear
	 */
	private void lockIn()
	{
		if (!out.hasValue()) 
		{
			return;
		}
		
		ParameterValue previousValue = getOwner().getValueAtKeyFrameBeforeFrame(getFrame());
		ParameterValue nextValue = getOwner().getValueAtKeyFrameAfterFrame(getFrame());
		if (previousValue == null || nextValue == null)
		{
			return;
		}
		
		int deltaNextFrame = nextValue.getFrame() - getFrame();
		int deltaPreviousFrame = getFrame() - previousValue.getFrame();
		
		double y = getValue() + (deltaPreviousFrame * getInPercent()) / (deltaNextFrame * getOutPercent()) * (getValue() - out.getValue());
		
		in.setValue(y);
	}
	
	/**
	 * Lock the '<code>out</code>' value to the '<code>in</code>' value.
	 * <p/>
	 * This will adjust <code>out</code> so that <code>in</code>, <code>value</code> and <code>out</code> are colinear
	 */
	private void lockOut()
	{
		if (!in.hasValue()) 
		{
			return;
		}
		
		ParameterValue previousValue = getOwner().getValueAtKeyFrameBeforeFrame(getFrame());
		ParameterValue nextValue = getOwner().getValueAtKeyFrameAfterFrame(getFrame());
		if (previousValue == null || nextValue == null)
		{
			return;
		}
		
		int deltaNextFrame = nextValue.getFrame() - getFrame();
		int deltaPreviousFrame = getFrame() - previousValue.getFrame();
		
		double y = getValue() + (deltaNextFrame * getOutPercent()) / (deltaPreviousFrame * getInPercent()) * (getValue() - in.getValue());
		
		out.setValue(y);
	}
	
	/**
	 * Use a 3-point window to 'smooth' the curve at this point.
	 * <p/>
	 * This examines the 'previous' and 'next' points, and sets the control points for this value
	 * such that a smooth transition is obtained.
	 * <p/>
	 * If there are no 'previous' or 'next' points, this method will reset the control points to their default values.
	 */
	@Override
	public void smooth()
	{
		// Default 'in' and 'out' value to the same as the 'value'. This will result in a horizontal 'in-value-out' control line
		double inValue = getValue();
		double outValue = getValue();
		
		// If previous and next points exist, and they exist on different sides of 'value' vertically,
		// use them to choose a better value for 'in' based on the line joining 'previous' and 'next'
		ParameterValue previousValue = getOwner().getValueAtKeyFrameBeforeFrame(getFrame());
		ParameterValue nextValue = getOwner().getValueAtKeyFrameAfterFrame(getFrame());
		if (previousValue != null && nextValue != null && 
				Math.signum(getValue() - previousValue.getValue()) != Math.signum(getValue() - nextValue.getValue()))
		{
			// Compute the gradient of the line joining the previous and next points
			double m = (nextValue.getValue() - previousValue.getValue()) / (nextValue.getFrame() - previousValue.getFrame());
			double xIn = (getFrame() - previousValue.getFrame()) * getInPercent();
			double xOut = (nextValue.getFrame() - getFrame()) * getOutPercent();
			
			// Compute the value to use for the in control point such that it lies on the line joining the previous and next lines
			inValue = getValue() - m * xIn;
			outValue = getValue() + m * xOut;
		}
		
		// Set the values (bypass the locking as we have calculated what we need here)
		boolean wasLocked = isLocked();
		setLocked(false);
		
		setInValue(inValue);
		setOutValue(outValue);
		
		setLocked(wasLocked);
	}
	
	@Override
	public ParameterValue clone()
	{
		BasicBezierParameterValue result = new BasicBezierParameterValue(getValue(), getFrame(), getOwner(), getInValue(), getInPercent(), getOutValue(), getOutPercent());
		result.setName(getName());
		result.setLocked(isLocked());
		return result;
	}
	
	@Override
	public void translate(double delta)
	{
		if (delta == 0.0)
		{
			return;
		}
		
		setValue(getValue() + delta, true);
		if (in.hasValue())
		{
			in.setValue(in.value + delta);
		}
		if (out.hasValue())
		{
			out.setValue(out.value + delta);
		}
		
		fireChangeEvent(getValue());
	}
	
	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();
		
		Element result = WWXML.appendElement(parent, constants.getParameterValueElementName());
		
		WWXML.setTextAttribute(result, constants.getParameterValueAttributeType(), getType().name());
		WWXML.setIntegerAttribute(result, constants.getParameterValueAttributeFrame(), getFrame());
		WWXML.setDoubleAttribute(result, constants.getParameterValueAttributeValue(), getValue());
		WWXML.setDoubleAttribute(result, constants.getBezierValueAttributeInValue(), getInValue());
		WWXML.setDoubleAttribute(result, constants.getBezierValueAttributeInPercent(), getInPercent());
		WWXML.setDoubleAttribute(result, constants.getBezierValueAttributeOutValue(), getOutValue());
		WWXML.setDoubleAttribute(result, constants.getBezierValueAttributeOutPercent(), getOutPercent());
		WWXML.setBooleanAttribute(result, constants.getBezierValueAttributeLocked(), isLocked());
		
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
				BasicBezierParameterValue result = new BasicBezierParameterValue();
				result.setValue(WWXML.getDouble(element, ATTRIBUTE_PATH_PREFIX + constants.getParameterValueAttributeValue(), null));
				result.setFrame(WWXML.getInteger(element, ATTRIBUTE_PATH_PREFIX + constants.getParameterValueAttributeFrame(), null));
				result.setInValue(WWXML.getDouble(element, ATTRIBUTE_PATH_PREFIX + constants.getBezierValueAttributeInValue(), null));
				result.setInPercent(WWXML.getDouble(element, ATTRIBUTE_PATH_PREFIX + constants.getBezierValueAttributeInPercent(), null));
				result.setOutValue(WWXML.getDouble(element, ATTRIBUTE_PATH_PREFIX + constants.getBezierValueAttributeOutValue(), null));
				result.setOutPercent(WWXML.getDouble(element, ATTRIBUTE_PATH_PREFIX + constants.getBezierValueAttributeOutPercent(), null));
				result.setLocked(WWXML.getBoolean(element, ATTRIBUTE_PATH_PREFIX + constants.getBezierValueAttributeLocked(), null));
				
				result.setOwner((Parameter)context.getValue(constants.getParameterValueOwnerKey()));
				
				Validate.notNull(result.getOwner(), "No owner found in the context. Expected type Parameter under key " + constants.getParameterValueOwnerKey());

				return result;
			}
			default:
			{
				return null;
			}
		}
	}
	
	/**
	 * A simple container class that holds a value and time percent
	 */
	private static class BezierContolPoint
	{
		private Double value;
		private double percent = DEFAULT_CONTROL_POINT_PERCENTAGE;
		
		private boolean hasValue()
		{
			return value != null;
		}
		
		public void setValue(Double value)
		{
			this.value = value;
		}
		
		public Double getValue()
		{
			return value;
		}
		
		public void setPercent(double percent)
		{
			this.percent = percent;
		}
		
		public double getPercent()
		{
			return percent;
		}
	}

}
