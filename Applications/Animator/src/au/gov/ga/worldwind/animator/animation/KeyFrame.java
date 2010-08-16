package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.util.Logging;

import java.io.Serializable;
import java.util.logging.Level;

import nasa.worldwind.util.RestorableSupport;

/**
 * Represents a key frame in an animation.
 * <p/>
 * A Key frame contains all ParameterValues that have been recorded at that
 * frame, along with the index of the animation frame it corresponds to.
 */
class KeyFrame implements Comparable<KeyFrame>, Serializable, Restorable
{
	private int frame;
	private double value;
	private double inValue;
	private double inPercent;
	private double outValue;
	private double outPercent;
	private boolean lockInOut;

	private double[] values;
	private double maxValue = Double.NEGATIVE_INFINITY;
	private double minValue = Double.POSITIVE_INFINITY;

	/**
	 * Factory method.
	 * <p/>
	 * Creates a new {@link KeyFrame} instance from the provided XML string.

	 * @param stateInXml The XML representation of the {@link KeyFrame} to create
	 * 
	 * @return The created {@link KeyFrame}, or <code>null</code> if an problem 
	 * occurred during creation
	 */
	public static KeyFrame fromStateXml(String stateInXml)
	{
		try
		{
			KeyFrame keyFrame = new KeyFrame();
			keyFrame.restoreState(stateInXml);
			return keyFrame;
		}
		catch (Exception e)
		{
			Logging.logger().log(Level.WARNING, "Exception occured while creating key frame from XML", e);
			return null;
		}
	}
	
	/**
	 * Create a new key frame at the given animation frame, with the given value
	 * 
	 * @param frame the animation frame this key frame is associated with
	 * @param value the value associated with this key frame
	 */
	public KeyFrame(int frame, double value)
	{
		this.frame = frame;
		this.value = value;
		this.inValue = value;
		this.outValue = value;
		this.inPercent = OldParameter.DEFAULT_INOUT_PERCENT;
		this.outPercent = OldParameter.DEFAULT_INOUT_PERCENT;
	}

	/**
	 * Empty constructor for use when de-serialising
	 */
	KeyFrame(){};
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof KeyFrame))
		{
			return false;
		}
		return ((KeyFrame) obj).frame == this.frame;
	}

	@Override
	public int compareTo(KeyFrame o)
	{
		return this.frame - o.frame;
	}

	@Override
	public String getRestorableState()
	{
		RestorableSupport restorableSupport = RestorableSupport.newRestorableSupport();
		if (restorableSupport == null)
		{
			return null;
		}

		restorableSupport.addStateValueAsInteger("frame", frame);
		restorableSupport.addStateValueAsDouble("value", value);
		restorableSupport.addStateValueAsDouble("inValue", inValue);
		restorableSupport.addStateValueAsDouble("inPercent", inPercent);
		restorableSupport.addStateValueAsDouble("outValue", outValue);
		restorableSupport.addStateValueAsDouble("outPercent", outPercent);
		restorableSupport.addStateValueAsBoolean("lockInOut", lockInOut);

		return restorableSupport.getStateAsXml();
	}

	@Override
	public void restoreState(String stateInXml)
	{
		if (stateInXml == null) 
		{
			throw new IllegalArgumentException();
		}

		RestorableSupport restorableSupport;
		try
		{
			restorableSupport = RestorableSupport.parse(stateInXml);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Parsing failed", e);
		}

		frame = restorableSupport.getStateValueAsInteger("frame");
		value = restorableSupport.getStateValueAsDouble("value");
		inValue = restorableSupport.getStateValueAsDouble("inValue");
		inPercent = restorableSupport.getStateValueAsDouble("inPercent");
		outValue = restorableSupport.getStateValueAsDouble("outValue");
		outPercent = restorableSupport.getStateValueAsDouble("outPercent");
		lockInOut = restorableSupport.getStateValueAsBoolean("lockInOut");
	}

	/**
	 * @return the animation frame this key frame is associated with
	 */
	public int getFrame()
	{
		return frame;
	}

	/**
	 * @param frame the frame to set
	 */
	public void setFrame(int frame)
	{
		this.frame = frame;
	}

	/**
	 * @return the value of this key frame
	 */
	public double getValue()
	{
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value)
	{
		this.value = value;
	}

	/**
	 * @return the inValue
	 */
	public double getInValue()
	{
		return inValue;
	}

	/**
	 * @param inValue the inValue to set
	 */
	public void setInValue(double inValue)
	{
		this.inValue = inValue;
	}

	/**
	 * @return the inPercent
	 */
	public double getInPercent()
	{
		return inPercent;
	}

	/**
	 * @param inPercent the inPercent to set
	 */
	public void setInPercent(double inPercent)
	{
		this.inPercent = inPercent;
	}

	/**
	 * @return the outValue
	 */
	public double getOutValue()
	{
		return outValue;
	}

	/**
	 * @param outValue the outValue to set
	 */
	public void setOutValue(double outValue)
	{
		this.outValue = outValue;
	}

	/**
	 * @return the outPercent
	 */
	public double getOutPercent()
	{
		return outPercent;
	}

	/**
	 * @param outPercent the outPercent to set
	 */
	public void setOutPercent(double outPercent)
	{
		this.outPercent = outPercent;
	}

	/**
	 * @return the lockInOut
	 */
	public boolean isLockInOut()
	{
		return lockInOut;
	}

	/**
	 * @param lockInOut the lockInOut to set
	 */
	public void setLockInOut(boolean lockInOut)
	{
		this.lockInOut = lockInOut;
	}

	/**
	 * @return the values
	 */
	public double[] getValues()
	{
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(double[] values)
	{
		this.values = values;
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue()
	{
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(double maxValue)
	{
		this.maxValue = maxValue;
	}

	/**
	 * @return the minValue
	 */
	public double getMinValue()
	{
		return minValue;
	}

	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(double minValue)
	{
		this.minValue = minValue;
	}
	
	
	
}