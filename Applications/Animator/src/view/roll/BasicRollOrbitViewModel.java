package view.roll;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicOrbitViewModel;

/**
 * @author Michael de Hoog
 */
public class BasicRollOrbitViewModel extends BasicOrbitViewModel implements
		RollOrbitViewModel
{
	private Angle roll = Angle.ZERO;

	public Angle getRoll()
	{
		return roll;
	}

	public void setRoll(Angle roll)
	{
		this.roll = roll;
	}

	@Override
	protected Matrix computeHeadingPitchZoomTransform(Angle heading,
			Angle pitch, double zoom)
	{
		if (heading == null)
		{
			String message = Logging.getMessage("nullValue.HeadingIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (pitch == null)
		{
			String message = Logging.getMessage("nullValue.PitchIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Matrix transform;
		// Zoom.
		transform = Matrix.fromTranslation(0, 0, -zoom);
		// Roll
		transform = transform.multiply(Matrix.fromRotationZ(roll));
		// Pitch is treated clockwise as rotation about the X-axis. We flip the pitch value so that a positive
		// rotation produces a clockwise rotation (when facing the axis).
		transform = transform.multiply(Matrix.fromRotationX(pitch
				.multiply(-1.0)));
		// Heading.
		transform = transform.multiply(Matrix.fromRotationZ(heading));
		return transform;
	}
}
