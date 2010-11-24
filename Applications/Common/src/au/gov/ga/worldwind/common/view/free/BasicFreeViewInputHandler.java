package au.gov.ga.worldwind.common.view.free;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.BasicView;
import gov.nasa.worldwind.view.ViewUtil;

public class BasicFreeViewInputHandler extends AbstractInputFreeViewInputHandler
{
	private double headingSpeed = 0.15;
	private double pitchSpeed = 0.1;
	private double rollSpeed = 0.1;

	@Override
	public void look(double deltaHeading, double deltaPitch, double deltaRoll)
	{
		BasicView view = (BasicView) getView();
		if (view != null)
		{
			double deltaX = deltaPitch * pitchSpeed;
			double deltaY = deltaHeading * headingSpeed;
			double deltaZ = deltaRoll * rollSpeed;

			Matrix transform = FreeView.computeRotationTransform(view.getHeading(), view.getPitch(), view.getRoll());
			transform = Matrix.fromAxisAngle(Angle.fromDegrees(deltaX), 1, 0, 0).multiply(transform);
			transform = Matrix.fromAxisAngle(Angle.fromDegrees(deltaY), 0, 1, 0).multiply(transform);
			transform = Matrix.fromAxisAngle(Angle.fromDegrees(deltaZ), 0, 0, 1).multiply(transform);

			view.setHeading(ViewUtil.computeHeading(transform));
			view.setPitch(ViewUtil.computePitch(transform));
			view.setRoll(ViewUtil.computeRoll(transform));

			view.firePropertyChange(AVKey.VIEW, null, view);
		}
	}

	@Override
	public void move(double deltaX, double deltaY, double deltaZ)
	{
		View view = getView();
		if (view != null)
		{
			Vec4 forward = view.getForwardVector();
			Vec4 up = view.getUpVector();
			//Vec4 side = forward.transformBy3(Matrix.fromAxisAngle(Angle.fromDegrees(90), up));
			Vec4 side = up.cross3(forward);

			double scale = getScaleValueElevation();
			side = side.multiply3(deltaX * scale);
			up = up.multiply3(deltaY * scale);
			forward = forward.multiply3(deltaZ * scale);

			Vec4 eyePoint = view.getCurrentEyePoint();
			eyePoint = eyePoint.add3(forward.add3(up.add3(side)));
			Position newPosition = view.getGlobe().computePositionFromPoint(eyePoint);

			view.setEyePosition(newPosition);
			view.firePropertyChange(AVKey.VIEW, null, view);
		}
	}

	protected double getScaleValueElevation()
	{
		double[] range = new double[] { 10, 100000 };

		View view = getView();
		Position eyePos = view.getEyePosition();
		Globe globe = getWorldWindow().getModel().getGlobe();
		double radius = globe.getRadius();
		double surfaceElevation = globe.getElevation(eyePos.getLatitude(), eyePos.getLongitude());
		double t = getScaleValue(range[0], range[1], eyePos.getElevation() - surfaceElevation, 3.0 * radius, true);
		//t *= deviceAttributes.getSensitivity(); //TODO

		return t;
	}

	protected double getScaleValue(double minValue, double maxValue, double value, double range, boolean isExp)
	{
		double t = value / range;
		t = t < 0 ? 0 : (t > 1 ? 1 : t);
		if (isExp)
		{
			t = Math.pow(2.0, t) - 1.0;
		}
		return (minValue * (1.0 - t) + maxValue * t);
	}
}
