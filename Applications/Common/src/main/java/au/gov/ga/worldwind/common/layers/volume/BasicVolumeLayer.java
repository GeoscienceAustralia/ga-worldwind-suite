/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Box;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import javax.media.opengl.GL;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.worldwind.common.layers.Wireframeable;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.ColorMap;
import au.gov.ga.worldwind.common.util.CoordinateTransformationUtil;
import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.GeometryUtil;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.Validate;

import com.sun.opengl.util.j2d.TextureRenderer;

/**
 * Basic implementation of the {@link VolumeLayer} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicVolumeLayer extends AbstractLayer implements VolumeLayer, Wireframeable, SelectListener
{
	protected URL context;
	protected String url;
	protected String dataCacheName;
	protected VolumeDataProvider dataProvider;
	protected Double minimumDistance;
	protected double maxVariance = 0;
	protected CoordinateTransformation coordinateTransformation;
	protected ColorMap colorMap;
	protected Color noDataColor;

	protected final Object dataLock = new Object();
	protected boolean dataAvailable = false;
	protected FastShape topSurface, bottomSurface;
	protected TopBottomFastShape minLonCurtain, maxLonCurtain, minLatCurtain, maxLatCurtain;
	protected TextureRenderer topTexture, bottomTexture, minLonTexture, maxLonTexture, minLatTexture, maxLatTexture;
	protected int topOffset = 0, bottomOffset = 0, minLonOffset = 0, maxLonOffset = 0, minLatOffset = 0,
			maxLatOffset = 0;
	protected int lastTopOffset = -1, lastBottomOffset = -1, lastMinLonOffset = -1, lastMaxLonOffset = -1,
			lastMinLatOffset = -1, lastMaxLatOffset = -1;

	protected final double[] curtainTextureMatrix = new double[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

	protected Double minLonClip = null, maxLonClip = null, minLatClip = null, maxLatClip = null;
	protected boolean minLonClipDirty = false, maxLonClipDirty = false, minLatClipDirty = false,
			maxLatClipDirty = false;
	protected final double[] clippingPlanes = new double[16];

	protected boolean wireframe = false;
	protected final PickSupport pickSupport = new PickSupport();

	protected boolean dragging = false;
	protected double dragStartPosition;
	protected int dragStartSlice;
	protected Vec4 dragStartCenter;

	protected WorldWindow wwd;

	/**
	 * Create a new {@link BasicVolumeLayer}, using the provided layer params.
	 * 
	 * @param params
	 */
	public BasicVolumeLayer(AVList params)
	{
		context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		url = params.getStringValue(AVKey.URL);
		dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
		dataProvider = (VolumeDataProvider) params.getValue(AVKeyMore.DATA_LAYER_PROVIDER);

		minimumDistance = (Double) params.getValue(AVKeyMore.MINIMUM_DISTANCE);
		colorMap = (ColorMap) params.getValue(AVKeyMore.COLOR_MAP);
		noDataColor = (Color) params.getValue(AVKeyMore.NO_DATA_COLOR);

		Double d = (Double) params.getValue(AVKeyMore.MAX_VARIANCE);
		if (d != null)
			maxVariance = d;

		String s = (String) params.getValue(AVKey.COORDINATE_SYSTEM);
		if (s != null)
			coordinateTransformation = CoordinateTransformationUtil.getTransformationToWGS84(s);

		Integer i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_U);
		if (i != null)
			minLonOffset = i;
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MAX_U);
		if (i != null)
			maxLonOffset = i;
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_V);
		if (i != null)
			minLatOffset = i;
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MAX_V);
		if (i != null)
			maxLatOffset = i;
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_W);
		if (i != null)
			topOffset = i;
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MAX_W);
		if (i != null)
			bottomOffset = i;

		Validate.notBlank(url, "Model data url not set");
		Validate.notBlank(dataCacheName, "Model data cache name not set");
		Validate.notNull(dataProvider, "Model data provider is null");
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return new URL(context, url);
	}

	@Override
	public String getDataCacheName()
	{
		return dataCacheName;
	}

	@Override
	public boolean isLoading()
	{
		return dataProvider.isLoading();
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		dataProvider.addLoadingListener(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		dataProvider.removeLoadingListener(listener);
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		this.wwd = wwd;
		wwd.addSelectListener(this);
	}

	@Override
	public Sector getSector()
	{
		return dataProvider.getSector();
	}

	@Override
	public void dataAvailable(VolumeDataProvider provider)
	{
		calculateSurfaces();
		dataAvailable = true;
	}

	/**
	 * Calculate the 4 curtain and 2 horizontal surfaces used to render this
	 * volume. Should be called once after the {@link VolumeDataProvider}
	 * notifies this layer that the data is available.
	 */
	protected void calculateSurfaces()
	{
		double topElevation = 0;
		double bottomElevation = -dataProvider.getDepth();

		minLonCurtain = dataProvider.createLongitudeCurtain(0);
		minLonCurtain.setLighted(true);
		minLonCurtain.setCalculateNormals(true);
		minLonCurtain.setTopElevationOffset(topElevation);
		minLonCurtain.setBottomElevationOffset(bottomElevation);
		minLonCurtain.setTextureMatrix(curtainTextureMatrix);

		maxLonCurtain = dataProvider.createLongitudeCurtain(dataProvider.getXSize() - 1);
		maxLonCurtain.setLighted(true);
		maxLonCurtain.setCalculateNormals(true);
		maxLonCurtain.setReverseNormals(true);
		maxLonCurtain.setTopElevationOffset(topElevation);
		maxLonCurtain.setBottomElevationOffset(bottomElevation);
		maxLonCurtain.setTextureMatrix(curtainTextureMatrix);

		minLatCurtain = dataProvider.createLatitudeCurtain(0);
		minLatCurtain.setLighted(true);
		minLatCurtain.setCalculateNormals(true);
		minLatCurtain.setReverseNormals(true);
		minLatCurtain.setTopElevationOffset(topElevation);
		minLatCurtain.setBottomElevationOffset(bottomElevation);
		minLatCurtain.setTextureMatrix(curtainTextureMatrix);

		maxLatCurtain = dataProvider.createLatitudeCurtain(dataProvider.getYSize() - 1);
		maxLatCurtain.setLighted(true);
		maxLatCurtain.setCalculateNormals(true);
		maxLatCurtain.setTopElevationOffset(topElevation);
		maxLatCurtain.setBottomElevationOffset(bottomElevation);
		maxLatCurtain.setTextureMatrix(curtainTextureMatrix);

		Rectangle rectangle = new Rectangle(0, 0, dataProvider.getXSize(), dataProvider.getYSize());
		topSurface = dataProvider.createHorizontalSurface((float) maxVariance, rectangle);
		topSurface.setLighted(true);
		topSurface.setCalculateNormals(true);
		topSurface.setElevation(topElevation);

		bottomSurface = dataProvider.createHorizontalSurface((float) maxVariance, rectangle);
		bottomSurface.setLighted(true);
		bottomSurface.setCalculateNormals(true);
		bottomSurface.setReverseNormals(true);
		bottomSurface.setElevation(bottomElevation);

		//create the textures
		topTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getYSize(), true, true);
		bottomTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getYSize(), true, true);
		minLonTexture = new TextureRenderer(dataProvider.getYSize(), dataProvider.getZSize(), true, true);
		maxLonTexture = new TextureRenderer(dataProvider.getYSize(), dataProvider.getZSize(), true, true);
		minLatTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getZSize(), true, true);
		maxLatTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getZSize(), true, true);

		//update each shape's wireframe property so they match the layer's
		setWireframe(isWireframe());
	}

	/**
	 * Recalculate any surfaces that require recalculation. This includes
	 * regenerating textures when the user has dragged a surface to a different
	 * slice.
	 */
	protected void recalculateSurfaces()
	{
		if (!dataAvailable)
			return;

		//ensure the min/max offsets don't overlap one-another
		minLonOffset = Util.clamp(minLonOffset, 0, dataProvider.getXSize() - 1);
		maxLonOffset = Util.clamp(maxLonOffset, 0, dataProvider.getXSize() - 1 - minLonOffset);
		minLatOffset = Util.clamp(minLatOffset, 0, dataProvider.getYSize() - 1);
		maxLatOffset = Util.clamp(maxLatOffset, 0, dataProvider.getYSize() - 1 - minLatOffset);
		topOffset = Util.clamp(topOffset, 0, dataProvider.getZSize() - 1);
		bottomOffset = Util.clamp(bottomOffset, 0, dataProvider.getZSize() - 1 - topOffset);

		int maxLonSlice = dataProvider.getXSize() - 1 - maxLonOffset;
		int maxLatSlice = dataProvider.getYSize() - 1 - maxLatOffset;
		int bottomSlice = dataProvider.getZSize() - 1 - bottomOffset;

		//only recalculate those that have changed
		boolean recalculateMinLon = lastMinLonOffset != minLonOffset;
		boolean recalculateMaxLon = lastMaxLonOffset != maxLonOffset;
		boolean recalculateMinLat = lastMinLatOffset != minLatOffset;
		boolean recalculateMaxLat = lastMaxLatOffset != maxLatOffset;
		boolean recalculateTop = lastTopOffset != topOffset;
		boolean recalculateBottom = lastBottomOffset != bottomOffset;

		Rectangle lonRectangle = new Rectangle(0, 0, dataProvider.getYSize(), dataProvider.getZSize());
		Rectangle latRectangle = new Rectangle(0, 0, dataProvider.getXSize(), dataProvider.getZSize());
		Rectangle elevationRectangle = new Rectangle(0, 0, dataProvider.getXSize(), dataProvider.getYSize());
		Sector sector = getSector();
		double topPercent = topOffset / (double) (dataProvider.getZSize() - 1);
		double bottomPercent = bottomSlice / (double) (dataProvider.getZSize() - 1);

		if (recalculateMinLon)
		{
			double longitudeOffset =
					(minLonOffset / (double) (dataProvider.getXSize() - 1)) * sector.getDeltaLonDegrees();
			minLonClip = minLonOffset == 0 ? null : sector.getMinLongitude().degrees + longitudeOffset;
			minLonClipDirty = true;

			TopBottomFastShape newMinLonCurtain = dataProvider.createLongitudeCurtain(minLonOffset);
			minLonCurtain.setPositions(newMinLonCurtain.getPositions());

			updateTexture(generateTexture(0, minLonOffset, lonRectangle), minLonTexture, minLonCurtain);
			lastMinLonOffset = minLonOffset;
		}
		if (recalculateMaxLon)
		{
			double longitudeOffset =
					(maxLonOffset / (double) (dataProvider.getXSize() - 1)) * sector.getDeltaLonDegrees();
			maxLonClip = maxLonOffset == 0 ? null : sector.getMaxLongitude().degrees - longitudeOffset;
			maxLonClipDirty = true;

			TopBottomFastShape newMaxLonCurtain =
					dataProvider.createLongitudeCurtain(dataProvider.getXSize() - 1 - maxLonOffset);
			maxLonCurtain.setPositions(newMaxLonCurtain.getPositions());

			updateTexture(generateTexture(0, maxLonSlice, lonRectangle), maxLonTexture, maxLonCurtain);
			lastMaxLonOffset = maxLonOffset;
		}
		if (recalculateMinLat)
		{
			double latitudeOffset =
					(minLatOffset / (double) (dataProvider.getYSize() - 1)) * sector.getDeltaLatDegrees();
			minLatClip = minLatOffset == 0 ? null : sector.getMinLatitude().degrees + latitudeOffset;
			minLatClipDirty = true;

			TopBottomFastShape newMinLatCurtain = dataProvider.createLatitudeCurtain(minLatOffset);
			minLatCurtain.setPositions(newMinLatCurtain.getPositions());

			updateTexture(generateTexture(1, minLatOffset, latRectangle), minLatTexture, minLatCurtain);
			lastMinLatOffset = minLatOffset;
		}
		if (recalculateMaxLat)
		{
			double latitudeOffset =
					(maxLatOffset / (double) (dataProvider.getYSize() - 1)) * sector.getDeltaLatDegrees();
			maxLatClip = maxLatOffset == 0 ? null : sector.getMaxLatitude().degrees - latitudeOffset;
			maxLatClipDirty = true;

			TopBottomFastShape newMaxLatCurtain =
					dataProvider.createLatitudeCurtain(dataProvider.getYSize() - 1 - maxLatOffset);
			maxLatCurtain.setPositions(newMaxLatCurtain.getPositions());

			updateTexture(generateTexture(1, maxLatSlice, latRectangle), maxLatTexture, maxLatCurtain);
			lastMaxLatOffset = maxLatOffset;
		}
		if (recalculateTop)
		{
			double elevation = -dataProvider.getDepth() * topPercent;

			updateTexture(generateTexture(2, topOffset, elevationRectangle), topTexture, topSurface);
			lastTopOffset = topOffset;

			topSurface.setElevation(elevation);
			minLonCurtain.setTopElevationOffset(elevation);
			maxLonCurtain.setTopElevationOffset(elevation);
			minLatCurtain.setTopElevationOffset(elevation);
			maxLatCurtain.setTopElevationOffset(elevation);

			recalculateTextureMatrix(topPercent, bottomPercent);
		}
		if (recalculateBottom)
		{
			double elevation = -dataProvider.getDepth() * bottomPercent;

			updateTexture(generateTexture(2, bottomSlice, elevationRectangle), bottomTexture, bottomSurface);
			lastBottomOffset = bottomOffset;

			bottomSurface.setElevation(elevation);
			minLonCurtain.setBottomElevationOffset(elevation);
			maxLonCurtain.setBottomElevationOffset(elevation);
			minLatCurtain.setBottomElevationOffset(elevation);
			maxLatCurtain.setBottomElevationOffset(elevation);

			recalculateTextureMatrix(topPercent, bottomPercent);
		}
	}

	/**
	 * Recalculate the curtain texture matrix. When the top and bottom surface
	 * offsets aren't 0, the OpenGL texture matrix is used to offset the curtain
	 * textures.
	 * 
	 * @param topPercent
	 *            Location of the top surface (normalized to 0..1)
	 * @param bottomPercent
	 *            Location of the bottom surface (normalized to 0..1)
	 */
	protected void recalculateTextureMatrix(double topPercent, double bottomPercent)
	{
		Matrix m =
				Matrix.fromTranslation(0, topPercent, 0).multiply(Matrix.fromScale(1, bottomPercent - topPercent, 1));
		m.toArray(curtainTextureMatrix, 0, false);
	}

	/**
	 * Recalculate the clipping planes used to clip the surfaces when the user
	 * drags them.
	 * 
	 * @param dc
	 */
	protected void recalculateClippingPlanes(DrawContext dc)
	{
		if (!dataAvailable)
			return;

		if (minLonClipDirty && minLonClip != null)
		{
			Plane plane = calculateClippingPlane(dc, minLonClip, true, false);
			Vec4 vector = plane.getVector();
			clippingPlanes[0] = vector.x;
			clippingPlanes[1] = vector.y;
			clippingPlanes[2] = vector.z;
			clippingPlanes[3] = vector.w;
		}
		if (maxLonClipDirty && maxLonClip != null)
		{
			Plane plane = calculateClippingPlane(dc, maxLonClip, true, true);
			Vec4 vector = plane.getVector();
			clippingPlanes[4] = vector.x;
			clippingPlanes[5] = vector.y;
			clippingPlanes[6] = vector.z;
			clippingPlanes[7] = vector.w;
		}
		if (minLatClipDirty && minLatClip != null)
		{
			Plane plane = calculateClippingPlane(dc, minLatClip, false, false);
			Vec4 vector = plane.getVector();
			clippingPlanes[8] = vector.x;
			clippingPlanes[9] = vector.y;
			clippingPlanes[10] = vector.z;
			clippingPlanes[11] = vector.w;
		}
		if (maxLatClipDirty && maxLatClip != null)
		{
			Plane plane = calculateClippingPlane(dc, maxLatClip, false, true);
			Vec4 vector = plane.getVector();
			clippingPlanes[12] = vector.x;
			clippingPlanes[13] = vector.y;
			clippingPlanes[14] = vector.z;
			clippingPlanes[15] = vector.w;
		}
		minLonClipDirty = maxLonClipDirty = minLatClipDirty = maxLatClipDirty = false;
	}

	/**
	 * Calculate a single clipping plane at the provided latitude or longitude.
	 * 
	 * @param dc
	 * @param latOrLon
	 *            Latitude or longitude line through which the plane should
	 *            intersect
	 * @param isLongitude
	 *            Does latOrLon define a latitude or a longitude?
	 * @param isReversed
	 *            Should the plane's normal be reversed?
	 * @return Plane parallel to and intersecting the given latitude or
	 *         longitude
	 */
	protected Plane calculateClippingPlane(DrawContext dc, double latOrLon, boolean isLongitude, boolean isReversed)
	{
		Globe globe = dc.getGlobe();
		Sector sector = getSector();
		LatLon centroid = sector.getCentroid();
		Position p1 = null, p2 = null, p3 = null;

		if (isLongitude)
		{
			Angle longitude = Angle.fromDegrees(latOrLon);
			p1 = new Position(sector.getMinLatitude(), longitude, 0);
			p2 = new Position(centroid.latitude, longitude, 0);
			p3 = new Position(centroid.latitude, longitude, 100);
		}
		else
		{
			Angle latitude = Angle.fromDegrees(latOrLon);
			p1 = new Position(latitude, centroid.longitude, 100);
			p2 = new Position(latitude, centroid.longitude, 0);
			p3 = new Position(latitude, sector.getMinLongitude(), 0);
		}

		Vec4 v1 = globe.computePointFromPosition(isReversed ? p3 : p1);
		Vec4 v2 = globe.computePointFromPosition(p2);
		Vec4 v3 = globe.computePointFromPosition(isReversed ? p1 : p3);
		Line l1 = Line.fromSegment(v1, v2);
		Line l2 = Line.fromSegment(v2, v3);
		return GeometryUtil.createPlaneContainingLines(l1, l2);
	}

	/**
	 * Generate a texture slice through the volume at the given position. Uses a
	 * {@link ColorMap} to map values to colors (or simply interpolates the hue
	 * if no colormap is provided - assumes values between 0 and 1).
	 * 
	 * @param axis
	 *            Slicing axis (0 for a longitude slice, 1 for a latitude slice,
	 *            2 for an elevation slice).
	 * @param position
	 *            Longitude, latitude, or elevation at which to slice.
	 * @param rectangle
	 *            Sub-rectangle within the volume slice to get texture data for.
	 * @return A {@link BufferedImage} containing a representation of the volume
	 *         slice.
	 */
	protected BufferedImage generateTexture(int axis, int position, Rectangle rectangle)
	{
		BufferedImage image = new BufferedImage(rectangle.width, rectangle.height, BufferedImage.TYPE_INT_ARGB);
		for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++)
		{
			for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++)
			{
				int vx = axis == 2 ? x : axis == 1 ? x : position;
				int vy = axis == 2 ? y : axis == 1 ? position : x;
				int vz = axis == 2 ? position : y;
				float value = dataProvider.getValue(vx, vy, vz);
				int rgb = noDataColor != null ? noDataColor.getRGB() : 0;
				if (value != dataProvider.getNoDataValue())
				{
					if (colorMap != null)
						rgb = colorMap.calculateColor(value).getRGB();
					else
						rgb = Color.HSBtoRGB(-0.3f - value * 0.7f, 1.0f, 1.0f);
				}
				image.setRGB(x - rectangle.x, y - rectangle.y, rgb);
			}
		}
		return image;
	}

	/**
	 * Update the given {@link TextureRenderer} with the provided image, and
	 * sets the {@link FastShape}'s texture it.
	 * 
	 * @param image
	 *            Image to update texture with
	 * @param texture
	 *            Texture to update
	 * @param shape
	 *            Shape to set texture in
	 */
	protected void updateTexture(BufferedImage image, TextureRenderer texture, FastShape shape)
	{
		Graphics2D g = null;
		try
		{
			g = (Graphics2D) texture.getImage().getGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			g.drawImage(image, 0, 0, null);
		}
		finally
		{
			if (g != null)
			{
				g.dispose();
			}
		}
		texture.markDirty(0, 0, texture.getWidth(), texture.getHeight());
		shape.setTexture(texture.getTexture());
	}

	@Override
	public CoordinateTransformation getCoordinateTransformation()
	{
		return coordinateTransformation;
	}

	@Override
	protected void doPick(DrawContext dc, Point point)
	{
		doRender(dc);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!isEnabled())
		{
			return;
		}

		dataProvider.requestData(this);

		synchronized (dataLock)
		{
			//recalculate surfaces and clipping planes each frame (in case user drags one of the surfaces)
			recalculateSurfaces();
			recalculateClippingPlanes(dc);

			//sort the shapes from back-to-front
			FastShape[] shapes =
					new FastShape[] { topSurface, bottomSurface, minLonCurtain, maxLonCurtain, minLatCurtain,
							maxLatCurtain };
			Arrays.sort(shapes, new ShapeComparator(dc));

			//test all the shapes with the minimum distance, culling them if they are outside
			if (minimumDistance != null)
			{
				for (int i = 0; i < shapes.length; i++)
				{
					if (shapes[i] != null)
					{
						Extent extent = shapes[i].getExtent();
						if (extent != null)
						{
							double distanceToEye =
									extent.getCenter().distanceTo3(dc.getView().getEyePoint()) - extent.getRadius();
							if (distanceToEye > minimumDistance)
							{
								shapes[i] = null;
							}
						}
					}
				}
			}

			GL gl = dc.getGL();
			try
			{
				//push the OpenGL clipping plane state on the attribute stack
				gl.glPushAttrib(GL.GL_TRANSFORM_BIT);

				//update and enable the OpenGL clipping planes for each of the curtain clipping positions 
				boolean[] clipPlaneEnabled =
						new boolean[] { minLonClip != null, maxLonClip != null, minLatClip != null, maxLatClip != null };
				FastShape[] clipPlaneShapes =
						new FastShape[] { minLonCurtain, maxLonCurtain, minLatCurtain, maxLatCurtain };
				for (int i = 0; i < 4; i++)
				{
					gl.glClipPlane(GL.GL_CLIP_PLANE0 + i, clippingPlanes, i * 4);
					if (clipPlaneEnabled[i])
					{
						gl.glEnable(GL.GL_CLIP_PLANE0 + i);
					}
					else
					{
						gl.glDisable(GL.GL_CLIP_PLANE0 + i);
					}
				}

				boolean oldDeepPicking = dc.isDeepPickingEnabled();
				try
				{
					//deep picking needs to be enabled, because the shapes could be below the surface
					if (dc.isPickingMode())
					{
						dc.setDeepPickingEnabled(true);
						pickSupport.beginPicking(dc);
					}

					//draw each shape
					for (FastShape shape : shapes)
					{
						if (shape != null)
						{
							//don't clip this shape with this shape's clipping plane
							int clipPlaneShapeIndex = Util.indexInArray(clipPlaneShapes, shape);
							if (clipPlaneShapeIndex >= 0 && clipPlaneEnabled[clipPlaneShapeIndex])
							{
								gl.glDisable(GL.GL_CLIP_PLANE0 + clipPlaneShapeIndex);
							}

							//if in picking mode, render the shape with a unique picking color, and don't light or texture
							if (dc.isPickingMode())
							{
								Color color = dc.getUniquePickColor();
								pickSupport.addPickableObject(color.getRGB(), shape);
								shape.setColor(color);
							}
							else
							{
								shape.setColor(Color.white);
							}
							shape.setLighted(!dc.isPickingMode());
							shape.setTextured(!dc.isPickingMode());
							shape.render(dc);

							//renable the clipping plane disabled earlier
							if (clipPlaneShapeIndex >= 0 && clipPlaneEnabled[clipPlaneShapeIndex])
							{
								gl.glEnable(GL.GL_CLIP_PLANE0 + clipPlaneShapeIndex);
							}
						}
					}

					//disable all clipping planes enabled earlier
					for (int i = 0; i < 4; i++)
					{
						if (clipPlaneEnabled[i])
						{
							gl.glDisable(GL.GL_CLIP_PLANE0 + i);
						}
					}

					if (dc.isPickingMode())
					{
						pickSupport.resolvePick(dc, dc.getPickPoint(), this);
					}
					else if (dragging)
					{
						//render a bounding box around the data if the user is dragging a surface
						renderBoundingBox(dc);
					}
				}
				finally
				{
					//reset the deep picking flag
					if (dc.isPickingMode())
					{
						pickSupport.endPicking(dc);
						dc.setDeepPickingEnabled(oldDeepPicking);
					}
				}
			}
			finally
			{
				gl.glPopAttrib();
			}
		}
	}

	/**
	 * Render a bounding box around the data. Used when dragging surfaces, so
	 * user has an idea of where the data extents lie when slicing.
	 * 
	 * @param dc
	 */
	protected void renderBoundingBox(DrawContext dc)
	{
		Sector sector = dataProvider.getSector();
		Position center = new Position(sector.getCentroid(), dataProvider.getTop() - dataProvider.getDepth() / 2);
		Vec4 v1 = dc.getGlobe().computePointFromPosition(sector.getMinLatitude(), center.longitude, center.elevation);
		Vec4 v2 = dc.getGlobe().computePointFromPosition(sector.getMaxLatitude(), center.longitude, center.elevation);
		double distance = v1.distanceTo3(v2) / 2;
		LatLon latlon1 = new LatLon(center.latitude, sector.getMinLongitude());
		LatLon latlon2 = new LatLon(center.latitude, sector.getMaxLongitude());
		Box box = new Box(latlon1, latlon2, distance, distance);
		box.setAltitudes(dataProvider.getTop() - dataProvider.getDepth(), dataProvider.getTop());
		box.getAttributes().setDrawInterior(false);
		box.getAttributes().setDrawOutline(true);
		box.getAttributes().setOutlineMaterial(Material.WHITE);
		box.getAttributes().setOutlineWidth(2.0);
		box.render(dc);
	}

	@Override
	public boolean isWireframe()
	{
		return wireframe;
	}

	@Override
	public void setWireframe(boolean wireframe)
	{
		this.wireframe = wireframe;
		synchronized (dataLock)
		{
			if (topSurface != null)
			{
				topSurface.setWireframe(wireframe);
				bottomSurface.setWireframe(wireframe);
				minLonCurtain.setWireframe(wireframe);
				maxLonCurtain.setWireframe(wireframe);
				minLatCurtain.setWireframe(wireframe);
				maxLatCurtain.setWireframe(wireframe);
			}
		}
	}

	@Override
	public void selected(SelectEvent event)
	{
		//we only care about drag events
		boolean drag = event.getEventAction().equals(SelectEvent.DRAG);
		boolean dragEnd = event.getEventAction().equals(SelectEvent.DRAG_END);
		if (!(drag || dragEnd))
		{
			return;
		}

		Object topObject = event.getTopObject();
		FastShape pickedShape = topObject instanceof FastShape ? (FastShape) topObject : null;
		if (pickedShape == null)
		{
			return;
		}

		boolean top = pickedShape == topSurface;
		boolean bottom = pickedShape == bottomSurface;
		boolean minLon = pickedShape == minLonCurtain;
		boolean maxLon = pickedShape == maxLonCurtain;
		boolean minLat = pickedShape == minLatCurtain;
		boolean maxLat = pickedShape == maxLatCurtain;
		if (top || bottom || minLon || maxLon || minLat || maxLat)
		{
			if (dragEnd)
			{
				dragging = false;
				event.consume();
			}
			else if (drag)
			{
				if (!dragging || dragStartCenter == null)
				{
					Extent extent = pickedShape.getExtent();
					if (extent != null)
					{
						dragStartCenter = extent.getCenter();
					}
				}

				if (dragStartCenter != null)
				{
					if (top || bottom)
					{
						dragElevation(event.getPickPoint(), pickedShape);
					}
					else if (minLon || maxLon)
					{
						dragLongitude(event.getPickPoint(), pickedShape);
					}
					else
					{
						dragLatitude(event.getPickPoint(), pickedShape);
					}
				}
				dragging = true;
				event.consume();
			}
		}
	}

	/**
	 * Drag an elevation surface up and down.
	 * 
	 * @param pickPoint
	 *            Point at which the user is dragging the mouse.
	 * @param shape
	 *            Shape to drag
	 */
	protected void dragElevation(Point pickPoint, FastShape shape)
	{
		// Calculate the plane projected from screen y=pickPoint.y
		Line screenLeftRay = wwd.getView().computeRayFromScreenPoint(pickPoint.x - 100, pickPoint.y);
		Line screenRightRay = wwd.getView().computeRayFromScreenPoint(pickPoint.x + 100, pickPoint.y);

		// As the two lines are very close to parallel, use an arbitrary line joining them rather than the two lines to avoid precision problems
		Line joiner = Line.fromSegment(screenLeftRay.getPointAt(500), screenRightRay.getPointAt(500));
		Plane screenPlane = GeometryUtil.createPlaneContainingLines(screenLeftRay, joiner);
		if (screenPlane == null)
		{
			return;
		}

		// Calculate the origin-marker ray
		Globe globe = wwd.getModel().getGlobe();
		Line centreRay = Line.fromSegment(globe.getCenter(), dragStartCenter);
		Vec4 intersection = screenPlane.intersect(centreRay);
		if (intersection == null)
		{
			return;
		}

		Position intersectionPosition = globe.computePositionFromPoint(intersection);
		if (!dragging)
		{
			dragStartPosition = intersectionPosition.elevation;
			dragStartSlice = shape == topSurface ? topOffset : bottomOffset;
		}
		else
		{
			double deltaElevation =
					(dragStartPosition - intersectionPosition.elevation)
							/ (wwd.getSceneController().getVerticalExaggeration());
			double deltaPercentage = deltaElevation / dataProvider.getDepth();
			int sliceMovement = (int) (deltaPercentage * (dataProvider.getZSize() - 1));
			if (shape == topSurface)
			{
				topOffset = Util.clamp(dragStartSlice + sliceMovement, 0, dataProvider.getZSize() - 1);
				bottomOffset = Util.clamp(bottomOffset, 0, dataProvider.getZSize() - 1 - topOffset);
			}
			else
			{
				bottomOffset = Util.clamp(dragStartSlice - sliceMovement, 0, dataProvider.getZSize() - 1);
				topOffset = Util.clamp(topOffset, 0, dataProvider.getZSize() - 1 - bottomOffset);
			}
		}
	}

	/**
	 * Drag a longitude curtain left and right.
	 * 
	 * @param pickPoint
	 *            Point at which the user is dragging the mouse.
	 * @param shape
	 *            Shape to drag
	 */
	protected void dragLongitude(Point pickPoint, FastShape shape)
	{
		Globe globe = wwd.getView().getGlobe();
		double centerElevation = globe.computePositionFromPoint(dragStartCenter).elevation;

		// Compute the ray from the screen point
		Line ray = wwd.getView().computeRayFromScreenPoint(pickPoint.x, pickPoint.y);
		Intersection[] intersections = globe.intersect(ray, centerElevation);
		if (intersections == null || intersections.length == 0)
		{
			return;
		}
		Vec4 intersection = ray.nearestIntersectionPoint(intersections);
		if (intersection == null)
		{
			return;
		}

		Position position = globe.computePositionFromPoint(intersection);
		if (!dragging)
		{
			dragStartPosition = position.longitude.degrees;
			dragStartSlice = shape == minLonCurtain ? minLonOffset : maxLonOffset;
		}
		else
		{
			double deltaLongitude = position.longitude.degrees - dragStartPosition;
			double deltaPercentage = deltaLongitude / dataProvider.getSector().getDeltaLonDegrees();
			int sliceMovement = (int) (deltaPercentage * (dataProvider.getXSize() - 1));
			if (shape == minLonCurtain)
			{
				minLonOffset = Util.clamp(dragStartSlice + sliceMovement, 0, dataProvider.getXSize() - 1);
				maxLonOffset = Util.clamp(maxLonOffset, 0, dataProvider.getXSize() - 1 - minLonOffset);
			}
			else
			{
				maxLonOffset = Util.clamp(dragStartSlice - sliceMovement, 0, dataProvider.getXSize() - 1);
				minLonOffset = Util.clamp(minLonOffset, 0, dataProvider.getXSize() - 1 - maxLonOffset);
			}
		}
	}

	/**
	 * Drag a latitude curtain left and right.
	 * 
	 * @param pickPoint
	 *            Point at which the user is dragging the mouse.
	 * @param shape
	 *            Shape to drag
	 */
	protected void dragLatitude(Point pickPoint, FastShape shape)
	{
		Globe globe = wwd.getView().getGlobe();
		double centerElevation = globe.computePositionFromPoint(dragStartCenter).elevation;

		// Compute the ray from the screen point
		Line ray = wwd.getView().computeRayFromScreenPoint(pickPoint.x, pickPoint.y);
		Intersection[] intersections = globe.intersect(ray, centerElevation);
		if (intersections == null || intersections.length == 0)
		{
			return;
		}
		Vec4 intersection = ray.nearestIntersectionPoint(intersections);
		if (intersection == null)
		{
			return;
		}

		Position position = globe.computePositionFromPoint(intersection);
		if (!dragging)
		{
			dragStartPosition = position.latitude.degrees;
			dragStartSlice = shape == minLatCurtain ? minLatOffset : maxLatOffset;
		}
		else
		{
			double deltaLatitude = position.latitude.degrees - dragStartPosition;
			double deltaPercentage = deltaLatitude / dataProvider.getSector().getDeltaLatDegrees();
			int sliceMovement = (int) (deltaPercentage * (dataProvider.getYSize() - 1));
			if (shape == minLatCurtain)
			{
				minLatOffset = Util.clamp(dragStartSlice + sliceMovement, 0, dataProvider.getYSize() - 1);
				maxLatOffset = Util.clamp(maxLatOffset, 0, dataProvider.getYSize() - 1 - minLatOffset);
			}
			else
			{
				maxLatOffset = Util.clamp(dragStartSlice - sliceMovement, 0, dataProvider.getYSize() - 1);
				minLatOffset = Util.clamp(minLatOffset, 0, dataProvider.getYSize() - 1 - maxLatOffset);
			}
		}
	}

	/**
	 * {@link Comparator} used to sort {@link FastShape}s from back-to-front
	 * (from the view eye point).
	 */
	protected class ShapeComparator implements Comparator<FastShape>
	{
		private final DrawContext dc;

		public ShapeComparator(DrawContext dc)
		{
			this.dc = dc;
		}

		@Override
		public int compare(FastShape o1, FastShape o2)
		{
			if (o1 == o2)
				return 0;
			if (o2 == null)
				return -1;
			if (o1 == null)
				return 1;

			Extent e1 = o1.getExtent();
			Extent e2 = o2.getExtent();
			if (e2 == null)
				return -1;
			if (e1 == null)
				return 1;

			Vec4 eyePoint = dc.getView().getEyePoint();
			double d1 = e1.getCenter().distanceToSquared3(eyePoint);
			double d2 = e2.getCenter().distanceToSquared3(eyePoint);
			return -Double.compare(d1, d2);
		}
	}
}
