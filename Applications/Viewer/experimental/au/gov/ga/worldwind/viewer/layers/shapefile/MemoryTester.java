package au.gov.ga.worldwind.viewer.layers.shapefile;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileUtils;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.common.util.Util;

public class MemoryTester
{
	public static void main(String[] args)
	{
		File dir = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/tiled");
		int levels = 5;
		int rows = 5;
		int cols = 10;
		int ignore = 10;

		DrawContext dc = new FakeDrawContext();
		List<ShapefileTileData> list = new ArrayList<ShapefileTileData>();

		long totalMemoryUse = 0;
		int totalPoints = 0;
		int totalShapes = 0;

		int ignoreCount = 0;
		int level = 0;
		//for (int level = 0; level < levels; level++)
		{
			int mult = (int) Math.pow(2, level);
			for (int row = 0; row < rows * mult; row++)
			{
				for (int col = 0; col < cols * mult; col++)
				{
					File f =
							new File(dir.getAbsolutePath() + "/" + level + "/"
									+ Util.paddedInt(row, 4) + "/" + Util.paddedInt(row, 4) + "_"
									+ Util.paddedInt(col, 4) + ".zip");

					if (f.exists())
					{
						try
						{
							Shapefile shapefile = ShapefileUtils.openZippedShapefile(f);
							long before = getMemoryUse();
							ShapefileTileData data =
									new ShapefileTileData(Sector.EMPTY_SECTOR, shapefile);
							long after = getMemoryUse();
							list.add(data);

							if (ignoreCount++ > ignore)
							{
								//totalPoints += data.getPointCount();
								totalMemoryUse += (after - before);
								//totalShapes += data.getShapeCount();
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						if (totalPoints != 0)
							System.out
									.println("AVERAGE memory per point = "
											+ (totalMemoryUse / totalPoints) + " ("
											+ totalMemoryUse + " / " + totalPoints
											+ ", total shapes = " + totalShapes + ")");
					}
				}
			}

			System.out.println("FOR LEVEL " + level + ", totalPoints = " + totalPoints
					+ ", totalMemoryUse = " + totalMemoryUse);

			//for merged lines:
			//80 (633520 / 7853, total shapes = 38)

			//for lines:
			//137 (1082648 / 7853, total shapes = 868) (heaps more shapes)

			//for polygons:
			//78 (1233464 / 15732, total shapes = 38)

			//for points:
			//79 (626528 / 7853, total shapes = 38)


			//449128 for extra 830 shapes
			//541 per shape
		}
	}

	// PRIVATE //
	private static long fSLEEP_INTERVAL = 100;

	private static long getMemoryUse()
	{
		putOutTheGarbage();
		long totalMemory = Runtime.getRuntime().totalMemory();

		putOutTheGarbage();
		long freeMemory = Runtime.getRuntime().freeMemory();

		return (totalMemory - freeMemory);
	}

	private static void putOutTheGarbage()
	{
		collectGarbage();
		collectGarbage();
	}

	private static void collectGarbage()
	{
		try
		{
			System.gc();
			Thread.sleep(fSLEEP_INTERVAL);
			System.runFinalization();
			Thread.sleep(fSLEEP_INTERVAL);
		}
		catch (InterruptedException ex)
		{
			ex.printStackTrace();
		}
	}
}
