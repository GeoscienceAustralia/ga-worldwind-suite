package au.gov.ga.worldwind.theme;

import gov.nasa.worldwind.Disposable;

public interface ThemePiece extends Disposable
{
	public void setup(Theme theme);
	public String getDisplayName();
	public void setDisplayName(String name);
	public boolean isOn();
	public void setOn(boolean on);
	public void addListener(ThemePieceListener listener);
	public void removeListener(ThemePieceListener listener);

	public interface ThemePieceListener
	{
		public void onToggled(boolean on);
	}
}
