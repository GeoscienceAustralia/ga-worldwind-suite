package au.gov.ga.worldwind.animator.animation.elevation;

import java.util.List;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;
import au.gov.ga.worldwind.animator.terrain.exaggeration.VerticalExaggerationElevationModel;

/**
 * An interface for an {@link Animatable} object that allows control over the elevation model of an animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface AnimatableElevation extends Animatable
{

	/**
	 * @return The elevation model this instance is controlling
	 */
	VerticalExaggerationElevationModel getRootElevationModel();
	
	/**
	 * @return The list of model identifiers that identify the elevation models included in this {@link AnimatableElevation} 
	 */
	List<ElevationModelIdentifier> getElevationModelIdentifiers();
	
	/**
	 * @return Whether this {@link AnimatableElevation} has the elevation model identified by the provided {@link ElevationModelIdentifier} 
	 */
	boolean hasElevationModel(ElevationModelIdentifier modelIdentifier);

	/**
	 * Add the elevation model identified by the provided identifier to this {@link AnimatableElevation}, if it isn't already
	 */
	void addElevationModel(ElevationModelIdentifier modelIdentifier);
	
	/**
	 * Add the provided elevation exaggerator to this {@link AnimatableElevation}
	 */
	void addElevationExaggerator(ElevationExaggeration exaggerator);
}
