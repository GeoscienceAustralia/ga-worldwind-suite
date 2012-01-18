package au.gov.ga.worldwind.common.layers.curtain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.AVKeyMore;

/**
 * Extension of the {@link DataConfigurationUtils} class that adds some extra
 * XML parsing for curtain layer definitions.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainDataConfigurationUtils extends DataConfigurationUtils
{
	public static AVList getLevelSetConfigParams(Element domElement, AVList params)
	{
		params = DataConfigurationUtils.getLevelSetConfigParams(domElement, params);

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.FULL_WIDTH, "FullSize/Dimension/@width", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.FULL_HEIGHT, "FullSize/Dimension/@height", xpath);

		return params;
	}
}
