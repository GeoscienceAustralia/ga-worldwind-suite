package au.gov.ga.worldwind.theme;

public interface ThemeHUD extends ThemePiece
{
	public String getPosition();
	public void setPosition(String position);
	
	public interface ThemeHUDListener extends ThemePieceListener
	{
		public void positionChanged(ThemeHUD source);
	}
	
	public class ThemeHUDAdapter extends ThemePieceAdapter implements ThemeHUDListener
	{
		@Override
		public void positionChanged(ThemeHUD source)
		{
		}
	}
}
