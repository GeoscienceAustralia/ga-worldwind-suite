package au.gov.ga.worldwind.animator.layers.misc;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.PatternFactory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.animator.util.FileUtil;

public class Landmarks extends RenderableLayer
{
	private BufferedImage image;

	public Landmarks(Globe globe)
	{
		image = createDiskImage(10, Color.red);

		String landmarksString = "";
		try
		{
			landmarksString = FileUtil.readFileAsString(new File(
					"D:/West Macs Imagery/landmarks.csv"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		String[] landmarksSplit = landmarksString.split("[\\n\\r\\f]+");
		List<Landmark> landmarks = new ArrayList<Landmark>();
		for (String landmarkString : landmarksSplit)
		{
			if (!landmarkString.trim().startsWith("#"))
			{
				String[] split = landmarkString.split(",");
				Landmark landmark = null;
				try
				{
					double lat = Double.parseDouble(split[0]);
					double lon = Double.parseDouble(split[1]);
					double number = Double.parseDouble(split[2]);
					String description = split[3];
					landmark = new Landmark(LatLon.fromDegrees(lat, lon),
							number, description, image);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				if (landmark != null)
				{
					landmarks.add(landmark);
				}
			}
		}
		for (Landmark landmark : landmarks)
		{
			addRenderable(landmark);
			GlobeAnnotation anno = new GlobeAnnotation(landmark.number + "",
					landmark.getPosition());
			addRenderable(anno);
		}
	}

	private static class Landmark extends GlobeAnnotation
	{
		private double number;
		private String description;

		public Landmark(LatLon latlon, double number, String description,
				BufferedImage image)
		{
			super("", new Position(latlon, 0));
			this.number = number;
			this.description = description;

			AnnotationAttributes aa = getAttributes();

			aa.setBorderWidth(0);
			aa.setImageSource(image);
			aa.setAdjustWidthToText(Annotation.SIZE_FIXED);
			aa.setSize(new Dimension(image.getWidth(), image.getHeight()));
			aa.setBackgroundColor(new Color(0, 0, 0, 0));
			aa.setCornerRadius(0);
			aa.setInsets(new Insets(0, 0, 0, 0));
			aa.setDrawOffset(new Point(0, -image.getHeight() / 2));
			aa.setLeader(FrameFactory.LEADER_NONE);
			aa.setDistanceMaxScale(1);
			aa.setDistanceMinScale(1);
			aa.setDistanceMinOpacity(1);
		}

		@SuppressWarnings("unused")
		public double getNumber()
		{
			return number;
		}

		@SuppressWarnings("unused")
		public String getDescription()
		{
			return description;
		}
	}

	public static BufferedImage createDiskImage(int size, Color color)
	{
		BufferedImage image = PatternFactory.createPattern(
				PatternFactory.PATTERN_CIRCLE, new Dimension(size, size), 1f,
				color);
		Graphics g = image.getGraphics();
		g.setColor(Color.black);
		g.drawOval(size / 2 - 1, size / 2 - 1, 1, 1);
		g.dispose();
		return image;
	}
}
