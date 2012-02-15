package au.gov.ga.worldwind.viewer.panels.layers;

import java.net.URL;

/**
 * Tree node that acts as a folder for layer nodes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FolderNode extends AbstractNode
{
	public FolderNode(String name, URL infoURL, URL iconURL, boolean expanded)
	{
		super(name, infoURL, iconURL, expanded);
	}
}