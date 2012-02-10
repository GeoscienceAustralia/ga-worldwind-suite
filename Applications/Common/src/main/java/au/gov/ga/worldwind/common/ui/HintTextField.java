package au.gov.ga.worldwind.common.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * A text field that displays a hint when no text is entered and the field is not
 * in focus.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class HintTextField extends JTextField implements FocusListener
{
	private static final Color HINT_COLOR = Color.LIGHT_GRAY;
	private static final long serialVersionUID = 5203008099174510305L;
	private String hint;
	
	public HintTextField(String hint)
	{
		this.hint = hint;
		addFocusListener(this);
		setForeground(HINT_COLOR);
	}

	@Override
	public void focusGained(FocusEvent e)
	{
		if (getText().isEmpty())
		{
			setForeground(UIManager.getColor("TextField.foreground"));
			setText("");
		}
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		if (getText().isEmpty())
		{
			setForeground(HINT_COLOR);
			setText(hint);
		}
	}
	
	@Override
	public String getText()
	{
		String typed = super.getText();
		return typed.equals(hint) ? "" : typed;
	}
	
}