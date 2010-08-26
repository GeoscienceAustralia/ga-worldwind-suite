<%@ page import="java.io.*,java.util.*,java.net.URL,java.util.zip.*" %>

<%!
	private static final String path = "/web/html/test_root/docs/resources/images/world-wind/tiles-orig";

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
	 * Adds an image/elevation format to the static maps.
	 * 
	 * @param extension File extension
	 * @param contentType HTTP content type to return for this format
	 * @param alternateMimeTypes List of alternative mime types that could be provided in the F query parameter
	 */
	private static void addFormat(String extension, String contentType, String... alternateMimeTypes)
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
	 * @param value String to pad
	 * @param charcount Required string length
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
	 * @param rowDirectory Tile row directory
	 * @param filename Tile filename
	 * @param L Tile level
	 * @param T Tile dataset
	 * @param extension File extension (if null, searches all possible extensions)
	 * @param mask Should we search in the 'mask' directory?
	 * @return File if a file exists, else null
	 */
	private static File findFile(String rowDirectory, String filename, String L, String T, String extension, boolean mask)
	{
		//if searching for mask, replace last directory in T string with 'mask'
		if (mask)
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
					File file = findFile(rowDirectory, filename, L, T, ext, false);
					if (file != null)
						return file;
				}
			}
		}
		else
		{
			//build the tile filename string
			StringBuilder sb = new StringBuilder(path);
			sb.append("/");
			if(T != null)
			{
				sb.append(T);
				sb.append("/");
			}
			if(L != null)
			{
				sb.append(L);
				sb.append("/");
			}
			if(rowDirectory != null)
			{
				sb.append(rowDirectory);
				sb.append("/");
			}
			sb.append(filename);
			sb.append(".");
			sb.append(extension);
			
			//create a file object and return if the file exists
			File file = new File(sb.toString());
			if (file.exists())
				return file;
		}
		return null;
	}

	/**
	 * Does the dataset/level directory exist?
	 * 
	 * @param L Tile level
	 * @param T Tile dataset
	 * @return true if the directory exists, else false
	 */
	private static boolean directoryExists(String L, String T)
	{
		File file = new File(path + "/" + T + "/" + L);
		return file.isDirectory();
	}

	/**
	 * Find a blank tile file. First searches the dataset/level directory, then the dataset directory, and finally 'path'.
	 * 
	 * @param L Tile level
	 * @param T Tile dataset
	 * @param extension File extension of the blank file under 'path'
	 * @return File if a blank file exists, else null
	 */
	private static File findBlankFile(String L, String T, String extension, boolean mask)
	{
		String filename = "blank";
		File file = findFile(null, filename, L, T, null, mask);
		if(file != null)
			return file;
			
		file = findFile(null, filename, null, T, null, mask);
		if(file != null)
			return file;
		
		file = findFile(null, filename, null, null, extension, false);
		if(file != null)
			return file;
		
		return null;
	}

	/**
	 * Extracts the file extension from a filename
	 * 
	 * @param file
	 * @return File extension of file
	 */
	private static String getFileExtension(File file)
	{
		String name = file.getName();
		return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
	}

	/**
	 * Handle a tile request. This function should be called from the JSPs.
	 * 
	 * @param request
	 * @param response
	 * @param blankOnError If the file is not found, should I return a blank file?
	 * @param searchForMask Should I search for a corresponding mask file to return in a zip?
	 * @throws IOException
	 */
	private static void handleRequest(HttpServletRequest request, HttpServletResponse response, boolean blankOnError, boolean searchForMask) throws IOException
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

		//get the parameters from the request
		String X = request.getParameter("X");
		String Y = request.getParameter("Y");
		String L = request.getParameter("L");
		String T = request.getParameter("T");
		String F = request.getParameter("F");

		if (X == null || Y == null || L == null || T == null)
		{
			//if the essential parameters are not defined, just return
			return;
		}
		
		//calculate tile directory and filename
		String rowDirectory = paddedInt(Y, 4);
		String filename = rowDirectory + "_" + paddedInt(X, 4);

		File file = null, mask = null;
		boolean needsZip = false;
		String contentType = null;

		if (F == null)
		{
			//if no format has been specified, search for a file using any known extension
			file = findFile(rowDirectory, filename, L, T, null, false);
			if (file != null)
			{
				//if the file has been found, set the correct content type
				String extension = getFileExtension(file);
				contentType = getContentTypeForExtension(extension);
			}
			else if (blankOnError && directoryExists(L, T))
			{
				//if no file has been found and a blank should be returned, return a blank file (default to jpg)
				String extension = "jpg";
				file = findBlankFile(L, T, extension, false);
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
			file = findFile(rowDirectory, filename, L, T, extension, false);
			
			if (file == null && isZip)
			{
				//if no file has been found but a zip is requested
				needsZip = true;
				//search for a file using any known extension
				file = findFile(rowDirectory, filename, L, T, null, false);
				if (searchForMask)
				{
					//search for a file in the mask directory using any known extension
					mask = findFile(rowDirectory, filename, L, T, null, true);
				}
			}

			if (file == null && blankOnError && directoryExists(L, T))
			{
				//if still no file has been found and a blank file should be returned
				if(isZip)
				{
					//if a zip is requested, find blank files for the image and mask (default to jpg and png)
					file = findBlankFile(L, T, "jpg", false);
					if(searchForMask)
					{
						mask = findBlankFile(L, T, "png", true);
					}
				}
				else
				{
					//find a blank file for the requested extension
					file = findBlankFile(L, T, extension, false);
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
		FileInputStream fis;

		//set the content type and get the outputstream
		response.setContentType(contentType);
		OutputStream os = response.getOutputStream();

		if (needsZip)
		{
			//set the save-as filename in the http header
			response.addHeader("Content-disposition", "inline; filename=" + filename + ".zip");

			//create a zip outputstream wrapper around the response outputstream
			ZipOutputStream zos = new ZipOutputStream(os);
			//zos.setLevel(ZipOutputStream.STORED);

			//add the file to the zip file
			fis = new FileInputStream(file);
			zos.putNextEntry(new ZipEntry(filename + "." + getFileExtension(file)));
			while ((size = fis.read(buffer)) >= 0)
			{
				zos.write(buffer, 0, size);
			}
			zos.closeEntry();
			fis.close();

			if (mask != null)
			{
				//if a mask exists, add the mask to the zip file
				fis = new FileInputStream(mask);
				zos.putNextEntry(new ZipEntry(filename + "_mask." + getFileExtension(mask)));
				while ((size = fis.read(buffer)) >= 0)
				{
					zos.write(buffer, 0, size);
				}
				zos.closeEntry();
				fis.close();
			}

			//close the outputstreams
			zos.close();
			os.close();
		}
		else
		{
			//set the save-as filename in the http header
			response.addHeader("Content-disposition", "inline; filename=" + filename + "." + getFileExtension(file));
			
			//write the file to the response outputstream
			fis = new FileInputStream(file);
			while ((size = fis.read(buffer)) >= 0)
			{
				os.write(buffer, 0, size);
			}

			//close the streams
			fis.close();
			os.close();
		}
	}
%>
