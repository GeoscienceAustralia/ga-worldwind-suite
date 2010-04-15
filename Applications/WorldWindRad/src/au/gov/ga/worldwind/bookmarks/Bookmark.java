package au.gov.ga.worldwind.bookmarks;

import java.io.Serializable;

public class Bookmark implements Serializable
{
	private String name;
	private double lat;
	private double lon;
	private double elevation;
	private double heading;
	private double pitch;
	private double zoom;
	
	public Bookmark()
	{
	}

	public Bookmark(String name, double lat, double lon, double elevation,
			double heading, double pitch, double zoom)
	{
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.elevation = elevation;
		this.heading = heading;
		this.pitch = pitch;
		this.zoom = zoom;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getLat()
	{
		return lat;
	}

	public void setLat(double lat)
	{
		this.lat = lat;
	}

	public double getLon()
	{
		return lon;
	}

	public void setLon(double lon)
	{
		this.lon = lon;
	}

	public double getElevation()
	{
		return elevation;
	}

	public void setElevation(double elevation)
	{
		this.elevation = elevation;
	}

	public double getHeading()
	{
		return heading;
	}

	public void setHeading(double heading)
	{
		this.heading = heading;
	}

	public double getPitch()
	{
		return pitch;
	}

	public void setPitch(double pitch)
	{
		this.pitch = pitch;
	}

	public double getZoom()
	{
		return zoom;
	}

	public void setZoom(double zoom)
	{
		this.zoom = zoom;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
}
