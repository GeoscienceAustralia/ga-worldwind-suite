package settings;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class JIntegerField extends JTextField
{
	public JIntegerField(int value)
	{
		super();
		if(value >= 0)
			setText(String.valueOf(value));
	}

	protected Document createDefaultModel()
	{
		return new IntegerDocument();
	}

	public int getValue()
	{
		if(getText().length() == 0)
			return -1;
		return Integer.parseInt(getText());
	}

	static class IntegerDocument extends PlainDocument
	{
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException
		{
			String result = getText(0, offs) + str
					+ getText(offs, getLength() - offs);
			boolean doit = true;
			try
			{
				Integer.parseInt(result);
			}
			catch (Exception e)
			{
				doit = false;
			}

			if (doit)
			{
				super.insertString(offs, str, a);
			}
		}
	}
}
