package au.gov.ga.worldwind.animator.animation.elevation;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.*;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.terrain.AnimationElevationLoader;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifierFactory;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;
import au.gov.ga.worldwind.animator.terrain.exaggeration.VerticalExaggerationElevationModel;

/**
 * The default implementation of the {@link AnimatableElevation} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DefaultAnimatableElevation extends AnimatableBase implements AnimatableElevation
{
	private static final long serialVersionUID = 20100929L;
	
	private Map<Double, ElevationExaggerationParameter> exaggerationParameters = new HashMap<Double, ElevationExaggerationParameter>();

	private List<ElevationModelIdentifier> elevationModelIdentifiers = new ArrayList<ElevationModelIdentifier>();
	
	private CompoundElevationModel containerModel;
	private VerticalExaggerationElevationModel rootElevationModel;
	
	public DefaultAnimatableElevation()
	{
		containerModel = new CompoundElevationModel();
		rootElevationModel = new VerticalExaggerationElevationModel(containerModel);
		
		setName(getMessage(getElevationNameKey()));
	}
	
	/**
	 * Initialise the elevation with the provided elevation models and exaggerators
	 */
	public DefaultAnimatableElevation(List<ElevationModel> elevationModels, List<ElevationExaggeration> exaggerators)
	{
		this();
		
		for (ElevationModel model : elevationModels)
		{
			containerModel.addElevationModel(model);
			elevationModelIdentifiers.add(ElevationModelIdentifierFactory.createFromElevationModel(model));
		}
		
		rootElevationModel.addExaggerators(exaggerators);
		for (ElevationExaggeration exaggerator : exaggerators)
		{
			exaggerationParameters.put(exaggerator.getElevationBoundary(), new ElevationExaggerationParameterImpl(exaggerator));
		}
	}
	
	@Override
	public Collection<Parameter> getParameters()
	{
		return new ArrayList<Parameter>(exaggerationParameters.values());
	}

	@Override
	public VerticalExaggerationElevationModel getRootElevationModel()
	{
		return rootElevationModel;
	}

	@Override
	protected void doApply(AnimationContext animationContext, int frame)
	{
		for (ElevationExaggerationParameter p : exaggerationParameters.values())
		{
			if (p.isEnabled())
			{
				p.apply(animationContext, frame);
			}
		}
	}

	@Override
	public List<ElevationModelIdentifier> getElevationModelIdentifiers()
	{
		return Collections.unmodifiableList(elevationModelIdentifiers);
	}

	@Override
	public boolean hasElevationModel(ElevationModelIdentifier modelIdentifier)
	{
		return elevationModelIdentifiers.contains(modelIdentifier);
	}
	
	@Override
	public void addElevationModel(ElevationModelIdentifier modelIdentifier)
	{
		if (modelIdentifier == null || hasElevationModel(modelIdentifier))
		{
			return;
		}
		ElevationModel loadedModel = AnimationElevationLoader.loadElevationModel(modelIdentifier);
		if (loadedModel == null)
		{
			return;
		}
		
		containerModel.addElevationModel(loadedModel);
		elevationModelIdentifiers.add(modelIdentifier);

		fireChangeEvent(loadedModel);
	}
	
	@Override
	public void addElevationExaggerator(ElevationExaggeration exaggerator)
	{
		if (exaggerator == null)
		{
			return;
		}
		
		rootElevationModel.addExaggerator(exaggerator);
		
		ElevationExaggerationParameterImpl exaggerationParameter = new ElevationExaggerationParameterImpl(exaggerator);
		exaggerationParameters.put(exaggerator.getElevationBoundary(), exaggerationParameter);
		
		fireAddEvent(exaggerationParameter);
	}
	
	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Animatable fromXml(Element element, AnimationFileVersion versionId, AVList context)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
