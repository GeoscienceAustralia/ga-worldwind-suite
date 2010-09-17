package au.gov.ga.worldwind.common.layers.curtain;

public class CurtainTile
{
	private final CurtainLevel level;
	private final Segment segment;
	private final int row;
	private final int column;

	public CurtainTile(CurtainLevel level, Segment segment, int row, int column)
	{
		this.segment = segment;
		this.level = level;
		this.row = row;
		this.column = column;
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
}
