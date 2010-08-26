package au.gov.ga.worldwind.viewer.theme;

import javax.swing.JPanel;

public interface ThemePanel extends ThemePiece
{
	public JPanel getPanel();
	public boolean isResizable();
	public void setResizable(boolean resizable);
	public float getWeight();
	public void setWeight(float weight);
	public boolean isExpanded();
	public void setExpanded(boolean expanded);
	
	public interface ThemePanelListener extends ThemePieceListener
	{
		public void resizableToggled(ThemePanel source);
		public void weightChanged(ThemePanel source);
		public void expandedToggled(ThemePanel source);
	}
	
	public class ThemePanelAdapter extends ThemePieceAdapter implements ThemePanelListener
	{
		@Override
		public void expandedToggled(ThemePanel source)
		{
		}

		@Override
		public void resizableToggled(ThemePanel source)
		{
		}

		@Override
		public void weightChanged(ThemePanel source)
		{
		}
	}
}
