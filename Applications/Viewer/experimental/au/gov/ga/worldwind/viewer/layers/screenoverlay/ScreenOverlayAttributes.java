package au.gov.ga.worldwind.viewer.layers.screenoverlay;

/**
 * Attributes that control the look-and-feel of a {@link ScreenOverlayLayer}
 */
public interface ScreenOverlayAttributes
{
	
	/** @return The position the overlay is to be placed on the screen */
	ScreenOverlayPosition getPosition();
	
	/** @return the min height expression. Accepts Npx or N% formats */
	String getMinHeight();
	
	
	
}
