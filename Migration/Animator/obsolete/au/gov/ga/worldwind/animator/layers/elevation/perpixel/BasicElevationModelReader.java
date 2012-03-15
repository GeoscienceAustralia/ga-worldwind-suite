package au.gov.ga.worldwind.animator.layers.elevation.perpixel;

import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BasicElevationModelReader
{
	static
	{
		try
		{
			Class<BasicElevationModel> basicElevationModelClass = BasicElevationModel.class;
			Class<?> localTileClass = null;
			for (Class<?> declaredClass : basicElevationModelClass
					.getDeclaredClasses())
			{
				if (declaredClass.getSimpleName().equals("Tile"))
				{
					localTileClass = declaredClass;
					break;
				}
			}
			if (localTileClass == null)
				throw new ClassNotFoundException("Cannot find "
						+ basicElevationModelClass + "$Tile class");

			if (!Tile.class.isAssignableFrom(localTileClass))
				throw new ClassNotFoundException(Tile.class
						+ " is not assignable from " + localTileClass);

			Field localElevationsField = localTileClass
					.getDeclaredField("elevations");
			localElevationsField.setAccessible(true);

			tileClass = localTileClass;
			memoryCacheGetter = basicElevationModelClass.getDeclaredMethod(
					"getMemoryCache", new Class[] {});
			elevationsField = localElevationsField;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static Method memoryCacheGetter;
	private static Class<?> tileClass;
	private static Field elevationsField;

	private MemoryCache memoryCache;

	public BasicElevationModelReader(BasicElevationModel model)
	{
		try
		{
			memoryCache = (MemoryCache) memoryCacheGetter.invoke(model,
					new Object[] {});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public BufferWrapper getElevations(TileKey key)
	{
		Object tileObject = memoryCache.getObject(key);
		if (tileObject == null)
			return null;

		if (!tileObject.getClass().equals(tileClass))
			throw new IllegalStateException(tileObject.getClass()
					+ " is not an instance of " + tileClass);
		Tile tile = (Tile) tileObject;


		try
		{
			Object elevationsObject = elevationsField.get(tile);
			if (!(elevationsObject instanceof BufferWrapper))
				throw new IllegalStateException(elevationsObject
						+ " is not an instance of " + BufferWrapper.class);

			return (BufferWrapper) elevationsObject;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
