package util;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

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

	protected Document createDefaultModel()
	{
		return new DoubleDocument();
	}

	public Double getValue()
	{
		if (getText().length() == 0)
			return null;
		return value;
	}

	public void setValue(Double value)
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
				setText("");
			else if (precision != null)
				setText(String.format("%1." + precision + "f", value
						* getScale()));
			else
				setText(String.valueOf(value * getScale()));
			setting = false;
		}
	}

	private class DoubleDocument extends PlainDocument
	{
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException
		{
			String result = getText(0, offs) + str
					+ getText(offs, getLength() - offs);
			if (checkAndSet(result))
				super.insertString(offs, str, a);
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException
		{
			String result = getText(0, offs)
					+ getText(offs + len, getLength() - offs - len);
			if (checkAndSet(result))
				super.remove(offs, len);
		}

		private boolean checkAndSet(String result)
		{
			if (result.length() == 0)
				return true;

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
