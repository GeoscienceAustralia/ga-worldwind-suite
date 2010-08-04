/**
 * L2FProd.com Common Components 7.3 License.
 *
 * Copyright 2005-2007 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.ga.worldwind.components.collapsiblesplit.l2fprod;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * An empty icon with arbitrary width and height.
 */
public final class EmptyIcon implements Icon
{

	private int width;
	private int height;

	public EmptyIcon()
	{
		this(0, 0);
	}

	public EmptyIcon(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	@Override
	public int getIconHeight()
	{
		return height;
	}

	@Override
	public int getIconWidth()
	{
		return width;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
	}
}
