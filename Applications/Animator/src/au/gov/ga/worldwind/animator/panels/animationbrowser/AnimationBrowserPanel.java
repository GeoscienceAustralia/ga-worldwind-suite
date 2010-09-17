package au.gov.ga.worldwind.animator.panels.animationbrowser;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.CurrentlySelectedObject;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

/**
 * A panel that allows the user to view and manipulate {@link Animatable} objects,
 * and their {@link Parameter}s, associated with an animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimationBrowserPanel extends CollapsiblePanelBase
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
	private AnimationTreeModel treeModel;
	
	/**
	 * Constructor. Initialises the tree from the provided (mandatory) {@link Animation} instance.
	 * 
	 * @param animation The animation this panel is viewing (Mandatory)
	 */
	public AnimationBrowserPanel(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
		
		setName(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getAnimationBrowserPanelNameKey()));
		
		initialiseObjectTree();
		packTreeIntoPanel();
	}

	/**
	 * Pack the object tree into the panel, ready for display
	 */
	private void packTreeIntoPanel()
	{
		scrollPane = new JScrollPane(objectTree);
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Initialises the object tree. 
	 */
	private void initialiseObjectTree()
	{
		treeModel = new AnimationTreeModel(animation);
		
		objectTree = new NameableTree(treeModel);
		objectTree.setEditable(false);
		objectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		objectTree.setCellRenderer(new AnimationTreeRenderer());
		objectTree.setToggleClickCount(-1);
		objectTree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				CurrentlySelectedObject.set(e.getSource());
			}
		});
	}
	
	@Override
	public void refreshView(ChangeEvent e)
	{
		if (e != null && e.getSource() instanceof Animation)
		{
			this.animation = (Animation)e.getSource();
			treeModel = new AnimationTreeModel(animation);
			objectTree.setModel(treeModel);
		}
		objectTree.validate();
	}

	/**
	 * An extension of the {@link DefaultMutableTreeNode} that renders a {@link Nameable} object's name
	 * as the text value of the tree nodes. Used in conjunction with the {@link AnimationTreeRenderer}.
	 */
	private static final class NameableTree extends JTree
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
