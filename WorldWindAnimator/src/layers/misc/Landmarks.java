package layers.misc;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceCircle;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import util.FileUtil;

public class Landmarks extends RenderableLayer
{
	public Landmarks(Globe globe)
	{
		String landmarksString = "";
		try
		{
			landmarksString = FileUtil.readFileAsString(new File(
					"F:/West Macs Imagery/landmarks.csv"));
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
							number, description);
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
			SurfaceCircle sc = new SurfaceCircle(globe, landmark.latlon, 100,
					10, Color.red, Color.red);
			addRenderable(sc);
		}
	}

	private static class Landmark
	{
		private LatLon latlon;
		private double number;
		private String description;

		public Landmark(LatLon latlon, double number, String description)
		{
			this.latlon = latlon;
			this.number = number;
			this.description = description;
		}

		@Override
		public String toString()
		{
			return latlon + " " + description;
		}

		public LatLon getLatlon()
		{
			return latlon;
		}

		public double getNumber()
		{
			return number;
		}

		public String getDescription()
		{
			return description;
		}
	}
}
