package au.gov.ga.worldwind.common.view.state;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.view.transform.TransformBasicOrbitView;

public class ViewStateBasicOrbitView extends TransformBasicOrbitView
{
	protected double minimumFarDistance = MINIMUM_FAR_DISTANCE;

	@Override
	public void beforeComputeMatrices()
	{
		super.beforeComputeMatrices();
		minimumFarDistance = globe.getDiameter() / 2d;
	}

	@Override
	protected double computeFarClipDistance()
	{
		double far = super.computeFarClipDistance();
		return Math.max(far, minimumFarDistance);
	}

	@Override
	public void copyViewState(View view)
	{
		this.globe = view.getGlobe();

		Vec4 eyePoint = view.getCurrentEyePoint();
		Position eyePosition = globe.computePositionFromPoint(eyePoint);

		double eyePositionElevation = globe.getElevation(eyePosition.latitude, eyePosition.longitude);
		boolean belowSurface = eyePosition.elevation < eyePositionElevation;

		Vec4 centerPoint = view.getCenterPoint();
		//if the view is not looking at the globe, create a centerPoint just in front of the eye
		if (centerPoint == null || belowSurface)
		{
			Vec4 forward = view.getForwardVector();
			centerPoint = eyePoint.add3(forward);
		}
		Position centerPosition = globe.computePositionFromPoint(centerPoint);

		if (trySetOrientation(eyePosition, centerPosition))
			return;

		//if a center position just in front of the eye doesn't work, then try the closest position
		centerPosition = Util.computeViewClosestCenterPosition(view, eyePoint);

		if (trySetOrientation(eyePosition, centerPosition))
			return;

		//if everything failed, just set the view using the heading/pitch
		setEyePosition(eyePosition);
		setHeading(view.getHeading());
		setPitch(view.getPitch());
	}
}
