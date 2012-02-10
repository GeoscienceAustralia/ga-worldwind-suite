package au.gov.ga.worldwind.common.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

/**
 * {@link BasicAction} subclass that adds toggleable checkbox style selection.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SelectableAction extends BasicAction
{
	public SelectableAction(String name, Icon icon, boolean selected)
	{
		super(name, icon);
		setSelected(selected);
	}

	public SelectableAction(String name, String toolTipText, Icon icon, boolean selected)
	{
		super(name, toolTipText, icon);
		setSelected(selected);
	}

	public boolean isSelected()
	{
		Object o = getValue(Action.SELECTED_KEY);
		if (o == null || !(o instanceof Boolean))
			return false;
		return (Boolean) o;
	}

	public void setSelected(boolean selected)
	{
		putValue(Action.SELECTED_KEY, selected);
	}

	private void registerButton(final AbstractButton button, boolean addActionListener)
	{
		button.setSelected(isSelected());

		if (addActionListener)
		{
			button.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setSelected(!isSelected());
				}
			});
		}

		addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getPropertyName().equals(Action.SELECTED_KEY))
					button.setSelected(isSelected());
			}
		});
		button.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				setSelected(selected);
			}
		});
	}

	public void addToMenu(JMenu menu)
	{
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(this);
		registerButton(item, false);
		menu.add(item);
	}

	public void addToPopupMenu(JPopupMenu menu)
	{
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(this);
		registerButton(item, false);
		menu.add(item);
	}

	public void addToToolBar(JToolBar toolBar)
	{
		AbstractButton button = toolBar.add(this);
		registerButton(button, true);
	}

	public AbstractButton createButton()
	{
		JButton button = new JButton(this);
		registerButton(button, true);
		return button;
	}
}
