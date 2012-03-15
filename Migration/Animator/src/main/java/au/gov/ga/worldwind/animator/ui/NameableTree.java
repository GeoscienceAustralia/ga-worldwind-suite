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
package au.gov.ga.worldwind.animator.ui;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import au.gov.ga.worldwind.animator.util.Nameable;

/**
 * An extension of the {@link DefaultMutableTreeNode} that renders a {@link Nameable} object's name
 * as the text value of the tree nodes. Used in conjunction with the {@link AnimationTreeRenderer}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NameableTree extends JTree
{
	private static final long serialVersionUID = 20100907L;
	
	public NameableTree(TreeModel model)
	{
		super(model);
	}

	@Override
	public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		if (value instanceof Nameable)
		{
			return ((Nameable)value).getName();
		}
		
		return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
	}
}
