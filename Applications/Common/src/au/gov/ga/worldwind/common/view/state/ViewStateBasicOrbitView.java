package au.gov.ga.worldwind.common.view.state;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.view.transform.TransformBasicOrbitView;

public class ViewStateBasicOrbitView extends TransformBasicOrbitView
{
	@Override
	public void copyViewState(View view)
	{
		this.globe = view.getGlobe();

		Vec4 eyePoint = view.getCurrentEyePoint();
		Position eyePosition = globe.computePositionFromPoint(eyePoint);

		Vec4 centerPoint = view.getCenterPoint();
		//if the view is not looking at the globe, create a centerPoint just in front of the eye
		if (centerPoint == null)
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
