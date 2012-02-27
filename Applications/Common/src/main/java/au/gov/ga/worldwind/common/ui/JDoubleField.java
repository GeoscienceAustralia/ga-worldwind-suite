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
 * {@link JTextField} subclass that only allows the user to input valid double
 * values. Also allows limiting the input to positive values.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class JDoubleField extends JTextField
{
	private Double value;
	private boolean positive;
	private Integer precision;
	private double scale = 1;
	private boolean setting = false, updating = false;

	public JDoubleField(Double value, int precision)
	{
		super();
		this.precision = precision;
		setValue(value);
	}

	public JDoubleField(Double value)
	{
		super();
		this.precision = null;
		setValue(value);
	}

	@Override
	protected Document createDefaultModel()
	{
		return new DoubleDocument();
	}

	public Double getValue()
	{
		if (getText().length() == 0)
		{
			return null;
		}
		return value;
	}

	public void setValue(Double value)
	{
		this.value = value;
		if (!updating)
		{
			updateText();
		}
	}

	public boolean isPositive()
	{
		return positive;
	}

	public void setPositive(boolean positive)
	{
		this.positive = positive;
	}

	public double getScale()
	{
		return scale;
	}

	public void setScale(double scale)
	{
		this.scale = scale;
		updateText();
	}

	private void updateText()
	{
		if (!setting)
		{
			setting = true;
			if (value == null)
			{
				setText("");
			}
			else if (precision != null)
			{
				setText(String.format("%1." + precision + "f", value * getScale()));
			}
			else
			{
				setText(String.valueOf(value * getScale()));
			}
			setting = false;
		}
	}

	private class DoubleDocument extends PlainDocument
	{
		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
		{
			String result = getText(0, offs) + str + getText(offs, getLength() - offs);
			if (isValidNegativeSign(result) || checkAndSet(result))
			{
				super.insertString(offs, str, a);
			}
		}

		private boolean isValidNegativeSign(String result)
		{
			return result.trim().equals("-") && !isPositive();
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException
		{
			String result = getText(0, offs) + getText(offs + len, getLength() - offs - len);
			if (isValidNegativeSign(result) || checkAndSet(result))
			{
				super.remove(offs, len);
			}
		}

		private boolean checkAndSet(String result)
		{
			if (result.length() == 0)
			{
				return true;
			}

			boolean doit = true;
			Double value = null;
			try
			{
				value = Double.valueOf(result);
				doit = !isPositive() || value >= 0;
			}
			catch (Exception e)
			{
				doit = false;
			}
			if (doit && !setting)
			{
				updating = true;
				setValue(value / getScale());
				updating = false;
			}
			return doit;
		}
	}
}
