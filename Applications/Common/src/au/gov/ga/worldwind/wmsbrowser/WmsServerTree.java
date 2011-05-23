package au.gov.ga.worldwind.wmsbrowser;

import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.awt.Point;

import javax.swing.tree.DefaultMutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.LazyTree;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;

/**
 * An extension of the {@link LazyTree} class customised for
 * use in the WMS browser panel
 */
class WmsServerTree extends LazyTree
{
	private static final long serialVersionUID = 20101118L;

	public WmsServerTree(LazyTreeModel treeModel)
	{
		super(treeModel);
		setRootVisible(false);
		setDragEnabled(false);
		setEditable(false);
		setShowsRootHandles(true);
	}

	@Override
	public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		if (value instanceof DefaultMutableTreeNode)
		{
			Object nodeObject = ((DefaultMutableTreeNode)value).getUserObject();
			
			if (nodeObject instanceof WmsServerTreeObject)
			{
				return ((WmsServerTreeObject)nodeObject).getWmsServer().getName();
			}
			if (nodeObject instanceof WMSLayerInfo)
			{
				return ((WMSLayerInfo)nodeObject).getTitle();
			}
		}
		
		return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
	}
	
	public boolean isPointOnTreeElement(Point p)
	{
		return getPathForLocation((int)p.getX(), (int)p.getY()) != null;
	}

	public void selectTreeElementAtPoint(Point p)
	{
		getSelectionModel().setSelectionPath(getPathForLocation((int)p.getX(), (int)p.getY()));
	}
}