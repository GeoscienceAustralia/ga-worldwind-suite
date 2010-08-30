package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public interface IData extends IIconItem
{
	String getName();
	URL getInfoURL();
	boolean isBase();
	MutableTreeNode createMutableTreeNode(DefaultTreeModel model);
}
