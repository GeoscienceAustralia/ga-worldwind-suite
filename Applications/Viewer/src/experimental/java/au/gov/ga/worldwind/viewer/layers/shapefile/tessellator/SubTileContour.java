package au.gov.ga.worldwind.viewer.layers.shapefile.tessellator;

import gov.nasa.worldwind.geom.LatLon;

import java.util.ArrayList;
import java.util.List;

public class SubTileContour
{
	public final int shapeId;
	public final List<LatLon> points = new ArrayList<LatLon>();
	public boolean exited = false;
	public boolean entered = false;

	public SubTileContour(int shapeId, boolean entered)
	{
		this.shapeId = shapeId;
		this.entered = entered;
	}
}
