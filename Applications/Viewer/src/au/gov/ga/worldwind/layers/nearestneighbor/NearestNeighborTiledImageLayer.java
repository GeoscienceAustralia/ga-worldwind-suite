package au.gov.ga.worldwind.layers.nearestneighbor;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;

import java.util.ArrayList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.layers.ExtendedTiledImageLayer;

public class NearestNeighborTiledImageLayer extends ExtendedTiledImageLayer
{
	public NearestNeighborTiledImageLayer(Element domElement, AVList params)
	{
		super(domElement, params);
	}

	//*********************************************************************
	//Below here is copied (with slight modifications) from TiledImageLayer
	//*********************************************************************
	
	private ArrayList<TextureTile> topLevels;
	
	@Override
	public ArrayList<TextureTile> getTopLevels()
	{
		if (this.topLevels == null)
			this.createTopLevelTiles();

		return topLevels;
	}

	private void createTopLevelTiles()
	{
		LevelSet levels = getLevels();
		Sector sector = levels.getSector();

		Level level = levels.getFirstLevel();
		Angle dLat = level.getTileDelta().getLatitude();
		Angle dLon = level.getTileDelta().getLongitude();
		Angle latOrigin = levels.getTileOrigin().getLatitude();
		Angle lonOrigin = levels.getTileOrigin().getLongitude();

		// Determine the row and column offset from the common World Wind global tiling origin.
		int firstRow = Tile.computeRow(dLat, sector.getMinLatitude(), latOrigin);
		int firstCol = Tile.computeColumn(dLon, sector.getMinLongitude(), lonOrigin);
		int lastRow = Tile.computeRow(dLat, sector.getMaxLatitude(), latOrigin);
		int lastCol = Tile.computeColumn(dLon, sector.getMaxLongitude(), lonOrigin);

		int nLatTiles = lastRow - firstRow + 1;
		int nLonTiles = lastCol - firstCol + 1;

		this.topLevels = new ArrayList<TextureTile>(nLatTiles * nLonTiles);

		Angle p1 = Tile.computeRowLatitude(firstRow, dLat, latOrigin);
		for (int row = firstRow; row <= lastRow; row++)
		{
			Angle p2;
			p2 = p1.add(dLat);

			Angle t1 = Tile.computeColumnLongitude(firstCol, dLon, lonOrigin);
			for (int col = firstCol; col <= lastCol; col++)
			{
				Angle t2;
				t2 = t1.add(dLon);

				this.topLevels.add(new NearestNeighborTextureTile(new Sector(p1, p2, t1, t2), level, row, col));
				t1 = t2;
			}
			p1 = p2;
		}
	}
	
    @Override
	public TextureTile[][] getTilesInSector(Sector sector, int levelNumber)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        LevelSet levels = getLevels();
        Level targetLevel = levels.getLastLevel();
        if (levelNumber >= 0)
        {
            for (int i = levelNumber; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
            {
                if (levels.isLevelEmpty(i))
                    continue;

                targetLevel = levels.getLevel(i);
                break;
            }
        }

        // Collect all the tiles intersecting the input sector.
        LatLon delta = targetLevel.getTileDelta();
        LatLon origin = levels.getTileOrigin();
        final int nwRow = Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude(), origin.getLatitude());
        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin.getLongitude());
        final int seRow = Tile.computeRow(delta.getLatitude(), sector.getMinLatitude(), origin.getLatitude());
        final int seCol = Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin.getLongitude());

        int numRows = nwRow - seRow + 1;
        int numCols = seCol - nwCol + 1;
        TextureTile[][] sectorTiles = new TextureTile[numRows][numCols];

        for (int row = nwRow; row >= seRow; row--)
        {
            for (int col = nwCol; col <= seCol; col++)
            {
                TileKey key = new TileKey(targetLevel.getLevelNumber(), row, col, targetLevel.getCacheName());
                Sector tileSector = levels.computeSectorForKey(key);
                sectorTiles[nwRow - row][col - nwCol] = new NearestNeighborTextureTile(tileSector, targetLevel, row, col);
            }
        }

        return sectorTiles;
    }
}
