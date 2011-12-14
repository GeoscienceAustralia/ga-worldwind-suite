package au.gov.ga.worldwind.common.layers.borehole;

import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.layers.point.types.UrlMarker;
import au.gov.ga.worldwind.common.util.FastShape;

import com.sun.opengl.util.BufferUtil;

public class BoreholeImpl extends UrlMarker implements Borehole, Renderable
{
	private final BoreholeLayer layer;
	private final Object sampleLock = new Object();
	private List<BoreholeSample> samples = new ArrayList<BoreholeSample>();

	private FastShape fastShape;
	private FloatBuffer boreholeColorBuffer;
	private FloatBuffer pickingColorBuffer;

	private final PickSupport pickSupport = new PickSupport();

	public BoreholeImpl(BoreholeLayer layer, Position position, MarkerAttributes attrs)
	{
		super(position, attrs);
		this.layer = layer;
	}

	@Override
	public List<BoreholeSample> getSamples()
	{
		return samples;
	}

	public void setSamples(List<BoreholeSample> samples)
	{
		this.samples = samples;
	}

	public void addSample(BoreholeSample sample)
	{
		synchronized (sampleLock)
		{
			samples.add(sample);
		}
	}

	public void loadComplete()
	{
		List<Position> positions = new ArrayList<Position>();
		List<Color> colors = new ArrayList<Color>();

		double latitude = getPosition().getLatitude().degrees;
		double longitude = getPosition().getLongitude().degrees;
		for (BoreholeSample sample : getSamples())
		{
			positions.add(Position.fromDegrees(latitude, longitude, -sample.getDepthFrom()));
			positions.add(Position.fromDegrees(latitude, longitude, -sample.getDepthTo()));
			colors.add(sample.getColor());
			colors.add(sample.getColor());
		}

		boreholeColorBuffer = FastShape.color3ToFloatBuffer(colors);
		pickingColorBuffer = BufferUtil.newFloatBuffer(colors.size() * 3);

		fastShape = new FastShape(positions, GL.GL_LINES);
		fastShape.setColorBuffer(boreholeColorBuffer);
		fastShape.setFollowTerrain(true);
	}

	@Override
	public String getText()
	{
		return getTooltipText();
	}

	@Override
	public String getLink()
	{
		return getUrl();
	}

	@Override
	public void render(DrawContext dc)
	{
		if (fastShape == null)
			return;

		if (!dc.isPickingMode())
		{
			fastShape.setColorBuffer(boreholeColorBuffer);
			fastShape.render(dc);
		}
		else
		{
			//Don't calculate the picking buffer if the shape isn't going to be rendered anyway.
			//This check is also performed in the shape's render() function, so don't do it above.
			Extent extent = fastShape.getExtent();
			if (extent != null && !dc.getView().getFrustumInModelCoordinates().intersects(extent))
				return;

			boolean oldDeepPicking = dc.isDeepPickingEnabled();
			try
			{
				dc.setDeepPickingEnabled(true);
				pickSupport.beginPicking(dc);

				Color overallPickColor = dc.getUniquePickColor();
				pickSupport.addPickableObject(overallPickColor.getRGB(), this, getPosition());
				fastShape.setColor(overallPickColor);
				fastShape.setColorBuffer(null);
				fastShape.render(dc);

				PickedObject object = pickSupport.getTopObject(dc, dc.getPickPoint());
				pickSupport.clearPickList();

				if (object != null && object.getObject() == this)
				{
					//this borehole has been picked; now try picking the samples individually

					pickingColorBuffer.rewind();
					for (BoreholeSample sample : getSamples())
					{
						Color color = dc.getUniquePickColor();
						pickSupport.addPickableObject(color.getRGB(), sample, getPosition());
						pickingColorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f)
								.put(color.getBlue() / 255f);
						pickingColorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f)
								.put(color.getBlue() / 255f);
					}
					
					fastShape.setColorBuffer(pickingColorBuffer);
					fastShape.render(dc);
					
					pickSupport.resolvePick(dc, dc.getPickPoint(), layer);
				}
			}
			finally
			{
				pickSupport.endPicking(dc);
				dc.setDeepPickingEnabled(oldDeepPicking);
			}
		}
	}
}
