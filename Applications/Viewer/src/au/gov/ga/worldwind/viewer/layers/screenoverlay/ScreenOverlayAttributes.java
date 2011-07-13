package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import java.awt.Color;
import java.net.URL;

/**
 * Attributes that control the look-and-feel of a {@link ScreenOverlayLayer}
 */
public interface ScreenOverlayAttributes
{
	
	// Source data
	
	/** @return the source data URL, if one has been provided. If <code>null</code>, use the {@link #getSourceHtml()} method. */
	URL getSourceUrl();
	
	/** @return the source html content. */
	String getSourceHtml();
	
	/** @return a unique ID that can identify this overlay */
	String getSourceId();
	
	// Positioning
	
	/** @return The position the overlay is to be placed on the screen */
	ScreenOverlayPosition getPosition();
	
	// Sizing
	
	/** @return the expression for the minimum height of the overlay */
	LengthExpression getMinHeight();
	
	/** @return the expression for the maximum height of the overlay */
	LengthExpression getMaxHeight();
	
	/** @return the height (in pixels) the overlay should occupy given the screen height. Min height is given priority. */
	float getHeight(float screenHeight);
	
	/** @return the expression for the minimum width of the overlay */
	LengthExpression getMinWidth();
	
	/** @return the expression for the maximum width of the overlay */
	LengthExpression getMaxWidth();
	
	/** @return the width (in pixels) the overlay should occupy given the screen width. Min width is given priority. */
	float getWidth(float screenWidth);
	
	// Styling
	
	/** @return whether or not to draw the border */
	boolean isDrawBorder();
	
	/** @return the border color, or <code>null</code> if no border is to be drawn */
	Color getBorderColor();
	
	/** @return the border width to use. If no border is to be drawn, will return 0. */
	int getBorderWidth();
	
}
