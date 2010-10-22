package au.gov.ga.worldwind.viewer.panels.places;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.io.Serializable;

import au.gov.ga.worldwind.viewer.panels.layers.INode;

public class Place implements Serializable
{
	private String label = "";
	private LatLon latlon = LatLon.ZERO;
	private boolean visible = true;
	private double minZoom = -1;
	private double maxZoom = -1;
	private boolean saveCamera = false;
	private Position eyePosition = null;
	private Vec4 upVector = null;
	private boolean excludeFromPlaylist = false;
	private INode layers = null;

	public Place()
	{
	}

	public Place(String label, LatLon latlon)
	{
		this(label, latlon, -1);
	}

	public Place(String label, LatLon latlon, double minZoom)
	{
		this.label = label;
		this.latlon = latlon;
		this.minZoom = minZoom;
	}

	public Place(Place place)
	{
		setValuesFrom(place);
	}

	public void setValuesFrom(Place place)
	{
		this.label = place.label;
		this.latlon = place.latlon;
		this.visible = place.visible;
		this.minZoom = place.minZoom;
		this.maxZoom = place.maxZoom;
		this.saveCamera = place.saveCamera;
		this.eyePosition = place.eyePosition;
		this.upVector = place.upVector;
		this.excludeFromPlaylist = place.excludeFromPlaylist;
		this.layers = place.layers;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		if (label == null)
			label = "";
		this.label = label;
	}

	public LatLon getLatLon()
	{
		return latlon;
	}

	public void setLatLon(LatLon latlon)
	{
		if (latlon == null)
			latlon = LatLon.ZERO;
		this.latlon = latlon;
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

	public Position getEyePosition()
	{
		return eyePosition;
	}

	public void setEyePosition(Position eyePosition)
	{
		this.eyePosition = eyePosition;
	}

	public Vec4 getUpVector()
	{
		return upVector;
	}

	public void setUpVector(Vec4 upVector)
	{
		this.upVector = upVector;
	}

	public boolean isExcludeFromPlaylist()
	{
		return excludeFromPlaylist;
	}

	public void setExcludeFromPlaylist(boolean excludeFromPlaylist)
	{
		this.excludeFromPlaylist = excludeFromPlaylist;
	}

	public INode getLayers()
	{
		return layers;
	}

	public void setLayers(INode layers)
	{
		this.layers = layers;
	}
}
