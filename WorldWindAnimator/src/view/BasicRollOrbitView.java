package view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicOrbitView;
import gov.nasa.worldwind.view.OrbitViewModel;

public class BasicRollOrbitView extends BasicOrbitView implements RollOrbitView
{
	protected RollOrbitViewModel rollOrbitViewModel;

	public BasicRollOrbitView()
	{
		this(new BasicRollOrbitViewModel());
	}

	public BasicRollOrbitView(RollOrbitViewModel orbitViewModel)
	{
		super(orbitViewModel);
		this.rollOrbitViewModel = orbitViewModel;
		setOrbitViewLimits(new BasicRollOrbitViewLimits());
	}

	public RollOrbitViewLimits getRollOrbitViewLimits()
	{
		return (RollOrbitViewLimits) super.getOrbitViewLimits();
	}

	public void setRollOrbitViewLimits(RollOrbitViewLimits viewLimits)
	{
		super.setOrbitViewLimits(viewLimits);
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

	public void setCenter(LatLon center, Angle heading, Angle pitch,
			Angle roll, double zoom)
	{
		setCenterPosition(new Position(center, 0));
		setZoom(zoom);
		setHeading(heading);
		setPitch(pitch);
		setRoll(roll);
	}

	public void setEye(LatLon eye, Angle heading, Angle pitch, Angle roll,
			double zoom)
	{
		if (eye == null)
		{
			String message = Logging.getMessage("nullValue.PositionIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (heading == null || pitch == null || roll == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (this.globe == null)
		{
			String message = Logging
					.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Position eyePosition = new Position(eye, zoom);
		Vec4 newEyePoint = this.globe.computePointFromPosition(eyePosition);

		if (newEyePoint == null)
		{
			String message = Logging.getMessage("View.ErrorSettingOrientation",
					eyePosition);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Matrix transform = Matrix.IDENTITY;
		transform = transform.multiply(Matrix.fromRotationZ(roll));
		transform = transform
				.multiply(Matrix.fromRotationX(pitch.multiply(-1)));
		transform = transform.multiply(Matrix.fromRotationZ(heading));

		Vec4 direction = newEyePoint.getNegative3().normalize3();
		Vec4 left = direction.cross3(Vec4.UNIT_Y);

		Quaternion qh = Quaternion.fromAxisAngle(heading, direction);
		left = left.transformBy3(qh);

		Quaternion qp = Quaternion.fromAxisAngle(pitch, left);
		direction = direction.transformBy3(qp);

		Quaternion qr = Quaternion.fromAxisAngle(roll, direction);
		left = left.transformBy3(qr);

		Vec4 up = left.cross3(direction);
		Vec4 newCenterPoint = newEyePoint.add3(direction);

		OrbitViewModel.ModelCoordinates modelCoords = getOrbitViewModel()
				.computeModelCoordinates(this.globe, newEyePoint,
						newCenterPoint, up);

		if (modelCoords == null)
		{
			String message = Logging.getMessage("View.ErrorSettingOrientation",
					eyePosition);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		setCenter(modelCoords.getCenterPosition().getLatLon(), heading, pitch,
				roll, zoom);
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
