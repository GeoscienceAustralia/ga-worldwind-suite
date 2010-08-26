package au.gov.ga.worldwind.viewer.panels.layers;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

public class ClearableBasicTreeUI extends BasicTreeUI
{
	private Object lock = new Object();

	public void relayout(TreePath path)
	{
		treeState.invalidatePathBounds(path);
		updateSize();
	}

	public void relayout()
	{
		synchronized (lock)
		{
			treeState.invalidateSizes();
			updateSize();
		}
	}

	@Override
	public void paint(Graphics g, JComponent c)
	{
		//TODO uncommenting this causes a deadlock, but if relayout() is called while
		//painting, tree cell bounds can be broken. need some other way of synchronizing
		
		//synchronized (lock)
		{
			super.paint(g, c);
		}
	}
}
