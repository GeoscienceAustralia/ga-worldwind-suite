package gov.nasa.worldwind.ogc.custom.kml.impl;

import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.impl.KMLModelPlacemarkImpl;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.render.DrawContext;

public class CustomKMLModelPlacemarkImpl extends KMLModelPlacemarkImpl
{
	public CustomKMLModelPlacemarkImpl(KMLTraversalContext tc, KMLPlacemark placemark, KMLAbstractGeometry geom)
	{
		super(tc, placemark, geom);
		
		System.out.println(model.getLink().getHref());
	}
	
	@Override
	public void preRender(KMLTraversalContext tc, DrawContext dc)
	{
		super.preRender(tc, dc);
	}
}
