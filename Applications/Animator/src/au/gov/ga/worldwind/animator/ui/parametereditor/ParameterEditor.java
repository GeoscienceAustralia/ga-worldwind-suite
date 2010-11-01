package au.gov.ga.worldwind.animator.ui.parametereditor;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getParameterEditorWindowLabelKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.ChangeOfAnimationListener;
import au.gov.ga.worldwind.animator.ui.NameableTree;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * The parameter editor panel used to edit individual animation {@link Parameter}
 * curves on a 2D x-y time-value axis.
 */
public class ParameterEditor extends JFrame implements ChangeOfAnimationListener
{
	private static final long serialVersionUID = 20101101L;

	private Animator targetApplication;
	
	private JSplitPane containerPane;
	private JScrollPane leftScrollPane;
	private JScrollPane rightScrollPane;
	
	private JTree parameterTree;

	private TreeModel treeModel;
	
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
		treeModel = new ParameterTreeModel(targetApplication.getCurrentAnimation());
		parameterTree = new NameableTree(treeModel);
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
	
	@Override
	public void updateAnimation(Animation newAnimation)
	{
		treeModel = new ParameterTreeModel(newAnimation);
		parameterTree.setModel(treeModel);
		parameterTree.validate();
	}
	
}
