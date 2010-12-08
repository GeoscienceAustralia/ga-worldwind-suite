package au.gov.ga.worldwind.animator.panels.animationbrowser;

import gov.nasa.worldwind.globes.ElevationModel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.elevation.AnimatableElevation;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent.Type;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A Tree model that is backed by an animation
 */
public class AnimationTreeModel implements TreeModel, AnimationEventListener
{
	/** The animation backing this tree model */
	private Animation animation;

	/** The list of registered tree model listeners */
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>(); 
	
	/**
	 * Constructor. Initialises the (mandatory) {@link Animation} instance.
	 * 
	 * @param animation The animation backing the tree model
	 */
	public AnimationTreeModel(Animation animation)
	{
		Validate.notNull(animation, "An animation must be provided");
		this.animation = animation;
		this.animation.addChangeListener(this);
	}
	
	@Override
	public Object getRoot()
	{
		return animation;
	}

	@Override
	public Object getChild(Object parent, int index)
	{
		if (parent instanceof Animation)
		{
			return new ArrayList<Animatable>(((Animation)parent).getAnimatableObjects()).get(index);
		}
		else if (parent instanceof AnimatableElevation)
		{
			return getAnimatableElevationTreeChild(index);
		}
		else if (parent instanceof Animatable)
		{
			return new ArrayList<Parameter>(((Animatable)parent).getParameters()).get(index);
		}
		
		return null;
	}

	@Override
	public int getChildCount(Object parent)
	{
		if (parent instanceof Animation)
		{
			return ((Animation)parent).getAnimatableObjects().size();
		}
		else if (parent instanceof AnimatableElevation)
		{
			return getAnimatableElevationTreeChildCount();
		}
		else if (parent instanceof Animatable)
		{
			return ((Animatable)parent).getParameters().size();
		}
		return 0;
	}

	@Override
	public boolean isLeaf(Object node)
	{
		if (node instanceof Animation)
		{
			return ((Animation)node).getAnimatableObjects().isEmpty();
		}
		else if (node instanceof AnimatableElevation)
		{
			return getAnimatableElevationTreeChildCount() == 0;
		}
		else if (node instanceof Animatable)
		{
			return ((Animatable)node).getParameters().isEmpty();
		}
		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		if (parent instanceof Animation)
		{
			return new ArrayList<Animatable>(((Animation)parent).getAnimatableObjects()).indexOf(child);
		}
		else if (parent instanceof AnimatableElevation)
		{
			return getAnimatableElevationTreeIndexOfChild(child);
		}
		else if (parent instanceof Animatable)
		{
			return new ArrayList<Parameter>(((Animatable)parent).getParameters()).indexOf(child);
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		this.listeners.add(l);

	}

	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		this.listeners.remove(l);
	}

	@Override
	public void receiveAnimationEvent(AnimationEvent event)
	{
		if (isStructuralEvent(event))
		{
			notifyTreeChanged(event.getRootCause().getValue());
		}
	}
	
	private boolean isStructuralEvent(AnimationEvent event)
	{
		if (event == null)
		{
			return false;
		}
		AnimationEvent rootCause = event.getRootCause();
		Object value = rootCause.getValue();
		return ((rootCause.isOfType(Type.ADD) || rootCause.isOfType(Type.REMOVE)) &&
				(value instanceof Parameter || value instanceof Animatable || value instanceof ElevationModel));
	}

	protected void notifyTreeChanged(Object source)
	{
		TreeModelEvent e = new TreeModelEvent(source, new Object[]{animation});
		for (TreeModelListener listener : listeners)
		{
			listener.treeStructureChanged(e);
		}
	}
	
	/**
	 * Animatable elevation is a special case - list elevation models first
	 */
	private Object getAnimatableElevationTreeChild(int index)
	{
		AnimatableElevation animatableElevation = animation.getAnimatableElevation();
		int elevationModelCount = animatableElevation.getElevationModelIdentifiers().size();
		if (index < elevationModelCount)
		{
			return new AnimationElevationModelIdentifier(animatableElevation.getElevationModelIdentifiers().get(index));
		}
		return new ArrayList<Parameter>(animatableElevation.getParameters()).get(index - elevationModelCount);
	}

	private int getAnimatableElevationTreeChildCount()
	{
		return animation.getAnimatableElevation().getParameters().size() + animation.getAnimatableElevation().getElevationModelIdentifiers().size();
	}
	
	private int getAnimatableElevationTreeIndexOfChild(Object child)
	{
		AnimatableElevation animatableElevation = animation.getAnimatableElevation();
		int elevationModelCount = animatableElevation.getElevationModelIdentifiers().size();
		if (child instanceof AnimationElevationModelIdentifier)
		{
			return animatableElevation.getElevationModelIdentifiers().indexOf(((AnimationElevationModelIdentifier)child).getIdentifier());
		}
		return new ArrayList<Parameter>(animatableElevation.getParameters()).indexOf(child) + elevationModelCount;
	}
	
	/**
	 * A wrapper class that makes an {@link ElevationModelIdentifier} appear as an {@link AnimationObject}
	 */
	private static class AnimationElevationModelIdentifier implements ElevationModelIdentifier, AnimationObject
	{
		private ElevationModelIdentifier identifier;
		
		public AnimationElevationModelIdentifier(ElevationModelIdentifier identifier)
		{
			Validate.notNull(identifier, "An identifier is required");
			this.identifier = identifier;
		}
		
		@Override
		public String getName()
		{
			return identifier.getName();
		}

		@Override
		public void setName(String name)
		{
			// Do nothing
		}
		
		public ElevationModelIdentifier getIdentifier()
		{
			return identifier;
		}

		@Override
		public String getLocation()
		{
			return identifier.getLocation();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return identifier.equals(obj);
		}
		
		@Override
		public int hashCode()
		{
			return identifier.hashCode();
		}
		
	}
}
