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
package au.gov.ga.worldwind.common.ui.lazytree;

import au.gov.ga.worldwind.common.ui.ClearableBasicTreeUI;

/**
 * Tree subclass that uses the {@link LazyTreeController} to lazily load tree
 * nodes as they are expanded. Also overrides the tree's UI class with a
 * {@link ClearableBasicTreeUI}, which allows forcing a relayout of the tree
 * structure.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LazyTree extends LoadingTree
{
	public LazyTree(LazyTreeModel model)
	{
		super(model);
		setUI(new ClearableBasicTreeUI());
		LazyTreeController controller = new LazyTreeController(this, model);
		addTreeWillExpandListener(controller);
	}

	@Override
	public ClearableBasicTreeUI getUI()
	{
		return (ClearableBasicTreeUI) super.getUI();
	}
}
