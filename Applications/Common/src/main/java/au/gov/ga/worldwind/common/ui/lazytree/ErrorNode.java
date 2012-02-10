package au.gov.ga.worldwind.common.ui.lazytree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents a tree node that has an error. For example, this may be displayed
 * when there is an error downloading a particular layer definition.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ErrorNode extends DefaultMutableTreeNode
{
	public ErrorNode(String label)
	{
		super(label, false);
	}
}
