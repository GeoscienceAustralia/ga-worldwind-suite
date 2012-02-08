package au.gov.ga.worldwind.common.layers.model.gocad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.common.util.FastShape;

/**
 * Factory for creating {@link FastShape}s from GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadFactory
{
	private static final String OBJECT_END_REGEX = "END\\s*";
	private static final String COMMENT_REGEX = "\\s*#.*";

	public static boolean isGocadFileSuffix(String suffix)
	{
		return suffix.equalsIgnoreCase("ts") || suffix.equalsIgnoreCase("gp") || suffix.equalsIgnoreCase("vo")
				|| suffix.equalsIgnoreCase("pl");
	}

	/**
	 * Enumeration of different GOCAD file types.
	 */
	public enum GocadType
	{
		PLine(GocadPLineReader.HEADER_REGEX, GocadPLineReader.class),
		Voxet(GocadVoxetReader.HEADER_REGEX, GocadVoxetReader.class),
		TSurf(GocadTSurfReader.HEADER_REGEX, GocadTSurfReader.class);

		/**
		 * Regular expression used for matching the first line of the GOCAD file
		 * to this type.
		 */
		public final String headerRegex;
		/**
		 * {@link GocadReader} implementation used for reading this type.
		 */
		public final Class<? extends GocadReader> readerClass;

		private GocadType(String headerRegex, Class<? extends GocadReader> readerClass)
		{
			this.headerRegex = headerRegex;
			this.readerClass = readerClass;
		}

		/**
		 * @return An instance of a {@link GocadReader} for reading a file of
		 *         this type.
		 */
		public GocadReader instanciateReader()
		{
			try
			{
				return readerClass.newInstance();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}

	public static List<FastShape> read(File file, GocadReaderParameters parameters)
	{
		try
		{
			return read(new FileReader(file), file.toURI().toURL(), parameters);
		}
		catch (MalformedURLException e)
		{
			//won't ever happen
			return null;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static List<FastShape> read(InputStream is, URL context, GocadReaderParameters parameters)
	{
		return read(new InputStreamReader(is), context, parameters);
	}

	/**
	 * Read a GOCAD source to a {@link FastShape}.
	 * 
	 * @param reader
	 *            Reader to read from
	 * @return A list of {@link FastShape}s containing the geometry from the
	 *         GOCAD file
	 */
	public static List<FastShape> read(Reader reader, URL context, GocadReaderParameters parameters)
	{
		List<FastShape> shapes = new ArrayList<FastShape>();

		try
		{
			BufferedReader br = new BufferedReader(reader);
			while (true)
			{
				String line = br.readLine();
				if (line == null)
				{
					if (shapes.size() == 0)
					{
						throw new IllegalArgumentException("No GOCAD objects found");
					}
					//file is finished, so break out of loop
					break;
				}

				//check if the line matches any of the GOCAD object header regexes
				GocadType type = determineGocadType(line);
				if (type == null)
				{
					//if this line doesn't, try the next line
					continue;
				}

				GocadReader gocadReader = type.instanciateReader();
				gocadReader.begin(parameters);
				while (true)
				{
					line = br.readLine();
					if (line == null)
					{
						throw new IllegalArgumentException("GOCAD file ended unexpectedly");
					}
					if (line.matches(COMMENT_REGEX))
					{
						//don't pass comment lines to the reader
						continue;
					}
					if (line.matches(OBJECT_END_REGEX))
					{
						//object has ended, break out of the loop to parse the next object (if any)
						break;
					}
					gocadReader.addLine(line);
				}
				shapes.add(gocadReader.end(context));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return shapes;
	}

	/**
	 * Determine the {@link GocadType} from the header line in the file.
	 * 
	 * @param line
	 *            First line in the GOCAD file
	 * @return {@link GocadType} matched for the header, or null if none found.
	 */
	protected static GocadType determineGocadType(String line)
	{
		for (GocadType type : GocadType.values())
		{
			if (line.matches(type.headerRegex))
			{
				return type;
			}
		}
		return null;
	}
}
