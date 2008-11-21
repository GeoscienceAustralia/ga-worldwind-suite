package nasa.worldwind.cache;

import gov.nasa.worldwind.cache.BasicDataFileCache;
import gov.nasa.worldwind.util.Logging;

import java.io.File;

public class FixedBasicDataFileCache extends BasicDataFileCache
{
	public java.io.File newFile(String fileName)
	{
		if (fileName == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		File cacheWriteDir = getWriteLocation();
		if (cacheWriteDir != null)
		{
			String fullPath = this.makeFullPath(cacheWriteDir, fileName);
			File file = new File(fullPath);
			File parent = file.getParentFile();

			if (parent.exists())
				return file;
			else if (parent.mkdirs())
				return file;

			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			if (parent.exists())
				return file;
			else if (parent.mkdirs())
				return file;

			String msg = Logging.getMessage("generic.CantCreateCacheFile",
					fullPath);
			Logging.logger().severe(msg);
		}

		return null;
	}

	private String makeFullPath(java.io.File dir, String fileName)
	{
		return dir.getAbsolutePath() + "/" + fileName;
	}
}
