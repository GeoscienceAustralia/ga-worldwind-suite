package au.gov.ga.worldwind.common.layers.tiled.image.elevation;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.WWXML;

import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader.ElevationImageReaderDelegate;

/**
 * Treats retrieved image tiles as elevation data and calculates and displays the normal map
 * for the elevation model mesh that would be generated from the elevation data.
 * <p/>
 * <code>&lt;Delegate&gt;NormalMapReader(pixelType,byteOrder,missingData)&lt;/Delegate&gt;</code>
 * Where:
 * <ul>
 * 	<li>pixelType = the pixel format of the elevation tiles (one of "<code>Float32</code>", "<code>Int32</code>", "<code>Int16</code>" or "<code>Int8</code>")
 * 	<li>byteOrder = the byte order of the elevation tiles (one of "<code>little</code>" or "<code>big</code>")
 * 	<li>missingData = the value used in the elevation tiles to represent missing data (float)
 * </ul>
 */
public class NormalMapImageReaderDelegate extends ElevationImageReaderDelegate
{
	private final static String DEFINITION_STRING = "NormalMapReader";

	//TODO get from constructor or layer
	protected final double bakedExaggeration = 100.0;
	protected final double minElevationClamp = -Double.MAX_VALUE;
	protected final double maxElevationClamp = Double.MAX_VALUE;

	@SuppressWarnings("unused")
	private NormalMapImageReaderDelegate()
	{
		this(AVKey.INT16, AVKey.LITTLE_ENDIAN, -Double.MAX_VALUE);
	}

	public NormalMapImageReaderDelegate(String pixelType, String byteOrder, Double missingDataSignal)
	{
		super(pixelType, byteOrder, missingDataSignal);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\((\\w+),(\\w+)," + doublePattern + "\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				String pixelType = matcher.group(1);
				String byteOrder = matcher.group(2);
				double missingDataSignal = Double.parseDouble(matcher.group(3));
				return new NormalMapImageReaderDelegate(WWXML.parseDataType(pixelType),
						WWXML.parseByteOrder(byteOrder), missingDataSignal);
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + WWXML.dataTypeAsText(pixelType) + "," + WWXML.byteOrderAsText(byteOrder) + ","
				+ missingDataSignal + ")";
	}

	@Override
	protected BufferedImage generateImage(BufferWrapper elevations, int width, int height, Globe globe, Sector sector)
	{
		//normals array has one less in width and height than verts array:
		//this is because edge verts are shared between adjacent tiles

		BufferedImage image = new BufferedImage(width - 1, height - 1, BufferedImage.TYPE_INT_RGB);

		Vec4[] verts = calculateTileVerts(width,
				height,
				globe,
				sector,
				elevations,
				missingDataSignal,
				bakedExaggeration);
		Vec4[] normals = calculateNormals(width, height, verts);

		for (int y = 0, i = 0; y < height - 1; y++)
		{
			for (int x = 0; x < width - 1; x++, i++)
			{
				Vec4 normal = normals[i];
				if (normal == null)
				{
					normal = Vec4.ZERO;
				}

				int r = (int) (255.0 * (normal.x + 1) / 2);
				int g = (int) (255.0 * (normal.y + 1) / 2);
				int b = (int) (255.0 * (normal.z + 1) / 2);

				int argb = (0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
				image.setRGB(x, y, argb);
			}
		}

		return image;
	}

	protected Vec4[] calculateTileVerts(int width, int height, Globe globe, Sector sector, BufferWrapper elevations,
			double missingDataSignal, double bakedExaggeration)
	{
		Vec4[] verts = new Vec4[width * height];
		double dlon = sector.getDeltaLonDegrees() / width;
		double dlat = sector.getDeltaLatDegrees() / height;

		for (int y = 0, i = 0; y < height; y++)
		{
			Angle lat = sector.getMaxLatitude().subtractDegrees(dlat * y);
			for (int x = 0; x < width; x++, i++)
			{
				Angle lon = sector.getMinLongitude().addDegrees(dlon * x);
				double elevation = elevations.getDouble(i);
				if (elevation != missingDataSignal && elevation >= minElevationClamp && elevation <= maxElevationClamp)
				{
					verts[i] = globe.computePointFromPosition(lat, lon, elevation * bakedExaggeration);
				}
			}
		}

		return verts;
	}

	protected Vec4[] calculateNormals(int width, int height, Vec4[] verts)
	{
		Vec4[] norms = new Vec4[(width - 1) * (height - 1)];
		for (int y = 0, i = 0; y < height - 1; y++)
		{
			for (int x = 0; x < width - 1; x++, i++)
			{
				int vertIndex = width * y + x;
				Vec4 v0 = verts[vertIndex];
				if (v0 != null)
				{
					Vec4 v1 = verts[vertIndex + 1];
					Vec4 v2 = verts[vertIndex + width];

					norms[i] = v1 != null && v2 != null ? v1.subtract3(v0).cross3(v0.subtract3(v2)).normalize3() : null;
				}
			}
		}
		return norms;
	}
}
