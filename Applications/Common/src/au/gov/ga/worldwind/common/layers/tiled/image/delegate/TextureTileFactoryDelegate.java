package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.Util;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.TileKey;

/**
 * Implementation of {@link TileFactoryDelegate} which creates standard
 * {@link TextureTile}s. Also transforms tile's {@link TileKey} by adding a
 * random string to the data cache name. This means that layers with the same
 * data cache name will be cached separately in memory.
 * 
 * @author Michael de Hoog
 */
public class TextureTileFactoryDelegate implements TileFactoryDelegate
{
	private final static String DEFINITION_STRING = "TextureTile";

	public static TileKey appendToTileKeyCacheName(TileKey tileKey, String cacheName)
	{
		return new TileKey(tileKey.getLevelNumber(), tileKey.getRow(), tileKey.getColumn(),
				tileKey.getCacheName() + cacheName);
	}

	private final String random = "_" + Util.randomString(8);

	@Override
	public TextureTile createTextureTile(Sector sector, Level level, int row, int col)
	{
		return new DelegatorTextureTile(sector, level, row, col, this);
	}

	@Override
	public TileKey transformTileKey(TileKey tileKey)
	{
		return appendToTileKeyCacheName(tileKey, random);
	}

	@Override
	public Delegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new TextureTileFactoryDelegate();
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
