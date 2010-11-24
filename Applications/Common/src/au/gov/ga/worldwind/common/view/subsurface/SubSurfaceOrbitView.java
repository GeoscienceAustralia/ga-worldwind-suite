package au.gov.ga.worldwind.common.view.subsurface;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.AccessibleOrbitViewInputSupport;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;
import au.gov.ga.worldwind.common.view.state.ViewStateBasicOrbitView;

public class SubSurfaceOrbitView extends ViewStateBasicOrbitView
{
	public SubSurfaceOrbitView()
	{
		setViewInputHandler(new SubSurfaceOrbitViewInputHandler());

		setDetectCollisions(false);
		getViewInputHandler().setEnableSmoothing(false);
		getOrbitViewLimits().setPitchLimits(Angle.fromDegrees(-180), Angle.fromDegrees(180));
	}

	@Override
	public void focusOnViewportCenter()
	{
		if (this.dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		if (this.globe == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Matrix modelview =
				OrbitViewInputSupport.computeTransformMatrix(this.globe, this.center, this.heading, this.pitch,
						this.zoom);
		if (modelview != null)
		{
			Matrix modelviewInv = modelview.getInverse();
			if (modelviewInv != null)
			{
				Vec4 eyePoint = Vec4.UNIT_W.transformBy4(modelviewInv);
				Vec4 forward = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelviewInv);

				//calculate the center point as 1 unit vector foward from eye point
				Vec4 newCenterPoint = eyePoint.add3(forward);

				AccessibleOrbitViewInputSupport.AccessibleOrbitViewState modelCoords =
						AccessibleOrbitViewInputSupport.computeOrbitViewState(this.globe, modelview, newCenterPoint);
				if (validateModelCoordinates(modelCoords))
				{
					setModelCoordinates(modelCoords);
				}
			}
		}
	}

	@Override
	public void computeAndSetViewCenter()
	{
		super.computeAndSetViewCenter();
		//never let the view be in focus
		setViewOutOfFocus(true);
	}

	@Override
	public void setOrientation(Position eyePosition, Position centerPosition)
	{
		super.setOrientation(eyePosition, centerPosition);
		computeAndSetViewCenterIfNeeded();
	}

	@Override
	public void setZoom(double zoom)
	{
		this.zoom = zoom;
		computeAndSetViewCenterIfNeeded();
	}
}
