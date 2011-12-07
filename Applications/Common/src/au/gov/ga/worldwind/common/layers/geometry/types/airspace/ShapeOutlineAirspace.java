package au.gov.ga.worldwind.common.layers.geometry.types.airspace;

import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * An interface for airspaces that support the drawing of the generating shape
 * outlines.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ShapeOutlineAirspace extends Airspace
{
	/**
	 * Set whether to draw the shape outline at the top of the airspace.
	 */
	public void setDrawUpperShapeOutline(boolean drawUpperShapeOutline);

	/**
	 * Set whether to draw the shape outline at the top of the airspace.
	 */
	public void setDrawLowerShapeOutline(boolean drawLowerShapeOutline);

}
