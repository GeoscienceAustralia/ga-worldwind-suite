package au.gov.ga.worldwind.common.layers.curtain.delegate;

import au.gov.ga.worldwind.common.layers.curtain.CurtainLevel;
import au.gov.ga.worldwind.common.layers.curtain.Segment;
import au.gov.ga.worldwind.common.layers.delegate.ITileFactoryDelegate;

public interface ICurtainTileFactoryDelegate extends
		ITileFactoryDelegate<DelegatorCurtainTextureTile, Segment, CurtainLevel>
{
}
