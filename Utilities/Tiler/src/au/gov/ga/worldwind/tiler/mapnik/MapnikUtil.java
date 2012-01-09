package au.gov.ga.worldwind.tiler.mapnik;

import java.io.File;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.gov.ga.worldwind.tiler.util.InputStreamHandler;
import au.gov.ga.worldwind.tiler.util.Prefs;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.TilerException;


public class MapnikUtil
{
	private final static Preferences preferences = Prefs.getPreferences();

	private final static String PYTHON_KEY = "Python Binary";
	private final static String MAPNIK_KEY = "Mapnik Script";

	public static String getPythonBinary()
	{
		return preferences.get(PYTHON_KEY, null);
	}

	public static String getMapnikScript()
	{
		return preferences.get(MAPNIK_KEY, null);
	}

	public static void setPythonBinary(String value)
	{
		preferences.put(PYTHON_KEY, value);
	}

	public static void setMapnikScript(String value)
	{
		preferences.put(MAPNIK_KEY, value);
	}

	private static File getPythonBinaryFile() throws TilerException
	{
		String str = getPythonBinary();
		File file = str == null ? null : new File(str);
		if (file == null || !file.isFile())
		{
			throw new TilerException("Python binary setting is incorrect");
		}
		return file;
	}

	private static File getMapnikScriptFile() throws TilerException
	{
		String str = getMapnikScript();
		File file = str == null ? null : new File(str);
		if (file == null || !file.isFile())
		{
			throw new TilerException("Mapnik script setting is incorrect");
		}
		return file;
	}

	public static Sector getSector(File input) throws TilerException
	{
		File python = getPythonBinaryFile();
		File mapnik = getMapnikScriptFile();
		String command =
				"\"" + python.getAbsolutePath() + "\" \"" + mapnik.getAbsolutePath() + "\" \""
						+ input.getAbsolutePath() + "\" -v -n --no-color";
		final StringBuilder sb = new StringBuilder();
		final StringBuilder eb = new StringBuilder();
		try
		{
			Process process = Runtime.getRuntime().exec(command);
			new InputStreamHandler(process.getInputStream())
			{
				@Override
				public void handle(String string)
				{
					sb.append(string);
				}
			};
			new InputStreamHandler(process.getErrorStream())
			{
				@Override
				public void handle(String string)
				{
					eb.append(string);
				}
			};
			process.waitFor();
		}
		catch (Exception e)
		{
			throw new TilerException(e.getLocalizedMessage());
		}

		//unfortunately new version of nik2img prints everything to stderr, so can't do the following
		/*String error = eb.toString();
		if (error.length() > 0)
			throw new TilerException(error);*/

		String output = sb.toString() + eb.toString();
		Pattern pattern = Pattern.compile("Step.+Map long/lat bbox.+\\(.+\\)");
		Matcher matcher = pattern.matcher(output);
		if (matcher.find())
		{
			String found = matcher.group();
			pattern = Pattern.compile("-?[\\d\\.]+,-?[\\d\\.]+,-?[\\d\\.]+,-?[\\d\\.]+");
			matcher = pattern.matcher(found);
			if (matcher.find())
			{
				found = matcher.group();
				found = found.replace(" ", "");
				String[] split = found.split(",");
				try
				{
					double minlon = Double.parseDouble(split[0]);
					double minlat = Double.parseDouble(split[1]);
					double maxlon = Double.parseDouble(split[2]);
					double maxlat = Double.parseDouble(split[3]);
					return new Sector(minlat, minlon, maxlat, maxlon);
				}
				catch (Exception e)
				{
				}
			}
		}

		return null;
	}

	public static void tile(Sector sector, int width, int height, boolean ignoreBlank, boolean reproject, File input, File dst,
			final Logger logger) throws TilerException
	{
		File python = getPythonBinaryFile();
		File mapnik = getMapnikScriptFile();

		String format = dst.getName().toLowerCase().endsWith("jpg") ? "jpeg" : "png";
		String command =
				"\"" + python.getAbsolutePath() + "\" \"" + mapnik.getAbsolutePath() + "\" \""
						+ input.getAbsolutePath() + "\" \"" + dst.getAbsolutePath() + "\" -f " + format + " -b "
						+ sector.getMinLongitude() + " " + sector.getMinLatitude() + " " + sector.getMaxLongitude()
						+ " " + sector.getMaxLatitude() + " -d " + width + " " + height + " --no-open --no-color";
		if(reproject)
		{
			command += " -s epsg:4326";
		}
		
		try
		{
			Process process = Runtime.getRuntime().exec(command);

			new InputStreamHandler(process.getInputStream())
			{
				@Override
				public void handle(String string)
				{
					logger.finer(string);
				}
			};
			new InputStreamHandler(process.getErrorStream())
			{
				@Override
				public void handle(String string)
				{
					logger.severe(string);
				}
			};
			process.waitFor();
		}
		catch (Exception e)
		{
			throw new TilerException(e.getLocalizedMessage());
		}
	}
}
