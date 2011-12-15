package au.gov.ga.worldwind.common.view.free;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicView;
import gov.nasa.worldwind.view.ViewUtil;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.view.transform.TransformView;

public class FreeView extends BasicView implements TransformView
{
	protected double minimumFarDistance = MINIMUM_FAR_DISTANCE;
	protected double minimumNearDistance = 0.1;

	public FreeView()
	{
		this.viewInputHandler = new BasicFreeViewInputHandler();
	}
	
	protected static Matrix computeRotationTransform(Angle heading, Angle pitch, Angle roll)
	{
		Matrix transform = Matrix.IDENTITY;
		transform = transform.multiply(Matrix.fromAxisAngle(pitch, -1, 0, 0));
		transform = transform.multiply(Matrix.fromAxisAngle(roll, 0, 1, 0));
		transform = transform.multiply(Matrix.fromAxisAngle(heading, 0, 0, 1));
		return transform;
	}

	protected static Matrix computePositionTransform(Globe globe, Position eyePosition)
	{
		Vec4 point = globe.computePointFromPosition(eyePosition);
		return Matrix.fromTranslation(point.getNegative3());
	}

	@Override
	public void beforeComputeMatrices()
	{
		minimumFarDistance = globe.getDiameter() / 2d;
	}

	@Override
	public Matrix computeModelView()
	{
		//default:
		//return ViewUtil.computeTransformMatrix(this.globe, this.eyePosition, this.heading, this.pitch, this.roll);

		Matrix transform = computeRotationTransform(heading, pitch, roll);
		transform = transform.multiply(computePositionTransform(globe, eyePosition));
		return transform;
	}

	@Override
	public Matrix computeProjection()
	{
		double viewportWidth = this.viewport.getWidth() <= 0.0 ? 1.0 : this.viewport.getWidth();
		double viewportHeight = this.viewport.getHeight() <= 0.0 ? 1.0 : this.viewport.getHeight();

		return Matrix.fromPerspective(this.fieldOfView, viewportWidth, viewportHeight,
				this.nearClipDistance, this.farClipDistance);
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
		this.projection = computeProjection();
		// Compute the current frustum.
		this.frustum = Frustum.fromProjectionMatrix(this.projection);

		//========== load GL matrix state ==========//
		loadGLViewState(dc, this.modelview, this.projection);

		//========== after apply (GL matrix state) ==========//
		afterDoApply();
	}

	protected void afterDoApply()
	{
		// Establish frame-specific values.
        this.lastEyePosition = this.computeEyePositionFromModelview();
        this.horizonDistance = this.computeHorizonDistance();
		
		// Clear cached computations.
		this.lastEyePoint = null;
		this.lastUpVector = null;
		this.lastForwardVector = null;
		this.lastFrustumInModelCoords = null;
	}

	@Override
	protected double computeNearDistance(Position eyePosition)
	{
		double near = super.computeNearDistance(eyePosition);
		return near < minimumNearDistance ? minimumNearDistance : near;
	}

	@Override
	protected double computeFarDistance(Position eyePosition)
	{
		double far = super.computeFarDistance(eyePosition);
		return far < minimumFarDistance ? minimumFarDistance : far;
	}

	@Override
	public void copyViewState(View view)
	{
		this.globe = view.getGlobe();

		Matrix transform = view.getModelviewMatrix();
		setHeading(ViewUtil.computeHeading(transform));
		setPitch(ViewUtil.computePitch(transform));
		setRoll(ViewUtil.computeRoll(transform));

		setEyePosition(view.getCurrentEyePosition());
	}
}
