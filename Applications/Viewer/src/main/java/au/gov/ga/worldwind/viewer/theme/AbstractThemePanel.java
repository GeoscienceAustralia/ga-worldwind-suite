package au.gov.ga.worldwind.viewer.theme;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Abstract implementation of the {@link ThemePanel} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractThemePanel extends JPanel implements ThemePanel
{
	public static final int MINIMUM_LIST_HEIGHT = 25;

	private List<ThemePieceListener> listeners = new ArrayList<ThemePieceListener>();
	private List<ThemePanelListener> panelListeners = new ArrayList<ThemePanelListener>();
	private String displayName;
	private boolean resizable = true;
	private float weight = 1f;
	private boolean expanded = true;

	public AbstractThemePanel()
	{
		super();
	}

	public AbstractThemePanel(LayoutManager layout)
	{
		super(layout);
	}

	@Override
	public JPanel getPanel()
	{
		return this;
	}

	@Override
	public boolean isOn()
	{
		return isVisible();
	}

	@Override
	public void setOn(boolean on)
	{
		if (isOn() != on)
		{
			setVisible(on);
			raiseOnToggled();
		}
	}

	@Override
	public String getDisplayName()
	{
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName)
	{
		if (this.displayName != displayName && (displayName == null || !displayName.equals(this.displayName)))
		{
			this.displayName = displayName;
			raiseDisplayNameChange();
		}
	}

	@Override
	public boolean isResizable()
	{
		return resizable;
	}

	@Override
	public void setResizable(boolean resizable)
	{
		if (this.resizable != resizable)
		{
			this.resizable = resizable;
			raiseResizableToggled();
		}
	}

	@Override
	public float getWeight()
	{
		return weight;
	}

	@Override
	public void setWeight(float weight)
	{
		if (this.weight != weight)
		{
			this.weight = weight;
			raiseWeightChanged();
		}
	}

	@Override
	public boolean isExpanded()
	{
		return expanded;
	}

	@Override
	public void setExpanded(boolean expanded)
	{
		if (this.expanded != expanded)
		{
			this.expanded = expanded;
			raiseExpandedToggled();
		}
	}

	@Override
	public void addListener(ThemePieceListener listener)
	{
		listeners.add(listener);
		if (listener instanceof ThemePanelListener)
			panelListeners.add((ThemePanelListener) listener);
	}

	@Override
	public void removeListener(ThemePieceListener listener)
	{
		listeners.remove(listener);
		if (listener instanceof ThemePanelListener)
			panelListeners.remove((ThemePanelListener) listener);
	}

	protected void raiseOnToggled()
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
			listeners.get(i).onToggled(this);
	}

	protected void raiseDisplayNameChange()
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
			listeners.get(i).displayNameChanged(this);
	}

	protected void raiseResizableToggled()
	{
		for (int i = panelListeners.size() - 1; i >= 0; i--)
			panelListeners.get(i).resizableToggled(this);
	}

	protected void raiseExpandedToggled()
	{
		for (int i = panelListeners.size() - 1; i >= 0; i--)
			panelListeners.get(i).expandedToggled(this);
	}

	protected void raiseWeightChanged()
	{
		for (int i = panelListeners.size() - 1; i >= 0; i--)
			panelListeners.get(i).weightChanged(this);
	}
}
