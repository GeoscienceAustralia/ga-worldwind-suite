package au.gov.ga.worldwind.viewer.panels.layers;

import java.net.URL;

public class FolderNode extends AbstractNode
{
	public FolderNode(String name, URL infoURL, URL iconURL, boolean expanded)
	{
		super(name, infoURL, iconURL, expanded);
	}

	public FolderNode(INode node)
	{
		this(node.getName(), node.getInfoURL(), node.getIconURL(), node.isExpanded());
	}
}