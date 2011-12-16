package au.gov.ga.worldwind.common.layers.model;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Basic implementation of a {@link ModelLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicModelLayer extends AbstractLayer implements ModelLayer
{
	private ModelProvider provider;
	private FastShape shape;

	private String url;
	private URL context;
	private String dataCacheName;

	private Color color;
	private Double lineWidth;
	private Double pointSize;

	public BasicModelLayer(AVList params)
	{
		context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		url = params.getStringValue(AVKey.URL);
		dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
		provider = (ModelProvider) params.getValue(AVKeyMore.DATA_LAYER_PROVIDER);

		color = (Color) params.getValue(AVKeyMore.COLOR);
		lineWidth = (Double) params.getValue(AVKeyMore.LINE_WIDTH);
		pointSize = (Double) params.getValue(AVKeyMore.POINT_SIZE);

		Validate.notBlank(url, "Model data url not set");
		Validate.notBlank(dataCacheName, "Model data cache name not set");

		Validate.notNull(provider, "Model data provider is null");
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
		return provider.isLoading();
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		provider.addLoadingListener(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		provider.removeLoadingListener(listener);
	}

	@Override
	public FastShape getShape()
	{
		return shape;
	}

	@Override
	public void setShape(FastShape shape)
	{
		this.shape = shape;
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!isEnabled())
		{
			return;
		}

		provider.requestData(this);

		if (shape == null)
		{
			return;
		}

		GL gl = dc.getGL();
		try
		{
			gl.glPushAttrib(GL.GL_POINT_BIT | GL.GL_LINE_BIT);

			if (pointSize != null)
			{
				gl.glPointSize(pointSize.floatValue());
			}
			if (lineWidth != null)
			{
				gl.glLineWidth(lineWidth.floatValue());
			}
			if (color != null)
			{
				shape.setColor(color);
			}

			shape.render(dc);
		}
		finally
		{
			gl.glPopAttrib();
		}
	}

	@Override
	public Sector getSector()
	{
		if (shape == null)
			return null;
		return shape.getSector();
	}

	@Override
	public void setup(WorldWindow wwd)
	{
	}
}
