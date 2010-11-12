package au.gov.ga.worldwind.common.layers.curtain.delegate;

import gov.nasa.worldwind.util.TileKey;
import au.gov.ga.worldwind.common.layers.curtain.CurtainLevel;
import au.gov.ga.worldwind.common.layers.curtain.CurtainTextureTile;
import au.gov.ga.worldwind.common.layers.curtain.Segment;
import au.gov.ga.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.worldwind.common.layers.delegate.ITileFactoryDelegate;

public class DelegatorCurtainTextureTile extends CurtainTextureTile implements IDelegatorTile
{
	protected final ITileFactoryDelegate<DelegatorCurtainTextureTile, Segment, CurtainLevel> delegate;
	protected TileKey transformedTileKey;

	public DelegatorCurtainTextureTile(Segment segment, CurtainLevel level, int row, int column,
			ITileFactoryDelegate<DelegatorCurtainTextureTile, Segment, CurtainLevel> delegate)
	{
		super(segment, level, row, column);
		this.delegate = delegate;
	}

	@Override
	public String getService()
	{
		return getLevel().getService();
	}

	@Override
	public String getDataset()
	{
		return getLevel().getDataset();
	}

	@Override
	protected CurtainTextureTile createTextureTile(Segment segment, CurtainLevel level, int row, int column)
	{
		return delegate.createTextureTile(segment, level, row, column);
	}

	@Override
	public TileKey getTransformedTileKey()
	{
		if (transformedTileKey == null)
		{
			transformedTileKey = delegate.transformTileKey(super.getTileKey());
		}
		return transformedTileKey;
	}

	@Override
	public TileKey getTileKey()
	{
		//In this instance, we can override getTileKey(), because it is not final in CurtainTile.
		//This means we don't have to override the other texture handling functions, which was
		//required in the DelegatorTextureTile because Tile.getTileKey() is final.
		return getTransformedTileKey();
	}

	@Override
	protected CurtainTextureTile getTileFromMemoryCache(TileKey tileKey)
	{
		tileKey = delegate.transformTileKey(tileKey);
		return super.getTileFromMemoryCache(tileKey);
	}
}
