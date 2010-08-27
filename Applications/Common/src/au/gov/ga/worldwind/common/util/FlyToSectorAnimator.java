package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Rectangle;

public class FlyToSectorAnimator
{
	public static FlyToOrbitViewAnimator createFlyToSectorAnimator(OrbitView orbitView,
			Position beginCenterPos, Position endCenterPos, Angle beginHeading, Angle beginPitch,
			double beginZoom, LatLon endVisibleDelta, long timeToMove)
	{
		Rectangle viewport = orbitView.getViewport();
		Angle fieldOfView = orbitView.getFieldOfView();

		double deltaLonDegrees = Math.min(endVisibleDelta.getLongitude().degrees, 90);
		double deltaLatDegrees = Math.min(endVisibleDelta.getLatitude().degrees, 90);

		double degreesPerPixelWidth = deltaLonDegrees / viewport.getWidth();
		double degreesPerPixelHeight = deltaLatDegrees / viewport.getHeight();
		double degreesPerPixel = Math.max(degreesPerPixelWidth, degreesPerPixelHeight);

		double metersPerPixel = 111111.11 * degreesPerPixel; //very! approximate degrees to meters conversion
		metersPerPixel *= 1.1; //zoom out just a little more, to add a slight border

		double viewportWidth = viewport.getWidth();
		double pixelSizeScale =
				2 * fieldOfView.tanHalfAngle() / (viewportWidth <= 0 ? 1d : viewportWidth);

		double endZoom = metersPerPixel / pixelSizeScale;

		return FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(orbitView, beginCenterPos,
				endCenterPos, beginHeading, Angle.ZERO, beginPitch, Angle.ZERO, beginZoom, endZoom,
				timeToMove, true);
	}
}
