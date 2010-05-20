package au.gov.ga.worldwind.theme;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public abstract class AbstractThemePanel extends JPanel implements ThemePanel
{
	private List<ThemePieceListener> listeners = new ArrayList<ThemePieceListener>();
	private String displayName;
	private boolean resizable = true;
	private float weight = 1f;

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
			for (ThemePieceListener listener : listeners)
				listener.onToggled(on);
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
		this.displayName = displayName;
	}

	@Override
	public boolean isResizable()
	{
		return resizable;
	}

	@Override
	public void setResizable(boolean resizable)
	{
		this.resizable = resizable;
	}

	@Override
	public float getWeight()
	{
		return weight;
	}

	@Override
	public void setWeight(float weight)
	{
		this.weight = weight;
	}

	@Override
	public void addListener(ThemePieceListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeListener(ThemePieceListener listener)
	{
		listeners.remove(listener);
	}
}
