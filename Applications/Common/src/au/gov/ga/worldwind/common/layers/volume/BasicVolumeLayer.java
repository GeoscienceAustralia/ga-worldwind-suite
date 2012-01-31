package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
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
	private final PickableObject minLatPO = new PickableObject(), maxLatPO = new PickableObject(),
			minLonPO = new PickableObject(), maxLonPO = new PickableObject(), topPO = new PickableObject(),
			bottomPO = new PickableObject();
	private int topOffset = 0, bottomOffset = 0, minLatOffset = 0, maxLatOffset = 0, minLonOffset = 0,
			maxLonOffset = 0;
	private int lastTopOffset = -1, lastBottomOffset = -1, lastMinLatOffset = -1, lastMaxLatOffset = -1,
			lastMinLonOffset = -1, lastMaxLonOffset = -1;

	private boolean wireframe = false;
	private final PickSupport pickSupport = new PickSupport();
	private FastShape pickedShape;

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
		dataAvailable = true;
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

		boolean recalculateMinLatTexture = lastMinLatOffset != minLatOffset;
		boolean recalculateMaxLatTexture = lastMaxLatOffset != maxLatOffset;
		boolean recalculateMinLonTexture = lastMinLonOffset != minLonOffset;
		boolean recalculateMaxLonTexture = lastMaxLonOffset != maxLonOffset;
		boolean recalculateTopTexture = lastTopOffset != topOffset;
		boolean recalculateBottomTexture = lastBottomOffset != bottomOffset;

		boolean recalculateEitherElevation = recalculateTopTexture || recalculateBottomTexture;
		boolean recalculateEitherLat = recalculateMinLatTexture || recalculateMaxLatTexture;
		boolean recalculateEitherLon = recalculateMinLonTexture || recalculateMaxLonTexture;
		boolean recalculateMinLat = recalculateMinLatTexture || recalculateEitherLon || recalculateEitherElevation;
		boolean recalculateMaxLat = recalculateMaxLatTexture || recalculateEitherLon || recalculateEitherElevation;
		boolean recalculateMinLon = recalculateMinLonTexture || recalculateEitherLat || recalculateEitherElevation;
		boolean recalculateMaxLon = recalculateMaxLonTexture || recalculateEitherLat || recalculateEitherElevation;
		boolean recalculateTop = recalculateEitherLat || recalculateEitherLon;
		boolean recalculateBottom = recalculateTop;
		boolean recalculateTopElevation = recalculateTopTexture;
		boolean recalculateBottomElevation = recalculateBottomTexture;

		TopBottomFastShape minLatCurtain = this.minLatCurtain, maxLatCurtain = this.maxLatCurtain, minLonCurtain =
				this.minLonCurtain, maxLonCurtain = this.maxLonCurtain;
		FastShape bottomSurface = this.bottomSurface, topSurface = this.topSurface;

		double topElevation = -dataProvider.getDepth() * (topOffset / (double) (dataProvider.getZSize() - 1));
		double bottomElevation = -dataProvider.getDepth() * (bottomSlice / (double) (dataProvider.getZSize() - 1));

		if (recalculateMinLat)
		{
			minLatCurtain =
					dataProvider.createLatitudeCurtain(minLatOffset, minLonOffset, maxLonSlice, topOffset, bottomSlice);
			minLatCurtain.setLighted(true);
			minLatCurtain.setCalculateNormals(true);
			minLatCurtain.setTopElevationOffset(topElevation);
			minLatCurtain.setBottomElevationOffset(bottomElevation);
			minLatCurtain.setTexture(minLatTexture.getTexture());
		}
		if (recalculateMaxLat)
		{
			maxLatCurtain =
					dataProvider.createLatitudeCurtain(maxLatSlice, minLonOffset, maxLonSlice, topOffset, bottomSlice);
			maxLatCurtain.setLighted(true);
			maxLatCurtain.setCalculateNormals(true);
			maxLatCurtain.setReverseNormals(true);
			maxLatCurtain.setTopElevationOffset(topElevation);
			maxLatCurtain.setBottomElevationOffset(bottomElevation);
			maxLatCurtain.setTexture(maxLatTexture.getTexture());
		}
		if (recalculateMinLon)
		{
			minLonCurtain =
					dataProvider
							.createLongitudeCurtain(minLonOffset, minLatOffset, maxLatSlice, topOffset, bottomSlice);
			minLonCurtain.setLighted(true);
			minLonCurtain.setCalculateNormals(true);
			minLonCurtain.setReverseNormals(true);
			minLonCurtain.setTopElevationOffset(topElevation);
			minLonCurtain.setBottomElevationOffset(bottomElevation);
			minLonCurtain.setTexture(minLonTexture.getTexture());
		}
		if (recalculateMaxLon)
		{
			maxLonCurtain =
					dataProvider.createLongitudeCurtain(maxLonSlice, minLatOffset, maxLatSlice, topOffset, bottomSlice);
			maxLonCurtain.setLighted(true);
			maxLonCurtain.setCalculateNormals(true);
			maxLonCurtain.setTopElevationOffset(topElevation);
			maxLonCurtain.setBottomElevationOffset(bottomElevation);
			maxLonCurtain.setTexture(maxLonTexture.getTexture());
		}
		if (recalculateTop)
		{
			topSurface =
					dataProvider.createHorizontalSurface((float) maxVariance, new Rectangle(minLatOffset, minLonOffset,
							maxLatSlice - minLatOffset + 1, maxLonSlice - minLonOffset + 1));
			topSurface.setLighted(true);
			topSurface.setCalculateNormals(true);
			topSurface.setElevation(topElevation);
			topSurface.setTexture(topTexture.getTexture());
		}
		if (recalculateBottom)
		{
			bottomSurface =
					dataProvider.createHorizontalSurface((float) maxVariance, new Rectangle(minLatOffset, minLonOffset,
							maxLatSlice - minLatOffset + 1, maxLonSlice - minLonOffset + 1));
			bottomSurface.setLighted(true);
			bottomSurface.setCalculateNormals(true);
			bottomSurface.setReverseNormals(true);
			bottomSurface.setElevation(bottomElevation);
			bottomSurface.setTexture(bottomTexture.getTexture());
		}
		if (recalculateTopElevation)
		{
			topSurface.setElevation(topElevation);
			minLatCurtain.setTopElevationOffset(topElevation);
			maxLatCurtain.setTopElevationOffset(topElevation);
			minLonCurtain.setTopElevationOffset(topElevation);
			maxLonCurtain.setTopElevationOffset(topElevation);
		}
		if (recalculateBottomElevation)
		{
			bottomSurface.setElevation(bottomElevation);
			minLatCurtain.setBottomElevationOffset(bottomElevation);
			maxLatCurtain.setBottomElevationOffset(bottomElevation);
			minLonCurtain.setBottomElevationOffset(bottomElevation);
			maxLonCurtain.setBottomElevationOffset(bottomElevation);
		}

		Rectangle latRectangle = new Rectangle(0, 0, dataProvider.getYSize(), dataProvider.getZSize());
		Rectangle lonRectangle = new Rectangle(0, 0, dataProvider.getXSize(), dataProvider.getZSize());
		Rectangle elevationRectangle = new Rectangle(0, 0, dataProvider.getXSize(), dataProvider.getYSize());

		if (recalculateMinLatTexture)
		{
			updateTexture(generateTexture(0, minLatOffset, latRectangle), minLatTexture, minLatCurtain);
			lastMinLatOffset = minLatOffset;
		}
		if (recalculateMaxLatTexture)
		{
			updateTexture(generateTexture(0, maxLatSlice, latRectangle), maxLatTexture, maxLatCurtain);
			lastMaxLatOffset = maxLatOffset;
		}
		if (recalculateMinLonTexture)
		{
			updateTexture(generateTexture(1, minLonOffset, lonRectangle), minLonTexture, minLonCurtain);
			lastMinLonOffset = minLonOffset;
		}
		if (recalculateMaxLonTexture)
		{
			updateTexture(generateTexture(1, maxLonSlice, lonRectangle), maxLonTexture, maxLonCurtain);
			lastMaxLonOffset = maxLonOffset;
		}
		if (recalculateTopTexture)
		{
			updateTexture(generateTexture(2, topOffset, elevationRectangle), topTexture, topSurface);
			lastTopOffset = topOffset;
		}
		if (recalculateBottomTexture)
		{
			updateTexture(generateTexture(2, bottomSlice, elevationRectangle), bottomTexture, bottomSurface);
			lastBottomOffset = bottomOffset;
		}

		synchronized (dataLock)
		{
			if (recalculateTop)
			{
				this.topSurface = topSurface;
			}
			if (recalculateBottom)
			{
				this.bottomSurface = bottomSurface;
			}
			if (recalculateMinLat)
			{
				this.minLatCurtain = minLatCurtain;
			}
			if (recalculateMinLon)
			{
				this.minLonCurtain = minLonCurtain;
			}
			if (recalculateMaxLat)
			{
				this.maxLatCurtain = maxLatCurtain;
			}
			if (recalculateMaxLon)
			{
				this.maxLonCurtain = maxLonCurtain;
			}

			setWireframe(isWireframe());
		}
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

			if (!dc.isPickingMode())
			{
				//render back-to-front
				for (FastShape shape : shapes)
				{
					if (shape != null)
					{
						shape.setColor(Color.white);
						shape.setLighted(true);
						shape.setTextured(true);
						shape.render(dc);
					}
				}

				if (dragging)
				{
					renderBoundingBox(dc);
				}
			}
			else
			{
				boolean oldDeepPicking = dc.isDeepPickingEnabled();
				try
				{
					//deep picking needs to be enabled, because boreholes are below the surface
					dc.setDeepPickingEnabled(true);
					pickSupport.beginPicking(dc);

					for (FastShape shape : shapes)
					{
						if (shape != null)
						{
							Color color = dc.getUniquePickColor();
							pickSupport.addPickableObject(color.getRGB(), shapeToPickableObject(shape));
							shape.setColor(color);
							shape.setLighted(false);
							shape.setTextured(false);
							shape.render(dc);
						}
					}

					pickSupport.resolvePick(dc, dc.getPickPoint(), this);
				}
				finally
				{
					pickSupport.endPicking(dc);
					dc.setDeepPickingEnabled(oldDeepPicking);
				}
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

	protected FastShape pickableObjectToShape(PickableObject po)
	{
		if (po == topPO)
			return topSurface;
		if (po == bottomPO)
			return bottomSurface;
		if (po == minLatPO)
			return minLatCurtain;
		if (po == maxLatPO)
			return maxLatCurtain;
		if (po == minLonPO)
			return minLonCurtain;
		if (po == maxLonPO)
			return maxLonCurtain;
		return null;
	}

	protected PickableObject shapeToPickableObject(FastShape shape)
	{
		if (shape == topSurface)
			return topPO;
		if (shape == bottomSurface)
			return bottomPO;
		if (shape == minLatCurtain)
			return minLatPO;
		if (shape == maxLatCurtain)
			return maxLatPO;
		if (shape == minLonCurtain)
			return minLonPO;
		if (shape == maxLonCurtain)
			return maxLonPO;
		return null;
	}

	@Override
	public void selected(SelectEvent event)
	{
		Object topObject = event.getTopObject();
		pickedShape = topObject instanceof PickableObject ? pickableObjectToShape((PickableObject) topObject) : null;

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
			if (event.getEventAction().equals(SelectEvent.DRAG_END))
			{
				dragging = false;
				event.consume();
			}
			else if (event.getEventAction().equals(SelectEvent.DRAG))
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

	private class PickableObject
	{
	}
}
