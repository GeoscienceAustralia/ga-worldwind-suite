package au.gov.ga.worldwind.animator.panels;

import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.*;
import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.AnimationObject;

/**
 * Unit tests for the {@link AnimationObjectTransferable} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class AnimationObjectTransferableTest
{
	private AnimationObjectTransferable classToBeTested;
	
	private AnimationObject animationObject;
	
	@Before
	public void setup()
	{
		Mockery mockContext = new Mockery();
		
		animationObject = mockContext.mock(AnimationObject.class);
		mockContext.checking(new Expectations(){{
			allowing(animationObject).getName();will(returnValue("TestAnimationObject"));
		}});
		
		classToBeTested = new AnimationObjectTransferable(animationObject);
	}
	
	@Test
	public void testSupportsStringFlavor()
	{
		assertEquals(true, classToBeTested.isDataFlavorSupported(getStringFlavor()));
	}
	
	@Test
	public void testSupportsAnimationObjectFlavor()
	{
		assertEquals(true, classToBeTested.isDataFlavorSupported(getAnimationObjectFlavor()));
	}
	
	@Test
	public void testDoesNotSupportOtherFlavor()
	{
		assertEquals(false, classToBeTested.isDataFlavorSupported(getLayerIdentifierFlavor()));
	}
	
	@Test
	public void testDoesNotSupportNullFlavor()
	{
		assertEquals(false, classToBeTested.isDataFlavorSupported(null));
	}

	@Test
	public void testGetWithStringFlavorReturnsName() throws Exception
	{
		assertEquals(animationObject.getName(), classToBeTested.getTransferData(getStringFlavor()));
	}
	
	@Test
	public void testGetWithAnimationObjectFlavorReturnsObject() throws Exception
	{
		assertEquals(animationObject, classToBeTested.getTransferData(getAnimationObjectFlavor()));
	}
	
	@Test
	public void testGetWithOtherFlavorReturnsNull() throws Exception
	{
		assertEquals(null, classToBeTested.getTransferData(getLayerIdentifierFlavor()));
	}
	
	@Test
	public void testGetWithNullFlavorReturnsNull() throws Exception
	{
		assertEquals(null, classToBeTested.getTransferData(null));
	}
	
}
