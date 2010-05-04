package au.gov.ga.worldwind.dataset;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.util.Icons;

public class DatasetCellRenderer extends JPanel implements TreeCellRenderer
{
	private JTree tree;
	private RendererMouseKeyListener mouseKeyListener = new RendererMouseKeyListener();

	private int mouseRow = -1, keyRow = -1, mouseDownRow = -1, keyDownRow = -1;
	private int mouseX = -1, mouseY = -1;

	private JButton button;
	private DefaultTreeCellRenderer label;

	public DatasetCellRenderer()
	{
		super(new BorderLayout(3, 3));
		setOpaque(false);

		button = new JButton();
		button.setOpaque(false);

		int height = Icons.add.getIconHeight();
		Dimension size = new Dimension(height + 2, height + 2);
		button.setPreferredSize(size);
		button.setMinimumSize(size);

		label = new DefaultTreeCellRenderer();

		add(button, BorderLayout.WEST);
		add(label, BorderLayout.CENTER);
	}

	private void selected(int row)
	{
		TreePath path = tree.getSelectionPath();
		Object value = path.getLastPathComponent();
		if(value != null && value instanceof DefaultMutableTreeNode)
		{
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			System.out.println(userObject.toString());
		}
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
			boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		//tree has changed!
		if (this.tree != tree)
		{
			if (this.tree != null)
			{
				this.tree.removeMouseListener(mouseKeyListener);
				this.tree.removeMouseMotionListener(mouseKeyListener);
				this.tree.removeKeyListener(mouseKeyListener);
			}
			this.tree = tree;
			tree.addMouseListener(mouseKeyListener);
			tree.addMouseMotionListener(mouseKeyListener);
			tree.addKeyListener(mouseKeyListener);
		}

		//update the label
		label.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if (value != null && value instanceof DefaultMutableTreeNode)
		{
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			if (userObject instanceof ILayerDefinition)
			{
				ILayerDefinition node = (ILayerDefinition) userObject;

				if (false)
					button.setIcon(Icons.remove);
				else
					button.setIcon(Icons.add);

				boolean mouseInsideButton = button.getBounds().contains(mouseX, mouseY);
				button.getModel().setRollover(
						(mouseInsideButton && row == mouseRow && mouseDownRow <= 0) || (hasFocus));
				button.getModel().setSelected(
						(mouseInsideButton && row == mouseDownRow)
								|| (hasFocus && row == keyDownRow));

				label.setIcon(null);
				
				//have to add it each time? it removes itself?
				add(label, BorderLayout.CENTER);
				return this;
			}
		}

		return label;
	}

	private class RendererMouseKeyListener extends MouseAdapter implements KeyListener
	{
		private int lastRow = -1;

		@Override
		public void mouseMoved(MouseEvent e)
		{
			setMouseRow(e.getX(), e.getY());
			repaintMouseRow();
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			setMouseRow(e.getX(), e.getY());
			repaintMouseRow();
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				setMouseRow(e.getX(), e.getY());
				if (mouseRow >= 0)
				{
					mouseDownRow = mouseRow;
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
				if (mouseRow >= 0 && mouseRow == mouseDownRow
						&& button.getBounds().contains(mouseX, mouseY))
				{
					selected(mouseRow);
				}

				mouseDownRow = -1;
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
				tree.repaint(tree.getRowBounds(lastRow));
			}
			lastRow = mouseRow;
			if (mouseRow >= 0)
			{
				tree.repaint(tree.getRowBounds(mouseRow));
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
					selected(keyRow);
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
	}
}