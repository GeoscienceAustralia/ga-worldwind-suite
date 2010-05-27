package au.gov.ga.worldwind.panels.dataset;

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

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.components.lazytree.ErrorNode;
import au.gov.ga.worldwind.components.lazytree.LoadingNode;
import au.gov.ga.worldwind.components.lazytree.LoadingTree;
import au.gov.ga.worldwind.util.HSLColor;
import au.gov.ga.worldwind.util.Icons;

public abstract class AbstractCellRenderer<E extends IIconItem, L extends IIconItem> extends JPanel
		implements TreeCellRenderer
{
	private LoadingTree tree;
	private RendererMouseKeyListener mouseKeyListener = new RendererMouseKeyListener();
	private Map<Integer, Rectangle> urlRows = new HashMap<Integer, Rectangle>();

	private int mouseRow = -1, keyRow = -1, mouseButtonDownRow = -1, mouseLabelDownRow = -1,
			keyDownRow = -1;
	private int mouseX = -1, mouseY = -1;

	private AbstractButton button;
	private DefaultTreeCellRenderer label;
	private JLabel infoLabel;

	private ImageIcon loadingIcon;

	public AbstractCellRenderer()
	{
		super(new BorderLayout(3, 3));
		setOpaque(false);

		button = createButton();
		button.setOpaque(false);

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

	public LoadingTree getTree()
	{
		return tree;
	}

	public Component getTreeCellRendererComponent(final JTree t, Object value, boolean selected,
			boolean expanded, boolean leaf, final int row, boolean hasFocus)
	{
		validateTree(t);

		//tree has changed!
		if (tree != t)
		{
			if (tree != null)
			{
				tree.removeMouseListener(mouseKeyListener);
				tree.removeMouseMotionListener(mouseKeyListener);
				tree.removeKeyListener(mouseKeyListener);
			}
			tree = (LoadingTree) t;
			tree.addMouseListener(mouseKeyListener);
			tree.addMouseMotionListener(mouseKeyListener);
			tree.addKeyListener(mouseKeyListener);

			loadingIcon.setImageObserver(tree);
		}

		//update the label
		label.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if (row < 0 || value == null)
			return label;

		if (value instanceof LoadingNode)
		{
			label.setIcon(loadingIcon);
			return label;
		}
		if (value instanceof ErrorNode)
		{
			label.setIcon(Icons.error.getIcon());
			return label;
		}

		E item = getValue(value);
		if (item == null)
		{
			label.setIcon(null);
			return label;
		}

		//for some reason we have to readd the label
		add(label, BorderLayout.CENTER);

		if (item.isIconLoaded())
		{
			label.setIcon(item.getIcon());
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
			item.loadIcon(afterLoad);
		}

		if (item.isLoading())
		{
			label.setIcon(loadingIcon);
		}

		boolean urlRow = isURLRow(item);
		L layerItem = getLayerValue(item);
		boolean layerRow = layerItem != null;

		if (layerRow)
		{
			boolean mouseInsideButton =
					mouseRow >= 0 && button.getBounds().contains(mouseX, mouseY);
			boolean rollover =
					(mouseInsideButton && row == mouseRow && mouseButtonDownRow <= 0) || (hasFocus);
			boolean down =
					(mouseInsideButton && row == mouseButtonDownRow)
							|| (hasFocus && row == keyDownRow);

			setupButton(button, layerItem, mouseInsideButton, rollover, down);
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

	protected abstract AbstractButton createButton();

	protected abstract void validateTree(JTree tree);

	protected abstract E getValue(Object value);

	protected abstract boolean isURLRow(E value);

	protected abstract L getLayerValue(E value);

	protected abstract void setupButton(AbstractButton button, L value, boolean mouseInsideButton,
			boolean rollover, boolean down);

	protected abstract String getLinkLabelToolTipText(Object value);

	protected abstract void buttonPressed(int row);

	protected abstract void linkClicked(int row);

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
						buttonPressed(mouseRow);
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
					buttonPressed(keyRow);
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
						tree.setToolTipText(getLinkLabelToolTipText(value));
					}
				}
			}
		}
	}
}