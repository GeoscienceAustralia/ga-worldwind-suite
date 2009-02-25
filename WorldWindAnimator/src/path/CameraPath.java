package path;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.OrbitView;

public class CameraPath
{
	private Parameter eyeLat;
	private Parameter eyeLon;
	private Parameter eyeZoom;
	private Parameter targetLat;
	private Parameter targetLon;
	private Parameter targetZoom;

	public void applyFrame(OrbitView view, int frame)
	{
		Position eyePosition = Position.fromDegrees(eyeLat.getValue(frame),
				eyeLon.getValue(frame), eyeZoom.getValue(frame));
		Position centerPosition = Position.fromDegrees(targetLat
				.getValue(frame), targetLon.getValue(frame), targetZoom
				.getValue(frame));
		view.setOrientation(eyePosition, centerPosition);
	}
}
