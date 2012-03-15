/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.worldwind.tiler.shapefile;

import java.io.IOException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.io.EndianDataInputStream;

/**
 * Class that reads the shapefile header from a file and provides access to the
 * shapetype, version, and shapefile bounds from the header.
 * <p>
 * Based on the ShapefileHeader class from GeoTools.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShapefileHeader
{
	public static final int SHAPEFILE_ID = 9994;
	public static final int VERSION = 1000;

	private final int fileCode;
	private final int fileLength;
	private final int version;
	private final int shapeType;
	private final Envelope bounds;

	public ShapefileHeader(EndianDataInputStream file) throws IOException
	{
		fileCode = file.readIntBE();
		if (fileCode != SHAPEFILE_ID)
		{
			System.err.println("WARNING filecode " + fileCode + " not a match for documented shapefile code "
					+ SHAPEFILE_ID);
		}

		for (int i = 0; i < 5; i++)
		{
			file.readIntBE();
		}
		fileLength = file.readIntBE();
		version = file.readIntLE();
		shapeType = file.readIntLE();

		//read in the bounding box
		double minx = file.readDoubleLE();
		double miny = file.readDoubleLE();
		double maxx = file.readDoubleLE();
		double maxy = file.readDoubleLE();
		bounds = new Envelope(minx, maxx, miny, maxy);

		//skip remaining unused bytes
		file.skipBytes(32);
	}

	public int getShapeType()
	{
		return shapeType;
	}

	public int getVersion()
	{
		return version;
	}

	public Envelope getBounds()
	{
		return bounds;
	}

	public String toString()
	{
		String res =
				new String("Sf-->type " + fileCode + " size " + fileLength + " version " + version + " Shape Type "
						+ shapeType);
		return res;
	}
}
