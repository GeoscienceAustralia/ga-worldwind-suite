package annotations;

import java.io.Serializable;

public class Annotation implements Serializable
{
	private String label;
	private double latitude;
	private double longitude;
	private boolean visible;
	
	public Annotation()
	{
	}
	
	public Annotation(String label, double latitude, double longitude)
	{
		this.label = label;
		this.latitude = latitude;
		this.longitude = longitude;
		this.visible = true;
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
}
