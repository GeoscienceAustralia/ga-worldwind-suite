package au.gov.ga.worldwind.common.layers.data;

import au.gov.ga.worldwind.common.layers.Bounded;

/**
 * Generic interface for objects that provide data to {@link DataLayer}
 * implementations.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <L>
 *            {@link DataLayer} type for which this provider provides data.
 */
public interface DataProvider<L extends DataLayer> extends Bounded
{
	public void requestData(L layer);
}
