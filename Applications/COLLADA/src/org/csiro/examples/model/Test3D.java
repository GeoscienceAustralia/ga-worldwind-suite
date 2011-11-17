package org.csiro.examples.model;

import gov.nasa.worldwind.custom.render.Ardor3DModel;
import gov.nasa.worldwind.custom.render.Movable3DModel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

public class Test3D extends ApplicationTemplate {

	public static class AppFrame extends ApplicationTemplate.AppFrame {

		public AppFrame() {
			super(true, true, false);
			try {
				RenderableLayer layer = new RenderableLayer();
				layer.setName("movable 3D object");

				layer
						.addRenderable(new Movable3DModel(
								"./testmodels/spaceship.3ds",
								new Position(Angle
										.fromDegrees(-34.940679766700), Angle
										.fromDegrees(138.623116628920), 200000),
								300000));

				/*
				 * Movable3DModel superdome = new Movable3DModel(
				 * "./testmodels/models/model.dae", new
				 * Position(Angle.fromDegrees(-34.940679766700),
				 * Angle.fromDegrees(138.623116628920), 70), 1.0);
				 * superdome.setUseArdor(true); layer.addRenderable(superdome);
				 */
				/*
				 * Movable3DModel macquarie = new Movable3DModel(
				 * "./testmodels/models/untitled.dae", new
				 * Position(Angle.fromDegrees(-42.88306855273),
				 * Angle.fromDegrees(147.3295738186), 25), 0.1);
				 * macquarie.setUseArdor(true); layer.addRenderable(macquarie);
				 */
				/*
				 * Ardor3DModel model = new Ardor3DModel(
				 * "./testmodels/drykovanov/models/untitled.dae", new
				 * Position(Angle.fromDegrees(-42.88306855273),
				 * Angle.fromDegrees(147.3295738186), 25));
				 * 
				 * layer.addRenderable(model);
				 */
				Ardor3DModel airap = new Ardor3DModel(
						"./testmodels/models/model.dae", new Position(Angle
								.fromDegrees(-34.940679766700), Angle
								.fromDegrees(138.623116628920), 70));

				layer.addRenderable(airap);

				
				layer.addRenderable(new Movable3DModel(
						"./testmodels/sea_horse/sea_horse.obj", new Position(
								Angle.fromDegrees(-42.88306855273), Angle
										.fromDegrees(147.3295738186), 2000),
						3000));

				layer.addRenderable(new Movable3DModel(
						"./testmodels/sea_horse/sea_horse.obj", new Position(
								Angle.fromDegrees(-42.98306855273), Angle
										.fromDegrees(147.3295738186), 2000),
						3000));
				
				this.getWwd().addSelectListener(
						new GenericDraggerAdjuster(this.getWwd()));
				insertBeforeCompass(this.getWwd(), layer);
				this.getLayerPanel().update(this.getWwd());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		ApplicationTemplate
				.start("Movable 3D Model Layer Test", AppFrame.class);
	}
}
