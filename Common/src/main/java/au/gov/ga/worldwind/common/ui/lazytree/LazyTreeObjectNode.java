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

import javax.swing.tree.MutableTreeNode;

/**
 * {@link LazyTreeNode} concrete subclass that takes {@link ITreeObject}s as its
 * user object. If the user object is an instance of {@link ILazyTreeObject},
 * it's children are loaded lazily when expanded.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LazyTreeObjectNode extends LazyTreeNode
{
	public LazyTreeObjectNode(ITreeObject userObject, LazyTreeModel model)
	{
		super(userObject, model);
		if (userObject instanceof ILazyTreeObject)
		{
			//wait to load children later
		}
		else
		{
			setChildren(userObject.getChildren(model));
		}
	}

	@Override
	public MutableTreeNode[] loadChildren(LazyTreeModel model) throws Exception
	{
		if (getUserObject() instanceof ILazyTreeObject)
		{
			ILazyTreeObject object = (ILazyTreeObject) getUserObject();
			object.load();
			return object.getChildren(model);
		}

		setAllowsChildren(false);
		return null;
	}

	public void refreshChildren(LazyTreeModel model)
	{
		if (getUserObject() instanceof ITreeObject)
		{
			ITreeObject userObject = (ITreeObject) getUserObject();
			reset();
			if (userObject instanceof ILazyTreeObject)
			{
			}
			else
			{
				setChildren(userObject.getChildren(model));
			}
		}
	}
}
