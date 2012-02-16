package au.gov.ga.worldwind.viewer.theme;

import gov.nasa.worldwind.avlist.AVKey;

/**
 * Represents a Heads-Up-Display object associated with a Theme. HUDs are
 * generally layers that don't belong in the layers list, such as the World Map
 * or Compass layers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ThemeHUD extends ThemePiece
{
	/**
	 * @return Screen position of this HUD, if applicable.
	 * @see AVKey
	 */
	String getPosition();

	/**
	 * Set the screen position of this HUD. Possible options are:
	 * <ul>
	 * <li>{@link AVKey#NORTH}</li>
	 * <li>{@link AVKey#SOUTH}</li>
	 * <li>{@link AVKey#EAST}</li>
	 * <li>{@link AVKey#WEST}</li>
	 * <li>{@link AVKey#NORTHEAST}</li>
	 * <li>{@link AVKey#NORTHWEST}</li>
	 * <li>{@link AVKey#SOUTHEAST}</li>
	 * <li>{@link AVKey#SOUTHWEST}</li>
	 * <li>{@link AVKey#CENTER}</li>
	 * </ul>
	 * 
	 * @param position
	 */
	void setPosition(String position);

	/**
	 * Listener interface for changes in {@link ThemeHUD} state.
	 */
	public interface ThemeHUDListener extends ThemePieceListener
	{
		/**
		 * Called when the {@link ThemeHUD} position changes.
		 * 
		 * @param source
		 */
		void positionChanged(ThemeHUD source);
	}

	/**
	 * Blank implementation of the {@link ThemeHUDListener} interface.
	 */
	public class ThemeHUDAdapter extends ThemePieceAdapter implements ThemeHUDListener
	{
		@Override
		public void positionChanged(ThemeHUD source)
		{
		}
	}
}
