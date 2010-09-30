package au.gov.ga.worldwind.common.view;

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

public class FreeView extends BasicView
{
	public FreeView()
	{
		this.viewInputHandler = new BasicFreeViewInputHandler();
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

		//========== modelview matrix state ==========//
		// Compute the current modelview matrix.
		this.modelview = computeModelViewMatrix();
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

		// Compute the current viewport dimensions.
		double viewportWidth = this.viewport.getWidth() <= 0.0 ? 1.0 : this.viewport.getWidth();
		double viewportHeight = this.viewport.getHeight() <= 0.0 ? 1.0 : this.viewport.getHeight();

		// Compute the current projection matrix.
		this.projection =
				Matrix.fromPerspective(this.fieldOfView, viewportWidth, viewportHeight,
						this.nearClipDistance, this.farClipDistance);

		// Compute the current frustum.
		this.frustum =
				Frustum.fromPerspective(this.fieldOfView, (int) viewportWidth,
						(int) viewportHeight, this.nearClipDistance, this.farClipDistance);

		//========== load GL matrix state ==========//
		loadGLViewState(dc, this.modelview, this.projection);

		//========== after apply (GL matrix state) ==========//
		afterDoApply();
	}

	protected void afterDoApply()
	{
		// Clear cached computations.
		this.lastEyePosition = null;
		this.lastEyePoint = null;
		this.lastUpVector = null;
		this.lastForwardVector = null;
		this.lastFrustumInModelCoords = null;
	}

	protected Matrix computeModelViewMatrix()
	{
		//default:
		//return ViewUtil.computeTransformMatrix(this.globe, this.eyePosition, this.heading, this.pitch, this.roll);

		Matrix transform = computeRotationTransform(heading, pitch, roll);
		transform = transform.multiply(computePositionTransform(globe, eyePosition));
		return transform;
	}

	protected Matrix computeRotationTransform(Angle heading, Angle pitch, Angle roll)
	{
		Matrix transform = Matrix.IDENTITY;
		transform = transform.multiply(Matrix.fromAxisAngle(pitch, -1, 0, 0));
		transform = transform.multiply(Matrix.fromAxisAngle(roll, 0, 1, 0));
		transform = transform.multiply(Matrix.fromAxisAngle(heading, 0, 0, 1));
		return transform;
	}

	protected Matrix computePositionTransform(Globe globe, Position eyePosition)
	{
		Vec4 point = globe.computePointFromPosition(eyePosition);
		return Matrix.fromTranslation(point.getNegative3());
	}

	public void rotate(double deltaX, double deltaY, double deltaZ)
	{
		Matrix transform = computeRotationTransform(heading, pitch, roll);

		transform = Matrix.fromAxisAngle(Angle.fromDegrees(deltaX), 1, 0, 0).multiply(transform);
		transform = Matrix.fromAxisAngle(Angle.fromDegrees(deltaY), 0, 1, 0).multiply(transform);
		transform = Matrix.fromAxisAngle(Angle.fromDegrees(deltaZ), 0, 0, 1).multiply(transform);

		setHeading(ViewUtil.computeHeading(transform));
		setPitch(ViewUtil.computePitch(transform));
		setRoll(ViewUtil.computeRoll(transform));
	}
}
