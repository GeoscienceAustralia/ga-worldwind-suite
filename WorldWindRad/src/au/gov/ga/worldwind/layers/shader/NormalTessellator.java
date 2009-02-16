/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package au.gov.ga.worldwind.layers.shader;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.globes.SectorGeometry;
import gov.nasa.worldwind.globes.SectorGeometryList;
import gov.nasa.worldwind.globes.Tessellator;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.awt.Point;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

/**
 * @author tag
 * @version $Id: RectangularTessellator.java 5273 2008-05-02 00:42:05Z tgaskins
 *          $
 * 
 *          Modified by Michael de Hoog to add simple normals based on globe
 *          ellipse, (globe.computeSurfaceNormalAtPoint()), also added more
 *          exact normal calculator for terrain tiles, see getNormals()
 */
public class NormalTessellator extends WWObjectImpl implements Tessellator
{
	protected static class RenderInfo
	{
		private final int density;
		private final int resolution;
		private final Vec4 referenceCenter;
		private final DoubleBuffer vertices;
		private final DoubleBuffer normals;
		private final DoubleBuffer texCoords;
		private final IntBuffer indices;

		private RenderInfo(int density, DoubleBuffer vertices,
				DoubleBuffer normals, Vec4 refCenter, int resolution)
		{
			this.density = density;
			this.vertices = vertices;
			this.texCoords = getTextureCoordinates(density);
			this.referenceCenter = refCenter;
			this.indices = getIndices(density);
			this.normals = normals;
			this.resolution = resolution;
		}

		private long getSizeInBytes()
		{
			// Texture coordinates are shared among all tiles of the same density, so do not count towards size.
			// 8 references, doubles in buffer.
			return 8 * 4 + (this.vertices.limit()) * Double.SIZE;
		}
	}

	public static class RectTile implements SectorGeometry
	{
		private final NormalTessellator tessellator;
		private final int level;
		private final Sector sector;
		private final int density;
		private final double log10CellSize;
		private Extent extent; // extent of sector in object coordinates
		private RenderInfo ri;

		private int minColorCode = 0;
		private int maxColorCode = 0;

		public RectTile(NormalTessellator tessellator, Extent extent,
				int level, int density, Sector sector, double cellSize)
		{
			this.tessellator = tessellator;
			this.level = level;
			this.density = density;
			this.sector = sector;
			this.extent = extent;
			this.log10CellSize = Math.log10(cellSize);
		}

		public Sector getSector()
		{
			return this.sector;
		}

		public Extent getExtent()
		{
			return this.extent;
		}

		public void renderMultiTexture(DrawContext dc, int numTextureUnits)
		{
			this.tessellator.renderMultiTexture(dc, this, numTextureUnits);
		}

		public void render(DrawContext dc)
		{
			this.tessellator.render(dc, this);
		}

		public void renderWireframe(DrawContext dc, boolean showTriangles,
				boolean showTileBoundary)
		{
			this.tessellator.renderWireframe(dc, this, showTriangles,
					showTileBoundary);
		}

		public void renderBoundingVolume(DrawContext dc)
		{
			this.tessellator.renderBoundingVolume(dc, this);
		}

		public PickedObject[] pick(DrawContext dc, List<Point> pickPoints)
		{
			return this.tessellator.pick(dc, this, pickPoints);
		}

		public void pick(DrawContext dc, Point pickPoint)
		{
			this.tessellator.pick(dc, this, pickPoint);
		}

		public Vec4 getSurfacePoint(Angle latitude, Angle longitude,
				double metersOffset)
		{
			return this.tessellator.getSurfacePoint(this, latitude, longitude,
					metersOffset);
		}
	}

	private static class CacheKey
	{
		private final Sector sector;
		private final int resolution;
		private final int density;
		private final Globe globe;
		private final Object globeStateKey;
		private final double verticalExaggeration;

		public CacheKey(Globe globe, Sector sector,
				double verticalExaggeration, int resolution, int density)
		{
			this.sector = sector;
			this.resolution = resolution;
			this.density = density;
			this.globe = globe;
			this.globeStateKey = globe.getStateKey();
			this.verticalExaggeration = verticalExaggeration;
		}

		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			CacheKey cacheKey = (CacheKey) o;

			if (density != cacheKey.density)
				return false;
			if (resolution != cacheKey.resolution)
				return false;
			if (Double.compare(cacheKey.verticalExaggeration,
					verticalExaggeration) != 0)
				return false;
			if (globe != null ? !globe.equals(cacheKey.globe)
					: cacheKey.globe != null)
				return false;
			if (globeStateKey != null ? !globeStateKey
					.equals(cacheKey.globeStateKey)
					: cacheKey.globeStateKey != null)
				return false;
			//noinspection RedundantIfStatement
			if (sector != null ? !sector.equals(cacheKey.sector)
					: cacheKey.sector != null)
				return false;

			return true;
		}

		public int hashCode()
		{
			int result;
			long temp;
			result = (sector != null ? sector.hashCode() : 0);
			result = 31 * result + resolution;
			result = 31 * result + density;
			result = 31 * result + (globe != null ? globe.hashCode() : 0);
			result = 31 * result
					+ (globeStateKey != null ? globeStateKey.hashCode() : 0);
			temp = verticalExaggeration != +0.0d ? Double
					.doubleToLongBits(verticalExaggeration) : 0L;
			result = 31 * result + (int) (temp ^ (temp >>> 32));
			return result;
		}
	}

	// TODO: Make all this configurable
	private static final double DEFAULT_LOG10_RESOLUTION_TARGET = 1.3;
	private static final int DEFAULT_MAX_LEVEL = 12;
	private static final int DEFAULT_NUM_LAT_SUBDIVISIONS = 5;
	private static final int DEFAULT_NUM_LON_SUBDIVISIONS = 10;
	private static final int DEFAULT_DENSITY = 20;
	private static final String CACHE_NAME = "Terrain";
	private static final String CACHE_ID = NormalTessellator.class.getName();

	// Tri-strip indices and texture coordinates. These depend only on density and can therefore be statically cached.
	private static final HashMap<Integer, DoubleBuffer> parameterizations = new HashMap<Integer, DoubleBuffer>();
	private static final HashMap<Integer, IntBuffer> indexLists = new HashMap<Integer, IntBuffer>();

	private ArrayList<RectTile> topLevels;
	private PickSupport pickSupport = new PickSupport();
	private SectorGeometryList currentTiles = new SectorGeometryList();
	private Frustum currentFrustum;
	private Sector currentCoverage; // union of all tiles selected during call to render()
	private boolean makeTileSkirts = true;
	private int currentLevel;
	private int maxLevel = DEFAULT_MAX_LEVEL;
	private Globe globe;
	private int density = DEFAULT_DENSITY;

	public SectorGeometryList tessellate(DrawContext dc)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (dc.getView() == null)
		{
			String msg = Logging.getMessage("nullValue.ViewIsNull");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		if (!WorldWind.getMemoryCacheSet().containsCache(CACHE_ID))
		{
			long size = Configuration.getLongValue(
					AVKey.SECTOR_GEOMETRY_CACHE_SIZE, 20000000L);
			MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
			cache.setName(CACHE_NAME);
			WorldWind.getMemoryCacheSet().addCache(CACHE_ID, cache);
		}

		if (this.topLevels == null)
			this.topLevels = this.createTopLevelTiles(dc);

		this.currentTiles.clear();
		this.currentLevel = 0;
		this.currentCoverage = null;

		this.currentFrustum = dc.getView().getFrustumInModelCoordinates();
		for (RectTile tile : this.topLevels)
		{
			this.selectVisibleTiles(dc, tile);
		}

		this.currentTiles.setSector(this.currentCoverage);

		for (SectorGeometry tile : this.currentTiles)
		{
			this.makeVerts(dc, (RectTile) tile);
		}

		return this.currentTiles;
	}

	private ArrayList<RectTile> createTopLevelTiles(DrawContext dc)
	{
		ArrayList<RectTile> tops = new ArrayList<RectTile>(
				DEFAULT_NUM_LAT_SUBDIVISIONS * DEFAULT_NUM_LON_SUBDIVISIONS);

		this.globe = dc.getGlobe();
		double deltaLat = 180d / DEFAULT_NUM_LAT_SUBDIVISIONS;
		double deltaLon = 360d / DEFAULT_NUM_LON_SUBDIVISIONS;
		Angle lastLat = Angle.NEG90;

		for (int row = 0; row < DEFAULT_NUM_LAT_SUBDIVISIONS; row++)
		{
			Angle lat = lastLat.addDegrees(deltaLat);
			if (lat.getDegrees() + 1d > 90d)
				lat = Angle.POS90;

			Angle lastLon = Angle.NEG180;

			for (int col = 0; col < DEFAULT_NUM_LON_SUBDIVISIONS; col++)
			{
				Angle lon = lastLon.addDegrees(deltaLon);
				if (lon.getDegrees() + 1d > 180d)
					lon = Angle.POS180;

				Sector tileSector = new Sector(lastLat, lat, lastLon, lon);
				tops.add(this.createTile(dc, tileSector, 0));
				lastLon = lon;
			}
			lastLat = lat;
		}

		return tops;
	}

	private RectTile createTile(DrawContext dc, Sector tileSector, int level)
	{
		Cylinder cylinder = dc.getGlobe().computeBoundingCylinder(
				dc.getVerticalExaggeration(), tileSector);
		double cellSize = tileSector.getDeltaLatRadians()
				* dc.getGlobe().getRadius() / this.density;

		return new RectTile(this, cylinder, level, this.density, tileSector,
				cellSize);
	}

	public boolean isMakeTileSkirts()
	{
		return makeTileSkirts;
	}

	public void setMakeTileSkirts(boolean makeTileSkirts)
	{
		this.makeTileSkirts = makeTileSkirts;
	}

	public int getTargetResolution(DrawContext dc, RectTile tile)
	{
		return dc.getGlobe().getElevationModel().getTargetResolution(dc,
				tile.sector, tile.density);
	}

	private void selectVisibleTiles(DrawContext dc, RectTile tile)
	{
		Extent extent = tile.getExtent();
		if (extent != null && !extent.intersects(this.currentFrustum))
			return;

		if (this.currentLevel < this.maxLevel - 1 && this.needToSplit(dc, tile))
		{
			++this.currentLevel;
			RectTile[] subtiles = this.split(dc, tile);
			for (RectTile child : subtiles)
			{
				this.selectVisibleTiles(dc, child);
			}
			--this.currentLevel;
			return;
		}
		this.currentCoverage = tile.getSector().union(this.currentCoverage);
		this.currentTiles.add(tile);
	}

	private boolean needToSplit(DrawContext dc, RectTile tile)
	{
		Vec4[] corners = tile.sector.computeCornerPoints(dc.getGlobe());
		Vec4 centerPoint = tile.sector.computeCenterPoint(dc.getGlobe());

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

		double logDist = Math.log10(minDistance);
		boolean useTile = tile.log10CellSize <= (logDist - DEFAULT_LOG10_RESOLUTION_TARGET);

		return !useTile;
	}

	private RectTile[] split(DrawContext dc, RectTile tile)
	{
		Sector[] sectors = tile.sector.subdivide();

		RectTile[] subTiles = new RectTile[4];
		subTiles[0] = this.createTile(dc, sectors[0], tile.level + 1);
		subTiles[1] = this.createTile(dc, sectors[1], tile.level + 1);
		subTiles[2] = this.createTile(dc, sectors[2], tile.level + 1);
		subTiles[3] = this.createTile(dc, sectors[3], tile.level + 1);

		return subTiles;
	}

	private NormalTessellator.CacheKey createCacheKey(DrawContext dc,
			RectTile tile, int resolution)
	{
		return new CacheKey(dc.getGlobe(), tile.sector, dc
				.getVerticalExaggeration(), resolution, tile.density);
	}

	private void makeVerts(DrawContext dc, RectTile tile)
	{
		int resolution = this.getTargetResolution(dc, tile);

		MemoryCache cache = WorldWind.getMemoryCache(CACHE_ID);
		CacheKey cacheKey = this.createCacheKey(dc, tile, resolution);
		tile.ri = (RenderInfo) cache.getObject(cacheKey);
		if (tile.ri != null)
			return;

		tile.ri = this.buildVerts(dc, tile, resolution, this.makeTileSkirts);
		if (tile.ri != null && tile.ri.resolution >= 0)
		{
			cacheKey = this.createCacheKey(dc, tile, tile.ri.resolution);
			cache.add(cacheKey, tile.ri, tile.ri.getSizeInBytes());
		}
	}

	public RenderInfo buildVerts(DrawContext dc, RectTile tile, int resolution,
			boolean makeSkirts)
	{
		int density = tile.density;
		int side = density + 3;
		int numVertices = side * side;
		java.nio.DoubleBuffer verts = BufferUtil
				.newDoubleBuffer(numVertices * 3);

		Globe globe = dc.getGlobe();
		ElevationModel.Elevations elevations = globe.getElevationModel()
				.getElevations(tile.sector, resolution);

		Angle dLat = tile.sector.getDeltaLat().divide(density);
		Angle latMin = tile.sector.getMinLatitude();
		Angle latMax = tile.sector.getMaxLatitude();
		Angle latStart = latMin.subtract(dLat);

		Angle dLon = tile.sector.getDeltaLon().divide(density);
		Angle lonMin = tile.sector.getMinLongitude();
		Angle lonMax = tile.sector.getMaxLongitude();
		Angle lonStart = lonMin.subtract(dLon);

		Angle lat, lon;
		int iv = 0;
		double elevation, verticalExaggeration = dc.getVerticalExaggeration();
		Vec4 p;

		//LatLon centroid = tile.sector.getCentroid();
		//Vec4 refCenter = globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(), 0d);
		Vec4 refCenter = Vec4.UNIT_W;

		//calculate verts without skirts
		lat = latStart;
		for (int j = 0; j < side; j++)
		{
			lon = lonStart;
			for (int i = 0; i < side; i++)
			{
				elevation = verticalExaggeration
						* elevations.getElevation(lat.radians, lon.radians);
				p = globe.computePointFromPosition(lat, lon, elevation);
				verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y)
						.put(iv++, p.z - refCenter.z);

				lon = lon.add(dLon);
			}
			lat = lat.add(dLat);
		}

		//calculate normals
		java.nio.DoubleBuffer norms = getNormals(density, verts, refCenter);

		//fold down the sides as skirts
		double exaggeratedMinElevation = makeSkirts ? Math.abs(globe
				.getMinElevation()
				* verticalExaggeration) : 0;
		lat = latMin;
		for (int j = 0; j < side; j++)
		{
			//min longitude
			elevation = verticalExaggeration
					* elevations.getElevation(lat.radians, lonMin.radians)
					- exaggeratedMinElevation;
			p = globe.computePointFromPosition(lat, lonMin, elevation);
			iv = (j * side) * 3;
			verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y)
					.put(iv++, p.z - refCenter.z);

			//max longitude
			elevation = verticalExaggeration
					* elevations.getElevation(lat.radians, lonMax.radians)
					- exaggeratedMinElevation;
			p = globe.computePointFromPosition(lat, lonMax, elevation);
			iv = ((j + 1) * side - 1) * 3;
			verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y)
					.put(iv++, p.z - refCenter.z);

			if (j > density)
				lat = latMax;
			else if (j != 0)
				lat = lat.add(dLat);
		}

		lon = lonMin;
		for (int i = 0; i < side; i++)
		{
			//min latitude
			elevation = verticalExaggeration
					* elevations.getElevation(latMin.radians, lon.radians)
					- exaggeratedMinElevation;
			p = globe.computePointFromPosition(latMin, lon, elevation);
			iv = i * 3;
			verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y)
					.put(iv++, p.z - refCenter.z);

			//max latitude
			elevation = verticalExaggeration
					* elevations.getElevation(latMax.radians, lon.radians)
					- exaggeratedMinElevation;
			p = globe.computePointFromPosition(latMax, lon, elevation);
			iv = (side * (side - 1) + i) * 3;
			verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y)
					.put(iv++, p.z - refCenter.z);

			if (i > density)
				lon = lonMax;
			else if (i != 0)
				lon = lon.add(dLon);
		}

		return new RenderInfo(density, verts, norms, refCenter, elevations
				.getResolution());
	}

	private void renderMultiTexture(DrawContext dc, RectTile tile,
			int numTextureUnits)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (numTextureUnits < 1)
		{
			String msg = Logging
					.getMessage("generic.NumTextureUnitsLessThanOne");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		this.render(dc, tile, numTextureUnits);
	}

	private void render(DrawContext dc, RectTile tile)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		this.render(dc, tile, 1);
	}

	private long render(DrawContext dc, RectTile tile, int numTextureUnits)
	{
		if (tile.ri == null)
		{
			String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		dc.getView().pushReferenceCenter(dc, tile.ri.referenceCenter);

		GL gl = dc.getGL();
		gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL.GL_DOUBLE, 0, tile.ri.vertices.rewind());

		gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
		gl.glNormalPointer(GL.GL_DOUBLE, 0, tile.ri.normals.rewind());

		for (int i = 0; i < numTextureUnits; i++)
		{
			gl.glClientActiveTexture(GL.GL_TEXTURE0 + i);
			gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
			gl
					.glTexCoordPointer(2, GL.GL_DOUBLE, 0, tile.ri.texCoords
							.rewind());
		}

		gl.glDrawElements(javax.media.opengl.GL.GL_TRIANGLE_STRIP,
				tile.ri.indices.limit(), javax.media.opengl.GL.GL_UNSIGNED_INT,
				tile.ri.indices.rewind());

		gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
		gl.glPopClientAttrib();

		dc.getView().popReferenceCenter(dc);

		return tile.ri.indices.limit() - 2; // return number of triangles rendered
	}

	private void renderWireframe(DrawContext dc, RectTile tile,
			boolean showTriangles, boolean showTileBoundary)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (tile.ri == null)
		{
			String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		java.nio.IntBuffer indices = getIndices(tile.ri.density);
		indices.rewind();

		dc.getView().pushReferenceCenter(dc, tile.ri.referenceCenter);

		javax.media.opengl.GL gl = dc.getGL();
		gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT | GL.GL_POLYGON_BIT
				| GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
		gl.glDisable(javax.media.opengl.GL.GL_DEPTH_TEST);
		gl.glEnable(javax.media.opengl.GL.GL_CULL_FACE);
		gl.glCullFace(javax.media.opengl.GL.GL_BACK);
		gl.glDisable(javax.media.opengl.GL.GL_TEXTURE_2D);
		gl.glColor4d(1d, 1d, 1d, 0.2);
		gl.glPolygonMode(javax.media.opengl.GL.GL_FRONT,
				javax.media.opengl.GL.GL_LINE);

		if (showTriangles)
		{
			gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
			gl.glEnableClientState(GL.GL_VERTEX_ARRAY);

			gl.glVertexPointer(3, GL.GL_DOUBLE, 0, tile.ri.vertices);
			gl.glDrawElements(javax.media.opengl.GL.GL_TRIANGLE_STRIP, indices
					.limit(), javax.media.opengl.GL.GL_UNSIGNED_INT, indices);

			gl.glPopClientAttrib();
		}

		dc.getView().popReferenceCenter(dc);

		if (showTileBoundary)
			this.renderPatchBoundary(dc, tile, gl);

		gl.glPopAttrib();
	}

	private void renderPatchBoundary(DrawContext dc, RectTile tile, GL gl)
	{
		// TODO: Currently only works if called from renderWireframe because no state is set here.
		// TODO: Draw the boundary using the vertices along the boundary rather than just at the corners.
		gl.glColor4d(1d, 0, 0, 1d);
		Vec4[] corners = tile.sector.computeCornerPoints(dc.getGlobe());

		gl.glBegin(javax.media.opengl.GL.GL_QUADS);
		gl.glVertex3d(corners[0].x, corners[0].y, corners[0].z);
		gl.glVertex3d(corners[1].x, corners[1].y, corners[1].z);
		gl.glVertex3d(corners[2].x, corners[2].y, corners[2].z);
		gl.glVertex3d(corners[3].x, corners[3].y, corners[3].z);
		gl.glEnd();
	}

	private void renderBoundingVolume(DrawContext dc, RectTile tile)
	{
		Extent extent = tile.getExtent();
		if (extent == null)
			return;

		if (extent instanceof Cylinder)
			((Cylinder) extent).render(dc);
	}

	private PickedObject[] pick(DrawContext dc, RectTile tile,
			List<Point> pickPoints)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (pickPoints.size() == 0)
			return null;

		if (tile.ri == null)
			return null;

		PickedObject[] pos = new PickedObject[pickPoints.size()];
		this.renderTrianglesWithUniqueColors(dc, tile);
		for (int i = 0; i < pickPoints.size(); i++)
		{
			pos[i] = this.resolvePick(dc, tile, pickPoints.get(i));
		}

		return pos;
	}

	private void pick(DrawContext dc, RectTile tile, Point pickPoint)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (tile.ri == null)
			return;

		renderTrianglesWithUniqueColors(dc, tile);
		PickedObject po = this.resolvePick(dc, tile, pickPoint);
		if (po != null)
			dc.addPickedObject(po);
	}

	private void renderTrianglesWithUniqueColors(DrawContext dc, RectTile tile)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (tile.ri.vertices == null)
			return;

		tile.ri.vertices.rewind();
		tile.ri.indices.rewind();

		javax.media.opengl.GL gl = dc.getGL();

		if (null != tile.ri.referenceCenter)
			dc.getView().pushReferenceCenter(dc, tile.ri.referenceCenter);

		tile.minColorCode = dc.getUniquePickColor().getRGB();
		int trianglesNum = tile.ri.indices.capacity() - 2;

		gl.glBegin(GL.GL_TRIANGLES);
		for (int i = 0; i < trianglesNum; i++)
		{
			java.awt.Color color = dc.getUniquePickColor();
			gl.glColor3ub((byte) (color.getRed() & 0xFF), (byte) (color
					.getGreen() & 0xFF), (byte) (color.getBlue() & 0xFF));

			int vIndex = 3 * tile.ri.indices.get(i);
			gl.glVertex3d(tile.ri.vertices.get(vIndex), tile.ri.vertices
					.get(vIndex + 1), tile.ri.vertices.get(vIndex + 2));

			vIndex = 3 * tile.ri.indices.get(i + 1);
			gl.glVertex3d(tile.ri.vertices.get(vIndex), tile.ri.vertices
					.get(vIndex + 1), tile.ri.vertices.get(vIndex + 2));

			vIndex = 3 * tile.ri.indices.get(i + 2);
			gl.glVertex3d(tile.ri.vertices.get(vIndex), tile.ri.vertices
					.get(vIndex + 1), tile.ri.vertices.get(vIndex + 2));
		}
		gl.glEnd();
		tile.maxColorCode = dc.getUniquePickColor().getRGB();

		if (null != tile.ri.referenceCenter)
			dc.getView().popReferenceCenter(dc);
	}

	private PickedObject resolvePick(DrawContext dc, RectTile tile,
			Point pickPoint)
	{
		int colorCode = this.pickSupport.getTopColor(dc, pickPoint);
		if (colorCode < tile.minColorCode || colorCode > tile.maxColorCode)
			return null;

		double EPSILON = (double) 0.00001f;

		int triangleIndex = colorCode - tile.minColorCode - 1;

		if (tile.ri.indices == null
				|| triangleIndex >= (tile.ri.indices.capacity() - 2))
			return null;

		double centerX = tile.ri.referenceCenter.x;
		double centerY = tile.ri.referenceCenter.y;
		double centerZ = tile.ri.referenceCenter.z;

		int vIndex = 3 * tile.ri.indices.get(triangleIndex);
		Vec4 v0 = new Vec4((tile.ri.vertices.get(vIndex++) + centerX),
				(tile.ri.vertices.get(vIndex++) + centerY), (tile.ri.vertices
						.get(vIndex) + centerZ));

		vIndex = 3 * tile.ri.indices.get(triangleIndex + 1);
		Vec4 v1 = new Vec4((tile.ri.vertices.get(vIndex++) + centerX),
				(tile.ri.vertices.get(vIndex++) + centerY), (tile.ri.vertices
						.get(vIndex) + centerZ));

		vIndex = 3 * tile.ri.indices.get(triangleIndex + 2);
		Vec4 v2 = new Vec4((tile.ri.vertices.get(vIndex++) + centerX),
				(tile.ri.vertices.get(vIndex++) + centerY), (tile.ri.vertices
						.get(vIndex) + centerZ));

		// get triangle edge vectors and plane normal
		Vec4 e1 = v1.subtract3(v0);
		Vec4 e2 = v2.subtract3(v0);
		Vec4 N = e1.cross3(e2); // if N is 0, the triangle is degenerate, we are not dealing with it

		Line ray = dc.getView().computeRayFromScreenPoint(pickPoint.getX(),
				pickPoint.getY());

		Vec4 w0 = ray.getOrigin().subtract3(v0);
		double a = -N.dot3(w0);
		double b = N.dot3(ray.getDirection());
		if (java.lang.Math.abs(b) < EPSILON) // ray is parallel to triangle plane
			return null; // if a == 0 , ray lies in triangle plane
		double r = a / b;

		Vec4 intersect = ray.getOrigin().add3(ray.getDirection().multiply3(r));
		Position pp = dc.getGlobe().computePositionFromPoint(intersect);

		// Draw the elevation from the elevation model, not the geode.
		double elev = dc.getGlobe().getElevation(pp.getLatitude(),
				pp.getLongitude());
		Position p = new Position(pp.getLatitude(), pp.getLongitude(), elev);

		return new PickedObject(pickPoint, colorCode, p, pp.getLatitude(), pp
				.getLongitude(), elev, true);
	}

	private Vec4 getSurfacePoint(RectTile tile, Angle latitude,
			Angle longitude, double metersOffset)
	{
		Vec4 result = this.getSurfacePoint(tile, latitude, longitude);
		if (metersOffset != 0 && result != null)
			result = applyOffset(this.globe, result, metersOffset);

		return result;
	}

	/**
	 * Offsets <code>point</code> by <code>metersOffset</code> meters.
	 * 
	 * @param globe
	 *            the <code>Globe</code> from which to offset
	 * @param point
	 *            the <code>Vec4</code> to offset
	 * @param metersOffset
	 *            the magnitude of the offset
	 * @return <code>point</code> offset along its surface normal as if it were
	 *         on <code>globe</code>
	 */
	private static Vec4 applyOffset(Globe globe, Vec4 point, double metersOffset)
	{
		Vec4 normal = globe.computeSurfaceNormalAtPoint(point);
		point = Vec4.fromLine3(point, metersOffset, normal);
		return point;
	}

	private Vec4 getSurfacePoint(RectTile tile, Angle latitude, Angle longitude)
	{
		if (latitude == null || longitude == null)
		{
			String msg = Logging.getMessage("nullValue.LatLonIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (!tile.sector.contains(latitude, longitude))
		{
			// not on this geometry
			return null;
		}

		if (tile.ri == null)
			return null;

		double lat = latitude.getDegrees();
		double lon = longitude.getDegrees();

		double bottom = tile.sector.getMinLatitude().getDegrees();
		double top = tile.sector.getMaxLatitude().getDegrees();
		double left = tile.sector.getMinLongitude().getDegrees();
		double right = tile.sector.getMaxLongitude().getDegrees();

		double leftDecimal = (lon - left) / (right - left);
		double bottomDecimal = (lat - bottom) / (top - bottom);

		int row = (int) (bottomDecimal * (tile.density));
		int column = (int) (leftDecimal * (tile.density));

		double l = createPosition(column, leftDecimal, tile.ri.density);
		double h = createPosition(row, bottomDecimal, tile.ri.density);

		Vec4 result = interpolate(row, column, l, h, tile.ri);
		result = result.add3(tile.ri.referenceCenter);

		return result;
	}

	/**
	 * Computes from a column (or row) number, and a given offset ranged [0,1]
	 * corresponding to the distance along the edge of this sector, where
	 * between this column and the next column the corresponding position will
	 * fall, in the range [0,1].
	 * 
	 * @param start
	 *            the number of the column or row to the left, below or on this
	 *            position
	 * @param decimal
	 *            the distance from the left or bottom of the current sector
	 *            that this position falls
	 * @param density
	 *            the number of intervals along the sector's side
	 * @return a decimal ranged [0,1] representing the position between two
	 *         columns or rows, rather than between two edges of the sector
	 */
	private static double createPosition(int start, double decimal, int density)
	{
		double l = ((double) start) / (double) density;
		double r = ((double) (start + 1)) / (double) density;

		return (decimal - l) / (r - l);
	}

	/**
	 * Calculates a <code>Point</code> that sits at <code>xDec</code> offset
	 * from <code>column</code> to <code>column +
	 * 1</code> and at <code>yDec</code> offset from <code>row</code> to
	 * <code>row + 1</code>. Accounts for the diagonals.
	 * 
	 * @param row
	 *            represents the row which corresponds to a <code>yDec</code>
	 *            value of 0
	 * @param column
	 *            represents the column which corresponds to an
	 *            <code>xDec</code> value of 0
	 * @param xDec
	 *            constrained to [0,1]
	 * @param yDec
	 *            constrained to [0,1]
	 * @param ri
	 *            the render info holding the vertices, etc.
	 * @return a <code>Point</code> geometrically within or on the boundary of
	 *         the quadrilateral whose bottom left corner is indexed by (
	 *         <code>row</code>, <code>column</code>)
	 */
	private static Vec4 interpolate(int row, int column, double xDec,
			double yDec, RenderInfo ri)
	{
		row++;
		column++;

		int numVerticesPerEdge = ri.density + 3;

		int bottomLeft = row * numVerticesPerEdge + column;

		bottomLeft *= 3;

		int numVertsTimesThree = numVerticesPerEdge * 3;

		Vec4 bL = new Vec4(ri.vertices.get(bottomLeft), ri.vertices
				.get(bottomLeft + 1), ri.vertices.get(bottomLeft + 2));
		Vec4 bR = new Vec4(ri.vertices.get(bottomLeft + 3), ri.vertices
				.get(bottomLeft + 4), ri.vertices.get(bottomLeft + 5));

		bottomLeft += numVertsTimesThree;

		Vec4 tL = new Vec4(ri.vertices.get(bottomLeft), ri.vertices
				.get(bottomLeft + 1), ri.vertices.get(bottomLeft + 2));
		Vec4 tR = new Vec4(ri.vertices.get(bottomLeft + 3), ri.vertices
				.get(bottomLeft + 4), ri.vertices.get(bottomLeft + 5));

		return interpolate(bL, bR, tR, tL, xDec, yDec);
	}

	/**
	 * Calculates the point at (xDec, yDec) in the two triangles defined by {bL,
	 * bR, tL} and {bR, tR, tL}. If thought of as a quadrilateral, the diagonal
	 * runs from tL to bR. Of course, this isn't a quad, it's two triangles.
	 * 
	 * @param bL
	 *            the bottom left corner
	 * @param bR
	 *            the bottom right corner
	 * @param tR
	 *            the top right corner
	 * @param tL
	 *            the top left corner
	 * @param xDec
	 *            how far along, [0,1] 0 = left edge, 1 = right edge
	 * @param yDec
	 *            how far along, [0,1] 0 = bottom edge, 1 = top edge
	 * @return the point xDec, yDec in the co-ordinate system defined by bL, bR,
	 *         tR, tL
	 */
	private static Vec4 interpolate(Vec4 bL, Vec4 bR, Vec4 tR, Vec4 tL,
			double xDec, double yDec)
	{
		double pos = xDec + yDec;
		if (pos == 1)
		{
			// on the diagonal - what's more, we don't need to do any "oneMinusT" calculation
			return new Vec4(tL.x * yDec + bR.x * xDec, tL.y * yDec + bR.y
					* xDec, tL.z * yDec + bR.z * xDec);
		}
		else if (pos > 1)
		{
			// in the "top right" half

			// vectors pointing from top right towards the point we want (can be thought of as "negative" vectors)
			Vec4 horizontalVector = (tL.subtract3(tR)).multiply3(1 - xDec);
			Vec4 verticalVector = (bR.subtract3(tR)).multiply3(1 - yDec);

			return tR.add3(horizontalVector).add3(verticalVector);
		}
		else
		{
			// pos < 1 - in the "bottom left" half

			// vectors pointing from the bottom left towards the point we want
			Vec4 horizontalVector = (bR.subtract3(bL)).multiply3(xDec);
			Vec4 verticalVector = (tL.subtract3(bL)).multiply3(yDec);

			return bL.add3(horizontalVector).add3(verticalVector);
		}
	}

	private static DoubleBuffer getTextureCoordinates(int density)
	{
		if (density < 1)
			density = 1;

		// Approximate 1 to avoid shearing off of right and top skirts in SurfaceTileRenderer.
		// TODO: dig into this more: why are the skirts being sheared off?
		final double one = 0.999999;

		DoubleBuffer p = parameterizations.get(density);
		if (p != null)
			return p;

		int coordCount = (density + 3) * (density + 3);
		p = BufferUtil.newDoubleBuffer(2 * coordCount);
		double delta = 1d / density;
		int k = 2 * (density + 3);
		for (int j = 0; j < density; j++)
		{
			double v = j * delta;

			// skirt column; duplicate first column
			p.put(k++, 0d);
			p.put(k++, v);

			// interior columns
			for (int i = 0; i < density; i++)
			{
				p.put(k++, i * delta); // u
				p.put(k++, v);
			}

			// last interior column; force u to 1.
			p.put(k++, one);//1d);
			p.put(k++, v);

			// skirt column; duplicate previous column
			p.put(k++, one);//1d);
			p.put(k++, v);
		}

		// Last interior row
		//noinspection UnnecessaryLocalVariable
		double v = one;//1d;
		p.put(k++, 0d); // skirt column
		p.put(k++, v);

		for (int i = 0; i < density; i++)
		{
			p.put(k++, i * delta); // u
			p.put(k++, v);
		}
		p.put(k++, one);//1d); // last interior column
		p.put(k++, v);

		p.put(k++, one);//1d); // skirt column
		p.put(k++, v);

		// last skirt row
		int kk = k - 2 * (density + 3);
		for (int i = 0; i < density + 3; i++)
		{
			p.put(k++, p.get(kk++));
			p.put(k++, p.get(kk++));
		}

		// first skirt row
		k = 0;
		kk = 2 * (density + 3);
		for (int i = 0; i < density + 3; i++)
		{
			p.put(k++, p.get(kk++));
			p.put(k++, p.get(kk++));
		}

		parameterizations.put(density, p);

		return p;
	}

	protected static IntBuffer getIndices(int density)
	{
		if (density < 1)
			density = 1;

		// return a pre-computed buffer if possible.
		java.nio.IntBuffer buffer = indexLists.get(density);
		if (buffer != null)
			return buffer;

		int sideSize = density + 2;

		int indexCount = 2 * sideSize * sideSize + 4 * sideSize - 2;
		buffer = BufferUtil.newIntBuffer(indexCount);
		int k = 0;
		for (int i = 0; i < sideSize; i++)
		{
			buffer.put(k);
			if (i > 0)
			{
				buffer.put(++k);
				buffer.put(k);
			}

			if (i % 2 == 0) // even
			{
				buffer.put(++k);
				for (int j = 0; j < sideSize; j++)
				{
					k += sideSize;
					buffer.put(k);
					buffer.put(++k);
				}
			}
			else
			// odd
			{
				buffer.put(--k);
				for (int j = 0; j < sideSize; j++)
				{
					k -= sideSize;
					buffer.put(k);
					buffer.put(--k);
				}
			}
		}

		indexLists.put(density, buffer);

		return buffer;
	}

	protected static DoubleBuffer getNormals(int density,
			DoubleBuffer vertices, Vec4 referenceCenter)
	{
		int side = density + 3;
		int numVertices = side * side;
		java.nio.DoubleBuffer normals = BufferUtil
				.newDoubleBuffer(numVertices * 3);
		Vec4 p0, p1, p2, p3, p4;

		//don't calculate skirt normals yet
		for (int j = 1; j < side - 1; j++)
		{
			for (int i = 1; i < side - 1; i++)
			{
				int index0 = j * side + i;
				int index1 = j * side + (i - 1);
				int index2 = j * side + (i + 1);
				int index3 = (j - 1) * side + i;
				int index4 = (j + 1) * side + i;
				p0 = getVec4(index0, vertices, referenceCenter);
				p1 = getVec4(index1, vertices, referenceCenter);
				p2 = getVec4(index2, vertices, referenceCenter);
				p3 = getVec4(index3, vertices, referenceCenter);
				p4 = getVec4(index4, vertices, referenceCenter);

				Vec4 n1 = p0.subtract3(p1).normalize3().cross3(
						p0.subtract3(p3).normalize3());
				Vec4 n2 = p0.subtract3(p2).normalize3().cross3(
						p0.subtract3(p4).normalize3());
				Vec4 normal = n1.add3(n2).normalize3();

				normals.put(index0 * 3, normal.x).put(index0 * 3 + 1, normal.y)
						.put(index0 * 3 + 2, normal.z);
			}
		}

		//copy skirt normals from neighbours
		for (int i = 1; i < side - 1; i++)
		{
			int di = i;
			int si = side + i;
			copyNormalInBuffer(si, di, normals);

			di = side * (side - 1) + i;
			si = side * (side - 2) + i;
			copyNormalInBuffer(si, di, normals);
		}
		for (int i = 0; i < side; i++)
		{
			int di = i * side;
			int si = i * side + 1;
			copyNormalInBuffer(si, di, normals);

			di = i * side + (side - 1);
			si = i * side + (side - 2);
			copyNormalInBuffer(si, di, normals);
		}

		return normals;
	}

	private static Vec4 getVec4(int index, DoubleBuffer vertices,
			Vec4 referenceCenter)
	{
		return new Vec4(vertices.get(index * 3) + referenceCenter.x, vertices
				.get(index * 3 + 1)
				+ referenceCenter.y, vertices.get(index * 3 + 2)
				+ referenceCenter.z);
	}

	private static void copyNormalInBuffer(int srcIndex, int dstIndex,
			DoubleBuffer normals)
	{
		normals.put(dstIndex * 3, normals.get(srcIndex * 3));
		normals.put(dstIndex * 3 + 1, normals.get(srcIndex * 3 + 1));
		normals.put(dstIndex * 3 + 2, normals.get(srcIndex * 3 + 2));
	}

	// TODO: The following method was brought over from BasicRectangularTessellator and is unchecked.
	//    // Compute normals for a strip
	//    protected static java.nio.DoubleBuffer getNormals(int density, DoubleBuffer vertices,
	//        java.nio.IntBuffer indices, Vec4 referenceCenter)
	//    {
	//        int numVertices = (density + 3) * (density + 3);
	//        //int sideSize = density + 2;
	//        int numFaces = indices.limit() - 2;
	//        double centerX = referenceCenter.x;
	//        double centerY = referenceCenter.y;
	//        double centerZ = referenceCenter.z;
	//        // Create normal buffer
	//        java.nio.DoubleBuffer normals = BufferUtil.newDoubleBuffer(numVertices * 3);
	//        // Create per vertex normal lists
	//        ArrayList<ArrayList<Vec4>> normalLists = new ArrayList<ArrayList<Vec4>>(numVertices);
	//        for (int i = 0; i < numVertices; i++)
	//            normalLists.add(new ArrayList<Vec4>());
	//        // Go through all faces in the strip and store normals in lists
	//        for (int i = 0; i < numFaces; i++)
	//        {
	//            int vIndex = 3 * indices.get(i);
	//            Vec4 v0 = new Vec4((vertices.get(vIndex++) + centerX),
	//                (vertices.get(vIndex++) + centerY),
	//                (vertices.get(vIndex) + centerZ));
	//
	//            vIndex = 3 * indices.get(i + 1);
	//            Vec4 v1 = new Vec4((vertices.get(vIndex++) + centerX),
	//                (vertices.get(vIndex++) + centerY),
	//                (vertices.get(vIndex) + centerZ));
	//
	//            vIndex = 3 * indices.get(i + 2);
	//            Vec4 v2 = new Vec4((vertices.get(vIndex++) + centerX),
	//                (vertices.get(vIndex++) + centerY),
	//                (vertices.get(vIndex) + centerZ));
	//
	//            // get triangle edge vectors and plane normal
	//            Vec4 e1 = v1.subtract3(v0);
	//            Vec4 e2 = v2.subtract3(v0);
	//            Vec4 N = e1.cross3(e2).normalize3();  // if N is 0, the triangle is degenerate
	//
	//            // Store the face's normal for each of the vertices that make up the face.
	//            // TODO: Clear up warnings here
	//            normalLists.get(indices.get(i)).add(N);
	//            normalLists.get(indices.get(i + 1)).add(N);
	//            normalLists.get(indices.get(i + 2)).add(N);
	//            //System.out.println("Normal: " + N);
	//        }
	//
	//        // Now loop through each vertex, and average out all the normals stored.
	//        int idx = 0;
	//        for (int i = 0; i < numVertices; i++)
	//        {
	//            Vec4 normal = Vec4.ZERO;
	//            // Sum
	//            for (int j = 0; j < normalLists.get(i).size(); ++j)
	//                normal = normal.add3(normalLists.get(i).get(j));
	//            // Average
	//            normal = normal.multiply3(1.0f / normalLists.get(i).size()).normalize3();
	//            // Fill normal buffer
	//            normals.put(idx++, normal.x);
	//            normals.put(idx++, normal.y);
	//            normals.put(idx++, normal.z);
	//            //System.out.println("Normal: " + normal + " - " + normalLists[i].size());
	//            //System.out.println("Normal buffer: " + normals.get(idx - 3) + ", " + normals.get(idx - 2) + ", " + normals.get(idx - 1));
	//        }
	//
	//        return normals;
	//    }
}
