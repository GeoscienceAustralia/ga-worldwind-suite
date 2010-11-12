package au.gov.ga.worldwind.common.layers.tiled.image.elevation;

import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.render.WWIcon;

import java.io.InputStream;

import au.gov.ga.worldwind.common.layers.LayerFactory;
import au.gov.ga.worldwind.common.layers.delegate.transformer.ColorLimitTransformerDelegate;
import au.gov.ga.worldwind.common.layers.delegate.transformer.ResizeTransformerDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageDelegateFactory;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader.ColorMapElevationImageReaderDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader.ShadedElevationImageReaderDelegate;
import au.gov.ga.worldwind.common.terrain.ElevationModelFactory;

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

			InputStream eis =
					this.getClass().getResourceAsStream(
							"/config/Earth/EarthElevationModelAsBil16.xml");

			ElevationModelFactory emf = new ElevationModelFactory();
			ElevationModel elevationModel = (ElevationModel) emf.createFromConfigSource(eis, null);
			getWwd().getModel().getGlobe().setElevationModel(elevationModel);

			ImageDelegateFactory.get().registerDelegate(NormalMapImageReaderDelegate.class);
			ImageDelegateFactory.get().registerDelegate(ShaderRenderDelegate.class);

			LayerFactory factory = new LayerFactory();

			InputStream is =
					this.getClass().getResourceAsStream("EarthElevationModelAsBil16Shaded.xml");
			Layer layer = (Layer) factory.createFromConfigSource(is, null);
			//layer.setOpacity(0.2);
			insertAfterPlacenames(getWwd(), layer);

			is = this.getClass().getResourceAsStream("EarthElevationModelAsBil16Colored.xml");
			layer = (Layer) factory.createFromConfigSource(is, null);
			insertAfterPlacenames(getWwd(), layer);

			TerrainProfileLayer profile = new TerrainProfileLayer();
			profile.setEventSource(getWwd());
			profile.setFollow(TerrainProfileLayer.FOLLOW_VIEW);
			profile.setStartLatLon(LatLon.fromDegrees(0, -10));
			profile.setEndLatLon(LatLon.fromDegrees(0, 65));
			profile.setZeroBased(false);
			profile.setKeepProportions(false);
			profile.setShowProfileLine(true);
			insertBeforePlacenames(getWwd(), profile);

			// Update layer panel
			this.getLayerPanel().update(getWwd());
		}
	}
}
