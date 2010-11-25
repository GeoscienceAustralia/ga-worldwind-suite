package au.gov.ga.worldwind.common.ui;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * A simple extension of the {@link JTextField} to make it appear like a label,
 * but allow text selection via the mouse
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