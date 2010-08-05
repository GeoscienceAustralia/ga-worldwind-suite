package au.gov.ga.worldwind.layers.shapefile.surfaceshape;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.examples.util.ShapefileLoader;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.SurfaceShapeLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.util.AVKeyMore;

public class SurfaceShapeShapefileLayerFactory
{
	public static SurfaceShapeLayer createLayer(AVList params)
	{
		String s = params.getStringValue(AVKey.URL);

		if (s == null)
		{
			String message = "Shapefile URL not specified";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		URL context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		URL url;
		try
		{
			url = new URL(context, s);
		}
		catch (MalformedURLException e)
		{
			String message = "Shapefile URL malformed";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Shapefile shapefile = new Shapefile(url);
		Layer layer = ShapefileLoader.makeLayerFromShapefile(shapefile);

		if (layer instanceof SurfaceShapeLayer)
		{
			SurfaceShapeLayer ssl = (SurfaceShapeLayer) layer;

			s = params.getStringValue(AVKey.DISPLAY_NAME);
			if (s != null)
				ssl.setName(s);

			Long l = (Long) params.getValue(AVKey.EXPIRY_TIME);
			if (l != null)
				ssl.setExpiryTime(l);

			ShapeAttributes attributes = new BasicShapeAttributes();
			setAttributesFromParams(attributes, params);

			for (Renderable renderable : ssl.getRenderables())
			{
				if (renderable instanceof SurfaceShape)
					((SurfaceShape) renderable).setAttributes(attributes);
			}
			
			ssl.setPickEnabled(false);

			return ssl;
		}

		return null;
	}

	public static SurfaceShapeLayer createLayer(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		AbstractLayer.getLayerConfigParams(domElement, params);
		getAttributeParams(domElement, params);

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);

		return createLayer(params);
	}

	protected static void setAttributesFromParams(ShapeAttributes attributes, AVList params)
	{
		Boolean b = (Boolean) params.getValue(ShapeAttributeKey.DRAW_INTERIOR);
		if (b != null)
			attributes.setDrawInterior(b);

		b = (Boolean) params.getValue(ShapeAttributeKey.DRAW_OUTLINE);
		if (b != null)
			attributes.setDrawOutline(b);

		b = (Boolean) params.getValue(ShapeAttributeKey.ANTIALIASING);
		if (b != null)
			attributes.setEnableAntialiasing(b);

		Color c = (Color) params.getValue(ShapeAttributeKey.INTERIOR_COLOR);
		if (c != null)
			attributes.setInteriorMaterial(new Material(c));

		c = (Color) params.getValue(ShapeAttributeKey.OUTLINE_COLOR);
		if (c != null)
			attributes.setOutlineMaterial(new Material(c));

		Double d = (Double) params.getValue(ShapeAttributeKey.INTERIOR_OPACITY);
		if (d != null)
			attributes.setInteriorOpacity(d);

		d = (Double) params.getValue(ShapeAttributeKey.OUTLINE_OPACITY);
		if (d != null)
			attributes.setOutlineOpacity(d);

		d = (Double) params.getValue(ShapeAttributeKey.OUTLINE_WIDTH);
		if (d != null)
			attributes.setOutlineWidth(d);

		Integer i = (Integer) params.getValue(ShapeAttributeKey.STIPPLE_FACTOR);
		if (i != null)
			attributes.setOutlineStippleFactor(i);

		i = (Integer) params.getValue(ShapeAttributeKey.STIPPLE_PATTERN);
		if (i != null)
			attributes.setOutlineStipplePattern(i.shortValue());
	}

	protected static AVList getAttributeParams(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetBooleanParam(domElement, params, ShapeAttributeKey.DRAW_INTERIOR,
				"DrawInterior", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, ShapeAttributeKey.DRAW_OUTLINE,
				"DrawOutline", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, ShapeAttributeKey.ANTIALIASING,
				"Antialiasing", xpath);
		WWXML.checkAndSetColorParam(domElement, params, ShapeAttributeKey.INTERIOR_COLOR,
				"InteriorColor", xpath);
		WWXML.checkAndSetColorParam(domElement, params, ShapeAttributeKey.OUTLINE_COLOR,
				"OutlineColor", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, ShapeAttributeKey.INTERIOR_OPACITY,
				"InteriorOpacity", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, ShapeAttributeKey.OUTLINE_OPACITY,
				"OutlineOpacity", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, ShapeAttributeKey.OUTLINE_WIDTH,
				"OutlineWidth", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, ShapeAttributeKey.STIPPLE_FACTOR,
				"StippleFactor", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, ShapeAttributeKey.STIPPLE_PATTERN,
				"StipplePattern", xpath);

		return params;
	}
}
