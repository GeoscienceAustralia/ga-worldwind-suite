package au.gov.ga.worldwind.tiler.seismic;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.gdal.gdal.Dataset;

import au.gov.ga.worldwind.tiler.application.Executable;
import au.gov.ga.worldwind.tiler.gdal.GDALTile;
import au.gov.ga.worldwind.tiler.gdal.GDALTileParameters;
import au.gov.ga.worldwind.tiler.gdal.GDALUtil;
import au.gov.ga.worldwind.tiler.util.Util;

public class SeismicTiler
{
	public static void main(String[] args) throws Exception
	{
		Executable.setGDALEnvironmentVariables();
		
		boolean mask = false;
		File input = new File("c:/data/projects/AEM flight lines/10300.jpg");
		File output = new File("c:/data/projects/AEM flight lines/tiles");
		Dataset dataset = GDALUtil.open(input);
		Insets insets = new Insets(0, 0, 0, 0);
		int tilesize = 512;
		String format = mask ? "png" : "jpg";

		int width = dataset.GetRasterXSize() - insets.left - insets.right;
		int height = dataset.GetRasterYSize() - insets.top - insets.bottom;

		int levels = levelCount(width, height, tilesize);
		System.out.println("Level count = " + levels);

		int printWidth = width, printHeight = height;
		for (int level = levels - 1; level >= 0; level--)
		{
			System.out.println("Level " + level + ": " + printWidth + "" + "x" + printHeight);
			printWidth = (printWidth + 1) / 2;
			printHeight = (printHeight + 1) / 2;
		}

		//calculate blank rows per column (columns of constant color)
		System.out.println("Calculating columns to remove from the top and bottom of the image...");
		File topFile = new File(input.getParentFile(), input.getName() + ".top.dat");
		File bottomFile = new File(input.getParentFile(), input.getName() + ".bottom.dat");

		int[] constantPixelsFromTop = loadIntArrayFromFile(topFile);
		int[] constantPixelsFromBottom = loadIntArrayFromFile(bottomFile);
		if (!(constantPixelsFromTop == null || constantPixelsFromBottom == null || constantPixelsFromTop.length != width
				|| constantPixelsFromBottom.length != width))
		{
			System.out.println("Loaded from previous calculations");
		}
		else
		{
			constantPixelsFromTop = new int[width];
			constantPixelsFromBottom = new int[width];

			int constantWidth = Math.max(1, 10 * tilesize * tilesize / height);
			for (int startX = 0; startX < width; startX += constantWidth)
			{
				int w = Math.min(constantWidth, width - startX);

				//get an image of the full height, 1 pixel wide at column x
				Rectangle src = new Rectangle(insets.left + startX, insets.top, w, height);
				GDALTileParameters parameters = new GDALTileParameters(dataset, src.getSize(), src);
				GDALTile tile = new GDALTile(parameters);
				BufferedImage image = tile.getAsImage();

				System.out.println((100 * (startX + 1) / width) + "% done");

				for (int x = 0; x < w; x++)
				{
					int fromTop = 0;
					int fromBottom = 0;
					int lastColor = 0;
					for (int y = 0; y < height; y++)
					{
						int thisColor = image.getRGB(x, y);
						if (y > 0 && lastColor != thisColor)
						{
							break;
						}
						lastColor = thisColor;
						fromTop++;
					}

					if (fromTop < height)
					{
						for (int y = height - 1; y >= 0; y--)
						{
							int thisColor = image.getRGB(x, y);
							if (y < height - 1 && lastColor != thisColor)
							{
								break;
							}
							lastColor = thisColor;
							fromBottom++;
						}
					}

					constantPixelsFromTop[startX + x] = fromTop;
					constantPixelsFromBottom[startX + x] = fromBottom;
				}
			}

			saveIntArrayToFile(constantPixelsFromTop, topFile);
			saveIntArrayToFile(constantPixelsFromBottom, bottomFile);
		}

		//calculate tiling parameters
		int xStrips = Math.max(1, tilesize / width);
		int yStrips = Math.max(1, tilesize / height);
		int rows = (height - 1) / (tilesize * xStrips) + 1;
		int cols = (width - 1) / (tilesize * yStrips) + 1;
		File levelDir = new File(output, String.valueOf(levels - 1));

		//create top level tiles
		System.out.println("Creating top level tiles...");
		for (int y = 0, row = 0; y < height; y += tilesize * xStrips, row++)
		{
			//create row directory
			String rowPadded = Util.paddedInt(row, 4);
			File rowDir = new File(levelDir, rowPadded);
			rowDir.mkdirs();

			int h = Math.min(tilesize * xStrips / yStrips, height - y);

			for (int x = 0, col = 0; x < width; x += tilesize * yStrips, col++)
			{
				File imageFile = tileFile(levelDir, row, col, format);
				if (imageFile.exists())
					continue;

				int w = Math.min(tilesize * yStrips / xStrips, width - x);

				Rectangle src = new Rectangle(x + insets.left, y + insets.top, w, h);
				GDALTileParameters parameters = new GDALTileParameters(dataset, src.getSize(), src);
				GDALTile tile = new GDALTile(parameters);
				BufferedImage image = tile.getAsImage();

				image =
						removeConstantColumns(image, constantPixelsFromTop, constantPixelsFromBottom, x, y, width,
								height, mask);

				ImageIO.write(image, format, imageFile);
			}
		}

		//create overviews
		for (int level = levels - 2; level >= 0; level--)
		{
			int lastRows = rows;
			int lastCols = cols;

			if (cols == 1)
			{
				xStrips <<= 1;
				rows = (rows + 1) / 2;
			}
			if (rows == 1)
			{
				yStrips <<= 1;
				cols = (cols + 1) / 2;
			}

			rows = (rows + 1) / 2;
			cols = (cols + 1) / 2;

			System.out.println("Level " + level);
			System.out.println("lastRows x lastCols = " + lastRows + " x " + lastCols);
			System.out.println("Rows x Cols = " + rows + " x " + cols);
			System.out.println("xStrips,yStrips = " + xStrips + "," + yStrips);

			File lastLevelDir = levelDir;
			levelDir = new File(output, String.valueOf(level));
			levelDir.mkdirs();

			int rowMultiplier = lastRows == 1 ? 0 : 1;
			int rowDivisor = lastCols == 1 ? 1 : 2;
			int colMultiplier = lastCols == 1 ? 0 : 1;
			int colDelta = lastRows == 1 ? 2 : 0;

			for (int row = 0; row < rows; row++)
			{
				//if lastRows == 1: 0,0,0,0 / 0,0,0,0
				//if lastCols == 1: 0,1,2,3 / 4,5,6,7
				//            else: 0,0,1,1 / 2,2,3,3

				String rowPadded = Util.paddedInt(row, 4);
				File rowDir = new File(levelDir, rowPadded);
				rowDir.mkdirs();

				int firstRow = row * 4;
				int r0 = rowMultiplier * (firstRow + 0) / rowDivisor;
				int r1 = rowMultiplier * (firstRow + 1) / rowDivisor;
				int r2 = rowMultiplier * (firstRow + 2) / rowDivisor;
				int r3 = rowMultiplier * (firstRow + 3) / rowDivisor;

				for (int col = 0; col < cols; col++)
				{
					//if lastCols == 1: 0,0,0,0 / 0,0,0,0
					//if lastRows == 1: 0,1,2,3 / 4,5,6,7
					//            else: 0,1,0,1 / 2,3,2,3

					File imageFile = tileFile(levelDir, row, col, format);
					if (imageFile.exists())
					{
						continue;
					}

					int firstCol = col * (lastRows == 1 ? 4 : 2);
					int c0 = colMultiplier * (firstCol);
					int c1 = colMultiplier * (firstCol + 1);
					int c2 = colMultiplier * (firstCol + colDelta);
					int c3 = colMultiplier * (firstCol + colDelta + 1);

					File src0 = tileFile(lastLevelDir, r0, c0, format);
					File src1 = tileFile(lastLevelDir, r1, c1, format);
					File src2 = tileFile(lastLevelDir, r2, c2, format);
					File src3 = tileFile(lastLevelDir, r3, c3, format);

					BufferedImage img0 = src0.exists() ? ImageIO.read(src0) : null;
					BufferedImage img1 = src1.exists() ? ImageIO.read(src1) : null;
					BufferedImage img2 = src2.exists() ? ImageIO.read(src2) : null;
					BufferedImage img3 = src3.exists() ? ImageIO.read(src3) : null;

					int w0 = img0 == null ? 0 : (img0.getWidth() + 1) / 2;
					int w1 = img1 == null ? 0 : (img1.getWidth() + 1) / 2;
					int w2 = img2 == null ? 0 : (img2.getWidth() + 1) / 2;
					int w3 = img3 == null ? 0 : (img3.getWidth() + 1) / 2;
					int h0 = img0 == null ? 0 : (img0.getHeight() + 1) / 2;
					int h1 = img1 == null ? 0 : (img1.getHeight() + 1) / 2;
					int h2 = img2 == null ? 0 : (img2.getHeight() + 1) / 2;
					int h3 = img3 == null ? 0 : (img3.getHeight() + 1) / 2;

					int w = w0 + (lastCols == 1 ? 0 : w1) + (lastRows == 1 ? w2 + w3 : 0);
					int h = h0 + (lastRows == 1 ? 0 : h2) + (lastCols == 1 ? h1 + h3 : 0);

					int type = mask ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
					BufferedImage image = new BufferedImage(w, h, type);
					Graphics2D g = image.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

					int x = 0;
					int y = 0;
					if (img0 != null)
					{
						g.drawImage(img0, x, y, w0, h0, null);
					}
					x += lastCols == 1 ? 0 : w0;
					y += lastCols == 1 ? h0 : 0;
					if (img1 != null)
					{
						g.drawImage(img1, x, y, w1, h1, null);
					}
					x += lastCols == 1 ? 0 : lastRows == 1 ? w1 : -w0;
					y += lastCols == 1 ? h1 : lastRows == 1 ? 0 : h0;
					if (img2 != null)
					{
						g.drawImage(img2, x, y, w2, h2, null);
					}
					x += lastCols == 1 ? 0 : w2;
					y += lastCols == 1 ? h2 : 0;
					if (img3 != null)
					{
						g.drawImage(img3, x, y, w3, h3, null);
					}

					g.dispose();
					ImageIO.write(image, format, imageFile);
				}
			}
		}
	}

	private static int levelCount(int width, int height, int tilesize)
	{
		float xCount = width / (float) tilesize;
		float yCount = height / (float) tilesize;
		int levels = 0;

		while (4 * xCount * yCount >= 1)
		{
			levels++;
			xCount /= 2f;
			yCount /= 2f;
		}
		return levels;
	}

	private static File tileFile(File levelDir, int row, int col, String ext)
	{
		String paddedRow = Util.paddedInt(row, 4);
		String paddedCol = Util.paddedInt(col, 4);
		File rowDir = new File(levelDir, paddedRow);
		return new File(rowDir, paddedRow + "_" + paddedCol + "." + ext);
	}

	private static BufferedImage removeConstantColumns(BufferedImage image, int[] constantPixelsFromTop,
			int[] constantPixelsFromBottom, int startX, int startY, int totalWidth, int totalHeight, boolean mask)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		int type = mask ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage newImage = new BufferedImage(width, height, type);

		for (int x = 0; x < width; x++)
		{
			int fromTop = constantPixelsFromTop[startX + x];
			int fromBottom = constantPixelsFromBottom[startX + x];

			for (int y = 0; y < height; y++)
			{
				boolean withinTop = y + startY < fromTop;
				boolean withinBottom = y + startY > totalHeight - 1 - fromBottom;
				if (withinTop || withinBottom)
				{
					newImage.setRGB(x, y, mask ? 0 : 0xffffffff);
				}
				else
				{
					newImage.setRGB(x, y, mask ? 0xffffffff : image.getRGB(x, y));
				}
			}
		}

		return newImage;
	}

	private static void saveIntArrayToFile(int[] array, File file)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos = new DataOutputStream(fos);
			for (int i : array)
			{
				dos.writeInt(i);
			}
			dos.close();
			fos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static int[] loadIntArrayFromFile(File file)
	{
		if (!file.exists())
			return null;

		int size = (int) (file.length() / 4l);
		try
		{
			FileInputStream fis = new FileInputStream(file);
			DataInputStream dis = new DataInputStream(fis);
			int[] array = new int[size];
			for (int i = 0; i < size; i++)
			{
				array[i] = dis.readInt();
			}
			return array;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
