/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.util.Logging;

import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: AbstractFileCache.java 3400 2007-10-28 07:49:25Z tgaskins $
 */
public class AbstractFileCache implements FileCache
{
	private final java.util.LinkedList<java.io.File> cacheDirs = new java.util.LinkedList<java.io.File>();
	private java.io.File cacheWriteDir = null;

	protected void initialize(java.io.InputStream xmlConfigStream)
	{
		javax.xml.parsers.DocumentBuilderFactory docBuilderFactory = javax.xml.parsers.DocumentBuilderFactory
				.newInstance();

		try
		{
			javax.xml.parsers.DocumentBuilder docBuilder = docBuilderFactory
					.newDocumentBuilder();
			org.w3c.dom.Document doc = docBuilder.parse(xmlConfigStream);

			// The order of the following two calls is important, because building the writable location may entail
			// creating a location that's included in the specified read locations.
			this.buildWritePaths(doc);
			this.buildReadPaths(doc);

			if (this.cacheWriteDir == null)
			{
				Logging.logger().warning("FileCache.NoFileCacheWriteLocation");
			}

			if (this.cacheDirs.size() == 0)
			{
				// This should not happen because the writable cache is added to the read list, but check nonetheless
				String message = Logging
						.getMessage("FileCache.NoFileCacheReadLocations");
				Logging.logger().severe(message);
				throw new IllegalStateException(message);
			}
		}
		catch (javax.xml.parsers.ParserConfigurationException e)
		{
			String message = Logging
					.getMessage("FileCache.NoFileCacheReadLocations");
			Logging.logger().severe(message);
			throw new IllegalStateException(message, e);
		}
		catch (org.xml.sax.SAXException e)
		{
			String message = Logging
					.getMessage("FileCache.NoFileCacheReadLocations");
			Logging.logger().severe(message);
			throw new IllegalStateException(message, e);
		}
		catch (java.io.IOException e)
		{
			String message = Logging
					.getMessage("FileCache.ExceptionReadingCacheLocationFile");
			Logging.logger().severe(message);
			throw new IllegalStateException(message, e);
		}
	}

	public void addCacheLocation(String newPath)
	{
		this.addCacheLocation(this.cacheDirs.size(), newPath);
	}

	public void addCacheLocation(int index, String newPath)
	{
		if (newPath == null || newPath.length() == 0)
		{
			String message = Logging
					.getMessage("nullValue.FileCachePathIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (index < 0)
		{
			String message = Logging.getMessage("generic.InvalidIndex", index);
			Logging.logger().fine(message);
			throw new IllegalArgumentException(message);
		}

		if (index > 0 && index > this.cacheDirs.size())
			index = this.cacheDirs.size();

		java.io.File newFile = new java.io.File(newPath);

		if (this.cacheDirs.contains(newFile))
			this.cacheDirs.remove(newFile);

		this.cacheDirs.add(index, newFile);
	}

	public void removeCacheLocation(String newPath)
	{
		if (newPath == null || newPath.length() == 0)
		{
			String message = Logging
					.getMessage("nullValue.FileCachePathIsNull");
			Logging.logger().severe(message);
			// Just warn and return.
			return;
		}

		java.io.File newFile = new java.io.File(newPath);

		if (newFile.equals(this.cacheWriteDir))
		{
			String message = Logging.getMessage(
					"FileCache.CannotRemoveWriteLocationFromSearchList",
					newPath);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.cacheDirs.remove(new java.io.File(newPath));
	}

	public java.util.List<java.io.File> getCacheLocations()
	{
		// Return a copy.
		return new java.util.LinkedList<java.io.File>(this.cacheDirs);
	}

	public java.io.File getWriteLocation()
	{
		return this.cacheWriteDir;
	}

	private void buildReadPaths(org.w3c.dom.Node dataFileCacheNode)
	{
		javax.xml.xpath.XPathFactory pathFactory = javax.xml.xpath.XPathFactory
				.newInstance();
		javax.xml.xpath.XPath pathFinder = pathFactory.newXPath();

		try
		{
			org.w3c.dom.NodeList locationNodes = (org.w3c.dom.NodeList) pathFinder
					.evaluate("/dataFileCache/readLocations/location",
							dataFileCacheNode.getFirstChild(),
							javax.xml.xpath.XPathConstants.NODESET);
			for (int i = 0; i < locationNodes.getLength(); i++)
			{
				org.w3c.dom.Node location = locationNodes.item(i);
				String prop = pathFinder.evaluate("@property", location);
				String wwDir = pathFinder.evaluate("@wwDir", location);
				String append = pathFinder.evaluate("@append", location);

				String path = buildLocationPath(prop, append, wwDir);
				if (path == null)
				{
					Logging.logger().log(
							Level.WARNING,
							"FileCache.CacheLocationInvalid",
							prop != null ? prop : Logging
									.getMessage("generic.Unknown"));
					continue;
				}

				// Even paths that don't exist or are otherwise problematic are added to the list because they may
				// become readable during the session. E.g., removable media. So add them to the search list.

				java.io.File pathFile = new java.io.File(path);
				if (pathFile.exists() && !pathFile.isDirectory())
				{
					Logging.logger()
							.log(Level.WARNING,
									"FileCache.CacheLocationIsFile",
									pathFile.getPath());
				}

				if (!this.cacheDirs.contains(pathFile)) // filter out duplicates
				{
					this.cacheDirs.add(pathFile);
				}
			}
		}
		catch (javax.xml.xpath.XPathExpressionException e)
		{
			String message = Logging
					.getMessage("FileCache.NoFileCacheReadLocations");
			Logging.logger().severe(message);
			throw new IllegalStateException(message, e);
		}
	}

	private void buildWritePaths(org.w3c.dom.Node dataFileCacheNode)
	{
		javax.xml.xpath.XPathFactory pathFactory = javax.xml.xpath.XPathFactory
				.newInstance();
		javax.xml.xpath.XPath pathFinder = pathFactory.newXPath();

		try
		{
			org.w3c.dom.NodeList locationNodes = (org.w3c.dom.NodeList) pathFinder
					.evaluate("/dataFileCache/writeLocations/location",
							dataFileCacheNode.getFirstChild(),
							javax.xml.xpath.XPathConstants.NODESET);
			for (int i = 0; i < locationNodes.getLength(); i++)
			{
				org.w3c.dom.Node location = locationNodes.item(i);
				String prop = pathFinder.evaluate("@property", location);
				String wwDir = pathFinder.evaluate("@wwDir", location);
				String append = pathFinder.evaluate("@append", location);
				String create = pathFinder.evaluate("@create", location);

				String path = buildLocationPath(prop, append, wwDir);
				if (path == null)
				{
					Logging.logger().log(
							Level.WARNING,
							"FileCache.CacheLocationInvalid",
							prop != null ? prop : Logging
									.getMessage("generic.Unknown"));
					continue;
				}

				Logging.logger().log(Level.FINER,
						"FileCache.AttemptingWriteCache", path);
				java.io.File pathFile = new java.io.File(path);
				if (!pathFile.exists() && create != null
						&& (create.contains("t") || create.contains("T")))
				{
					Logging.logger().log(Level.FINER,
							"FileCache.MakingDirsFor", path);
					pathFile.mkdirs();
				}

				if (pathFile.isDirectory() && pathFile.canWrite()
						&& pathFile.canRead())
				{
					Logging.logger().log(Level.FINER,
							"FileCache.WriteCacheSuccessful", path);
					this.cacheWriteDir = pathFile;
					this.cacheDirs.addFirst(pathFile); // writable location is always first in search path
					break; // only need one
				}
			}
		}
		catch (javax.xml.xpath.XPathExpressionException e)
		{
			String message = Logging
					.getMessage("FileCache.NoFileCacheReadLocations");
			Logging.logger().severe(message);
			throw new IllegalStateException(message, e);
		}
	}

	private static String buildLocationPath(String property, String append,
			String wwDir)
	{
		String path = propertyToPath(property);

		if (append != null && append.length() != 0)
			path = appendPathPart(path, append.trim());

		if (wwDir != null && wwDir.length() != 0)
			path = appendPathPart(path, wwDir.trim());

		return path;
	}

	private static String appendPathPart(String firstPart, String secondPart)
	{
		if (secondPart == null || secondPart.length() == 0)
			return firstPart;
		if (firstPart == null || secondPart.length() == 0)
			return secondPart;

		firstPart = stripTrailingSeparator(firstPart);
		secondPart = stripLeadingSeparator(secondPart);

		return firstPart + System.getProperty("file.separator") + secondPart;
	}

	private static String stripTrailingSeparator(String s)
	{
		if (s.endsWith("/") || s.endsWith("\\"))
			return s.substring(0, s.length() - 1);
		else
			return s;
	}

	private static String stripLeadingSeparator(String s)
	{
		if (s.startsWith("/") || s.startsWith("\\"))
			return s.substring(1, s.length());
		else
			return s;
	}

	private static String propertyToPath(String propName)
	{
		if (propName == null || propName.length() == 0)
			return null;

		String prop = System.getProperty(propName);
		if (prop != null)
			return prop;

		if (propName
				.equalsIgnoreCase("gov.nasa.worldwind.platform.alluser.cache"))
			return determineAllUserCacheDir();

		if (propName.equalsIgnoreCase("gov.nasa.worldwind.platform.user.cache"))
			return determineSingleUserCacheDir();

		return null;
	}

	private static String determineAllUserCacheDir()
	{
		if (gov.nasa.worldwind.Configuration.isMacOS())
		{
			return "/Library/Caches";
		}
		else if (gov.nasa.worldwind.Configuration.isWindowsOS())
		{
			String path = System.getenv("ALLUSERSPROFILE");
			if (path == null)
			{
				Logging.logger().severe(
						"generic.AllUsersWindowsProfileNotKnown");
				return null;
			}
			return path + "\\Application Data";
		}
		else if (gov.nasa.worldwind.Configuration.isLinuxOS()
				|| gov.nasa.worldwind.Configuration.isUnixOS()
				|| gov.nasa.worldwind.Configuration.isSolarisOS())
		{
			return "/var/cache/";
		}
		else
		{
			Logging.logger().warning("generic.UnknownOperatingSystem");
			return null;
		}
	}

	private static String determineSingleUserCacheDir()
	{
		String home = getUserHomeDir();
		if (home == null)
		{
			Logging.logger().warning("generic.UsersHomeDirectoryNotKnown");
			return null;
		}

		String path = null;

		if (gov.nasa.worldwind.Configuration.isMacOS())
		{
			path = "/Library/Caches";
		}
		else if (gov.nasa.worldwind.Configuration.isWindowsOS())
		{
			path = System.getenv("USERPROFILE");
			if (path == null)
			{
				Logging.logger().fine("generic.UsersWindowsProfileNotKnown");
				return null;
			}
			path += "\\Application Data";
		}
		else if (gov.nasa.worldwind.Configuration.isLinuxOS()
				|| gov.nasa.worldwind.Configuration.isUnixOS()
				|| gov.nasa.worldwind.Configuration.isSolarisOS())
		{
			path = "/var/cache/";
		}
		else
		{
			Logging.logger().fine("generic.UnknownOperatingSystem");
		}

		if (path == null)
			return null;

		return home + path;
	}

	private static String getUserHomeDir()
	{
		return System.getProperty("user.home");
	}

	public boolean contains(String fileName)
	{
		if (fileName == null)
			return false;

		for (java.io.File cacheDir : this.cacheDirs)
		{
			java.io.File file;
			if (fileName.startsWith(cacheDir.getAbsolutePath()))
				file = new java.io.File(fileName);
			else
				file = this.cachePathForFile(cacheDir, fileName);

			if (file.exists())
				return true;
		}

		return false;
	}

	private java.io.File cachePathForFile(java.io.File file, String fileName)
	{
		return new java.io.File(file.getAbsolutePath() + "/" + fileName);
	}

	private String makeFullPath(java.io.File dir, String fileName)
	{
		return dir.getAbsolutePath() + "/" + fileName;
	}

	/**
	 * @param fileName
	 *            the name to give the newly created file
	 * @return a handle to the newly created file if it could be created and
	 *         added to the cache, otherwise null
	 * @throws IllegalArgumentException
	 *             if <code>fileName</code> is null
	 */
	public java.io.File newFile(String fileName)
	{
		if (fileName == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (this.cacheWriteDir != null)
		{
			String fullPath = this.makeFullPath(this.cacheWriteDir, fileName);
			java.io.File file = new java.io.File(fullPath);

			if (createDir(file.getParentFile()))
			{
				return file;
			}
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			if (createDir(file.getParentFile()))
			{
				return file;
			}

			String msg = Logging.getMessage("generic.CantCreateCacheFile",
					fullPath);
			Logging.logger().severe(msg);
		}

		return null;
	}

	private boolean createDir(java.io.File file)
	{
		if (file.exists())
			return true;
		return file.mkdirs();
	}

	/**
	 * @param fileName
	 *            the name of the file to find
	 * @param checkClassPath
	 *            if <code>true</code>, the class path is first searched for the
	 *            file, otherwise the class path is not searched unless it's one
	 *            of the explicit paths in the cache search directories
	 * @return a handle to the requested file if it exists in the cache,
	 *         otherwise null
	 * @throws IllegalArgumentException
	 *             if <code>fileName</code> is null
	 */
	public java.net.URL findFile(String fileName, boolean checkClassPath)
	{
		if (fileName == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (checkClassPath)
		{
			java.net.URL url = this.getClass().getClassLoader().getResource(
					fileName);
			if (url != null)
				return url;
		}

		for (java.io.File dir : this.cacheDirs)
		{
			if (!dir.exists())
				continue;

			java.io.File file = new java.io.File(this.makeFullPath(dir,
					fileName));
			if (file.exists())
			{
				try
				{
					return file.toURI().toURL();
				}
				catch (java.net.MalformedURLException e)
				{
					Logging.logger().log(
							Level.SEVERE,
							Logging.getMessage(
									"FileCache.ExceptionCreatingURLForFile",
									file.getPath()), e);
				}
			}
		}

		return null;
	}

	/**
	 * @param url
	 *            the "file:" URL of the file to remove from the cache
	 * @throws IllegalArgumentException
	 *             if <code>url</code> is null
	 */
	public void removeFile(java.net.URL url)
	{
		if (url == null)
		{
			String msg = Logging.getMessage("nullValue.URLIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		try
		{
			java.io.File file = new java.io.File(url.toURI());

			if (file.exists())
				file.delete();
		}
		catch (java.net.URISyntaxException e)
		{
			Logging.logger().log(
					Level.SEVERE,
					Logging.getMessage("FileCache.ExceptionRemovingFile", url
							.toString()), e);
		}
	}
}