package au.gov.ga.worldwind.animator.ui.parametereditor;

import gov.nasa.worldwind.globes.ElevationModel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent.Type;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A tree model that only displays editable parameters
 * contained within the current animation
 */
class ParameterTreeModel implements TreeModel, AnimationEventListener
{
	private Animation animation;
	
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>(); 
	
	public ParameterTreeModel(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
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
			return getAnimatablesWithParameters((Animation)parent).get(index);
		}
		if (parent instanceof Animatable)
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
			return getAnimatablesWithParameters((Animation)parent).size();
		}
		else if (parent instanceof Animatable)
		{
			return (((Animatable)parent).getParameters()).size();
		}
		return 0;
	}

	@Override
	public boolean isLeaf(Object node)
	{
		if (node instanceof Animation)
		{
			return getAnimatablesWithParameters((Animation)node).isEmpty();
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
			return getAnimatablesWithParameters((Animation)parent).indexOf(child);
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
	
	private List<Animatable> getAnimatablesWithParameters(Animation animation)
	{
		List<Animatable> result = new ArrayList<Animatable>();
		for (Animatable animatable : animation.getAnimatableObjects())
		{
			if (!animatable.getParameters().isEmpty())
			{
				result.add(animatable);
			}
		}
		return result;
	}
}