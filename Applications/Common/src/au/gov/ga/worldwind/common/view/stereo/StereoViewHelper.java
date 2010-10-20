package au.gov.ga.worldwind.common.view.stereo;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;

import java.awt.Rectangle;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.view.stereo.StereoView.Eye;

public class StereoViewHelper
{
	private Eye eye = Eye.LEFT;
	private boolean stereo = false;

	private StereoViewParameters parameters = new BasicStereoViewParameters();

	private double lastFocalLength = 1;
	private double lastEyeSeparation = 0;

	public void setup(boolean stereo, Eye eye)
	{
		//don't allow null eye
		if (eye == null)
			eye = Eye.LEFT;

		this.stereo = stereo;
		this.eye = eye;
	}

	public Eye getEye()
	{
		return eye;
	}

	public boolean isStereo()
	{
		return stereo;
	}

	public StereoViewParameters getParameters()
	{
		return parameters;
	}

	public void setParameters(StereoViewParameters parameters)
	{
		this.parameters = parameters;
	}

	public Matrix calculateProjectionMatrix(View view, double nearDistance, double farDistance)
	{
		if (stereo)
		{
			return calculateStereoProjectionMatrix(view, nearDistance, farDistance, eye,
					lastFocalLength, lastEyeSeparation);
		}
		else
		{
			return calculateMonoProjectionMatrix(view, nearDistance, farDistance);
		}
	}

	protected void calculateStereoParameters(View view)
	{
		if (parameters.isDynamicStereo())
		{
			AsymmetricFrutumParameters afp =
					calculateStereoParameters(view, parameters.getEyeSeparationMultiplier());
			this.lastFocalLength = afp.focalLength;
			this.lastEyeSeparation = afp.eyeSeparation;
		}
		else
		{
			this.lastFocalLength = parameters.getFocalLength();
			this.lastEyeSeparation = parameters.getEyeSeparation();
		}
	}

	public void beforeComputeMatrices(View view)
	{
		calculateStereoParameters(view);
	}

	public Matrix transformModelView(Matrix matrix)
	{
		if (matrix != null && stereo)
		{
			matrix =
					Matrix.fromTranslation((eye == Eye.RIGHT ? 1 : -1) * lastEyeSeparation * 0.5,
							0, 0).multiply(matrix);
		}
		return matrix;
	}

	public Matrix computeProjection(View view)
	{
		return calculateProjectionMatrix(view, view.getNearClipDistance(),
				view.getFarClipDistance());
	}

	public static Matrix calculateStereoProjectionMatrix(View view, double nearDistance,
			double farDistance, Eye eye, double focalLength, double eyeSeparation)
	{
		Rectangle viewport = view.getViewport();
		double viewportWidth = viewport.getWidth() <= 0d ? 1d : viewport.getWidth();
		double viewportHeight = viewport.getHeight() <= 0d ? 1d : viewport.getHeight();
		double aspectratio = viewportWidth / viewportHeight;
		double vfov = view.getFieldOfView().radians / aspectratio;
		double widthdiv2 = nearDistance * Math.tan(vfov / 2.0);
		double multiplier = eye == Eye.RIGHT ? 1d : -1d;
		double distance = multiplier * 0.5 * eyeSeparation * nearDistance / focalLength;

		double top = widthdiv2;
		double bottom = -widthdiv2;
		double left = -aspectratio * widthdiv2 + distance;
		double right = aspectratio * widthdiv2 + distance;

		return fromFrustum(left, right, bottom, top, nearDistance, farDistance);
	}

	public static Matrix calculateMonoProjectionMatrix(View view, double nearDistance,
			double farDistance)
	{
		Rectangle viewport = view.getViewport();
		double viewportWidth = viewport.getWidth() <= 0d ? 1d : viewport.getWidth();
		double viewportHeight = viewport.getHeight() <= 0d ? 1d : viewport.getHeight();
		return Matrix.fromPerspective(view.getFieldOfView(), viewportWidth, viewportHeight,
				nearDistance, farDistance);
	}

	public static Matrix fromFrustum(double left, double right, double bottom, double top,
			double near, double far)
	{
		double A = (right + left) / (right - left);
		double B = (top + bottom) / (top - bottom);
		double C = -(far + near) / (far - near);
		double D = -(2 * far * near) / (far - near);
		double E = (2 * near) / (right - left);
		double F = (2 * near) / (top - bottom);
		return new Matrix(E, 0, A, 0, 0, F, B, 0, 0, 0, C, D, 0, 0, -1, 0);
	}

	public static AsymmetricFrutumParameters calculateStereoParameters(View view,
			double separationExaggeration)
	{
		Vec4 eyePoint = view.getCurrentEyePoint();
		double distanceFromOrigin = eyePoint.getLength3();

		//calculate center point
		double radius = view.getGlobe().getEquatorialRadius();
		double amount = Util.percentDouble(distanceFromOrigin, radius, radius * 5d);

		Vec4 centerPoint = view.getCenterPoint();
		double distanceToCenter = distanceFromOrigin; //default to a valid value

		if (centerPoint != null)
		{
			distanceToCenter = eyePoint.distanceTo3(centerPoint);
		}
		else if (distanceFromOrigin > radius)
		{
			//pythagoras, distanceFromOrigin is hypotenuse
			distanceToCenter = Math.sqrt(distanceFromOrigin * distanceFromOrigin - radius * radius);
		}

		//calculate distance from eye to center
		double focalLength = Util.mixDouble(amount, distanceToCenter, distanceFromOrigin);

		//exaggerate separation more when zoomed out
		separationExaggeration =
				Util.mixDouble(amount, separationExaggeration, separationExaggeration * 4);

		//move focal length closer as view is pitched
		amount = Util.percentDouble(view.getPitch().degrees, 0d, 90d);
		focalLength = Util.mixDouble(amount, focalLength, focalLength / 3d);

		double eyeSeparation = separationExaggeration * focalLength / 100d;

		return new AsymmetricFrutumParameters(focalLength, eyeSeparation);
	}

	public static class AsymmetricFrutumParameters
	{
		public final double focalLength;
		public final double eyeSeparation;

		public AsymmetricFrutumParameters(double focalLength, double eyeSeparation)
		{
			this.focalLength = focalLength;
			this.eyeSeparation = eyeSeparation;
		}
	}
}
