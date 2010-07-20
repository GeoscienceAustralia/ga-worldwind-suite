package au.gov.ga.worldwind.tiler.application;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconst;

import au.gov.ga.worldwind.tiler.application.CmdLineParser.IllegalOptionValueException;
import au.gov.ga.worldwind.tiler.application.CmdLineParser.Option;
import au.gov.ga.worldwind.tiler.application.CmdLineParser.OptionException;
import au.gov.ga.worldwind.tiler.application.Tiler.TilingType;
import au.gov.ga.worldwind.tiler.gdal.GDALUtil;
import au.gov.ga.worldwind.tiler.util.MinMaxArray;
import au.gov.ga.worldwind.tiler.util.NullableNumberArray;
import au.gov.ga.worldwind.tiler.util.NumberArray;
import au.gov.ga.worldwind.tiler.util.ProgressReporter;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.Util;


public class Console
{
	public final static String LOGGER = "ConsoleLogger";

	private static void printUsage()
	{
		String text =
				"Usage: [{-h,--help}] [{-i,--images}] [{-e,--elevations}] [{-p,--reproject}]\n"
						+ "       [{-z,--lzts} lzts] [{-t,--tilesize} size] [{-f,--format} {JPG|PNG}]\n"
						+ "       [{-d,--datatype} {BYTE|INT16|INT32|FLOAT32}] [{-a,--addalpha}]\n"
						+ "       [{-b,--band} band] [{-n,--nooverviews}] [{-l,--levels} levels]\n"
						+ "       [{-m,--nomagnification}] [{-g,--nominification}]\n"
						+ "       [{-o,--setoutside} \"value[,value...]]\"\n"
						+ "       [{-r,--replacevalues} \"min1[,min1...] max1[,max1...] min2[,min2...]\n"
						+ "                              max2[,max2...] with[,with...] else[,else...]\"\n"
						+ "       input_file output_directory\n"
						+ "\n"
						+ "General switches:\n"
						+ "  -h         Show this help\n"
						+ "  -i         Generate image tiles (default)\n"
						+ "  -e         Generate elevation tiles\n"
						+ "  -p         Reproject if required (if input dataset is not using WGS84 or\n"
						+ "             similar)\n"
						+ "  -z lzts    Level zero tile size in degrees (default: 36.0 for images, 20.0\n"
						+ "             for elevations)\n"
						+ "  -t size    Width and height of tiles (default: 512 for images, 150 for\n"
						+ "             elevations)\n"
						+ "  -n         Don't generate overviews\n"
						+ "  -l levels  Number of levels to generate (default: calculated optimal)\n"
						+ "  -m         Disable bilinear magnification (if the top level tiles generated\n"
						+ "             have a higher resolution than the dataset)\n"
						+ "  -g         Disable bilinear minification when generating overviews\n"
						+ "  -o \"...\"   Set values outside extends to (number of values must equal the\n"
						+ "             number of output bands, blanks permitted)\n"
						+ "  -r \"...\"   Replace values between (number of values in each group must\n"
						+ "             equal the number of output bands, blanks permitted)\n"
						+ "Image specific switches:\n"
						+ "  -f format  Image output format (default: JPG)\n"
						+ "  -a         Add alpha band to image tiles if input has no alpha band\n"
						+ "Elevation specific switches:\n"
						+ "  -d type    Elevation output format (default: INT16)\n"
						+ "  -b band    Band to read from for elevation data (default: 1)";
		System.out.println(text);
	}

	public static void main(String[] args)
	{
		try
		{
			Executable.setGDALEnvironmentVariables();
		}
		catch (FileNotFoundException e1)
		{
			System.err.println(e1.getMessage());
			System.exit(2);
		}
		catch (Exception e)
		{
			System.out.println("WARNING: " + e.getLocalizedMessage());
		}

		//-i --images
		//-e --elevations
		//-p --reproject
		//-t --tilesize 512
		//-z --lzts 36
		//-f --format JPG|PNG     (images)
		//-d --datatype BYTE|INT16|INT32|FLOAT32     (elevations)
		//-a --addalpha      (images)
		//-b --band 1      (elevations)
		//-n --nooverviews
		//-l --levels n
		//-m --nomagnification
		//-g --nominification
		//-s --setoutside n,n,n
		//-r --replacevalues "n,n,n n,n,n n,n,n n,n,n n,n,n n,n,n"

		CmdLineParser parser = new CmdLineParser();

		Option helpO = parser.addBooleanOption('h', "help");
		Option imagesO = parser.addBooleanOption('i', "images");
		Option elevationsO = parser.addBooleanOption('e', "elevations");
		Option reprojectO = parser.addBooleanOption('p', "reproject");
		Option tilesizeO = parser.addIntegerOption('t', "tilesize");
		Option lztsO = parser.addDoubleOption('z', "lzts");
		Option formatO = parser.addStringOption('f', "format");
		Option datatypeO = parser.addStringOption('d', "datatype");
		Option addalphaO = parser.addBooleanOption('a', "addalpha");
		Option bandO = parser.addIntegerOption('b', "band");
		Option nooverviewsO = parser.addBooleanOption('n', "nooverviews");
		Option levelsO = parser.addIntegerOption('l', "levels");
		Option bilinearO = parser.addBooleanOption('m', "nomagnification");
		Option bilinearOverviewsO = parser.addBooleanOption('g', "nominification");
		Option outsideO = new Option('o', "setoutside", true)
		{
			@Override
			protected Object parseValue(String arg, Locale locale)
					throws IllegalOptionValueException
			{
				return parseNumberArray(this, arg);
			}
		};
		parser.addOption(outsideO);
		Option replaceO = new Option('r', "replacevalues", true)
		{
			@Override
			protected Object parseValue(String arg, Locale locale)
					throws IllegalOptionValueException
			{
				return parseReplaceValues(this, arg);
			}
		};
		parser.addOption(replaceO);

		try
		{
			parser.parse(args);
		}
		catch (OptionException e)
		{
			exitWithMessage(e.getMessage());
		}

		Boolean help = (Boolean) parser.getOptionValue(helpO, false);
		String[] otherArgs = parser.getRemainingArgs();
		if (help || otherArgs.length < 1)
		{
			printUsage();
			System.exit(0);
		}
		else if (otherArgs.length < 2)
		{
			exitWithMessage("Output directory not defined");
		}

		String inputFile = otherArgs[0];
		String outputDir = otherArgs[1];

		File input = new File(inputFile);
		if (!input.exists())
		{
			exitWithMessage("File not found: " + inputFile);
		}

		File output = new File(outputDir);
		if (output.exists() && !output.isDirectory())
		{
			exitWithMessage("Not a directory: " + outputDir);
		}

		Boolean images = (Boolean) parser.getOptionValue(imagesO, true);
		Boolean elevations = (Boolean) parser.getOptionValue(elevationsO, false) && !images;
		Boolean reproject = (Boolean) parser.getOptionValue(reprojectO, false);
		Boolean nobilinear = (Boolean) parser.getOptionValue(bilinearO, false);
		Boolean nobilinearOverviews = (Boolean) parser.getOptionValue(bilinearOverviewsO, false);
		boolean bilinear = !nobilinear;
		boolean bilinearOverviews = !nobilinearOverviews;

		Integer tilesize = (Integer) parser.getOptionValue(tilesizeO, elevations ? 150 : 512);
		Double lzts = (Double) parser.getOptionValue(lztsO, elevations ? 20d : 36d);

		String imageFormat = ((String) parser.getOptionValue(formatO, "jpg")).toLowerCase();
		if (!(imageFormat.equals("jpg") || imageFormat.equals("png")))
		{
			exitWithMessage("Unknown image format: " + imageFormat.toUpperCase());
		}

		boolean isFloat = false;
		String dataType = ((String) parser.getOptionValue(datatypeO, "INT16")).toUpperCase();
		int bufferType = 0;
		if (dataType.equals("BYTE"))
		{
			bufferType = gdalconst.GDT_Byte;
		}
		else if (dataType.equals("INT16"))
		{
			bufferType = gdalconst.GDT_Int16;
		}
		else if (dataType.equals("INT32"))
		{
			bufferType = gdalconst.GDT_Int32;
		}
		else if (dataType.equals("FLOAT32"))
		{
			bufferType = gdalconst.GDT_Float32;
			isFloat = true;
		}
		else
		{
			exitWithMessage("Unknown data type: " + dataType);
		}

		Boolean addAlpha = (Boolean) parser.getOptionValue(addalphaO, false);
		Integer band = (Integer) parser.getOptionValue(bandO, 1);
		Boolean nooverviews = (Boolean) parser.getOptionValue(nooverviewsO, false);
		Integer levels = (Integer) parser.getOptionValue(levelsO);
		NullableNumberArray outside = (NullableNumberArray) parser.getOptionValue(outsideO);
		ReplaceValues replaces =
				(ReplaceValues) parser.getOptionValue(replaceO, new ReplaceValues());

		try
		{
			GDALUtil.init();
			Dataset dataset = GDALUtil.open(input);
			Sector sector = GDALUtil.getSector(dataset);
			if (levels == null)
				levels = Util.levelCount(dataset, lzts, sector, tilesize);
			int level = levels - 1;
			int bandCount = dataset.getRasterCount();
			if (addAlpha && bandCount == 3)
				bandCount = 4;

			if (outside != null && outside.length() != bandCount)
			{
				exitWithMessage("Outside value count (" + outside.length()
						+ ") doesn't equal output band count (" + bandCount + ")");
			}
			else if (replaces.replaceMinMaxs != null && replaces.valueCount != bandCount)
			{
				exitWithMessage("Replace group value count (" + replaces.valueCount
						+ ") doesn't equal output band count (" + bandCount + ")");
			}

			ConsoleProgressReporter reporter = new ConsoleProgressReporter();

			LogWriter logWriter = null;
			try
			{
				output.mkdirs();
				logWriter = new LogWriter(output);
				String infoText = GDALUtil.getInfoText(dataset, sector);
				String tileText = GDALUtil.getTileText(dataset, sector, lzts, levels, !nooverviews);

				if (elevations)
				{
					logWriter.startLog(TilingType.Elevations, input, output, sector, level,
							tilesize, lzts, imageFormat, addAlpha, band, bufferType, bilinear,
							reproject, infoText, tileText, outside, replaces.replaceMinMaxs,
							replaces.replace, replaces.otherwise, isFloat);

					NumberArray minMax = new NumberArray(2);
					Tiler.tileElevations(dataset, reproject, bilinear, sector, level, tilesize,
							lzts, bufferType, band, outside, replaces.replaceMinMaxs,
							replaces.replace, replaces.otherwise, minMax, output, reporter);
					if (!nooverviews)
					{
						Overviewer.createElevationOverviews(output, tilesize, tilesize, bufferType,
								ByteOrder.LITTLE_ENDIAN, outside, sector, lzts, bilinearOverviews,
								reporter);
					}
					logWriter.logMinMax(minMax, isFloat);
				}
				else
				{
					logWriter.startLog(TilingType.Images, input, output, sector, level, tilesize,
							lzts, imageFormat, addAlpha, band, bufferType, bilinear, reproject,
							infoText, tileText, outside, replaces.replaceMinMaxs, replaces.replace,
							replaces.otherwise, isFloat);

					Tiler.tileImages(dataset, reproject, bilinear, sector, level, tilesize, lzts,
							imageFormat, addAlpha, outside, replaces.replaceMinMaxs,
							replaces.replace, replaces.otherwise, output, reporter);
					if (!nooverviews)
					{
						Overviewer.createImageOverviews(output, imageFormat, tilesize, tilesize,
								outside, sector, lzts, bilinearOverviews, reporter);
					}
				}
			}
			finally
			{
				if (logWriter != null)
					try
					{
						logWriter.finishLog();
					}
					catch (IOException e)
					{
					}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void exitWithMessage(String message)
	{
		System.err.println(message);
		System.exit(2);
	}

	private static NullableNumberArray parseNumberArray(Option option, String arg)
			throws IllegalOptionValueException
	{
		try
		{
			String[] split = (arg + " ").split(","); //split doesn't work if there are no values
			Double[] d = stringsToDoubles(split);
			NullableNumberArray n = new NullableNumberArray(d.length);
			n.setDoubles(d);
			return n;
		}
		catch (Exception e)
		{
			throw new IllegalOptionValueException(option, arg);
		}
	}

	private static ReplaceValues parseReplaceValues(Option option, String arg)
			throws IllegalOptionValueException
	{
		ReplaceValues replaceValues = new ReplaceValues();
		replaceValues.replaceMinMaxs = new MinMaxArray[2];

		String[] groups = arg.split(" ");
		if (groups.length != 6)
			throw new IllegalOptionValueException(option, arg);

		for (int i = 0; i < groups.length; i++)
			groups[i] = groups[i] + " ";

		String[] min1 = groups[0].split(",");
		String[] max1 = groups[1].split(",");
		String[] min2 = groups[2].split(",");
		String[] max2 = groups[3].split(",");
		String[] replace = groups[4].split(",");
		String[] otherwise = groups[5].split(",");

		if (!(min1.length == max1.length && min1.length == min2.length
				&& min1.length == max2.length && min1.length == replace.length && min1.length == otherwise.length))
			throw new IllegalOptionValueException(option, arg);

		int length = min1.length;
		replaceValues.valueCount = length;
		replaceValues.replaceMinMaxs[0] = new MinMaxArray(length);
		replaceValues.replaceMinMaxs[1] = new MinMaxArray(length);
		replaceValues.replace = new NullableNumberArray(length);
		replaceValues.otherwise = new NullableNumberArray(length);

		try
		{
			Double[] min1D = stringsToDoubles(min1);
			Double[] max1D = stringsToDoubles(max1);
			Double[] min2D = stringsToDoubles(min2);
			Double[] max2D = stringsToDoubles(max2);
			Double[] replaceD = stringsToDoubles(replace);
			Double[] otherwiseD = stringsToDoubles(otherwise);

			replaceValues.replaceMinMaxs[0].setMinMaxDoubles(min1D, max1D);
			replaceValues.replaceMinMaxs[1].setMinMaxDoubles(min2D, max2D);
			replaceValues.replace.setDoubles(replaceD);
			replaceValues.otherwise.setDoubles(otherwiseD);
		}
		catch (Exception e)
		{
			throw new IllegalOptionValueException(option, arg);
		}

		return replaceValues;
	}

	private static Double[] stringsToDoubles(String[] s)
	{
		Double[] d = new Double[s.length];
		for (int i = 0; i < s.length; i++)
		{
			String t = s[i].trim();
			if (t.length() == 0)
			{
				d[i] = null;
			}
			else
			{
				d[i] = Double.parseDouble(t);
			}
		}
		return d;
	}

	private static class ReplaceValues
	{
		public int valueCount;
		public MinMaxArray[] replaceMinMaxs;
		public NullableNumberArray replace;
		public NullableNumberArray otherwise;
	}

	public static class ConsoleProgressReporter implements ProgressReporter
	{
		private Logger logger;
		private int oldforty = -1;
		private int forty = 0;
		private boolean needsNewLine = false;

		public ConsoleProgressReporter()
		{
			logger = new ConsoleLogger(LOGGER);
		}

		@Override
		public void cancel()
		{
		}

		@Override
		public void done()
		{
		}

		@Override
		public Logger getLogger()
		{
			return logger;
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

		@Override
		public void progress(double percent)
		{
			forty = (int) (percent * 40);
			if (forty < oldforty)
			{
				oldforty = -1;
			}
			if (oldforty != forty)
			{
				for (int i = oldforty + 1; i <= forty; i++)
				{
					if (i % 4 == 0)
						System.out.print((i * 2 + i / 2));
					else
						System.out.print(".");
					needsNewLine = true;
				}
				oldforty = forty;
			}
		}

		public void printNewLineIfNeeded()
		{
			if (needsNewLine)
			{
				System.out.println();
				oldforty = -1;
				needsNewLine = false;
			}
		}

		private class ConsoleLogger extends Logger
		{
			public ConsoleLogger(String name)
			{
				super(name, null);
			}

			@Override
			public void log(LogRecord record)
			{
				String level = record.getLevel().getName();
				String msg = record.getMessage();
				String line = msg == null ? "Unknown" : msg;
				DateFormat df = new SimpleDateFormat("[HH:mm:ss]");
				String prefix = df.format(new Date()) + " - " + level + " - ";
				String text = prefix + line;
				if (record.getThrown() != null)
					text += ": " + record.getThrown();

				if (record.getLevel() == Level.SEVERE)
				{
					printNewLineIfNeeded();
					System.err.println(text);
				}
				else
				{
					printNewLineIfNeeded();
					System.out.println(text);
				}
			}
		}
	}
}
