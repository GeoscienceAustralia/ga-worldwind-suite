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

	public CameraPath(Parameter eyeLat, Parameter eyeLon, Parameter eyeZoom,
			Parameter targetLat, Parameter targetLon, Parameter targetZoom)
	{
		this.eyeLat = eyeLat;
		this.eyeLon = eyeLon;
		this.eyeZoom = eyeZoom;
		this.targetLat = targetLat;
		this.targetLon = targetLon;
		this.targetZoom = targetZoom;
	}

	public void applyFrame(OrbitView view, int frame)
	{
		Position eyePosition = Position.fromDegrees(eyeLat.getValue(frame),
				eyeLon.getValue(frame), z2c(eyeZoom.getValue(frame)));
		Position centerPosition = Position.fromDegrees(targetLat
				.getValue(frame), targetLon.getValue(frame), z2c(targetZoom
				.getValue(frame)));
		view.setOrientation(eyePosition, centerPosition);
	}

	public int getFirstFrame()
	{
		int frame = eyeLat.getFirstFrame();
		frame = Math.min(frame, eyeLon.getFirstFrame());
		frame = Math.min(frame, eyeZoom.getFirstFrame());
		frame = Math.min(frame, targetLat.getFirstFrame());
		frame = Math.min(frame, targetLon.getFirstFrame());
		frame = Math.min(frame, targetZoom.getFirstFrame());
		return frame;
	}

	public int getLastFrame()
	{
		int frame = eyeLat.getLastFrame();
		frame = Math.max(frame, eyeLon.getLastFrame());
		frame = Math.max(frame, eyeZoom.getLastFrame());
		frame = Math.max(frame, targetLat.getLastFrame());
		frame = Math.max(frame, targetLon.getLastFrame());
		frame = Math.max(frame, targetZoom.getLastFrame());
		return frame;
	}

	public static double c2z(double camera)
	{
		return Math.log(camera + 1);
	}

	public static double z2c(double zoom)
	{
		return Math.pow(Math.E, zoom) - 1;
	}
}
