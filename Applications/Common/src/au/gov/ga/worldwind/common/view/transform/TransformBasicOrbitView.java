package au.gov.ga.worldwind.common.view.transform;

import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;
import gov.nasa.worldwind.view.orbit.OrientationBasicOrbitView;

import javax.media.opengl.GL;

public class TransformBasicOrbitView extends OrientationBasicOrbitView implements TransformView
{
	@Override
	public void beforeComputeMatrices()
	{
	}

	@Override
	public Matrix computeModelView()
	{
		return OrbitViewInputSupport.computeTransformMatrix(this.globe,
															this.center,
															this.heading,
															this.pitch,
															this.zoom);
	}

	@Override
	public Matrix computeProjection()
	{
		double viewportWidth = this.viewport.getWidth() <= 0.0 ? 1.0 : this.viewport.getWidth();
		double viewportHeight = this.viewport.getHeight() <= 0.0 ? 1.0 : this.viewport.getHeight();

		return Matrix.fromPerspective(this.fieldOfView,
										viewportWidth,
										viewportHeight,
										this.nearClipDistance,
										this.farClipDistance);
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

		beforeComputeMatrices();

		//========== modelview matrix state ==========//
		// Compute the current modelview matrix.
		this.modelview = computeModelView();
		if (this.modelview == null)
		{
			this.modelview = Matrix.IDENTITY;
		}
		// Compute the current inverse-modelview matrix.
		this.modelviewInv = this.modelview.getInverse();
		if (this.modelviewInv == null)
		{
			this.modelviewInv = Matrix.IDENTITY;
		}

		//========== projection matrix state ==========//
		// Get the current OpenGL viewport state.
		int[] viewportArray = new int[4];
		this.dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
		this.viewport = new java.awt.Rectangle(viewportArray[0], viewportArray[1], viewportArray[2], viewportArray[3]);

		// Compute the current clip plane distances (Use utils methods to better handle underlying elevation data)
		this.nearClipDistance = TransformViewUtils.computeNearClippingDistance(dc);
		this.farClipDistance = TransformViewUtils.computeFarClippingDistance(dc);
		
		// Compute the current projection matrix.
		this.projection = computeProjection();
		
		// Compute the current frustum.
		this.frustum = Frustum.fromProjectionMatrix(this.projection);

		//========== load GL matrix state ==========//
		loadGLViewState(dc, this.modelview, this.projection);

		//========== after apply (GL matrix state) ==========//
		afterDoApply();
	}
}
