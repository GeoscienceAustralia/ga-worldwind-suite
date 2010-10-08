package au.gov.ga.worldwind.common.layers.shapefile.point.marker;

import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.render.WWIcon;

import java.io.InputStream;

import nasa.worldwind.layers.FogLayer;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.XMLUtil;

public class Sandpit extends ApplicationTemplate
{
	public static void main(String[] args)
	{
		System.setProperty("http.proxyHost", "proxy.agso.gov.au");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("http.nonProxyHosts", "localhost");

		//Configuration.setValue(AVKey.VERTICAL_EXAGGERATION, 100d);

		ApplicationTemplate.start("Sandpit", AppFrame.class);
	}

	public static class AppFrame extends ApplicationTemplate.AppFrame
	{
		protected WWIcon highlit;

		public AppFrame()
		{
			super(true, true, false);

			InputStream is = Sandpit.class.getResourceAsStream("layer_definition.xml");
			Element domElement = XMLUtil.getElementFromSource(is);

			ShapefileMarkerLayer layer = new ShapefileMarkerLayer(domElement, null);
			layer.setName("Markers");
			insertAfterPlacenames(getWwd(), layer);
			getWwd().addSelectListener(layer);
			
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
