/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.worldwind.tiler.ribbon;

import java.io.File;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.SpatialReference;

/**
 * Command line utility for generating a path for the Ribbon XML layer
 * definition from a SEGY file. Uses the simplification algorithm in OGR.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LineSimplifier
{
	public static void main(String[] args)
	{
		run(args);

		/*File file =
				new File(
						"V:/projects/2013 projects/13-7128_-_World_Wind_-_Bonaparte/animation/data/SEGY/new/GA336_107_rel_mig_4-45deg.sgy");
		args = new String[] { file.getAbsolutePath(), "EPSG:32752", "EPSG:4326", "0.001" };
		run(args);*/
	}

	public static void run(String[] args)
	{
		String usage =
				LineSimplifier.class.getSimpleName() + " <source file> <source SRS> <target SRS> <simplify tolerance>";
		if (args.length != 4)
		{
			System.out.println(usage);
			return;
		}

		ogr.RegisterAll();

		File file = new File(args[0]);
		if (!file.exists())
		{
			System.err.println("File not found: " + file.getAbsolutePath());
			return;
		}

		SpatialReference sourceSR = new SpatialReference();
		sourceSR.SetFromUserInput(args[1]);

		SpatialReference targetSR = new SpatialReference();
		targetSR.SetFromUserInput(args[2]);

		double simplify = Double.parseDouble(args[3]);

		DataSource source = ogr.Open(file.getAbsolutePath());
		Geometry lineString = new Geometry(ogrConstants.wkbLineString);

		for (int i = 0; i < source.GetLayerCount(); i++)
		{
			Layer layer = source.GetLayer(i);
			Feature feature;
			while ((feature = layer.GetNextFeature()) != null)
			{
				Geometry geometry = feature.GetGeometryRef();
				if (geometry != null)
				{
					geometry.AssignSpatialReference(sourceSR);
					geometry.TransformTo(targetSR);

					for (int j = 0; j < geometry.GetPointCount(); j++)
					{
						double[] point = geometry.GetPoint(j);
						if (point.length >= 3)
						{
							lineString.AddPoint(point[0], point[1], point[2]);
						}
						else
						{
							lineString.AddPoint(point[0], point[1]);
						}
					}
				}
			}
		}

		Geometry simplified = lineString.Simplify(simplify);

		System.out.println("\t<Path>");
		for (int i = 0; i < simplified.GetPointCount(); i++)
		{
			double[] point = simplified.GetPoint(i);
			System.out.println("\t\t<LatLon units=\"degrees\" latitude=\"" + point[1] + "\" longitude=\"" + point[0]
					+ "\" />");
		}
		System.out.println("\t</Path>");
	}
}
