package keyframe;

import java.io.Serializable;

import gov.nasa.worldwind.geom.Position;

public class CameraPoint implements Comparable<CameraPoint>, Serializable
{
	public final double time;
	public final Position eyePosition;
	public final Position lookAt;

	//spring constants for eyePosition and lookAt in this class?

	public CameraPoint(double time, Position eyePosition)
	{
		this(time, eyePosition, null);
	}

	public CameraPoint(double time, Position eyePosition, Position lookAt)
	{
		this.time = time;
		this.eyePosition = eyePosition;
		this.lookAt = lookAt;
	}

	public int compareTo(CameraPoint o)
	{
		return Double.compare(time, o.time);
	}
}
