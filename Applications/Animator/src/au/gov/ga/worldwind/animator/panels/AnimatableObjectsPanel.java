package au.gov.ga.worldwind.animator.panels;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A panel that allows the user to view and manipulate animatable objects
 * associated with the animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class AnimatableObjectsPanel extends CollapsiblePanelBase
{
	private static final long serialVersionUID = 20100907L;

	/** The animation this panel is viewing */
	private Animation animation;

	/** A scrollable container for holding the tree */
	private JScrollPane scrollPane;
	
	/** 
	 * The tree that allows the user to browse through an animation's {@link Animatable} objects
	 * and associated {@link Parameter}s.
	 */
	private JTree objectTree;
	
	/**
	 * The model associated with the object tree
	 */
	private DefaultTreeModel treeModel;
	
	/**
	 * Constructor. Initialises the tree from the provided (mandatory) {@link Animation} instance.
	 * 
	 * @param animation The animation this panel is viewing (Mandatory)
	 */
	public AnimatableObjectsPanel(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
		
		initialiseObjectTree();
		updateTree();
		packTreeIntoPanel();
	}

	/**
	 * Pack the object tree into the panel, ready for display
	 */
	private void packTreeIntoPanel()
	{
		scrollPane = new JScrollPane();
		scrollPane.add(objectTree);
		
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Initialises the object tree. 
	 */
	private void initialiseObjectTree()
	{
		TreeNode animationNode = new DefaultMutableTreeNode(animation.getName());
		
		treeModel = new DefaultTreeModel(animationNode);
		
		objectTree = new NameableTree(treeModel);
		objectTree.setEditable(true);
		objectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	
	/**
	 * Update the tree to display the {@link Animatable} objects and {@link Parameter}s of the
	 * current animation.
	 */
	public void updateTree()
	{
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
		
	}
	
	/**
	 * An extension of the {@link DefaultMutableTreeNode} that renders a {@link Nameable} object's name
	 * as the text value of the tree nodes.
	 */
	private class NameableTree extends JTree
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
	
}
