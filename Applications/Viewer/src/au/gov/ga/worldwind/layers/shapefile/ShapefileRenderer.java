package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.render.DrawContext;

import java.awt.Color;
import java.util.List;

import au.gov.ga.worldwind.util.HSLColor;

import nasa.worldwind.retrieve.ExtendedRetrievalService.SectorPolyline;

public class ShapefileRenderer
{
	private boolean showImageTileOutlines;

	public void setShowImageTileOutlines(boolean showImageTileOutlines)
	{
		this.showImageTileOutlines = showImageTileOutlines;
	}

	public void renderTiles(DrawContext dc, List<ShapefileTile> tiles)
	{
		for (ShapefileTile tile : tiles)
		{
			tile.render(dc);

			if (showImageTileOutlines)
			{
				SectorPolyline polyline = new SectorPolyline(tile.getSector());
				polyline.setColor(getColor(tile.getLevelNumber()));
				polyline.render(dc);
			}
		}
	}

	private Color getColor(int level)
	{
		HSLColor color = new HSLColor(level * 60, 100, 50);
		return color.getRGB();
	}
}
