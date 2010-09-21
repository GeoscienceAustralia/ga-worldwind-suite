package au.gov.ga.worldwind.animator.panels.animationbrowser;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.CurrentlySelectedObject;
import au.gov.ga.worldwind.animator.animation.camera.Camera;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.common.ui.BasicAction;

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
	
	private JToolBar toolbar;
	
	/** 
	 * The tree that allows the user to browse through an animation's {@link Animatable} objects
	 * and associated {@link Parameter}s.
	 */
	private JTree objectTree;
	
	/**
	 * The model associated with the object tree
	 */
	private AnimationTreeModel treeModel;
	
	// Actions
	private BasicAction removeAnimationObjectAction;
	
	/**
	 * Constructor. Initialises the tree from the provided (mandatory) {@link Animation} instance.
	 * 
	 * @param animation The animation this panel is viewing (Mandatory)
	 */
	public AnimationBrowserPanel(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
		
		setName(getMessage(getAnimationBrowserPanelNameKey()));
		
		initialiseObjectTree();
		initialiseActions();
		initialiseToolbar();
		packComponents();
	}
	
	private void initialiseActions()
	{
		removeAnimationObjectAction = new BasicAction(getMessage(getAnimationBrowserRemoveObjectLabelKey()), Icons.delete.getIcon());
		removeAnimationObjectAction.setEnabled(false);
		removeAnimationObjectAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				promptToRemoveSelectedObject();
			}
		});
	}

	private void promptToRemoveSelectedObject()
	{
		if (objectTree.isSelectionEmpty())
		{
			return;
		}
		
		AnimationObject selectedObject = (AnimationObject)objectTree.getSelectionPath().getLastPathComponent();
		if (!isRemovable(selectedObject))
		{
			return;
		}
		
		boolean removalConfirmed = promptUserForConfirmationOfRemoval(selectedObject);
		if (removalConfirmed)
		{
			animation.removeAnimatableObject((Animatable)selectedObject); // TODO Make this more general to allow extension in the future
			removeAnimationObjectAction.setEnabled(false);
		}
		
	}
	
	protected boolean promptUserForConfirmationOfRemoval(AnimationObject selectedObject)
	{
		int response = JOptionPane.showConfirmDialog(getParentWindow(),
 				 									 getMessage(getQueryRemoveObjectFromAnimationMessageKey(), selectedObject.getName()), 
 				 									 getMessage(getQueryRemoveObjectFromAnimationCaptionKey()),
 				 									 JOptionPane.YES_NO_OPTION,
 				 									 JOptionPane.QUESTION_MESSAGE);

		return response == JOptionPane.YES_OPTION;
	}

	private void initialiseToolbar()
	{
		toolbar = new JToolBar();
		toolbar.setActionMap(null);
		toolbar.add(removeAnimationObjectAction);
	}

	/**
	 * Pack the browser components into the parent panel, ready for display
	 */
	private void packComponents()
	{
		scrollPane = new JScrollPane(objectTree);
		add(toolbar, BorderLayout.NORTH);
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
				AnimationObject selectedObject = (AnimationObject)e.getPath().getLastPathComponent();
				CurrentlySelectedObject.set(selectedObject);
				if (isRemovable(selectedObject))
				{
					removeAnimationObjectAction.setEnabled(true);
				}
				else
				{
					removeAnimationObjectAction.setEnabled(false);
				}
			}
		});
		objectTree.setActionMap(null); // Remove the default key bindings so our custom ones will work
	}
	
	private boolean isRemovable(AnimationObject selectedObject)
	{
		return selectedObject instanceof Animatable && !(selectedObject instanceof Camera);
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
