package au.gov.ga.worldwind.common.layers.point.types;

import java.net.MalformedURLException;
import java.net.URL;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import au.gov.ga.worldwind.common.layers.point.PointLayer;
import au.gov.ga.worldwind.common.layers.point.PointLayerHelper;
import au.gov.ga.worldwind.common.layers.point.PointProperties;
import au.gov.ga.worldwind.common.util.DefaultLauncher;

/**
 * {@link PointLayer} implementation which extends {@link AnnotationLayer} and
 * uses Annotations to represent points.
 * 
 * @author Michael de Hoog
 */
public class AnnotationPointLayer extends AnnotationLayer implements PointLayer, SelectListener
{
	private final PointLayerHelper helper;
	private GlobeAnnotation pickedAnnotation;

	public AnnotationPointLayer(PointLayerHelper helper)
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
		PointProperties properties = helper.getStyle(attributeValues);
		GlobeAnnotation annotation = new GlobeAnnotation(properties.text, position);
		annotation.setValue(AVKey.URL, properties.link);
		AnnotationAttributes attributes = new AnnotationAttributes();
		properties.style.setPropertiesFromAttributes(helper.getContext(), attributeValues,
				attributes, annotation);
		annotation.setAttributes(attributes);
		this.addAnnotation(annotation);
	}

	@Override
	public void loadComplete()
	{
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
		if (topPickedObject != null && topPickedObject.getObject() instanceof GlobeAnnotation)
		{
			if (pickedAnnotation != null)
			{
				highlight(pickedAnnotation, false);
			}

			pickedAnnotation = (GlobeAnnotation) topPickedObject.getObject();
			highlight(pickedAnnotation, true);

			if (e.getEventAction() == SelectEvent.LEFT_PRESS)
			{
				String link = pickedAnnotation.getStringValue(AVKey.URL);
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
		else if (pickedAnnotation != null)
		{
			highlight(pickedAnnotation, false);
			pickedAnnotation = null;
		}
	}

	protected void highlight(GlobeAnnotation annotation, boolean highlight)
	{
		annotation.getAttributes().setHighlighted(highlight);
	}
}
