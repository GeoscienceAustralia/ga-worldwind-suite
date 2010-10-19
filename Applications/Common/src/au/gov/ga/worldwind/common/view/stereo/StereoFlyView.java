package au.gov.ga.worldwind.common.view.stereo;

import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.ViewUtil;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.view.state.ViewStateBasicFlyView;

public class StereoFlyView extends ViewStateBasicFlyView implements StereoView
{
	private Eye eye = Eye.LEFT;
	private boolean drawingStereo = false;
	private double separationExaggeration = 1;

	private double focalLength = 1;
	private double eyeSeparation = 0;

	@Override
	public Eye getEye()
	{
		return eye;
	}

	@Override
	public void setEye(Eye eye)
	{
		this.eye = eye;
	}

	@Override
	public boolean isDrawingStereo()
	{
		return drawingStereo;
	}

	@Override
	public void setDrawingStereo(boolean drawingStereo)
	{
		this.drawingStereo = drawingStereo;
	}

	@Override
	public double getSeparationExaggeration()
	{
		return separationExaggeration;
	}

	@Override
	public void setSeparationExaggeration(double separationExaggeration)
	{
		this.separationExaggeration = separationExaggeration;
	}

	protected void calculateStereoParameters()
	{
		AsymmetricFrutumParameters parameters =
				StereoViewSupport.calculateStereoParameters(this, separationExaggeration);
		this.focalLength = parameters.focalLength;
		this.eyeSeparation = parameters.eyeSeparation;
	}

	@Override
	public Matrix calculateProjectionMatrix(double nearDistance, double farDistance)
	{
		if (isDrawingStereo())
		{
			return StereoViewSupport.calculateStereoProjectionMatrix(this, nearDistance,
					farDistance, eye, focalLength, eyeSeparation);
		}
		else
		{
			return StereoViewSupport.calculateMonoProjectionMatrix(this, nearDistance, farDistance);
		}
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
			String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (dc.getGlobe() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Update DrawContext and Globe references.
		this.dc = dc;
		this.globe = this.dc.getGlobe();


		//calculate the focal length and eye separation
		calculateStereoParameters();


		//========== modelview matrix state ==========//
		// Compute the current modelview matrix.
		this.modelview =
				ViewUtil.computeTransformMatrix(this.globe, this.eyePosition, this.heading,
						this.pitch, this.roll);
		this.modelview =
				StereoViewSupport.transformTransformMatrix(this, this.modelview, eyeSeparation);
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
		this.viewport =
				new java.awt.Rectangle(viewportArray[0], viewportArray[1], viewportArray[2],
						viewportArray[3]);

		// Compute the current clip plane distances.
		this.nearClipDistance = this.computeNearClipDistance();
		this.farClipDistance = this.computeFarClipDistance();

		// Compute the current projection matrix.
		this.projection = calculateProjectionMatrix(this.nearClipDistance, this.farClipDistance);
		// Compute the current frustum.
		this.frustum = Frustum.fromProjectionMatrix(this.projection);

		//========== load GL matrix state ==========//
		loadGLViewState(dc, this.modelview, this.projection);

		//========== after apply (GL matrix state) ==========//
		afterDoApply();
	}
}
