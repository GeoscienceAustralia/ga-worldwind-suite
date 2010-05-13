package au.gov.ga.worldwind.panels.layers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.util.DefaultLauncher;
import au.gov.ga.worldwind.util.HSLColor;
import au.gov.ga.worldwind.util.Icons;

public class LayerCellRenderer extends JPanel implements TreeCellRenderer
{
	private LayerTree tree;
	private RendererMouseKeyListener mouseKeyListener = new RendererMouseKeyListener();
	private Map<Integer, Rectangle> urlRows = new HashMap<Integer, Rectangle>();

	private int mouseRow = -1, keyRow = -1, mouseButtonDownRow = -1, mouseLabelDownRow = -1,
			keyDownRow = -1;
	private int mouseX = -1, mouseY = -1;

	private JCheckBox button;
	private DefaultTreeCellRenderer label;

	private ImageIcon loadingIcon;

	public LayerCellRenderer()
	{
		super(new BorderLayout(3, 3));
		setOpaque(false);

		button = new JCheckBox();
		button.setOpaque(false);

		label = new DefaultTreeCellRenderer();
		label.setTextNonSelectionColor(Color.black);
		label.setTextSelectionColor(Color.black);
		Color backgroundSelection = label.getBackgroundSelectionColor();
		HSLColor hsl = new HSLColor(backgroundSelection);
		label.setBackgroundSelectionColor(hsl.adjustTone(80));
		label.setBorderSelectionColor(hsl.adjustTone(20));

		add(button, BorderLayout.WEST);
		add(label, BorderLayout.CENTER);

		loadingIcon = Icons.newLoadingIcon();
	}

	private void toggleLayer(int row)
	{
		TreePath path = tree.getSelectionPath();
		Object value = path.getLastPathComponent();
		if (value != null && value instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) value;
			tree.getModel().setEnabled(layer, !tree.getModel().isEnabled(layer));
		}
	}

	private void linkClicked(int row)
	{
		TreePath path = tree.getSelectionPath();
		Object value = path.getLastPathComponent();
		if (value != null && value instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) value;
			if (layer.getDescriptionURL() != null)
			{
				DefaultLauncher.openURL(layer.getDescriptionURL());
			}
		}
	}

	public Component getTreeCellRendererComponent(final JTree tree, Object value, boolean selected,
			boolean expanded, boolean leaf, final int row, boolean hasFocus)
	{
		if (!(tree instanceof LayerTree))
			throw new IllegalArgumentException("Tree must be a LayerTree");

		//tree has changed!
		if (this.tree != tree)
		{
			if (this.tree != null)
			{
				this.tree.removeMouseListener(mouseKeyListener);
				this.tree.removeMouseMotionListener(mouseKeyListener);
				this.tree.removeKeyListener(mouseKeyListener);
			}
			this.tree = (LayerTree) tree;
			tree.addMouseListener(mouseKeyListener);
			tree.addMouseMotionListener(mouseKeyListener);
			tree.addKeyListener(mouseKeyListener);

			loadingIcon.setImageObserver(tree);
		}

		//update the label
		label.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if (row < 0)
			return label;

		Component returnValue = label;
		boolean urlRow = false;

		if (value != null && value instanceof INode)
		{
			INode node = (INode) value;
			if (node.isIconLoaded())
			{
				label.setIcon(node.getIcon());
			}
			else
			{
				Runnable afterLoad = new Runnable()
				{
					public void run()
					{
						tree.repaint();
					}
				};
				node.loadIcon(afterLoad);
			}

			if (node.isLoading())
			{
				label.setIcon(loadingIcon);
			}

			if (node instanceof ILayerNode)
			{
				ILayerNode layer = (ILayerNode) node;

				if (layer.hasError())
				{
					//may be better to put the error on a separate line
					label.setIcon(Icons.error.getIcon());
					label.setText(label.getText() + " - " + layer.getError().getMessage());
				}
				else if (layer.getDescriptionURL() != null)
				{
					//make the label look like a link
					String text =
							"<html><font color=\"#0000CF\"><u>" + label.getText()
									+ "</u></font></html>";
					label.setText(text);
					urlRow = true;
				}

				//have to add it each time? it removes itself?
				add(label, BorderLayout.CENTER);
				returnValue = this;

				boolean mouseInsideButton =
						mouseRow >= 0 && button.getBounds().contains(mouseX, mouseY);
				boolean rollover =
						(mouseInsideButton && row == mouseRow && mouseButtonDownRow <= 0)
								|| (hasFocus);
				boolean down =
						(mouseInsideButton && row == mouseButtonDownRow)
								|| (hasFocus && row == keyDownRow);

				button.getModel().setRollover(rollover && row >= 0);
				button.getModel().setPressed(down && row >= 0);
				button.getModel().setArmed(down && row >= 0);

				button.setSelected(layer.isEnabled());
			}
		}

		if (urlRow)
		{
			Rectangle labelBounds = new Rectangle(returnValue.getPreferredSize());
			if (returnValue == this)
			{
				//ensure the label is in the correct position by forcing a layout
				returnValue.doLayout();
				labelBounds.x += label.getLocation().x;
			}
			if (label.getIcon() != null)
			{
				labelBounds.x += label.getIcon().getIconWidth() + label.getIconTextGap();
			}
			urlRows.put(row, labelBounds);
		}
		else
			urlRows.remove(row);

		return returnValue;
	}

	private class RendererMouseKeyListener extends MouseAdapter implements KeyListener
	{
		private int lastRow = -1;
		private int lastCursor = -1;
		private int lastCursorRow = -1;

		@Override
		public void mouseMoved(MouseEvent e)
		{
			setMouseRow(e.getX(), e.getY());
			repaintMouseRow();
			checkForLinkLabel();
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			setMouseRow(e.getX(), e.getY());
			repaintMouseRow();
			checkForLinkLabel();
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				setMouseRow(e.getX(), e.getY());
				if (mouseRow >= 0)
				{
					if (button.getBounds().contains(mouseX, mouseY))
					{
						mouseButtonDownRow = mouseRow;
					}
					else if (urlRows.containsKey(mouseRow)
							&& urlRows.get(mouseRow).contains(mouseX, mouseY))
					{
						mouseLabelDownRow = mouseRow;
					}
				}
				repaintMouseRow();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				setMouseRow(e.getX(), e.getY());

				//only selected if it was the same row the mouse down was on
				if (mouseRow >= 0)
				{
					if (mouseButtonDownRow == mouseRow
							&& button.getBounds().contains(mouseX, mouseY))
					{
						toggleLayer(mouseRow);
					}
					else if (mouseLabelDownRow == mouseRow && urlRows.containsKey(mouseRow)
							&& urlRows.get(mouseRow).contains(mouseX, mouseY))
					{
						linkClicked(mouseRow);
					}
				}

				mouseButtonDownRow = -1;
				repaintMouseRow();
			}
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			setMouseRow(e.getX(), e.getY());
			repaintMouseRow();
		}

		private void setMouseRow(int x, int y)
		{
			mouseRow = tree.getRowForLocation(x, y);
			if (mouseRow >= 0)
			{
				Rectangle bounds = tree.getRowBounds(mouseRow);
				mouseX = x - bounds.x;
				mouseY = y - bounds.y;
			}
		}

		private void repaintMouseRow()
		{
			//mouse moved out of the last row
			if (lastRow >= 0 && mouseRow != lastRow)
			{
				Rectangle bounds = tree.getRowBounds(lastRow);
				if (bounds != null)
					tree.repaint(bounds);
			}
			lastRow = mouseRow;
			if (mouseRow >= 0)
			{
				Rectangle bounds = tree.getRowBounds(mouseRow);
				if (bounds != null)
					tree.repaint(bounds);
			}
		}

		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_SPACE)
			{
				setKeyRow();
				if (keyRow >= 0)
				{
					keyDownRow = keyRow;
				}
				repaintKeyRow();
			}
		}

		public void keyReleased(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_SPACE)
			{
				setKeyRow();
				if (keyRow >= 0 && keyRow == keyDownRow)
				{
					toggleLayer(keyRow);
				}
				keyDownRow = -1;
				repaintKeyRow();
			}
		}

		public void keyTyped(KeyEvent e)
		{
		}

		private void setKeyRow()
		{
			int[] s = tree.getSelectionRows();
			if (s != null && s.length > 0)
			{
				keyRow = s[0];
			}
			else
			{
				keyRow = -1;
			}
		}

		private void repaintKeyRow()
		{
			if (keyRow >= 0)
			{
				Rectangle bounds = tree.getRowBounds(keyRow);
				tree.repaint(bounds);
			}
		}

		private void checkForLinkLabel()
		{
			int cursor = -1;
			if (mouseRow >= 0 && urlRows.containsKey(mouseRow))
			{
				Rectangle labelBounds = urlRows.get(mouseRow);
				if (labelBounds.contains(mouseX, mouseY))
				{
					cursor = Cursor.HAND_CURSOR;
				}
			}

			//only set the cursor if it is not the same as the last one set
			if (lastCursor != cursor || lastCursorRow != mouseRow)
			{
				lastCursor = cursor;
				lastCursorRow = mouseRow;
				if (cursor == -1)
				{
					tree.setCursor(null);
					tree.setToolTipText(null);
				}
				else
				{
					tree.setCursor(Cursor.getPredefinedCursor(cursor));
					TreePath path = tree.getPathForRow(mouseRow);
					if (path != null)
					{
						Object value = path.getLastPathComponent();
						if (value != null && value instanceof ILayerNode)
						{
							ILayerNode layer = (ILayerNode) value;
							if (layer.getDescriptionURL() != null)
							{
								tree.setToolTipText(layer.getDescriptionURL().toExternalForm());
							}
						}
					}
				}
			}
		}
	}
}
