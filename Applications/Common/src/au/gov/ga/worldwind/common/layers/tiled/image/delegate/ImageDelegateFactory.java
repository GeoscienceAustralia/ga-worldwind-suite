package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import au.gov.ga.worldwind.common.layers.delegate.AbstractDelegateFactory;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader.ColorMapElevationImageReaderDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader.ShadedElevationImageReaderDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.nearestneighbor.NearestNeighborTextureTileFactoryDelegate;

/**
 * Factory which creates delegates for the {@link DelegatorTiledImageLayer}.
 * 
 * @author Michael de Hoog
 */
public class ImageDelegateFactory extends AbstractDelegateFactory
{
	private static ImageDelegateFactory instance = new ImageDelegateFactory();

	public static ImageDelegateFactory get()
	{
		return instance;
	}

	private ImageDelegateFactory()
	{
		super();

		//register the specific delegates applicable to this Image factory
		registerDelegate(ImageURLRequesterDelegate.class);
		registerDelegate(ImageLocalRequesterDelegate.class);

		registerDelegate(TextureTileFactoryDelegate.class);
		registerDelegate(NearestNeighborTextureTileFactoryDelegate.class);

		registerDelegate(ShadedElevationImageReaderDelegate.class);
		registerDelegate(ColorMapElevationImageReaderDelegate.class);
	}
}
