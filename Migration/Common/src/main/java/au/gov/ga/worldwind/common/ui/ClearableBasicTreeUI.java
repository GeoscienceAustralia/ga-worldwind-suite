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
package au.gov.ga.worldwind.common.ui;

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

/**
 * {@link BasicTreeUI} subclass that adds the ability to relayout the tree (or
 * certain subpaths).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ClearableBasicTreeUI extends BasicTreeUI
{
	private boolean relayout = false;
	private Set<TreePath> invalidatedPaths = new HashSet<TreePath>();

	/**
	 * Relayout the specified tree path.
	 * 
	 * @param path
	 *            Path to relayout
	 */
	public void relayout(TreePath path)
	{
		invalidatedPaths.add(path);
	}

	/**
	 * Relayout the tree associated with this UI.
	 */
	public void relayout()
	{
		relayout = true;
	}

	@Override
	public void paint(Graphics g, JComponent c)
	{
		if (relayout || !invalidatedPaths.isEmpty())
		{
			if (relayout)
			{
				treeState.invalidateSizes();
			}
			else
			{
				for (TreePath path : invalidatedPaths)
				{
					treeState.invalidatePathBounds(path);
				}
			}

			updateSize();

			relayout = false;
			invalidatedPaths.clear();
		}

		super.paint(g, c);
	}
}
