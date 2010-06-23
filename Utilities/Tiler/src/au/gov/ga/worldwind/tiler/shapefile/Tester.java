package au.gov.ga.worldwind.tiler.shapefile;

import java.io.File;

import au.gov.ga.worldwind.tiler.application.Console;
import au.gov.ga.worldwind.tiler.util.ProgressReporter;



public class Tester
{
	public static void main(String[] args)
	{
		/*try
		{
			String input = "C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete.shp";
			//String input = "C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/f/GSHHS_f_L1.shp";
			String output = "C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete_vivid.shp";
			//String output = "C:/WINNT/Profiles/u97852/Desktop/calcrete/coast_full.shp";
			com.vividsolutions.jump.io.ShapefileReader reader =
					new com.vividsolutions.jump.io.ShapefileReader();
			FeatureCollection fc = reader.read(new DriverProperties(input));

			ShapefileWriter writer = new ShapefileWriter();
			writer.write(fc, new DriverProperties(new File(output).getAbsolutePath()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/

		/*try
		{
			File shpInput = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete.shp");
			ShapefileReader reader = new ShapefileReader(shpInput);
			reader.open();
			FeatureCollection collection = new FeatureDataset(reader.getSchema());
			Feature feature;
			while ((feature = reader.read()) != null)
			{
				collection.add(feature);
			}
			reader.close();

			String output = "C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete_vivid_mine.shp";
			ShapefileWriter writer = new ShapefileWriter();
			writer.write(collection, new DriverProperties(new File(output).getAbsolutePath()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/

		//File input = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete.shp");
		File input = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/c/GSHHS_c_L1.shp");
		File output = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/tiled");
		double lzts = 36;
		int level = 0;
		ProgressReporter progress = new Console.ConsoleProgressReporter();
		ShapefileTiler.tile(input, output, level, lzts, progress);

		/*File shpInput = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete.shp");
		File shxInput = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete.shx");
		File dbfInput = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete.dbf");
		File shpOutput = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete_test.shp");
		File shxOutput = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete_test.shx");
		File dbfOutput = new File("C:/WINNT/Profiles/u97852/Desktop/calcrete/calcrete_test.dbf");

		try
		{
			ShpInputStream sis = new ShpInputStream(new FileInputStream(shpInput));
			EsriGraphicList list = sis.getGeometry();

			//ShxInputStream xis = new ShxInputStream(new FileInputStream(shxInput));

			DbfInputStream dis = new DbfInputStream(new FileInputStream(dbfInput));
			DbfTableModel dtm = new DbfTableModel(dis);

			if (list != null && dtm != null && dtm.getRowCount() == list.size())
			{
				list.setTable(dtm);
			}

			OMGraphic graphic = list.getOMGraphicAt(0);
			System.out.println(graphic);
			
			

			ShpOutputStream sos = new ShpOutputStream(new FileOutputStream(shpOutput));
			int[][] index = sos.writeGeometry(list);

			ShxOutputStream xos = new ShxOutputStream(new FileOutputStream(shxOutput));
			xos.writeIndex(index, list.getType(), list.getExtents());

			DbfOutputStream dos = new DbfOutputStream(new FileOutputStream(dbfOutput));
			dos.writeModel(list.getTable());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}
}
