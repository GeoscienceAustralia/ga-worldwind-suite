/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;


/**
 * A basic implementation of the {@link BezierParameterValue} interface.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicBezierParameterValue extends BasicParameterValue implements BezierParameterValue
{
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
	 * @param value
	 * @param owner
	 * 
	 * TODO: Make this more applicable to beziers
	 */
	public BasicBezierParameterValue(double value, Parameter owner)
	{
		super(value, owner);
	}
	
	@Override
	public ParameterValueType getType()
	{
		return ParameterValueType.BEZIER;
	}
	
	@Override
	public void setInValue(double value)
	{
		this.in.setValue(value);
		if (isLocked() && in.hasValue()) 
		{
			lockOut();
		}
	}

	@Override
	public double getInValue()
	{
		return in.getValue();
	}
	
	@Override
	public void setInPercent(double percent)
	{
		this.in.setPercent(percent);
	}
	
	@Override
	public double getInPercent()
	{
		return this.in.getPercent();
	}

	@Override
	public void setOutValue(double value)
	{
		this.out.setValue(value);
		if (isLocked() && out.hasValue())
		{
			lockIn();
		}
	}

	@Override
	public double getOutValue()
	{
		return out.getValue();
	}

	@Override
	public void setOutPercent(double percent)
	{
		this.out.setPercent(percent);
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
	 * This will:
	 * <ul>
	 * 	<li>Adjust <code>in</code> so that <code>in</code>, <code>value</code> and <code>out</code> are colinear
	 *  <li>Adjust <code>in</code> so that <code>out</code> and <code>in</code> are equidistant from <code>value</code>
	 * </ul>
	 */
	private void lockIn()
	{
		if (!out.hasValue()) 
		{
			return;
		}
		
		double outValueDelta = out.getValue() - getValue();
		in.setValue(getValue() - outValueDelta);
	}
	
	/**
	 * Lock the '<code>out</code>' value to the '<code>in</code>' value.
	 * <p/>
	 * This will:
	 * <ul>
	 * 	<li>Adjust <code>out</code> so that <code>in</code>, <code>value</code> and <code>out</code> are colinear
	 *  <li>Adjust <code>out</code> so that <code>out</code> and <code>in</code> are equidistant from <code>value</code>
	 * </ul>
	 */
	private void lockOut()
	{
		if (!in.hasValue()) 
		{
			return;
		}
		double inValueDelta = in.getValue() - getValue();
		out.setValue(getValue() - inValueDelta);
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
