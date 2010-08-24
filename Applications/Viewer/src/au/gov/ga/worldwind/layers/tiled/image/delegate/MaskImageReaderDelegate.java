package au.gov.ga.worldwind.layers.tiled.image.delegate;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import au.gov.ga.worldwind.util.Util;

public class MaskImageReaderDelegate implements ImageReaderDelegate
{
	public final static String DEFINITION_STRING = "MaskReader";

	//dataset/level/row/tile.jpg
	//mask/level/row/tile.png
	private int upDirectoryCount = 3;

	@Override
	public BufferedImage readImage(URL url) throws IOException
	{
		boolean isZIP = url.toString().toLowerCase().endsWith("zip");
		if (isZIP)
		{
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

			if (image == null)
				return mask;
			if (mask == null)
				return image;

			return compose(image, mask);
		}

		File imageFile = Util.urlToFile(url);
		if (imageFile == null || !imageFile.exists())
			return null;

		File maskFile = getMaskFile(imageFile);

		BufferedImage image = ImageIO.read(imageFile);
		if (!maskFile.exists())
			return image;

		BufferedImage mask = ImageIO.read(maskFile);
		return compose(image, mask);
	}

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

	protected BufferedImage compose(BufferedImage image, BufferedImage mask)
	{
		Graphics2D g2d = mask.createGraphics();
		g2d.setComposite(AlphaComposite.SrcIn);
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		return mask;
	}

	@Override
	public Delegate fromDefinition(String definition)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new MaskImageReaderDelegate();
		return null;
	}

	@Override
	public String toDefinition()
	{
		return DEFINITION_STRING;
	}
}
