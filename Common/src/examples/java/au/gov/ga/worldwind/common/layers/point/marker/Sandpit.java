package au.gov.ga.worldwind.common.layers.point.marker;

import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.io.InputStream;



import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.WorldWindowRegistry;
import au.gov.ga.worldwind.common.layers.point.PointLayer;
import au.gov.ga.worldwind.common.layers.point.PointLayerFactory;
import au.gov.ga.worldwind.common.util.XMLUtil;

public class Sandpit extends ApplicationTemplate
{
	public static void main(String[] args)
	{
		System.setProperty("java.net.useSystemProxies", "true");

		//Configuration.setValue(AVKey.VERTICAL_EXAGGERATION, 100d);

		ApplicationTemplate.start("Sandpit", AppFrame.class);
	}

	public static class AppFrame extends ApplicationTemplate.AppFrame
	{
		protected WWIcon highlit;

		public AppFrame()
		{
			super(true, true, false);
			
			WorldWindowRegistry.INSTANCE.register(getWwd());

			InputStream is = Sandpit.class.getResourceAsStream("layer_definition.xml");
			Element domElement = XMLUtil.getElementFromSource(is);

			PointLayer layer = PointLayerFactory.createPointLayer(domElement, null);
			layer.setName("Markers");
			insertAfterPlacenames(getWwd(), layer);
			
			FogLayer fog = new FogLayer();
			insertBeforeLayerName(getWwd(), fog, "NASA Blue Marble Image");
			//insertAfterPlacenames(getWwd(), fog);

			// Update layer panel
			this.getLayerPanel().update(getWwd());

			//getWwd().getModel().setShowWireframeExterior(true);
			//getWwd().getModel().setShowWireframeInterior(true);
		}
	}
}
