package au.gov.ga.worldwind.animator.animation.elevation;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getElevationNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.terrain.AnimationElevationLoader;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifierFactory;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifierImpl;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;
import au.gov.ga.worldwind.animator.terrain.exaggeration.VerticalExaggerationElevationModel;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * The default implementation of the {@link AnimatableElevation} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DefaultAnimatableElevation extends AnimatableBase implements AnimatableElevation
{
	private static final long serialVersionUID = 20100929L;

	private static final String DEFAULT_NAME = "Elevation";
	
	private Animation animation;
	
	private Map<Double, ElevationExaggerationParameter> exaggerationParameters = new TreeMap<Double, ElevationExaggerationParameter>();

	private List<ElevationModelIdentifier> elevationModelIdentifiers = new ArrayList<ElevationModelIdentifier>();
	
	private CompoundElevationModel containerModel = new CompoundElevationModel();
	private VerticalExaggerationElevationModel rootElevationModel = new VerticalExaggerationElevationModel(containerModel);

	/**
	 * For use in de-serialisation. Not to be used generally.
	 */
	@SuppressWarnings("unused")
	private DefaultAnimatableElevation() { }
	
	/**
	 * Initialise the animatable elevation with no elevation models or exaggerators
	 */
	public DefaultAnimatableElevation(Animation animation)
	{
		super(getMessage(getElevationNameKey()) == null ? DEFAULT_NAME : getMessage(getElevationNameKey()));
		
		Validate.notNull(animation, "An animation is required");
		
		this.animation = animation;
	}
	
	/**
	 * Initialise the animatable elevation with the provided elevation models and exaggerators
	 */
	public DefaultAnimatableElevation(Animation animation, List<ElevationModel> elevationModels, List<ElevationExaggeration> exaggerators)
	{
		this(animation);
		
		for (ElevationModel model : elevationModels)
		{
			containerModel.addElevationModel(model);
			elevationModelIdentifiers.add(ElevationModelIdentifierFactory.createFromElevationModel(model));
		}
		
		rootElevationModel.addExaggerators(exaggerators);
		for (ElevationExaggeration exaggerator : exaggerators)
		{
			exaggerationParameters.put(exaggerator.getElevationBoundary(), new ElevationExaggerationParameterImpl(animation, exaggerator));
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
		
		ElevationExaggerationParameterImpl exaggerationParameter = new ElevationExaggerationParameterImpl(animation, exaggerator);
		exaggerationParameters.put(exaggerator.getElevationBoundary(), exaggerationParameter);
		
		fireAddEvent(exaggerationParameter);
	}
	
	/**
	 * For use during de-serialisation
	 */
	private void addElevationExaggerationParameter(ElevationExaggerationParameter parameter)
	{
		if (parameter == null)
		{
			return;
		}
		
		rootElevationModel.addExaggerator(parameter.getElevationExaggeration());
		exaggerationParameters.put(parameter.getElevationExaggeration().getElevationBoundary(), parameter);
	}
	
	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();
		
		Element result = WWXML.appendElement(parent, constants.getAnimatableElevationName());
		WWXML.setTextAttribute(result, constants.getAnimatableElevationAttributeName(), getName());
		WWXML.setBooleanAttribute(result, constants.getAnimatableElevationAttributeEnabled(), isEnabled());
		
		// Add the elevation model identifiers
		Element identifierContainer = WWXML.appendElement(result, constants.getAnimatableElevationModelContainerName());
		for (ElevationModelIdentifier identifier : getElevationModelIdentifiers())
		{
			Element identifierElement = WWXML.appendElement(identifierContainer, constants.getAnimatableElevationModelIdentifierName());
			WWXML.setTextAttribute(identifierElement, constants.getAnimatableElevationModelIdentifierAttributeName(), identifier.getName());
			WWXML.setTextAttribute(identifierElement, constants.getAnimatableElevationModelIdentifierAttributeUrl(), identifier.getLocation());
		}
		
		// Add the exaggeration parameters
		for (ElevationExaggerationParameter parameter : exaggerationParameters.values())
		{
			result.appendChild(parameter.toXml(result, version));
		}
		
		return result;
	}

	@Override
	public Animatable fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version ID is required");
		Validate.notNull(context, "A context is required");
		
		AnimationIOConstants constants = version.getConstants();
		
		switch (version)
		{
			case VERSION020:
			{
				Validate.isTrue(context.hasKey(constants.getAnimationKey()), "An animation is required in context.");
				
				DefaultAnimatableElevation result = new DefaultAnimatableElevation((Animation)context.getValue(constants.getAnimationKey()));
				
				// Load the models
				Element[] modelIdentifierElements = WWXML.getElements(element, "./" + constants.getAnimatableElevationModelContainerName() + "/*", null);
				if (modelIdentifierElements != null)
				{
					for (Element modelIdentifierElement : modelIdentifierElements)
					{
						String modelName = WWXML.getText(modelIdentifierElement, ATTRIBUTE_PATH_PREFIX + constants.getAnimatableElevationModelIdentifierAttributeName());
						String modelUrl = WWXML.getText(modelIdentifierElement, ATTRIBUTE_PATH_PREFIX + constants.getAnimatableElevationModelIdentifierAttributeUrl());
						result.addElevationModel(new ElevationModelIdentifierImpl(modelName, modelUrl));
					}
				}
				
				// Load the exaggerators
				ElevationExaggerationParameterImpl parameterFactory = new ElevationExaggerationParameterImpl();
				Element[] exaggeratorElements = WWXML.getElements(element, "./" + constants.getElevationExaggerationName(), null);
				if (exaggeratorElements != null)
				{
					for (Element exaggeratorElement : exaggeratorElements)
					{
						ElevationExaggerationParameter parameter = (ElevationExaggerationParameter)parameterFactory.fromXml(exaggeratorElement, version, context);
						result.addElevationExaggerationParameter(parameter);
					}
				}
				
				return result;
			}
		}
		
		return null;
	}

}
