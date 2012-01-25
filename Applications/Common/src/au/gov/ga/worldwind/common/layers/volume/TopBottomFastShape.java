package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;

import java.nio.IntBuffer;
import java.util.List;

import au.gov.ga.worldwind.common.util.FastShape;

public class TopBottomFastShape extends FastShape
{
	protected double topElevationOffset = 0d;
	protected double bottomElevationOffset = 0d;
	protected boolean recalculateRequired = false;

	public TopBottomFastShape(List<Position> positions, int mode)
	{
		super(positions, mode);
	}

	public TopBottomFastShape(List<Position> positions, IntBuffer indices, int mode)
	{
		super(positions, indices, mode);
	}

	public double getTopElevationOffset()
	{
		return topElevationOffset;
	}

	public void setTopElevationOffset(double topElevationOffset)
	{
		frontLock.writeLock().lock();
		try
		{
			recalculateRequired = true;
			this.topElevationOffset = topElevationOffset;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	public double getBottomElevationOffset()
	{
		return bottomElevationOffset;
	}

	public void setBottomElevationOffset(double bottomElevationOffset)
	{
		frontLock.writeLock().lock();
		try
		{
			recalculateRequired = true;
			this.bottomElevationOffset = bottomElevationOffset;
		}
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	@Override
	protected double calculateElevationOffset(LatLon position)
	{
		double elevationOffset = super.calculateElevationOffset(position);
		if (position instanceof TopBottomPosition)
		{
			elevationOffset += ((TopBottomPosition) position).isBottom() ? bottomElevationOffset : topElevationOffset;
		}
		return elevationOffset;
	}

	@Override
	protected boolean isVertexRecalculationRequired(DrawContext dc)
	{
		boolean recalcuationRequired = super.isVertexRecalculationRequired(dc) || recalculateRequired;
		recalculateRequired = false;
		return recalcuationRequired;
	}
}
