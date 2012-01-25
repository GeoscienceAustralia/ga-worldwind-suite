package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.worldwind.common.layers.Wireframeable;
import au.gov.ga.worldwind.common.util.CoordinateTransformationUtil;
import au.gov.ga.worldwind.common.util.FastShape;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class BasicVolumeLayer extends AbstractLayer implements VolumeLayer, Wireframeable
{
	private VolumeDataProvider dataProvider;
	private float maxVariance = 0;
	private CoordinateTransformation coordinateTransformation = CoordinateTransformationUtil
			.getTransformationToWGS84("EPSG:28354"); //TODO

	private final Object dataLock = new Object();
	private boolean dataAvailable = false;
	private FastShape topSurface;
	private FastShape bottomSurface;
	private TopBottomFastShape minLatCurtain;
	private TopBottomFastShape maxLatCurtain;
	private TopBottomFastShape minLonCurtain;
	private TopBottomFastShape maxLonCurtain;

	private int minLatSlice = 0;
	private int maxLatSlice = Integer.MAX_VALUE;
	private int minLonSlice = 0;
	private int maxLonSlice = Integer.MAX_VALUE;
	private int topSlice = 0;
	private int bottomSlice = Integer.MAX_VALUE;

	private int lastMinLatSlice = -1;
	private int lastMaxLatSlice = -1;
	private int lastMinLonSlice = -1;
	private int lastMaxLonSlice = -1;
	private int lastTopSlice = -1;
	private int lastBottomSlice = -1;

	private boolean wireframe = false;

	//TEMP
	private JFrame frame;
	private JSlider minLatSlider;
	private JSlider maxLatSlider;
	private JSlider minLonSlider;
	private JSlider maxLonSlider;
	private JSlider topSlider;
	private JSlider bottomSlider;
	private WorldWindow wwd;

	//TEMP

	public BasicVolumeLayer()
	{
		// TODO Auto-generated constructor stub
		dataProvider = new SGridVolumeDataProvider();


		//TEMP
		GridBagConstraints c;
		frame = new JFrame(getName());
		frame.getRootPane().setLayout(new GridBagLayout());

		minLatSlider = new JSlider(0, 1000, 0);
		minLatSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				minLatSlice = minLatSlider.getValue();
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		frame.getRootPane().add(minLatSlider, c);

		maxLatSlider = new JSlider(0, 1000, 1000);
		maxLatSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				maxLatSlice = maxLatSlider.getValue();
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.getRootPane().add(maxLatSlider, c);

		minLonSlider = new JSlider(0, 1000, 0);
		minLonSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				minLonSlice = minLonSlider.getValue();
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.getRootPane().add(minLonSlider, c);

		maxLonSlider = new JSlider(0, 1000, 1000);
		maxLonSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				maxLonSlice = maxLonSlider.getValue();
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.getRootPane().add(maxLonSlider, c);

		topSlider = new JSlider(0, 1000, 0);
		topSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				topSlice = topSlider.getValue();
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridy = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.getRootPane().add(topSlider, c);

		bottomSlider = new JSlider(0, 1000, 1000);
		bottomSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				bottomSlice = bottomSlider.getValue();
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridy = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.getRootPane().add(bottomSlider, c);

		frame.pack();
		frame.setVisible(true);
		//TEMP
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		// TODO Auto-generated constructor stub
		return new URL(
				"file:///V:/projects/presentations/11-5902 - Broken Hill 3D model data visualisation/Source/gocad/sgrid_volume/GWMAR_NBC_Vol_200x200.sg");
	}

	@Override
	public String getDataCacheName()
	{
		// TODO Auto-generated constructor stub
		return "GWMAR_NBC_Vol_400x400.sg";
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

		//TEMP
		minLatSlider.setMaximum(provider.getXSize() - 1);
		maxLatSlider.setMaximum(provider.getXSize() - 1);
		minLonSlider.setMaximum(provider.getYSize() - 1);
		maxLonSlider.setMaximum(provider.getYSize() - 1);
		topSlider.setMaximum(provider.getZSize() - 1);
		bottomSlider.setMaximum(provider.getZSize() - 1);
		//TEMP
	}

	protected void recalculateSurfaces()
	{
		if (!dataAvailable)
			return;

		minLatSlice = Math.max(0, minLatSlice);
		maxLatSlice = Math.max(minLatSlice, Math.min(dataProvider.getXSize() - 1, maxLatSlice));
		minLonSlice = Math.max(0, minLonSlice);
		maxLonSlice = Math.max(minLonSlice, Math.min(dataProvider.getYSize() - 1, maxLonSlice));
		topSlice = Math.max(0, topSlice);
		bottomSlice = Math.max(topSlice, Math.min(dataProvider.getZSize() - 1, bottomSlice));

		boolean recalculateMinLat = lastMinLatSlice != minLatSlice;
		boolean recalculateMaxLat = lastMaxLatSlice != maxLatSlice;
		boolean recalculateMinLon = lastMinLonSlice != minLonSlice;
		boolean recalculateMaxLon = lastMaxLonSlice != maxLonSlice;
		recalculateMinLat |= recalculateMinLon | recalculateMaxLon;
		recalculateMaxLat |= recalculateMinLon | recalculateMaxLon;
		recalculateMinLon |= recalculateMinLat | recalculateMaxLat;
		recalculateMaxLon |= recalculateMinLat | recalculateMaxLat;
		boolean recalculateTop = recalculateMinLat || recalculateMaxLat || recalculateMinLon || recalculateMaxLon;
		boolean recalculateBottom = recalculateTop;
		boolean recalculateTopElevation = lastTopSlice != topSlice;
		boolean recalculateBottomElevation = lastBottomSlice != bottomSlice;

		boolean recalculateEitherElevation = recalculateTopElevation | recalculateBottomElevation;
		boolean recalculateAnySide = recalculateMinLat | recalculateMinLon;
		boolean recalculateMinLatTexture = recalculateMinLat | recalculateEitherElevation;
		boolean recalculateMaxLatTexture = recalculateMaxLat | recalculateEitherElevation;
		boolean recalculateMinLonTexture = recalculateMinLon | recalculateEitherElevation;
		boolean recalculateMaxLonTexture = recalculateMaxLon | recalculateEitherElevation;
		boolean recalculateTopTexture = recalculateTopElevation | recalculateAnySide;
		boolean recalculateBottomTexture = recalculateBottomElevation | recalculateAnySide;

		TopBottomFastShape minLatCurtain = this.minLatCurtain, maxLatCurtain = this.maxLatCurtain, minLonCurtain =
				this.minLonCurtain, maxLonCurtain = this.maxLonCurtain;
		FastShape bottomSurface = this.bottomSurface, topSurface = this.topSurface;

		double topElevation = -dataProvider.getDepth() * (topSlice / (double) (dataProvider.getZSize() - 1));
		double bottomElevation = -dataProvider.getDepth() * (bottomSlice / (double) (dataProvider.getZSize() - 1));

		if (recalculateMinLat)
		{
			minLatCurtain = dataProvider.createLatitudeCurtain(minLatSlice, minLonSlice, maxLonSlice);
			minLatCurtain.setLighted(true);
			minLatCurtain.setCalculateNormals(true);
			minLatCurtain.setTopElevationOffset(topElevation);
			minLatCurtain.setBottomElevationOffset(bottomElevation);
			lastMinLatSlice = minLatSlice;
		}
		if (recalculateMaxLat)
		{
			maxLatCurtain = dataProvider.createLatitudeCurtain(maxLatSlice, minLonSlice, maxLonSlice);
			maxLatCurtain.setLighted(true);
			maxLatCurtain.setCalculateNormals(true);
			maxLatCurtain.setReverseNormals(true);
			maxLatCurtain.setTopElevationOffset(topElevation);
			maxLatCurtain.setBottomElevationOffset(bottomElevation);
			lastMaxLatSlice = maxLatSlice;
		}
		if (recalculateMinLon)
		{
			minLonCurtain = dataProvider.createLongitudeCurtain(minLonSlice, minLatSlice, maxLatSlice);
			minLonCurtain.setLighted(true);
			minLonCurtain.setCalculateNormals(true);
			minLonCurtain.setReverseNormals(true);
			minLonCurtain.setTopElevationOffset(topElevation);
			minLonCurtain.setBottomElevationOffset(bottomElevation);
			lastMinLonSlice = minLonSlice;
		}
		if (recalculateMaxLon)
		{
			maxLonCurtain = dataProvider.createLongitudeCurtain(maxLonSlice, minLatSlice, maxLatSlice);
			maxLonCurtain.setLighted(true);
			maxLonCurtain.setCalculateNormals(true);
			maxLonCurtain.setTopElevationOffset(topElevation);
			maxLonCurtain.setBottomElevationOffset(bottomElevation);
			lastMaxLonSlice = maxLonSlice;
		}
		if (recalculateTop)
		{
			topSurface =
					dataProvider.createHorizontalSurface(maxVariance, new Rectangle(minLatSlice, minLonSlice,
							maxLatSlice - minLatSlice + 1, maxLonSlice - minLonSlice + 1));
			topSurface.setLighted(true);
			topSurface.setCalculateNormals(true);
			topSurface.setElevation(topElevation);
		}
		if (recalculateBottom)
		{
			bottomSurface =
					dataProvider.createHorizontalSurface(maxVariance, new Rectangle(minLatSlice, minLonSlice,
							maxLatSlice - minLatSlice + 1, maxLonSlice - minLonSlice + 1));
			bottomSurface.setLighted(true);
			bottomSurface.setCalculateNormals(true);
			bottomSurface.setReverseNormals(true);
			bottomSurface.setElevation(bottomElevation);
		}
		if (recalculateTopElevation)
		{
			topSurface.setElevation(topElevation);
			minLatCurtain.setTopElevationOffset(topElevation);
			maxLatCurtain.setTopElevationOffset(topElevation);
			minLonCurtain.setTopElevationOffset(topElevation);
			maxLonCurtain.setTopElevationOffset(topElevation);
			lastTopSlice = topSlice;
		}
		if (recalculateBottomElevation)
		{
			bottomSurface.setElevation(bottomElevation);
			minLatCurtain.setBottomElevationOffset(bottomElevation);
			maxLatCurtain.setBottomElevationOffset(bottomElevation);
			minLonCurtain.setBottomElevationOffset(bottomElevation);
			maxLonCurtain.setBottomElevationOffset(bottomElevation);
			lastBottomSlice = bottomSlice;
		}
		
		//TODO after a while, regenerating textures crashes the video card. could this be a memory issue? does it run out of texture ids?
		//or could it just possibly be the regeneration of fastshapes causing the JVM to run out of buffer memory?

		if (recalculateMinLatTexture)
		{
			setTexture(
					generateTexture(0, minLatSlice, new Rectangle(minLonSlice, topSlice, maxLonSlice - minLonSlice + 1,
							bottomSlice - topSlice + 1)), minLatCurtain);
		}
		if (recalculateMaxLatTexture)
		{
			setTexture(
					generateTexture(0, maxLatSlice, new Rectangle(minLonSlice, topSlice, maxLonSlice - minLonSlice + 1,
							bottomSlice - topSlice + 1)), maxLatCurtain);
		}
		if (recalculateMinLonTexture)
		{
			setTexture(
					generateTexture(1, minLonSlice, new Rectangle(minLatSlice, topSlice, maxLatSlice - minLatSlice + 1,
							bottomSlice - topSlice + 1)), minLonCurtain);
		}
		if (recalculateMaxLonTexture)
		{
			setTexture(
					generateTexture(1, maxLonSlice, new Rectangle(minLatSlice, topSlice, maxLatSlice - minLatSlice + 1,
							bottomSlice - topSlice + 1)), maxLonCurtain);
		}
		if (recalculateTopTexture)
		{
			setTexture(
					generateTexture(2, topSlice, new Rectangle(minLatSlice, minLonSlice, maxLatSlice - minLatSlice + 1,
							maxLonSlice - minLonSlice + 1)), topSurface);
		}
		if (recalculateBottomTexture)
		{
			setTexture(
					generateTexture(2, bottomSlice, new Rectangle(minLatSlice, minLonSlice, maxLatSlice - minLatSlice
							+ 1, maxLonSlice - minLonSlice + 1)), bottomSurface);
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
				int rgb = 0;
				if (value != dataProvider.getNoDataValue())
				{
					rgb = Color.HSBtoRGB(-0.3f - value * 0.7f, 1.0f, 1.0f);
				}
				image.setRGB(x - rectangle.x, y - rectangle.y, rgb);
			}
		}
		return image;
	}

	protected void setTexture(BufferedImage image, FastShape shape)
	{
		Texture texture = shape.getTexture();
		if (texture != null)
		{
			texture.updateImage(TextureIO.newTextureData(image, true));
		}
		else
		{
			shape.setTexture(TextureIO.newTexture(image, true));
		}
	}

	@Override
	public CoordinateTransformation getCoordinateTransformation()
	{
		return coordinateTransformation;
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

			/*for (FastShape shape : shapes)
			{
				if (shape != null)
				{
					shape.render(dc);
				}
			}*/

			for (int i = shapes.length - 1; i >= 0; i--)
			{
				FastShape shape = shapes[i];
				if (shape != null)
				{
					shape.render(dc);
				}
			}
		}
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
			return Double.compare(d1, d2);
		}
	}
}
