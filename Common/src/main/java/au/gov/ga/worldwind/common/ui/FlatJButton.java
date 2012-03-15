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

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * {@link JButton} subclass that is rendered as flat (no border). Also adds the
 * ability to restrict the width of the button to the height, to ensure a square
 * button.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FlatJButton extends JButton
{
	private boolean hasFocus = false;
	private boolean mouseInside = false;

	public FlatJButton(Icon icon)
	{
		super(icon);
		init();
	}

	public FlatJButton(String text)
	{
		super(text);
		init();
	}

	public FlatJButton(String text, Icon icon)
	{
		super(text, icon);
		init();
	}

	/**
	 * Restrict the width of this button to it's preferred height, so that it is
	 * square.
	 */
	public void restrictSize()
	{
		Dimension size = getPreferredSize();
		size.width = size.height;
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
	}

	private void init()
	{
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFocusPainted(false);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				mouseInside = true;
				setContentAreaFilled(true);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				mouseInside = false;
				setContentAreaFilled(hasFocus);
			}
		});

		addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				hasFocus = true;
				setContentAreaFilled(true);
			}

			@Override
			public void focusLost(FocusEvent e)
			{
				hasFocus = false;
				setContentAreaFilled(mouseInside);
			}
		});
	}

	@Override
	public void setContentAreaFilled(boolean b)
	{
		super.setContentAreaFilled(b && isEnabled());
	}
}
