package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLTextRenderer;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.Tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import javax.media.opengl.GL;

import com.sun.opengl.util.j2d.TextRenderer;

public abstract class TiledShapefileLayer extends AbstractLayer
{
	// Infrastructure
	private static final LevelComparer levelComparer = new LevelComparer();
	private final LevelSet levels;
	private ArrayList<ShapefileTile> topLevels;
	private boolean forceLevelZeroLoads = false;
	private boolean levelZeroLoaded = false;
	private boolean retainLevelZeroTiles = false;
	private String tileCountName;
	private double splitScale = 0.9;

	// Diagnostic flags
	private boolean showImageTileOutlines = false;
	private boolean drawTileBoundaries = false;
	private boolean drawTileIDs = false;
	private boolean drawBoundingVolumes = false;

	// Stuff computed each frame
	private ArrayList<ShapefileTile> currentTiles = new ArrayList<ShapefileTile>();
	private ShapefileTile currentResourceTile;
	private boolean atMaxResolution = false;
	private PriorityBlockingQueue<Runnable> requestQ = new PriorityBlockingQueue<Runnable>(200);

	private ShapefileRenderer renderer = new ShapefileRenderer();

	abstract protected void requestTile(DrawContext dc, ShapefileTile tile);

	abstract protected void forceTileLoad(ShapefileTile tile);

	public TiledShapefileLayer(LevelSet levelSet)
	{
		if (levelSet == null)
		{
			String message = Logging.getMessage("nullValue.LevelSetIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.levels = new LevelSet(levelSet); // the caller's levelSet may change internally, so we copy it.

		//        this.createTopLevelTiles();

		this.tileCountName = this.getName() + " Tiles";
	}

	@Override
	public Object setValue(String key, Object value)
	{
		// Offer it to the level set
		if (this.getLevels() != null)
			this.getLevels().setValue(key, value);

		return super.setValue(key, value);
	}

	@Override
	public Object getValue(String key)
	{
		Object value = super.getValue(key);

		return value != null ? value : this.getLevels().getValue(key); // see if the level set has it
	}

	@Override
	public void setName(String name)
	{
		super.setName(name);
		this.tileCountName = this.getName() + " Tiles";
	}

	public boolean isForceLevelZeroLoads()
	{
		return this.forceLevelZeroLoads;
	}

	public void setForceLevelZeroLoads(boolean forceLevelZeroLoads)
	{
		this.forceLevelZeroLoads = forceLevelZeroLoads;
	}

	public boolean isRetainLevelZeroTiles()
	{
		return retainLevelZeroTiles;
	}

	public void setRetainLevelZeroTiles(boolean retainLevelZeroTiles)
	{
		this.retainLevelZeroTiles = retainLevelZeroTiles;
	}

	public boolean isDrawTileIDs()
	{
		return drawTileIDs;
	}

	public void setDrawTileIDs(boolean drawTileIDs)
	{
		this.drawTileIDs = drawTileIDs;
	}

	public boolean isDrawTileBoundaries()
	{
		return drawTileBoundaries;
	}

	public void setDrawTileBoundaries(boolean drawTileBoundaries)
	{
		this.drawTileBoundaries = drawTileBoundaries;
	}

	public boolean isShowImageTileOutlines()
	{
		return showImageTileOutlines;
	}

	public void setShowImageTileOutlines(boolean showImageTileOutlines)
	{
		this.showImageTileOutlines = showImageTileOutlines;
	}

	public boolean isDrawBoundingVolumes()
	{
		return drawBoundingVolumes;
	}

	public void setDrawBoundingVolumes(boolean drawBoundingVolumes)
	{
		this.drawBoundingVolumes = drawBoundingVolumes;
	}

	/**
	 * Sets the parameter controlling a layer's displayed resolution as distance
	 * changes between the globe's surface and the eye point. Higher resolution
	 * is displayed as the split scale increases from 1.0. Lower resolution is
	 * displayed as the split scale decreases from 1.0. The default value is
	 * specified in the layer's configuration, or is 0.9 if not specified there.
	 * 
	 * @param splitScale
	 *            a value near 1.0 that controls the image resolution as the
	 *            distance between the globe's surface and the eye point change.
	 *            Increasing values select higher resolution, decreasing values
	 *            select lower resolution. Typical values range between 0.8 and
	 *            1.2.
	 */
	public void setSplitScale(double splitScale)
	{
		this.splitScale = splitScale;
	}

	/**
	 * Returns the split scale value controlling image resolution relative to
	 * the distance between the globe's surface at the image position and the
	 * eye point.
	 * 
	 * @return the current split scale.
	 * 
	 * @see #setSplitScale(double)
	 */
	public double getSplitScale()
	{
		return this.splitScale;
	}

	protected LevelSet getLevels()
	{
		return levels;
	}

	protected PriorityBlockingQueue<Runnable> getRequestQ()
	{
		return requestQ;
	}

	@Override
	public boolean isMultiResolution()
	{
		return this.getLevels() != null && this.getLevels().getNumLevels() > 1;
	}

	@Override
	public boolean isAtMaxResolution()
	{
		return this.atMaxResolution;
	}

	/**
	 * Specifies the time of the layer's most recent dataset update, beyond
	 * which cached data is invalid. If greater than zero, the layer ignores and
	 * eliminates any in-memory or on-disk cached data older than the time
	 * specified, and requests new information from the data source. If zero,
	 * the default, the layer applies any expiry times associated with its
	 * individual levels, but only for on-disk cached data. In-memory cached
	 * data is expired only when the expiry time is specified with this method
	 * and is greater than zero. This method also overwrites the expiry times of
	 * the layer's individual levels if the value specified to the method is
	 * greater than zero.
	 * 
	 * @param expiryTime
	 *            the expiry time of any cached data, expressed as a number of
	 *            milliseconds beyond the epoch. The default expiry time is
	 *            zero.
	 * 
	 * @see System#currentTimeMillis() for a description of milliseconds beyond
	 *      the epoch.
	 */
	@Override
	public void setExpiryTime(long expiryTime) // Override this method to use intrinsic level-specific expiry times
	{
		super.setExpiryTime(expiryTime);

		if (expiryTime > 0)
			this.levels.setExpiryTime(expiryTime); // remove this in sub-class to use level-specific expiry times
	}

	public ArrayList<ShapefileTile> getTopLevels()
	{
		if (this.topLevels == null)
			this.createTopLevelTiles();

		return topLevels;
	}

	private void createTopLevelTiles()
	{
		Sector sector = this.levels.getSector();

		Level level = levels.getFirstLevel();
		Angle dLat = level.getTileDelta().getLatitude();
		Angle dLon = level.getTileDelta().getLongitude();
		Angle latOrigin = this.levels.getTileOrigin().getLatitude();
		Angle lonOrigin = this.levels.getTileOrigin().getLongitude();

		// Determine the row and column offset from the common World Wind global tiling origin.
		int firstRow = Tile.computeRow(dLat, sector.getMinLatitude(), latOrigin);
		int firstCol = Tile.computeColumn(dLon, sector.getMinLongitude(), lonOrigin);
		int lastRow = Tile.computeRow(dLat, sector.getMaxLatitude(), latOrigin);
		int lastCol = Tile.computeColumn(dLon, sector.getMaxLongitude(), lonOrigin);

		int nLatTiles = lastRow - firstRow + 1;
		int nLonTiles = lastCol - firstCol + 1;

		this.topLevels = new ArrayList<ShapefileTile>(nLatTiles * nLonTiles);

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

				this.topLevels.add(new ShapefileTile(new Sector(p1, p2, t1, t2), level, row, col));
				t1 = t2;
			}
			p1 = p2;
		}
	}

	private void loadAllTopLevelTiles(DrawContext dc)
	{
		for (ShapefileTile tile : this.getTopLevels())
		{
			if (!tile.isTileInMemory())
				this.forceTileLoad(tile);
		}

		this.levelZeroLoaded = true;
	}

	// ============== Tile Assembly ======================= //
	// ============== Tile Assembly ======================= //
	// ============== Tile Assembly ======================= //

	private void assembleTiles(DrawContext dc)
	{
		this.currentTiles.clear();

		for (ShapefileTile tile : this.getTopLevels())
		{
			if (this.isTileVisible(dc, tile))
			{
				this.currentResourceTile = null;
				this.addTileOrDescendants(dc, tile);
			}
		}
	}

	private void addTileOrDescendants(DrawContext dc, ShapefileTile tile)
	{
		if (this.meetsRenderCriteria(dc, tile))
		{
			this.addTile(dc, tile);
			return;
		}

		// The incoming tile does not meet the rendering criteria, so it must be subdivided and those
		// subdivisions tested against the criteria.

		// All tiles that meet the selection criteria are drawn, but some of those tiles will not have
		// textures associated with them either because their texture isn't loaded yet or because they
		// are finer grain than the layer has textures for. In these cases the tiles use the texture of
		// the closest ancestor that has a texture loaded. This ancestor is called the currentResourceTile.
		// A texture transform is applied during rendering to align the sector's texture coordinates with the
		// appropriate region of the ancestor's texture.

		ShapefileTile ancestorResource = null;

		try
		{
			// TODO: Revise this to reflect that the parent layer is only requested while the algorithm continues
			// to search for the layer matching the criteria.
			// At this point the tile does not meet the render criteria but it may have its texture in memory.
			// If so, register this tile as the resource tile. If not, then this tile will be the next level
			// below a tile with texture in memory. So to provide progressive resolution increase, add this tile
			// to the draw list. That will cause the tile to be drawn using its parent tile's texture, and it will
			// cause it's texture to be requested. At some future call to this method the tile's texture will be in
			// memory, it will not meet the render criteria, but will serve as the parent to a tile that goes
			// through this same process as this method recurses. The result of all this is that a tile isn't rendered
			// with its own texture unless all its parents have their textures loaded. In addition to causing
			// progressive resolution increase, this ensures that the parents are available as the user zooms out, and
			// therefore the layer remains visible until the user is zoomed out to the point the layer is no longer
			// active.
			if (tile.isTileInMemory() || tile.getLevelNumber() == 0)
			{
				ancestorResource = this.currentResourceTile;
				this.currentResourceTile = tile;
			}
			else if (!tile.getLevel().isEmpty())
			{
				//                this.addTile(dc, tile);
				//                return;

				// Issue a request for the parent before descending to the children.
				//                if (tile.getLevelNumber() < this.levels.getNumLevels())
				//                {
				//                    // Request only tiles with data associated at this level
				//                    if (!this.levels.isResourceAbsent(tile))
				//                        this.requestTexture(dc, tile);
				//                }
			}

			ShapefileTile[] subTiles =
					tile.createSubTiles(this.levels.getLevel(tile.getLevelNumber() + 1));
			for (ShapefileTile child : subTiles)
			{
				if (this.isTileVisible(dc, child))
					this.addTileOrDescendants(dc, child);
			}
		}
		finally
		{
			if (ancestorResource != null) // Pop this tile as the currentResource ancestor
				this.currentResourceTile = ancestorResource;
		}
	}

	private void addTile(DrawContext dc, ShapefileTile tile)
	{
		tile.setFallbackTile(null);

		if (tile.isTileInMemory())
		{
			this.addTileToCurrent(tile);
			return;
		}

		// Level 0 loads may be forced
		if (tile.getLevelNumber() == 0 && this.forceLevelZeroLoads && !tile.isTileInMemory())
		{
			this.forceTileLoad(tile);
			if (tile.isTileInMemory())
			{
				this.addTileToCurrent(tile);
				return;
			}
		}

		// Tile's texture isn't available, so request it
		if (tile.getLevelNumber() < this.levels.getNumLevels())
		{
			// Request only tiles with data associated at this level
			if (!this.levels.isResourceAbsent(tile))
				this.requestTile(dc, tile);
		}

		// Set up to use the currentResource tile's texture
		if (this.currentResourceTile != null)
		{
			if (this.currentResourceTile.getLevelNumber() == 0 && this.forceLevelZeroLoads
					&& !this.currentResourceTile.isTileInMemory()
					&& !this.currentResourceTile.isTileInMemory())
				this.forceTileLoad(this.currentResourceTile);

			if (this.currentResourceTile.isTileInMemory())
			{
				tile.setFallbackTile(currentResourceTile);
				this.addTileToCurrent(tile);
			}
		}
	}

	private void addTileToCurrent(ShapefileTile tile)
	{
		this.currentTiles.add(tile);
	}

	private boolean isTileVisible(DrawContext dc, ShapefileTile tile)
	{
		//        if (!(tile.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates())
		//            && (dc.getVisibleSector() == null || dc.getVisibleSector().intersects(tile.getSector()))))
		//            return false;
		//
		//        Position eyePos = dc.getView().getEyePosition();
		//        LatLon centroid = tile.getSector().getCentroid();
		//        Angle d = LatLon.greatCircleDistance(eyePos, centroid);
		//        if ((!tile.getLevelName().equals("0")) && d.compareTo(tile.getSector().getDeltaLat().multiply(2.5)) == 1)
		//            return false;
		//
		//        return true;
		//
		return tile.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates())
				&& (dc.getVisibleSector() == null || dc.getVisibleSector().intersects(
						tile.getSector()));
	}

	private boolean meetsRenderCriteria(DrawContext dc, ShapefileTile tile)
	{
		return this.levels.isFinalLevel(tile.getLevelNumber())
				|| !needToSplit(dc, tile.getSector());
	}

	private boolean needToSplit(DrawContext dc, Sector sector)
	{
		Vec4[] corners = sector.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration());
		Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe(), dc.getVerticalExaggeration());

		View view = dc.getView();
		double d1 = view.getEyePoint().distanceTo3(corners[0]);
		double d2 = view.getEyePoint().distanceTo3(corners[1]);
		double d3 = view.getEyePoint().distanceTo3(corners[2]);
		double d4 = view.getEyePoint().distanceTo3(corners[3]);
		double d5 = view.getEyePoint().distanceTo3(centerPoint);

		double minDistance = d1;
		if (d2 < minDistance)
			minDistance = d2;
		if (d3 < minDistance)
			minDistance = d3;
		if (d4 < minDistance)
			minDistance = d4;
		if (d5 < minDistance)
			minDistance = d5;

		double cellSize = (Math.PI * sector.getDeltaLatRadians() * dc.getGlobe().getRadius()) / 20; // TODO

		return !(Math.log10(cellSize) <= (Math.log10(minDistance) - this.getSplitScale()));
	}

	private boolean atMaxLevel(DrawContext dc)
	{
		Position vpc = dc.getViewportCenterPosition();
		if (dc.getView() == null || this.getLevels() == null || vpc == null)
			return false;

		if (!this.getLevels().getSector().contains(vpc.getLatitude(), vpc.getLongitude()))
			return true;

		Level nextToLast = this.getLevels().getNextToLastLevel();
		if (nextToLast == null)
			return true;

		Sector centerSector =
				nextToLast.computeSectorForPosition(vpc.getLatitude(), vpc.getLongitude(),
						this.levels.getTileOrigin());
		return this.needToSplit(dc, centerSector);
	}

	// ============== Rendering ======================= //
	// ============== Rendering ======================= //
	// ============== Rendering ======================= //

	@Override
	public void render(DrawContext dc)
	{
		this.atMaxResolution = this.atMaxLevel(dc);
		super.render(dc);
	}

	@Override
	protected final void doRender(DrawContext dc)
	{
		if (this.forceLevelZeroLoads && !this.levelZeroLoaded)
			this.loadAllTopLevelTiles(dc);
		if (dc.getSurfaceGeometry() == null || dc.getSurfaceGeometry().size() < 1)
			return;

		renderer.setShowImageTileOutlines(this.showImageTileOutlines);

		draw(dc);
	}

	private void draw(DrawContext dc)
	{
		this.assembleTiles(dc); // Determine the tiles to draw.

		if (this.currentTiles.size() >= 1)
		{
			if (this.getScreenCredit() != null)
			{
				dc.addScreenCredit(this.getScreenCredit());
			}

			ShapefileTile[] sortedTiles = new ShapefileTile[this.currentTiles.size()];
			sortedTiles = this.currentTiles.toArray(sortedTiles);
			Arrays.sort(sortedTiles, levelComparer);

			GL gl = dc.getGL();

			if (this.getOpacity() < 1)
			{
				gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT | GL.GL_CURRENT_BIT);
				this.setBlendingFunction(dc);
			}
			else
			{
				gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT);
			}

			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glCullFace(GL.GL_BACK);

			dc.setPerFrameStatistic(PerformanceStatistic.IMAGE_TILE_COUNT, this.tileCountName,
					this.currentTiles.size());
			renderer.renderTiles(dc, this.currentTiles);

			gl.glPopAttrib();

			if (this.drawTileIDs)
				this.drawTileIDs(dc, this.currentTiles);

			if (this.drawBoundingVolumes)
				this.drawBoundingVolumes(dc, this.currentTiles);

			// Check texture expiration. Memory-cached textures are checked for expiration only when an explicit,
			// non-zero expiry time has been set for the layer. If none has been set, the expiry times of the layer's
			// individual levels are used, but only for images in the local file cache, not textures in memory. This is
			// to avoid incurring the overhead of checking expiration of in-memory textures, a very rarely used feature.
			if (this.getExpiryTime() > 0 && this.getExpiryTime() < System.currentTimeMillis())
				this.checkTileExpiration(dc, this.currentTiles);

			this.currentTiles.clear();
		}

		this.sendRequests();
		this.requestQ.clear();
	}

	private void checkTileExpiration(DrawContext dc, List<ShapefileTile> tiles)
	{
		for (ShapefileTile tile : tiles)
		{
			if (tile.isTileExpired())
				this.requestTile(dc, tile);
		}
	}

	protected void setBlendingFunction(DrawContext dc)
	{
		// Set up a premultiplied-alpha blending function. Any texture read by JOGL will have alpha-premultiplied color
		// components, as will any DDS file created by World Wind or the World Wind WMS. We'll also set up the base
		// color as a premultiplied color, so that any incoming premultiplied color will be properly combined with the
		// base color.

		GL gl = dc.getGL();

		double alpha = this.getOpacity();
		gl.glColor4d(alpha, alpha, alpha, alpha);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void sendRequests()
	{
		Runnable task = this.requestQ.poll();
		while (task != null)
		{
			if (!WorldWind.getTaskService().isFull())
			{
				WorldWind.getTaskService().addTask(task);
			}
			task = this.requestQ.poll();
		}
	}

	@Override
	public boolean isLayerInView(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (dc.getView() == null)
		{
			String message =
					Logging.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		return !(dc.getVisibleSector() != null && !this.levels.getSector().intersects(
				dc.getVisibleSector()));
	}

	private Vec4 computeReferencePoint(DrawContext dc)
	{
		if (dc.getViewportCenterPosition() != null)
			return dc.getGlobe().computePointFromPosition(dc.getViewportCenterPosition());

		java.awt.geom.Rectangle2D viewport = dc.getView().getViewport();
		int x = (int) viewport.getWidth() / 2;
		for (int y = (int) (0.5 * viewport.getHeight()); y >= 0; y--)
		{
			Position pos = dc.getView().computePositionFromScreenPoint(x, y);
			if (pos == null)
				continue;

			return dc.getGlobe()
					.computePointFromPosition(pos.getLatitude(), pos.getLongitude(), 0d);
		}

		return null;
	}

	protected Vec4 getReferencePoint(DrawContext dc)
	{
		return this.computeReferencePoint(dc);
	}

	private static class LevelComparer implements Comparator<ShapefileTile>
	{
		public int compare(ShapefileTile ta, ShapefileTile tb)
		{
			int la =
					ta.getFallbackTile() == null ? ta.getLevelNumber() : ta.getFallbackTile()
							.getLevelNumber();
			int lb =
					tb.getFallbackTile() == null ? tb.getLevelNumber() : tb.getFallbackTile()
							.getLevelNumber();

			return la < lb ? -1 : la == lb ? 0 : 1;
		}
	}

	private void drawTileIDs(DrawContext dc, ArrayList<ShapefileTile> tiles)
	{
		java.awt.Rectangle viewport = dc.getView().getViewport();
		TextRenderer textRenderer =
				OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), java.awt.Font
						.decode("Arial-Plain-13"));

		dc.getGL().glDisable(GL.GL_DEPTH_TEST);
		dc.getGL().glDisable(GL.GL_BLEND);
		dc.getGL().glDisable(GL.GL_TEXTURE_2D);

		textRenderer.beginRendering(viewport.width, viewport.height);
		textRenderer.setColor(java.awt.Color.YELLOW);
		for (ShapefileTile tile : tiles)
		{
			String tileLabel = tile.getLabel();

			if (tile.getFallbackTile() != null)
				tileLabel += "/" + tile.getFallbackTile().getLabel();

			LatLon ll = tile.getSector().getCentroid();
			Vec4 pt =
					dc.getGlobe().computePointFromPosition(ll.getLatitude(), ll.getLongitude(),
							dc.getGlobe().getElevation(ll.getLatitude(), ll.getLongitude()));
			pt = dc.getView().project(pt);
			textRenderer.draw(tileLabel, (int) pt.x, (int) pt.y);
		}
		textRenderer.setColor(java.awt.Color.WHITE);
		textRenderer.endRendering();
	}

	private void drawBoundingVolumes(DrawContext dc, ArrayList<ShapefileTile> tiles)
	{
		float[] previousColor = new float[4];
		dc.getGL().glGetFloatv(GL.GL_CURRENT_COLOR, previousColor, 0);
		dc.getGL().glColor3d(0, 1, 0);

		for (ShapefileTile tile : tiles)
		{
			((Cylinder) tile.getExtent(dc)).render(dc);
		}

		Cylinder c =
				dc.getGlobe().computeBoundingCylinder(dc.getVerticalExaggeration(),
						this.levels.getSector());
		dc.getGL().glColor3d(1, 1, 0);
		c.render(dc);

		dc.getGL().glColor4fv(previousColor, 0);
	}

	/*public int computeLevelForResolution(Sector sector, double resolution)
	{
		if (sector == null)
		{
			String message = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		// Find the first level exceeding the desired resolution
		double texelSize;
		Level targetLevel = this.levels.getLastLevel();
		for (int i = 0; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
		{
			if (this.levels.isLevelEmpty(i))
				continue;

			texelSize = this.levels.getLevel(i).getTexelSize();
			if (texelSize > resolution)
				continue;

			targetLevel = this.levels.getLevel(i);
			break;
		}

		// Choose the level closest to the resolution desired
		if (targetLevel.getLevelNumber() != 0
				&& !this.levels.isLevelEmpty(targetLevel.getLevelNumber() - 1))
		{
			Level nextLowerLevel = this.levels.getLevel(targetLevel.getLevelNumber() - 1);
			double dless = Math.abs(nextLowerLevel.getTexelSize() - resolution);
			double dmore = Math.abs(targetLevel.getTexelSize() - resolution);
			if (dless < dmore)
				targetLevel = nextLowerLevel;
		}

		Logging.logger().fine(
				Logging.getMessage("layers.TiledImageLayer.LevelSelection", targetLevel
						.getLevelNumber(), Double.toString(targetLevel.getTexelSize())));
		return targetLevel.getLevelNumber();
	}

	public long countImagesInSector(Sector sector)
	{
		long count = 0;
		for (int i = 0; i <= this.getLevels().getLastLevel().getLevelNumber(); i++)
		{
			if (!this.levels.isLevelEmpty(i))
				count += countImagesInSector(sector, i);
		}
		return count;
	}

	public long countImagesInSector(Sector sector, int levelNumber)
	{
		if (sector == null)
		{
			String msg = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Level targetLevel = this.levels.getLastLevel();
		if (levelNumber >= 0)
		{
			for (int i = levelNumber; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
			{
				if (this.levels.isLevelEmpty(i))
					continue;

				targetLevel = this.levels.getLevel(i);
				break;
			}
		}

		// Collect all the tiles intersecting the input sector.
		LatLon delta = targetLevel.getTileDelta();
		LatLon origin = this.levels.getTileOrigin();
		final int nwRow =
				Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude(), origin.getLatitude());
		final int nwCol =
				Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin
						.getLongitude());
		final int seRow =
				Tile.computeRow(delta.getLatitude(), sector.getMinLatitude(), origin.getLatitude());
		final int seCol =
				Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin
						.getLongitude());

		long numRows = nwRow - seRow + 1;
		long numCols = seCol - nwCol + 1;

		return numRows * numCols;
	}

	public ShapefileTile[][] getTilesInSector(Sector sector, int levelNumber)
	{
		if (sector == null)
		{
			String msg = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Level targetLevel = this.levels.getLastLevel();
		if (levelNumber >= 0)
		{
			for (int i = levelNumber; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
			{
				if (this.levels.isLevelEmpty(i))
					continue;

				targetLevel = this.levels.getLevel(i);
				break;
			}
		}

		// Collect all the tiles intersecting the input sector.
		LatLon delta = targetLevel.getTileDelta();
		LatLon origin = this.levels.getTileOrigin();
		final int nwRow =
				Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude(), origin.getLatitude());
		final int nwCol =
				Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin
						.getLongitude());
		final int seRow =
				Tile.computeRow(delta.getLatitude(), sector.getMinLatitude(), origin.getLatitude());
		final int seCol =
				Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin
						.getLongitude());

		int numRows = nwRow - seRow + 1;
		int numCols = seCol - nwCol + 1;
		ShapefileTile[][] sectorTiles = new ShapefileTile[numRows][numCols];

		for (int row = nwRow; row >= seRow; row--)
		{
			for (int col = nwCol; col <= seCol; col++)
			{
				TileKey key =
						new TileKey(targetLevel.getLevelNumber(), row, col, targetLevel
								.getCacheName());
				Sector tileSector = this.levels.computeSectorForKey(key);
				sectorTiles[nwRow - row][col - nwCol] =
						new ShapefileTile(tileSector, targetLevel, row, col);
			}
		}

		return sectorTiles;
	}*/
}
