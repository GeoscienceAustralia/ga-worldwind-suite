package au.gov.ga.worldwind.animator.layers.misc;

import static au.gov.ga.worldwind.animator.util.Util.isBlank;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;

import javax.xml.xpath.XPath;

import nasa.worldwind.layers.FogLayer;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.util.AVKeyMore;

/**
 * A factory class that can be used to create instances of a {@link FogLayer} 
 * from a layer definition file
 * <p/>
 * Implemented as a separate factory class to re-use the existing world wind 
 * fog layer.
 */
public class FogLayerFactory
{

	public static final String LAYER_TYPE = "FogLayer";
	
	/**
	 * Create a new {@link FogLayer} from the provided parameters.
	 * <p/>
	 * Optional parameters include:
	 * <ul>
	 * 	<li>AVKeyMore.DISPLAY_NAME
	 * 	<li>AVKeyMore.FOG_NEAR_FACTOR
	 * 	<li>AVKeyMore.FOG_FAR_FACTOR
	 * 	<li>AVKeyMore.FOG_COLOR
	 * </ul>
	 */
	public static FogLayer createFromParams(AVList params)
	{
		FogLayer result = new FogLayer();
		
		String s = params.getStringValue(AVKeyMore.DISPLAY_NAME);
		if (!isBlank(s))
		{
			result.setName(s);
		}
		
		Float f = (Float)params.getValue(AVKeyMore.FOG_NEAR_FACTOR);
		if (f != null)
		{
			result.setNearFactor(f);
		}
		
		f = (Float)params.getValue(AVKeyMore.FOG_FAR_FACTOR);
		if (f != null)
		{
			result.setFarFactor(f);
		}
		
		Color c = (Color)params.getValue(AVKeyMore.FOG_COLOR);
		if (c != null)
		{
			result.setColor(c);
		}
		
		return result;
	}
	
	public static FogLayer createFromDefinition(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}
		
		AbstractLayer.getLayerConfigParams(domElement, params);
		
		checkAndSetFloatParam(domElement, params, AVKeyMore.FOG_NEAR_FACTOR, "NearFactor", null);
		checkAndSetFloatParam(domElement, params, AVKeyMore.FOG_FAR_FACTOR, "FarFactor", null);
		WWXML.checkAndSetColorParam(domElement, params, AVKeyMore.FOG_COLOR, "FogColor", null);
		
		return createFromParams(params);
	}
	
	private static void checkAndSetFloatParam(Element context, AVList params, String paramKey, String paramName, XPath xpath)
	{
		if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            Double d = WWXML.getDouble(context, paramName, xpath);
            if (d != null)
                params.setValue(paramKey, d.floatValue());
        }
	}
	
}
