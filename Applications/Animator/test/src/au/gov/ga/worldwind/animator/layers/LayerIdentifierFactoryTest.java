package au.gov.ga.worldwind.animator.layers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for the {@link LayerIdentifierFactory} class
 */
public class LayerIdentifierFactoryTest
{
	
	@Test
	public void testLoadFromDefinitionWithDisplayNameElement()
	{
		URL definitionUrl = getClass().getResource("layerDefinitionWithDisplayName.xml");
		
		LayerIdentifier result = LayerIdentifierFactory.createFromDefinition(definitionUrl);
		
		assertNotNull(result);
		assertEquals(definitionUrl.toExternalForm(), result.getLocation());
		assertEquals("myName", result.getName());
	}
	
	@Test
	public void testLoadFromDefinitionWithEmptyDisplayNameElement()
	{
		URL definitionUrl = getClass().getResource("layerDefinitionWithEmptyDisplayName.xml");
		
		LayerIdentifier result = LayerIdentifierFactory.createFromDefinition(definitionUrl);
		
		assertNotNull(result);
		assertEquals(definitionUrl.toExternalForm(), result.getLocation());
		assertEquals("layerDefinitionWithEmptyDisplayName", result.getName());
	}
	
	@Test
	public void testLoadFromDefinitionWithoutDisplayNameElement()
	{
		URL definitionUrl = getClass().getResource("layerDefinitionWithoutDisplayName.xml");
		
		LayerIdentifier result = LayerIdentifierFactory.createFromDefinition(definitionUrl);
		
		assertNotNull(result);
		assertEquals(definitionUrl.toExternalForm(), result.getLocation());
		assertEquals("layerDefinitionWithoutDisplayName", result.getName());
	}
	
	@Test
	public void testLoadFromPropertiesFile()
	{
		List<LayerIdentifier> result = LayerIdentifierFactory.readFromPropertiesFile("au.gov.ga.worldwind.animator.layers.testLayerIdentities");
		
		assertNotNull(result);
		assertEquals(3, result.size());
		
		assertEquals("Layer1", result.get(0).getName());
		assertEquals("Layer 2", result.get(1).getName());
		assertEquals("Another - Layer", result.get(2).getName());
		
		assertEquals("http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/layer1.xml", result.get(0).getLocation());
		assertEquals("http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/layer_2.xml", result.get(1).getLocation());
		assertEquals("http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/another_layer.xml", result.get(2).getLocation());
	}
	
}
