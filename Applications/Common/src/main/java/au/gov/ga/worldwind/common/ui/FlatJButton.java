package au.gov.ga.worldwind.common.ui;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * {@link JButton} subclass that is rendered as flat (no border). Also adds the
 * ability to restrict the width of the button to the height, to ensure a square
 * button.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FlatJButton extends JButton
{
	private boolean hasFocus = false;
	private boolean mouseInside = false;

	public FlatJButton(Icon icon)
	{
		super(icon);
		init();
	}

	public FlatJButton(String text)
	{
		super(text);
		init();
	}

	public FlatJButton(String text, Icon icon)
	{
		super(text, icon);
		init();
	}

	/**
	 * Restrict the width of this button to it's preferred height, so that it is
	 * square.
	 */
	public void restrictSize()
	{
		Dimension size = getPreferredSize();
		size.width = size.height;
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
	}

	private void init()
	{
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFocusPainted(false);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				mouseInside = true;
				setContentAreaFilled(true);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				mouseInside = false;
				setContentAreaFilled(hasFocus);
			}
		});

		addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				hasFocus = true;
				setContentAreaFilled(true);
			}

			@Override
			public void focusLost(FocusEvent e)
			{
				hasFocus = false;
				setContentAreaFilled(mouseInside);
			}
		});
	}

	@Override
	public void setContentAreaFilled(boolean b)
	{
		super.setContentAreaFilled(b && isEnabled());
	}
}
