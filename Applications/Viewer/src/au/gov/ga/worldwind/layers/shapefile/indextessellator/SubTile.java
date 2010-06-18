package au.gov.ga.worldwind.layers.shapefile.indextessellator;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.util.Util;

public class SubTile
{
	public final int x;
	public final int y;

	public final IndexedLatLon minmin;
	public final IndexedLatLon minmax;
	public final IndexedLatLon maxmax;
	public final IndexedLatLon maxmin;

	private final List<IndexedContour> contours = new ArrayList<IndexedContour>();
	private IndexedContour current = null;
	private int startPointsIndex = 0;
	private boolean noEntryShapeExists = false;
	private boolean filled = false;

	public SubTile(int x, int y, IndexedLatLon minmin, IndexedLatLon minmax, IndexedLatLon maxmax,
			IndexedLatLon maxmin)
	{
		this.x = x;
		this.y = y;
		this.minmin = minmin;
		this.minmax = minmax;
		this.maxmax = maxmax;
		this.maxmin = maxmin;
	}

	public List<IndexedContour> getContours()
	{
		return contours;
	}

	public void addVertex(int shapeId, int vertexIndex, boolean entry, boolean exit)
	{
		//create a new pointset if:
		// - the record has entered the tile (each entry creates a new shape)
		// - no current pointset exists (continueShape is called without an enter)
		// - the shape id is different (a new shape has started, through continueShape without an enter)

		boolean newShapeId = current == null || current.shapeId != shapeId;

		if (entry || newShapeId)
		{
			if (newShapeId)
				startPointsIndex = contours.size();

			current = new IndexedContour(shapeId, entry);
			contours.add(current);

			//if this shape was not created by a record entering the tile, then a 'noEntryShapeExists'
			if (!entry)
				noEntryShapeExists = true;
		}

		if (exit)
		{
			current.exited = true;
		}

		//add the vertex index
		current.indices.add(vertexIndex);
	}

	public void joinOrphanPolygons(int shapeId)
	{
		//Polygons must either be fully contained in the tile, or both enter AND exit.
		//If a pointset enters but not exits, there MUST be another pointset that exits
		//but not enters. Join these two pointsets (they will share a end point).

		if (noEntryShapeExists)
		{
			int noExitIndex = -1, noEntryIndex = -1;
			for (int i = startPointsIndex; i < contours.size(); i++)
			{
				IndexedContour p = contours.get(i);
				if (p.entered != p.exited)
				{
					if (!p.entered)
						noEntryIndex = i;
					else if (!p.exited)
						noExitIndex = i;
				}

				if (noEntryIndex >= 0 && noExitIndex >= 0)
					break;
			}

			if (noEntryIndex >= 0 && noExitIndex >= 0)
			{
				IndexedContour noEntry = contours.get(noEntryIndex);
				IndexedContour noExit = contours.get(noExitIndex);

				//check if the two pointsets share a point
				int i1 = noEntry.indices.get(0);
				int i2 = noExit.indices.get(noExit.indices.size() - 1);
				int start = 1;
				if (i1 != i2)
				{
					String message = "First entry point doesn't join up with last exit point";
					Logging.logger().warning(message);
					start = 0;
				}

				//add all noEntry's points (except the shared point) to the noExit
				//pointset (so that it has an exit), and then remove the noEntry pointset
				for (int i = start; i < noEntry.indices.size(); i++)
				{
					noExit.indices.add(noEntry.indices.get(i));
				}
				contours.remove(noEntryIndex);
				noExit.exited = true;
			}

			//calling this function twice shouldn't have any affect; set flag to false
			noEntryShapeExists = false;
		}
	}

	public void markFilled()
	{
		filled = true;
	}

	public boolean contains(LatLon latlon)
	{
		return containsDegrees(latlon.latitude.degrees, latlon.longitude.degrees);
	}

	public boolean containsDegrees(double lat, double lon)
	{
		return lat >= this.minmin.latitude.degrees && lat <= this.maxmax.latitude.degrees
				&& lon >= this.minmin.longitude.degrees && lon <= this.maxmax.longitude.degrees;
	}

	public LatLon limitLatLonWithinTile(LatLon latlon)
	{
		double lat =
				Util.limitRange(latlon.latitude.degrees, minmin.latitude.degrees,
						maxmax.latitude.degrees);
		double lon =
				Util.limitRange(latlon.longitude.degrees, minmin.longitude.degrees,
						maxmax.longitude.degrees);

		return LatLon.fromDegrees(lat, lon);
	}

	public double deltaLatDegrees()
	{
		return maxmax.latitude.degrees - minmin.latitude.degrees;
	}

	public double deltaLonDegrees()
	{
		return maxmax.longitude.degrees - minmin.longitude.degrees;
	}

	public double latFract(LatLon latlon)
	{
		return (latlon.latitude.degrees - minmin.latitude.degrees) / deltaLatDegrees();
	}

	public double lonFract(LatLon latlon)
	{
		return (latlon.longitude.degrees - minmin.longitude.degrees) / deltaLonDegrees();
	}
}
