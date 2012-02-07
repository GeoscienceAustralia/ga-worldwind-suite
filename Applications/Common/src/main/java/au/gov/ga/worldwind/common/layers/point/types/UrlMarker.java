package au.gov.ga.worldwind.common.layers.point.types;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

/**
 * Extension to the {@link BasicMarker} class which adds a 'url' property, so
 * that markers can have associated urls.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class UrlMarker extends BasicMarker
{
	private String url;
	private String tooltipText;
	private Material backupMaterial;

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

	public String getTooltipText()
	{
		return tooltipText;
	}

	public void setTooltipText(String tooltipText)
	{
		this.tooltipText = tooltipText;
	}
	
	public void backupMaterial()
	{
		if(backupMaterial == null)
			backupMaterial = getAttributes().getMaterial();
	}
	
	public void restoreMaterial()
	{
		if(backupMaterial != null)
			getAttributes().setMaterial(backupMaterial);
	}
}
