package au.gov.ga.worldwind.animator.view.roll;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;

import javax.media.opengl.GL;

/**
 * @author Michael de Hoog
 */
public class BasicRollOrbitView extends BasicOrbitView implements RollOrbitView
{
	public BasicRollOrbitView()
	{
		super();
		setOrbitViewLimits(new BasicRollOrbitViewLimits());
	}

	public RollOrbitViewLimits getRollOrbitViewLimits()
	{
		return (RollOrbitViewLimits) getOrbitViewLimits();
	}

	public void setRollOrbitViewLimits(RollOrbitViewLimits viewLimits)
	{
		setOrbitViewLimits(viewLimits);
	}

	public Angle getRoll()
	{
		return roll;
	}

	public void setRoll(Angle roll)
	{
		if (roll == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		roll = normalizedRoll(roll);
		roll = BasicRollOrbitViewLimits.limitRoll(roll, this.getRollOrbitViewLimits());
		this.roll = roll;
	}

	public static Angle normalizedRoll(Angle unnormalizedRoll)
	{
		if (unnormalizedRoll == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Normalize roll to the range [-180, 180].
		double degrees = unnormalizedRoll.degrees;
		double roll = degrees % 360;
		return Angle.fromDegrees(roll > 180 ? roll - 360 : (roll < -180 ? 360 + roll : roll));
	}

	@Override
	protected void doApply(DrawContext dc)
	{
		//from BasicOrbitView.doApply(), with roll added

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
		this.modelview = RollOrbitViewInputSupport.computeTransformMatrix(this.globe, this.center, this.heading, this.pitch, this.roll, this.zoom);
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
		// Compute the current clip plane distances.

		this.nearClipDistance = computeNearClipDistance();
		this.farClipDistance = computeFarClipDistance();
		// Compute the current viewport dimensions.
		double viewportWidth = this.viewport.getWidth() <= 0.0 ? 1.0 : this.viewport.getWidth();
		double viewportHeight = this.viewport.getHeight() <= 0.0 ? 1.0 : this.viewport.getHeight();
		// Compute the current projection matrix.
		this.projection = Matrix.fromPerspective(this.fieldOfView, viewportWidth, viewportHeight, this.nearClipDistance, this.farClipDistance);
		// Compute the current frustum.
		this.frustum = Frustum.fromPerspective(this.fieldOfView, (int) viewportWidth, (int) viewportHeight, this.nearClipDistance, this.farClipDistance);

		//========== load GL matrix state ==========//
		loadGLViewState(dc, this.modelview, this.projection);

		//========== after apply (GL matrix state) ==========//
		afterDoApply();
	}
}
