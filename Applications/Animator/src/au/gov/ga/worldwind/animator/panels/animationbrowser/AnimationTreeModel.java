package au.gov.ga.worldwind.animator.panels.animationbrowser;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class AnimationTreeModel implements TreeModel, ChangeListener
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
		else if (node instanceof Animatable)
		{
			return ((Animatable)node).getParameters().isEmpty();
		}
		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		// Do nothing - shouldn't allow tree modifications

	}

	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		if (parent instanceof Animation)
		{
			return new ArrayList<Animatable>(((Animation)parent).getAnimatableObjects()).indexOf(child);
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
	public void stateChanged(ChangeEvent e)
	{
		// If the change came from a parameter or animatable object,
		// fire a potential tree change
		if ((e.getSource() instanceof Animatable) || (e.getSource() instanceof Parameter) || (e.getSource() instanceof Animation))
		{
			notifyTreeChanged(e.getSource());
		}

	}
	
	protected void notifyTreeChanged(Object source)
	{
		TreeModelEvent e = new TreeModelEvent(source, new Object[]{animation});
		for (TreeModelListener listener : listeners)
		{
			listener.treeStructureChanged(e);
		}
	}

}
