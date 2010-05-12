package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

import javax.swing.ImageIcon;

import au.gov.ga.worldwind.components.lazytree.ILoadingNode;

public interface IIconItem extends ILoadingNode
{
	public boolean isIconLoaded();

	public void loadIcon(Runnable afterLoad);

	public ImageIcon getIcon();

	public URL getIconURL();

	public void setIconURL(URL iconURL);
}
