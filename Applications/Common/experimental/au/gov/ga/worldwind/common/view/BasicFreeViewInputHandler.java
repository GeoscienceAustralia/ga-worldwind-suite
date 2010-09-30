package au.gov.ga.worldwind.common.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

public class BasicFreeViewInputHandler extends AbstractInputFreeViewInputHandler
{
	private double headingSpeed = 0.15;
	private double pitchSpeed = 0.1;
	private double rollSpeed = 0.1;

	@Override
	public void look(double deltaHeading, double deltaPitch, double deltaRoll)
	{
		View view = getView();
		if (view != null)
		{
			((FreeView) view).rotate(-deltaPitch * pitchSpeed, deltaHeading * headingSpeed,
					deltaRoll * rollSpeed);

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
		double[] range = new double[] { 1, 100000 }; //TODO

		View view = getView();
		Position eyePos = view.getEyePosition();
		Globe globe = getWorldWindow().getModel().getGlobe();
		double radius = globe.getRadius();
		double surfaceElevation = globe.getElevation(eyePos.getLatitude(), eyePos.getLongitude());
		double t =
				getScaleValue(range[0], range[1], eyePos.getElevation() - surfaceElevation,
						3.0 * radius, true);
		//t *= deviceAttributes.getSensitivity(); //TODO

		return t;
	}

	protected double getScaleValue(double minValue, double maxValue, double value, double range,
			boolean isExp)
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
