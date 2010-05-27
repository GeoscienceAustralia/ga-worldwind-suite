package au.gov.ga.worldwind.panels.dataset;

import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.panels.layers.LayerTreeModel;
import au.gov.ga.worldwind.util.DefaultLauncher;
import au.gov.ga.worldwind.util.Icons;

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
	protected boolean isURLRow(IData value)
	{
		return value.getInfoURL() != null;
	}

	@Override
	protected String getLinkLabelToolTipText(Object value)
	{
		IData data = getValue(value);
		if (data != null && data.getInfoURL() != null)
			return data.getInfoURL().toExternalForm();
		return null;
	}

	@Override
	protected void setupButton(AbstractButton button, ILayerDefinition value,
			boolean mouseInsideButton, boolean rollover, boolean down)
	{
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
	protected void linkClicked(int row)
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
}