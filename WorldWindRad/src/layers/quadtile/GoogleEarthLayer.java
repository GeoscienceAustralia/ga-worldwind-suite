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
package layers.quadtile;

//import org.apache.log4j.Logger;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;

/**
 * Tile Naming Conventions
 * http://modestmaps.mapstraction.com/trac/wiki/TileNamingConventions Google
 * Examples Satellite (jpg)
 * http://kh0.google.com/kh?n=404&v=17&t=tqtsqrqtrtttqsqsrrtr Map (png)
 * http://mt1.google.com/mt?n=404&v=w2.39&x=329&y=792&zoom=6 Hybrid (png)
 * http://mt1.google.com/mt?n=404&v=w2t.39&x=329&y=792&zoom=6 Rules
 * http://[server].google.com/tiles/kh?n=404&v=[version]&t=[location]
 * 
 * Servers: Choice of four, encountered: kh0, kh1, kh2, kh3. Version:
 * Occasionally-updated integer, currently 17 (Feb 10). Location: Array of
 * successive zooms, starting at t for whole planet. o q = upper left, r = upper
 * right, s = lower right, t = lower left. o Number of characters = amount of
 * zoom, 1 = whole planet, 20 = maximum zoom for certain metro areas.
 * 
 * http://[server].google.com/mt?n=404&v=[version]&x=[column]&y=[row]&zoom=[zoom
 * ]
 * 
 * Servers: Choice of four, encountered: mt0, mt1, mt2, mt3. Version:
 * Occasionally-updated value, currently 2.39 and 2t.39 (Feb 10). Row, Column:
 * 0, 0 = upper-left. Zoom: Lower number = tighter zoom, 1 = maximum zoom, 17 =
 * whole planet.
 * 
 * @author Vladimir Silva
 * 
 */
public class GoogleEarthLayer extends AbstractQuadKeyLayer
{
	//private static final Logger logger = Logger.getLogger(GoogleEarthLayer.class);

	static final String cacheRoot = "GoogleEarth/";

	// Only satellite tiles are supported (jpg)
	private final String mapExtension = ".jpg";

	// default transparency
	private double opacity = 0.9;


	/**
	 * Constructor
	 */
	public GoogleEarthLayer()
	{
		super("Google Earth");
		super.cacheRoot = "GoogleEarth/";
		setOpacity(opacity);
	}


	@Override
	protected void doRender(DrawContext dc)
	{
		super.doRenderTiles(dc);

		//		// return if not at the min display zoom level
		//		if ( zoomLevel < minZoomLevel) 
		//			return;
		//
		//		// render MT (mass transit) tile 
		//		final String mtUrl = buildMtTileUrl(tileX, tileY, 17 - zoomLevel);
		//		final String mtKey = tileX + "." + tileY + ".png";
		//		
		//		renderTile(dc, mtKey , mtUrl, sector);
	}


	/**
	 * Build mass transit tile PNG urls
	 * 
	 * @param zoom
	 * @return
	 */
	//	private String buildMtTileUrl(int tileX, int tileY, int zoom) {
	//		// format: http://mt[0-3].google.com/mt?n=404&v=w2t.63&x=1916&y=3187&zoom=4
	//		// Use a rand server # (0-3) Zoom level start at 17 (whole earth)
	//		return "http://mt"
	//			+ (int)(Math.random() * 4.0) 
	//			+ ".google.com/mt?n=404&v=w2t.63&x=" + tileX  
	//			+ "&y=" + tileY + "&zoom=" + zoom;
	//		
	//	}
	/**
	 * Render a neighbor tile
	 * 
	 * @param tileX
	 *            tile column
	 * @param tileY
	 *            tile row
	 * @param zoomLevel
	 * @param dc
	 */
	protected void renderNeighbor(int tileX, int tileY, int zoomLevel,
			DrawContext dc)
	{
		String quadKey = TileToQuadKey(tileX, tileY, zoomLevel);
		Sector sector = QuadKeyToSector(quadKey);

		Sector visible = dc.getVisibleSector();
		Sector intersection = visible.intersection(sector);

		boolean insideView = intersection != null;

		// tile outside eye view
		if (!insideView)
			return;


		String tileURL = buildRequestUrl(quadKey, null, null);

		renderTile(dc, quadKey + mapExtension, tileURL, sector);

		// render ass transtit (MT) tile
		//		final String mtUrl = buildMtTileUrl(tileX, tileY, 17 - zoomLevel);
		//		final String mtKey = tileX + "." + tileY + ".png";
		//		
		//		renderTile(dc, mtKey , mtUrl, sector);
	}


	/**
	 * Build tile request URL. Quad Key: Array of successive zooms, starting at
	 * t for whole planet.
	 * 
	 * @param mapType
	 * @param quadKey
	 * @param mapExtension
	 * @return
	 */
	protected String buildRequestUrl(String quadKey, String mapType,
			String mapExtension)
	{
		// random server # (0-3)
		int server = (int) (Math.random() * 4.0);
		return "http://kh" + server + ".google.com/kh?n=404&v=23&t=t" + quadKey;
	}


	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if (!enabled)
			loadingTiles.clear();
	}


	/**
	 * Get the quad key for a tile column and row at zoom. Quad Key: Array of
	 * successive zooms, starting at t for whole planet.
	 * 
	 * q = upper left, r = upper right, s = lower right, t = lower left.
	 * Example: Satellite (jpg)
	 * http://kh0.google.com/kh?n=404&v=17&t=tqtsqrqtrtttqsqsrrtr
	 * 
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
			if (cell == 0)
				quad += "q";
			if (cell == 1)
				quad += "r";
			if (cell == 2)
				quad += "t";
			if (cell == 3)
				quad += "s";
		}
		return quad;
	}


	/**
	 * Returns the bounding box for a grid square represented by the given quad
	 * key
	 * 
	 * @param quadKey
	 * @param x
	 * @param y
	 * @param zoomLevel
	 * @return
	 */
	protected Box QuadKeyToBox(String quadKey, int x, int y, int zoomLevel)
	{
		char c = quadKey.charAt(0);

		int tileSize = 2 << (18 - zoomLevel - 1);

		if (c == 'q')
		{
			y = y - tileSize;
		}
		else if (c == 'r')
		{
			y = y - tileSize;
			x = x + tileSize;
		}

		else if (c == 's')
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
