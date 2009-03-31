package nasa.worldwind.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

public class BasicRollOrbitViewModel implements RollOrbitViewModel
{
	public static class BasicModelCoordinates implements
			RollOrbitViewModel.RollModelCoordinates
	{
		private final Position center;
		private final Angle heading;
		private final Angle pitch;
		private final Angle roll;
		private final double zoom;

		public BasicModelCoordinates(Position center, Angle heading,
				Angle pitch, Angle roll, double zoom)
		{
			if (center == null)
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

			this.center = center;
			this.heading = heading;
			this.pitch = pitch;
			this.roll = roll;
			this.zoom = zoom;
		}

		public Position getCenterPosition()
		{
			return this.center;
		}

		public Angle getHeading()
		{
			return this.heading;
		}

		public Angle getPitch()
		{
			return this.pitch;
		}

		public Angle getRoll()
		{
			return roll;
		}

		public double getZoom()
		{
			return this.zoom;
		}

		@Override
		public String toString()
		{
			return center + " " + heading + " " + pitch + " " + roll + " "
					+ zoom;
		}
	}

	public BasicRollOrbitViewModel()
	{
	}

	public Matrix computeTransformMatrix(Globe globe, Position center,
			Angle heading, Angle pitch, Angle roll, double zoom)
	{
		if (globe == null)
		{
			String message = Logging.getMessage("nullValue.GlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (center == null)
		{
			String message = "nullValue.CenterIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (heading == null)
		{
			String message = "nullValue.HeadingIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (pitch == null)
		{
			String message = "nullValue.PitchIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (roll == null)
		{
			String message = "nullValue.RollIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Construct the model-view transform matrix for the specified coordinates.
		// Because this is a model-view transform, matrices are applied in reverse order.
		Matrix transform;
		// Zoom, heading, pitch.
		transform = this.computeHeadingPitchRollZoomTransform(heading, pitch,
				roll, zoom);
		// Center position.
		transform = transform.multiply(this.computeCenterTransform(globe,
				center));

		return transform;
	}

	public RollModelCoordinates computeModelCoordinates(Globe globe,
			Vec4 eyePoint, Vec4 centerPoint, Vec4 up)
	{
		if (globe == null)
		{
			String message = Logging.getMessage("nullValue.GlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (eyePoint == null)
		{
			String message = "nullValue.EyePointIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (centerPoint == null)
		{
			String message = "nullValue.CenterPointIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (up == null)
		{
			String message = "nullValue.UpIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Matrix modelview = Matrix.fromViewLookAt(eyePoint, centerPoint, up);
		return this.computeModelCoordinates(globe, modelview, centerPoint);
	}

	public RollModelCoordinates computeModelCoordinates(Globe globe,
			Matrix modelTransform, Vec4 centerPoint)
	{
		if (globe == null)
		{
			String message = Logging.getMessage("nullValue.GlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (modelTransform == null)
		{
			String message = "nullValue.ModelTransformIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (centerPoint == null)
		{
			String message = "nullValue.CenterPointIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Compute the center position.
		Position centerPos = globe.computePositionFromPoint(centerPoint);
		// Compute the center position transform.
		Matrix centerTransform = this.computeCenterTransform(globe, centerPos);
		Matrix centerTransformInv = centerTransform.getInverse();
		if (centerTransformInv == null)
		{
			String message = Logging.getMessage("generic.NoninvertibleMatrix");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		// Compute the heading-pitch-zoom transform.
		Matrix hprzTransform = modelTransform.multiply(centerTransformInv);
		// Extract the heading, pitch, and zoom values from the transform.
		Angle heading = this.computeHeading(hprzTransform);
		Angle pitch = this.computePitch(hprzTransform);
		Angle roll = this.computeRoll(hprzTransform);
		double zoom = this.computeZoom(hprzTransform);
		if (heading == null || pitch == null || roll == null)
			return null;

		return new BasicModelCoordinates(centerPos, heading, pitch, roll, zoom);
	}

	protected Matrix computeCenterTransform(Globe globe, Position center)
	{
		if (globe == null)
		{
			String message = Logging.getMessage("nullValue.GlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (center == null)
		{
			String message = "nullValue.CenterIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// The view eye position will be the same as the center position.
		// This is only the case without any zoom, heading, and pitch.
		Vec4 eyePoint = globe.computePointFromPosition(center);
		// The view forward direction will be colinear with the
		// geoid surface normal at the center position.
		Vec4 normal = globe.computeSurfaceNormalAtLocation(
				center.getLatitude(), center.getLongitude());
		Vec4 lookAtPoint = eyePoint.subtract3(normal);
		// The up direction will be pointing towards the north pole.
		Vec4 north = globe.computeNorthPointingTangentAtLocation(center
				.getLatitude(), center.getLongitude());
		// Creates a viewing matrix looking from eyePoint towards lookAtPoint,
		// with the given up direction. The forward, right, and up vectors
		// contained in the matrix are guaranteed to be orthogonal. This means
		// that the Matrix's up may not be equivalent to the specified up vector
		// here (though it will point in the same general direction).
		// In this case, the forward direction would not be affected.
		return Matrix.fromViewLookAt(eyePoint, lookAtPoint, north);
	}

	protected Matrix computeHeadingPitchRollZoomTransform(Angle heading,
			Angle pitch, Angle roll, double zoom)
	{
		if (heading == null)
		{
			String message = "nullValue.HeadingIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (pitch == null)
		{
			String message = "nullValue.PitchIsNull";
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

	protected Angle computeHeading(Matrix headingPitchRollZoomTransform)
	{
		if (headingPitchRollZoomTransform == null)
		{
			String message = "nullValue.HeadingPitchZoomTransformTransformIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return headingPitchRollZoomTransform.getRotationZ();
	}

	protected Angle computePitch(Matrix headingPitchRollZoomTransform)
	{
		if (headingPitchRollZoomTransform == null)
		{
			String message = "nullValue.HeadingPitchZoomTransformTransformIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Angle a = headingPitchRollZoomTransform.getRotationX();
		if (a != null)
		{
			a = a.multiply(-1.0);
			if (a.degrees < 0 && a.degrees > -1E-5)
				a = Angle.ZERO;
		}
		return a;
	}

	protected Angle computeRoll(Matrix headingPitchRollZoomTransform)
	{
		if (headingPitchRollZoomTransform == null)
		{
			String message = "nullValue.HeadingPitchZoomTransformTransformIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return headingPitchRollZoomTransform.getRotationY();
	}

	protected double computeZoom(Matrix headingPitchRollZoomTransform)
	{
		if (headingPitchRollZoomTransform == null)
		{
			String message = "nullValue.HeadingPitchZoomTransformTransformIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Vec4 v = headingPitchRollZoomTransform.getTranslation();
		return v != null ? v.getLength3() : 0.0;
	}

	public Matrix computeTransformMatrix(Globe globe, Position center,
			Angle heading, Angle pitch, double zoom)
	{
		return computeTransformMatrix(globe, center, heading, pitch,
				Angle.ZERO, zoom);
	}
}
