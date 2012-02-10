package au.gov.ga.worldwind.common.ui.resizabletoolbar;

import java.awt.Dimension;

import javax.swing.JToolBar;

/**
 * {@link JToolBar} subclass that uses the {@link WrapLayout} layout so that it
 * can be resized (icons are wrapped to the next line if too small).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ResizableToolBar extends JToolBar
{
	public ResizableToolBar()
	{
		super(JToolBar.HORIZONTAL);
		setLayout(new WrapLayout(WrapLayout.LEFT, 0, 0));
		setFloatable(false);
	}

	@Override
	public void addSeparator()
	{
		//hack, as separator doesn't report it's size, so FlowLayout allocates zero height
		super.addSeparator(new Dimension(8, 25));
	}
}
