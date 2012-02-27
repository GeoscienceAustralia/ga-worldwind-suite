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
package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.worldwind.common.util.URLUtil;

import com.sun.opengl.util.BufferUtil;

/**
 * {@link VolumeDataProvider} implementation which reads volume data from a
 * GOCAD SGrid (.sg) file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SGridVolumeDataProvider extends AbstractVolumeDataProvider
{
	private final static Pattern paintedVariablePattern = Pattern.compile("\\*painted\\*variable:\\s*(.*?)\\s*");
	private final static Pattern axisPattern = Pattern
			.compile("AXIS_(\\S+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+).*");
	private final static Pattern propAlignmentPattern = Pattern.compile("PROP_ALIGNMENT\\s+(.*?)\\s*");
	private final static Pattern asciiDataFilePattern = Pattern.compile("ASCII_DATA_FILE\\s+(.*?)\\s*");
	private final static Pattern propertyNamePattern = Pattern.compile("PROPERTY\\s+(\\d+)\\s+\"?(.*?)\"?\\s*");
	private final static Pattern propertyNoDataPattern = Pattern
			.compile("PROP_NO_DATA_VALUE\\s+(\\d+)\\s+([\\d.\\-]+)\\s*");

	private String asciiDataFile;
	private String paintedVariable;
	private int paintedVariableId;
	private boolean cellCentered;

	@Override
	protected boolean doLoadData(URL url, VolumeLayer layer)
	{
		ZipFile zip = null;
		try
		{
			File file = URLUtil.urlToFile(url);

			InputStream sgInputStream = null;
			try
			{
				if (file.getName().toLowerCase().endsWith(".zip"))
				{
					zip = new ZipFile(file);
					Enumeration<? extends ZipEntry> entries = zip.entries();
					ZipEntry sgEntry = null;
					while (entries.hasMoreElements())
					{
						ZipEntry entry = entries.nextElement();
						if (entry.getName().toLowerCase().endsWith(".sg"))
						{
							sgEntry = entry;
							break;
						}
					}

					if (sgEntry == null)
					{
						throw new IOException("Could not find .sg file in zip");
					}

					sgInputStream = zip.getInputStream(sgEntry);
				}
				else
				{
					sgInputStream = new FileInputStream(file);
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(sgInputStream));
				String line;
				while ((line = reader.readLine()) != null)
				{
					parseLine(line);
				}
			}
			finally
			{
				if (sgInputStream != null)
				{
					sgInputStream.close();
				}
			}

			if (asciiDataFile == null)
			{
				throw new IOException("Data file not specified");
			}
			if (xSize == 0 || ySize == 0 || zSize == 0)
			{
				throw new IOException("Volume dimensions are 0");
			}
			if (cellCentered)
			{
				xSize--;
				ySize--;
				zSize--;
			}

			InputStream dataInputStream = null;
			try
			{
				if (zip != null)
				{
					ZipEntry dataEntry = zip.getEntry(asciiDataFile);
					dataInputStream = zip.getInputStream(dataEntry);
				}
				if (dataInputStream == null)
				{
					File data = new File(file.getParent(), asciiDataFile);
					if (data.exists())
					{
						dataInputStream = new FileInputStream(data);
					}
				}
				if (dataInputStream == null)
				{
					throw new IOException("Data file '" + asciiDataFile + "' not found");
				}

				//TODO add support for SGrid binary property files

				//setup data variables
				sector = null;
				double[] transformed = new double[3];
				CoordinateTransformation transformation = layer.getCoordinateTransformation();
				positions = new ArrayList<Position>(xSize * ySize);
				int positionIndex = 0;
				double firstZValue = 0;
				data = BufferUtil.newFloatBuffer(xSize * ySize * zSize);
				top = 0;

				//setup the ASCII data file line regex
				String doublePattern = "([\\d.\\-]+)";
				String nonCapturingDoublePattern = "(?:[\\d.\\-]+)";
				String spacerPattern = "\\s+";
				//regex for coordinates
				String lineRegex = doublePattern + spacerPattern + doublePattern + spacerPattern + doublePattern;
				for (int property = 1; property < paintedVariableId; property++)
				{
					//ignore all properties in between coordinates and painted property
					lineRegex += spacerPattern + nonCapturingDoublePattern;
				}
				//only capture the painted property
				lineRegex += spacerPattern + doublePattern + ".*";
				Pattern linePattern = Pattern.compile(lineRegex);

				//read the ASCII data file
				BufferedReader reader = new BufferedReader(new InputStreamReader(dataInputStream));
				String line;
				while ((line = reader.readLine()) != null)
				{
					Matcher matcher = linePattern.matcher(line);
					if (matcher.matches())
					{
						double x = Double.parseDouble(matcher.group(1));
						double y = Double.parseDouble(matcher.group(2));
						double z = Double.parseDouble(matcher.group(3));
						float value = Float.parseFloat(matcher.group(4));

						//transform the point
						if (transformation != null)
						{
							//TODO should the z coordinate be transformed here?
							transformation.TransformPoint(transformed, x, y, z);
							x = transformed[0];
							y = transformed[1];
							z = transformed[2];
						}

						//only store the first width*height positions (the rest are evenly spaced at different depths)
						if (positionIndex < xSize * ySize)
						{
							Position position = Position.fromDegrees(y, x, z);
							positions.add(position);
							top += z / (double) (xSize * ySize);

							//update the sector to include this latitude/longitude
							if (sector == null)
							{
								sector =
										new Sector(position.latitude, position.latitude, position.longitude,
												position.longitude);
							}
							else
							{
								sector = sector.union(position.latitude, position.longitude);
							}
						}

						if (positionIndex == 0)
						{
							firstZValue = z;
						}
						else if (positionIndex == xSize * ySize * (zSize - 1))
						{
							//positionIndex is the same x/y as 0, but at the bottom elevation instead of top,
							//so we can calculate the depth as the difference between the two elevations
							depth = firstZValue - z;
						}

						//put the data into the floatbuffer
						data.put(value);
						positionIndex++;
					}
				}
			}
			finally
			{
				if (dataInputStream != null)
				{
					dataInputStream.close();
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			if (zip != null)
			{
				try
				{
					zip.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		/*try
		{
			ArrayVolumeDataProvider.saveVolumeDataProviderToArrayFile(this,
					URLUtil.urlToFile(new URL(url.toExternalForm() + ".array")));
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}*/

		layer.dataAvailable(this);
		return true;
	}

	protected void parseLine(String line)
	{
		Matcher matcher = paintedVariablePattern.matcher(line);
		if (matcher.matches())
		{
			paintedVariable = matcher.group(1);
		}

		matcher = axisPattern.matcher(line);
		if (matcher.matches())
		{
			//check for AXIS_N
			if (matcher.group(1).equals("N"))
			{
				xSize = (int) Double.parseDouble(matcher.group(2));
				ySize = (int) Double.parseDouble(matcher.group(3));
				zSize = (int) Double.parseDouble(matcher.group(4));
			}
		}

		matcher = propAlignmentPattern.matcher(line);
		if (matcher.matches())
		{
			String propAlignment = matcher.group(1);
			cellCentered = propAlignment.toLowerCase().equals("cells");
		}

		matcher = asciiDataFilePattern.matcher(line);
		if (matcher.matches())
		{
			asciiDataFile = matcher.group(1);
		}

		matcher = propertyNamePattern.matcher(line);
		if (matcher.matches())
		{
			int propertyId = Integer.parseInt(matcher.group(1));
			String propertyName = matcher.group(2);
			if (propertyName.equals(paintedVariable))
			{
				paintedVariableId = propertyId;
			}
		}

		matcher = propertyNoDataPattern.matcher(line);
		if (matcher.matches())
		{
			int propertyId = Integer.parseInt(matcher.group(1));
			float noDataValue = Float.parseFloat(matcher.group(2));
			if (propertyId == paintedVariableId)
			{
				this.noDataValue = noDataValue;
			}
		}
	}
}
