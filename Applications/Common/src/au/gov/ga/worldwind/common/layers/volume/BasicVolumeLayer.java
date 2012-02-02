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

public class BasicVolumeLayer extends AbstractLayer implements VolumeLayer, Wireframeable, SelectListener
{
	private URL context;
	private String url;
	private String dataCacheName;
	private VolumeDataProvider dataProvider;
	private Double minimumDistance;
	private double maxVariance = 0;
	private CoordinateTransformation coordinateTransformation;
	private ColorMap colorMap;
	private Color noDataColor;

	private final Object dataLock = new Object();
	private boolean dataAvailable = false;
	private FastShape topSurface, bottomSurface;
	private TopBottomFastShape minLatCurtain, maxLatCurtain, minLonCurtain, maxLonCurtain;
	private TextureRenderer topTexture, bottomTexture, minLatTexture, maxLatTexture, minLonTexture, maxLonTexture;
	private int topOffset = 0, bottomOffset = 0, minLatOffset = 0, maxLatOffset = 0, minLonOffset = 0,
			maxLonOffset = 0;
	private int lastTopOffset = -1, lastBottomOffset = -1, lastMinLatOffset = -1, lastMaxLatOffset = -1,
			lastMinLonOffset = -1, lastMaxLonOffset = -1;

	private double[] curtainTextureMatrix = new double[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

	private Double minLatClip = null, maxLatClip = null, minLonClip = null, maxLonClip = null;
	private boolean minLatClipDirty = false, maxLatClipDirty = false, minLonClipDirty = false, maxLonClipDirty = false;
	private double[] clippingPlanes = new double[4 * 4];

	private boolean wireframe = false;
	private final PickSupport pickSupport = new PickSupport();

	private boolean dragging = false;
	private double dragStartPosition;
	private int dragStartSlice;
	private Vec4 dragStartCenter;

	private WorldWindow wwd;

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
			minLatOffset = i;
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MAX_U);
		if (i != null)
			maxLatOffset = i;
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_V);
		if (i != null)
			minLonOffset = i;
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MAX_V);
		if (i != null)
			maxLonOffset = i;
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

	protected void calculateSurfaces()
	{
		double topElevation = 0;
		double bottomElevation = -dataProvider.getDepth();

		minLatCurtain = dataProvider.createLatitudeCurtain(0);
		minLatCurtain.setLighted(true);
		minLatCurtain.setCalculateNormals(true);
		minLatCurtain.setTopElevationOffset(topElevation);
		minLatCurtain.setBottomElevationOffset(bottomElevation);
		minLatCurtain.setTextureMatrix(curtainTextureMatrix);

		maxLatCurtain = dataProvider.createLatitudeCurtain(dataProvider.getXSize() - 1);
		maxLatCurtain.setLighted(true);
		maxLatCurtain.setCalculateNormals(true);
		maxLatCurtain.setReverseNormals(true);
		maxLatCurtain.setTopElevationOffset(topElevation);
		maxLatCurtain.setBottomElevationOffset(bottomElevation);
		maxLatCurtain.setTextureMatrix(curtainTextureMatrix);

		minLonCurtain = dataProvider.createLongitudeCurtain(0);
		minLonCurtain.setLighted(true);
		minLonCurtain.setCalculateNormals(true);
		minLonCurtain.setReverseNormals(true);
		minLonCurtain.setTopElevationOffset(topElevation);
		minLonCurtain.setBottomElevationOffset(bottomElevation);
		minLonCurtain.setTextureMatrix(curtainTextureMatrix);

		maxLonCurtain = dataProvider.createLongitudeCurtain(dataProvider.getYSize() - 1);
		maxLonCurtain.setLighted(true);
		maxLonCurtain.setCalculateNormals(true);
		maxLonCurtain.setTopElevationOffset(topElevation);
		maxLonCurtain.setBottomElevationOffset(bottomElevation);
		maxLonCurtain.setTextureMatrix(curtainTextureMatrix);

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

		setWireframe(isWireframe());
	}

	protected void recalculateSurfaces()
	{
		if (!dataAvailable)
			return;

		if (topTexture == null)
		{
			topTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getYSize(), true, true);
			bottomTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getYSize(), true, true);
			minLatTexture = new TextureRenderer(dataProvider.getYSize(), dataProvider.getZSize(), true, true);
			maxLatTexture = new TextureRenderer(dataProvider.getYSize(), dataProvider.getZSize(), true, true);
			minLonTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getZSize(), true, true);
			maxLonTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getZSize(), true, true);
		}

		minLatOffset = Util.clamp(minLatOffset, 0, dataProvider.getXSize() - 1);
		maxLatOffset = Util.clamp(maxLatOffset, 0, dataProvider.getXSize() - 1 - minLatOffset);
		minLonOffset = Util.clamp(minLonOffset, 0, dataProvider.getYSize() - 1);
		maxLonOffset = Util.clamp(maxLonOffset, 0, dataProvider.getYSize() - 1 - minLonOffset);
		topOffset = Util.clamp(topOffset, 0, dataProvider.getZSize() - 1);
		bottomOffset = Util.clamp(bottomOffset, 0, dataProvider.getZSize() - 1 - topOffset);

		int maxLatSlice = dataProvider.getXSize() - 1 - maxLatOffset;
		int maxLonSlice = dataProvider.getYSize() - 1 - maxLonOffset;
		int bottomSlice = dataProvider.getZSize() - 1 - bottomOffset;

		boolean recalculateMinLat = lastMinLatOffset != minLatOffset;
		boolean recalculateMaxLat = lastMaxLatOffset != maxLatOffset;
		boolean recalculateMinLon = lastMinLonOffset != minLonOffset;
		boolean recalculateMaxLon = lastMaxLonOffset != maxLonOffset;
		boolean recalculateTop = lastTopOffset != topOffset;
		boolean recalculateBottom = lastBottomOffset != bottomOffset;

		Rectangle latRectangle = new Rectangle(0, 0, dataProvider.getYSize(), dataProvider.getZSize());
		Rectangle lonRectangle = new Rectangle(0, 0, dataProvider.getXSize(), dataProvider.getZSize());
		Rectangle elevationRectangle = new Rectangle(0, 0, dataProvider.getXSize(), dataProvider.getYSize());
		Sector sector = getSector();

		double topPercent = topOffset / (double) (dataProvider.getZSize() - 1);
		double bottomPercent = bottomSlice / (double) (dataProvider.getZSize() - 1);

		if (recalculateMinLat)
		{
			double longitudeOffset =
					(minLatOffset / (double) (dataProvider.getXSize() - 1)) * sector.getDeltaLonDegrees();
			minLatClip = minLatOffset == 0 ? null : sector.getMinLongitude().degrees + longitudeOffset;
			minLatClipDirty = true;

			TopBottomFastShape newMinLatCurtain = dataProvider.createLatitudeCurtain(minLatOffset);
			minLatCurtain.setPositions(newMinLatCurtain.getPositions());

			updateTexture(generateTexture(0, minLatOffset, latRectangle), minLatTexture, minLatCurtain);
			lastMinLatOffset = minLatOffset;
		}
		if (recalculateMaxLat)
		{
			double longitudeOffset =
					(maxLatOffset / (double) (dataProvider.getXSize() - 1)) * sector.getDeltaLonDegrees();
			maxLatClip = maxLatOffset == 0 ? null : sector.getMaxLongitude().degrees - longitudeOffset;
			maxLatClipDirty = true;

			TopBottomFastShape newMaxLatCurtain =
					dataProvider.createLatitudeCurtain(dataProvider.getXSize() - 1 - maxLatOffset);
			maxLatCurtain.setPositions(newMaxLatCurtain.getPositions());

			updateTexture(generateTexture(0, maxLatSlice, latRectangle), maxLatTexture, maxLatCurtain);
			lastMaxLatOffset = maxLatOffset;
		}
		if (recalculateMinLon)
		{
			double latitudeOffset =
					(minLonOffset / (double) (dataProvider.getYSize() - 1)) * sector.getDeltaLatDegrees();
			minLonClip = minLonOffset == 0 ? null : sector.getMinLatitude().degrees + latitudeOffset;
			minLonClipDirty = true;

			TopBottomFastShape newMinLonCurtain = dataProvider.createLongitudeCurtain(minLonOffset);
			minLonCurtain.setPositions(newMinLonCurtain.getPositions());

			updateTexture(generateTexture(1, minLonOffset, lonRectangle), minLonTexture, minLonCurtain);
			lastMinLonOffset = minLonOffset;
		}
		if (recalculateMaxLon)
		{
			double latitudeOffset =
					(maxLonOffset / (double) (dataProvider.getYSize() - 1)) * sector.getDeltaLatDegrees();
			maxLonClip = maxLonOffset == 0 ? null : sector.getMaxLatitude().degrees - latitudeOffset;
			maxLonClipDirty = true;

			TopBottomFastShape newMaxLonCurtain =
					dataProvider.createLongitudeCurtain(dataProvider.getYSize() - 1 - maxLonOffset);
			maxLonCurtain.setPositions(newMaxLonCurtain.getPositions());

			updateTexture(generateTexture(1, maxLonSlice, lonRectangle), maxLonTexture, maxLonCurtain);
			lastMaxLonOffset = maxLonOffset;
		}
		if (recalculateTop)
		{
			double elevation = -dataProvider.getDepth() * topPercent;

			updateTexture(generateTexture(2, topOffset, elevationRectangle), topTexture, topSurface);
			lastTopOffset = topOffset;

			topSurface.setElevation(elevation);
			minLatCurtain.setTopElevationOffset(elevation);
			maxLatCurtain.setTopElevationOffset(elevation);
			minLonCurtain.setTopElevationOffset(elevation);
			maxLonCurtain.setTopElevationOffset(elevation);

			recalculateTextureMatrix(topPercent, bottomPercent);
		}
		if (recalculateBottom)
		{
			double elevation = -dataProvider.getDepth() * bottomPercent;

			updateTexture(generateTexture(2, bottomSlice, elevationRectangle), bottomTexture, bottomSurface);
			lastBottomOffset = bottomOffset;

			bottomSurface.setElevation(elevation);
			minLatCurtain.setBottomElevationOffset(elevation);
			maxLatCurtain.setBottomElevationOffset(elevation);
			minLonCurtain.setBottomElevationOffset(elevation);
			maxLonCurtain.setBottomElevationOffset(elevation);

			recalculateTextureMatrix(topPercent, bottomPercent);
		}
	}

	protected void recalculateTextureMatrix(double topPercent, double bottomPercent)
	{
		Matrix m =
				Matrix.fromTranslation(0, topPercent, 0).multiply(Matrix.fromScale(1, bottomPercent - topPercent, 1));
		m.toArray(curtainTextureMatrix, 0, false);
	}

	protected void recalculateClipPlanes(DrawContext dc)
	{
		if (minLatClipDirty && minLatClip != null)
		{
			Plane plane = calculateClipPlane(dc, minLatClip, true, false);
			Vec4 vector = plane.getVector();
			clippingPlanes[0] = vector.x;
			clippingPlanes[1] = vector.y;
			clippingPlanes[2] = vector.z;
			clippingPlanes[3] = vector.w;
		}
		if (maxLatClipDirty && maxLatClip != null)
		{
			Plane plane = calculateClipPlane(dc, maxLatClip, true, true);
			Vec4 vector = plane.getVector();
			clippingPlanes[4] = vector.x;
			clippingPlanes[5] = vector.y;
			clippingPlanes[6] = vector.z;
			clippingPlanes[7] = vector.w;
		}
		if (minLonClipDirty && minLonClip != null)
		{
			Plane plane = calculateClipPlane(dc, minLonClip, false, false);
			Vec4 vector = plane.getVector();
			clippingPlanes[8] = vector.x;
			clippingPlanes[9] = vector.y;
			clippingPlanes[10] = vector.z;
			clippingPlanes[11] = vector.w;
		}
		if (maxLonClipDirty && maxLonClip != null)
		{
			Plane plane = calculateClipPlane(dc, maxLonClip, false, true);
			Vec4 vector = plane.getVector();
			clippingPlanes[12] = vector.x;
			clippingPlanes[13] = vector.y;
			clippingPlanes[14] = vector.z;
			clippingPlanes[15] = vector.w;
		}
		minLatClipDirty = maxLatClipDirty = minLonClipDirty = maxLonClipDirty = false;
	}

	protected Plane calculateClipPlane(DrawContext dc, double latOrLon, boolean isLongitude, boolean isReversed)
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
			recalculateSurfaces();
			recalculateClipPlanes(dc);

			FastShape[] shapes =
					new FastShape[] { topSurface, bottomSurface, minLatCurtain, maxLatCurtain, minLonCurtain,
							maxLonCurtain };
			Arrays.sort(shapes, new ShapeComparator(dc));

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
				gl.glPushAttrib(GL.GL_TRANSFORM_BIT);

				boolean[] clipPlaneEnabled =
						new boolean[] { minLatClip != null, maxLatClip != null, minLonClip != null, maxLonClip != null };
				FastShape[] clipPlaneShapes =
						new FastShape[] { minLatCurtain, maxLatCurtain, minLonCurtain, maxLonCurtain };

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

					for (FastShape shape : shapes)
					{
						if (shape != null)
						{
							int clipPlaneShapeIndex = Util.indexInArray(clipPlaneShapes, shape);

							if (clipPlaneShapeIndex >= 0)
							{
								if (clipPlaneEnabled[clipPlaneShapeIndex])
								{
									gl.glDisable(GL.GL_CLIP_PLANE0 + clipPlaneShapeIndex);
								}
							}

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

							if (clipPlaneShapeIndex >= 0)
							{
								if (clipPlaneEnabled[clipPlaneShapeIndex])
								{
									gl.glEnable(GL.GL_CLIP_PLANE0 + clipPlaneShapeIndex);
								}
							}
						}
					}

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
						renderBoundingBox(dc);
					}
				}
				finally
				{
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
				minLatCurtain.setWireframe(wireframe);
				maxLatCurtain.setWireframe(wireframe);
				minLonCurtain.setWireframe(wireframe);
				maxLonCurtain.setWireframe(wireframe);
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
		boolean minLat = pickedShape == minLatCurtain;
		boolean maxLat = pickedShape == maxLatCurtain;
		boolean minLon = pickedShape == minLonCurtain;
		boolean maxLon = pickedShape == maxLonCurtain;
		if (top || bottom || minLat || maxLat || minLon || maxLon)
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
					else if (minLat || maxLat)
					{
						dragLatitude(event.getPickPoint(), pickedShape);
					}
					else
					{
						dragLongitude(event.getPickPoint(), pickedShape);
					}
				}
				dragging = true;
				event.consume();
			}
		}
	}

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
			dragStartPosition = position.longitude.degrees;
			dragStartSlice = shape == minLatCurtain ? minLatOffset : maxLatOffset;
		}
		else
		{
			double deltaLongitude = position.longitude.degrees - dragStartPosition;
			double deltaPercentage = deltaLongitude / dataProvider.getSector().getDeltaLonDegrees();
			int sliceMovement = (int) (deltaPercentage * (dataProvider.getXSize() - 1));
			if (shape == minLatCurtain)
			{
				minLatOffset = Util.clamp(dragStartSlice + sliceMovement, 0, dataProvider.getXSize() - 1);
				maxLatOffset = Util.clamp(maxLatOffset, 0, dataProvider.getXSize() - 1 - minLatOffset);
			}
			else
			{
				maxLatOffset = Util.clamp(dragStartSlice - sliceMovement, 0, dataProvider.getXSize() - 1);
				minLatOffset = Util.clamp(minLatOffset, 0, dataProvider.getXSize() - 1 - maxLatOffset);
			}
		}
	}

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
			dragStartPosition = position.latitude.degrees;
			dragStartSlice = shape == minLonCurtain ? minLonOffset : maxLonOffset;
		}
		else
		{
			double deltaLatitude = position.latitude.degrees - dragStartPosition;
			double deltaPercentage = deltaLatitude / dataProvider.getSector().getDeltaLatDegrees();
			int sliceMovement = (int) (deltaPercentage * (dataProvider.getYSize() - 1));
			if (shape == minLonCurtain)
			{
				minLonOffset = Util.clamp(dragStartSlice + sliceMovement, 0, dataProvider.getYSize() - 1);
				maxLonOffset = Util.clamp(maxLonOffset, 0, dataProvider.getYSize() - 1 - minLonOffset);
			}
			else
			{
				maxLonOffset = Util.clamp(dragStartSlice - sliceMovement, 0, dataProvider.getYSize() - 1);
				minLonOffset = Util.clamp(minLonOffset, 0, dataProvider.getYSize() - 1 - maxLonOffset);
			}
		}
	}

	private class ShapeComparator implements Comparator<FastShape>
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
