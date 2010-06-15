package au.gov.ga.worldwind.panels.layers;

import java.awt.Frame;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.components.ImageDialog;
import au.gov.ga.worldwind.panels.dataset.AbstractCellRenderer;
import au.gov.ga.worldwind.util.DefaultLauncher;
import au.gov.ga.worldwind.util.Icons;

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
	protected boolean isInfoRow(INode value)
	{
		return value.getInfoURL() != null;
	}

	@Override
	protected boolean isLegendRow(ILayerNode value)
	{
		return value.getLegendURL() != null;
	}

	@Override
	protected boolean isQueryRow(ILayerNode value)
	{
		return value.getQueryURL() != null;
	}

	/*@Override
	protected String getLinkLabelToolTipText(Object value)
	{
		INode node = getValue(value);
		if (node != null && node.getInfoURL() != null)
			return node.getInfoURL().toExternalForm();
		return null;
	}*/

	@Override
	protected void setupLabel(DefaultTreeCellRenderer label, INode value)
	{
		ILayerNode layer = getLayerValue(value);
		if (layer != null)
		{
			if (layer.hasError())
			{
				String message = layer.getError().getLocalizedMessage();
				if (message == null || message.length() == 0)
					message = layer.getError().toString();
				label.setText(label.getText() + " - " + message);
				label.setIcon(Icons.error.getIcon());
			}
			else if (layer.getOpacity() != 0d && layer.getOpacity() != 1d)
			{
				label.setText(label.getText() + " (" + (int) Math.round(layer.getOpacity() * 100d)
						+ "%)");
			}
		}
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
	protected void infoClicked(int row)
	{
		TreePath path = getTree().getPathForRow(row);
		if (path != null)
		{
			INode node = getValue(path.getLastPathComponent());
			if (node != null && node.getInfoURL() != null)
				DefaultLauncher.openURL(node.getInfoURL());
		}
	}

	@Override
	protected void legendClicked(int row)
	{
		TreePath path = getTree().getPathForRow(row);
		if (path != null)
		{
			INode node = getValue(path.getLastPathComponent());
			ILayerNode layer = getLayerValue(node);
			if (layer != null && layer.getLegendURL() != null)
			{
				try
				{
					//TODO create a loading dialog while downloading legend image
					//TODO cache downloaded legend?
					Image image = ImageIO.read(layer.getLegendURL());
					//TODO check that returned value is an instanceof Frame (or implement a better way)
					Frame frame = (Frame) SwingUtilities.getWindowAncestor(getTree());
					ImageDialog dialog =
							new ImageDialog(frame, layer.getName() + " legend", false, image,
									Icons.legend.getIcon());
					dialog.setLocationRelativeTo(frame);
					dialog.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void queryClicked(int row)
	{
		// TODO Auto-generated method stub
	}
}
