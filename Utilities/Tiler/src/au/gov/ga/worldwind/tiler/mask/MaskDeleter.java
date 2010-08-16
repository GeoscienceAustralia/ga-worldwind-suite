package au.gov.ga.worldwind.tiler.mask;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

public class MaskDeleter
{
	public static void main(String[] args)
	{
		/*File imageDir = new File("S:/resources/images/world-wind/tiles/magnetics/edition5/magnetics");
		File maskDir = new File("S:/resources/images/world-wind/tiles/magnetics/edition5/mask");
		//File maskDir = null;
		String imageExt = "jpg";
		String maskExt = "png";
		deleteBlank(imageDir, imageExt, maskDir, maskExt);*/

		/*deleteImagesWithBlankMasks(new File[] {
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/dose"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/potassium"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/ratio_tk"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/ratio_uk"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/ratio_ut"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/ratio_uut"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/ternary"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/thorium"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/uranium") },
				"jpg",
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/full/mask"),
				"png");
		
		deleteImagesWithBlankMasks(new File[] {
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/areas/dose"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/areas/potassium"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/areas/ratio_tk"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/areas/ratio_uk"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/areas/ratio_ut"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/areas/ternary"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/areas/thorium"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/areas/uranium") },
				"jpg",
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition1/areas/mask"),
				"png");
		
		deleteImagesWithBlankMasks(new File[] {
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/dose"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/potassium"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/ratio_tk"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/ratio_uk"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/ratio_ut"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/ratio_uut"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/ratio_uut_nosun"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/ternary"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/thorium"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/totaldose"),
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/uranium") },
				"jpg",
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/full/mask"),
				"png");
		
		deleteImagesWithBlankMasks(new File[] {
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/ratio_uut_clip/ratio_uut_clip") },
				"jpg",
				new File("S:/resources/images/world-wind/tiles/radiometrics/edition2/ratio_uut_clip/mask"),
				"png");*/
		
		deleteBlankImages(new File("S:/resources/images/world-wind/tiles/surface_uranium/calcrete_raster"), "png");
		
		deleteImagesWithBlankMasks(new File[] {
				new File("S:/resources/images/world-wind/tiles/surface_uranium/u2th/u2th") },
				"jpg",
				new File("S:/resources/images/world-wind/tiles/surface_uranium/u2th/mask"),
				"png");
		
		deleteImagesWithBlankMasks(new File[] {
				new File("S:/resources/images/world-wind/tiles/terrain/MRVBF/MRVBF") },
				"jpg",
				new File("S:/resources/images/world-wind/tiles/terrain/MRVBF/mask"),
				"png");
		
		deleteImagesWithBlankMasks(new File[] {
				new File("S:/resources/images/world-wind/tiles/terrain/MRVBF_clip/MRVBF_clip") },
				"jpg",
				new File("S:/resources/images/world-wind/tiles/terrain/MRVBF_clip/mask"),
				"png");
		
		deleteImagesWithBlankMasks(new File[] {
				new File("S:/resources/images/world-wind/tiles/terrain/ozhill2/ozhill2") },
				"jpg",
				new File("S:/resources/images/world-wind/tiles/terrain/ozhill2/mask"),
				"png");
	}

	public static void deleteBlankImages(File imageDir, String imageExt)
	{
		try
		{
			List<File> images = new ArrayList<File>();

			System.out.println("Searching for images");

			addNonMaskImages(imageDir, images, imageExt);
			int deleteCount = 0;

			System.out.println("Found " + images.size() + " images");
			Set<File> directories = new HashSet<File>();

			for (int i = 0; i < images.size(); i++)
			{
				File imageFile = images.get(i);

				System.out.println("Reading " + imageFile + " (" + (i + 1) + "/" + images.size()
						+ " - " + ((i + 1) * 100 / images.size()) + "%)");

				BufferedImage image = ImageIO.read(imageFile);
				if (isEmpty(image))
				{
					System.out.println("Deleting: " + imageFile);
					imageFile.delete();
					directories.add(imageFile.getParentFile());
					deleteCount++;
				}
			}
			
			System.out.println("Searching for empty row directories");
			for (File dir : directories)
			{
				File[] files = dir.listFiles();
				if (files == null || files.length == 0)
				{
					System.out.println("Deleting empty directory: " + dir);
					dir.delete();
				}
			}

			System.out.println("Complete: deleted " + deleteCount + "/" + images.size()
					+ " images");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void deleteImagesWithBlankMasks(File[] imageDirs, String imageExt, File maskDir,
			String maskExt)
	{
		try
		{
			@SuppressWarnings("unchecked")
			List<File>[] images = new List[imageDirs.length];
			List<File> masks = new ArrayList<File>();

			for (int i = 0; i < imageDirs.length; i++)
				images[i] = new ArrayList<File>();

			System.out.println("Searching for masks");

			addMaskImages(maskDir, images, masks, imageDirs, imageExt, maskDir, maskExt);
			int deleteCount = 0;

			System.out.println("Found " + masks.size() + " masks with images");
			Set<File> directories = new HashSet<File>();

			for (int i = 0; i < masks.size(); i++)
			{
				File maskFile = masks.get(i);

				System.out.println("Reading " + maskFile + " (" + (i + 1) + "/" + masks.size()
						+ " - " + ((i + 1) * 100 / masks.size()) + "%)");

				BufferedImage image = ImageIO.read(maskFile);
				if (isEmpty(image))
				{
					System.out.println("Deleting: " + maskFile);
					maskFile.delete();
					directories.add(maskFile.getParentFile());

					for (List<File> imageList : images)
					{
						File imageFile = imageList.get(i);
						System.out.println("Deleting: " + imageFile);
						imageFile.delete();
						directories.add(imageFile.getParentFile());
					}

					deleteCount++;
				}
			}

			System.out.println("Searching for empty row directories");
			for (File dir : directories)
			{
				File[] files = dir.listFiles();
				if (files == null || files.length == 0)
				{
					System.out.println("Deleting empty directory: " + dir);
					dir.delete();
				}
			}

			System.out.println("Complete: deleted " + deleteCount + "/" + masks.size() + " images");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected static boolean isEmpty(BufferedImage image)
	{
		if (!image.getColorModel().hasAlpha())
			throw new IllegalArgumentException("Image has no alpha channel");

		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int rgb = image.getRGB(x, y);
				int alpha = (rgb >> 24) & 0xff;
				if (alpha != 0)
					return false;
			}
		}
		return true;
	}

	protected static void addNonMaskImages(File dir, List<File> images, String ext)
	{
		File[] files = dir.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isDirectory())
				{
					addNonMaskImages(file, images, ext);
				}
				else if (file.getName().toLowerCase().endsWith("." + ext))
				{
					images.add(file);
				}
			}
		}
	}

	protected static void addMaskImages(File dir, List<File>[] images, List<File> masks,
			File[] imageDirs, String imageExt, File maskDir, String maskExt)
			throws FileNotFoundException
	{
		File[] files = dir.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isDirectory())
				{
					addMaskImages(file, images, masks, imageDirs, imageExt, maskDir, maskExt);
				}
				else if (file.getName().toLowerCase().endsWith("." + maskExt))
				{
					String filename = file.getAbsolutePath();
					filename = filename.substring(maskDir.getAbsolutePath().length());
					filename = filename.substring(0, filename.length() - maskExt.length());

					for (int i = 0; i < imageDirs.length; i++)
					{
						File image = new File(imageDirs[i], filename + imageExt);

						if (!image.exists())
							throw new FileNotFoundException("Image not found: " + image);

						images[i].add(image);
					}

					masks.add(file);
				}
			}
		}
	}
}
