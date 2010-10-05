package au.gov.ga.worldwind.animator.terrain;

import gov.nasa.worldwind.globes.ElevationModel;
import au.gov.ga.worldwind.animator.util.Nameable;

/**
 * Allows access to the identification details of an {@link ElevationModel}, including
 * it's location and name
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface ElevationModelIdentifier extends Nameable
{
	String getName();
	String getLocation();
}
