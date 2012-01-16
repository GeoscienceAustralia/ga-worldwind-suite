package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;

/**
 * A {@link GlobeAnnotation} subclass which modifies the setDepthFunc function
 * to ensure that the annotation is always drawn on top of everything else.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OnTopGlobeAnnotation extends GlobeAnnotation
{
	public OnTopGlobeAnnotation(String text, Position position)
	{
		super(text, position);
	}

	@Override
	protected void setDepthFunc(DrawContext dc, Vec4 screenPoint)
	{
		//passing 0 for the screenPoint's z-coordinate causes the glDepthFunc to always pass
		screenPoint = new Vec4(screenPoint.x, screenPoint.y, 0.0, screenPoint.w);
		super.setDepthFunc(dc, screenPoint);
	}
}
