package au.gov.ga.worldwind.animator.util;

/**
 * Extension of the common {@link au.gov.ga.worldwind.common.util.Icons}
 * accessor class with additional icons specific to the Animator project.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Icons extends au.gov.ga.worldwind.common.util.Icons
{
	public static final Icons armed = new Icons("armed.gif");
	public static final Icons disarmed = new Icons("disarmed.gif");
	public static final Icons elevation = new Icons("elevation.gif");
	public static final Icons key = new Icons("key.gif");
	public static final Icons animatableLayer = new Icons("layer_object.gif");
	public static final Icons lock = new Icons("lock.gif");
	public static final Icons animatableObject = new Icons("object.gif");
	public static final Icons parameter = new Icons("parameter.gif");
	public static final Icons partialArmed = new Icons("partial_armed.gif");
	public static final Icons render = new Icons("render.gif");
	public static final Icons unlock = new Icons("unlock.gif");
	public static final Icons zoomx = new Icons("zoomx.gif");
	public static final Icons zoomxy = new Icons("zoomxy.gif");
	public static final Icons zoomy = new Icons("zoomy.gif");

	protected Icons(String string)
	{
		super(string);
	}
}
