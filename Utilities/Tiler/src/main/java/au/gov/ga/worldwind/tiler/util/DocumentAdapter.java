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
