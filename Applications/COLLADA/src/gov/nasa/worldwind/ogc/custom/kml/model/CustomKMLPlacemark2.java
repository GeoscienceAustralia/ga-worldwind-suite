package gov.nasa.worldwind.ogc.custom.kml.model;

import gov.nasa.worldwind.ogc.custom.kml.impl.CustomKMLModelPlacemarkImpl;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;

public class CustomKMLPlacemark2 extends KMLPlacemark
{
	public CustomKMLPlacemark2(String namespaceURI)
	{
		super(namespaceURI);
	}

	@Override
	protected KMLRenderable selectModelRenderable(KMLTraversalContext tc, KMLAbstractGeometry geom)
	{
		return new CustomKMLModelPlacemarkImpl(tc, this, geom);
	}
}
