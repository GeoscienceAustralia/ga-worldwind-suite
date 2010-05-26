package au.gov.ga.worldwind.theme;

import gov.nasa.worldwind.Disposable;

import javax.swing.Icon;

public interface ThemePiece extends Disposable
{
	public void setup(Theme theme);
	public String getDisplayName();
	public void setDisplayName(String name);
	public boolean isOn();
	public void setOn(boolean on);
	public void addListener(ThemePieceListener listener);
	public void removeListener(ThemePieceListener listener);
	public Icon getIcon();

	public interface ThemePieceListener
	{
		public void onToggled(ThemePiece source);
		public void displayNameChanged(ThemePiece source);
	}

	public class ThemePieceAdapter implements ThemePieceListener
	{
		@Override
		public void displayNameChanged(ThemePiece source)
		{
		}
		
		@Override
		public void onToggled(ThemePiece source)
		{
		}
	}
}
