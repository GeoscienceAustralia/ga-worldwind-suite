package au.gov.ga.worldwind.animator.panels.animationbrowser;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.panels.DataTransferFlavors;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A transfer handler for the {@link AnimationBrowserPanel}.
 * <p/>
 * Can handle transfers of:
 * <ul>
 * 	<li>Animation objects from within the panel (i.e. changing the order of an animation object),
 * 	<li>TODO: A layer identifier from the layer palette panel (i.e. adding a new layer from the palette to the current animation); and
 * 	<li>TODO: A layer definition file from an external source (i.e. adding a layer from an external file, not via the palette)
 * </ul>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class AnimationBrowserTransferHandler extends TransferHandler
{
	private static final long serialVersionUID = 20100923L;
	
	private JTree animationObjectTree;
	
	private Animation animation;
	
	public AnimationBrowserTransferHandler(Animation animation, JTree animationObjectTree)
	{
		super();
		Validate.notNull(animation, "An animation is required");
		Validate.notNull(animationObjectTree, "An animation tree is required");
		this.animation = animation;
		this.animationObjectTree = animationObjectTree;
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		return COPY_OR_MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		if (c != animationObjectTree)
		{
			return null;
		}
		
		AnimationObject selectedObject = getSelectedAnimationObject();
		if (!isSelectedObjectTransferable(selectedObject))
		{
			return null;
		}
		
		return new AnimationObjectTransferable(selectedObject);
	}
	
	@Override
	public boolean importData(TransferSupport support)
	{
		if (!canImport(support))
		{
			return false;
		}
		
		AnimationObject object = null;
		try
		{
			// Try to import an animation object first...
			if (support.isDataFlavorSupported(DataTransferFlavors.getAnimationObjectFlavor()))
			{
				object = (AnimationObject)support.getTransferable().getTransferData(DataTransferFlavors.getAnimationObjectFlavor());
				animation.changeOrderOfAnimatableObject((Animatable)object, getNewIndexForAnimationObject(object, support));
			}
		
		}
		catch (Exception e)
		{
			ExceptionLogger.logException(e);
			return false;
		}
		
		notifyAnimationTreeChanged(object, support);
		return true;
		
	}
	
	/**
	 * @return The new index for the animation object being transferred
	 */
	private int getNewIndexForAnimationObject(AnimationObject object, TransferSupport support)
	{
		int targetIndex = ((JTree.DropLocation)support.getDropLocation()).getChildIndex();
		int currentIndex = animation.getAnimatableObjects().indexOf(object);
		
		if (targetIndex <= currentIndex)
		{
			return targetIndex;
		}
		return targetIndex - 1;
	}

	private void notifyAnimationTreeChanged(AnimationObject object, TransferSupport support)
	{
		((AnimationTreeModel)animationObjectTree.getModel()).notifyTreeChanged(animation);
		animationObjectTree.setSelectionPath(new TreePath(new Object[]{animation, object}));
	}

	@Override
	public boolean canImport(TransferSupport support)
	{
		return support.isDrop() &&
				isTargetingAnimationObjectTree(support) &&
				isTargetingCorrectLevelInAnimationObjectTree(support) &&
				isSupportedFlavor(support); 
	}
	
	private boolean isSupportedFlavor(TransferSupport support)
	{
		return support.isDataFlavorSupported(DataTransferFlavors.getAnimationObjectFlavor());// TODO: Add cases for layer identifiers and 
	}
	
	private boolean isTargetingAnimationObjectTree(TransferSupport support)
	{
		return support.getComponent() == animationObjectTree;
	}
	
	private boolean isTargetingCorrectLevelInAnimationObjectTree(TransferSupport support)
	{
		if (!(support.getDropLocation() instanceof JTree.DropLocation))
		{
			return false;
		}
		
		JTree.DropLocation dropLocation = (JTree.DropLocation)support.getDropLocation();
		return dropLocation.getPath().getPathCount() == 1;
	}
	
	private boolean isSelectedObjectTransferable(AnimationObject selectedObject)
	{
		return (selectedObject instanceof Animatable);
	}
	
	private AnimationObject getSelectedAnimationObject()
	{
		if (animationObjectTree.isSelectionEmpty())
		{
			return null;
		}
		return (AnimationObject)animationObjectTree.getSelectionPath().getLastPathComponent();
	}
}
