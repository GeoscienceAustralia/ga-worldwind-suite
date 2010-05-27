package au.gov.ga.worldwind.panels.layers;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.panels.dataset.AbstractCellRenderer;
import au.gov.ga.worldwind.util.DefaultLauncher;

public class LayerCellRenderer extends AbstractCellRenderer<INode, ILayerNode>
{
	@Override
	protected AbstractButton createButton()
	{
		return new JCheckBox();
	}

	@Override
	protected void validateTree(JTree tree)
	{
		if (!(tree instanceof LayerTree))
			throw new IllegalArgumentException("Tree must be a LayerTree");
	}

	@Override
	protected INode getValue(Object value)
	{
		if (value != null && value instanceof INode)
			return (INode) value;
		return null;
	}

	@Override
	protected ILayerNode getLayerValue(INode value)
	{
		if (value != null && value instanceof ILayerNode)
			return (ILayerNode) value;
		return null;
	}

	@Override
	protected boolean isURLRow(INode value)
	{
		return value.getInfoURL() != null;
	}

	@Override
	protected String getLinkLabelToolTipText(Object value)
	{
		INode node = getValue(value);
		if (node != null && node.getInfoURL() != null)
			return node.getInfoURL().toExternalForm();
		return null;
	}

	@Override
	protected void setupButton(AbstractButton button, ILayerNode value, boolean mouseInsideButton,
			boolean rollover, boolean down)
	{
		button.getModel().setRollover(rollover);
		button.getModel().setPressed(down);
		button.getModel().setArmed(down);
		button.setSelected(value.isEnabled());
	}

	@Override
	protected void buttonPressed(int row)
	{
		TreePath path = getTree().getPathForRow(row);
		if (path != null)
		{
			ILayerNode layer = getLayerValue(getValue(path.getLastPathComponent()));
			if (layer != null)
			{
				LayerTreeModel model = (LayerTreeModel) getTree().getModel();
				model.setEnabled(layer, !model.isEnabled(layer));
			}
		}
	}

	@Override
	protected void linkClicked(int row)
	{
		TreePath path = getTree().getPathForRow(row);
		if (path != null)
		{
			INode node = getValue(path.getLastPathComponent());
			if (node != null && node.getInfoURL() != null)
				DefaultLauncher.openURL(node.getInfoURL());
		}
	}
}
