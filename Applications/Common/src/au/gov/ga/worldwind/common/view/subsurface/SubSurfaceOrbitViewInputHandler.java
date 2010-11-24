package au.gov.ga.worldwind.common.view.subsurface;

import gov.nasa.worldwind.animation.AnimationController;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;

public class SubSurfaceOrbitViewInputHandler extends OrbitViewInputHandler
{
	@Override
	protected void changeZoom(BasicOrbitView view, AnimationController animControl, double change,
			ActionAttributes attrib)
	{
		view.computeAndSetViewCenterIfNeeded();

		if (animControl.get(VIEW_ANIM_ZOOM) != null)
			animControl.remove(VIEW_ANIM_ZOOM);

		Vec4 eyePoint = view.getCurrentEyePoint();
		Position eyePosition = view.getGlobe().computePositionFromPoint(eyePoint);
		double elevation = view.getGlobe().getElevation(eyePosition.latitude, eyePosition.longitude);
		Position surfacePosition = new Position(eyePosition, elevation);
		Vec4 surfacePoint = view.getGlobe().computePointFromPosition(surfacePosition);
		double altitude = eyePoint.getLength3() - surfacePoint.getLength3();

		view.setZoom(computeNewZoomFromAltitude(altitude, view.getZoom(), change, view.getOrbitViewLimits()));

		view.firePropertyChange(AVKey.VIEW, null, view);
	}

	protected static double computeNewZoomFromAltitude(double altitude, double curZoom, double change,
			OrbitViewLimits limits)
	{
		altitude = Math.max(20000, altitude);
		double newAltitude = computeNewZoom(altitude, change, limits);
		double delta = newAltitude - altitude;
		return curZoom + delta;
	}
}