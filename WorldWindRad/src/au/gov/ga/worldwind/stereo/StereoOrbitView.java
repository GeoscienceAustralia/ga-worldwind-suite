package au.gov.ga.worldwind.stereo;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.BasicOrbitView;
import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.settings.Settings.ProjectionMode;

public class StereoOrbitView extends BasicOrbitView
{
	public enum Eye
	{
		LEFT,
		RIGHT
	}

	private Eye eye = Eye.LEFT;
	private boolean drawingStereo;

	public StereoOrbitView()
	{
		this(new StereoOrbitViewModel());
	}

	public StereoOrbitView(StereoOrbitViewModel orbitViewModel)
	{
		super(orbitViewModel);
		orbitViewModel.setView(this);
	}

	public Eye getEye()
	{
		return eye;
	}

	public void setEye(Eye eye)
	{
		this.eye = eye;
	}

	public void setDrawingStereo(boolean drawing)
	{
		this.drawingStereo = drawing;
	}

	public boolean isDrawingStereo()
	{
		return drawingStereo;
	}

	private static Matrix fromFrustum(double left, double right, double bottom,
			double top, double near, double far)
	{
		double A = (right + left) / (right - left);
		double B = (top + bottom) / (top - bottom);
		double C = -(far + near) / (far - near);
		double D = -(2 * far * near) / (far - near);
		double E = (2 * near) / (right - left);
		double F = (2 * near) / (top - bottom);
		return new Matrix(E, 0, A, 0, 0, F, B, 0, 0, 0, C, D, 0, 0, -1, 0);
	}

	@Override
	protected void doApply(DrawContext dc)
	{
		super.doApply(dc);

		if (isDrawingStereo())
		{
			if (Settings.get().getProjectionMode() == ProjectionMode.ASYMMETRIC_FRUSTUM)
			{
				double fov = getFieldOfView().radians;
				double nearDistance = getNearClipDistance();
				double farDistance = getFarClipDistance();
				nearDistance = nearDistance <= 0.0 ? getAutoNearClipDistance()
						: nearDistance;
				farDistance = farDistance <= 0.0 ? getAutoFarClipDistance()
						: farDistance;

				double aspectratio = this.viewport.getWidth()
						/ this.viewport.getHeight();
				double vfov = fov / aspectratio;
				double widthdiv2 = nearDistance * Math.tan(vfov / 2.0);
				double distance = (getEye() == Eye.RIGHT ? 1 : -1) * 0.5
						* Settings.get().getEyeSeparation() * nearDistance
						/ Settings.get().getFocalLength();
				double top = widthdiv2;
				double bottom = -widthdiv2;
				double left = -aspectratio * widthdiv2 + distance;
				double right = aspectratio * widthdiv2 + distance;

				this.projection = fromFrustum(left, right, bottom, top,
						nearDistance, farDistance);
			}
		}
		
		this.viewSupport.loadGLViewState(dc, this.modelview, this.projection);
	}
}
