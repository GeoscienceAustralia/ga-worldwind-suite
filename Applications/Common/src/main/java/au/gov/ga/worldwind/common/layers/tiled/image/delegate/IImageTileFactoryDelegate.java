package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Level;
import au.gov.ga.worldwind.common.layers.delegate.ITileFactoryDelegate;

/**
 * Sub-interface of the {@link ITileFactoryDelegate} for ease-of-use with layers
 * that use {@link DelegatorTextureTile}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IImageTileFactoryDelegate extends ITileFactoryDelegate<DelegatorTextureTile, Sector, Level>
{
}
