package au.gov.ga.worldwind.tiler.mask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MaskZipper
{
	public static void main(String[] args)
	{
		File imageDir = new File("D:/Groundwater Data/layers/tiles/bh_spot2p5m_wgs84_ecw_tiles");
		File maskDir = new File("D:/Groundwater Data/layers/tiles/bh_spot2p5m_wgs84_ecw_mask");
		File outputDir = new File("D:/Groundwater Data/layers/tiles/bh_spot2p5m_wgs84_ecw");
		String imageExt = "jpg";
		String maskExt = "png";
		String outputExt = "zip";
		zip(imageDir, imageExt, maskDir, maskExt, outputDir, outputExt);
	}

	public static void zip(File imageDir, String imageExt, File maskDir, String maskExt,
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

				int indexOfLastPeriod = imageFile.getName().lastIndexOf('.');
				String filename = imageFile.getName().substring(0, indexOfLastPeriod);

				imageFile =
						copyfile(imageFile, new File(filename + "."
								+ (maskFile == null ? maskExt : imageExt)));
				maskFile =
						maskFile == null ? null : copyfile(maskFile, new File(filename + "_mask."
								+ maskExt));

				System.out.println("Writing " + outputFile + " (" + (i + 1) + "/" + images.size()
						+ " - " + ((i + 1) * 100 / images.size()) + "%)");

				String command =
						"7za a -tzip \""
								+ outputFile.getAbsolutePath()
								+ "\" \""
								+ imageFile.getAbsolutePath()
								+ "\""
								+ (maskFile == null ? "" : " \"" + maskFile.getAbsolutePath()
										+ "\"");
				Process process = Runtime.getRuntime().exec(command);
				process.waitFor();

				imageFile.delete();
				if (maskFile != null)
					maskFile.delete();
			}

			System.out.println("Complete");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static File copyfile(File src, File dst) throws IOException
	{
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		return dst;
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
				else if (file.getName().toLowerCase().endsWith("." + maskExt))
				{
					String filename = file.getAbsolutePath();
					filename = filename.substring(imageDir.getAbsolutePath().length());
					filename = filename.substring(0, filename.length() - maskExt.length());
					File output = new File(outputDir, filename + outputExt);
					if (output.exists())
					{
						System.err.println("Output already exists: " + output);
					}
					else
					{
						images.add(file);
						masks.add(null);
						outputs.add(output);
					}
				}
			}
		}
	}
}
