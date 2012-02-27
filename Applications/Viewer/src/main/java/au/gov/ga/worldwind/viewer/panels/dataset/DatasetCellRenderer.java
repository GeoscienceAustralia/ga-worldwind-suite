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
package au.gov.ga.worldwind.viewer.panels.dataset;

import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTreeModel;

/**
 * Concrete subclass of the {@link AbstractCellRenderer} for the Dataset tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetCellRenderer extends AbstractCellRenderer<IData, ILayerDefinition>
{
	private LayerTreeModel layerTreeModel;

	public void setLayerTreeModel(LayerTreeModel layerTreeModel)
	{
		this.layerTreeModel = layerTreeModel;
	}

	@Override
	protected AbstractButton createButton()
	{
		JButton button = new JButton();

		int height = Icons.add.getIcon().getIconHeight();
		Dimension size = new Dimension(height + 2, height + 2);
		button.setPreferredSize(size);
		button.setMinimumSize(size);

		return button;
	}

	@Override
	protected void validateTree(JTree tree)
	{
		if (!(tree instanceof DatasetTree))
			throw new IllegalArgumentException("Tree must be a DatasetTree");
	}

	@Override
	protected IData getValue(Object value)
	{
		if (value != null && value instanceof DefaultMutableTreeNode)
		{
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			if (userObject != null && userObject instanceof IData)
				return (IData) userObject;
		}
		return null;
	}

	@Override
	protected ILayerDefinition getLayerValue(IData value)
	{
		if (value != null && value instanceof ILayerDefinition)
			return (ILayerDefinition) value;
		return null;
	}

	@Override
	protected boolean isInfoRow(IData value)
	{
		return value.getInfoURL() != null;
	}

	@Override
	protected boolean isLegendRow(ILayerDefinition value)
	{
		return false;
	}

	@Override
	protected boolean isQueryRow(ILayerDefinition value)
	{
		return false;
	}

	/*@Override
	protected String getLinkLabelToolTipText(Object value)
	{
		IData data = getValue(value);
		if (data != null && data.getInfoURL() != null)
			return data.getInfoURL().toExternalForm();
		return null;
	}*/

	@Override
	protected void setupLabel(DefaultTreeCellRenderer label, IData value)
	{
	}

	@Override
	protected void setupButton(AbstractButton button, ILayerDefinition value, boolean mouseInsideButton,
			boolean rollover, boolean down)
	{
		if (layerTreeModel == null)
			return;

		if (layerTreeModel.containsLayer(value))
			button.setIcon(Icons.remove.getIcon());
		else
			button.setIcon(Icons.add.getIcon());

		button.getModel().setRollover(rollover);
		button.getModel().setSelected(down);
	}

	@Override
	protected void buttonPressed(int row)
	{
		if (layerTreeModel == null)
			return;

		TreePath path = getTree().getPathForRow(row);
		if (path != null)
		{
			ILayerDefinition layer = getLayerValue(getValue(path.getLastPathComponent()));
			if (layer != null)
			{
				if (layerTreeModel.containsLayer(layer))
					layerTreeModel.removeLayer(layer);
				else
					layerTreeModel.addLayer(layer, path.getPath());
				getTree().repaint();
			}
		}
	}

	@Override
	protected void infoClicked(int row)
	{
		TreePath path = getTree().getPathForRow(row);
		if (path != null)
		{
			IData data = getValue(path.getLastPathComponent());
			if (data != null && data.getInfoURL() != null)
			{
				DefaultLauncher.openURL(data.getInfoURL());
			}
		}
	}

	@Override
	protected void legendClicked(int row)
	{
	}

	@Override
	protected void queryClicked(int row)
	{
	}
}
