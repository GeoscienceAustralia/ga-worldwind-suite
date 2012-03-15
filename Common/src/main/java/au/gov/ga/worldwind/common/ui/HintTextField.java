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

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * A text field that displays a hint when no text is entered and the field is not
 * in focus.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class HintTextField extends JTextField implements FocusListener
{
	private static final Color HINT_COLOR = Color.LIGHT_GRAY;
	private static final long serialVersionUID = 5203008099174510305L;
	private String hint;
	
	public HintTextField(String hint)
	{
		this.hint = hint;
		addFocusListener(this);
		setForeground(HINT_COLOR);
	}

	@Override
	public void focusGained(FocusEvent e)
	{
		if (getText().isEmpty())
		{
			setForeground(UIManager.getColor("TextField.foreground"));
			setText("");
		}
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		if (getText().isEmpty())
		{
			setForeground(HINT_COLOR);
			setText(hint);
		}
	}
	
	@Override
	public String getText()
	{
		String typed = super.getText();
		return typed.equals(hint) ? "" : typed;
	}
	
}
