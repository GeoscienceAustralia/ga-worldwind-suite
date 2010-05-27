package au.gov.ga.worldwind.panels.dataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.components.lazytree.ErrorNode;
import au.gov.ga.worldwind.components.lazytree.LoadingNode;
import au.gov.ga.worldwind.panels.layers.LayerTreeModel;
import au.gov.ga.worldwind.util.DefaultLauncher;
import au.gov.ga.worldwind.util.HSLColor;
import au.gov.ga.worldwind.util.Icons;

public class DatasetCellRenderer extends JPanel implements TreeCellRenderer
{
	private DatasetTree tree;
	private RendererMouseKeyListener mouseKeyListener = new RendererMouseKeyListener();
	private Map<Integer, Rectangle> urlRows = new HashMap<Integer, Rectangle>();

	private int mouseRow = -1, keyRow = -1, mouseButtonDownRow = -1, mouseLabelDownRow = -1,
			keyDownRow = -1;
	private int mouseX = -1, mouseY = -1;

	private JButton button;
	private DefaultTreeCellRenderer label;
	private JLabel infoLabel;

	private ImageIcon loadingIcon;

	private LayerTreeModel layerTreeModel;

	public DatasetCellRenderer()
	{
		super(new BorderLayout(3, 3));
		setOpaque(false);

		button = new JButton();
		button.setOpaque(false);

		int height = Icons.add.getIcon().getIconHeight();
		Dimension size = new Dimension(height + 2, height + 2);
		button.setPreferredSize(size);
		button.setMinimumSize(size);

		label = new DefaultTreeCellRenderer();
		label.setTextNonSelectionColor(Color.black);
		label.setTextSelectionColor(Color.black);
		Color backgroundSelection = label.getBackgroundSelectionColor();
		HSLColor hsl = new HSLColor(backgroundSelection);
		label.setBackgroundSelectionColor(hsl.adjustTone(80));
		label.setBorderSelectionColor(hsl.adjustShade(40));

		infoLabel = new JLabel(Icons.info.getIcon());
		infoLabel.setOpaque(false);

		add(button, BorderLayout.WEST);
		add(label, BorderLayout.CENTER);
		add(infoLabel, BorderLayout.EAST);

		loadingIcon = Icons.newLoadingIcon();
	}

	public void setLayerTreeModel(LayerTreeModel layerTreeModel)
	{
		this.layerTreeModel = layerTreeModel;
	}

	private void toggleLayer(int row)
	{
		if (layerTreeModel == null)
			return;

		TreePath path = tree.getSelectionPath();
		if (path != null)
		{
			Object value = path.getLastPathComponent();
			if (value != null && value instanceof DefaultMutableTreeNode)
			{
				Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
				if (userObject != null && userObject instanceof ILayerDefinition)
				{
					ILayerDefinition layer = (ILayerDefinition) userObject;
					if (layerTreeModel.containsLayer(layer))
					{
						layerTreeModel.removeLayer(layer);
					}
					else
					{
						layerTreeModel.addLayer(layer, path.getPath());
					}
					tree.repaint();
				}
			}
		}
	}

	private void linkClicked(int row)
	{
		TreePath path = tree.getSelectionPath();
		Object value = path.getLastPathComponent();
		if (value != null && value instanceof DefaultMutableTreeNode)
		{
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			if (userObject != null && userObject instanceof IData)
			{
				IData data = (IData) userObject;
				if (data.getDescriptionURL() != null)
				{
					DefaultLauncher.openURL(data.getDescriptionURL());
				}
			}
		}
	}

	public Component getTreeCellRendererComponent(final JTree t, Object value, boolean selected,
			boolean expanded, boolean leaf, final int row, boolean hasFocus)
	{
		if (!(t instanceof DatasetTree))
			throw new IllegalArgumentException("Tree must be a DatasetTree");

		//tree has changed!
		if (tree != t)
		{
			if (tree != null)
			{
				tree.removeMouseListener(mouseKeyListener);
				tree.removeMouseMotionListener(mouseKeyListener);
				tree.removeKeyListener(mouseKeyListener);
			}
			tree = (DatasetTree) t;
			tree.addMouseListener(mouseKeyListener);
			tree.addMouseMotionListener(mouseKeyListener);
			tree.addKeyListener(mouseKeyListener);

			loadingIcon.setImageObserver(tree);
		}

		//update the label
		label.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if (row < 0 || value == null || !(value instanceof DefaultMutableTreeNode))
			return label;

		//for some reason we have to readd the label
		add(label, BorderLayout.CENTER);

		boolean urlRow = false;
		boolean layerRow = false;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		if (node instanceof LoadingNode)
		{
			label.setIcon(loadingIcon);
		}
		else if (node instanceof ErrorNode)
		{
			label.setIcon(Icons.error.getIcon());
		}
		else
		{
			Object userObject = node.getUserObject();
			if (userObject != null)
			{
				if (userObject instanceof IData)
				{
					IData data = (IData) userObject;
					if (data.isIconLoaded())
					{
						label.setIcon(data.getIcon());
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
						data.loadIcon(afterLoad);
					}

					if (data.isLoading())
					{
						label.setIcon(loadingIcon);
					}

					if (data.getDescriptionURL() != null)
					{
						urlRow = true;
					}
				}
				else
				{
					label.setIcon(null);
				}

				if (userObject instanceof ILayerDefinition && layerTreeModel != null)
				{
					layerRow = true;
					ILayerDefinition layer = (ILayerDefinition) userObject;
					if (layerTreeModel.containsLayer(layer))
						button.setIcon(Icons.remove.getIcon());
					else
						button.setIcon(Icons.add.getIcon());

					boolean mouseInsideButton =
							mouseRow >= 0 && button.getBounds().contains(mouseX, mouseY);
					boolean rollover =
							(mouseInsideButton && row == mouseRow && mouseButtonDownRow <= 0)
									|| (hasFocus);
					boolean down =
							(mouseInsideButton && row == mouseButtonDownRow)
									|| (hasFocus && row == keyDownRow);

					button.getModel().setRollover(rollover && row >= 0);
					button.getModel().setSelected(down && row >= 0);
				}
			}
		}

		//the button is only visible if the row represents a layer
		button.setVisible(layerRow);

		//set up the info icon label
		infoLabel.setVisible(urlRow);
		if (selected)
			infoLabel.setIcon(Icons.info.getIcon());
		else
			infoLabel.setIcon(Icons.infowhite.getIcon());

		if (urlRow)
		{
			Rectangle labelBounds = new Rectangle(infoLabel.getPreferredSize());
			//ensure the label is in the correct position by forcing a layout
			labelBounds.x += infoLabel.getLocation().x;
			urlRows.put(row, labelBounds);
		}
		else
			urlRows.remove(row);

		return this;
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
				tree.repaint(tree.getRowBounds(mouseRow));
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
						if (value != null && value instanceof DefaultMutableTreeNode)
						{
							Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
							if (userObject instanceof IData)
							{
								IData data = (IData) userObject;
								if (data.getDescriptionURL() != null)
								{
									tree.setToolTipText(data.getDescriptionURL().toExternalForm());
								}
							}
						}
					}
				}
			}
		}
	}
}