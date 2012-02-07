package au.gov.ga.worldwind.tiler.mask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoMaskDeleter
{
	public static void main(String[] args)
	{
		File imageDir = new File("G:/LandCover/Trends/forest_and_woodland_jpg");
		File maskDir = new File("G:/LandCover/Trends/forest_and_woodland_mask");
		String imageExt = "jpg";
		String maskExt = "png";
		delete(imageDir, imageExt, maskDir, maskExt);
	}

	public static void delete(File imageDir, String imageExt, File maskDir, String maskExt)
	{
		try
		{
			List<File> images = new ArrayList<File>();
			addImages(imageDir, images, imageDir, imageExt, maskDir, maskExt);

			System.out.println("Found " + images.size() + " images without masks");

			for (int i = 0; i < images.size(); i++)
			{
				File imageFile = images.get(i);

				System.out.println("Deleting " + imageFile + " (" + (i + 1) + "/" + images.size()
						+ " - " + ((i + 1) * 100 / images.size()) + "%)");

				imageFile.delete();
			}

			System.out.println("Complete");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected static void addImages(File dir, List<File> images, File imageDir, String imageExt,
			File maskDir, String maskExt)
	{
		File[] files = dir.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isDirectory())
				{
					addImages(file, images, imageDir, imageExt, maskDir, maskExt);
				}
				else if (file.getName().toLowerCase().endsWith("." + imageExt))
				{
					String filename = file.getAbsolutePath();
					filename = filename.substring(imageDir.getAbsolutePath().length());
					filename = filename.substring(0, filename.length() - imageExt.length());
					File mask = new File(maskDir, filename + maskExt);
					if (!mask.exists())
					{
						images.add(file);
					}
				}
			}
		}
	}
}
