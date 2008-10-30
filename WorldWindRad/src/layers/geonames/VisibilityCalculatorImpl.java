package layers.geonames;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

public class VisibilityCalculatorImpl implements VisibilityCalculator
{
	private Sector sector;
	private int levels;
	private Position eye;
	private Object lock = new Object();

	private Sector[] levelSectors;
	private boolean dirty = true;

	public VisibilityCalculatorImpl()
	{
	}

	public boolean isVisible(GeoName geoname)
	{
		synchronized (lock)
		{
			if (dirty || levelSectors.length < levels)
			{
				calculateLevelSectors();
			}

			if (geoname.level >= levels)
				return false;

			if (geoname.level <= 1) //levels 0 and 1 are always visible
				return true;
			if (levelSectors[geoname.level].contains(geoname.latlon))
				return true;
			if (sector == null)
				return true;

			return sector.contains(geoname.latlon);
		}
	}

	private void calculateLevelSectors()
	{
		levelSectors = new Sector[levels];
		double width = 360 / 2;
		double height = 180 / 2;
		for (int i = 0; i < levels; i++)
		{
			width /= 2d;
			height /= 2d;
			double lat = eye.getLatitude().degrees; //-90 to 90
			double lon = eye.getLongitude().degrees; //-180 to 180
			levelSectors[i] = Sector.fromDegrees(lat - height, lat + height, lon
					- width, lon + width);
		}
		
		dirty = false;
	}

	public Sector getSector()
	{
		return sector;
	}

	public void setSector(Sector sector)
	{
		synchronized (lock)
		{
			this.sector = sector;
		}
	}

	public int getLevels()
	{
		return levels;
	}

	public void setLevels(int levels)
	{
		synchronized (lock)
		{
			this.levels = levels;
			dirty = true;
		}
	}

	public Position getEye()
	{
		return eye;
	}

	public void setEye(Position eye)
	{
		synchronized (lock)
		{
			this.eye = eye;
			dirty = true;
		}
	}
}
