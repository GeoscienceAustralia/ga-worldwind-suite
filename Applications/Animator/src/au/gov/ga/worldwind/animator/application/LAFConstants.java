package au.gov.ga.worldwind.animator.application;

import java.awt.Color;

/**
 * Look and Feel (Color etc.) constants used throughout the application
 */
public class LAFConstants
{
	private static final Color HIGHLIGHT_COLOR = new Color(230, 247, 252);
	public static Color getHighlightColor(){ return HIGHLIGHT_COLOR; }
	
	private static final Color UNSELECTED_KEY_COLOR = new Color(255, 0, 0, 128);
	public static Color getUnselectedKeyColor() { return UNSELECTED_KEY_COLOR; }
	
	private static final Color KEY_SELECTOR_COLOR = new Color(0, 0, 255, 128);
	public static Color getKeySelectorColor() { return KEY_SELECTOR_COLOR; }
	
	private static final Color HIGHLIGHTED_KEY_SELECTOR_COLOR = new Color(0, 255, 0, 128);
	public static Color getHighlightedKeySelectorColor() { return HIGHLIGHTED_KEY_SELECTOR_COLOR; }
	
	private static final Color VALID_FIELD_COLOR = Color.WHITE;
	public static Color getValidFieldColor() { return VALID_FIELD_COLOR; }
	
	private static final Color INVALID_FIELD_COLOR = Color.PINK;
	public static Color getInvalidFieldColor() { return INVALID_FIELD_COLOR; }
}
