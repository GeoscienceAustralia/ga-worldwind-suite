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

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.ChangeOfAnimationListener;
import au.gov.ga.worldwind.animator.ui.NameableTree;
import au.gov.ga.worldwind.animator.util.Icons;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * The parameter editor panel used to edit animation {@link Parameter}
 * curves on a 2D x-y time-value axis.
 * <p/>
 * Contains an animation tree for selecting parameters, and a {@link ParameterCurvePanel} used
 * to contain multiple {@link ParameterCurve}s.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ParameterEditor extends JFrame implements ChangeOfAnimationListener, ParameterTreeModel.ParameterSelectionListener
{
	private static final long serialVersionUID = 20101101L;

	private Animator targetApplication;
	
	private JSplitPane containerPane;
	private JToolBar toolbar;
	private JScrollPane leftScrollPane;
	private JScrollPane rightScrollPane;
	
	private JTree parameterTree;
	private ParameterTreeModel treeModel;

	private ParameterCurvePanel curvePanel;
	
	// Actions used in the parameter editor
	private BasicAction unselectAllAction;
	private BasicAction zoomAllToFitAction;
	private BasicAction zoomFrameToFitAction;
	private BasicAction zoomValueToFitAction;
	
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
	
		setupActions();
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
		curvePanel.destroy();
	}

	private void addSelectedCurves()
	{
		for (Parameter p : treeModel.getSelectedParameters())
		{
			curvePanel.addCurveForParameter(p);
		}
	}
	
	private void setupActions()
	{
		unselectAllAction = new BasicAction(getMessage(getUnselectAllMenuLabelKey()), Icons.uncheckall.getIcon());
		unselectAllAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				treeModel.unselectAllParameters();
			}
		});
		
		zoomAllToFitAction = new BasicAction(getMessage(getZoomAllMenuLabelKey()), Icons.zoomxy.getIcon());
		zoomAllToFitAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				curvePanel.zoomToFit();
			}
		});
		
		zoomFrameToFitAction = new BasicAction(getMessage(getZoomFrameMenuLabelKey()), Icons.zoomx.getIcon());
		zoomFrameToFitAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				curvePanel.zoomToFitFrame();
			}
		});
		
		zoomValueToFitAction = new BasicAction(getMessage(getZoomValueMenuLabelKey()), Icons.zoomy.getIcon());
		zoomValueToFitAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				curvePanel.zoomToFitValue();
			}
		});
	}
	
	private void setupSplitPane()
	{
		setLayout(new BorderLayout());
		
		treeModel = new ParameterTreeModel(targetApplication.getCurrentAnimation());
		treeModel.addParameterSelectionListener(this);
		parameterTree = new NameableTree(treeModel);
		parameterTree.setCellRenderer(new ParameterTreeRenderer(treeModel));
		parameterTree.setEditable(false);
		parameterTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		parameterTree.setToggleClickCount(-1);
		parameterTree.setActionMap(null); // Remove the default key bindings so our custom ones will work

		leftScrollPane = new JScrollPane(parameterTree);
		
		curvePanel = new ParameterCurvePanel(targetApplication);
		
		rightScrollPane = new JScrollPane(curvePanel.getJPanel());
		
		containerPane = new JSplitPane();
		containerPane.setDividerLocation(300);
		containerPane.setLeftComponent(leftScrollPane);
		containerPane.setRightComponent(rightScrollPane);
		
		toolbar = new JToolBar();
		toolbar.add(unselectAllAction);
		toolbar.addSeparator();
		toolbar.add(zoomAllToFitAction);
		toolbar.add(zoomFrameToFitAction);
		toolbar.add(zoomValueToFitAction);
		
		add(toolbar, BorderLayout.NORTH);
		add(containerPane, BorderLayout.CENTER);
	}
	
	@Override
	public void updateAnimation(Animation newAnimation)
	{
		treeModel = new ParameterTreeModel(newAnimation);
		treeModel.addParameterSelectionListener(this);
		parameterTree.setModel(treeModel);
		parameterTree.setCellRenderer(new ParameterTreeRenderer(treeModel));
		parameterTree.validate();
		
		removeAndDestroyAllCurves();
		
		treeModel.selectParameter(targetApplication.getCurrentAnimation().getCamera().getEyeLat());
		treeModel.selectParameter(targetApplication.getCurrentAnimation().getCamera().getEyeLon());
		treeModel.selectParameter(targetApplication.getCurrentAnimation().getCamera().getEyeElevation());
		
		if (isVisible())
		{
			repaint();
		}
	}

	@Override
	public void selectedStatusChanged(Parameter p)
	{
		if (!isVisible())
		{
			return;
		}
		if (treeModel.isSelected(p))
		{
			curvePanel.addCurveForParameter(p);
		}
		else
		{
			curvePanel.removeCurveForParameter(p);
		}
	}
	
	@Override
	public void selectedStatusesChanged()
	{
		removeAndDestroyAllCurves();
		addSelectedCurves();
		repaint();
	}
}
