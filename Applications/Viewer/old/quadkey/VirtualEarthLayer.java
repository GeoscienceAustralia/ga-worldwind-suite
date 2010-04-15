/*******************************************************************************
 * Copyright (c) 2006 Vladimir Silva and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Silva - initial API and implementation
 *******************************************************************************/
package quadkey;


//import org.apache.log4j.Logger;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceTileRenderer;

/**
 * Virtual Earth Tile System: 
 * http://msdn2.microsoft.com/en-us/library/bb259689.aspx
 * 
 * MS VE uses a mercator projection which significantly distorts scale and area (particularly near the poles).
 * 
 * To optimize the performance of map retrieval and display, the rendered map is cut into tiles of 256 x 256 pixels each. 
 * As the number of pixels differs at each level of detail, so does the number of tiles:
 * 
 * To optimize the indexing and storage of tiles, the two-dimensional tile XY coordinates are combined
 * into one-dimensional strings called quadtree keys, or “quadkeys” for short. Each quadkey uniquely identifies 
 * a single tile at a particular level of detail.
 * 
 * This layer works by computing the quad key and the lat/lon sector of the eye position tile and the rendering neighbor 
 * tiles in a circular pattern.
 * 
 * The VE projection (Mecator) doesn't match the ellipsoidal lat/lon projection of World Wind, however by computing the
 * sector of the VE tile and rendering using the {@link SurfaceTileRenderer} the original image is warped to fit the
 * target projection (WW). This seems to be correct at high zoom levels.
 * 
 * @author Vladimir Silva
 *
 */
public class VirtualEarthLayer extends AbstractQuadKeyLayer
{
	//private static final Logger logger = Logger.getLogger(VirtualEarthLayer.class);
	
	// default transparency
	private double opacity = 0.8;

	/**
	 * Constructor
	 */
	public VirtualEarthLayer() { 
		super("MS Virtual Earth");
		super.cacheRoot = "VirtualEarth/";
		setOpacity(opacity);
	}
	
	@Override
	protected void doRender(DrawContext dc) 
	{
		super.doRenderTiles(dc);
	}

	
	/**
	 * Render a neighbor tile
	 * @param tileX tile column
	 * @param tileY tile row
	 * @param zoomLevel
	 * @param dc
	 */
	protected void renderNeighbor(int tileX, int tileY, int zoomLevel, DrawContext dc)
	{
		String quadKey 		= TileToQuadKey(tileX, tileY, zoomLevel);
		Sector sector 		= QuadKeyToSector(quadKey);
		Sector visible		= dc.getVisibleSector();
		Sector intersection = visible.intersection(sector);
		
		boolean insideView = intersection != null;
		
		if ( ! insideView ) {
			return;
		}
		
		
		String tileURL = buildRequestUrl(quadKey, getMapType(), mapExtension);
		
		renderTile(dc, quadKey + mapExtension, tileURL, sector);
	}
	
	/**
	 * Build tile request URL 
	 * @param mapType
	 * @param quadKey
	 * @param mapExtension
	 * @return
	 */
	protected String buildRequestUrl(String quadKey, String mapType, String mapExtension) {
		return "http://" + mapType
	    	+ quadKey.charAt(quadKey.length() - 1)
	    	+ ".ortho.tiles.virtualearth.net/tiles/"
	    	+ mapType + quadKey + mapExtension 
	    	+ "?g=1";
	}
	
	
    @Override
    public void setEnabled(boolean enabled) 
    {
    	super.setEnabled(enabled);
    	if ( ! enabled ) loadingTiles.clear();
    }
    
	
	/**
	 * Get the VE quad key for a tile column and row at zoom
	 * @param tx
	 * @param ty
	 * @param zl
	 * @return
	 */
	protected String TileToQuadKey(int tx, int ty, int zl)
	{
		String quad = "";
		for (int i = zl; i > 0; i--)
		{
			int mask = 1 << (i - 1);
			int cell = 0;
			if ((tx & mask) != 0)
			{
				cell++;
			}
			if ((ty & mask) != 0)
			{
				cell += 2;
			}
			quad += cell;
		}
		return quad;
	}
	

	/**
	 * Returns the bounding box for a grid square represented by
	 * the given quad key
	 * @param quadKey
	 * @param x
	 * @param y
	 * @param zoomLevel
	 * @return
	 */
    public Box QuadKeyToBox(String quadKey, int x, int y, int zoomLevel)
    {
        char c = quadKey.charAt(0);

        int tileSize = 2 << (18 - zoomLevel - 1);

        if (c == '0')
        {
            y = y - tileSize;
        }

        else if (c == '1')
        {
            y = y - tileSize;
            x = x + tileSize;
        }

        else if (c == '3')
        {
            x = x + tileSize;
        }

        if (quadKey.length() > 1)
        {
            return QuadKeyToBox(quadKey.substring(1), x, y, zoomLevel + 1);
        }
        else
        {
            return new Box(x, y, tileSize, tileSize);
        } 
    }
}
