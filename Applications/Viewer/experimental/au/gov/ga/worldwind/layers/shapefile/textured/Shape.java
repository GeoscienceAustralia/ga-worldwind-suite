package au.gov.ga.worldwind.layers.shapefile.textured;

import gov.nasa.worldwind.geom.LatLon;

import java.awt.Color;
import java.net.URL;
import java.util.List;

public class Shape
{
	private final URL resourceURL;
	private final int recordNumber;
	private final Color color;
	private final Color pickColor;
	private final List<LatLon> points;

	public Shape(URL resourceURL, int recordNumber, Color color, Color pickColor,
			List<LatLon> points)
	{
		this.recordNumber = recordNumber;
		this.resourceURL = resourceURL;
		this.color = color;
		this.pickColor = pickColor;
		this.points = points;
	}

	public URL getResourceURL()
	{
		return resourceURL;
	}

	public int getRecordNumber()
	{
		return recordNumber;
	}

	public Color getColor()
	{
		return color;
	}

	public Color getPickColor()
	{
		return pickColor;
	}

	public List<LatLon> getPoints()
	{
		return points;
	}

	/*@Override
	public int hashCode()
	{
		if (identifier != null)
			return identifier.hashCode();
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (identifier != null && obj instanceof Shape)
			return identifier.equals(((Shape) obj).identifier);
		return super.equals(obj);
	}*/
}
