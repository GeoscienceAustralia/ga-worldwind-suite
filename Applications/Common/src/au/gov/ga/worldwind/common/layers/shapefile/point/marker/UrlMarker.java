package au.gov.ga.worldwind.common.layers.shapefile.point.marker;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

public class UrlMarker extends BasicMarker
{
	private String url;
	
	public UrlMarker(Position position, MarkerAttributes attrs)
	{
		super(position, attrs);
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
}
