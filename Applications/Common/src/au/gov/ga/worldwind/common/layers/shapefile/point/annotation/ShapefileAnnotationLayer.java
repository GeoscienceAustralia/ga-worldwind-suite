package au.gov.ga.worldwind.common.layers.shapefile.point.annotation;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;

import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.shapefile.point.ShapefilePointLayer;
import au.gov.ga.worldwind.common.layers.shapefile.point.Style;
import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.common.util.Setupable;

public class ShapefileAnnotationLayer extends ShapefilePointLayer implements SelectListener,
		Setupable
{
	protected AnnotationLayer annotationLayer;
	protected GlobeAnnotation pickedAnnotation;

	public ShapefileAnnotationLayer(Element domElement, AVList params)
	{
		super(domElement, params);
		init();
	}

	protected void init()
	{
		annotationLayer = new AnnotationLayer();
		setPickEnabled(true);
	}

	@Override
	public void setPickEnabled(boolean pickable)
	{
		super.setPickEnabled(pickable);
		annotationLayer.setPickEnabled(pickable);
	}

	@Override
	protected void addPoint(Position position, AVList attrib, Style style, String text, String link)
	{
		GlobeAnnotation annotation = new GlobeAnnotation(text, position);
		annotation.setValue(AVKey.URL, link);
		AnnotationAttributes attributes = new AnnotationAttributes();
		style.setPropertiesFromAttributes(context, attrib, attributes, annotation);
		annotation.setAttributes(attributes);
		annotationLayer.addAnnotation(annotation);
	}

	@Override
	protected void doPick(DrawContext dc, Point point)
	{
		annotationLayer.pick(dc, point);
	}

	@Override
	protected void doPreRender(DrawContext dc)
	{
		annotationLayer.preRender(dc);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		annotationLayer.render(dc);
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

	@Override
	public void setup(WorldWindow wwd)
	{
		wwd.addSelectListener(this);
	}
}
