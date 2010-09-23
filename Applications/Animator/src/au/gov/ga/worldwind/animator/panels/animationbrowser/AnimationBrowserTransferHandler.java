package au.gov.ga.worldwind.animator.panels.animationbrowser;

import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.getAnimationObjectFlavor;
import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.getFileListFlavor;
import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.getLayerIdentifierFlavor;
import gov.nasa.worldwind.layers.Layer;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.layer.AnimatableLayer;
import au.gov.ga.worldwind.animator.animation.layer.DefaultAnimatableLayer;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifier;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifierFactory;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerOpacityParameter;
import au.gov.ga.worldwind.animator.layers.AnimationLayerLoader;
import au.gov.ga.worldwind.animator.panels.AnimationObjectTransferable;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A transfer handler for the {@link AnimationBrowserPanel}.
 * <p/>
 * Can handle transfers of:
 * <ul>
 * 	<li>Animation objects from within the panel (i.e. changing the order of an animation object),
 * 	<li>A layer identifier from the layer palette panel (i.e. adding a new layer from the palette to the current animation); and
 * 	<li>A layer definition file from an external source (i.e. adding a layer from an external file, not via the palette)
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
			if (support.isDataFlavorSupported(getAnimationObjectFlavor()))
			{
				object = moveAnimationObject(support);
			}
			
			if (support.isDataFlavorSupported(getLayerIdentifierFlavor()))
			{
				object = addLayerFromIdentifier(support);
			}
			
			if (support.isDataFlavorSupported(getFileListFlavor()))
			{
				object = addLayerFromDefinitionFile(support);
			}
		
		}
		catch (Exception e)
		{
			ExceptionLogger.logException(e);
			return false;
		}
		
		if (object == null)
		{
			return false;
		}
		
		notifyAnimationTreeChanged(object, support);
		return true;
		
	}

	/**
	 * Change the order within the current animation of the AnimationObject being transferred
	 */
	private AnimationObject moveAnimationObject(TransferSupport support) throws Exception
	{
		AnimationObject object = (AnimationObject)support.getTransferable().getTransferData(getAnimationObjectFlavor());
		animation.changeOrderOfAnimatableObject((Animatable)object, getNewIndexForAnimationObject(object, support));
		
		return object;
	}
	
	/**
	 * @return The new index for the animation object being transferred
	 */
	private int getNewIndexForAnimationObject(AnimationObject object, TransferSupport support)
	{
		int targetIndex = getDropIndex(support);
		int currentIndex = animation.getAnimatableObjects().indexOf(object);
		
		if (targetIndex <= currentIndex)
		{
			return targetIndex;
		}
		return targetIndex - 1;
	}

	/**
	 * Add an animation layer from the layer identifier being transferred, if one does not already exist
	 * <p/>
	 * Wires in an opacity parameter by default.
	 */
	private AnimationObject addLayerFromIdentifier(TransferSupport support) throws Exception
	{
		LayerIdentifier identifier = (LayerIdentifier)support.getTransferable().getTransferData(getLayerIdentifierFlavor());
		
		return addLayerFromIdentifier(identifier, getDropIndex(support));
	}

	/**
	 * Add an animation layer from a layer definition files, if they don't already exist
	 * <p/>
	 * Wires in an opacity parameter by default
	 */
	private AnimationObject addLayerFromDefinitionFile(TransferSupport support) throws Exception
	{
		List<?> files = (List<?>)support.getTransferable().getTransferData(getFileListFlavor());
		for (Object fileObject : files)
		{
			// Add the first file in the list
			if (fileObject instanceof File)
			{
				URL layerUrl = ((File)fileObject).toURI().toURL();
				LayerIdentifier identifier = LayerIdentifierFactory.createFromDefinition(layerUrl);
				
				return addLayerFromIdentifier(identifier, getDropIndex(support));
			}
		}
		
		return null;
	}
	
	/**
	 * Add an animation layer from the provided layer identifier if one does not already exist
	 * <p/>
	 * Wires in an opacity parameter by default.
	 */
	private AnimationObject addLayerFromIdentifier(LayerIdentifier identifier, int dropIndex)
	{
		if (animation.hasLayer(identifier))
		{
			return null;
		}
		
		Layer loadedLayer = AnimationLayerLoader.loadLayer(identifier);
		AnimatableLayer animatableLayer = new DefaultAnimatableLayer(loadedLayer);
		animatableLayer.addParameter(new LayerOpacityParameter(animation, loadedLayer));
		
		animation.addAnimatableObject(dropIndex, animatableLayer);
		
		return animatableLayer;
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
		return support.isDataFlavorSupported(getAnimationObjectFlavor()) ||
				support.isDataFlavorSupported(getLayerIdentifierFlavor()) ||
				support.isDataFlavorSupported(getFileListFlavor()); 
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
	
	private int getDropIndex(TransferSupport support)
	{
		return ((JTree.DropLocation)support.getDropLocation()).getChildIndex();
	}
}
