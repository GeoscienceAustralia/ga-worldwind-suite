package au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Treats the retrieved image tiles as an elevation model, and applies a colour map based on the elevations
 * recorded in the retrieved tiles.
 * <p/>
 * <code>&lt;Delegate&gt;ColorMapReader(pixelType,byteOrder,missingData)&lt;/Delegate&gt;</code>
 * Where:
 * <ul>
 * 	<li>pixelType = the pixel format of the elevation tiles (one of "<code>Float32</code>", "<code>Int32</code>", "<code>Int16</code>" or "<code>Int8</code>")
 * 	<li>byteOrder = the byte order of the elevation tiles (one of "<code>little</code>" or "<code>big</code>")
 * 	<li>missingData = the value used in the elevation tiles to represent missing data (float)
 * </ul>
 * <p/>
 * When parsing from a layer definition file, the colour map must be provided in the layer xml, as follows:
 * <pre>
 * &lt;ColorMap interpolateHue="true"&gt;
 *   &lt;Entry elevation="e" red="r" green="g" blue="b" alpha="a"/&gt;
 *   ...
 * &lt;/ColorMap&gt;
 * </pre>
 * Where:
 * <ul>
 * 	<li>interpolateHue = if true, will use the HSB colour space and interpolate between elevations using hue. If false, will
 *      use the RGB colour space and interpolate each channel.
 *  <li>elevation = the elevation at which this colour map entry applies (float in metres)
 *  <li>red = the red channel of the colour entry (integer in range [0, 255])
 *  <li>green = the green channel of the colour entry (integer in range [0, 255])
 *  <li>blue = the blue channel of the colour entry (integer in range [0, 255])
 *  <li>alpha = the alpha channel of the colour entry (integer in range [0, 255])
 * </ul>
 */
public class ColorMapElevationImageReaderDelegate extends ElevationImageReaderDelegate
{
	private final static String DEFINITION_STRING = "ColorMapReader";
	private final static double EPSILON = 1e-10;

	private final boolean useHueInterpolation;
	private final NavigableMap<Double, Color> colorMap;

	@SuppressWarnings("unused")
	private ColorMapElevationImageReaderDelegate()
	{
		this(AVKey.INT16, AVKey.LITTLE_ENDIAN, -Double.MAX_VALUE, true, new TreeMap<Double, Color>());
	}

	public ColorMapElevationImageReaderDelegate(String pixelType, String byteOrder, Double missingDataSignal,
			boolean useUseInterpolation, NavigableMap<Double, Color> colorMap)
	{
		super(pixelType, byteOrder, missingDataSignal);
		this.useHueInterpolation = useUseInterpolation;
		this.colorMap = colorMap;
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

				boolean useHueInterpolation = true;
				NavigableMap<Double, Color> colorMap = new TreeMap<Double, Color>();
				if (layerElement != null)
				{
					XPath xpath = WWXML.makeXPath();
					useHueInterpolation = XMLUtil.getBoolean(layerElement, "ColorMap/@interpolateHue", true, xpath);
					parseColorMapXml(layerElement, colorMap, xpath);
				}

				return new ColorMapElevationImageReaderDelegate(WWXML.parseDataType(pixelType),
						WWXML.parseByteOrder(byteOrder), missingDataSignal, useHueInterpolation, colorMap);
			}
		}
		return null;
	}

	protected void parseColorMapXml(Element element, NavigableMap<Double, Color> colorMap, XPath xpath)
	{
		Element[] mapEntries = WWXML.getElements(element, "ColorMap/Entry", xpath);
		if (mapEntries != null)
		{
			for (Element entry : mapEntries)
			{
				Double elevation = WWXML.getDouble(entry, "@elevation", xpath);
				if (elevation == null)
					continue;

				//don't allow a duplicate key
				while (colorMap.containsKey(elevation))
					elevation += EPSILON;

				int red = XMLUtil.getInteger(entry, "@red", 0, xpath);
				int green = XMLUtil.getInteger(entry, "@green", 0, xpath);
				int blue = XMLUtil.getInteger(entry, "@blue", 0, xpath);
				int alpha = XMLUtil.getInteger(entry, "@alpha", 255, xpath);
				Color color = new Color(red, green, blue, alpha);
				colorMap.put(elevation, color);
			}
		}
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
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0, i = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++, i++)
			{
				double elevation = elevations.getDouble(i);
				if (elevation == missingDataSignal)
				{
					image.setRGB(x, y, 0);
				}
				else
				{
					Entry<Double, Color> lessEntry = colorMap.floorEntry(elevation);
					Entry<Double, Color> greaterEntry = colorMap.ceilingEntry(elevation);
					double mixer = 0;
					if (lessEntry != null && greaterEntry != null)
					{
						double window = greaterEntry.getKey() - lessEntry.getKey();
						if (window > 0)
						{
							mixer = (elevation - lessEntry.getKey()) / window;
						}
					}
					Color color0 = lessEntry == null ? null : lessEntry.getValue();
					Color color1 = greaterEntry == null ? null : greaterEntry.getValue();
					int rgba = interpolateColor(color0, color1, mixer, useHueInterpolation);
					image.setRGB(x, y, rgba);
				}
			}
		}

		return image;
	}

	protected static int interpolateColor(Color color0, Color color1, double mixer, boolean useHue)
	{
		if (color0 == null && color1 == null)
			return 0;
		if (color1 == null)
			return color0.getRGB();
		if (color0 == null)
			return color1.getRGB();
		if (mixer <= 0d)
			return color0.getRGB();
		if (mixer >= 1d)
			return color1.getRGB();

		if (useHue)
		{
			float[] hsb0 = Color.RGBtoHSB(color0.getRed(), color0.getGreen(), color0.getBlue(), null);
			float[] hsb1 = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
			float h0 = hsb0[0];
			float h1 = hsb1[0];

			if (h1 < h0)
				h1 += 1f;
			if (h1 - h0 > 0.5f)
				h0 += 1f;

			float h = interpolateFloat(h0, h1, mixer);
			float s = interpolateFloat(hsb0[1], hsb1[1], mixer);
			float b = interpolateFloat(hsb0[2], hsb1[2], mixer);
			int alpha = interpolateInt(color0.getAlpha(), color1.getAlpha(), mixer);
			Color color = Color.getHSBColor(h, s, b);
			return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
		}
		else
		{
			int r = interpolateInt(color0.getRed(), color1.getRed(), mixer);
			int g = interpolateInt(color0.getGreen(), color1.getGreen(), mixer);
			int b = interpolateInt(color0.getBlue(), color1.getBlue(), mixer);
			int a = interpolateInt(color0.getAlpha(), color1.getAlpha(), mixer);
			return new Color(r, g, b, a).getRGB();
		}
	}

	protected static int interpolateInt(int i0, int i1, double mixer)
	{
		return (int) Math.round(i0 * (1d - mixer) + i1 * mixer);
	}

	protected static float interpolateFloat(float f0, float f1, double mixer)
	{
		return (float) (f0 * (1d - mixer) + f1 * mixer);
	}
}
