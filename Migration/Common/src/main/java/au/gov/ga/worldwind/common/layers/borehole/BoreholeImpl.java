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

/**
 * Basic implementation of a {@link Borehole}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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

	/**
	 * Notify this {@link Borehole} that all samples have been added to it, and
	 * it can create it's geometry. This should be called by the
	 * {@link BoreholeLayer} in it's own loadComplete() function.
	 */
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

		//check if the borehole is within the minimum drawing distance; if not, don't draw
		Extent extent = fastShape.getExtent();
		if (extent != null && layer.getMinimumDistance() != null)
		{
			double distanceToEye = extent.getCenter().distanceTo3(dc.getView().getEyePoint()) - extent.getRadius();
			if (distanceToEye > layer.getMinimumDistance())
				return;
		}

		if (!dc.isPickingMode())
		{
			fastShape.setColorBuffer(boreholeColorBuffer);
			fastShape.render(dc);
		}
		else
		{
			//Don't calculate the picking buffer if the shape isn't going to be rendered anyway.
			//This check is also performed in the shape's render() function, so don't do it above.
			if (extent != null && !dc.getView().getFrustumInModelCoordinates().intersects(extent))
				return;

			boolean oldDeepPicking = dc.isDeepPickingEnabled();
			try
			{
				//deep picking needs to be enabled, because boreholes are below the surface
				dc.setDeepPickingEnabled(true);
				pickSupport.beginPicking(dc);

				//First pick on the entire object by setting the shape to a single color.
				//This will determine if we have to go further and pick individual samples.
				Color overallPickColor = dc.getUniquePickColor();
				pickSupport.addPickableObject(overallPickColor.getRGB(), this, getPosition());
				fastShape.setColor(overallPickColor);
				fastShape.setColorBuffer(null);
				fastShape.render(dc);

				PickedObject object = pickSupport.getTopObject(dc, dc.getPickPoint());
				pickSupport.clearPickList();

				if (object != null && object.getObject() == this)
				{
					//This borehole has been picked; now try picking the samples individually

					//Put unique pick colours into the pickingColorBuffer (2 per sample)
					pickingColorBuffer.rewind();
					for (BoreholeSample sample : getSamples())
					{
						Color color = dc.getUniquePickColor();
						pickSupport.addPickableObject(color.getRGB(), sample, getPosition());
						for (int i = 0; i < 2; i++)
						{
							pickingColorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f)
									.put(color.getBlue() / 255f);
						}
					}

					//render the shape with the pickingColorBuffer, and then resolve the pick
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
