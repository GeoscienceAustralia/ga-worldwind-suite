package au.gov.ga.worldwind.dataset;

import java.net.URL;

import javax.swing.ImageIcon;

public interface IIconItem
{
	public boolean isIconLoaded();

	/**
	 * Load the icon
	 * 
	 * @param afterLoad
	 *            Runs after load
	 * @return True if a load was initiated, false if a load is in progress or
	 *         load is not required
	 */
	public boolean loadIcon(Runnable afterLoad);

	public ImageIcon getIcon();
	
	public URL getIconURL();

	public void setIconURL(URL iconURL);
}
