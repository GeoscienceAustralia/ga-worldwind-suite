package au.gov.ga.worldwind.viewer.terrain;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.RectangularTessellator;
import gov.nasa.worldwind.terrain.RectangularTessellatorAccessible;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;

public class WireframeRectangularTessellator extends RectangularTessellatorAccessible
{
	private boolean wireframeDepthTesting = true;
	private boolean backfaceCulling = false;
	private boolean smartSkirts = true;

	public boolean isWireframeDepthTesting()
	{
		return wireframeDepthTesting;
	}

	public void setWireframeDepthTesting(boolean wireframeDepthTesting)
	{
		this.wireframeDepthTesting = wireframeDepthTesting;
	}

	public boolean isBackfaceCulling()
	{
		return backfaceCulling;
	}

	public void setBackfaceCulling(boolean backfaceCulling)
	{
		this.backfaceCulling = backfaceCulling;
	}

	public boolean isSmartSkirts()
	{
		return smartSkirts;
	}

	public void setSmartSkirts(boolean smartSkirts)
	{
		this.smartSkirts = smartSkirts;
	}

	@Override
	protected void renderWireframe(DrawContext dc, RectTile tile, boolean showTriangles, boolean showTileBoundary)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		RenderInfo ri = getRenderInfo(tile);

		if (ri == null)
		{
			String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		dc.getView().pushReferenceCenter(dc, getReferenceCenter(ri));

		javax.media.opengl.GL gl = dc.getGL();
		gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT | GL.GL_POLYGON_BIT | GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT
				| GL.GL_CURRENT_BIT);
		//gl.glEnable(GL.GL_BLEND);
		//gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
		//gl.glDisable(javax.media.opengl.GL.GL_DEPTH_TEST);
		gl.glEnable(javax.media.opengl.GL.GL_CULL_FACE);
		gl.glCullFace(javax.media.opengl.GL.GL_BACK);
		gl.glDisable(javax.media.opengl.GL.GL_TEXTURE_2D);
		gl.glColor4d(0.6, 0.8, 0.8, 1.0);
		gl.glPolygonMode(javax.media.opengl.GL.GL_FRONT, javax.media.opengl.GL.GL_LINE);

		if (isWireframeDepthTesting())
		{
			gl.glEnable(GL.GL_POLYGON_OFFSET_LINE);
			gl.glPolygonOffset(-1, 1);
		}
		else
		{
			gl.glDisable(javax.media.opengl.GL.GL_DEPTH_TEST);
		}

		if (showTriangles)
		{
			OGLStackHandler ogsh = new OGLStackHandler();

			try
			{
				ogsh.pushClientAttrib(gl, GL.GL_CLIENT_VERTEX_ARRAY_BIT);

				gl.glEnableClientState(GL.GL_VERTEX_ARRAY);

				DoubleBuffer vertices = getVertices(ri);
				IntBuffer indices = getIndices(ri);
				gl.glVertexPointer(3, GL.GL_DOUBLE, 0, vertices.rewind());
				gl.glDrawElements(javax.media.opengl.GL.GL_TRIANGLE_STRIP, indices.limit(),
						javax.media.opengl.GL.GL_UNSIGNED_INT, indices.rewind());
			}
			finally
			{
				ogsh.pop(gl);
			}
		}

		dc.getView().popReferenceCenter(dc);

		gl.glPopAttrib();

		if (showTileBoundary)
			this.renderPatchBoundary(dc, tile);
	}

	@Override
	protected long render(DrawContext dc, RectTile tile, int numTextureUnits)
	{
		if (!backfaceCulling)
		{
			dc.getGL().glDisable(GL.GL_CULL_FACE);
		}
		return super.render(dc, tile, numTextureUnits);
	}

	@Override
	public SectorGeometryList tessellate(DrawContext dc)
	{
		SectorGeometryList currentTiles = super.tessellate(dc);

		if (smartSkirts)
		{
			Map<RectTileKey, RowColRectTile> tileMap = new HashMap<RectTileKey, RowColRectTile>();
			for (SectorGeometry t : currentTiles)
			{
				RowColRectTile tile = (RowColRectTile) t;
				RectTileKey tileKey = new RectTileKey(tile.getLevel(), tile.getRow(), tile.getColumn());
				tileMap.put(tileKey, tile);
			}
			for (SectorGeometry tile : currentTiles)
			{
				fixSkirts(dc, (RowColRectTile) tile, tileMap);
			}
		}

		return currentTiles;
	}

	@Override
	protected void makeVerts(DrawContext dc, RectTile tile)
	{
		//vertices are rebuilt if required in the super method
		((RowColRectTile) tile).rebuiltVertices = false;
		super.makeVerts(dc, tile);
	}

	@Override
	public RenderInfo buildVerts(DrawContext dc, RectTile tile, boolean makeSkirts)
	{
		//mark the tile's vertices as rebuilt
		((RowColRectTile) tile).rebuiltVertices = true;
		return super.buildVerts(dc, tile, false);
	}

	@Override
	protected ArrayList<LatLon> computeLocations(RectTile tile)
	{
		//Changed to remove the latMax/lonMax calculations, as the small difference in the double
		//lat/lon locations between the skirts and the tile edges were causing large differences
		//in the returned elevation. Perhaps an ElevationModel bug?

		int density = getDensity(tile);
		int numVertices = (density + 3) * (density + 3);

		Sector sector = tile.getSector();
		Angle dLat = sector.getDeltaLat().divide(density);
		Angle lat = sector.getMinLatitude();

		Angle lonMin = sector.getMinLongitude();
		Angle dLon = sector.getDeltaLon().divide(density);

		ArrayList<LatLon> latlons = new ArrayList<LatLon>(numVertices);
		for (int j = 0; j <= density + 2; j++)
		{
			Angle lon = lonMin;
			for (int i = 0; i <= density + 2; i++)
			{
				latlons.add(new LatLon(lat, lon));

				if (i != 0 && i <= density)
					lon = lon.add(dLon);

				if (lon.degrees < -180)
					lon = Angle.NEG180;
				else if (lon.degrees > 180)
					lon = Angle.POS180;
			}

			if (j != 0 && j <= density)
				lat = lat.add(dLat);
		}

		return latlons;
	}

	protected void fixSkirts(DrawContext dc, RowColRectTile tile, Map<RectTileKey, RowColRectTile> tileMap)
	{
		int row = tile.getRow();
		int column = tile.getColumn();
		int level = tile.getLevel();
		int sRow = row / 2;
		int sColumn = column / 2;
		int sLevel = level - 1;
		boolean topHalf = row % 2 == 0;
		boolean leftHalf = column % 2 == 0;

		RowColRectTile sLeft = leftHalf ? tileMap.get(new RectTileKey(sLevel, sRow, sColumn - 1)) : null;
		RowColRectTile sRight = !leftHalf ? tileMap.get(new RectTileKey(sLevel, sRow, sColumn + 1)) : null;
		RowColRectTile sTop = topHalf ? tileMap.get(new RectTileKey(sLevel, sRow - 1, sColumn)) : null;
		RowColRectTile sBottom = !topHalf ? tileMap.get(new RectTileKey(sLevel, sRow + 1, sColumn)) : null;

		RowColRectTile left = sLeft == null ? tileMap.get(new RectTileKey(level, row, column - 1)) : null;
		RowColRectTile top = sTop == null ? tileMap.get(new RectTileKey(level, row - 1, column)) : null;

		boolean anyRebuilt =
				tile.rebuiltVertices || (sLeft != null && sLeft.rebuiltVertices)
						|| (sRight != null && sRight.rebuiltVertices) || (sTop != null && sTop.rebuiltVertices)
						|| (sBottom != null && sBottom.rebuiltVertices) || (left != null && left.rebuiltVertices)
						|| (top != null && top.rebuiltVertices);
		if (!anyRebuilt)
			return;

		DoubleBuffer vertices = getVertices(getRenderInfo(tile));
		Vec4 refCenter = getReferenceCenter(getRenderInfo(tile));
		int density = getDensity(tile);
		int size = density + 3;

		if (sLeft != null)
		{
			DoubleBuffer leftVertices = getVertices(getRenderInfo(sLeft));
			Vec4 leftRefCenter = getReferenceCenter(getRenderInfo(sLeft));
			int srcStart = topHalf ? 1 : density / 2 + 1;
			subdivideVerticesFromNeighboringSuperTile(leftVertices, vertices, leftRefCenter, refCenter, size, srcStart,
					size - 1, 0, true);
		}
		else if (left != null)
		{
			DoubleBuffer leftVertices = getVertices(getRenderInfo(left));
			Vec4 leftRefCenter = getReferenceCenter(getRenderInfo(left));
			copyVerticesFromNeighboringTile(leftVertices, vertices, leftRefCenter, refCenter, size, size - 1, 0, true);
		}
		if (sRight != null)
		{
			DoubleBuffer rightVertices = getVertices(getRenderInfo(sRight));
			Vec4 rightRefCenter = getReferenceCenter(getRenderInfo(sRight));
			int srcStart = topHalf ? 1 : density / 2 + 1;
			subdivideVerticesFromNeighboringSuperTile(rightVertices, vertices, rightRefCenter, refCenter, size,
					srcStart, 0, size - 1, true);
		}
		if (sTop != null)
		{
			DoubleBuffer topVertices = getVertices(getRenderInfo(sTop));
			Vec4 topRefCenter = getReferenceCenter(getRenderInfo(sTop));
			int srcStart = leftHalf ? 1 : density / 2 + 1;
			subdivideVerticesFromNeighboringSuperTile(topVertices, vertices, topRefCenter, refCenter, size, srcStart,
					size - 1, 0, false);
		}
		else if (top != null)
		{
			DoubleBuffer topVertices = getVertices(getRenderInfo(top));
			Vec4 topRefCenter = getReferenceCenter(getRenderInfo(top));
			copyVerticesFromNeighboringTile(topVertices, vertices, topRefCenter, refCenter, size, size - 1, 0, false);
		}
		if (sBottom != null)
		{
			DoubleBuffer bottomVertices = getVertices(getRenderInfo(sBottom));
			Vec4 bottomRefCenter = getReferenceCenter(getRenderInfo(sBottom));
			int srcStart = leftHalf ? 1 : density / 2 + 1;
			subdivideVerticesFromNeighboringSuperTile(bottomVertices, vertices, bottomRefCenter, refCenter, size,
					srcStart, 0, size - 1, false);
		}

		if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
		{
			GL gl = dc.getGL();
			OGLStackHandler ogsh = new OGLStackHandler();

			try
			{
				ogsh.pushClientAttrib(gl, GL.GL_CLIENT_VERTEX_ARRAY_BIT);
				int bufferIdVertices = getBufferIdVertices(getRenderInfo(tile));

				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferIdVertices);
				gl.glBufferData(GL.GL_ARRAY_BUFFER, vertices.limit() * 8, vertices.rewind(), GL.GL_DYNAMIC_DRAW);
			}
			finally
			{
				ogsh.pop(gl);
			}
		}
	}

	private void subdivideVerticesFromNeighboringSuperTile(DoubleBuffer src, DoubleBuffer dst, Vec4 srcRefCenter,
			Vec4 dstRefCenter, int size, int srcStart, int srcRC, int dstRC, boolean column)
	{
		int offsetFactor = (column ? 1 : size) * 3;
		int srcOffset = srcRC * offsetFactor;
		int dstOffset = dstRC * offsetFactor;
		int stride = (column ? size : 1) * 3;

		Vec4 last = null;
		for (int di = 1, si = srcStart; di < size - 1; di += 2, si++)
		{
			int srcIndex = srcOffset + si * stride;
			Vec4 current = new Vec4(src.get(srcIndex), src.get(srcIndex + 1), src.get(srcIndex + 2));
			current = current.add3(srcRefCenter).subtract3(dstRefCenter);

			Vec4 previous = last == null ? current : last.add3(current).divide3(2);
			last = current;

			int dstIndex = dstOffset + (di - 1) * stride;
			dst.put(dstIndex, previous.x).put(dstIndex + 1, previous.y).put(dstIndex + 2, previous.z);

			dstIndex += stride;
			dst.put(dstIndex, current.x).put(dstIndex + 1, current.y).put(dstIndex + 2, current.z);

			if (di >= size - 2)
			{
				dstIndex += stride;
				dst.put(dstIndex, current.x).put(dstIndex + 1, current.y).put(dstIndex + 2, current.z);
			}
		}
	}

	private void copyVerticesFromNeighboringTile(DoubleBuffer src, DoubleBuffer dst, Vec4 srcRefCenter,
			Vec4 dstRefCenter, int size, int srcRC, int dstRC, boolean column)
	{
		int offsetFactor = (column ? 1 : size) * 3;
		int srcOffset = srcRC * offsetFactor;
		int dstOffset = dstRC * offsetFactor;
		int stride = (column ? size : 1) * 3;

		for (int i = 0; i < size; i++)
		{
			//don't use skirts to copy from
			int srcIndex = srcOffset + i * stride;
			int dstIndex = dstOffset + i * stride;
			dst.put(dstIndex, src.get(srcIndex) + srcRefCenter.x - dstRefCenter.x);
			dst.put(dstIndex + 1, src.get(srcIndex + 1) + srcRefCenter.y - dstRefCenter.y);
			dst.put(dstIndex + 2, src.get(srcIndex + 2) + srcRefCenter.z - dstRefCenter.z);
		}
	}

	@Override
	protected RectTile[] split(DrawContext dc, RectTile tile)
	{
		//override the split() function to speed up the row/column calculation
		//we don't need to override the createTopLevelTiles() function, as it's only called once

		Sector[] sectors = tile.getSector().subdivide();

		int row = ((RowColRectTile) tile).getRow() * 2;
		int column = ((RowColRectTile) tile).getColumn() * 2;

		RectTile[] subTiles = new RectTile[4];
		subTiles[0] = this.createTile(dc, sectors[0], getLevel(tile) + 1, row, column);
		subTiles[1] = this.createTile(dc, sectors[1], getLevel(tile) + 1, row, column + 1);
		subTiles[2] = this.createTile(dc, sectors[2], getLevel(tile) + 1, row + 1, column);
		subTiles[3] = this.createTile(dc, sectors[3], getLevel(tile) + 1, row + 1, column + 1);

		return subTiles;
	}

	@Override
	protected RectTile createTile(DrawContext dc, Sector tileSector, int level)
	{
		double deltaLat = 180d / DEFAULT_NUM_LAT_SUBDIVISIONS;
		double deltaLon = 360d / DEFAULT_NUM_LON_SUBDIVISIONS;

		LatLon centroid = tileSector.getCentroid();
		int row = getTileY(centroid.latitude, Angle.NEG90, level, deltaLat);
		int column = getTileX(centroid.longitude, Angle.NEG180, level, deltaLon);

		return createTile(dc, tileSector, level, row, column);
	}

	protected RectTile createTile(DrawContext dc, Sector tileSector, int level, int row, int column)
	{
		Extent extent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), tileSector);
		double cellSize = tileSector.getDeltaLatRadians() * dc.getGlobe().getRadius() / this.density;

		return new RowColRectTile(this, extent, level, this.density, tileSector, cellSize, row, column);
	}

	protected static int getTileX(Angle longitude, Angle longitudeOrigin, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double X = (longitude.degrees - longitudeOrigin.degrees) / (lztsd * layerpow);
		return (int) X;
	}

	protected static int getTileY(Angle latitude, Angle latitudeOrigin, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double Y = (latitude.degrees - latitudeOrigin.degrees) / (lztsd * layerpow);
		return (int) Y;
	}

	protected static class RowColRectTile extends RectTile
	{
		protected boolean rebuiltVertices = false;
		protected final int row;
		protected final int column;

		public RowColRectTile(RectangularTessellator tessellator, Extent extent, int level, int density, Sector sector,
				double cellSize, int row, int column)
		{
			super(tessellator, extent, level, density, sector, cellSize);
			this.row = row;
			this.column = column;
		}

		public int getLevel()
		{
			return level;
		}

		public int getRow()
		{
			return row;
		}

		public int getColumn()
		{
			return column;
		}

		@Override
		public String toString()
		{
			return "(" + level + "," + row + "," + column + ")";
		}
	}

	protected static class RectTileKey
	{
		protected final int level;
		protected final int row;
		protected final int column;

		public RectTileKey(int level, int row, int column)
		{
			this.level = level;
			this.row = row;
			this.column = column;
		}

		@Override
		public int hashCode()
		{
			int result;
			result = level;
			result = 29 * result + row;
			result = 29 * result + column;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof RectTileKey)
			{
				RectTileKey key = (RectTileKey) obj;
				return key.level == this.level && key.column == this.column && key.row == this.row;
			}
			return false;
		}
	}
}
