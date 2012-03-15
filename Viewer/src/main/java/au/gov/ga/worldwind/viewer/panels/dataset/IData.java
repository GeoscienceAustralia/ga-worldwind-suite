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

import java.net.URL;

import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;

/**
 * Super interface for all nodes in the dataset tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IData extends IIconItem
{
	/**
	 * @return The name (label) of this node
	 */
	String getName();

	/**
	 * @return The URL to the information/metadata for this node
	 */
	URL getInfoURL();

	/**
	 * @return Is this node a base node in the hierarchy of its children? When
	 *         adding layers from the dataset panel to the layers panel, the
	 *         parent hierarchy is also added, up until a base node.
	 */
	boolean isBase();

	/**
	 * Create a {@link MutableTreeNode} for this node in the provided tree
	 * model.
	 * 
	 * @param model
	 * @return New {@link MutableTreeNode} associated with this node.
	 */
	MutableTreeNode createMutableTreeNode(LazyTreeModel model);
}
