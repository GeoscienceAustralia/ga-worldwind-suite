package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;

/**
 * Interface representing a layer definition.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerDefinition extends IData
{
	/**
	 * @return URL pointing at the layer definition file.
	 */
	URL getLayerURL();

	/**
	 * @return Should this layer be added to the layers panel by default? This
	 *         is done when loading the Viewer for the first time (and the
	 *         layers persistance file does not yet exist).
	 */
	boolean isDefault();

	/**
	 * @return If this layer is added by default, should it be added in the
	 *         enabled state?
	 * @see ILayerDefinition#isDefault()
	 */
	boolean isEnabled();
}
