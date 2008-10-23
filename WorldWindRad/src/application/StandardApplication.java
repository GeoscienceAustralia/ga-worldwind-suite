package application;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.formats.dds.DDSConverter;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.Earth.OpenStreetMapLayer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import layers.radiometry.AreasLayer;
import layers.radiometry.DoseRateLayer;
import layers.radiometry.GAGlobe;
import layers.radiometry.GravityLayer;
import layers.radiometry.PotassiumLayer;
import layers.radiometry.TernaryLayer;
import layers.radiometry.ThoriumLayer;
import layers.radiometry.UraniumLayer;
import stereo.StereoOrbitView;
import stereo.StereoSceneController;
import stereo.StereoOrbitView.StereoMode;

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

		ApplicationTemplate.start("Radiometry", AppFrame.class);
	}

	public static class AppFrame extends ApplicationTemplate.AppFrame
	{
		public AppFrame()
		{
			super(true, true, false);

			getWwd().getModel().setGlobe(new GAGlobe());

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
			
			Layer gravity = new GravityLayer();
			gravity.setEnabled(false);
			layers.add(gravity);

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

			OpenStreetMapLayer osml = new OpenStreetMapLayer();
			osml.setEnabled(false);
			layers.add(osml);

			getLayerPanel().update(getWwd());

			//getWwd().getModel().setShowWireframeInterior(true);
			getWwd().getSceneController().setVerticalExaggeration(1);

			((StereoOrbitView) getWwd().getView()).setMode(StereoMode.NONE);
			//((StereoOrbitView)getWwd().getView()).setEyeSeparation(100);

			//new DownloadStatus().setVisible(true);
		}
	}
}
