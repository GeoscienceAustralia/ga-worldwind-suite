package au.gov.ga.worldwind.common.layers.curtain.delegate;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.TileKey;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.curtain.CurtainLevel;
import au.gov.ga.worldwind.common.layers.curtain.Segment;
import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.util.Util;

public class CurtainTextureTileFactoryDelegate implements ICurtainTileFactoryDelegate
{
	private final static String DEFINITION_STRING = "CurtainTextureTile";

	public static TileKey appendToTileKeyCacheName(TileKey tileKey, String cacheName)
	{
		return new TileKey(tileKey.getLevelNumber(), tileKey.getRow(), tileKey.getColumn(), tileKey.getCacheName()
				+ cacheName);
	}

	private final String random = "_" + Util.randomString(8);

	@Override
	public DelegatorCurtainTextureTile createTextureTile(Segment segment, CurtainLevel level, int row, int col)
	{
		return new DelegatorCurtainTextureTile(segment, level, row, col, this);
	}

	@Override
	public TileKey transformTileKey(TileKey tileKey)
	{
		return appendToTileKeyCacheName(tileKey, random);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new CurtainTextureTileFactoryDelegate();
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
