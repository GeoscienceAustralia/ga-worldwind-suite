package au.gov.ga.worldwind.layers.shapefile.indextessellator;

import java.util.ArrayList;
import java.util.List;

public class IndexedContour
{
	public final int shapeId;
	public final List<Integer> indices = new ArrayList<Integer>();
	public boolean exited = false;
	public boolean entered = false;

	public IndexedContour(int shapeId, boolean entered)
	{
		this.shapeId = shapeId;
		this.entered = entered;
	}
}
