package au.gov.ga.worldwind.viewer.theme;

import au.gov.ga.worldwind.viewer.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.viewer.panels.layers.ThemeLayersPanel;

/**
 * {@link ILayerDefinition} subclass for layers defined in the theme file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ThemeLayer extends ILayerDefinition
{
	/**
	 * @return Is this layer visible to the user in the {@link ThemeLayersPanel}
	 *         ?
	 */
	boolean isVisible();
}
