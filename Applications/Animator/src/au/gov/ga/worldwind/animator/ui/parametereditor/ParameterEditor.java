package au.gov.ga.worldwind.animator.ui.parametereditor;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getParameterEditorWindowLabelKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.ChangeOfAnimationListener;
import au.gov.ga.worldwind.animator.ui.NameableTree;
import au.gov.ga.worldwind.animator.ui.parametereditor.ParameterCurve.ParameterCurveListener;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * The parameter editor panel used to edit individual animation {@link Parameter}
 * curves on a 2D x-y time-value axis.
 */
public class ParameterEditor extends JFrame implements ChangeOfAnimationListener, ParameterCurveListener
{
	private static final long serialVersionUID = 20101101L;

	private Animator targetApplication;
	
	private JSplitPane containerPane;
	private JToolBar toolbar;
	private JScrollPane leftScrollPane;
	private JScrollPane rightScrollPane;
	
	private JTree parameterTree;
	private TreeModel treeModel;

	private JPanel curvePanel;
	
	private List<ParameterCurve> curves = new ArrayList<ParameterCurve>();
	
	private AtomicBoolean settingBounds = new AtomicBoolean(false);
	
	public ParameterEditor(Animator targetApplication)
	{
		Validate.notNull(targetApplication, "A Animator instance must be provided");
		this.targetApplication = targetApplication;
		
		this.setTitle(getMessage(getParameterEditorWindowLabelKey()));
		this.setSize(new Dimension(640, 480));
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

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		
		if (!visible)
		{
			removeAndDestroyAllCurves();
		}
		else
		{
			addSelectedCurves();
		}
	}

	private void removeAndDestroyAllCurves()
	{
		for (ParameterCurve curve : curves)
		{
			curvePanel.remove(curve);
			curve.destroy();
		}
		curvePanel.removeAll();
		curves.clear();
		curvePanel.validate();
	}
	
	private void addSelectedCurves()
	{
		curves.add(new ParameterCurve(targetApplication.getCurrentAnimation().getCamera().getEyeLat()));
		curves.add(new ParameterCurve(targetApplication.getCurrentAnimation().getCamera().getEyeLon()));
		curves.add(new ParameterCurve(targetApplication.getCurrentAnimation().getCamera().getEyeElevation()));
		
		curvePanel.add(Box.createVerticalStrut(10));
		for (ParameterCurve curve : curves)
		{
			curvePanel.add(curve);
			curvePanel.add(Box.createVerticalStrut(10));
			curve.addCurveListener(this);
		}
		
		curvePanel.validate();
		curvePanel.repaint();
	}
	
	private void setupSplitPane()
	{
		setLayout(new BorderLayout());
		
		treeModel = new ParameterTreeModel(targetApplication.getCurrentAnimation());
		parameterTree = new NameableTree(treeModel);
		parameterTree.setCellRenderer(new ParameterTreeRenderer());
		parameterTree.setEditable(false);
		parameterTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		parameterTree.setToggleClickCount(-1);
		parameterTree.setActionMap(null); // Remove the default key bindings so our custom ones will work

		leftScrollPane = new JScrollPane(parameterTree);
		
		curvePanel = new JPanel();
		curvePanel.setLayout(new BoxLayout(curvePanel, BoxLayout.Y_AXIS));
		
		rightScrollPane = new JScrollPane(curvePanel);
		
		containerPane = new JSplitPane();
		containerPane.setDividerLocation(300);
		containerPane.setLeftComponent(leftScrollPane);
		containerPane.setRightComponent(rightScrollPane);
		
		toolbar = new JToolBar();
		
		add(toolbar, BorderLayout.NORTH);
		add(containerPane, BorderLayout.CENTER);
	}
	
	@Override
	public void updateAnimation(Animation newAnimation)
	{
		treeModel = new ParameterTreeModel(newAnimation);
		parameterTree.setModel(treeModel);
		parameterTree.validate();
		removeAndDestroyAllCurves();
		if (isVisible())
		{
			addSelectedCurves();
		}
	}
	
	@Override
	public void curveBoundsChanged(ParameterCurve source, ParameterCurveBounds newBounds)
	{
		if (settingBounds.get())
		{
			return;
		}
		settingBounds.set(true);
		for (ParameterCurve curve : curves)
		{
			if (curve == source)
			{
				continue;
			}
			
			if (newBounds.getMinFrame() == curve.getCurveBounds().getMinFrame() && 
					newBounds.getMaxFrame() == curve.getCurveBounds().getMaxFrame())
			{
				continue;
			}
			
			curve.setCurveFrameBounds(newBounds.getMinFrame(), newBounds.getMaxFrame());
		}
		settingBounds.set(false);
		repaint();
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D)g;
		
		paintCurrentFrameLine(g2);
	}

	/** Paint a line at the current animation frame through all parameter curves */
	private void paintCurrentFrameLine(Graphics2D g2)
	{
		// TODO Auto-generated method stub
		
	}
	
}
