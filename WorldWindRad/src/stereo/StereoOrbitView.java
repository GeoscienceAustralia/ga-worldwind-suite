package stereo;

import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicOrbitView;

import javax.media.opengl.GL;

import settings.Settings;
import settings.Settings.ProjectionMode;

public class StereoOrbitView extends BasicOrbitView
{
	public enum Eye
	{
		LEFT,
		RIGHT
	}

	private Eye eye = Eye.LEFT;
	private boolean drawing;

	public StereoOrbitView()
	{
		this(new StereoOrbitViewModel());
	}

	public StereoOrbitView(StereoOrbitViewModel orbitViewModel)
	{
		super(orbitViewModel);
		orbitViewModel.setView(this);
	}

	@Override
	protected void doApply(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (dc.getGL() == null)
		{
			String message = Logging
					.getMessage("nullValue.DrawingContextGLIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (dc.getGlobe() == null)
		{
			String message = Logging
					.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Update DrawContext and Globe references.
		this.dc = dc;
		this.globe = this.dc.getGlobe();

		//========== modelview matrix state ==========//
		// Compute the current modelview matrix.
		this.modelview = this.orbitViewModel.computeTransformMatrix(this.globe,
				this.center, this.heading, this.pitch, this.zoom);
		if (this.modelview == null)
			this.modelview = Matrix.IDENTITY;

		// Compute the current inverse-modelview matrix.
		this.modelviewInv = this.modelview.getInverse();
		if (this.modelviewInv == null)
			this.modelviewInv = Matrix.IDENTITY;

		//========== projection matrix state ==========//
		// Get the current OpenGL viewport state.
		int[] viewportArray = new int[4];
		this.dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
		this.viewport = new java.awt.Rectangle(viewportArray[0],
				viewportArray[1], viewportArray[2], viewportArray[3]);
		// Compute the current clip plane distances.
		double nearDistance = this.nearClipDistance > 0 ? this.nearClipDistance
				: getAutoNearClipDistance();
		double farDistance = this.farClipDistance > 0 ? this.farClipDistance
				: getAutoFarClipDistance();
		// Compute the current projection matrix.
		this.projection = Matrix.fromPerspective(this.fieldOfView,
				this.viewport.getWidth(), this.viewport.getHeight(),
				nearDistance, farDistance);
		// Compute the current frustum.
		this.frustum = Frustum.fromPerspective(this.fieldOfView,
				(int) this.viewport.getWidth(),
				(int) this.viewport.getHeight(), nearDistance, farDistance);

		//stereo
		if (isDrawing())
		{
			if (Settings.get().getProjectionMode() == ProjectionMode.ASYMMETRIC_FRUSTUM)
			{
				double aspectratio = this.viewport.getWidth()
						/ this.viewport.getHeight();
				double vfov = this.fieldOfView.radians / aspectratio;
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

		//========== load GL matrix state ==========//
		this.viewSupport.loadGLViewState(dc, this.modelview, this.projection);

		//========== after apply (GL matrix state) ==========//
		afterDoApply();
	}

	private static Matrix fromFrustum(double left, double right, double bottom,
			double top, double near, double far)
	{
		double A = (right + left) / (right - left);
		double B = (top + bottom) / (top - bottom);
		double C = (far + near) / (far - near);
		double D = (2 * far * near) / (far - near);
		double E = (2 * near) / (right - left);
		double F = (2 * near) / (top - bottom);
		return new Matrix(E, 0, A, 0, 0, F, B, 0, 0, 0, C, D, 0, 0, -1, 0);
	}

	public Eye getEye()
	{
		return eye;
	}

	public void setEye(Eye eye)
	{
		this.eye = eye;
	}

	public void setDrawing(boolean drawing)
	{
		this.drawing = drawing;
	}

	public boolean isDrawing()
	{
		return drawing;
	}
}
