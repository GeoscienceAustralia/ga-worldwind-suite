package au.gov.ga.worldwind.animator.ui;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import au.gov.ga.worldwind.animator.util.Nameable;

/**
 * An extension of the {@link DefaultMutableTreeNode} that renders a {@link Nameable} object's name
 * as the text value of the tree nodes. Used in conjunction with the {@link AnimationBrowserTreeRenderer}.
 */
public class NameableTree extends JTree
{
	private static final long serialVersionUID = 20100907L;
	
	public NameableTree(TreeModel model)
	{
		super(model);
	}

	@Override
	public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		if (value instanceof Nameable)
		{
			return ((Nameable)value).getName();
		}
		
		return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
	}
}