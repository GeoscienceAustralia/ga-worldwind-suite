package au.gov.ga.worldwind.animator.panels.animationbrowser;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DropMode;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.CurrentlySelectedObject;
import au.gov.ga.worldwind.animator.animation.camera.Camera;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.panels.AnimatorCollapsiblePanel;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.ui.NameableTree;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A panel that allows the user to view and manipulate {@link Animatable} objects,
 * and their {@link Parameter}s, associated with an animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimationBrowserPanel extends CollapsiblePanelBase implements AnimatorCollapsiblePanel
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
	private BasicAction moveObjectUpAction;
	private BasicAction moveObjectDownAction;
	private BasicAction enableAllAction;
	private BasicAction disableAllAction;
	private BasicAction armAllAction;
	private BasicAction disarmAllAction;
	
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

	/**
	 * Initialises the object tree. 
	 */
	private void initialiseObjectTree()
	{
		treeModel = new AnimationTreeModel(animation);
		
		objectTree = new NameableTree(treeModel);
		objectTree.setEditable(false);
		objectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		objectTree.setCellRenderer(new AnimationBrowserTreeRenderer());
		objectTree.setToggleClickCount(-1);
		objectTree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				AnimationObject selectedObject = getSelectedAnimationObject();
				
				CurrentlySelectedObject.set(selectedObject);
				
				removeAnimationObjectAction.setEnabled(selectedObject != null && isRemovable(selectedObject));
				moveObjectUpAction.setEnabled(selectedObject != null && isMovable(selectedObject) && !isFirstObject(selectedObject));
				moveObjectDownAction.setEnabled(selectedObject != null && isMovable(selectedObject) && !isLastObject(selectedObject));
			}
		});
		objectTree.setActionMap(null); // Remove the default key bindings so our custom ones will work
		
		objectTree.setTransferHandler(new AnimationBrowserTransferHandler(animation, objectTree));
		objectTree.setDragEnabled(true);
		objectTree.setDropMode(DropMode.INSERT);
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
		
		moveObjectUpAction = new BasicAction(getMessage(getAnimationBrowserMoveUpLabelKey()), Icons.up.getIcon());
		moveObjectUpAction.setEnabled(false);
		moveObjectUpAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				moveSelectedObjectUp();
			}
		});
		
		moveObjectDownAction = new BasicAction(getMessage(getAnimationBrowserMoveUpLabelKey()), Icons.down.getIcon());
		moveObjectDownAction.setEnabled(false);
		moveObjectDownAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				moveSelectedObjectDown();
			}
		});
		
		enableAllAction = new BasicAction(getMessage(getAnimationBrowserEnableAllLabelKey()), Icons.checkall.getIcon());
		enableAllAction.setEnabled(true);
		enableAllAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setAllEnabled(true);
			}
		});
		
		disableAllAction = new BasicAction(getMessage(getAnimationBrowserDisableAllLabelKey()), Icons.uncheckall.getIcon());
		disableAllAction.setEnabled(true);
		disableAllAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setAllEnabled(false);
			}
		});
		
		armAllAction = new BasicAction(getMessage(getAnimationBrowserArmAllLabelKey()), Icons.armed.getIcon());
		armAllAction.setEnabled(true);
		armAllAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setAllArmed(true);
			}
		});
		
		disarmAllAction = new BasicAction(getMessage(getAnimationBrowserDisarmAllLabelKey()), Icons.disarmed.getIcon());
		disarmAllAction.setEnabled(true);
		disarmAllAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setAllArmed(false);
			}
		});
		
		
	}

	private void promptToRemoveSelectedObject()
	{
		AnimationObject selectedObject = getSelectedAnimationObject();
		if (!isRemovable(selectedObject))
		{
			return;
		}
		
		boolean removalConfirmed = promptUserForConfirmationOfRemoval(selectedObject);
		if (removalConfirmed)
		{
			if (selectedObject instanceof ElevationModelIdentifier)
			{
				animation.getAnimatableElevation().removeElevationModel((ElevationModelIdentifier)selectedObject);
			}
			else
			{
				animation.removeAnimatableObject((Animatable)selectedObject); // TODO Make this more general to allow extension in the future
			}
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
	
	private void moveSelectedObjectUp()
	{
		AnimationObject selectedObject = getSelectedAnimationObject();
		if (selectedObject == null)
		{
			return;
		}
		if (isMovable(selectedObject) && !isFirstObject(selectedObject))
		{
			animation.moveAnimatableObject((Animatable)selectedObject, indexOf(selectedObject) - 1);
			treeModel.notifyTreeChanged(selectedObject);
			objectTree.setSelectionPath(new TreePath(new Object[]{animation, selectedObject}));
		}
	}
	
	private void moveSelectedObjectDown()
	{
		AnimationObject selectedObject = getSelectedAnimationObject();
		if (selectedObject == null)
		{
			return;
		}
		if (isMovable(selectedObject) && !isLastObject(selectedObject))
		{
			animation.moveAnimatableObject((Animatable)selectedObject, indexOf(selectedObject) + 1);
			treeModel.notifyTreeChanged(selectedObject);
			objectTree.setSelectionPath(new TreePath(new Object[]{animation, selectedObject}));
		}
	}
	
	private void setAllEnabled(boolean enabled)
	{
		for (Animatable animatable : animation.getAnimatableObjects())
		{
			animatable.setEnabled(enabled);
		}
		objectTree.repaint();
	}
	
	private void setAllArmed(boolean armed)
	{
		for (Animatable animatable : animation.getAnimatableObjects())
		{
			animatable.setArmed(armed);
		}
		objectTree.repaint();
	}

	private boolean isLastObject(AnimationObject selectedObject)
	{
		return indexOf(selectedObject) == animation.getAnimatableObjects().size() - 1;
	}

	private int indexOf(AnimationObject object)
	{
		return animation.getAnimatableObjects().indexOf(object);
	}

	private boolean isFirstObject(AnimationObject object)
	{
		return animation.getAnimatableObjects().indexOf(object) == 0;
	}

	private boolean isMovable(AnimationObject object)
	{
		return object != null && object instanceof Animatable;
	}
	
	private boolean isRemovable(AnimationObject selectedObject)
	{
		return selectedObject != null && ((selectedObject instanceof Animatable && !(selectedObject instanceof Camera)) || (selectedObject instanceof ElevationModelIdentifier));
	}
	
	private AnimationObject getSelectedAnimationObject()
	{
		if (objectTree.isSelectionEmpty())
		{
			return null;
		}
		Object selectedObject = objectTree.getSelectionPath().getLastPathComponent();
		if (!(selectedObject instanceof AnimationObject))
		{
			return null;
		}
		return (AnimationObject)selectedObject;
	}
	
	private void initialiseToolbar()
	{
		toolbar = new JToolBar();
		toolbar.setActionMap(null);
		toolbar.add(removeAnimationObjectAction);
		toolbar.addSeparator();
		toolbar.add(moveObjectUpAction);
		toolbar.add(moveObjectDownAction);
		toolbar.addSeparator();
		toolbar.add(enableAllAction);
		toolbar.add(disableAllAction);
		toolbar.addSeparator();
		toolbar.add(armAllAction);
		toolbar.add(disarmAllAction);
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
	
	@Override
	public void updateAnimation(Animation newAnimation)
	{
		if (newAnimation == null)
		{
			return;
		}
		
		this.animation = newAnimation;
		treeModel = new AnimationTreeModel(animation);
		objectTree.setModel(treeModel);
		objectTree.setTransferHandler(new AnimationBrowserTransferHandler(animation, objectTree));
		objectTree.validate();
	}
}
