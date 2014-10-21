<%--
# Copyright 2012 Geoscience Australia
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
--%>

<%@ page import="java.io.*,java.util.*,java.net.URL,java.util.zip.*" %>

<%!
	/**
	 * This script handles tile requests for both imagery and elevation tiles.
	 * 
	 * @author Michael de Hoog
	 */

	private static final String devRootPath = "/nas/web/data/dev/gis_data/world-wind/tiles";
	private static final String testRootPath = "/nas/web/data/test/gis_data/world-wind/tiles";
	private static final String prodRootPath = "/nas/web/data/prod/gis_data/world-wind/tiles";

	private static final Map<String, String> extensions = new HashMap<String, String>();
	private static final Map<String, String> contentTypes = new HashMap<String, String>();

	static
	{
		addFormat("zip", "application/zip");
		addFormat("jpg", "image/jpeg", "image/jpg");
		addFormat("png", "image/png");
		//addFormat("dds", "image/x-dds");
		//addFormat("gif", "image/gif");
		addFormat("bil", "application/bil", "application/bil16", "application/bil32");
	}

	/**
	 * Container object to return from the findFile function.
	 */
	private static class ExtensionInputStream
	{
		public final InputStream inputStream;
		public final String extension;
		private final ZipFile zipFile;

		public ExtensionInputStream(InputStream inputStream, String extension, ZipFile zipFile)
		{
			this.inputStream = inputStream;
			this.extension = extension;
			this.zipFile = zipFile;
		}

		public void close() throws IOException
		{
			if (inputStream != null)
				inputStream.close();
			if (zipFile != null)
				zipFile.close();
		}
	}

	/**
	 * Class which executes a command, waits for the process to complete, and
	 * then allows access to the stdout and stderr produced by the command.
	 */
	private static class CommandExecutor
	{
		private byte[] input;
		private byte[] error;

		public CommandExecutor(String command) throws IOException
		{
			Process process = Runtime.getRuntime().exec(command);
			InputStream inputStream = process.getInputStream();
			InputStream errorStream = process.getErrorStream();
			StreamReader inputReader = new StreamReader(inputStream);
			StreamReader errorReader = new StreamReader(errorStream);
			inputReader.start();
			errorReader.start();

			try
			{
				process.waitFor();
				inputReader.join();
				errorReader.join();
			}
			catch (InterruptedException e)
			{
			}

			input = inputReader.data;
			error = errorReader.data;
		}

		/**
		 * Thread which reads from an InputStream, and stores the data read in a
		 * byte[] array.
		 */
		private static class StreamReader extends Thread
		{
			private final InputStream stream;
			private byte[] data;

			public StreamReader(InputStream stream)
			{
				this.stream = stream;
			}

			@Override
			public void run()
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try
				{
					int read;
					byte[] buffer = new byte[1024];
					while ((read = stream.read(buffer)) >= 0)
						baos.write(buffer, 0, read);
				}
				catch (Exception e)
				{
				}
				data = baos.toByteArray();
			}
		}
	}

	/**
	 * Adds an image/elevation format to the static maps.
	 * 
	 * @param extension
	 *            File extension
	 * @param contentType
	 *            HTTP content type to return for this format
	 * @param alternateMimeTypes
	 *            List of alternative mime types that could be provided in the F
	 *            query parameter
	 */
	private static void addFormat(String extension, String contentType,
			String... alternateMimeTypes)
	{
		extensions.put(extension, extension);
		extensions.put(contentType, extension);
		for (String alternateMimeType : alternateMimeTypes)
		{
			extensions.put(alternateMimeType, extension);
		}
		contentTypes.put(extension, contentType);
	}

	/**
	 * Get the extension for a format provided in the F query parameter.
	 * 
	 * @param format
	 * @return Extension matching format
	 */
	private static String getExtensionForFormat(String format)
	{
		if (extensions.containsKey(format))
		{
			return extensions.get(format);
		}
		return null;
	}

	/**
	 * Get the content type to return for a particular file extension.
	 * 
	 * @param extension
	 * @return Content type matching extension
	 */
	private static String getContentTypeForExtension(String extension)
	{
		if (contentTypes.containsKey(extension))
		{
			return contentTypes.get(extension);
		}
		return null;
	}

	/**
	 * Add 0s to an string until it is a certain length.
	 * 
	 * @param value
	 *            String to pad
	 * @param charcount
	 *            Required string length
	 * @return 0 padded string
	 */
	private static String paddedInt(String value, int charcount)
	{
		while (value.length() < charcount)
		{
			value = "0" + value;
		}
		return value;
	}

	/**
	 * Find a file with the tiles path.
	 * 
	 * @param rootPath
	 *            Root directory of the tiles
	 * @param rowDirectory
	 *            Tile row directory
	 * @param filename
	 *            Tile filename
	 * @param L
	 *            Tile level
	 * @param T
	 *            Tile dataset
	 * @param extension
	 *            File extension (if null, searches all possible extensions)
	 * @param mask
	 *            Should we search in the 'mask' directory?
	 * @return InputStream if a file exists, else null
	 */
	private static ExtensionInputStream findFile(String rootPath, String rowDirectory, String filename,
			String L, String T, String extension, boolean mask, boolean useJavaZip) throws IOException
	{
		//if searching for mask, replace last directory in T string with 'mask'
		if (mask && T != null)
		{
			String maskT = "";
			int indexOfLastSlash = T.lastIndexOf('/');
			if (indexOfLastSlash >= 0)
			{
				maskT = T.substring(0, indexOfLastSlash + 1);
			}
			T = maskT + "mask";
		}

		//if no extension was provided, recurse with all known extensions
		if (extension == null)
		{
			Set<String> extensions = contentTypes.keySet();
			for (String ext : extensions)
			{
				if (ext != null)
				{
					ExtensionInputStream result =
							findFile(rootPath, rowDirectory, filename, L, T, ext, false, useJavaZip);
					if (result != null)
						return result;
				}
			}
		}
		else
		{
			//build the tile filename string
			String parentPath = rootPath;
			if (T != null)
				parentPath += "/" + T;

			String levelPath = "";
			if (L != null)
				levelPath += L + "/";
			String rowPath = "";
			if(rowDirectory != null)
				rowPath += rowDirectory + "/";
			String namePath = filename + "." + extension;

			File parent = new File(parentPath);
			if (parent.isDirectory())
			{
				File file = new File(parent, levelPath + rowPath + namePath);
				if (file.isFile())
				{
					return new ExtensionInputStream(new FileInputStream(file), extension, null);
				}
				
				if(L != null)
				{
					File levelParent = new File(parent, L + ".zip");
					if(levelParent.isFile())
					{
						return getFileFromZip(levelParent, rowPath + namePath, extension, useJavaZip);
					}
					
					if(rowDirectory != null)
					{
						File rowParent = new File(parentPath + "/" + L, rowDirectory + ".zip");
						if(rowParent.isFile())
						{
							return getFileFromZip(rowParent, namePath, extension, useJavaZip);
						}
					}
				}
			}
			else
			{
				if (!(parent.exists() && parent.getName().toLowerCase().endsWith(".zip")))
					parent = new File(parentPath + ".zip");

				if (parent.isFile())
				{
					return getFileFromZip(parent, levelPath + rowPath + namePath, extension, useJavaZip);
				}
			}
		}
		return null;
	}

	/**
	 * Create an InputStream wrapping an entry within a zip file.
	 * 
	 * @param zipFile
	 * @param entry
	 * @param extension
	 * @return InputStream if the entry exists, else null.
	 * @throws IOException
	 */
	private static ExtensionInputStream getFileFromZip(File zipFile, String entry,
			String extension, boolean useJavaZip) throws IOException
	{
		if (useJavaZip)
		{
			boolean close = true;
			ZipFile zip = null;
			try
			{
				zip = new ZipFile(zipFile);
				ZipEntry zentry = zip.getEntry(entry);
				if (zentry != null)
				{
					ExtensionInputStream retval =
							new ExtensionInputStream(zip.getInputStream(zentry), extension, zip);
					//zip is closed later in handleRequest
					close = false;
					return retval;
				}
			}
			finally
			{
				if (zip != null && close)
				{
					zip.close();
				}
			}
		}
		else
		{
			String command = "unzip -p " + zipFile.getAbsolutePath() + " " + entry;
			CommandExecutor executor = new CommandExecutor(command);

			if (executor.input.length > 0 && executor.error.length == 0)
			{
				ByteArrayInputStream bais = new ByteArrayInputStream(executor.input);
				return new ExtensionInputStream(bais, extension, null);
			}
		}
		return null;
	}

	/**
	 * Does the dataset/level directory exist?
	 * 
	 * @param rootPath
	 *            Root directory of the tiles
	 * @param L
	 *            Tile level
	 * @param T
	 *            Tile dataset
	 * @return true if the directory exists, else false
	 */
	private static boolean directoryExists(String rootPath, String L, String T, boolean useJavaZip)
			throws IOException
	{
		File parent = new File(rootPath + "/" + T);
		if (parent.isDirectory())
		{
			return new File(parent, L).isDirectory() || new File(parent, L + ".zip").isFile();
		}
		else
		{
			if (!(parent.exists() && parent.getName().toLowerCase().endsWith(".zip")))
				parent = new File(rootPath + "/" + T + ".zip");

			if (parent.isFile())
			{
				return entryExistsInZip(parent, L + "/", useJavaZip);
			}
		}
		return false;
	}

	/**
	 * Tests whether a zip file contains a particular entry.
	 * 
	 * @param zipFile
	 * @param entry
	 * @return True if entry exists in zipFile
	 * @throws IOException
	 */
	private static boolean entryExistsInZip(File zipFile, String entry, boolean useJavaZip)
			throws IOException
	{
		if (useJavaZip)
		{
			ZipFile zip = null;
			try
			{
				zip = new ZipFile(zipFile);
				ZipEntry zentry = zip.getEntry(entry);
				return zentry != null;
			}
			catch (OutOfMemoryError e)
			{
				//only throw OutOfMemoryErrors
				throw e;
			}
			catch (Exception e)
			{
			}
			finally
			{
				if (zip != null)
				{
					zip.close();
				}
			}
		}
		else
		{
			String command = "unzip -Z -1 " + zipFile.getAbsolutePath() + " " + entry;
			CommandExecutor executor = new CommandExecutor(command);
			return executor.input.length > 0 && executor.error.length == 0;
		}
		return false;
	}

	/**
	 * Find a blank tile file. First searches the dataset/level directory, then
	 * the dataset directory, and finally 'rootPath'.
	 * 
	 * @param rootPath
	 *            Root directory of the tiles
	 * @param L
	 *            Tile level
	 * @param T
	 *            Tile dataset
	 * @param extension
	 *            File extension of the blank file under 'rootPath'
	 * @return File if a blank file exists, else null
	 */
	private static ExtensionInputStream findBlankFile(String rootPath, String L, String T,
			String extension, boolean mask, boolean useJavaZip) throws IOException
	{
		String filename = "blank";
		ExtensionInputStream result;

		//commented out searching inside layer folders for blanks, as currently no layers implement this
		/*result = findFile(rootPath, null, filename, L, T, null, mask);
		if (result != null)
			return result;

		result = findFile(rootPath, null, filename, null, T, null, mask);
		if (result != null)
			return result;*/

		result = findFile(rootPath, null, filename, null, null, extension, false, useJavaZip);
		if (result != null)
			return result;

		return null;
	}

	/**
	 * Handle a tile request. This function should be called from the JSPs.
	 * 
	 * @param request
	 * @param response
	 * @param blankOnError
	 *            If the file is not found, should I return a blank file?
	 * @param searchForMask
	 *            Should I search for a corresponding mask file to return in a
	 *            zip?
	 * @throws IOException
	 */
	private static void handleRequest(HttpServletRequest request, HttpServletResponse response,
			boolean blankOnError, boolean searchForMask) throws IOException
	{
		try
		{
			handleRequest(request, response, blankOnError, searchForMask, true);
		}
		catch (OutOfMemoryError e)
		{
			//There is a bug in Java 5.0 and earlier in which zip files are read into memory on
			//load. This causes OutOfMemoryErrors, and makes reading from zip files much slower
			//than Java 6.0. If this occurs, try the 'unzip' system command instead.
			handleRequest(request, response, blankOnError, searchForMask, false);
		}
	}

	private static void handleRequest(HttpServletRequest request, HttpServletResponse response,
			boolean blankOnError, boolean searchForMask, boolean useJavaZip) throws IOException
	{
		/*
		 * Implementation notes:
		 *
		 * If F parameter is null:
		 * - search for any file that matches the X/Y/L/T parameters, with any extension, and return it with the correct content type
		 *
		 * If F parameter is not null:
		 * 	- if the F requested is zip:
		 * 		- search for any file that matches the X/Y/L/T parameters, if it is a zip, return it, if not, also search for any file that
		 * 		  matches X/Y/L/T parameters in a mask directory, zip the two files together, and return the zipped file (if no mask is
		 *  	  found, still zip the single file found and return the zipped file)
		 * 	- if the format requested is not zip:
		 *		- search for a file that matches the X/Y/L/T/F parameters, and return it
		 * 
		 * otherwise if the L/T/F parameters are valid and blankError is true, return blank matching F
		 * otherwise return 404
		 */
		
		response.addHeader("Access-Control-Allow-Origin", "*");

		//get the parameters from the request
		String X = request.getParameter("X"); //col
		String Y = request.getParameter("Y"); //row
		String L = request.getParameter("L"); //level
		String T = request.getParameter("T"); //dataset
		String F = request.getParameter("F"); //format

		if (X == null || Y == null || L == null || T == null)
		{
			//if the essential parameters are not defined, just return
			return;
		}
		
		//determine the root path of the tiles
		boolean sandpit = request.getServerPort() == 8500;
		String rootPath = sandpit ? devRootPath : prodRootPath;

		//calculate tile directory and filename
		String rowDirectory = paddedInt(Y, 4);
		String filename = rowDirectory + "_" + paddedInt(X, 4);

		ExtensionInputStream file = null, mask = null;
		boolean needsZip = false;
		String contentType = null;

		try
		{
			if (F == null)
			{
				//if no format has been specified, search for a file using any known extension
				file = findFile(rootPath, rowDirectory, filename, L, T, null, false, useJavaZip);
				if (file != null)
				{
					//if the file has been found, set the correct content type
					contentType = getContentTypeForExtension(file.extension);
				}
				else if (blankOnError && directoryExists(rootPath, L, T, useJavaZip))
				{
					//if no file has been found and a blank should be returned, return a blank file (default to jpg)
					String extension = "jpg";
					file = findBlankFile(rootPath, L, T, extension, false, useJavaZip);
					contentType = getContentTypeForExtension(extension);
				}
			}
			else
			{
				//format has been specified; get the extension for the requested format
				F = F.toLowerCase();
				String extension = getExtensionForFormat(F);

				if (extension == null)
				{
					//if the format is unknown, return a 400 error
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}

				//set the content type for the requested extension
				contentType = getContentTypeForExtension(extension);
				boolean isZip = extension.equals("zip");

				//find a file matching the extension
				file = findFile(rootPath, rowDirectory, filename, L, T, extension, false, useJavaZip);

				if (file == null && isZip)
				{
					//if no file has been found but a zip is requested
					needsZip = true;
					//search for a file using any known extension
					file = findFile(rootPath, rowDirectory, filename, L, T, null, false, useJavaZip);
					if (searchForMask)
					{
						//search for a file in the mask directory using any known extension
						mask = findFile(rootPath, rowDirectory, filename, L, T, null, true, useJavaZip);
					}
				}

				if (file == null && blankOnError && directoryExists(rootPath, L, T, useJavaZip))
				{
					//if still no file has been found and a blank file should be returned
					if (isZip)
					{
						//if a zip is requested, find blank files for the image and mask (default to jpg and png)
						file = findBlankFile(rootPath, L, T, "jpg", false, useJavaZip);
						if (searchForMask)
						{
							mask = findBlankFile(rootPath, L, T, "png", true, useJavaZip);
						}
					}
					else
					{
						//find a blank file for the requested extension
						file = findBlankFile(rootPath, L, T, extension, false, useJavaZip);
					}
				}
			}

			if (file == null)
			{
				//if no file has been found to return, return a 404 error
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			int size;
			byte[] buffer = new byte[1024];
			InputStream is;

			//set the content type and get the outputstream
			response.setContentType(contentType);
			OutputStream os = null;

			try
			{
				os = response.getOutputStream();

				if (needsZip)
				{
					//set the save-as filename in the http header
					response.addHeader("Content-disposition", "inline; filename=" + filename
							+ ".zip");

					//create a zip outputstream wrapper around the response outputstream
					ZipOutputStream zos = null;
					try
					{
						zos = new ZipOutputStream(os);
						//zos.setLevel(ZipOutputStream.STORED);

						//add the file to the zip file
						is = file.inputStream;
						zos.putNextEntry(new ZipEntry(filename + "." + file.extension));
						while ((size = is.read(buffer)) >= 0)
						{
							zos.write(buffer, 0, size);
						}
						zos.closeEntry();

						if (mask != null)
						{
							//if a mask exists, add the mask to the zip file
							is = mask.inputStream;
							zos.putNextEntry(new ZipEntry(filename + "_mask." + mask.extension));
							while ((size = is.read(buffer)) >= 0)
							{
								zos.write(buffer, 0, size);
							}
							zos.closeEntry();
						}
					}
					finally
					{
						//close the outputstream
						if (zos != null)
						{
							zos.close();
						}
					}
				}
				else
				{
					//set the save-as filename in the http header
					response.addHeader("Content-disposition", "inline; filename=" + filename + "."
							+ file.extension);

					//write the file to the response outputstream
					is = file.inputStream;
					while ((size = is.read(buffer)) >= 0)
					{
						os.write(buffer, 0, size);
					}
				}
			}
			finally
			{
				if (os != null)
				{
					os.flush();
					os.close();
				}
			}
		}
		finally
		{
			if (file != null)
			{
				file.close();
			}
			if (mask != null)
			{
				mask.close();
			}
		}
	}
%>
