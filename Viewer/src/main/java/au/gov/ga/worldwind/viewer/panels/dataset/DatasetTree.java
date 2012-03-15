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
package au.gov.ga.worldwind.viewer.panels.dataset;

import au.gov.ga.worldwind.common.ui.ClearableBasicTreeUI;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTree;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;

/**
 * {@link LazyTree} subclass with a custom cell renderer that renders dataset
 * nodes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetTree extends LazyTree
{
	public DatasetTree(LazyTreeModel treeModel)
	{
		super(treeModel);

		setUI(new ClearableBasicTreeUI());
		setCellRenderer(new DatasetCellRenderer());

		setRootVisible(false);
		setShowsRootHandles(true);
		setRowHeight(0);
	}

	@Override
	public LazyTreeModel getModel()
	{
		return (LazyTreeModel) super.getModel();
	}

	@Override
	public ClearableBasicTreeUI getUI()
	{
		return (ClearableBasicTreeUI) super.getUI();
	}

	public DatasetCellRenderer getDatasetCellRenderer()
	{
		return (DatasetCellRenderer) getCellRenderer();
	}
}
