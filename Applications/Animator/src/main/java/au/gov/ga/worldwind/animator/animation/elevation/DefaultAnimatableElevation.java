package au.gov.ga.worldwind.animator.animation.elevation;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getElevationNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.terrain.AnimationElevationLoader;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifierFactory;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifierImpl;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;
import au.gov.ga.worldwind.animator.terrain.exaggeration.VerticalExaggerationElevationModel;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * The default implementation of the {@link AnimatableElevation} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DefaultAnimatableElevation extends AnimatableBase implements AnimatableElevation
{
	private static final long serialVersionUID = 20100929L;

	private static final String DEFAULT_NAME = "Elevation";

	private Map<Double, ElevationExaggerationParameter> exaggerationParameters =
			new TreeMap<Double, ElevationExaggerationParameter>();

	private Map<ElevationModelIdentifier, ElevationModel> elevationModelIdentification =
			new LinkedHashMap<ElevationModelIdentifier, ElevationModel>();

	private CompoundElevationModel containerModel = new CompoundElevationModel();
	private VerticalExaggerationElevationModel rootElevationModel = new VerticalExaggerationElevationModel(
			containerModel);

	/**
	 * For use in de-serialisation. Not to be used generally.
	 */
	@SuppressWarnings("unused")
	private DefaultAnimatableElevation()
	{
	}

	/**
	 * Initialise the animatable elevation with no elevation models or
	 * exaggerators
	 */
	public DefaultAnimatableElevation(Animation animation)
	{
		this(null, animation);
	}

	/**
	 * Initialise the animatable elevation with no elevation models or
	 * exaggerators
	 */
	public DefaultAnimatableElevation(String name, Animation animation)
	{
		super(name, animation);

		Validate.notNull(animation, "An animation is required");

		this.animation = animation;
		this.addChangeListener(animation);
	}

	/**
	 * Initialise the animatable elevation with the provided elevation models
	 * and exaggerators
	 */
	public DefaultAnimatableElevation(Animation animation, List<ElevationModel> elevationModels,
			List<ElevationExaggeration> exaggerators)
	{
		this(animation);

		for (ElevationModel model : elevationModels)
		{
			containerModel.addElevationModel(model);
			elevationModelIdentification.put(ElevationModelIdentifierFactory.createFromElevationModel(model), model);
		}

		rootElevationModel.addExaggerators(exaggerators);
		for (ElevationExaggeration exaggerator : exaggerators)
		{
			exaggerationParameters.put(exaggerator.getElevationBoundary(), new ElevationExaggerationParameterImpl(
					animation, exaggerator));
		}
	}
	
	@Override
	protected String getDefaultName()
	{
		return getMessageOrDefault(getElevationNameKey(), DEFAULT_NAME);
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
	protected void doApply()
	{
		for (ElevationExaggerationParameter p : exaggerationParameters.values())
		{
			if (p.isEnabled())
			{
				p.apply();
			}
		}
	}

	@Override
	public List<ElevationModelIdentifier> getElevationModelIdentifiers()
	{
		return Collections.unmodifiableList(new ArrayList<ElevationModelIdentifier>(elevationModelIdentification
				.keySet()));
	}

	@Override
	public boolean hasElevationModel(ElevationModelIdentifier modelIdentifier)
	{
		return elevationModelIdentification.containsKey(modelIdentifier);
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
		elevationModelIdentification.put(modelIdentifier, loadedModel);

		fireAddEvent(loadedModel);
	}

	@Override
	public void removeElevationModel(ElevationModelIdentifier modelIdentifier)
	{
		if (modelIdentifier == null || !hasElevationModel(modelIdentifier))
		{
			return;
		}

		ElevationModel model = elevationModelIdentification.get(modelIdentifier);
		containerModel.removeElevationModel(model);
		elevationModelIdentification.remove(modelIdentifier);

		fireRemoveEvent(model);
	}

	@Override
	public void addElevationExaggerator(ElevationExaggeration exaggerator)
	{
		if (exaggerator == null)
		{
			return;
		}

		rootElevationModel.addExaggerator(exaggerator);

		ElevationExaggerationParameterImpl exaggerationParameter =
				new ElevationExaggerationParameterImpl(animation, exaggerator);
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
	public void removeElevationExaggerator(ElevationExaggeration exaggerator)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getAnimatableElevationElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		DefaultAnimatableElevation result = new DefaultAnimatableElevation(name, animation);
		AnimationIOConstants constants = version.getConstants();

		// Load the models
		Element[] modelIdentifierElements =
				WWXML.getElements(element, "./" + constants.getAnimatableElevationModelContainerName() + "/*", null);
		if (modelIdentifierElements != null)
		{
			for (Element modelIdentifierElement : modelIdentifierElements)
			{
				String modelName =
						WWXML.getText(modelIdentifierElement,
								ATTRIBUTE_PATH_PREFIX + constants.getAnimatableAttributeName());
				String modelUrl =
						WWXML.getText(modelIdentifierElement,
								ATTRIBUTE_PATH_PREFIX + constants.getAnimatableAttributeUrl());
				result.addElevationModel(new ElevationModelIdentifierImpl(modelName, modelUrl));
			}
		}

		// Load the exaggerators
		ElevationExaggerationParameterImpl parameterFactory = new ElevationExaggerationParameterImpl();
		Element[] exaggeratorElements =
				WWXML.getElements(element, "./" + constants.getElevationExaggerationElementName(), null);
		if (exaggeratorElements != null)
		{
			for (Element exaggeratorElement : exaggeratorElements)
			{
				ElevationExaggerationParameter parameter =
						(ElevationExaggerationParameter) parameterFactory.fromXml(exaggeratorElement, version, context);
				result.addElevationExaggerationParameter(parameter);
			}
		}

		return result;
	}

	@Override
	protected void saveAnimatableToXml(Element element, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();

		// Add the elevation model identifiers
		Element identifierContainer =
				WWXML.appendElement(element, constants.getAnimatableElevationModelContainerName());
		for (ElevationModelIdentifier identifier : getElevationModelIdentifiers())
		{
			Element identifierElement =
					WWXML.appendElement(identifierContainer, constants.getAnimatableElevationModelIdentifierName());
			WWXML.setTextAttribute(identifierElement, constants.getAnimatableAttributeName(), identifier.getName());
			WWXML.setTextAttribute(identifierElement, constants.getAnimatableAttributeUrl(), identifier.getLocation());
		}

		// Add the exaggeration parameters
		for (ElevationExaggerationParameter parameter : exaggerationParameters.values())
		{
			parameter.toXml(element, version);
		}
	}
}
