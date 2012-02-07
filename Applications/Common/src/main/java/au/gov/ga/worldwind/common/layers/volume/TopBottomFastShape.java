package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import java.nio.IntBuffer;
import java.util.List;

import au.gov.ga.worldwind.common.util.FastShape;

/**
 * {@link FastShape} subclass which defines top and bottom elevation offsets.
 * When calculating the {@link FastShape}s vertices, if a position is a
 * {@link TopBottomPosition}, it is offset by either the top or bottom elevation
 * offset. This class can also define a {@link LatLon} offset.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TopBottomFastShape extends FastShape
{
	protected double topElevationOffset = 0d;
	protected double bottomElevationOffset = 0d;
	protected LatLon latlonOffset = LatLon.ZERO;

	public TopBottomFastShape(List<Position> positions, int mode)
	{
		super(positions, mode);
	}

	public TopBottomFastShape(List<Position> positions, IntBuffer indices, int mode)
	{
		super(positions, indices, mode);
	}

	/**
	 * @return The elevation offset by which top positions are offset.
	 */
	public double getTopElevationOffset()
	{
		return topElevationOffset;
	}

	/**
	 * Set the elevation offset by which top positions are offset.
	 * 
	 * @param topElevationOffset
	 */
	public void setTopElevationOffset(double topElevationOffset)
	{
		if (this.topElevationOffset != topElevationOffset)
		{
			frontLock.writeLock().lock();
			try
			{

				verticesDirty = true;
				this.topElevationOffset = topElevationOffset;
			}
			finally
			{
				frontLock.writeLock().unlock();
			}
		}
	}

	/**
	 * @return The elevation offset by which bottom positions are offset.
	 */
	public double getBottomElevationOffset()
	{
		return bottomElevationOffset;
	}

	/**
	 * Set the elevation offset by which bottom positions are offset.
	 * 
	 * @param bottomElevationOffset
	 */
	public void setBottomElevationOffset(double bottomElevationOffset)
	{
		if (this.bottomElevationOffset != bottomElevationOffset)
		{
			frontLock.writeLock().lock();
			try
			{
				verticesDirty = true;
				this.bottomElevationOffset = bottomElevationOffset;
			}
			finally
			{
				frontLock.writeLock().unlock();
			}
		}
	}

	/**
	 * @return The {@link LatLon} offset by which each position is offset.
	 */
	public LatLon getLatlonOffset()
	{
		return latlonOffset;
	}

	/**
	 * Set the {@link LatLon} offset by which each position is offset.
	 * 
	 * @param latlonOffset
	 */
	public void setLatlonOffset(LatLon latlonOffset)
	{
		if (!LatLon.equals(latlonOffset, this.latlonOffset))
		{
			frontLock.writeLock().lock();
			try
			{
				verticesDirty = true;
				this.latlonOffset = latlonOffset;
			}
			finally
			{
				frontLock.writeLock().unlock();
			}
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
	protected LatLon calculateLatLonOffset()
	{
		return latlonOffset;
	}
}
