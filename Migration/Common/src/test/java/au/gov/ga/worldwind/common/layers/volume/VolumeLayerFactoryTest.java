package au.gov.ga.worldwind.common.layers.volume;

import static org.junit.Assert.assertNotNull;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.net.URL;

import org.junit.Test;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

public class VolumeLayerFactoryTest
{
	@Test
	public void testCreateVolumeLayer()
	{
		URL url = this.getClass().getResource("dummyVolumeLayer.xml");
		Element element = XMLUtil.getElementFromSource(url);
		AVList params = new AVListImpl();
		VolumeLayer layer = VolumeLayerFactory.createVolumeLayer(element, params);
		assertNotNull(layer);
		assertNotNull(params.getValue(AVKeyMore.COLOR_MAP));
		assertNotNull(params.getValue(AVKeyMore.NO_DATA_COLOR));
		assertNotNull(params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_U));
		assertNotNull(params.getValue(AVKeyMore.MAX_VARIANCE));
		assertNotNull(params.getValue(AVKeyMore.MINIMUM_DISTANCE));
		assertNotNull(params.getValue(AVKeyMore.DATA_LAYER_PROVIDER));
	}
}
