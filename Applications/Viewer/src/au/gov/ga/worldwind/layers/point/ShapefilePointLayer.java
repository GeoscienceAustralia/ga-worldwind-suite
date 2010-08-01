package au.gov.ga.worldwind.layers.point;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.util.VecBuffer;

import org.w3c.dom.Element;

public abstract class ShapefilePointLayer extends PointLayer
{
	protected boolean loaded = false;

	public ShapefilePointLayer(AVList params)
	{
		super(params);
	}

	public ShapefilePointLayer(Element domElement, AVList params)
	{
		super(domElement, params);
	}

	@Override
	protected boolean isLoaded()
	{
		return loaded;
	}

	@Override
	protected void load(String url)
	{
		Shapefile shapefile = new Shapefile(url);
		while (shapefile.hasNext())
		{
			ShapefileRecord record = shapefile.nextRecord();
			DBaseRecord attributes = record.getAttributes();

			for (int part = 0; part < record.getNumberOfParts(); part++)
			{
				VecBuffer buffer = record.getPointBuffer(part);
				int size = buffer.getSize();
				for (int i = 0; i < size; i++)
				{
					addPoint(buffer.getPosition(i), attributes);
				}
			}
		}

		loaded = true;
	}
}
