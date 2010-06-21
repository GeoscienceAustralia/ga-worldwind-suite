package au.gov.ga.worldwind.layers.shapefile.indextessellator;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;

import java.util.List;

public class IndexedLatLon extends LatLon
{
	private int index = -1;

	public IndexedLatLon(Angle latitude, Angle longitude)
	{
		super(latitude, longitude);
	}

	public IndexedLatLon(LatLon latlon)
	{
		super(latlon);
	}

	public boolean isIndexed()
	{
		return index >= 0;
	}

	public int getIndex()
	{
		return index;
	}

	public void indexIfNot(List<IndexedLatLon> list)
	{
		if (!isIndexed())
			index(list);
	}

	public void index(List<IndexedLatLon> list)
	{
		if (isIndexed())
			throw new IllegalStateException("already indexed");

		index = list.size();
		list.add(this);
	}

	public void replace(List<IndexedLatLon> list, IndexedLatLon replacement)
	{
		if (!isIndexed())
			throw new IllegalStateException("not yet indexed");
		if (replacement.isIndexed())
			throw new IllegalStateException("replacement already indexed");

		replacement.index = index;
		list.set(index, replacement);
	}

	public static IndexedLatLon fromDegrees(double lat, double lon)
	{
		return new IndexedLatLon(Angle.fromDegrees(lat), Angle.fromDegrees(lon));
	}
}
