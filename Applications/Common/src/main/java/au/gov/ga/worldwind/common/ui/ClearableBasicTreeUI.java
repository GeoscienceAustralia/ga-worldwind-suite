package au.gov.ga.worldwind.common.ui;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

public class ClearableBasicTreeUI extends BasicTreeUI
{
	private boolean relayout = false;
	private TreePath invalidatePath = null;

	public void relayout(TreePath path)
	{
		invalidatePath = path;
	}

	public void relayout()
	{
		relayout = true;
	}

	@Override
	public void paint(Graphics g, JComponent c)
	{
		if (relayout || invalidatePath != null)
		{
			if (invalidatePath == null)
				treeState.invalidateSizes();
			else
				treeState.invalidatePathBounds(invalidatePath);

			updateSize();

			relayout = false;
			invalidatePath = null;
		}

		super.paint(g, c);
	}
}
