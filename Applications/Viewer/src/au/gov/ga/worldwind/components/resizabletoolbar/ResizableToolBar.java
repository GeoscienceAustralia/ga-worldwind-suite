package au.gov.ga.worldwind.components.resizabletoolbar;

import java.awt.Dimension;

import javax.swing.JToolBar;

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
