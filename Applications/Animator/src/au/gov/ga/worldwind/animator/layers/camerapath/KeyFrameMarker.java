package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;

import java.awt.Color;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A superclass for camera key frame markers
 */
public abstract class KeyFrameMarker extends BasicMarker
{	
	private static final BasicMarkerAttributes DEFAULT_UNHIGHLIGHTED_ATTRIBUTES;
	private static final BasicMarkerAttributes DEFAULT_HIGHLIGHTED_ATTRIBUTES;
	static
	{
		DEFAULT_HIGHLIGHTED_ATTRIBUTES = new BasicMarkerAttributes();
		DEFAULT_HIGHLIGHTED_ATTRIBUTES.setMaterial(new Material(Color.YELLOW));
		DEFAULT_HIGHLIGHTED_ATTRIBUTES.setMaxMarkerSize(2000d);
		
		DEFAULT_UNHIGHLIGHTED_ATTRIBUTES = new BasicMarkerAttributes(DEFAULT_HIGHLIGHTED_ATTRIBUTES);
		DEFAULT_UNHIGHLIGHTED_ATTRIBUTES.setMaterial(new Material(Color.MAGENTA));
	}
	
	private BasicMarkerAttributes unhighlightedAttributes = DEFAULT_UNHIGHLIGHTED_ATTRIBUTES;
	private BasicMarkerAttributes highlightedAttributes = DEFAULT_HIGHLIGHTED_ATTRIBUTES;
	
	private Animation animation;
	private int frame;
	
	public KeyFrameMarker(Animation animation, int frame, Position position)
	{
		super(position, DEFAULT_UNHIGHLIGHTED_ATTRIBUTES);
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
		this.frame = frame;
		setAttributes(getUnhighlightedAttributes());
	}

	public void highlight()
	{
		setAttributes(getHighlightedAttributes());
	}
	
	public void unhighlight()
	{
		setAttributes(getUnhighlightedAttributes());
	}
	
	/**
	 * @return The marker attributes to use for an un-highlighted marker
	 */
	public BasicMarkerAttributes getUnhighlightedAttributes()
	{
		return unhighlightedAttributes;
	}
	
	/**
	 * @return The marker attributes to use for a highlighted marker
	 */
	public BasicMarkerAttributes getHighlightedAttributes()
	{
		return highlightedAttributes;
	}
	
	public abstract void applyPositionChangeToAnimation();
	
	public int getFrame()
	{
		return frame;
	}
	
	public Animation getAnimation()
	{
		return animation;
	}
}
