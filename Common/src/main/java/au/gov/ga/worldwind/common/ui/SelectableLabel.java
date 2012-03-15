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

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * A simple extension of the {@link JTextField} to make it appear like a label,
 * but allow text selection via the mouse
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class SelectableLabel extends JTextArea
{
	private static final long serialVersionUID = 20101123L;

	public SelectableLabel(String value)
	{
		super(value);
		setEditable(false);
		//setBorder(null);
		setForeground(UIManager.getColor("Label.foreground"));
		setFont(UIManager.getFont("Label.font"));
		setOpaque(false);
		setCaretPosition(0); // Required to prevent first part of the first character being chopped
	}
}
