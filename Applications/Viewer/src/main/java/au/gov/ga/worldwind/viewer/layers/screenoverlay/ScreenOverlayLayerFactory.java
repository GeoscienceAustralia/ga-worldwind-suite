package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import static gov.nasa.worldwind.layers.AbstractLayer.getLayerConfigParams;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;


/**
 * A factory class for creating instances of {@link ScreenOverlayLayer}s
 * from xml documents etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ScreenOverlayLayerFactory
{
	/**
	 * Create and return a new {@link ScreenOverlayLayer} from the provided XML document and parameters.
	 * <p/>
	 * Where the XML document and params define the same initialisation parameter, the one in the
	 * params list will be used. 
	 * <p/>
	 * If the provided XML document is <code>null</code>, the provided params will be used to fully initialise
	 * the {@link ScreenOverlayLayer}.
	 */
	public static ScreenOverlayLayer createScreenOverlayLayer(Element domElement, AVList params)
	{
		Validate.isTrue(domElement != null || params != null, "Either an XML document or params must be provided.");
		
		if (domElement == null)
		{
			return new ScreenOverlayLayer(params);
		}
		
		if (params == null)
		{
			params = new AVListImpl();
		}
		
		params = getLayerConfigParams(domElement, params);
		params = getScreenOverlayParams(domElement, params);
		
		return new ScreenOverlayLayer(params);
	}

	private static AVList getScreenOverlayParams(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		XPath xpath = WWXML.makeXPath();

		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.URL, "URL", xpath);
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.OVERLAY_CONTENT, "Content", xpath);
		
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.POSITION, "Position", xpath);
		
		XMLUtil.checkAndSetIntegerParam(domElement, params, ScreenOverlayKeys.BORDER_WIDTH, "BorderWidth", xpath);
		XMLUtil.checkAndSetColorParam(domElement, params, ScreenOverlayKeys.BORDER_COLOR, "BorderColor", xpath);
		XMLUtil.checkAndSetBooleanParam(domElement, params, ScreenOverlayKeys.DRAW_BORDER, "DrawBorder", xpath);
		
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.MIN_HEIGHT, "MinHeight", xpath);
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.MAX_HEIGHT, "MaxHeight", xpath);
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.MIN_WIDTH, "MinWidth", xpath);
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.MAX_WIDTH, "MaxWidth", xpath);

		return params;
	}
}
