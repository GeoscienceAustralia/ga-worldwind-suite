package au.gov.ga.worldwind.animator.ui.parametereditor;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getParameterEditorWindowLabelKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.globes.ElevationModel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent.Type;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.ui.NameableTree;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * The parameter editor panel used to edit individual animation {@link Parameter}
 * curves on a 2D x-y time-value axis.
 */
public class ParameterEditor extends JFrame
{
	private static final long serialVersionUID = 20101101L;

	private Animator targetApplication;
	
	private JSplitPane containerPane;
	private JScrollPane leftScrollPane;
	private JScrollPane rightScrollPane;
	
	private JTree parameterTree;
	
	public ParameterEditor(Animator targetApplication)
	{
		Validate.notNull(targetApplication, "A Animator instance must be provided");
		this.targetApplication = targetApplication;
		
		this.setTitle(getMessage(getParameterEditorWindowLabelKey()));
		
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				ParameterEditor.this.targetApplication.setParameterEditorVisible(false);
			}
		});
		
		setupSplitPane();
	}

	private void setupSplitPane()
	{
		TreeModel model = new ParameterTreeModel(targetApplication.getCurrentAnimation());
		parameterTree = new NameableTree(model);
		parameterTree.setEditable(false);
		parameterTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		parameterTree.setToggleClickCount(-1);
		parameterTree.setActionMap(null); // Remove the default key bindings so our custom ones will work

		leftScrollPane = new JScrollPane(parameterTree);
		
		rightScrollPane = new JScrollPane(new JPanel());
		
		containerPane = new JSplitPane();
		containerPane.setDividerLocation(300);
		containerPane.setLeftComponent(leftScrollPane);
		containerPane.setRightComponent(rightScrollPane);
		
		add(containerPane);
	}
	
	/**
	 * A tree model that only displays editable parameters
	 * contained within the current animation
	 */
	private static class ParameterTreeModel implements TreeModel, AnimationEventListener
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
	
}
