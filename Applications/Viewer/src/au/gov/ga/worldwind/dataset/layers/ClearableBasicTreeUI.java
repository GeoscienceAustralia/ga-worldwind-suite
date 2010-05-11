package au.gov.ga.worldwind.dataset.layers;

import javax.swing.plaf.basic.BasicTreeUI;

public class ClearableBasicTreeUI extends BasicTreeUI
{
	public void relayout()
	{
		treeState.invalidateSizes();
		updateSize();
	}
}
