package au.gov.ga.worldwind.common.ui.lazytree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public interface ITreeObject
{
	public MutableTreeNode[] getChildren(DefaultTreeModel model);
}
