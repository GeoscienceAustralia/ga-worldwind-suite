package au.gov.ga.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.HSLColor;
import au.gov.ga.worldwind.common.util.Validate;

import com.sun.opengl.util.BufferUtil;

/**
 * {@link GocadReader} implementation for reading Voxet GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadVoxetReader implements GocadReader
{
	public final static String HEADER_REGEX = "(?i).*voxet.*";

	private final static Pattern axisPattern = Pattern
			.compile("AXIS_(\\S+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+).*");
	private final static Pattern propertyPattern = Pattern.compile("PROP_(\\S+)\\s+(\\d+)\\s+(\\S+).*");
	private final static Pattern namePattern = Pattern.compile("name:\\s*(.*)\\s*");

	private String name;

	private Vec4 axisO;
	private Vec4 axisU;
	private Vec4 axisV;
	private Vec4 axisW;
	private Vec4 axisMIN;
	private Vec4 axisMAX;
	private Vec4 axisN;
	private Vec4 axisD;

	private Double noDataValue = null;
	private int esize = 4;
	private String etype = "IEEE";
	private String format = "RAW";
	private int offset = 0;
	private String file;

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
	}

	private void parseAxis(Matcher matcher)
	{
		String type = matcher.group(1);
		double d0 = Double.parseDouble(matcher.group(2));
		double d1 = Double.parseDouble(matcher.group(3));
		double d2 = Double.parseDouble(matcher.group(4));
		Vec4 v = new Vec4(d0, d1, d2);

		if (type.equals("O"))
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
			axisN = v;
		}
		else if (type.equals("D"))
		{
			axisD = v;
		}
	}

	private void parseProperty(Matcher matcher)
	{
		String type = matcher.group(1);
		String value = matcher.group(3);

		if (type.equals("NO_DATA_VALUE"))
		{
			noDataValue = Double.parseDouble(value);
		}
		else if (type.equals("ESIZE"))
		{
			esize = Integer.parseInt(value);
		}
		/*else if (type.equals("SIGNED"))
		{
			signed = Integer.parseInt(value) != 0;
		}*/
		else if (type.equals("ETYPE"))
		{
			etype = value;
		}
		else if (type.equals("FORMAT"))
		{
			format = value;
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
			if (axisD == null || axisMIN == null || axisMAX == null)
			{
				return null;
			}

			double nx = (axisMAX.x - axisMIN.x) / axisD.x + 1;
			double ny = (axisMAX.y - axisMIN.y) / axisD.y + 1;
			double nz = (axisMAX.z - axisMIN.z) / axisD.z + 1;
			axisN = new Vec4(nx, ny, nz);
		}

		long nu = Math.round(axisN.x), nv = Math.round(axisN.y), nw = Math.round(axisN.z);
		Vec4 axisUStride = axisU.multiply3((axisMAX.x - axisMIN.x) / (axisN.x - 1));
		Vec4 axisVStride = axisV.multiply3((axisMAX.y - axisMIN.y) / (axisN.y - 1));
		Vec4 axisWStride = axisW.multiply3((axisMAX.z - axisMIN.z) / (axisN.z - 1));
		Vec4 axisUOrigin = axisU.multiply3(axisMIN.x);
		Vec4 axisVOrigin = axisV.multiply3(axisMIN.y);
		Vec4 axisWOrigin = axisW.multiply3(axisMIN.z);
		Vec4 origin =
				new Vec4(axisO.x + axisUOrigin.x + axisVOrigin.x + axisWOrigin.x, axisO.y + axisUOrigin.y
						+ axisVOrigin.y + axisWOrigin.y, axisO.z + axisUOrigin.z + axisVOrigin.z + axisWOrigin.z);

		Validate.isTrue(esize == 4, "Unsupported PROP_ESIZE value: " + esize); //TODO support "1"?
		Validate.isTrue("RAW".equals(format), "Unsupported PROP_FORMAT value: " + format); //TODO support "SEGY"?
		Validate.isTrue("IBM".equals(etype) || "IEEE".equals(etype), "Unsupported PROP_ETYPE value: " + format);

		int strideU = parameters.getVoxetSubsamplingU();
		int strideV = parameters.getVoxetSubsamplingV();
		int strideW = parameters.getVoxetSubsamplingW();

		if (parameters.isVoxetDynamicSubsampling())
		{
			float samplesPerAxis = parameters.getVoxetDynamicSubsamplingSamplesPerAxis();
			strideU = Math.max(1, Math.round((float) axisN.x / samplesPerAxis));
			strideV = Math.max(1, Math.round((float) axisN.y / samplesPerAxis));
			strideW = Math.max(1, Math.round((float) axisN.z / samplesPerAxis));
		}

		int uSamples = (int) (1 + (nu - 1) / strideU);
		int vSamples = (int) (1 + (nv - 1) / strideV);
		int wSamples = (int) (1 + (nw - 1) / strideW);

		List<Position> positions = new ArrayList<Position>();
		float[] values = new float[uSamples * vSamples * wSamples];
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

			if (parameters.isVoxetBilinearMinification())
			{
				//contains the number of values summed
				int[] count = new int[values.length];

				//read all the values, and sum them in regions
				for (int w = 0; w < nw; w++)
				{
					int wRegion = (w / strideW) * vSamples * uSamples;
					for (int v = 0; v < nv; v++)
					{
						int vRegion = (v / strideV) * uSamples;
						for (int u = 0; u < nu; u++)
						{
							float value = readNextFloat(is, parameters.getByteOrder(), ieee);
							if (!Float.isNaN(value) && value != noDataValue)
							{
								int uRegion = (u / strideU);
								int valueIndex = wRegion + vRegion + uRegion;

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
				for (int w = 0, wi = 0; w < nw; w += strideW, wi++)
				{
					int wOffset = wi * vSamples * uSamples;
					Vec4 wAdd = axisWStride.multiply3(w);
					for (int v = 0, vi = 0; v < nv; v += strideV, vi++)
					{
						int vOffset = vi * uSamples;
						Vec4 vAdd = axisVStride.multiply3(v);
						for (int u = 0, ui = 0; u < nu; u += strideU, ui++)
						{
							int uOffset = ui;
							int valueIndex = wOffset + vOffset + uOffset;
							float value = values[valueIndex];

							if (!Float.isNaN(value))
							{
								Vec4 uAdd = axisUStride.multiply3(u);
								Vec4 p =
										new Vec4(origin.x + uAdd.x + vAdd.x + wAdd.x, origin.y + uAdd.y + vAdd.y
												+ wAdd.y, origin.z + uAdd.z + vAdd.z + wAdd.z);
								if (transformation != null)
								{
									transformation.TransformPoint(transformed, p.x, p.y, 0);
									positions.add(Position.fromDegrees(transformed[1], transformed[0], p.z));
								}
								else
								{
									positions.add(Position.fromDegrees(p.y, p.x, p.z));
								}
							}
						}
					}
				}
			}
			else
			{
				//non-bilinear is simple; we can skip over any input values that don't contribute to the points
				int valueIndex = 0;
				for (int w = 0; w < nw; w += strideW)
				{
					Vec4 wAdd = axisWStride.multiply3(w);
					for (int v = 0; v < nv; v += strideV)
					{
						Vec4 vAdd = axisVStride.multiply3(v);
						for (int u = 0; u < nu; u += strideU)
						{
							float value = readNextFloat(is, parameters.getByteOrder(), ieee);
							if (!Float.isNaN(value) && value != noDataValue)
							{
								values[valueIndex++] = value;
								min = Math.min(min, value);
								max = Math.max(max, value);

								Vec4 uAdd = axisUStride.multiply3(u);
								Vec4 p =
										new Vec4(origin.x + uAdd.x + vAdd.x + wAdd.x, origin.y + uAdd.y + vAdd.y
												+ wAdd.y, origin.z + uAdd.z + vAdd.z + wAdd.z);
								if (transformation != null)
								{
									transformation.TransformPoint(transformed, p.x, p.y, 0);
									positions.add(Position.fromDegrees(transformed[1], transformed[0], p.z));
								}
								else
								{
									positions.add(Position.fromDegrees(p.y, p.x, p.z));
								}
							}
							skipBytes(is, esize * Math.min(strideU - 1, nu - u - 1));
						}
						skipBytes(is, esize * nu * Math.min(strideV - 1, nv - v - 1));
					}
					skipBytes(is, esize * nu * nv * Math.min(strideW - 1, nw - w - 1));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		//create a color buffer containing a color for each point
		int colorBufferElementSize = parameters.isVoxetAlphaFromValue() ? 4 : 3;
		FloatBuffer colorBuffer = BufferUtil.newFloatBuffer(positions.size() * colorBufferElementSize);
		for (float value : values)
		{
			//check that this value is valid; only non-NaN floats have points associated
			if (!Float.isNaN(value))
			{
				float percent = (value - min) / (max - min);
				HSLColor hsl = new HSLColor((1f - percent) * 300f, 100f, 50f);
				Color color = hsl.getRGB();
				colorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f);
				if (parameters.isVoxetAlphaFromValue())
				{
					colorBuffer.put(percent);
				}
			}
		}

		if (name == null)
		{
			name = "Voxet";
		}

		FastShape shape = new FastShape(positions, GL.GL_POINTS);
		shape.setName(name);
		shape.setColorBuffer(colorBuffer);
		shape.setColorBufferElementSize(colorBufferElementSize);
		shape.setForceSortedPrimitives(true);
		shape.setFollowTerrain(true);
		return shape;
	}

	private static void skipBytes(InputStream is, long n) throws IOException
	{
		while (n > 0)
		{
			long skipped = is.skip(n);
			if (skipped < 0)
			{
				throw new IOException("Error skipping in InputStream");
			}
			n -= skipped;
		}
	}

	private static float readNextFloat(InputStream is, ByteOrder byteOrder, boolean ieee) throws IOException
	{
		int b0, b1, b2, b3;
		if (byteOrder == ByteOrder.LITTLE_ENDIAN)
		{
			b3 = is.read();
			b2 = is.read();
			b1 = is.read();
			b0 = is.read();
		}
		else
		{
			b0 = is.read();
			b1 = is.read();
			b2 = is.read();
			b3 = is.read();
		}
		return bytesToFloat(b0, b1, b2, b3, ieee);
	}

	private static float bytesToFloat(int b0, int b1, int b2, int b3, boolean ieee)
	{
		if (ieee)
		{
			return Float.intBitsToFloat((b0) | (b1 << 8) | (b2 << 16) | b3 << 24);
		}
		else
		{
			//ibm
			byte S = (byte) ((b3 & 0x80) >> 7);
			int E = (b3 & 0x7f);
			long F = (b2 << 16) + (b1 << 8) + b0;

			if (S == 0 && E == 0 && F == 0)
				return 0;

			double A = 16.0;
			double B = 64.0;
			double e24 = 16777216.0; // 2^24
			double M = (double) F / e24;

			double F1 = S == 0 ? 1.0 : -1.0;
			return (float) (F1 * M * Math.pow(A, E - B));
		}
	}


}
