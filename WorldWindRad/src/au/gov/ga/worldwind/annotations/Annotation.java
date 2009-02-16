package au.gov.ga.worldwind.annotations;

import java.io.Serializable;

public class Annotation implements Serializable
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

	public Annotation()
	{
	}

	public Annotation(String label, double latitude, double longitude)
	{
		this(label, latitude, longitude, -1);
	}

	public Annotation(String label, double latitude, double longitude,
			double minZoom)
	{
		this.label = label;
		this.latitude = latitude;
		this.longitude = longitude;
		this.minZoom = minZoom;
		this.visible = true;
	}
	
	public Annotation(Annotation annotation)
	{
		setValuesFrom(annotation);
	}
	
	public void setValuesFrom(Annotation annotation)
	{
		this.label = annotation.label;
		this.latitude = annotation.latitude;
		this.longitude = annotation.longitude;
		this.minZoom = annotation.minZoom;
		this.maxZoom = annotation.maxZoom;
		this.visible = annotation.visible;
		this.saveCamera = annotation.saveCamera;
		this.zoom = annotation.zoom;
		this.heading = annotation.heading;
		this.pitch = annotation.pitch;
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
}
