package au.gov.ga.worldwind.panels.layers;

import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

public class ClearableBasicTreeUI extends BasicTreeUI
{
	public void relayout(TreePath path)
	{
		treeState.invalidatePathBounds(path);
		updateSize();
	}

	public void relayout()
	{
		treeState.invalidateSizes();
		updateSize();
	}
}
