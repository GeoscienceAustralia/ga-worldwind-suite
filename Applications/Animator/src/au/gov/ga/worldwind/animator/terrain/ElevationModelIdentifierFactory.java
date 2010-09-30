package au.gov.ga.worldwind.animator.terrain;

import static au.gov.ga.worldwind.animator.util.Util.*;

import java.net.URL;

import au.gov.ga.worldwind.common.util.AVKeyMore;
import gov.nasa.worldwind.globes.ElevationModel;

/**
 * A factory class to create instances of {@link ElevationModelIdentifier}s
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class ElevationModelIdentifierFactory
{

	/**
	 * Creates a new {@link ElevationModelIdentifier} that identifies the provided {@link ElevationModel}
	 */
	public static ElevationModelIdentifier createFromElevationModel(ElevationModel model)
	{
		if (model == null)
		{
			return null;
		}
		
		String modelName = model.getName();
		if (isBlank(modelName))
		{
			return null;
		}
		
		URL modelUrl = (URL)model.getValue(AVKeyMore.CONTEXT_URL);
		if (modelUrl == null)
		{
			return null;
		}
		
		return new ElevationModelIdentifierImpl(modelName, modelUrl.toExternalForm());
	}
	
}
