package au.gov.ga.worldwind.animator.layers.file;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.LevelSet;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMaskTiledImageLayer;

public class FileMaskTiledImageLayer extends ImmediateMaskTiledImageLayer
{
	//superclass MaskTiledImageLayer already supports File protocol,
	//so we don't have to override anything here 

	public FileMaskTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);
	}

	public FileMaskTiledImageLayer(AVList params)
	{
		super(params);
	}

	public FileMaskTiledImageLayer(String stateInXml)
	{
		super(stateInXml);
	}
}
