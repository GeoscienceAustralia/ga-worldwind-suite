package au.gov.ga.worldwind.common.layers.borehole;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.borehole.providers.ShapefileBoreholeProvider;
import au.gov.ga.worldwind.common.layers.data.DataLayerFactory;
import au.gov.ga.worldwind.common.layers.styled.StyleAndAttributeFactory;
import au.gov.ga.worldwind.common.util.AVKeyMore;

/**
 * Helper class for the creation of {@link BoreholeLayer}s. Contains XML parsing
 * functionality, as well as factory methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BoreholeLayerFactory
{
	protected static final String DATE_TIME_PATTERN = "dd MM yyyy HH:mm:ss z";

	/**
	 * Create a new {@link BoreholeLayer} from an XML definition.
	 * 
	 * @return New {@link BoreholeLayer}.
	 */
	public static BoreholeLayer createBoreholeLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		BoreholeLayer layer = new BasicBoreholeLayer(params);
		DataLayerFactory.setLayerParams(layer, params);
		return layer;
	}

	/**
	 * Fill the params with the values in the {@link BoreholeLayer} specific XML
	 * elements.
	 */
	public static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);
		WWXML.checkAndSetLongParam(domElement, params, AVKey.EXPIRY_TIME, "ExpiryTime", xpath);
		WWXML.checkAndSetDateTimeParam(domElement, params, AVKey.EXPIRY_TIME, "LastUpdate", DATE_TIME_PATTERN, xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.DATA_CACHE_NAME, "DataCacheName", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.POINT_TYPE, "PointType", xpath);

		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.BOREHOLE_UNIQUE_IDENTIFIER_ATTRIBUTE,
				"UniqueBoreholeIdentifier", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.BOREHOLE_SAMPLE_DEPTH_FROM_ATTRIBUTE,
				"SampleDepthAttributes/@from", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.BOREHOLE_SAMPLE_DEPTH_TO_ATTRIBUTE,
				"SampleDepthAttributes/@to", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.BOREHOLE_LINE_WIDTH, "LineWidth", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.BOREHOLE_MINIMUM_DISTANCE, "MinimumDistance", xpath);

		setupBoreholeProvider(domElement, xpath, params);

		Element styles = WWXML.getElement(domElement, "BoreholeStyles", xpath);
		StyleAndAttributeFactory.addStyles(styles, xpath, AVKeyMore.BOREHOLE_STYLES, params);

		Element attributes = WWXML.getElement(domElement, "BoreholeAttributes", xpath);
		StyleAndAttributeFactory.addAttributes(attributes, xpath, AVKeyMore.BOREHOLE_ATTRIBUTES, params);

		Element sampleStyles = WWXML.getElement(domElement, "SampleStyles", xpath);
		StyleAndAttributeFactory.addStyles(sampleStyles, xpath, AVKeyMore.BOREHOLE_SAMPLE_STYLES, params);

		Element sampleAttributes = WWXML.getElement(domElement, "SampleAttributes", xpath);
		StyleAndAttributeFactory.addAttributes(sampleAttributes, xpath, AVKeyMore.BOREHOLE_SAMPLE_ATTRIBUTES, params);

		return params;
	}

	/**
	 * Adds a {@link BoreholeProvider} to params matching the 'DataFormat' XML
	 * element.
	 */
	protected static void setupBoreholeProvider(Element domElement, XPath xpath, AVList params)
	{
		String format = WWXML.getText(domElement, "DataFormat", xpath);

		if ("Shapefile".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.BOREHOLE_PROVIDER, new ShapefileBoreholeProvider());
		}
		else
		{
			throw new IllegalArgumentException("Could not find point provider for DataFormat: " + format);
		}
	}
}
