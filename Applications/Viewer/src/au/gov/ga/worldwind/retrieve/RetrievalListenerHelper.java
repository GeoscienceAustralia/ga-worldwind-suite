package au.gov.ga.worldwind.retrieve;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.layers.Mercator.BasicMercatorTiledImageLayer;
import gov.nasa.worldwind.layers.Mercator.MercatorTiledImageLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.layers.rpf.RPFTiledImageLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import gov.nasa.worldwind.util.Tile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.layers.geonames.GeoNamesLayer;
import au.gov.ga.worldwind.layers.tiled.image.delegate.DelegatorTiledImageLayer;

public class RetrievalListenerHelper
{
	private static final Field[] LAYER_FIELDS;
	private static final Field[] ELEVATION_MODEL_FIELDS;
	private static final Field[] TILE_FIELDS;

	static
	{
		Class<?>[] classes =
				new Class<?>[] { DelegatorTiledImageLayer.class, TiledImageLayer.class,
						BasicElevationModel.class, BasicTiledImageLayer.class,
						PlaceNameLayer.class, RPFTiledImageLayer.class, SurfaceImage.class,
						MercatorTiledImageLayer.class, BasicMercatorTiledImageLayer.class,
						GeoNamesLayer.class };

		// Search classes above for declared classes that implement
		// RetrivalPostProcess AND contain a Field which is a subclass of Tile,
		// and add those Fields to an array
		LAYER_FIELDS =
				getMatchingFieldsFromInnerImplementingClasses(classes,
						RetrievalPostProcessor.class, Layer.class);
		ELEVATION_MODEL_FIELDS =
				getMatchingFieldsFromInnerImplementingClasses(classes,
						RetrievalPostProcessor.class, ElevationModel.class);
		TILE_FIELDS =
				getMatchingFieldsFromInnerImplementingClasses(classes,
						RetrievalPostProcessor.class, Tile.class);
	}

	private static Field[] getMatchingFieldsFromInnerImplementingClasses(Class<?>[] search,
			Class<?> interfaceType, Class<?> fieldType)
	{
		List<Field> fields = new ArrayList<Field>();
		for (Class<?> c : search)
		{
			//get a list of declared (inner) classes of the class
			Class<?>[] innerClasses = c.getDeclaredClasses();
			for (Class<?> innerClass : innerClasses)
			{
				if (interfaceType.isAssignableFrom(innerClass))
				{
					Field[] declaredFields = innerClass.getDeclaredFields();
					for (Field field : declaredFields)
					{
						if (fieldType.isAssignableFrom(field.getType()))
						{
							field.setAccessible(true);
							fields.add(field);
						}
					}
				}
			}
		}
		return fields.toArray(new Field[fields.size()]);
	}

	public static Layer getLayer(Retriever retriever)
	{
		return getObject(retriever, LAYER_FIELDS, Layer.class);
	}

	public static ElevationModel getElevationModel(Retriever retriever)
	{
		return getObject(retriever, ELEVATION_MODEL_FIELDS, ElevationModel.class);
	}

	public static Tile getTile(Retriever retriever)
	{
		return getObject(retriever, TILE_FIELDS, Tile.class);
	}

	private static <E> E getObject(Retriever retriever, Field[] fields, Class<? extends E> type)
	{
		if (retriever instanceof URLRetriever)
		{
			URLRetriever ur = (URLRetriever) retriever;
			RetrievalPostProcessor rpp = ur.getPostProcessor();
			for (Field field : fields)
			{
				if (field.getDeclaringClass().isAssignableFrom(rpp.getClass()))
				{
					try
					{
						Object object = field.get(rpp);
						if (object != null && type.isAssignableFrom(object.getClass()))
						{
							return type.cast(object);
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		return null;
	}
}
