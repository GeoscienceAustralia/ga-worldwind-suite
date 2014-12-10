/*******************************************************************************
 * Copyright 2014 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;
import au.gov.ga.worldwind.animator.animation.camera.Head;
import au.gov.ga.worldwind.animator.animation.camera.HeadImpl;
import au.gov.ga.worldwind.animator.animation.io.AnimationWriter;
import au.gov.ga.worldwind.animator.animation.io.XmlAnimationWriter;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.sun.SunPositionAnimatable;
import au.gov.ga.worldwind.animator.animation.sun.SunPositionAnimatableImpl;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.layers.LayerIdentifierFactory;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifierFactory;
import au.gov.ga.worldwind.animator.view.AnimatorView;
import au.gov.ga.worldwind.common.sun.SunPositionService;

/**
 * Utility that takes a log file from the Oculus viewer and generates an
 * animation from it.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OculusAnimationCreator
{
	private static class RotationPosition
	{
		public final Quaternion rotation;
		public final Vec4 position;

		public RotationPosition(Quaternion rotation, Vec4 position)
		{
			this.rotation = rotation;
			this.position = position;
		}
	}

	private static class Clip
	{
		public final Position location;
		public final Angle heading;
		public final File oculusFile;
		public final File outputFile;
		public final long start;
		public final long end;
		public final double groundOpacity;

		public Clip(Position location, Angle heading, File oculusFile, File outputFile, long start, long end,
				double groundOpacity)
		{
			this.location = location;
			this.heading = heading;
			this.oculusFile = oculusFile;
			this.outputFile = outputFile;
			this.start = start;
			this.end = end;
			this.groundOpacity = groundOpacity;
		}
	}

	public static void main(String[] args) throws IOException
	{
		long timeOffset = 12 * 60 * 60 * 1000; //computer was 11 hours behind, plus 1 hour for daylight savings?
		Clip[] clips =
				new Clip[] {
						new Clip(Position.fromDegrees(-35.101200, 149.376931, 684), Angle.fromDegrees(137),
								new File("C:/Data/Lake George/Logs/oculus_1415788716808.log"), new File(
										"C:/Data/Lake George/Logs/clip1.xml"), 1415788822595L, 1415788898595L, 1.0),
						new Clip(Position.fromDegrees(-35.101200, 149.376931, 684), Angle.fromDegrees(137),
								new File("C:/Data/Lake George/Logs/oculus_1415788716808.log"), new File(
										"C:/Data/Lake George/Logs/clip2.xml"), 1415788935595L, 1415789002595L, 1.0),
						new Clip(Position.fromDegrees(-35.101915, 149.379060, 677), Angle.fromDegrees(137),
								new File("C:/Data/Lake George/Logs/oculus_1415789677540.log"), new File(
										"C:/Data/Lake George/Logs/clip3.xml"), 1415789774095L, 1415789876595L, 1.0),
						new Clip(Position.fromDegrees(-35.103786, 149.379810, 677), Angle.fromDegrees(-136),
								new File("C:/Data/Lake George/Logs/oculus_1415790707204.log"), new File(
										"C:/Data/Lake George/Logs/clip4.xml"), 1415791149595L, 1415791193595L, 1.0),
						new Clip(Position.fromDegrees(-35.104341, 149.379882, 677), Angle.fromDegrees(-136),
								new File("C:/Data/Lake George/Logs/oculus_1415790707204.log"), new File(
										"C:/Data/Lake George/Logs/clip5.xml"), 1415791398595L, 1415791416595L, 1.0),
						new Clip(Position.fromDegrees(-35.105341, 149.379882, 677), Angle.fromDegrees(-136),
								new File("C:/Data/Lake George/Logs/oculus_1415790707204.log"), new File(
										"C:/Data/Lake George/Logs/clip6.xml"), 1415791586595L, 1415791643595L, 1.0),
						new Clip(Position.fromDegrees(-35.104374, 149.383387, 677), Angle.fromDegrees(-136),
								new File("C:/Data/Lake George/Logs/oculus_1415790707204.log"), new File(
										"C:/Data/Lake George/Logs/clip7.xml"), 1415791902595L, 1415791929595L, 1.0) };
		Angle fieldOfView = Angle.fromDegrees(90);


		AnimatorConfiguration.initialiseConfiguration();
		Settings.get();

		for (Clip clip : clips)
		{
			int frameRate = 25;

			WorldWindow wwd = new WorldWindowGLCanvas();
			AnimatorView view = new AnimatorView();
			//wwd.setModel(new BasicModel());
			view.setGlobe(new Earth());
			wwd.setView(view);

			WorldWindAnimationImpl animation = new WorldWindAnimationImpl(wwd);
			animation.getRenderParameters().setFrameRate(frameRate);
			animation.getRenderParameters().setRenderAlpha(true);
			//animation.getRenderParameters().setImageDimension(new Dimension(1920, 1080));
			animation.setZoomScalingRequired(false);

			animation.addElevationModel(ElevationModelIdentifierFactory.createFromDefinition(new URL(
					"http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/earth_elevation_model.xml")));
			animation.addElevationModel(ElevationModelIdentifierFactory.createFromDefinition(new URL(
					"file:/C:/Data/Lake%20George/Data/Lidar/Lake_George_1m_LIDAR_crop_tiles_em.xml")));

			animation.addLayer(LayerIdentifierFactory.createFromDefinition(new URL(
					"file:/C:/Data/Lake%20George/Data/effects/atmosphere_sky.xml")));
			animation.addLayer(LayerIdentifierFactory.createFromDefinition(new URL(
					"file:/C:/Data/Lake%20George/Data/seis_interp.xml")));
			animation.addLayer(LayerIdentifierFactory.createFromDefinition(new URL(
					"file:/C:/Data/Lake%20George/Data/mig_13LG003_25_dpth.xml")));
			animation.addLayer(LayerIdentifierFactory.createFromDefinition(new URL(
					"file:/C:/Data/Lake%20George/Data/Robust_Model_Geosoft_Image_cleaned.xml")));
			animation.addLayer(LayerIdentifierFactory.createFromDefinition(new URL(
					"file:/C:/Data/Lake%20George/Data/surfaces/surf_wgs84.xml")));
			animation.getLayers().get(animation.getLayers().size() - 1).setOpacity(0.5);
			animation.addLayer(LayerIdentifierFactory.createFromDefinition(new URL(
					"http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/landsat.xml")));
			animation.getLayers().get(animation.getLayers().size() - 1).setOpacity(clip.groundOpacity);
			animation.addLayer(LayerIdentifierFactory.createFromDefinition(new URL(
					"file:/C:/Data/Lake%20George/Data/Satellite/LakeGeorge_tiles.xml")));
			animation.getLayers().get(animation.getLayers().size() - 1).setOpacity(clip.groundOpacity);
			//animation.addLayer(LayerIdentifierFactory.createFromDefinition(new URL(
			//		"file:/C:/Data/Lake%20George/Data/effects/atmosphere_ground.xml")));
			animation.addLayer(LayerIdentifierFactory.createFromDefinition(new URL(
					"file:/C:/Data/Lake%20George/Data/effects/sun.xml")));

			SunPositionAnimatable sun = new SunPositionAnimatableImpl("sun", animation);
			animation.addAnimatableObject(sun);
			sun.getType().applyValueAnyway(SunPositionService.SunPositionType.SpecificTime.ordinal());
			sun.getTime().applyValueAnyway(clip.start + timeOffset);

			view.setCenterPosition(clip.location);
			view.setZoom(1);
			view.setPitch(Angle.POS90);
			view.setHeading(clip.heading);
			view.setFieldOfView(fieldOfView);

			for (Parameter parameter : animation.getAllParameters())
			{
				parameter.setArmed(true);
				parameter.setEnabled(true);
			}

			animation.recordKeyFrame(0);

			for (Parameter parameter : animation.getArmedParameters())
			{
				parameter.setArmed(false);
			}

			Head head = new HeadImpl(animation);
			animation.addAnimatableObject(1, head);

			NavigableMap<Double, RotationPosition> rotations = new TreeMap<Double, RotationPosition>();
			DataInputStream dis = new DataInputStream(new FileInputStream(clip.oculusFile));
			Long lastNanos = null;
			double seconds = 0;
			while (dis.available() > 0)
			{
				long millis = dis.readLong();
				long nanos = dis.readLong();
				float eye1RotationX = dis.readFloat();
				float eye1RotationY = dis.readFloat();
				float eye1RotationZ = dis.readFloat();
				float eye1RotationW = dis.readFloat();
				float eye1PositionX = dis.readFloat();
				float eye1PositionY = dis.readFloat();
				float eye1PositionZ = dis.readFloat();

				/*float eye2RotationX = */dis.readFloat();
				/*float eye2RotationY = */dis.readFloat();
				/*float eye2RotationZ = */dis.readFloat();
				/*float eye2RotationW = */dis.readFloat();
				/*float eye2PositionX = */dis.readFloat();
				/*float eye2PositionY = */dis.readFloat();
				/*float eye2PositionZ = */dis.readFloat();

				if (!(clip.start <= millis && millis <= clip.end))
					continue;

				seconds += lastNanos == null ? 0 : (nanos - lastNanos) / 1e9;
				lastNanos = nanos;
				rotations.put(seconds, new RotationPosition(new Quaternion(eye1RotationX, eye1RotationY, eye1RotationZ,
						eye1RotationW), new Vec4(eye1PositionX, eye1PositionY, eye1PositionZ)));
			}
			dis.close();

			int frames = (int) (frameRate * seconds);
			animation.setFrameCount(frames);

			for (int frame = 0; frame < frames; frame++)
			{
				double frameSeconds = frame / (double) frameRate;
				Entry<Double, RotationPosition> floor = rotations.floorEntry(frameSeconds);
				Entry<Double, RotationPosition> ceiling = rotations.ceilingEntry(frameSeconds);
				double mixer = (frameSeconds - floor.getKey()) / (ceiling.getKey() - floor.getKey());
				if (Double.isNaN(mixer))
				{
					mixer = 0;
				}
				Quaternion currentRotation =
						Quaternion.slerp(mixer, floor.getValue().rotation, ceiling.getValue().rotation);
				Vec4 currentPosition = Vec4.mix3(mixer, floor.getValue().position, ceiling.getValue().position);
				view.setHeadRotation(currentRotation);
				view.setHeadPosition(currentPosition);
				animation.recordKeyFrame(frame);
			}

			AnimationWriter writer = new XmlAnimationWriter();
			writer.writeAnimation(clip.outputFile, animation);
		}
	}
}
