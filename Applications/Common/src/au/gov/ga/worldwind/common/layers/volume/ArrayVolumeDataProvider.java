package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;

import com.sun.opengl.util.BufferUtil;

public class ArrayVolumeDataProvider extends AbstractVolumeDataProvider
{
	@Override
	protected boolean doLoadData(URL url, VolumeLayer layer)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(url.openStream());
			xSize = ois.readInt();
			ySize = ois.readInt();
			zSize = ois.readInt();
			double minLatitude = ois.readDouble();
			double maxLatitude = ois.readDouble();
			double minLongitude = ois.readDouble();
			double maxLongitude = ois.readDouble();
			sector = Sector.fromDegrees(minLatitude, maxLatitude, minLongitude, maxLongitude);
			top = ois.readDouble();
			depth = ois.readDouble();
			noDataValue = ois.readFloat();

			positions = new ArrayList<Position>(xSize * ySize);
			for (int y = 0; y < ySize; y++)
			{
				double latitude = minLatitude + (y / (double) (ySize - 1)) * (maxLatitude - minLatitude);
				for (int x = 0; x < xSize; x++)
				{
					double longitude = minLongitude + (x / (double) (xSize - 1)) * (maxLongitude - minLongitude);
					positions.add(Position.fromDegrees(latitude, longitude, ois.readDouble()));
				}
			}

			data = BufferUtil.newFloatBuffer(xSize * ySize * zSize);
			for (int i = 0; i < data.limit(); i++)
			{
				data.put(ois.readFloat());
			}
			data.rewind();

			layer.dataAvailable(this);
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static void saveVolumeDataProviderToArrayFile(AbstractVolumeDataProvider provider, File file)
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeInt(provider.xSize);
			oos.writeInt(provider.ySize);
			oos.writeInt(provider.zSize);
			oos.writeDouble(provider.sector.getMinLatitude().degrees);
			oos.writeDouble(provider.sector.getMaxLatitude().degrees);
			oos.writeDouble(provider.sector.getMinLongitude().degrees);
			oos.writeDouble(provider.sector.getMaxLongitude().degrees);
			oos.writeDouble(provider.top);
			oos.writeDouble(provider.depth);
			oos.writeFloat(provider.noDataValue);
			for (Position position : provider.positions)
			{
				oos.writeDouble(position.elevation);
			}
			for (int i = 0; i < provider.data.limit(); i++)
			{
				oos.writeFloat(provider.data.get(i));
			}
			oos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
