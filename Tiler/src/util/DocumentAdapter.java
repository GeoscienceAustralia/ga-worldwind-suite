package util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DocumentAdapter implements DocumentListener
{
	public void changedUpdate(DocumentEvent e)
	{
		anyChange(e);
	}

	public void insertUpdate(DocumentEvent e)
	{
		anyChange(e);
	}

	public void removeUpdate(DocumentEvent e)
	{
		anyChange(e);
	}

	public void anyChange(DocumentEvent e)
	{
	}
}
