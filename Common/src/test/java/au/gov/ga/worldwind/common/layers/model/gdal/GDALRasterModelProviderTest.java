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
package au.gov.ga.worldwind.common.layers.model.gdal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

import java.net.URL;
import java.nio.FloatBuffer;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.common.layers.model.ModelLayer;
import au.gov.ga.worldwind.common.util.FastShape;

/**
 * Unit tests for the {@link GDALRasterModelProvider}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelProviderTest
{

	private GDALRasterModelProvider classUnderTest;
	private Mockery mockContext;
	private ModelLayer modelLayer;
	
	@Before
	public void setup()
	{
		classUnderTest = new GDALRasterModelProvider();
		
		mockContext = new Mockery();
		
		modelLayer = mockContext.mock(ModelLayer.class);
	}
	
	@Test
	public void testConstructWithNull()
	{
		classUnderTest = new GDALRasterModelProvider(null);
		
		// Expect default parameters
		GDALRasterModelParameters defaults = new GDALRasterModelParameters();
		GDALRasterModelParameters actuals = classUnderTest.getModelParameters();
		
		assertEquals(defaults.getBand(), actuals.getBand());
		assertEquals(defaults.getDefaultColor(), actuals.getDefaultColor());
		assertEquals(defaults.getColorMap(), actuals.getColorMap());
		assertEquals(defaults.getMaxVariance(), actuals.getMaxVariance(), 0.001);
	}
	
	@Test
	public void loadWithNullUrl()
	{
		try
		{
			classUnderTest.doLoadData(null, modelLayer);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
			// pass
		}
	}
	
	@Test
	public void loadWithNullLayer()
	{
		try
		{
			URL url = getClass().getResource("testgrid.tif");
			classUnderTest.doLoadData(url, null);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
			// pass
		}
	}
	
	@Test
	public void loadValidUrl()
	{
		URL url = getClass().getResource("testgrid.tif");

		final FastShapeMatcher matcher = new FastShapeMatcher();
		mockContext.checking(new Expectations(){{{
			allowing(modelLayer).addShape(with(matcher));
		}}});
		
		boolean dataLoaded = classUnderTest.doLoadData(url, modelLayer);
		FastShape shape = matcher.shape;
		
		assertTrue(dataLoaded);
		
		assertNotNull(shape);
		assertTrue(shape.isForceSortedPrimitives());
		assertTrue(shape.isLighted());
		assertTrue(shape.isTwoSidedLighting());
		assertTrue(shape.isCalculateNormals());
		assertEquals(4, shape.getColorBufferElementSize());
		
		// With MaxVariance = 0 we expect every pixel to have a corresponding position
		List<Position> positions = shape.getPositions();
		assertNotNull(positions);
		assertEquals(76*52, positions.size());
		
		// Colour buffer should have a 4 element entry per-point 
		FloatBuffer colourBuffer = shape.getColorBuffer();
		assertNotNull(colourBuffer);
		assertEquals(76*52*4, colourBuffer.limit());
		
		Sector sector = shape.getSector();
		assertNotNull(sector);
		assertEquals(141.7855579, sector.getMaxLongitude().degrees, 0.0001);
		
	}
	
	/** A matcher that accepts any FastShape, and provides access for later inspection */
	private static class FastShapeMatcher extends BaseMatcher<FastShape>
	{
		public FastShape shape;
		
		@Override
		public boolean matches(Object item)
		{
			this.shape = (FastShape)item;
			return true;
		}

		@Override
		public void describeTo(Description description) { }
	}
}
