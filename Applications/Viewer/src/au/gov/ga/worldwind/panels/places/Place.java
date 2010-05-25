package au.gov.ga.worldwind.panels.places;

import java.io.Serializable;

public class Place implements Serializable
{
	private String label = "";
	private double latitude = 0;
	private double longitude = 0;
	private boolean visible = true;
	private double minZoom = -1;
	private double maxZoom = -1;
	private boolean saveCamera = false;
	private double zoom = 0;
	private double heading = 0;
	private double pitch = 0;
	private boolean excludeFromPlaylist = false;

	public Place()
	{
	}

	public Place(String label, double latitude, double longitude)
	{
		this(label, latitude, longitude, -1);
	}

	public Place(String label, double latitude, double longitude,
			double minZoom)
	{
		this.label = label;
		this.latitude = latitude;
		this.longitude = longitude;
		this.minZoom = minZoom;
		this.visible = true;
	}

	public Place(Place place)
	{
		setValuesFrom(place);
	}

	public void setValuesFrom(Place place)
	{
		this.label = place.label;
		this.latitude = place.latitude;
		this.longitude = place.longitude;
		this.minZoom = place.minZoom;
		this.maxZoom = place.maxZoom;
		this.visible = place.visible;
		this.saveCamera = place.saveCamera;
		this.zoom = place.zoom;
		this.heading = place.heading;
		this.pitch = place.pitch;
		this.excludeFromPlaylist = place.excludeFromPlaylist;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean enabled)
	{
		this.visible = enabled;
	}

	public double getMinZoom()
	{
		return minZoom;
	}

	public void setMinZoom(double minZoom)
	{
		this.minZoom = minZoom;
	}

	public double getMaxZoom()
	{
		return maxZoom;
	}

	public void setMaxZoom(double maxZoom)
	{
		this.maxZoom = maxZoom;
	}

	public boolean isSaveCamera()
	{
		return saveCamera;
	}

	public void setSaveCamera(boolean saveCamera)
	{
		this.saveCamera = saveCamera;
	}

	public double getZoom()
	{
		return zoom;
	}

	public void setZoom(double zoom)
	{
		this.zoom = zoom;
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

	public boolean isExcludeFromPlaylist()
	{
		return excludeFromPlaylist;
	}

	public void setExcludeFromPlaylist(boolean excludeFromPlaylist)
	{
		this.excludeFromPlaylist = excludeFromPlaylist;
	}
}
