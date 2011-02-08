package au.gov.ga.worldwind.tiler.ribbon;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.gdal.gdal.Dataset;

import au.gov.ga.worldwind.tiler.application.Executable;
import au.gov.ga.worldwind.tiler.gdal.GDALTile;
import au.gov.ga.worldwind.tiler.gdal.GDALTileParameters;
import au.gov.ga.worldwind.tiler.gdal.GDALUtil;
import au.gov.ga.worldwind.tiler.ribbon.definition.LayerDefinitionCreator;
import au.gov.ga.worldwind.tiler.util.Util;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * A tiler that is used to process long, thin images for use in <code>CurtainImageTiles</code>
 * (e.g. seismic, AEM, MT etc.)
 */
public class RibbonTiler
{
	public static void main(String[] args) throws Exception
	{
		Executable.setGDALEnvironmentVariables();
		
		RibbonTilingContext context = new RibbonTilingContext();
		JCommander jCommander = null;
		try
		{
			jCommander = new JCommander();
			jCommander.addObject(context);
			jCommander.parse(args);
		}
		catch (ParameterException e)
		{
			System.out.println(e.getLocalizedMessage());
			jCommander.usage();
			return;
		}
		
		new RibbonTiler().tileRibbon(context);
	}
	
	public void tileRibbon(RibbonTilingContext context) throws Exception
	{
		if (context == null)
		{
			throw new IllegalArgumentException("A contex is required");
		}
		
		log(context, "Tiled on: " + new Date(), true);
		log(context, "Source: " + context.getSourceFile().getAbsolutePath(), true);
		log(context, "", true);
		
		Dataset dataset = GDALUtil.open(context.getSourceFile());

		int width = dataset.GetRasterXSize() - context.getInsets().left - context.getInsets().right;
		int height = dataset.GetRasterYSize() - context.getInsets().top - context.getInsets().bottom;
		context.setSourceImageSize(new Dimension(width, height));
		
		int levels = levelCount(width, height, context.getTilesize());
		context.setNumLevels(levels);
		log(context, "Level count = " + levels, true);

		printLevelsSummary(context, width, height, levels);

		//calculate blank rows per column (columns of constant color)
		log(context, "Calculating columns to remove from the top and bottom of the image...", false);
		File topFile = new File(context.getTilesetRoot(), context.getTilesetName() + ".top.dat");
		File bottomFile = new File(context.getTilesetRoot(), context.getTilesetName() + ".bottom.dat");

		int[] constantPixelsFromTop = RibbonTilerUtils.loadIntArrayFromFile(topFile);
		int[] constantPixelsFromBottom = RibbonTilerUtils.loadIntArrayFromFile(bottomFile);
		if (!(constantPixelsFromTop == null || 
				constantPixelsFromBottom == null || 
				constantPixelsFromTop.length != width || 
				constantPixelsFromBottom.length != width))
		{
			log(context, "Loaded removal columns from previous calculations", false);
		}
		else
		{
			constantPixelsFromTop = new int[width];
			constantPixelsFromBottom = new int[width];

			int constantWidth = Math.max(1, 10 * context.getTilesize() * context.getTilesize() / height);
			for (int startX = 0; startX < width; startX += constantWidth)
			{
				int w = Math.min(constantWidth, width - startX);

				//get an image of the full height, 1 pixel wide at column x
				Rectangle src = new Rectangle(context.getInsets().left + startX, context.getInsets().top, w, height);
				GDALTileParameters parameters = new GDALTileParameters(dataset, src.getSize(), src);
				GDALTile tile = new GDALTile(parameters);
				BufferedImage image = tile.getAsImage();

				log(context, (100 * (startX + 1) / width) + "% done", false);

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

			RibbonTilerUtils.saveIntArrayToFile(constantPixelsFromTop, topFile);
			RibbonTilerUtils.saveIntArrayToFile(constantPixelsFromBottom, bottomFile);
		}

		//calculate tiling parameters
		int xStrips = Math.max(1, context.getTilesize() / width);
		int yStrips = Math.max(1, context.getTilesize() / height);
		int rows = (height - 1) / (context.getTilesize() * xStrips) + 1;
		int cols = (width - 1) / (context.getTilesize() * yStrips) + 1;
		File levelDir = new File(context.getTilesetRoot(), String.valueOf(levels - 1));

		//create top level tiles
		log(context, "Creating top level tiles...", false);
		for (int y = 0, row = 0; y < height; y += context.getTilesize() * xStrips, row++)
		{
			//create row directory
			String rowPadded = Util.paddedInt(row, 4);
			File rowDir = new File(levelDir, rowPadded);
			rowDir.mkdirs();

			int h = Math.min(context.getTilesize() * xStrips / yStrips, height - y);

			for (int x = 0, col = 0; x < width; x += context.getTilesize() * yStrips, col++)
			{
				File imageFile = tileFile(levelDir, row, col, context.getFormat());
				if (imageFile.exists())
				{
					continue;
				}

				int w = Math.min(context.getTilesize() * yStrips / xStrips, width - x);

				Rectangle src = new Rectangle(x + context.getInsets().left, y + context.getInsets().top, w, h);
				GDALTileParameters parameters = new GDALTileParameters(dataset, src.getSize(), src);
				GDALTile tile = new GDALTile(parameters);
				BufferedImage image = tile.getAsImage();

				image = removeConstantColumns(image, constantPixelsFromTop, constantPixelsFromBottom, x, y, width, height, context.isMask());

				ImageIO.write(image, context.getFormat(), imageFile);
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

			log(context, "", true);
			log(context, "Level " + level, true);
			log(context, "lastRows x lastCols = " + lastRows + " x " + lastCols, true);
			log(context, "Rows x Cols = " + rows + " x " + cols, true);
			log(context, "xStrips,yStrips = " + xStrips + "," + yStrips, true);
			log(context, "", true);
			
			File lastLevelDir = levelDir;
			levelDir = new File(context.getTilesetRoot(), String.valueOf(level));
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

					File imageFile = tileFile(levelDir, row, col, context.getFormat());
					if (imageFile.exists())
					{
						continue;
					}

					int firstCol = col * (lastRows == 1 ? 4 : 2);
					int c0 = colMultiplier * (firstCol);
					int c1 = colMultiplier * (firstCol + 1);
					int c2 = colMultiplier * (firstCol + colDelta);
					int c3 = colMultiplier * (firstCol + colDelta + 1);

					File src0 = tileFile(lastLevelDir, r0, c0, context.getFormat());
					File src1 = tileFile(lastLevelDir, r1, c1, context.getFormat());
					File src2 = tileFile(lastLevelDir, r2, c2, context.getFormat());
					File src3 = tileFile(lastLevelDir, r3, c3, context.getFormat());

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

					int type = context.isMask() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
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
					ImageIO.write(image, context.getFormat(), imageFile);
				}
			}
		}
		
		if (context.isCopySource())
		{
			try
			{
				Util.copyFileToDirectory(context.getSourceFile(), context.getTilesetRoot(), true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		if (context.isGenerateLayerDefinition())
		{
			LayerDefinitionCreator creator = new LayerDefinitionCreator();
			creator.createDefinition(context);
			log(context, "", true);
			log(context, "Layer definition file generated at " + context.getLayerDefinitionFile().getAbsolutePath(), true);
		}
	}

	private void printLevelsSummary(RibbonTilingContext context, int width,
			int height, int levels) {
		int printWidth = width, printHeight = height;
		for (int level = levels - 1; level >= 0; level--)
		{
			log(context, "Level " + level + ": " + printWidth + "" + "x" + printHeight, true);
			printWidth = (printWidth + 1) / 2;
			printHeight = (printHeight + 1) / 2;
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
	
	private static void log(RibbonTilingContext context, String msg, boolean addToTilingLog)
	{
		try
		{
			context.getStdWriter().write(msg + '\n');
			if (addToTilingLog)
			{
				context.getLogWriter().write(msg + '\n'); 
			}
			context.getStdWriter().flush();
			context.getLogWriter().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
