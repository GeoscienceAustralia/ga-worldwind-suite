package au.gov.ga.worldwind.common.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

public class BasicAction extends AbstractAction
{
	private List<ActionListener> listeners = new ArrayList<ActionListener>();

	public BasicAction(String name, Icon icon)
	{
		this(name, name, icon);
	}

	public BasicAction(String name, String toolTipText, Icon icon)
	{
		super(name, icon);
		setToolTipText(toolTipText);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
			listeners.get(i).actionPerformed(e);
	}

	public void addActionListener(ActionListener listener)
	{
		listeners.add(listener);
	}

	public void removeActionListener(ActionListener listener)
	{
		listeners.remove(listener);
	}

	public String getName()
	{
		return (String) getValue(Action.NAME);
	}

	public void setName(String name)
	{
		putValue(Action.NAME, name);
	}

	public Icon getIcon()
	{
		return (Icon) getValue(Action.SMALL_ICON);
	}

	public void setIcon(Icon icon)
	{
		putValue(Action.SMALL_ICON, icon);
	}

	public String getToolTipText()
	{
		return (String) getValue(Action.SHORT_DESCRIPTION);
	}

	public void setToolTipText(String toolTipText)
	{
		putValue(Action.SHORT_DESCRIPTION, toolTipText);
	}
}
