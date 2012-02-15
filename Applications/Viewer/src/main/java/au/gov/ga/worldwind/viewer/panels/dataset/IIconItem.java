package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;

import javax.swing.ImageIcon;

import au.gov.ga.worldwind.common.ui.lazytree.ILoadingNode;

/**
 * Represents a tree node that has an icon. The icon can be loaded lazily.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IIconItem extends ILoadingNode
{
	/**
	 * @return Has the icon associated with this node been loaded?
	 */
	boolean isIconLoaded();

	/**
	 * Load this node's icon.
	 * 
	 * @param afterLoad
	 *            Runnable to run after the icon has been loaded.
	 */
	void loadIcon(Runnable afterLoad);

	/**
	 * @return This node's icon.
	 */
	ImageIcon getIcon();

	/**
	 * @return The URL pointing to the icon.
	 */
	URL getIconURL();

	/**
	 * Set the icon's URL.
	 * 
	 * @param iconURL
	 */
	void setIconURL(URL iconURL);
}
