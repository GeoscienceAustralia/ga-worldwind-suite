package au.gov.ga.worldwind.tiler.mask;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class MaskBaker
{
	public static void main(String[] args)
	{
		File imageDir = new File("F:/LandCover/Trend in the maximum EVI values1_stretched/tiles");
		File maskDir = new File("F:/LandCover/DLCM_RGB/mask");
		File outputDir = new File("F:/LandCover/Trend in the maximum EVI values1_stretched/baked");
		String imageExt = "png";
		String maskExt = "png";
		String outputExt = "png";
		bake(imageDir, imageExt, maskDir, maskExt, outputDir, outputExt);
	}

	public static void bake(File imageDir, String imageExt, File maskDir, String maskExt,
			File outputDir, String outputExt)
	{
		try
		{
			List<File> images = new ArrayList<File>();
			List<File> masks = new ArrayList<File>();
			List<File> outputs = new ArrayList<File>();

			addImages(imageDir, images, masks, outputs, imageDir, imageExt, maskDir, maskExt,
					outputDir, outputExt);

			System.out.println("Found " + images.size() + " images");

			for (int i = 0; i < images.size(); i++)
			{
				File imageFile = images.get(i);
				File maskFile = masks.get(i);
				File outputFile = outputs.get(i);

				System.out.println("Writing " + outputFile + " (" + (i + 1) + "/" + images.size()
						+ " - " + ((i + 1) * 100 / images.size()) + "%)");

				BufferedImage image = ImageIO.read(imageFile);
				BufferedImage mask = ImageIO.read(maskFile);

				Graphics2D g2d = mask.createGraphics();
				g2d.setComposite(AlphaComposite.SrcIn);
				g2d.drawImage(image, 0, 0, null);
				g2d.dispose();

				if (!outputFile.getParentFile().exists())
				{
					outputFile.getParentFile().mkdirs();
				}
				ImageIO.write(mask, outputExt, outputFile);
			}

			System.out.println("Complete");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected static void addImages(File dir, List<File> images, List<File> masks,
			List<File> outputs, File imageDir, String imageExt, File maskDir, String maskExt,
			File outputDir, String outputExt)
	{
		File[] files = dir.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isDirectory())
				{
					addImages(file, images, masks, outputs, imageDir, imageExt, maskDir, maskExt,
							outputDir, outputExt);
				}
				else if (file.getName().toLowerCase().endsWith("." + imageExt))
				{
					String filename = file.getAbsolutePath();
					filename = filename.substring(imageDir.getAbsolutePath().length());
					filename = filename.substring(0, filename.length() - imageExt.length());
					File mask = new File(maskDir, filename + maskExt);
					File output = new File(outputDir, filename + outputExt);
					if (!mask.exists())
					{
						System.err.println("Mask doesn't exist: " + mask);
					}
					else if (output.exists())
					{
						System.err.println("Output already exists: " + output);
					}
					else
					{
						images.add(file);
						masks.add(mask);
						outputs.add(output);
					}
				}
			}
		}
	}
}
