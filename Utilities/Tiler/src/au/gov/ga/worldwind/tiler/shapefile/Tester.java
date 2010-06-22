package au.gov.ga.worldwind.tiler.shapefile;

import java.io.File;

import au.gov.ga.worldwind.tiler.application.Console;
import au.gov.ga.worldwind.tiler.util.ProgressReporter;

public class Tester
{
	public static void main(String[] args)
	{
		File input =
				new File(
						"C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete.shp");
		File output = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/tiled");
		double lzts = 36;
		int level = 0;
		ProgressReporter progress = new Console.ConsoleProgressReporter();
		ShapefileTiler.tile(input, output, level, lzts, progress);
	}
}
