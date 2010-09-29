package au.gov.ga.worldwind.tiler.seismic;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.gdal.gdal.Dataset;

import au.gov.ga.worldwind.tiler.gdal.GDALTile;
import au.gov.ga.worldwind.tiler.gdal.GDALTileParameters;
import au.gov.ga.worldwind.tiler.gdal.GDALUtil;

public class SeismicTiler
{
	public static void main(String[] args) throws Exception
	{
		File input = new File("D:/Seismic/S310_SWM13_final_mig_unscal.bmp");
		File output = new File("D:/Seismic/tiles3");
		Dataset dataset = GDALUtil.open(input);
		Insets insets = new Insets(0, 0, 0, 0);
		int tilesize = 512;
		String format = "jpg";

		int width = dataset.GetRasterXSize() - insets.left - insets.right;
		int height = dataset.GetRasterYSize() - insets.top - insets.bottom;

		int levels = levelCount(width, height, tilesize);
		System.out.println("Level count = " + levels);

		int printWidth = width, printHeight = height;
		for (int level = levels - 1; level >= 0; level--)
		{
			System.out.println("Level " + level + ": " + printWidth + "x" + printHeight);
			printWidth = (printWidth + 1) / 2;
			printHeight = (printHeight + 1) / 2;
		}

		File levelDir = new File(output, String.valueOf(levels - 1));
		levelDir.mkdirs();

		int xStrips = Math.max(1, tilesize / width);
		int yStrips = Math.max(1, tilesize / height);
		int rows = (height - 1) / (tilesize * xStrips) + 1;
		int cols = (width - 1) / (tilesize * yStrips) + 1;

		for (int y = 0, row = 0; y < height; y += tilesize * xStrips, row++)
		{
			for (int x = 0, col = 0; x < width; x += tilesize * yStrips, col++)
			{
				int w = Math.min(tilesize * yStrips / xStrips, width - x);
				int h = Math.min(tilesize * xStrips / yStrips, height - y);

				Rectangle src = new Rectangle(x + insets.left, y + insets.top, w, h);

				GDALTileParameters parameters = new GDALTileParameters(dataset, src.getSize(), src);
				GDALTile tile = new GDALTile(parameters);
				BufferedImage image = tile.getAsImage();

				File imageFile = new File(levelDir, row + "_" + col + "." + format);
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

					int firstCol = col * (lastRows == 1 ? 4 : 2);
					int c0 = colMultiplier * (firstCol);
					int c1 = colMultiplier * (firstCol + 1);
					int c2 = colMultiplier * (firstCol + colDelta);
					int c3 = colMultiplier * (firstCol + colDelta + 1);

					File src0 = new File(lastLevelDir, r0 + "_" + c0 + "." + format);
					File src1 = new File(lastLevelDir, r1 + "_" + c1 + "." + format);
					File src2 = new File(lastLevelDir, r2 + "_" + c2 + "." + format);
					File src3 = new File(lastLevelDir, r3 + "_" + c3 + "." + format);

					File imageFile = new File(levelDir, row + "_" + col + "." + format);

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

					BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
					Graphics2D g = image.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BILINEAR);

					int x = 0, y = 0;
					if (img0 != null)
					{
						g.drawImage(img0, x, y, w0, h0, null);
					}
					if (img1 != null)
					{
						x += lastCols == 1 ? 0 : w0;
						y += lastCols == 1 ? h0 : 0;
						g.drawImage(img1, x, y, w1, h1, null);
					}
					if (img2 != null)
					{
						x += lastCols == 1 ? 0 : lastRows == 1 ? w1 : -w0;
						y += lastCols == 1 ? h1 : lastRows == 1 ? 0 : h0;
						g.drawImage(img2, x, y, w2, h2, null);
					}
					if (img3 != null)
					{
						x += lastCols == 1 ? 0 : w2;
						y += lastCols == 1 ? h2 : 0;
						g.drawImage(img3, x, y, w3, h3, null);
					}

					g.dispose();
					ImageIO.write(image, format, imageFile);
				}
			}
		}

		//separate strips
		/*for (int level = levels - 1; level >= 0; level--)
		{
			levelDir = new File(output, String.valueOf(level));

			File[] files = levelDir.listFiles();
			for (File file : files)
			{
				if (!file.getName().toLowerCase().endsWith(format))
					continue;

				BufferedImage image = ImageIO.read(file);
				if (image.getWidth() <= tilesize && image.getHeight() <= tilesize)
				{
					continue;
				}
				else if (image.getWidth() * image.getHeight() > tilesize * tilesize)
				{
					throw new IllegalStateException("Too many pixels!");
				}

				BufferedImage tile =
						new BufferedImage(tilesize, tilesize, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = tile.createGraphics();

				int splitWidth = (image.getWidth() - 1) / tilesize + 1;
				int splitHeight = (image.getHeight() - 1) / tilesize + 1;

				if (splitWidth > 1)
				{
					int h = tilesize / image.getHeight();
					h = tilesize / h;
					for (int s = 0, sx = 0, dy = 0; s < splitWidth; s++, sx += tilesize, dy += h)
					{
						int w = Math.min(tilesize, image.getWidth() - sx);
						g.drawImage(image, 0, dy, w, dy + image.getHeight(), sx, 0, sx + w,
								image.getHeight(), null);
					}
				}

				g.dispose();
				ImageIO.write(tile, format, file);
			}
		}*/
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
}
