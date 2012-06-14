package au.gov.ga.worldwind.wmsbrowser;

import static org.junit.Assert.assertNotNull;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;

import org.junit.Test;

import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.wmsbrowser.WmsLayerInfoPanel.LayerInfoBuilder;

/**
 * Unit tests for the {@link LayerInfoBuilder} class.
 *
 */
public class LayerInfoBuilderTest
{

	public LayerInfoBuilderTest()
	{
		MessageSourceAccessor.addBundle("messages.wmsBrowserMessages");
	}
	
	@Test
	public void testTemplateSubstitution()
	{
		WMSLayerCapabilitiesTestImpl layerCapabilities = new WMSLayerCapabilitiesTestImpl();
		layerCapabilities.setTitle("Test Title");
		layerCapabilities.setGeographicBoundingBox(Sector.fromDegrees(-50d, 50d, -123d, 145d));
		
		
		LayerInfoBuilder classToBeTested = new LayerInfoBuilder(layerCapabilities);
		
		String formattedString = classToBeTested.getFormattedString();
		
		assertNotNull(formattedString);
	}

	/**
	 * An extension of the {@link WMSLayerCapabilities} that exposes setters,
	 * used for driving tests
	 */
	private static class WMSLayerCapabilitiesTestImpl extends WMSLayerCapabilities
	{
		public WMSLayerCapabilitiesTestImpl()
		{
			super("test");
		}
		
		@Override
		public void setTitle(String title)
		{
			super.setTitle(title);
		}
		
		@Override
		public void setGeographicBoundingBox(Sector geographicBoundingBox)
		{
			super.setGeographicBoundingBox(geographicBoundingBox);
		}
		
	}
	
}
