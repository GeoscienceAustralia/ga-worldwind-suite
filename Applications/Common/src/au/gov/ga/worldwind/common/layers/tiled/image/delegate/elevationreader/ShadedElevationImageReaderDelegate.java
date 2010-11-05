package au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWXML;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.tiled.image.delegate.Delegate;

public class ShadedElevationImageReaderDelegate extends ElevationImageReaderDelegate
{
	public static void main(String[] args) throws Exception
	{
		URL url =
				new URL(
						"file:/C:/data/LocalCopy/Java/WorldWind/Applications/Common/197_934.bil");
		AVList params = new AVListImpl();
		params.setValue(AVKey.TILE_WIDTH, 150);
		params.setValue(AVKey.TILE_HEIGHT, 150);
		params.setValue(AVKey.LEVEL_NAME, "1");
		params.setValue(AVKey.LEVEL_NUMBER, 1);
		params.setValue(AVKey.TILE_DELTA, LatLon.fromDegrees(36, 36));
		params.setValue(AVKey.DATA_CACHE_NAME, "temp");
		params.setValue(AVKey.TILE_URL_BUILDER, new TileUrlBuilder()
		{
			@Override
			public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
			{
				return null;
			}
		});
		params.setValue(AVKey.EXPIRY_TIME, 10l);
		params.setValue(AVKey.DATASET_NAME, "temp");
		params.setValue(AVKey.FORMAT_SUFFIX, ".bil");
		
		Level level = new Level(params);
		TextureTile tile = new TextureTile(Sector.fromDegrees(0, 1, 0, 1), level, 0, 0);
		ShadedElevationImageReaderDelegate reader =
				new ShadedElevationImageReaderDelegate(AVKey.INT16, AVKey.LITTLE_ENDIAN, -32768, 100, new Vec4(-0.7,
						0.7, -1));
		Globe globe = new Earth();
		BufferedImage image = reader.readImage(tile, url, globe);
		ImageIO.write(image, "png", new File("shaded.png"));
		
		
		
		ByteBuffer byteBuffer = WWIO.readURLContentToBuffer(url);

		int width = tile.getWidth();
		int height = tile.getHeight();

		// Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
		AVList bufferParams = new AVListImpl();
		bufferParams.setValue(AVKey.DATA_TYPE, AVKey.INT16);
		bufferParams.setValue(AVKey.BYTE_ORDER, AVKey.LITTLE_ENDIAN);
		BufferWrapper elevations = BufferWrapper.wrap(byteBuffer, bufferParams);
		
		BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		double[] minmax = reader.getMinMax(elevations, -32768);
		for (int y = 0, i = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++, i++)
			{
				double elevation = elevations.getDouble(i);
				double percent = (elevation - minmax[0]) / (minmax[1] - minmax[0]);
				percent = (percent * 10) % 1; 
			
				int r = (int) (255.0 * percent);
				int g = (int) (255.0 * percent);
				int b = (int) (255.0 * percent);

				int argb = (0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
				image2.setRGB(x, y, argb);
			}
		}
		
		ImageIO.write(image2, "png", new File("elevations.png"));
	}

	private final static String DEFINITION_STRING = "ShadedElevationReader";

	protected final double exaggeration;
	protected final Vec4 sunPosition;

	@SuppressWarnings("unused")
	private ShadedElevationImageReaderDelegate()
	{
		this(AVKey.INT16, AVKey.LITTLE_ENDIAN, -Double.MAX_VALUE, 10, new Vec4(-1, 1, -1).normalize3());
	}

	public ShadedElevationImageReaderDelegate(String pixelType, String byteOrder, double missingDataSignal,
			double exaggeration, Vec4 sunPosition)
	{
		super(pixelType, byteOrder, missingDataSignal);
		this.exaggeration = exaggeration;
		this.sunPosition = sunPosition;
	}

	@Override
	public Delegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern =
					Pattern.compile("(?:\\((\\w+),(\\w+)," + doublePattern + ",\\(" + doublePattern + ","
							+ doublePattern + "," + doublePattern + "\\)," + doublePattern + "\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				String pixelType = matcher.group(1);
				String byteOrder = matcher.group(2);
				double missingDataSignal = Double.parseDouble(matcher.group(3));
				double sunPositionX = Double.parseDouble(matcher.group(4));
				double sunPositionY = Double.parseDouble(matcher.group(5));
				double sunPositionZ = Double.parseDouble(matcher.group(6));
				double exaggeration = Double.parseDouble(matcher.group(7));
				Vec4 sunPosition = new Vec4(sunPositionX, sunPositionY, sunPositionZ).normalize3();

				return new ShadedElevationImageReaderDelegate(WWXML.parseDataType(pixelType),
						WWXML.parseByteOrder(byteOrder), missingDataSignal, exaggeration, sunPosition);
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + WWXML.dataTypeAsText(pixelType) + "," + WWXML.byteOrderAsText(byteOrder) + ","
				+ missingDataSignal + ",(" + sunPosition.x + "," + sunPosition.y + "," + sunPosition.z + "),"
				+ exaggeration + ")";
	}

	@Override
	protected BufferedImage generateImage(BufferWrapper elevations, int width, int height, Globe globe, Sector sector)
	{
		//image array has one less in width and height than verts array:
		//this is because edge verts are shared between adjacent tiles

		BufferedImage image = new BufferedImage(width - 1, height - 1, BufferedImage.TYPE_INT_RGB);

		Vec4[] verts =
				calculateTileVerts(width, height, sector, elevations, missingDataSignal, exaggeration * 0.000005);
		Vec4[] normals = calculateNormals(width, height, verts);

		for (int y = 0, i = 0; y < height - 1; y++)
		{
			for (int x = 0; x < width - 1; x++, i++)
			{
				Vec4 normal = normals[i];
				if (normal == null)
					normal = Vec4.ZERO;

				double light = Math.max(0d, normal.dot3(sunPosition));

				int r = (int) (255.0 * light);
				int g = (int) (255.0 * light);
				int b = (int) (255.0 * light);

				int argb = (0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
				image.setRGB(x, y, argb);
			}
		}

		return image;
	}

	protected Vec4[] calculateTileVerts(int width, int height, Sector sector, BufferWrapper elevations,
			double missingDataSignal, double exaggeration)
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
				//only add if lat/lon is inside level's sector
				//if (this.getLevels().getSector().contains(lat, lon))
				{
					double elevation = elevations.getDouble(i);
					if (elevation != missingDataSignal)
					{
						verts[i] = new Vec4(lat.degrees, lon.degrees, elevation * exaggeration);
					}
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
				//v0-v1
				//|
				//v2

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

	protected double[] getMinMax(BufferWrapper elevations, double missingDataSignal)
	{
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < elevations.length(); i++)
		{
			double value = elevations.getDouble(i);
			if (value != missingDataSignal)
			{
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
		}

		return new double[] { min, max };
	}
}
