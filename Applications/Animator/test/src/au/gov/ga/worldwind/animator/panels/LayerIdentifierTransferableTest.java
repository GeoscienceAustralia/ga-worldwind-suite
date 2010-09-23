package au.gov.ga.worldwind.animator.panels;

import static org.junit.Assert.assertEquals;
import static au.gov.ga.worldwind.animator.panels.DataTransferFlavors.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifier;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifierImpl;

/**
 * Unit tests for the {@link LayerIdentifierTransferable} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class LayerIdentifierTransferableTest
{
	private LayerIdentifierTransferable classToBeTested;
	
	private LayerIdentifier identifier = new LayerIdentifierImpl("TestLayer", "file://my/layer/location/layer.xml");
	
	@Before
	public void setup()
	{
		classToBeTested = new LayerIdentifierTransferable(identifier);
	}
	
	@Test
	public void testSupportsStringFlavor() throws Exception
	{
		assertEquals(true, classToBeTested.isDataFlavorSupported(getStringFlavor()));
	}
	
	@Test
	public void testSupportsURLFlavor() throws Exception
	{
		assertEquals(true, classToBeTested.isDataFlavorSupported(getURLFlavor()));
	}
	
	@Test
	public void testSupportsLayerIdentifierFlavor() throws Exception
	{
		assertEquals(true, classToBeTested.isDataFlavorSupported(getLayerIdentifierFlavor()));
	}

	@Test
	public void testGetWithStringFlavorReturnsName() throws Exception
	{
		assertEquals(identifier.getName(), classToBeTested.getTransferData(getStringFlavor()));
	}
	
	@Test
	public void testGetWithURLFlavorReturnsLocationAsUrl() throws Exception
	{
		assertEquals(new URL(identifier.getLocation()), classToBeTested.getTransferData(getURLFlavor()));
	}
	
	@Test
	public void testGetWithLayerIdentifierFlavorReturnsIdentifier() throws Exception
	{
		assertEquals(identifier, classToBeTested.getTransferData(getLayerIdentifierFlavor()));
	}
	
}
