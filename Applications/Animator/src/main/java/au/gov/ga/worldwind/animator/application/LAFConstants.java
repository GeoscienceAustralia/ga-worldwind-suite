package au.gov.ga.worldwind.animator.application;

import java.awt.Color;
import java.awt.Font;

/**
 * Look and Feel (Color etc.) constants used throughout the application
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LAFConstants
{
	private static final Color HIGHLIGHT_COLOR = new Color(230, 247, 252);
	public static Color getHighlightColor(){ return HIGHLIGHT_COLOR; }
	
	private static final Color KEY_COLOR = new Color(255, 0, 0, 128);
	public static Color getKeyColor() { return KEY_COLOR; }
	
	private static final Color HIGHLIGHTED_KEY_COLOR = new Color(0, 255, 255, 128);
	public static Color getHighlightedKeyColor() { return HIGHLIGHTED_KEY_COLOR; }
	
	private static final Color KEY_SELECTOR_COLOR = new Color(0, 0, 255, 128);
	public static Color getKeySelectorColor() { return KEY_SELECTOR_COLOR; }
	
	private static final Color HIGHLIGHTED_KEY_SELECTOR_COLOR = new Color(0, 255, 0, 128);
	public static Color getHighlightedKeySelectorColor() { return HIGHLIGHTED_KEY_SELECTOR_COLOR; }
	
	private static final Color VALID_FIELD_COLOR = Color.WHITE;
	public static Color getValidFieldColor() { return VALID_FIELD_COLOR; }
	
	private static final Color INVALID_FIELD_COLOR = Color.PINK;
	public static Color getInvalidFieldColor() { return INVALID_FIELD_COLOR; }
	
	private static final Font SUB_HEADING_FONT = new Font(Font.MONOSPACED, Font.ITALIC, 12);
	public static Font getSubHeadingFont() { return SUB_HEADING_FONT; }
	
	private static final Color CURVE_EDITOR_ACTIVE_BACKGROUND_COLOR = Color.WHITE;
	public static Color getCurveEditorActiveBackgroundColor() { return CURVE_EDITOR_ACTIVE_BACKGROUND_COLOR; }
	
	private static final Color CURVE_EDITOR_INACTIVE_BACKGROUND_COLOR = new Color(250, 250, 255);
	public static Color getCurveEditorInactiveBackgroundColor() { return CURVE_EDITOR_INACTIVE_BACKGROUND_COLOR; }
	
	private static final Color CURVE_EDITOR_GRID_COLOR = new Color(240, 240, 240);
	public static Color getCurveEditorGridColor() { return CURVE_EDITOR_GRID_COLOR; }
	
	private static final Color CURVE_EDITOR_CURRENT_FRAME_COLOR = new Color(255, 0, 0, 128);
	public static Color getCurveEditorCurrentFrameColor() { return CURVE_EDITOR_CURRENT_FRAME_COLOR; }
	
	private static final Color CURVE_EDITOR_CURRENT_MOUSE_POSITION_COLOR = new Color(0, 0, 255, 128);
	public static Color getCurveEditorCurrentMousePositionColor() { return CURVE_EDITOR_CURRENT_MOUSE_POSITION_COLOR; }
	
	private static final Color CURVE_KEY_HANDLE_COLOR = Color.ORANGE.darker().darker();
	public static Color getCurveKeyHandleColor() { return CURVE_KEY_HANDLE_COLOR; }
	
	private static final Color CURVE_HANDLE_JOINER_COLOR = Color.PINK;
	public static Color getCurveHandleJoinerColor() { return CURVE_HANDLE_JOINER_COLOR; }
}
