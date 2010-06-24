package au.gov.ga.worldwind.tiler.shapefile;

import java.io.File;

import au.gov.ga.worldwind.tiler.application.Console;
import au.gov.ga.worldwind.tiler.util.ProgressReporter;

public class Tester
{
	public static void main(String[] args)
	{
		//File input = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete.shp");
		File input0 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/c/GSHHS_c_L1.shp");
		File input1 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/l/GSHHS_l_L1.shp");
		File input2 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/i/GSHHS_i_L1.shp");
		File input3 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/h/GSHHS_h_L1.shp");
		File input4 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/f/GSHHS_f_L1.shp");
		
		File output = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/tiled");
		double lzts = 36;
		int level = 1;
		File input = input1;
		
		ProgressReporter progress = new Console.ConsoleProgressReporter();
		ShapefileTiler.tile(input, output, level, lzts, progress);
	}
}
