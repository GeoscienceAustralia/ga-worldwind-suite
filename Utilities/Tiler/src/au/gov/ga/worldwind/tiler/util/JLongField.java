package au.gov.ga.worldwind.tiler.util;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class JLongField extends JTextField
{
	private Long value;
	private boolean positive;
	private boolean setting = false, updating = false;

	public JLongField(Long value)
	{
		super();
		setValue(value);
	}

	protected Document createDefaultModel()
	{
		return new LongDocument();
	}

	public Long getValue()
	{
		if (getText().length() == 0 || getText().equals("-"))
			return null;
		return value;
	}

	public void setValue(Long value)
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

	private class LongDocument extends PlainDocument
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
			if (result.length() == 0 || (result.equals("-") && !isPositive()))
				return true;

			boolean doit = true;
			Long value = null;
			try
			{
				value = Long.valueOf(result);
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
