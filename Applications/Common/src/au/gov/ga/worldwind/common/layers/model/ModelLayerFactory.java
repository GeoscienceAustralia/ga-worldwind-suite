package au.gov.ga.worldwind.common.layers.model;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.data.DataLayerFactory;
import au.gov.ga.worldwind.common.layers.model.gocad.GocadModelProvider;
import au.gov.ga.worldwind.common.util.AVKeyMore;

/**
 * Factory used for creating {@link ModelLayer} instances an XML definition.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ModelLayerFactory
{
	/**
	 * Create a new {@link ModelLayer} from an XML definition.
	 * 
	 * @return New {@link ModelLayer}.
	 */
	public static ModelLayer createModelLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		ModelLayer layer = new BasicModelLayer(params);
		DataLayerFactory.setLayerParams(layer, params);
		return layer;
	}

	/**
	 * Fill the params with the values in the {@link ModelLayer} specific XML
	 * elements.
	 */
	public static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);
		WWXML.checkAndSetLongParam(domElement, params, AVKey.EXPIRY_TIME, "ExpiryTime", xpath);
		WWXML.checkAndSetDateTimeParam(domElement, params, AVKey.EXPIRY_TIME, "LastUpdate",
				DataLayerFactory.DATE_TIME_PATTERN, xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.DATA_CACHE_NAME, "DataCacheName", xpath);

		WWXML.checkAndSetColorParam(domElement, params, AVKey.COLOR, "Color", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.LINE_WIDTH, "LineWidth", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.POINT_SIZE, "PointSize", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.MINIMUM_DISTANCE, "MinimumDistance", xpath);

		setupModelProvider(domElement, xpath, params);

		return params;
	}

	/**
	 * Adds a {@link ModelProvider} to params matching the 'DataFormat' XML
	 * element.
	 */
	protected static void setupModelProvider(Element domElement, XPath xpath, AVList params)
	{
		String format = WWXML.getText(domElement, "DataFormat", xpath);

		if ("GOCAD".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new GocadModelProvider());
		}
		else
		{
			throw new IllegalArgumentException("Could not find model provider for DataFormat: " + format);
		}
	}
}
