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
package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.WorldWindow;

import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;

import au.gov.ga.worldwind.common.ui.ClearableBasicTreeUI;
import au.gov.ga.worldwind.common.ui.SwingUtil;
import au.gov.ga.worldwind.common.ui.lazytree.LoadingTree;
import au.gov.ga.worldwind.common.util.Loader.LoadingListener;

/**
 * {@link JTree} used for rendering a hierarchy of layers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTree extends LoadingTree implements LoadingListener
{
	private final LayerEnabler enabler;
	private final ClearableBasicTreeUI ui;

	public LayerTree(WorldWindow wwd, INode root)
	{
		super();
		ui = new ClearableBasicTreeUI();
		setUI(ui);

		enabler = new LayerEnabler(this, wwd);
		LayerTreeModel model = new LayerTreeModel(this, root, enabler); //constructor calls this.setModel()

		setCellRenderer(new LayerCellRenderer());
		setCellEditor(new LayerTreeCellEditor(this));
		setEditable(true);
		setShowsRootHandles(true);
		setRootVisible(false);
		setRowHeight(0);
		addTreeExpansionListener(model);
		model.expandNodes();
	}

	public LayerEnabler getEnabler()
	{
		return enabler;
	}

	public LayerTreeModel getLayerModel()
	{
		return (LayerTreeModel) getModel();
	}

	@Override
	public ClearableBasicTreeUI getUI()
	{
		if (super.getUI() != this.ui)
			setUI(this.ui);
		return (ClearableBasicTreeUI) super.getUI();
	}

	public LayerCellRenderer getLayerCellRenderer()
	{
		return (LayerCellRenderer) getCellRenderer();
	}

	private static class LayerTreeCellEditor extends DefaultTreeCellEditor
	{
		public LayerTreeCellEditor(JTree tree)
		{
			super(tree, null);
		}

		@Override
		protected boolean shouldStartEditingTimer(EventObject event)
		{
			//only allow editing with edit button, not with mouse click
			return false;
		}
	}

	@Override
	public void loadingStateChanged(boolean isLoading)
	{
		repaint();
	}

	public void relayoutOnEDT()
	{
		SwingUtil.invokeLaterTaskOnEDT(new Runnable()
		{
			@Override
			public void run()
			{
				getUI().relayout();
				repaint();
			}
		});
	}
}
