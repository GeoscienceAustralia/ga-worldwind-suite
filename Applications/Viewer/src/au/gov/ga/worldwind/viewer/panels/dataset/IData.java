package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;

import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;

public interface IData extends IIconItem
{
	String getName();
	URL getInfoURL();
	boolean isBase();
	MutableTreeNode createMutableTreeNode(LazyTreeModel model);
}
