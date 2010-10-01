package au.gov.ga.worldwind.common.layers.shapefile.point.marker;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.awt.Color;
import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.shapefile.point.ShapefilePointLayer;
import au.gov.ga.worldwind.common.layers.shapefile.point.Style;
import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.common.util.Setupable;

public class ShapefileMarkerLayer extends ShapefilePointLayer implements SelectListener, Setupable
{
	private MarkerLayer markerLayer;
	private List<Marker> markers = new ArrayList<Marker>();
	private UrlMarker pickedMarker;
	private Material backupMaterial;
	private Material highlightMaterial;

	public ShapefileMarkerLayer(Element domElement, AVList params)
	{
		super(domElement, params);
		markerLayer = new MarkerLayer();
		highlightMaterial = new Material(Color.white);
		setPickEnabled(true);
	}

	@Override
	protected void load(URL url)
	{
		markers.clear();
		super.load(url);
		markerLayer.setMarkers(markers);
	}
	
	@Override
	public void setPickEnabled(boolean pickable)
	{
		super.setPickEnabled(pickable);
		markerLayer.setPickEnabled(pickable);
	}

	@Override
	protected void addPoint(Position position, AVList attrib, Style style, String text, String link)
	{
		MarkerAttributes attributes = new BasicMarkerAttributes();
		style.setPropertiesFromAttributes(context, attrib, attributes);
		UrlMarker marker = new UrlMarker(position, attributes);
		marker.setUrl(link);
		markers.add(marker);
	}

	@Override
	protected void doPick(DrawContext dc, Point point)
	{
		markerLayer.pick(dc, point);
	}

	@Override
	protected void doPreRender(DrawContext dc)
	{
		markerLayer.preRender(dc);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		markerLayer.render(dc);
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		wwd.addSelectListener(this);
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
