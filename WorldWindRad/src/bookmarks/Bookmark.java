package bookmarks;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

public class Bookmark
{
	public String name;
	public Position center;
	public Angle heading;
	public Angle pitch;
	public double zoom;

	public Bookmark(String name, Position center, Angle heading,
			Angle pitch, double zoom)
	{
		this.name = name;
		this.center = center;
		this.heading = heading;
		this.pitch = pitch;
		this.zoom = zoom;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
