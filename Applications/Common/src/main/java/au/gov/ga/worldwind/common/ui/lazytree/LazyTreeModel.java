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
import javax.swing.tree.TreeModel;

/**
 * The model interface to use for {@link LazyTree}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface LazyTreeModel extends TreeModel
{
	/**
	 * Remove the given tree node from it's parent.
	 * 
	 * @param childAt
	 *            Child to remove
	 */
	void removeNodeFromParent(MutableTreeNode childAt);

	/**
	 * Insert the given tree node into the given parent's children at the
	 * specified index.
	 * 
	 * @param newChild
	 *            Child to add
	 * @param parent
	 *            Parent to add child to
	 * @param index
	 *            Index at which to insert
	 */
	void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index);
}
