package au.gov.ga.worldwind.viewer.panels.dataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import au.gov.ga.worldwind.common.ui.lazytree.ErrorNode;
import au.gov.ga.worldwind.common.ui.lazytree.LoadingNode;
import au.gov.ga.worldwind.common.ui.lazytree.LoadingTree;
import au.gov.ga.worldwind.common.util.HSLColor;
import au.gov.ga.worldwind.common.util.Icons;

public abstract class AbstractCellRenderer<E extends IIconItem, L extends IIconItem> extends JPanel
		implements TreeCellRenderer
{
	private LoadingTree tree;
	private RendererMouseKeyListener mouseKeyListener = new RendererMouseKeyListener();
	private Map<Integer, Rectangle> infoRows = new HashMap<Integer, Rectangle>();
	private Map<Integer, Rectangle> legendRows = new HashMap<Integer, Rectangle>();
	private Map<Integer, Rectangle> queryRows = new HashMap<Integer, Rectangle>();

	private int mouseRow = -1, keyRow = -1, mouseButtonDownRow = -1, keyDownRow = -1;
	private int mouseInfoRow = -1, mouseLegendRow = -1, mouseQueryRow = -1;
	private int mouseInfoDownRow = -1, mouseLegendDownRow = -1, mouseQueryDownRow = -1;
	private int mouseX = -1, mouseY = -1;

	private AbstractButton button;
	private DefaultTreeCellRenderer label;
	private JPanel buttonPanel;
	private JLabel infoLabel;
	private JLabel legendLabel;
	private JLabel queryLabel;

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
		label.setBackgroundSelectionColor(hsl.adjustTone(15));
		label.setBorderSelectionColor(hsl.adjustShade(40));

		infoLabel = new JLabel(Icons.infowhite.getIcon());
		infoLabel.setOpaque(false);
		legendLabel = new JLabel(Icons.legendwhite.getIcon());
		legendLabel.setOpaque(false);
		queryLabel = new JLabel(Icons.crosshairwhite.getIcon());
		queryLabel.setOpaque(false);

		add(button, BorderLayout.WEST);
		add(label, BorderLayout.CENTER);
		buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setOpaque(false);
		add(buttonPanel, BorderLayout.EAST);
		
		GridBagConstraints c;
		Insets insets = new Insets(0, 0, 0, 2);
		int i = 0;
		
		c = new GridBagConstraints();
		c.gridx = i++;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = (Insets) insets.clone();
		buttonPanel.add(infoLabel, c);
		
		c = new GridBagConstraints();
		c.gridx = i++;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = (Insets) insets.clone();
		buttonPanel.add(legendLabel, c);
		
		c = new GridBagConstraints();
		c.gridx = i++;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = (Insets) insets.clone();
		buttonPanel.add(queryLabel, c);

		loadingIcon = Icons.newLoadingIcon();
	}

	public LoadingTree getTree()
	{
		return tree;
	}

	@Override
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
				@Override
				public void run()
				{
					tree.repaint();
				}
			};
			item.loadIcon(afterLoad);

			//icon may have loaded straight away; check
			if (item.isIconLoaded())
			{
				label.setIcon(item.getIcon());
			}
		}

		if (item.isLoading())
		{
			label.setIcon(loadingIcon);
		}

		setupLabel(label, item);

		boolean infoRow = isInfoRow(item);
		L layerItem = getLayerValue(item);
		boolean layerRow = layerItem != null;
		boolean legendRow = layerRow ? isLegendRow(layerItem) : false;
		boolean queryRow = layerRow ? isQueryRow(layerItem) : false;

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

		//set up the icon labels
		updateLabel(row, mouseInfoRow, infoRow, infoLabel, Icons.info.getIcon(), Icons.infowhite
				.getIcon());
		updateLabel(row, mouseLegendRow, legendRow, legendLabel, Icons.legend.getIcon(),
				Icons.legendwhite.getIcon());
		updateLabel(row, mouseQueryRow, queryRow, queryLabel, Icons.crosshair.getIcon(),
				Icons.crosshairwhite.getIcon());

		//ensure the labels are in the correct position by forcing a layout
		validate();

		//update the row/rectangle maps
		updateLabelMap(row, infoRow, infoRows, infoLabel);
		updateLabelMap(row, legendRow, legendRows, legendLabel);
		updateLabelMap(row, queryRow, queryRows, queryLabel);

		return this;
	}

	private void updateLabel(int row, int mouseRow, boolean isRow, JLabel label, Icon overIcon,
			Icon otherIcon)
	{
		label.setVisible(isRow);
		if (row == mouseRow)
			label.setIcon(overIcon);
		else
			label.setIcon(otherIcon);
	}

	private void updateLabelMap(int row, boolean isRow, Map<Integer, Rectangle> map, JLabel label)
	{
		if (isRow)
		{
			Rectangle labelBounds = new Rectangle(label.getPreferredSize());
			labelBounds.x += label.getLocation().x + buttonPanel.getLocation().x;
			map.put(row, labelBounds);
		}
		else
			map.remove(row);
	}

	protected abstract AbstractButton createButton();

	protected abstract void validateTree(JTree tree);

	protected abstract E getValue(Object value);

	protected abstract boolean isInfoRow(E value);

	protected abstract boolean isLegendRow(L value);

	protected abstract boolean isQueryRow(L value);

	protected abstract L getLayerValue(E value);

	protected abstract void setupLabel(DefaultTreeCellRenderer label, E value);

	protected abstract void setupButton(AbstractButton button, L value, boolean mouseInsideButton,
			boolean rollover, boolean down);

	//protected abstract String getLinkLabelToolTipText(Object value);

	protected abstract void buttonPressed(int row);

	protected abstract void infoClicked(int row);

	protected abstract void legendClicked(int row);

	protected abstract void queryClicked(int row);

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
					else
					{
						if (mouseInsideMapBounds(infoRows))
						{
							mouseInfoDownRow = mouseRow;
						}
						else if (mouseInsideMapBounds(legendRows))
						{
							mouseLegendDownRow = mouseRow;
						}
						else if (mouseInsideMapBounds(queryRows))
						{
							mouseQueryDownRow = mouseRow;
						}
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
					else if (mouseInfoDownRow == mouseRow && mouseInsideMapBounds(infoRows))
					{
						infoClicked(mouseRow);
					}
					else if (mouseLegendDownRow == mouseRow && mouseInsideMapBounds(legendRows))
					{
						legendClicked(mouseRow);
					}
					else if (mouseQueryDownRow == mouseRow && mouseInsideMapBounds(queryRows))
					{
						queryClicked(mouseRow);
					}
				}

				mouseButtonDownRow = -1;
				repaintMouseRow();
			}
		}

		private boolean mouseInsideMapBounds(Map<Integer, Rectangle> map)
		{
			return map.containsKey(mouseRow) && map.get(mouseRow).contains(mouseX, mouseY);
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

		@Override
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

		@Override
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

		@Override
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
			mouseInfoRow = -1;
			mouseLegendRow = -1;
			mouseQueryRow = -1;
			if (mouseRow >= 0)
			{
				if (infoRows.containsKey(mouseRow))
				{
					Rectangle labelBounds = infoRows.get(mouseRow);
					if (labelBounds.contains(mouseX, mouseY))
					{
						cursor = Cursor.HAND_CURSOR;
						mouseInfoRow = mouseRow;
					}
				}
				if (legendRows.containsKey(mouseRow))
				{
					Rectangle labelBounds = legendRows.get(mouseRow);
					if (labelBounds.contains(mouseX, mouseY))
					{
						cursor = Cursor.HAND_CURSOR;
						mouseLegendRow = mouseRow;
					}
				}
				if (queryRows.containsKey(mouseRow))
				{
					Rectangle labelBounds = queryRows.get(mouseRow);
					if (labelBounds.contains(mouseX, mouseY))
					{
						cursor = Cursor.CROSSHAIR_CURSOR;
						mouseQueryRow = mouseRow;
					}
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
					//tree.setToolTipText(null);
				}
				else
				{
					tree.setCursor(Cursor.getPredefinedCursor(cursor));
					/*TreePath path = tree.getPathForRow(mouseRow);
					if (path != null)
					{
						Object value = path.getLastPathComponent();
						tree.setToolTipText(getLinkLabelToolTipText(value));
					}*/
				}
			}
		}
	}
}