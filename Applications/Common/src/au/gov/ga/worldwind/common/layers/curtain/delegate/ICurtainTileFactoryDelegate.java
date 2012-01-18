package au.gov.ga.worldwind.common.layers.curtain.delegate;

import au.gov.ga.worldwind.common.layers.curtain.CurtainLevel;
import au.gov.ga.worldwind.common.layers.curtain.Segment;
import au.gov.ga.worldwind.common.layers.delegate.ITileFactoryDelegate;

/**
 * {@link ITileFactoryDelegate} for the {@link DelegatorTiledCurtainLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ICurtainTileFactoryDelegate extends
		ITileFactoryDelegate<DelegatorCurtainTextureTile, Segment, CurtainLevel>
{
}
