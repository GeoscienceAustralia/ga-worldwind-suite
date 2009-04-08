package view.roll;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicOrbitView;

/**
 * @author Michael de Hoog
 */
public class BasicRollOrbitView extends BasicOrbitView implements RollOrbitView
{
	protected RollOrbitViewModel rollOrbitViewModel;

	public BasicRollOrbitView()
	{
		this(new BasicRollOrbitViewModel());
	}

	public BasicRollOrbitView(RollOrbitViewModel rollOrbitViewModel)
	{
		super(rollOrbitViewModel);
		this.rollOrbitViewModel = rollOrbitViewModel;
		setOrbitViewLimits(new BasicRollOrbitViewLimits());
	}

	public RollOrbitViewLimits getRollOrbitViewLimits()
	{
		return (RollOrbitViewLimits) getOrbitViewLimits();
	}

	public void setRollOrbitViewLimits(RollOrbitViewLimits viewLimits)
	{
		setOrbitViewLimits(viewLimits);
	}

	public Angle getRoll()
	{
		return rollOrbitViewModel.getRoll();
	}

	public void setRoll(Angle roll)
	{
		if (roll == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		roll = normalizedRoll(roll);
		roll = BasicRollOrbitViewLimits.limitRoll(roll, this
				.getRollOrbitViewLimits());
		rollOrbitViewModel.setRoll(roll);
	}

	public static Angle normalizedRoll(Angle unnormalizedRoll)
	{
		if (unnormalizedRoll == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Normalize roll to the range [-180, 180].
		double degrees = unnormalizedRoll.degrees;
		double roll = degrees % 360;
		return Angle.fromDegrees(roll > 180 ? roll - 360
				: (roll < -180 ? 360 + roll : roll));
	}
}
