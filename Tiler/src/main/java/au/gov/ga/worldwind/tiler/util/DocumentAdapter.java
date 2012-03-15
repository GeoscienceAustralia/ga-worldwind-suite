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
package au.gov.ga.worldwind.tiler.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Abstract implementation of the {@link DocumentListener} interface which
 * provides an simple function that notifies of any change to the document.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class DocumentAdapter implements DocumentListener
{
	public final void changedUpdate(DocumentEvent e)
	{
		anyChange(e);
	}

	public final void insertUpdate(DocumentEvent e)
	{
		anyChange(e);
	}

	public final void removeUpdate(DocumentEvent e)
	{
		anyChange(e);
	}

	/**
	 * Something in the document changed.
	 * 
	 * @param e
	 */
	public abstract void anyChange(DocumentEvent e);
}
