/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.common.layers.delegate.reader;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.Globe;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.worldwind.common.layers.delegate.ITileReaderDelegate;
import au.gov.ga.worldwind.common.util.URLUtil;

/**
 * Implementation of {@link ITileReaderDelegate} which supports reading an image
 * from a zip file. Supports image masks, which may be saved as a separate image
 * within the zip file. Also supports searching for image masks within a
 * directory relative to the input URL, which is useful for local file tilesets
 * with masks.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MaskImageReaderDelegate implements ITileReaderDelegate
{
	private final static String DEFINITION_STRING = "MaskReader";

	//dataset/level/row/tile.jpg
	//mask/level/row/tile.png
	private int upDirectoryCount = 3;

	public MaskImageReaderDelegate()
	{
	}

	public MaskImageReaderDelegate(int upDirectoryCount)
	{
		this.upDirectoryCount = upDirectoryCount;
	}

	@Override
	public BufferedImage readImage(IDelegatorTile tile, URL url, Globe globe) throws IOException
	{
		boolean isZIP = url.toString().toLowerCase().endsWith("zip");
		if (isZIP)
		{
			//if the url is pointing to a zip file, attempt to extract the image (and mask if exists)
			BufferedImage image = null, mask = null;

			ZipInputStream zis = new ZipInputStream(url.openStream());
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null)
			{
				try
				{
					BufferedImage bi = ImageIO.read(zis);
					String lower = entry.getName().toLowerCase();
					if (lower.contains("mask") || bi.getColorModel().hasAlpha())
					{
						mask = bi;
					}
					else
					{
						image = bi;
					}
				}
				catch (IOException e)
				{
					//ignore (read next ZipEntry)
				}
			}

			//if both image and mask don't exist, at least return one of them
			if (image == null)
				return mask;
			if (mask == null)
				return image;

			//compose the image and mask together
			return compose(image, mask);
		}

		if (url.getProtocol().equalsIgnoreCase("jar") || url.getProtocol().equalsIgnoreCase("zip"))
		{
			//if the URL is pointing to an entry within a zip file, then create a
			//new URL for the mask png file inside another zip file (mask.zip)
			
			String urlString = url.toString();
			int indexOfBang = urlString.lastIndexOf('!');

			String zipFile = urlString.substring(0, indexOfBang);
			int lastIndexOfSlash = zipFile.lastIndexOf('/');
			String maskFile = zipFile.substring(0, lastIndexOfSlash + 1) + "mask.zip";

			String entry = urlString.substring(indexOfBang);
			int lastIndexOfPeriod = entry.lastIndexOf('.');
			entry = entry.substring(0, lastIndexOfPeriod + 1) + "png";

			URL maskUrl = new URL(maskFile + entry);

			BufferedImage image = null, mask = null;
			try
			{
				image = ImageIO.read(url);
				mask = ImageIO.read(maskUrl);
			}
			catch (Exception e)
			{
			}

			if (image == null)
				return null;
			if (mask == null)
				return image;
			return compose(image, mask);
		}
		else
		{
			File imageFile = URLUtil.urlToFile(url);
			if (imageFile == null || !imageFile.exists())
				return null;

			//search for a mask file relative to the image file
			File maskFile = getMaskFile(imageFile);

			BufferedImage image = ImageIO.read(imageFile);
			if (!maskFile.exists())
				return image;

			//if the mask file exists, compose the image and mask together
			BufferedImage mask = ImageIO.read(maskFile);
			return compose(image, mask);
		}
	}

	/**
	 * Create a File pointing to a 'mask' directory relative to the imageFile
	 * passed. The function moves up {@code upDirectoryCount} parent
	 * directories, replaces the directory with 'mask', and then moves back down
	 * the directories and file again.
	 * 
	 * @param imageFile
	 *            File for which to find a mask
	 * @return
	 */
	protected File getMaskFile(File imageFile)
	{
		String[] directories = new String[upDirectoryCount];
		File parent = imageFile.getParentFile();
		for (int i = upDirectoryCount - 1; i >= 0 && parent != null; i--)
		{
			directories[i] = parent.getName();
			parent = parent.getParentFile();
		}

		if (upDirectoryCount > 0)
		{
			parent = new File(parent, "mask");
			for (int i = 1; i < upDirectoryCount; i++)
			{
				parent = new File(parent, directories[i]);
			}
		}

		int lastIndexOfPeriod = imageFile.getName().lastIndexOf('.');
		String filename = imageFile.getName().substring(0, lastIndexOfPeriod);
		return new File(parent, filename + ".png");
	}

	/**
	 * Add the alpha channel of mask to image, and return the composed image.
	 * 
	 * @param image
	 * @param mask
	 * @return image masked by mask
	 */
	protected BufferedImage compose(BufferedImage image, BufferedImage mask)
	{
		Graphics2D g2d = mask.createGraphics();
		g2d.setComposite(AlphaComposite.SrcIn);
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		return mask;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new MaskImageReaderDelegate();
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
