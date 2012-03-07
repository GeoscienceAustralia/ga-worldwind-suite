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
package au.gov.ga.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.Validate;

import com.sun.opengl.util.BufferUtil;

/**
 * {@link GocadReader} implementation for reading GOCAD GSurf files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadGSurfReader implements GocadReader
{
	public final static String HEADER_REGEX = "(?i).*gsurf.*";

	private final static Pattern axisPattern = Pattern
			.compile("(?:(ORIGIN)|(?:AXIS_(\\S+)))\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)(?:\\s+([\\d.\\-]+))?.*");
	private final static Pattern namePattern = Pattern.compile("name:\\s*(.*)\\s*");
	private final static Pattern typePattern = Pattern.compile("TYPE\\s+(\\w+)\\s*");
	private final static Pattern zpositivePattern = Pattern.compile("ZPOSITIVE\\s+(\\w+)\\s*");
	private final static Pattern colorPattern = Pattern.compile("\\*solid\\*color:.+");
	private final static Pattern propertyPattern = Pattern.compile("PROP_(\\S+)\\s+(\\d+)\\s+(\\S+).*");

	private String name;
	private boolean zPositive = true;

	private Vec4 axisO;
	private Vec4 axisU;
	private Vec4 axisV;
	private Vec4 axisW;
	private Vec4 axisMIN;
	private Vec4 axisMAX;
	private Vec4 axisN;

	private Double noDataValue = null;
	private int esize = 4;
	private String etype = "IEEE";
	private int offset = 0;
	private String file;

	private Color color;
	private boolean cellCentered = false;

	private GocadReaderParameters parameters;

	@Override
	public void begin(GocadReaderParameters parameters)
	{
		this.parameters = parameters;
	}

	@Override
	public void addLine(String line)
	{
		Matcher matcher = axisPattern.matcher(line);
		if (matcher.matches())
		{
			parseAxis(matcher);
			return;
		}

		matcher = propertyPattern.matcher(line);
		if (matcher.matches())
		{
			parseProperty(matcher);
			return;
		}

		matcher = namePattern.matcher(line);
		if (matcher.matches())
		{
			name = matcher.group(1);
			return;
		}

		matcher = zpositivePattern.matcher(line);
		if (matcher.matches())
		{
			zPositive = !matcher.group(1).equalsIgnoreCase("depth");
		}

		matcher = colorPattern.matcher(line);
		if (matcher.matches())
		{
			color = GocadColor.gocadLineToColor(line);
			return;
		}

		matcher = typePattern.matcher(line);
		if (matcher.matches())
		{
			cellCentered = matcher.group(1).equalsIgnoreCase("cells");
		}
	}

	private void parseAxis(Matcher matcher)
	{
		String type = matcher.group(1);
		if (type == null)
		{
			type = matcher.group(2);
		}
		double d0 = Double.parseDouble(matcher.group(3));
		double d1 = Double.parseDouble(matcher.group(4));
		double d2 = 0;
		String group5 = matcher.group(5);
		if (group5 != null)
		{
			d2 = Double.parseDouble(group5);
		}

		Vec4 v = new Vec4(d0, d1, d2);
		if (type.equals("O") || type.equals("ORIGIN"))
		{
			axisO = v;
		}
		else if (type.equals("U"))
		{
			axisU = v;
		}
		else if (type.equals("V"))
		{
			axisV = v;
		}
		else if (type.equals("W"))
		{
			axisW = v;
		}
		else if (type.equals("MIN"))
		{
			axisMIN = v;
		}
		else if (type.equals("MAX"))
		{
			axisMAX = v;
		}
		else if (type.equals("N"))
		{
			axisN = new Vec4(v.x, v.y, 1);
		}
	}

	private void parseProperty(Matcher matcher)
	{
		String type = matcher.group(1);
		int id = Integer.parseInt(matcher.group(2));
		String value = matcher.group(3);

		//currently only read the first property's parameters:
		if (id != 1)
			return;

		if (type.equals("NO_DATA_VALUE"))
		{
			noDataValue = Double.parseDouble(value);
		}
		else if (type.equals("ESIZE"))
		{
			esize = Integer.parseInt(value);
		}
		else if (type.equals("TYPE"))
		{
			etype = value;
		}
		else if (type.equals("OFFSET"))
		{
			offset = Integer.parseInt(value);
		}
		else if (type.equals("FILE"))
		{
			file = value;
		}
	}

	@Override
	public FastShape end(URL context)
	{
		if (axisN == null)
		{
			return null;
		}

		if (cellCentered)
		{
			axisN = new Vec4(axisN.x - 1, axisN.y - 1, 1);
		}

		int nu = (int) Math.round(axisN.x), nv = (int) Math.round(axisN.y);
		Vec4 axisUStride = axisU.multiply3((axisMAX.x - axisMIN.x) / (axisN.x - 1));
		Vec4 axisVStride = axisV.multiply3((axisMAX.y - axisMIN.y) / (axisN.y - 1));
		Vec4 axisUOrigin = axisU.multiply3(axisMIN.x);
		Vec4 axisVOrigin = axisV.multiply3(axisMIN.y);
		Vec4 axisWOrigin = axisW.multiply3(axisMIN.z);
		Vec4 origin =
				new Vec4(axisO.x + axisUOrigin.x + axisVOrigin.x + axisWOrigin.x, axisO.y + axisUOrigin.y
						+ axisVOrigin.y + axisWOrigin.y, axisO.z + axisUOrigin.z + axisVOrigin.z + axisWOrigin.z);

		Validate.isTrue(esize == 4, "Unsupported PROP_ESIZE value: " + esize);
		Validate.isTrue("IEEE".equals(etype), "Unsupported PROP_ETYPE value: " + etype);

		int strideU = parameters.getSubsamplingU();
		int strideV = parameters.getSubsamplingV();

		if (parameters.isDynamicSubsampling())
		{
			float samplesPerAxis = parameters.getDynamicSubsamplingSamplesPerAxis();
			strideU = Math.max(1, Math.round((float) axisN.x / samplesPerAxis));
			strideV = Math.max(1, Math.round((float) axisN.y / samplesPerAxis));
		}

		int uSamples = (int) (1 + (nu - 1) / strideU);
		int vSamples = (int) (1 + (nv - 1) / strideV);

		List<Position> positions = new ArrayList<Position>(uSamples * vSamples);
		float[] values = new float[uSamples * vSamples];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = Float.NaN;
		}

		double[] transformed = new double[3];
		CoordinateTransformation transformation = parameters.getCoordinateTransformation();

		float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
		try
		{
			URL fileUrl = new URL(context, file);
			InputStream is = new BufferedInputStream(fileUrl.openStream());
			is.skip(offset);

			boolean ieee = "IEEE".equals(etype);

			if (parameters.isBilinearMinification())
			{
				//contains the number of values summed
				int[] count = new int[values.length];

				//read all the values, and sum them in regions
				for (int v = 0; v < nv; v++)
				{
					int vRegion = (v / strideV) * uSamples;
					for (int u = 0; u < nu; u++)
					{
						float value = GocadVoxetReader.readNextFloat(is, parameters.getByteOrder(), ieee);
						if (!Float.isNaN(value) && value != noDataValue)
						{
							int uRegion = (u / strideU);
							int valueIndex = vRegion + uRegion;

							//if this is the first value for this region, set it, otherwise add it
							if (count[valueIndex] == 0)
							{
								values[valueIndex] = value;
							}
							else
							{
								values[valueIndex] += value;
							}
							count[valueIndex]++;
						}
					}
				}

				//divide all the sums by the number of values summed (basically, average)
				for (int i = 0; i < values.length; i++)
				{
					if (count[i] > 0)
					{
						values[i] /= count[i];
						min = Math.min(min, values[i]);
						max = Math.max(max, values[i]);
					}
				}

				//create points for each summed region that has a value
				for (int v = 0, vi = 0; v < nv; v += strideV, vi++)
				{
					int vOffset = vi * uSamples;
					Vec4 vAdd = axisVStride.multiply3(v);
					for (int u = 0, ui = 0; u < nu; u += strideU, ui++)
					{
						int uOffset = ui;
						int valueIndex = vOffset + uOffset;
						float value = values[valueIndex];

						Vec4 uAdd = axisUStride.multiply3(u);
						Vec4 p =
								Float.isNaN(value) ? new Vec4(origin.x + uAdd.x + vAdd.x, origin.y + uAdd.y + vAdd.y,
										origin.z + uAdd.z + vAdd.z) : new Vec4(origin.x + uAdd.x + vAdd.x + axisW.x
										* value, origin.y + uAdd.y + vAdd.y + axisW.y * value, origin.z + uAdd.z
										+ vAdd.z + axisW.z * value);

						if (transformation != null)
						{
							transformation.TransformPoint(transformed, p.x, p.y, zPositive ? p.z : -p.z);
							positions.add(PositionWithCoord.fromDegrees(transformed[1], transformed[0], transformed[2],
									ui, vi));
						}
						else
						{
							positions.add(PositionWithCoord.fromDegrees(p.y, p.x, zPositive ? p.z : -p.z, ui, vi));
						}
					}
				}
			}
			else
			{
				//non-bilinear is simple; we can skip over any input values that don't contribute to the points
				int valueIndex = 0;
				for (int v = 0, vi = 0; v < nv; v += strideV, vi++)
				{
					Vec4 vAdd = axisVStride.multiply3(v);
					for (int u = 0, ui = 0; u < nu; u += strideU, ui++)
					{
						Vec4 uAdd = axisUStride.multiply3(u);
						Vec4 p;

						float value = GocadVoxetReader.readNextFloat(is, parameters.getByteOrder(), ieee);
						if (!Float.isNaN(value) && value != noDataValue)
						{
							values[valueIndex] = value;
							min = Math.min(min, value);
							max = Math.max(max, value);
							p =
									new Vec4(origin.x + uAdd.x + vAdd.x + axisW.x * value, origin.y + uAdd.y + vAdd.y
											+ axisW.y * value, origin.z + uAdd.z + vAdd.z + axisW.z * value);
						}
						else
						{
							p =
									new Vec4(origin.x + uAdd.x + vAdd.x, origin.y + uAdd.y + vAdd.y, origin.z + uAdd.z
											+ vAdd.z);
						}

						if (transformation != null)
						{
							transformation.TransformPoint(transformed, p.x, p.y, zPositive ? p.z : -p.z);
							positions.add(PositionWithCoord.fromDegrees(transformed[1], transformed[0], transformed[2],
									ui, vi));
						}
						else
						{
							positions.add(PositionWithCoord.fromDegrees(p.y, p.x, zPositive ? p.z : -p.z, ui, vi));
						}

						valueIndex++;
						GocadVoxetReader.skipBytes(is, esize * Math.min(strideU - 1, nu - u - 1));
					}
					GocadVoxetReader.skipBytes(is, esize * nu * Math.min(strideV - 1, nv - v - 1));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		BinaryTriangleTree btt = new BinaryTriangleTree(positions, uSamples, vSamples);
		btt.setForceGLTriangles(true); //ensures that the shape's triangles can be sorted when transparent
		FastShape shape = btt.buildMesh(parameters.getMaxVariance());
		positions = shape.getPositions();

		if (name == null)
		{
			name = "GSurf";
		}

		shape.setName(name);
		shape.setForceSortedPrimitives(true);
		shape.setLighted(true);
		shape.setCalculateNormals(true);
		shape.setTwoSidedLighting(true);

		//create a color buffer containing a color for each point
		int colorBufferElementSize = 4;
		FloatBuffer colorBuffer = BufferUtil.newFloatBuffer(positions.size() * colorBufferElementSize);
		for (Position position : positions)
		{
			PositionWithCoord pwv = (PositionWithCoord) position;
			int u = pwv.u, v = pwv.v;
			int un = u > 0 ? u - 1 : u, up = u < uSamples - 1 ? u + 1 : u, vn = v > 0 ? v - 1 : v, vp =
					v < vSamples - 1 ? v + 1 : v;
			v *= uSamples;
			vn *= uSamples;
			vp *= uSamples;
			//check all values around the current position for NODATA; if NODATA, use a transparent color
			float value = values[u + v], l = values[un + v], r = values[up + v], t = values[u + vn], b = values[u + vp], tl =
					values[un + vn], tr = values[up + vn], bl = values[un + vp], br = values[up + vp];
			if (Float.isNaN(value) || Float.isNaN(l) || Float.isNaN(r) || Float.isNaN(t) || Float.isNaN(b)
					|| Float.isNaN(tl) || Float.isNaN(tr) || Float.isNaN(bl) || Float.isNaN(br))
			{
				//this or adjacent cell is NODATA
				for (int i = 0; i < colorBufferElementSize; i++)
				{
					colorBuffer.put(0);
				}
			}
			else
			{
				Color color = this.color;
				if (parameters.getColorMap() != null)
				{
					color = parameters.getColorMap().calculateColorNotingIsValuesPercentages(value, min, max);
				}
				else
				{
					//float percent = (value - min) / (max - min);
					//color = new HSLColor((1f - percent) * 300f, 100f, 50f).getRGB();
				}
				colorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f)
						.put(color.getAlpha() / 255f);
			}
		}
		shape.setColorBuffer(colorBuffer);
		shape.setColorBufferElementSize(colorBufferElementSize);

		return shape;
	}

	protected static class PositionWithCoord extends Position
	{
		public final int u;
		public final int v;

		public PositionWithCoord(Angle latitude, Angle longitude, double elevation, int u, int v)
		{
			super(latitude, longitude, elevation);
			this.u = u;
			this.v = v;
		}

		public static PositionWithCoord fromDegrees(double latitude, double longitude, double elevation, int u, int v)
		{
			return new PositionWithCoord(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), elevation, u, v);
		}
	}
}
