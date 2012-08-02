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
package au.gov.ga.worldwind.animator.terrain.exaggeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.ZeroElevationModel;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link AnimatorVerticalExaggerationServiceImpl} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimatorVerticalExaggerationServiceImplTest
{
	private static final double DELTA = 0.0001;

	private VerticalExaggerationElevationModel em;
	private Mockery mockContext;
	private DrawContext dc;
	
	private AnimatorVerticalExaggerationServiceImpl classUnderTest;
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		
		
		dc = mockContext.mock(DrawContext.class);
		
		final Globe globe = mockContext.mock(Globe.class);
		
		em = new VerticalExaggerationElevationModel(new ZeroElevationModel());
		
		mockContext.checking(new Expectations(){{{
			allowing(globe).getElevationModel(); will(returnValue(em));
			allowing(dc).getGlobe(); will(returnValue(globe));
		}}});
		
		classUnderTest = new AnimatorVerticalExaggerationServiceImpl();
	}
	
	@Test
	public void testApplyExaggerationNoExaggerators()
	{
		assertEquals(100, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
	}
	
	@Test
	public void testApplyExaggerationSingleExaggerator()
	{
		em.addExaggerator(new ElevationExaggerationImpl(10, 0));
		
		assertEquals(1000, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
		assertEquals(-100, classUnderTest.applyVerticalExaggeration(dc, -100), DELTA);
	}
	
	@Test
	public void testGetGlobalExaggeration()
	{
		assertEquals(1.0, classUnderTest.getGlobalVerticalExaggeration(dc), DELTA);
	}
	
	@Test
	public void testIsVEChangedNoExaggeratorsNoMark()
	{
		assertTrue(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedNoExaggeratorsWithMark()
	{
		classUnderTest.markVerticalExaggeration(this, dc);
		assertFalse(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedWithExaggeratorsNoMarkNoChange()
	{
		em.addExaggerator(new ElevationExaggerationImpl(30, -10));
		em.addExaggerator(new ElevationExaggerationImpl(10, 0));
		em.addExaggerator(new ElevationExaggerationImpl(20, 10));
		
		assertTrue(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedWithExaggeratorsNoChange()
	{
		em.addExaggerator(new ElevationExaggerationImpl(30, -10));
		em.addExaggerator(new ElevationExaggerationImpl(10, 0));
		em.addExaggerator(new ElevationExaggerationImpl(20, 10));
		
		classUnderTest.markVerticalExaggeration(this, dc);
		
		assertFalse(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedWithExaggeratorsNewExaggerator()
	{
		em.addExaggerator(new ElevationExaggerationImpl(30, -10));
		em.addExaggerator(new ElevationExaggerationImpl(10, 0));
		em.addExaggerator(new ElevationExaggerationImpl(20, 10));
		
		classUnderTest.markVerticalExaggeration(this, dc);
		
		em.addExaggerator(new ElevationExaggerationImpl(40, 20));
		
		assertTrue(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedWithExaggeratorsChangedExaggerator()
	{
		em.addExaggerator(new ElevationExaggerationImpl(30, -10));
		em.addExaggerator(new ElevationExaggerationImpl(10, 0));
		em.addExaggerator(new ElevationExaggerationImpl(20, 10));
		
		classUnderTest.markVerticalExaggeration(this, dc);
		
		em.addExaggerator(new ElevationExaggerationImpl(11, 0));
		
		assertTrue(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedWithExaggeratorsRemovedExaggerator()
	{
		em.addExaggerator(new ElevationExaggerationImpl(30, -10));
		em.addExaggerator(new ElevationExaggerationImpl(10, 0));
		em.addExaggerator(new ElevationExaggerationImpl(20, 10));
		
		classUnderTest.markVerticalExaggeration(this, dc);
		
		em.removeExaggerator(0);
		
		assertTrue(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedAfterClear()
	{
		em.addExaggerator(new ElevationExaggerationImpl(10, 0));
		
		classUnderTest.markVerticalExaggeration(this, dc);
		classUnderTest.clearMark(this);
		
		assertTrue(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testCheckAndMarkWithChange()
	{
		em.addExaggerator(new ElevationExaggerationImpl(30, -10));
		em.addExaggerator(new ElevationExaggerationImpl(10, 0));
		em.addExaggerator(new ElevationExaggerationImpl(20, 10));
		
		classUnderTest.markVerticalExaggeration(this, dc);
		
		assertFalse(classUnderTest.checkAndMarkVerticalExaggeration(this, dc));
		
		em.removeExaggerator(0);
		
		assertTrue(classUnderTest.checkAndMarkVerticalExaggeration(this, dc));
	}
}
