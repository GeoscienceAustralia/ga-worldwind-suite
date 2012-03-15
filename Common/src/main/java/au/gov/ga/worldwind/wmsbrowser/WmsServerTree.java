/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.wmsbrowser;

import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.awt.Component;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import au.gov.ga.worldwind.common.ui.lazytree.ErrorNode;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTree;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.LoadingNode;
import au.gov.ga.worldwind.common.util.Icons;

/**
 * An extension of the {@link LazyTree} class customised for
 * use in the WMS browser panel
 * 
 * @author James Navin (james.navin@ga.gov.au)
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
		
		setCellRenderer(new WmsServerTreeCellRenderer());
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
	
	/**
	 * A {@link TreeCellRenderer} that decorates the WMS tree with icons appropriate for servers/layers etc.
	 */
	private static class WmsServerTreeCellRenderer extends DefaultTreeCellRenderer
	{
		private ImageIcon loadingIcon;
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			if (loadingIcon == null)
			{
				loadingIcon = Icons.newLoadingIcon();
				loadingIcon.setImageObserver(tree);
			}
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			if (value instanceof ErrorNode)
			{
				setIcon(Icons.error.getIcon());
			}
			else if (value instanceof LoadingNode)
			{
				setIcon(loadingIcon);
			}
			else if (value instanceof DefaultMutableTreeNode)
			{
				Object nodeObject = ((DefaultMutableTreeNode)value).getUserObject();
				
				if (nodeObject instanceof WmsServerTreeObject)
				{
					setIcon(Icons.wmsbrowser.getIcon());
				}
				if (nodeObject instanceof WMSLayerInfo)
				{
					setIcon(Icons.image.getIcon());
				}
			}
			
			return this;
		}
	}
}
