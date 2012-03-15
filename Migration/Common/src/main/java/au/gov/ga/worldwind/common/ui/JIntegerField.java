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

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * {@link JTextField} subclass that only allows the user to input valid integer
 * values. Also allows limiting the input to positive values.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class JIntegerField extends JTextField
{
	private Integer value;
	private boolean positive;
	private boolean setting = false, updating = false;

	public JIntegerField(Integer value)
	{
		super();
		setValue(value);
	}

	public JIntegerField(boolean isPositive, Integer initialValue)
	{
		setValue(initialValue);
		setPositive(isPositive);
	}

	@Override
	protected Document createDefaultModel()
	{
		return new IntegerDocument();
	}

	public Integer getValue()
	{
		if (getText().length() == 0)
			return null;
		return value;
	}

	public void setValue(Integer value)
	{
		this.value = value;
		if (!updating)
			updateText();
	}

	public boolean isPositive()
	{
		return positive;
	}

	public void setPositive(boolean positive)
	{
		this.positive = positive;
	}

	private void updateText()
	{
		if (!setting)
		{
			setting = true;
			if (value == null)
				setText("");
			else
				setText(String.valueOf(value));
			setting = false;
		}
	}

	private class IntegerDocument extends PlainDocument
	{
		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
		{
			String result = getText(0, offs) + str + getText(offs, getLength() - offs);
			if (checkAndSet(result))
				super.insertString(offs, str, a);
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException
		{
			String result = getText(0, offs) + getText(offs + len, getLength() - offs - len);
			if (checkAndSet(result))
				super.remove(offs, len);
		}

		private boolean checkAndSet(String result)
		{
			if (result.length() == 0)
				return true;

			boolean doit = true;
			Integer value = null;
			try
			{
				value = Integer.valueOf(result);
				doit = !isPositive() || value >= 0;
			}
			catch (Exception e)
			{
				doit = false;
			}
			if (doit && !setting)
			{
				updating = true;
				setValue(value);
				updating = false;
			}
			return doit;
		}
	}
}
