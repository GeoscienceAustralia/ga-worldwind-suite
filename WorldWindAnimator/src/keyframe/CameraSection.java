package keyframe;

import gov.nasa.worldwind.geom.Position;

public class CameraSection extends CameraPoint
{
	public final double centerVelocity;
	public final double lookAtVelocity;
	public final double centerZoomVelocity;
	public final double lookAtZoomVelocity;
	public final boolean allowDeceleration;
	
	public CameraSection(double time, Position eyePosition)
	{
		super(time, eyePosition);
	}
	
	public CameraSection(double time, Position eyePosition, Position lookAt)
	{
		super(time, eyePosition, lookAt);
	}
}
