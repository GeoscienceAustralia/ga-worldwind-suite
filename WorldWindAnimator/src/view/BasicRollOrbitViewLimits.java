package view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicOrbitViewLimits;

public class BasicRollOrbitViewLimits extends BasicOrbitViewLimits implements
		RollOrbitViewLimits
{
	protected Angle minRoll;
	protected Angle maxRoll;

	public BasicRollOrbitViewLimits()
	{
		super();
		minRoll = Angle.NEG180;
		maxRoll = Angle.POS180;
		maxPitch = Angle.POS180;
	}

	public Angle[] getRollLimits()
	{
		return new Angle[] { this.minRoll, this.maxRoll };
	}

	public void setRollLimits(Angle minAngle, Angle maxAngle)
	{
		if (minAngle == null || maxAngle == null)
		{
			String message = Logging
					.getMessage("nullValue.MinOrMaxAngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.minRoll = minAngle;
		this.maxRoll = maxAngle;
	}

	public static Angle limitRoll(Angle angle, RollOrbitViewLimits viewLimits)
	{
		if (angle == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (viewLimits == null)
		{
			String message = Logging.getMessage("nullValue.ViewLimitsIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Angle[] limits = viewLimits.getRollLimits();
		Angle newAngle = angle;

		if (angle.compareTo(limits[0]) < 0)
		{
			newAngle = limits[0];
		}
		else if (angle.compareTo(limits[1]) > 0)
		{
			newAngle = limits[1];
		}

		return newAngle;
	}
}
