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
package au.gov.ga.worldwind.common.ui.resizabletoolbar;

import java.awt.Dimension;

import javax.swing.JToolBar;

/**
 * {@link JToolBar} subclass that uses the {@link WrapLayout} layout so that it
 * can be resized (icons are wrapped to the next line if too small).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ResizableToolBar extends JToolBar
{
	public ResizableToolBar()
	{
		super(JToolBar.HORIZONTAL);
		setLayout(new WrapLayout(WrapLayout.LEFT, 0, 0));
		setFloatable(false);
	}

	@Override
	public void addSeparator()
	{
		//hack, as separator doesn't report it's size, so FlowLayout allocates zero height
		super.addSeparator(new Dimension(8, 25));
	}
}
