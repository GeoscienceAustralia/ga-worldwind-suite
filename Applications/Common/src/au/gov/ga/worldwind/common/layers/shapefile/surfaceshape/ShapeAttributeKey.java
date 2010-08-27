package au.gov.ga.worldwind.common.layers.shapefile.surfaceshape;

import gov.nasa.worldwind.avlist.AVKey;

/**
 * Some extra AVKeys that define ShapeAttributes in SurfaceShape layers
 * 
 * @author Michael de Hoog
 */
public interface ShapeAttributeKey extends AVKey
{
	final String DRAW_INTERIOR = "gov.nasa.worldwind.render.ShapeAttributes.DrawInterior";
	final String DRAW_OUTLINE = "gov.nasa.worldwind.render.ShapeAttributes.DrawOutline";
	final String ANTIALIASING = "gov.nasa.worldwind.render.ShapeAttributes.Antialiasing";
	final String INTERIOR_COLOR = "gov.nasa.worldwind.render.ShapeAttributes.InteriorColor";
	final String OUTLINE_COLOR = "gov.nasa.worldwind.render.ShapeAttributes.OutlineColor";
	final String INTERIOR_OPACITY = "gov.nasa.worldwind.render.ShapeAttributes.InteriorOpacity";
	final String OUTLINE_OPACITY = "gov.nasa.worldwind.render.ShapeAttributes.OutlineOpacity";
	final String OUTLINE_WIDTH = "gov.nasa.worldwind.render.ShapeAttributes.OutlineWidth";
	final String STIPPLE_FACTOR = "gov.nasa.worldwind.render.ShapeAttributes.StippleFactor";
	final String STIPPLE_PATTERN = "gov.nasa.worldwind.render.ShapeAttributes.StipplePattern";
}
