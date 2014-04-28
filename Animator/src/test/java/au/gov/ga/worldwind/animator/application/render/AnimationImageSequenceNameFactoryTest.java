package au.gov.ga.worldwind.animator.application.render;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.common.view.stereo.IStereoViewDelegate.Eye;

/**
 * Unit tests for the {@link AnimationImageSequenceNameFactory} class
 */
public class AnimationImageSequenceNameFactoryTest
{
	private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

	private Mockery mockContext;
	
	private Animation animation;
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		
		animation = mockContext.mock(Animation.class);
	}
	
	private void setAnimationFrameCount(final int frameCount)
	{
		mockContext.checking(new Expectations(){{
			allowing(animation).getFrameCount();will(returnValue(frameCount));
		}});
	}
	
	@Test
	public void testCreateImageSequenceNamePadding()
	{
		setAnimationFrameCount(1000);
		assertEquals("theFrame0013", AnimationImageSequenceNameFactory.createImageSequenceName(animation, 13, "theFrame"));
	}
	
	@Test
	public void testCreateImageSequenceNameWithNullAnimation()
	{
		try
		{
			assertEquals("theFrame0013", AnimationImageSequenceNameFactory.createImageSequenceName(null, 13, "theFrame"));
			fail();
		}
		catch (Exception e)
		{
			assertTrue("Expected IllegalArgumentException. Got: " + e.getClass().getCanonicalName(), e instanceof IllegalArgumentException);
		}
	}
	
	@Test
	public void testCreateImageSequenceNameWithBlankFrameName()
	{
		setAnimationFrameCount(10);
		assertEquals("frame13", AnimationImageSequenceNameFactory.createImageSequenceName(animation, 13, null));
	}
	
	@Test
	public void testCreateImageSequenceFile()
	{
		setAnimationFrameCount(222);
		File outputFile = AnimationImageSequenceNameFactory.createImageSequenceFile(animation, 56, "myFrame", TEMP_DIR);
		
		assertNotNull(outputFile);
		assertEquals("myFrame056.tga", outputFile.getName());
		assertEquals(TEMP_DIR.getAbsolutePath(), outputFile.getParent());
		
		outputFile.deleteOnExit();
	}
	
	@Test
	public void testCreateStereoImageSequenceFileRight()
	{
		setAnimationFrameCount(222);
		File outputFile = AnimationImageSequenceNameFactory.createStereoImageSequenceFile(animation, 56, "myFrame", TEMP_DIR, Eye.RIGHT);
		
		assertNotNull(outputFile);
		assertEquals("myFrame056.tga", outputFile.getName());
		assertEquals(TEMP_DIR.getAbsolutePath() + File.separator + "myFrame_right", outputFile.getParent());
		
		outputFile.deleteOnExit();
	}
	
	@Test
	public void testCreateStereoImageSequenceFileLeft()
	{
		setAnimationFrameCount(222);
		File outputFile = AnimationImageSequenceNameFactory.createStereoImageSequenceFile(animation, 56, "myFrame", TEMP_DIR, Eye.LEFT);
		
		assertNotNull(outputFile);
		assertEquals("myFrame056.tga", outputFile.getName());
		assertEquals(TEMP_DIR.getAbsolutePath() + File.separator + "myFrame_left", outputFile.getParent());
		
		outputFile.deleteOnExit();
	}
}
