package au.gov.ga.worldwind.common.layers.curtain;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Earth.BMNGWMSLayer;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.io.InputStream;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.curtain.delegate.DelegatorTiledCurtainLayer;
import au.gov.ga.worldwind.common.util.XMLUtil;

public class Sandpit extends ApplicationTemplate
{
	public static void main(String[] args)
	{
		System.setProperty("java.net.useSystemProxies", "true");

		//Configuration.setValue(AVKey.VERTICAL_EXAGGERATION, 100d);
		Configuration.setValue(AVKey.INITIAL_ALTITUDE, 100000);
		Configuration.setValue(AVKey.INITIAL_HEADING, 0);
		Configuration.setValue(AVKey.INITIAL_PITCH, 90);
		Configuration.setValue(AVKey.INITIAL_LATITUDE, -27);
		Configuration.setValue(AVKey.INITIAL_LONGITUDE, 133.5);
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, BMNGWMSLayer.class.getName());
		Configuration.setValue(AVKey.VERTICAL_EXAGGERATION, 10);

		ApplicationTemplate.start("Sandpit", AppFrame.class);
	}

	public static class AppFrame extends ApplicationTemplate.AppFrame
	{
		public AppFrame()
		{
			super(true, true, false);

			InputStream is = Sandpit.class.getResourceAsStream("CurtainLayerDefinition.xml");
			Element element = XMLUtil.getElementFromSource(is);
			
			getWwd().getModel().getLayers().get(1).setOpacity(0.5);
			getWwd().getModel().getGlobe().getTessellator().setMakeTileSkirts(false);
			

			DelegatorTiledCurtainLayer layer = new DelegatorTiledCurtainLayer(element, null);
			insertAfterPlacenames(getWwd(), layer);

			// Update layer panel
			this.getLayerPanel().update(getWwd());
		}
	}
}
