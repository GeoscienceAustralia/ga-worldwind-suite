/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.ui.parametereditor;

import gov.nasa.worldwind.globes.ElevationModel;

import java.util.ArrayList;
import java.util.Collections;
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
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A tree model that only displays editable parameters
 * contained within the current animation
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
class ParameterTreeModel implements TreeModel, AnimationEventListener
{
	private Animation animation;
	
	private List<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>(); 
	private List<ParameterSelectionListener> selectionListeners = new ArrayList<ParameterSelectionListener>();
	
	private List<Parameter> selectedParameters = new ArrayList<Parameter>();
	
	public ParameterTreeModel(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
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
			return getAnimatablesWithParameters((Animation)parent).get(index);
		}
		if (parent instanceof Animatable)
		{
			return getEditableParameters((Animatable)parent).get(index);
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
			return getEditableParameters((Animatable)parent).indexOf(child);
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		if (treeListeners.contains(l))
		{
			return;
		}
		this.treeListeners.add(l);

	}

	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		this.treeListeners.remove(l);
	}
	
	public void addParameterSelectionListener(ParameterSelectionListener l)
	{
		if (selectionListeners.contains(l))
		{
			return;
		}
		selectionListeners.add(l);
	}
	
	public void removeParameterSelectionListener(ParameterSelectionListener l)
	{
		selectionListeners.remove(l);
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
		for (TreeModelListener listener : treeListeners)
		{
			listener.treeStructureChanged(e);
		}
	}
	
	private List<Animatable> getAnimatablesWithParameters(Animation animation)
	{
		List<Animatable> result = new ArrayList<Animatable>();
		for (Animatable animatable : animation.getAnimatableObjects())
		{
			if (!getEditableParameters(animatable).isEmpty())
			{
				result.add(animatable);
			}
		}
		return result;
	}
	
	private ArrayList<Parameter> getEditableParameters(Animatable parent)
	{
		return new ArrayList<Parameter>(((Animatable)parent).getParameters());
	}
	
	public boolean isSelected(Parameter p)
	{
		return selectedParameters.contains(p);
	}
	
	public void selectParameter(Parameter p)
	{
		selectedParameters.add(p);
		notifySelectedStatusChanged(p);
	}
	
	public void unselectParameter(Parameter p)
	{
		selectedParameters.remove(p);
		notifySelectedStatusChanged(p);
	}

	public void unselectAllParameters()
	{
		selectedParameters.clear();
		notifySelectedStatusesChanged();
	}
	
	public List<Parameter> getSelectedParameters()
	{
		return Collections.unmodifiableList(selectedParameters);
	}

	public void toggleParameterSelection(Parameter p)
	{
		if (isSelected(p))
		{
			unselectParameter(p);
		}
		else
		{
			selectParameter(p);
		}
	}
	
	private void notifySelectedStatusChanged(Parameter p)
	{
		for (int i = selectionListeners.size() - 1; i >= 0; i--)
		{
			selectionListeners.get(i).selectedStatusChanged(p);
		}
	}
	
	private void notifySelectedStatusesChanged()
	{
		for (int i = selectionListeners.size() - 1; i >= 0; i--)
		{
			selectionListeners.get(i).selectedStatusesChanged();
		}
	}
	
	/**
	 * An interface for classes that want to be notified of changes to a parameter's 'selection' status
	 */
	public static interface ParameterSelectionListener
	{
		void selectedStatusChanged(Parameter p);
		void selectedStatusesChanged();
	}

}
