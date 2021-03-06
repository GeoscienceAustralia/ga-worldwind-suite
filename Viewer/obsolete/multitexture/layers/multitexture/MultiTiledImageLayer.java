/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.layers.multitexture;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWIO;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import com.sun.opengl.util.j2d.TextRenderer;

/**
 * @author tag
 * @version $Id: TiledImageLayer.java 5102 2008-04-21 05:41:05Z tgaskins $
 */
public abstract class MultiTiledImageLayer extends AbstractLayer
{
	// Infrastructure
	private static final LevelComparer levelComparer = new LevelComparer();
	private final LevelSet levels;
	private ArrayList<MultiTextureTile> topLevels;
	private boolean forceLevelZeroLoads = false;
	private boolean levelZeroLoaded = false;
	private boolean retainLevelZeroTiles = false;
	private String tileCountName;
	private double splitScale = 0.9; // TODO: Make configurable
	private boolean useMipMaps = false;
	private ArrayList<String> supportedImageFormats = new ArrayList<String>();

	// Diagnostic flags
	private boolean showImageTileOutlines = false;
	private boolean drawTileBoundaries = false;
	private boolean useTransparentTextures = false;
	private boolean drawTileIDs = false;
	private boolean drawBoundingVolumes = false;
	private TextRenderer textRenderer = null;

	// Stuff computed each frame
	private ArrayList<MultiTextureTile> currentTiles = new ArrayList<MultiTextureTile>();
	private MultiTextureTile currentResourceTile;
	private Vec4 referencePoint;
	private boolean atMaxResolution = false;
	private PriorityBlockingQueue<Runnable> requestQ = new PriorityBlockingQueue<Runnable>(
			200);

	abstract protected void requestTexture(DrawContext dc, MultiTextureTile tile);

	abstract protected void forceTextureLoad(MultiTextureTile tile);

	public MultiTiledImageLayer(LevelSet levelSet)
	{
		if (levelSet == null)
		{
			String message = Logging.getMessage("nullValue.LevelSetIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.levels = new LevelSet(levelSet); // the caller's levelSet may change internally, so we copy it.

		this.createTopLevelTiles();

		this.setPickEnabled(false); // textures are assumed to be terrain unless specifically indicated otherwise.
		this.tileCountName = this.getName() + " Tiles";
	}

	@Override
	public void setName(String name)
	{
		super.setName(name);
		this.tileCountName = this.getName() + " Tiles";
	}

	public boolean isUseTransparentTextures()
	{
		return this.useTransparentTextures;
	}

	public void setUseTransparentTextures(boolean useTransparentTextures)
	{
		this.useTransparentTextures = useTransparentTextures;
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

	protected LevelSet getLevels()
	{
		return levels;
	}

	protected void setSplitScale(double splitScale)
	{
		this.splitScale = splitScale;
	}

	protected PriorityBlockingQueue<Runnable> getRequestQ()
	{
		return requestQ;
	}

	public boolean isMultiResolution()
	{
		return this.getLevels() != null && this.getLevels().getNumLevels() > 1;
	}

	public boolean isAtMaxResolution()
	{
		return this.atMaxResolution;
	}

	public boolean isUseMipMaps()
	{
		return useMipMaps;
	}

	public void setUseMipMaps(boolean useMipMaps)
	{
		this.useMipMaps = useMipMaps;
	}

	private void createTopLevelTiles()
	{
		Sector sector = this.levels.getSector();

		Angle dLat = this.levels.getLevelZeroTileDelta().getLatitude();
		Angle dLon = this.levels.getLevelZeroTileDelta().getLongitude();

		// Determine the row and column offset from the common World Wind global tiling origin.
		Level level = levels.getFirstLevel();
		int firstRow = Tile.computeRow(level.getTileDelta().getLatitude(),
				sector.getMinLatitude());
		int firstCol = Tile.computeColumn(level.getTileDelta().getLongitude(),
				sector.getMinLongitude());
		int lastRow = Tile.computeRow(level.getTileDelta().getLatitude(),
				sector.getMaxLatitude());
		int lastCol = Tile.computeColumn(level.getTileDelta().getLongitude(),
				sector.getMaxLongitude());

		int nLatTiles = lastRow - firstRow + 1;
		int nLonTiles = lastCol - firstCol + 1;

		this.topLevels = new ArrayList<MultiTextureTile>(nLatTiles * nLonTiles);

		Angle p1 = Tile.computeRowLatitude(firstRow, dLat);
		for (int row = firstRow; row <= lastRow; row++)
		{
			Angle p2;
			p2 = p1.add(dLat);

			Angle t1 = Tile.computeColumnLongitude(firstCol, dLon);
			for (int col = firstCol; col <= lastCol; col++)
			{
				Angle t2;
				t2 = t1.add(dLon);

				this.topLevels.add(new MultiTextureTile(new Sector(p1, p2, t1,
						t2), level, row, col));
				t1 = t2;
			}
			p1 = p2;
		}
	}

	private void loadAllTopLevelTextures(DrawContext dc)
	{
		for (MultiTextureTile tile : this.topLevels)
		{
			if (!tile.areTexturesInMemory(dc.getTextureCache()))
				this.forceTextureLoad(tile);
		}

		this.levelZeroLoaded = true;
	}

	// ============== Tile Assembly ======================= //
	// ============== Tile Assembly ======================= //
	// ============== Tile Assembly ======================= //

	private void assembleTiles(DrawContext dc)
	{
		this.currentTiles.clear();

		for (MultiTextureTile tile : this.topLevels)
		{
			if (this.isTileVisible(dc, tile))
			{
				this.currentResourceTile = null;
				this.addTileOrDescendants(dc, tile);
			}
		}
	}

	private void addTileOrDescendants(DrawContext dc, MultiTextureTile tile)
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

		MultiTextureTile ancestorResource = null;

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
			if (tile.areTexturesInMemory(dc.getTextureCache())
					|| tile.getLevelNumber() == 0)
			{
				ancestorResource = this.currentResourceTile;
				this.currentResourceTile = tile;
			}
			else if (!tile.getLevel().isEmpty())
			{
				//                this.addTile(dc, tile);
				//                return;

				// Issue a request for the parent before descending to the children.
				if (tile.getLevelNumber() < this.levels.getNumLevels())
				{
					// Request only tiles with data associated at this level
					if (!this.levels.isResourceAbsent(tile))
						this.requestTexture(dc, tile);
				}
			}

			MultiTextureTile[] subTiles = tile.createSubTiles(this.levels
					.getLevel(tile.getLevelNumber() + 1));
			for (MultiTextureTile child : subTiles)
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

	private void addTile(DrawContext dc, MultiTextureTile tile)
	{
		tile.setFallbackTile(null);

		if (tile.areTexturesInMemory(dc.getTextureCache()))
		{
			//            System.out.printf("Sector %s, min = %f, max = %f\n", tile.getSector(),
			//                dc.getGlobe().getMinElevation(tile.getSector()), dc.getGlobe().getMaxElevation(tile.getSector()));
			this.addTileToCurrent(tile);
			return;
		}

		// Level 0 loads may be forced
		if (tile.getLevelNumber() == 0 && this.forceLevelZeroLoads
				&& !tile.areTexturesInMemory(dc.getTextureCache()))
		{
			this.forceTextureLoad(tile);
			if (tile.areTexturesInMemory(dc.getTextureCache()))
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
				this.requestTexture(dc, tile);
		}

		// Set up to use the currentResource tile's texture
		if (this.currentResourceTile != null)
		{
			if (this.currentResourceTile.getLevelNumber() == 0
					&& this.forceLevelZeroLoads
					&& !this.currentResourceTile.areTexturesInMemory(dc
							.getTextureCache())
					&& !this.currentResourceTile.areTexturesInMemory(dc
							.getTextureCache()))
				this.forceTextureLoad(this.currentResourceTile);

			if (this.currentResourceTile.areTexturesInMemory(dc
					.getTextureCache()))
			{
				tile.setFallbackTile(currentResourceTile);
				this.addTileToCurrent(tile);
			}
		}
	}

	private void addTileToCurrent(MultiTextureTile tile)
	{
		this.currentTiles.add(tile);
	}

	private boolean isTileVisible(DrawContext dc, MultiTextureTile tile)
	{
		//        if (!(tile.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates())
		//            && (dc.getVisibleSector() == null || dc.getVisibleSector().intersects(tile.getSector()))))
		//            return false;
		//
		//        Position eyePos = dc.getView().getEyePosition();
		//        LatLon centroid = tile.getSector().getCentroid();
		//        Angle d = LatLon.greatCircleDistance(eyePos.getLatLon(), centroid);
		//        if ((!tile.getLevelName().equals("0")) && d.compareTo(tile.getSector().getDeltaLat().multiply(2.5)) == 1)
		//            return false;
		//
		//        return true;
		//
		return tile.getExtent(dc).intersects(
				dc.getView().getFrustumInModelCoordinates())
				&& (dc.getVisibleSector() == null || dc.getVisibleSector()
						.intersects(tile.getSector()));
	}

	//
	//    private boolean meetsRenderCriteria2(DrawContext dc, MultiTextureTile tile)
	//    {
	//        if (this.levels.isFinalLevel(tile.getLevelNumber()))
	//            return true;
	//
	//        Sector sector = tile.getSector();
	//        Vec4[] corners = sector.computeCornerPoints(dc.getGlobe());
	//        Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe());
	//
	//        View view = dc.getView();
	//        double d1 = view.getEyePoint().distanceTo3(corners[0]);
	//        double d2 = view.getEyePoint().distanceTo3(corners[1]);
	//        double d3 = view.getEyePoint().distanceTo3(corners[2]);
	//        double d4 = view.getEyePoint().distanceTo3(corners[3]);
	//        double d5 = view.getEyePoint().distanceTo3(centerPoint);
	//
	//        double minDistance = d1;
	//        if (d2 < minDistance)
	//            minDistance = d2;
	//        if (d3 < minDistance)
	//            minDistance = d3;
	//        if (d4 < minDistance)
	//            minDistance = d4;
	//        if (d5 < minDistance)
	//            minDistance = d5;
	//
	//        double r = 0;
	//        if (minDistance == d1)
	//            r = corners[0].getLength3();
	//        if (minDistance == d2)
	//            r = corners[1].getLength3();
	//        if (minDistance == d3)
	//            r = corners[2].getLength3();
	//        if (minDistance == d4)
	//            r = corners[3].getLength3();
	//        if (minDistance == d5)
	//            r = centerPoint.getLength3();
	//
	//        double texelSize = tile.getLevel().getTexelSize(r);
	//        double pixelSize = dc.getView().computePixelSizeAtDistance(minDistance);
	//
	//        return 2 * pixelSize >= texelSize;
	//    }

	private boolean meetsRenderCriteria(DrawContext dc, MultiTextureTile tile)
	{
		return this.levels.isFinalLevel(tile.getLevelNumber())
				|| !needToSplit(dc, tile.getSector());
	}

	private boolean needToSplit(DrawContext dc, Sector sector)
	{
		Vec4[] corners = sector.computeCornerPoints(dc.getGlobe());
		Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe());

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

		double cellSize = (Math.PI * sector.getDeltaLatRadians() * dc
				.getGlobe().getRadius()) / 20; // TODO

		return !(Math.log10(cellSize) <= (Math.log10(minDistance) - this.splitScale));
	}

	private boolean atMaxLevel(DrawContext dc)
	{
		Position vpc = dc.getViewportCenterPosition();
		if (dc.getView() == null || this.getLevels() == null || vpc == null)
			return false;

		if (!this.getLevels().getSector().contains(vpc.getLatitude(),
				vpc.getLongitude()))
			return true;

		Level nextToLast = this.getLevels().getNextToLastLevel();
		if (nextToLast == null)
			return true;

		Sector centerSector = nextToLast.computeSectorForPosition(vpc
				.getLatitude(), vpc.getLongitude());
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
			this.loadAllTopLevelTextures(dc);
		if (dc.getSurfaceGeometry() == null
				|| dc.getSurfaceGeometry().size() < 1)
			return;

		dc.getGeographicSurfaceTileRenderer().setShowImageTileOutlines(
				this.showImageTileOutlines);

		draw(dc);
	}

	private void draw(DrawContext dc)
	{
		this.referencePoint = this.computeReferencePoint(dc);

		this.assembleTiles(dc); // Determine the tiles to draw.

		if (this.currentTiles.size() >= 1)
		{
			MultiTextureTile[] sortedTiles = new MultiTextureTile[this.currentTiles
					.size()];
			sortedTiles = this.currentTiles.toArray(sortedTiles);
			Arrays.sort(sortedTiles, levelComparer);

			GL gl = dc.getGL().getGL2();

			if (this.isUseTransparentTextures() || this.getOpacity() < 1)
			{
				gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT
						| GL.GL_CURRENT_BIT);
				gl.glColor4d(1d, 1d, 1d, this.getOpacity());
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			}
			else
			{
				gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT);
			}

			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glCullFace(GL.GL_BACK);

			dc.setPerFrameStatistic(PerformanceStatistic.IMAGE_TILE_COUNT,
					this.tileCountName, this.currentTiles.size());
			dc.getGeographicSurfaceTileRenderer().renderTiles(dc,
					this.currentTiles);

			gl.glPopAttrib();

			if (this.drawTileIDs)
				this.drawTileIDs(dc, this.currentTiles);

			if (this.drawBoundingVolumes)
				this.drawBoundingVolumes(dc, this.currentTiles);

			this.currentTiles.clear();
		}

		this.sendRequests();
		this.requestQ.clear();
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
			String message = Logging
					.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		return !(dc.getVisibleSector() != null && !this.levels.getSector()
				.intersects(dc.getVisibleSector()));
	}

	private Vec4 computeReferencePoint(DrawContext dc)
	{
		if (dc.getViewportCenterPosition() != null)
			return dc.getGlobe().computePointFromPosition(
					dc.getViewportCenterPosition());

		java.awt.geom.Rectangle2D viewport = dc.getView().getViewport();
		int x = (int) viewport.getWidth() / 2;
		for (int y = (int) (0.5 * viewport.getHeight()); y >= 0; y--)
		{
			Position pos = dc.getView().computePositionFromScreenPoint(x, y);
			if (pos == null)
				continue;

			return dc.getGlobe().computePointFromPosition(pos.getLatitude(),
					pos.getLongitude(), 0d);
		}

		return null;
	}

	protected Vec4 getReferencePoint()
	{
		return this.referencePoint;
	}

	private static class LevelComparer implements Comparator<MultiTextureTile>
	{
		public int compare(MultiTextureTile ta, MultiTextureTile tb)
		{
			int la = ta.getFallbackTile() == null ? ta.getLevelNumber() : ta
					.getFallbackTile().getLevelNumber();
			int lb = tb.getFallbackTile() == null ? tb.getLevelNumber() : tb
					.getFallbackTile().getLevelNumber();

			return la < lb ? -1 : la == lb ? 0 : 1;
		}
	}

	private void drawTileIDs(DrawContext dc, ArrayList<MultiTextureTile> tiles)
	{
		java.awt.Rectangle viewport = dc.getView().getViewport();
		if (this.textRenderer == null)
		{
			this.textRenderer = new TextRenderer(java.awt.Font
					.decode("Arial-Plain-13"), true, true);
			this.textRenderer.setUseVertexArrays(false);
		}

		dc.getGL().getGL2().glDisable(GL.GL_DEPTH_TEST);
		dc.getGL().getGL2().glDisable(GL.GL_BLEND);
		dc.getGL().getGL2().glDisable(GL.GL_TEXTURE_2D);

		this.textRenderer.setColor(java.awt.Color.YELLOW);
		this.textRenderer.beginRendering(viewport.width, viewport.height);
		for (MultiTextureTile tile : tiles)
		{
			String tileLabel = tile.getLabel();

			if (tile.getFallbackTile() != null)
				tileLabel += "/" + tile.getFallbackTile().getLabel();

			LatLon ll = tile.getSector().getCentroid();
			Vec4 pt = dc.getGlobe().computePointFromPosition(
					ll.getLatitude(),
					ll.getLongitude(),
					dc.getGlobe().getElevation(ll.getLatitude(),
							ll.getLongitude()));
			pt = dc.getView().project(pt);
			this.textRenderer.draw(tileLabel, (int) pt.x, (int) pt.y);
		}
		this.textRenderer.endRendering();
	}

	private void drawBoundingVolumes(DrawContext dc,
			ArrayList<MultiTextureTile> tiles)
	{
		float[] previousColor = new float[4];
		dc.getGL().getGL2().glGetFloatv(GL.GL_CURRENT_COLOR, previousColor, 0);
		dc.getGL().getGL2().glColor3d(0, 1, 0);

		for (MultiTextureTile tile : tiles)
		{
			((Cylinder) tile.getExtent(dc)).render(dc);
		}

		Cylinder c = dc.getGlobe().computeBoundingCylinder(
				dc.getVerticalExaggeration(), this.levels.getSector());
		dc.getGL().getGL2().glColor3d(1, 1, 0);
		c.render(dc);

		dc.getGL().getGL2().glColor4fv(previousColor, 0);
	}

	// ============== Image Composition ======================= //
	// ============== Image Composition ======================= //
	// ============== Image Composition ======================= //

	public ArrayList<String> getAvailableImageFormats()
	{
		return new ArrayList<String>(this.supportedImageFormats);
	}

	public boolean isImageFormatAvailable(String imageFormat)
	{
		return imageFormat != null
				&& this.supportedImageFormats.contains(imageFormat);
	}

	public String getDefaultImageFormat()
	{
		return this.supportedImageFormats.size() > 0 ? this.supportedImageFormats
				.get(0)
				: null;
	}

	protected void setAvailableImageFormats(String[] formats)
	{
		this.supportedImageFormats.clear();

		if (formats != null)
		{
			for (String format : formats)
				this.supportedImageFormats.add(format);
		}
	}

	private BufferedImage requestImage(MultiTextureTile tile, String mimeType)
			throws URISyntaxException
	{
		String pathBase = tile.getPath().substring(0,
				tile.getPath().lastIndexOf("."));
		String suffix = WWIO.makeSuffixForMimeType(mimeType);
		String path = pathBase + suffix;
		URL url = WorldWind.getDataFileCache().findFile(path, false);

		if (url == null) // image is not local
			return null;

		if (WWIO.isFileOutOfDate(url, tile.getLevel().getExpiryTime()))
		{
			// The file has expired. Delete it.
			WorldWind.getDataFileCache().removeFile(url);
			String message = Logging.getMessage("generic.DataFileExpired", url);
			Logging.logger().fine(message);
		}
		else
		{
			try
			{
				File imageFile = new File(url.toURI());
				BufferedImage image = ImageIO.read(imageFile);
				if (image == null)
				{
					String message = Logging.getMessage(
							"generic.ImageReadFailed", imageFile);
					throw new RuntimeException(message);
				}

				this.levels.unmarkResourceAbsent(tile);
				return image;
			}
			catch (IOException e)
			{
				// Assume that something's wrong with the file and delete it.
				gov.nasa.worldwind.WorldWind.getDataFileCache().removeFile(url);
				this.levels.markResourceAbsent(tile);
				String message = Logging.getMessage(
						"generic.DeletedCorruptDataFile", url);
				Logging.logger().info(message);
			}
		}

		return null;
	}

	private void downloadImage(final MultiTextureTile tile, String mimeType)
			throws Exception
	{
		//        System.out.println(tile.getPath());
		final URL resourceURL = tile.getResourceURL(mimeType);
		Retriever retriever;

		String protocol = resourceURL.getProtocol();

		if ("http".equalsIgnoreCase(protocol))
		{
			retriever = new HTTPRetriever(resourceURL,
					new HttpRetrievalPostProcessor(tile));
		}
		else
		{
			String message = Logging
					.getMessage("layers.TextureLayer.UnknownRetrievalProtocol",
							resourceURL);
			throw new RuntimeException(message);
		}

		retriever.setConnectTimeout(10000);
		retriever.setReadTimeout(20000);
		retriever.call();
	}

	public int computeLevelForResolution(Sector sector, Globe globe,
			double resolution)
	{
		if (sector == null)
		{
			String message = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (globe == null)
		{
			String message = Logging.getMessage("nullValue.GlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		double texelSize = 0;
		Level targetLevel = this.levels.getLastLevel();
		for (int i = 0; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
		{
			if (this.levels.isLevelEmpty(i))
				continue;

			texelSize = this.levels.getLevel(i).getTexelSize(globe.getRadius());
			if (texelSize > resolution)
				continue;

			targetLevel = this.levels.getLevel(i);
			break;
		}

		Logging.logger().info(
				Logging.getMessage("layers.TiledImageLayer.LevelSelection",
						targetLevel.getLevelNumber(), texelSize));
		return targetLevel.getLevelNumber();
	}

	public BufferedImage composeImageForSector(Sector sector, int imageWidth,
			int imageHeight, int levelNumber, String mimeType,
			boolean abortOnError, BufferedImage image)
	{
		if (sector == null)
		{
			String message = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (levelNumber < 0)
		{
			levelNumber = this.levels.getLastLevel().getLevelNumber();
		}
		else if (levelNumber > this.levels.getLastLevel().getLevelNumber())
		{
			Logging.logger().warning(
					Logging.getMessage(
							"generic.LevelRequestedGreaterThanMaxLevel",
							levelNumber, this.levels.getLastLevel()
									.getLevelNumber()));
			levelNumber = this.levels.getLastLevel().getLevelNumber();
		}

		MultiTextureTile[][] tiles = this.getTilesInSector(sector, levelNumber);

		if (tiles.length == 0 || tiles[0].length == 0)
		{
			Logging
					.logger()
					.severe(
							Logging
									.getMessage("layers.TiledImageLayer.NoImagesAvailable"));
			return null;
		}

		if (image == null)
			image = new BufferedImage(imageWidth, imageHeight,
					BufferedImage.TYPE_INT_RGB);

		Graphics2D g = image.createGraphics();

		for (MultiTextureTile[] row : tiles)
		{
			for (MultiTextureTile tile : row)
			{
				if (tile == null)
					continue;

				BufferedImage tileImage;
				try
				{
					tileImage = this.getImage(tile, mimeType);

					double sh = ((double) imageHeight / (double) tileImage
							.getHeight())
							* (tile.getSector().getDeltaLat().divide(sector
									.getDeltaLat()));
					double sw = ((double) imageWidth / (double) tileImage
							.getWidth())
							* (tile.getSector().getDeltaLon().divide(sector
									.getDeltaLon()));

					double dh = imageHeight
							* (-tile.getSector().getMaxLatitude().subtract(
									sector.getMaxLatitude()).degrees / sector
									.getDeltaLat().degrees);
					double dw = imageWidth
							* (tile.getSector().getMinLongitude().subtract(
									sector.getMinLongitude()).degrees / sector
									.getDeltaLon().degrees);

					AffineTransform txf = g.getTransform();
					g.translate(dw, dh);
					g.scale(sw, sh);
					g.drawImage(tileImage, 0, 0, null);
					g.setTransform(txf);
				}
				catch (Exception e)
				{
					if (abortOnError)
						throw new RuntimeException(e);

					String message = Logging.getMessage(
							"generic.ExceptionWhileRequestingImage", tile
									.getPath());
					Logging.logger().log(java.util.logging.Level.WARNING,
							message, e);
				}
			}
		}

		return image;
	}

	public int countImagesInSector(Sector sector, int levelNumber)
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
			for (int i = levelNumber; i < this.getLevels().getLastLevel()
					.getLevelNumber(); i++)
			{
				if (this.levels.isLevelEmpty(i))
					continue;

				targetLevel = this.levels.getLevel(i);
				break;
			}
		}

		// Collect all the tiles intersecting the input sector.
		LatLon delta = targetLevel.getTileDelta();
		final int nwRow = Tile.computeRow(delta.getLatitude(), sector
				.getMaxLatitude());
		final int nwCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMinLongitude());
		final int seRow = Tile.computeRow(delta.getLatitude(), sector
				.getMinLatitude());
		final int seCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMaxLongitude());

		int numRows = nwRow - seRow + 1;
		int numCols = seCol - nwCol + 1;

		return numRows * numCols;
	}

	private MultiTextureTile[][] getTilesInSector(Sector sector, int levelNumber)
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
			for (int i = levelNumber; i < this.getLevels().getLastLevel()
					.getLevelNumber(); i++)
			{
				if (this.levels.isLevelEmpty(i))
					continue;

				targetLevel = this.levels.getLevel(i);
				break;
			}
		}

		// Collect all the tiles intersecting the input sector.
		LatLon delta = targetLevel.getTileDelta();
		final int nwRow = Tile.computeRow(delta.getLatitude(), sector
				.getMaxLatitude());
		final int nwCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMinLongitude());
		final int seRow = Tile.computeRow(delta.getLatitude(), sector
				.getMinLatitude());
		final int seCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMaxLongitude());

		int numRows = nwRow - seRow + 1;
		int numCols = seCol - nwCol + 1;
		MultiTextureTile[][] sectorTiles = new MultiTextureTile[numRows][numCols];

		for (int row = nwRow; row >= seRow; row--)
		{
			for (int col = nwCol; col <= seCol; col++)
			{
				TileKey key = new TileKey(targetLevel.getLevelNumber(), row,
						col, targetLevel.getCacheName());
				Sector tileSector = this.levels.computeSectorForKey(key);
				sectorTiles[nwRow - row][col - nwCol] = new MultiTextureTile(
						tileSector, targetLevel, row, col);
			}
		}

		return sectorTiles;
	}

	private BufferedImage getImage(MultiTextureTile tile, String mimeType)
			throws Exception
	{
		// Read the image from disk.
		BufferedImage image = this.requestImage(tile, mimeType);
		if (image != null)
			return image;

		// Retrieve it from the net since it's not on disk.
		this.downloadImage(tile, mimeType);

		// Try to read from disk again after retrieving it from the net.
		image = this.requestImage(tile, mimeType);
		if (image == null)
		{
			String message = Logging.getMessage(
					"layers.TiledImageLayer.ImageUnavailable", tile.getPath());
			throw new RuntimeException(message);
		}

		return image;
	}

	private class HttpRetrievalPostProcessor implements RetrievalPostProcessor
	{
		private MultiTextureTile tile;

		public HttpRetrievalPostProcessor(MultiTextureTile tile)
		{
			this.tile = tile;
		}

		public ByteBuffer run(Retriever retriever)
		{
			if (!retriever.getState().equals(
					Retriever.RETRIEVER_STATE_SUCCESSFUL))
				return null;

			HTTPRetriever htr = (HTTPRetriever) retriever;
			if (htr.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT)
			{
				// Mark tile as missing to avoid excessive attempts
				MultiTiledImageLayer.this.levels.markResourceAbsent(tile);
				return null;
			}

			if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
				return null;

			URLRetriever r = (URLRetriever) retriever;
			ByteBuffer buffer = r.getBuffer();

			String suffix = WWIO.makeSuffixForMimeType(htr.getContentType());
			if (suffix == null)
			{
				return null; // TODO: log error
			}

			String path = tile.getPath().substring(0,
					tile.getPath().lastIndexOf("."));
			path += suffix;

			final File outFile = WorldWind.getDataFileCache().newFile(path);
			if (outFile == null)
				return null;

			try
			{
				WWIO.saveBuffer(buffer, outFile);
				return buffer;
			}
			catch (IOException e)
			{
				e.printStackTrace(); // TODO: log error
				return null;
			}
		}
	}
}