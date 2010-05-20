package au.gov.ga.worldwind.theme;

import javax.swing.JPanel;

public interface ThemePanel extends ThemePiece
{
	public JPanel getPanel();
	public boolean isResizable();
	public void setResizable(boolean resizable);
	public float getWeight();
	public void setWeight(float weight);
}
