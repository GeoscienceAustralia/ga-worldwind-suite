package tiler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import javax.imageio.ImageIO;

import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconstConstants;

import util.GDALTile;
import util.GDALUtil;
import util.Overviewer;
import util.Sector;

public class TilerConsole
{
	public static void main(String[] args)
	{
		new TilerConsole();
	}

	public TilerConsole()
	{
		File file = new File("D:/SW Margins/sonne/SONNE_100B.ers");
		File outputDir = new File("D:/SW Margins/sonne/tiledB");
		String outputExt = "bil";
		double lztd = 20d;
		int tilesize = 150;
		long nodata = -9999;
		boolean overviews = true;
		int threadCount = 1;
		boolean saveAlpha = false;
		int outputType = gdalconstConstants.GDT_Int16;

		Dataset dataset = GDALUtil.open(file);
		Sector sector = GDALUtil.getSector(dataset);
		int levels = GDALUtil.levelCount(dataset, lztd, sector, tilesize);
		double width = dataset.getRasterXSize();
		double height = dataset.getRasterYSize();
		int bufferTypeSize = 2;

		int level = levels - 1;
		//level = 1;
		double tilesizedegrees = Math.pow(0.5, level) * lztd;
		System.out.println("Calculating level " + level + ", tile size "
				+ tilesizedegrees + " ("
				+ (tilesizedegrees * height / sector.getDeltaLongitude())
				+ " x " + (tilesizedegrees * width / sector.getDeltaLatitude())
				+ ")");

		int minX = GDALUtil.getTileX(sector.getMinLongitude(), level, lztd);
		int maxX = GDALUtil.getTileX(sector.getMaxLongitude(), level, lztd);
		int minY = GDALUtil.getTileY(sector.getMinLatitude(), level, lztd);
		int maxY = GDALUtil.getTileY(sector.getMaxLatitude(), level, lztd);

		int size = (maxX - minX + 1) * (maxY - minY + 1);
		int count = 0;
		for (int Y = minY; Y <= maxY; Y++)
		{
			File rowDir = new File(outputDir, String.valueOf(level));
			rowDir = new File(rowDir, GDALUtil.paddedInt(Y, 4));
			if (!rowDir.exists())
			{
				rowDir.mkdirs();
			}

			for (int X = minX; X <= maxX; X++)
			{
				count++;
				System.out.println("Tile (" + X + "," + Y + "), " + count + "/"
						+ size + " (" + (count * 100 / size) + "%) (column "
						+ (X - minX + 1) + "/" + (maxX - minX + 1) + ", row "
						+ (Y - minY + 1) + "/" + (maxY - minY + 1) + ")");

				final double lat1 = (Y * tilesizedegrees) - 90;
				final double lon1 = (X * tilesizedegrees) - 180;
				final double lat2 = lat1 + tilesizedegrees;
				final double lon2 = lon1 + tilesizedegrees;

				final File dst = new File(rowDir, GDALUtil.paddedInt(Y, 4) + "_"
						+ GDALUtil.paddedInt(X, 4) + "." + outputExt);
				if (dst.exists())
				{
					System.out.println(dst.getAbsolutePath()
							+ " already exists.");
				}
				else
				{
					GDALTile tile = new GDALTile(dataset, tilesize, tilesize,
							lat1, lon1, lat2, lon2, saveAlpha);
					tile = tile.convertToType(outputType); //TODO only for bil
					tile.fillNodata(nodata);
					bufferTypeSize = tile.getBufferTypeSize();
					if (outputExt.toLowerCase().equals("bil"))
					{
						try
						{
							ByteBuffer bb = tile.getBuffer();
							bb.rewind();
							FileChannel fc = new RandomAccessFile(dst, "rw")
									.getChannel();
							MappedByteBuffer mbb = fc.map(MapMode.READ_WRITE,
									0, bb.limit());
							mbb.order(bb.order());
							mbb.put(bb);
							fc.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						BufferedImage image = tile.getAsImage();
						try
						{
							ImageIO.write(image, outputExt, dst);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

		if (overviews)
		{
			System.out.println();
			System.out.println("Creating overviews...");

			if (outputExt.toLowerCase().equals("bil"))
			{
				Overviewer.createBilOverviews(outputDir, tilesize, tilesize,
						threadCount, bufferTypeSize, ByteOrder.LITTLE_ENDIAN,
						nodata);
			}
			else
			{
				Overviewer.createImageOverviews(outputDir, outputExt, tilesize,
						tilesize, saveAlpha, threadCount);
			}
		}

		System.out.println("Done");
	}
}
