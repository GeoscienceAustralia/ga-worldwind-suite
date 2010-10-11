package au.gov.ga.worldwind.common.layers.point.types;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.common.layers.point.PointLayer;
import au.gov.ga.worldwind.common.layers.point.PointLayerHelper;
import au.gov.ga.worldwind.common.layers.point.PointProperties;
import au.gov.ga.worldwind.common.util.DefaultLauncher;

/**
 * {@link PointLayer} implementation which extends {@link MarkerLayer} and uses
 * Markers to represent points.
 * 
 * @author Michael de Hoog
 */
public class MarkerPointLayer extends MarkerLayer implements PointLayer, SelectListener
{
	private final PointLayerHelper helper;

	private List<Marker> markers = new ArrayList<Marker>();
	private UrlMarker pickedMarker;
	private Material backupMaterial;
	private Material highlightMaterial = new Material(Color.white);

	public MarkerPointLayer(PointLayerHelper helper)
	{
		this.helper = helper;
	}

	@Override
	public void render(DrawContext dc)
	{
		if (isEnabled())
		{
			helper.requestPoints(this);
		}
		super.render(dc);
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		wwd.addSelectListener(this);
	}

	@Override
	public Sector getSector()
	{
		return helper.getSector();
	}

	@Override
	public void addPoint(Position position, AVList attributeValues)
	{
		MarkerAttributes attributes = new BasicMarkerAttributes();
		PointProperties properties = helper.getStyle(attributeValues);
		properties.style.setPropertiesFromAttributes(helper.getContext(), attributeValues,
				attributes);
		UrlMarker marker = new UrlMarker(position, attributes);
		marker.setUrl(properties.link);
		markers.add(marker);
	}

	@Override
	public void loadComplete()
	{
		setMarkers(markers);
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return helper.getUrl();
	}

	@Override
	public String getDataCacheName()
	{
		return helper.getDataCacheName();
	}

	@Override
	public void selected(SelectEvent e)
	{
		if (e == null)
			return;

		PickedObject topPickedObject = e.getTopPickedObject();
		if (topPickedObject != null && topPickedObject.getObject() instanceof UrlMarker)
		{
			if (pickedMarker != null)
			{
				highlight(pickedMarker, false);
			}

			pickedMarker = (UrlMarker) topPickedObject.getObject();
			highlight(pickedMarker, true);

			if (e.getEventAction() == SelectEvent.LEFT_PRESS)
			{
				String link = pickedMarker.getUrl();
				if (link != null)
				{
					try
					{
						URL url = new URL(link);
						DefaultLauncher.openURL(url);
					}
					catch (MalformedURLException m)
					{
					}
				}
			}
		}
		else if (pickedMarker != null)
		{
			highlight(pickedMarker, false);
			pickedMarker = null;
		}
	}

	protected void highlight(Marker marker, boolean highlight)
	{
		if (highlight)
		{
			backupMaterial = marker.getAttributes().getMaterial();
			marker.getAttributes().setMaterial(highlightMaterial);
		}
		else if (backupMaterial != null)
		{
			marker.getAttributes().setMaterial(backupMaterial);
		}
	}
}
