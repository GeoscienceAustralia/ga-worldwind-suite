package au.gov.ga.worldwind.tiler.shapefile;

import java.io.IOException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.io.EndianDataInputStream;


public class ShapefileHeader
{
	public static final int SHAPEFILE_ID = 9994;
	public static final int VERSION = 1000;

	private final int fileCode;
	private final int fileLength;
	private final int version;
	private final int shapeType;
	private final Envelope bounds;

	public ShapefileHeader(EndianDataInputStream file) throws IOException
	{
		//  file.setLittleEndianMode(false);
		fileCode = file.readIntBE();
		// if(DEBUG)System.out.println("Sfh->Filecode "+fileCode);
		if (fileCode != SHAPEFILE_ID)
			System.err.println("Sfh->WARNING filecode " + fileCode
					+ " not a match for documented shapefile code " + SHAPEFILE_ID);

		for (int i = 0; i < 5; i++)
		{
			/*int tmp =*/file.readIntBE();
			// if(DEBUG)System.out.println("Sfh->blank "+tmp);
		}
		fileLength = file.readIntBE();

		//  file.setLittleEndianMode(true);
		version = file.readIntLE();
		shapeType = file.readIntLE();

		//read in the bounding box
		double minx = file.readDoubleLE();
		double miny = file.readDoubleLE();
		double maxx = file.readDoubleLE();
		double maxy = file.readDoubleLE();
		bounds = new Envelope(minx, maxx, miny, maxy);

		//skip remaining unused bytes
		// file.setLittleEndianMode(false);//well they may not be unused forever...
		file.skipBytes(32);
	}

	public int getShapeType()
	{
		return shapeType;
	}

	public int getVersion()
	{
		return version;
	}

	public Envelope getBounds()
	{
		return bounds;
	}

	public String toString()
	{
		String res =
				new String("Sf-->type " + fileCode + " size " + fileLength + " version " + version
						+ " Shape Type " + shapeType);
		return res;
	}
}
