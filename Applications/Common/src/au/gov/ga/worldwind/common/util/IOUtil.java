package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.IOException;
import java.io.InputStream;

public class IOUtil extends WWIO
{
	public static String readStreamToStringKeepingNewlines(InputStream stream, String encoding)
			throws IOException
	{
		if (stream == null)
		{
			String message = Logging.getMessage("nullValue.InputStreamIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (encoding == null)
		{
			encoding = DEFAULT_CHARACTER_ENCODING;
		}

		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = stream.read(buffer)) >= 0)
		{
			sb.append(new String(buffer, 0, length, encoding));
		}

		return sb.toString();
	}
}
