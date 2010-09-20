package au.gov.ga.worldwind.common.layers.curtain;

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.util.TileKey;

public class CurtainTile implements Cacheable
{
	private final CurtainLevel level;
	private final Segment segment;
	private final int row;
	private final int column;
	private final TileKey tileKey;
	// The following is late bound because it's only selectively needed and costly to create
	private String path;

	public CurtainTile(CurtainLevel level, Segment segment, int row, int column)
	{
		this.segment = segment;
		this.level = level;
		this.row = row;
		this.column = column;
		this.tileKey = new CurtainTileKey(this);
	}

	@Override
	public long getSizeInBytes()
	{
		// Return just an approximate size
		long size = 0;

		if (this.segment != null)
			size += this.segment.getSizeInBytes();

		if (this.path != null)
			size += this.path.length();

		size += 32; // to account for the references and the TileKey size

		return size;
	}

	public CurtainLevel getLevel()
	{
		return level;
	}

	public Segment getSegment()
	{
		return segment;
	}

	public int getRow()
	{
		return row;
	}

	public int getColumn()
	{
		return column;
	}

	public final TileKey getTileKey()
	{
		return tileKey;
	}

	public int getLevelNumber()
	{
		return level.getLevelNumber();
	}

	public String getPath()
	{
		if (this.path == null)
		{
			this.path = this.level.getPath() + "/" + this.row + "/" + this.row + "_" + this.column;
			if (!this.level.isEmpty())
				path += this.level.getFormatSuffix();
		}

		return this.path;
	}

	public String getPathBase()
	{
		String path = this.getPath();

		return path.contains(".") ? path.substring(0, path.lastIndexOf(".")) : path;
	}

	public String getLabel()
	{
		return row + "," + column + "@" + level.getLevelNumber();
	}
}
