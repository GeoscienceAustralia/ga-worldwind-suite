package keyframe;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

public class CameraPath implements Serializable
{
	public final CameraPoint initialPoint;
	public final SortedSet<CameraSection> sections = new TreeSet<CameraSection>();

	public CameraPath()
	{
	}
}
