/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.tiler.shapefile;

import java.io.File;

import au.gov.ga.worldwind.tiler.application.Console;
import au.gov.ga.worldwind.tiler.util.LatLon;
import au.gov.ga.worldwind.tiler.util.ProgressReporter;

public class Tester
{
	public static void main(String[] args)
	{
		//File input = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete.shp");
		/*File input0 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/c/GSHHS_c_L1.shp");
		File input1 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/l/GSHHS_l_L1.shp");
		File input2 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/i/GSHHS_i_L1.shp");
		File input3 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/h/GSHHS_h_L1.shp");
		File input4 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/f/GSHHS_f_L1.shp");*/
		File input = new File("E:/World/water/worldwaterbodies/worldwaterbodies_noaustralia.shp");
		//File input = new File("E:/World/water/worldwaterbodies/polygonthatbreakstiler.shp");

		File output = new File("E:/World/water/worldwaterbodies_noaustralia_shp");
		double lzts = 180;
		int level = 9;
		LatLon origin = LatLon.DEFAULT_ORIGIN;
		//File input = input1;

		ProgressReporter progress = new Console.ConsoleProgressReporter();
		ShapefileTiler.tile(input, output, level, lzts, origin, progress);
	}
}
