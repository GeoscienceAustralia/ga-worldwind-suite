package au.gov.ga.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.geom.Sector;

import java.io.File;
import java.net.URL;
import java.util.List;

import au.gov.ga.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.worldwind.common.layers.model.ModelLayer;
import au.gov.ga.worldwind.common.layers.model.ModelProvider;
import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.URLUtil;

/**
 * Implementation of a {@link ModelProvider} which reads data from a GOCAD file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadModelProvider extends AbstractDataProvider<ModelLayer> implements ModelProvider
{
	private FastShape shape;

	@Override
	public Sector getSector()
	{
		if (shape == null)
			return null;
		return shape.getSector();
	}

	@Override
	protected boolean doLoadData(URL url, ModelLayer layer)
	{
		File file = URLUtil.urlToFile(url);
		List<FastShape> shapes = GocadFactory.read(file);
		if (shapes != null && shapes.size() > 0)
		{
			//only use the first shape read from the GOCAD file
			shape = shapes.get(0);
			layer.setShape(shape);
			return true;
		}
		return false;
	}
}
