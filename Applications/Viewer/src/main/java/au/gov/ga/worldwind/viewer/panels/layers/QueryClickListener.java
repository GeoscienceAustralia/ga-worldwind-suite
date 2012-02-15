package au.gov.ga.worldwind.viewer.panels.layers;

import java.net.URL;

/**
 * Listener interface for layer query events.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface QueryClickListener
{
	/**
	 * Called when a layer query is initiated.
	 * 
	 * @param url
	 */
	void queryURLClicked(URL url);
}
