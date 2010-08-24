package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Level;

public class TextureTileFactoryDelegate implements TileFactoryDelegate
{
	public final static String DEFINITION_STRING = "TextureTile";
	
	@Override
	public TextureTile createTextureTile(Sector sector, Level level, int row, int col)
	{
		return new DelegatorTextureTile(sector, level, row, col, this);
	}

	@Override
	public Delegate fromDefinition(String definition)
	{
		if(definition.equalsIgnoreCase(DEFINITION_STRING))
			return new TextureTileFactoryDelegate();
		return null;
	}

	@Override
	public String toDefinition()
	{
		return DEFINITION_STRING;
	}
}
