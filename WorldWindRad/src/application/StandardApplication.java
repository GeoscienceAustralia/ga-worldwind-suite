package application;

import globes.GAGlobe;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;
import gov.nasa.worldwind.layers.Earth.OpenStreetMapLayer;
import gov.nasa.worldwind.layers.Earth.WorldBordersMetacartaLayer;

import javax.swing.UIManager;

import layers.other.GravityLayer;
import layers.other.MagneticsLayer;
import layers.radiometry.AreasLayer;
import layers.radiometry.DoseRateLayer;
import layers.radiometry.PotassiumLayer;
import layers.radiometry.RatioThKLayer;
import layers.radiometry.RatioUKLayer;
import layers.radiometry.RatioUThLayer;
import layers.radiometry.TernaryLayer;
import layers.radiometry.ThoriumLayer;
import layers.radiometry.UraniumLayer;
import stereo.StereoOrbitView;
import stereo.StereoSceneController;

public class StandardApplication extends ApplicationTemplate
{
	public static void main(String[] args)
	{
		System.setProperty("http.proxyHost", "proxy.agso.gov.au");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("sun.java2d.noddraw", "true");

		/*Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME,
				ReportingRetrievalService.class.getName());*/
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME,
				StereoSceneController.class.getName());
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, StereoOrbitView.class
				.getName());
		Configuration.setValue(AVKey.GLOBE_CLASS_NAME, GAGlobe.class.getName());

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}

		ApplicationTemplate.start("Radiometry", AppFrame.class);
	}

	public static class AppFrame extends ApplicationTemplate.AppFrame
	{
		public AppFrame()
		{
			super(true, true, false);

			LayerList layers = getWwd().getModel().getLayers();

			Layer ternary = new TernaryLayer();
			ternary.setEnabled(false);
			layers.add(ternary);

			Layer uranium = new UraniumLayer();
			uranium.setEnabled(false);
			layers.add(uranium);

			Layer thorium = new ThoriumLayer();
			thorium.setEnabled(false);
			layers.add(thorium);

			Layer potassium = new PotassiumLayer();
			potassium.setEnabled(false);
			layers.add(potassium);

			Layer doserate = new DoseRateLayer();
			doserate.setEnabled(false);
			layers.add(doserate);

			Layer areas = new AreasLayer();
			areas.setEnabled(false);
			layers.add(areas);

			Layer ratiothk = new RatioThKLayer();
			ratiothk.setEnabled(false);
			layers.add(ratiothk);

			Layer ratiouk = new RatioUKLayer();
			ratiouk.setEnabled(false);
			layers.add(ratiouk);

			Layer ratiouth = new RatioUThLayer();
			ratiouth.setEnabled(false);
			layers.add(ratiouth);

			Layer gravity = new GravityLayer();
			gravity.setEnabled(false);
			layers.add(gravity);
			
			Layer magnetics = new MagneticsLayer();
			magnetics.setEnabled(false);
			layers.add(magnetics);

			/*VectorLayer vectorLayer = new VectorLayer();
			vectorLayer.setEnabled(false);
			try
			{
				vectorLayer.addShapefileLayer(new URL("file:///C:/Data/Other Projects/Radiometry/Shapefiles/pol_lndd.shp"), null);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			layers.add(vectorLayer);*/

			/*try
			{
				ShapefileLayer shapefileLayer = new ShapefileLayer(
						new URL(
								"file:///C:/Data/Other Projects/Radiometry/Shapefiles/pol_lndd.shp"));
				shapefileLayer.setEnabled(false);
				layers.add(shapefileLayer);
			}
			catch (MalformedURLException e)
			{
			}*/

			Layer osml = new OpenStreetMapLayer();
			osml.setEnabled(false);
			layers.add(osml);

			Layer borders = new WorldBordersMetacartaLayer();
			borders.setEnabled(true);
			layers.add(borders);

			Layer graticule = new MGRSGraticuleLayer();
			graticule.setEnabled(false);
			layers.add(graticule);

			getLayerPanel().update(getWwd());

			//getWwd().getModel().setShowWireframeInterior(true);

			//new DownloadStatus().setVisible(true);
		}
	}
}
