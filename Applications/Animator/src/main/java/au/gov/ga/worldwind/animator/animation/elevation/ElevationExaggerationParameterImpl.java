package au.gov.ga.worldwind.animator.animation.elevation;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getElevationExaggerationNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggerationImpl;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Default implementation of the {@link ElevationExaggerationParameter}
 * interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
@EditableParameter(bound = true, minValue = 0, maxValue = Double.MAX_VALUE, units = "x")
public class ElevationExaggerationParameterImpl extends ParameterBase implements ElevationExaggerationParameter
{
	private static final long serialVersionUID = 2010L;

	private static final String DEFAULT_NAME = "Exaggeration";

	private ElevationExaggeration exaggerator;

	/**
	 * Constructor for de-serialisation.
	 */
	ElevationExaggerationParameterImpl()
	{
	};

	public ElevationExaggerationParameterImpl(Animation animation, ElevationExaggeration exaggerator)
	{
		this(null, animation, exaggerator);
	}

	public ElevationExaggerationParameterImpl(String name, Animation animation, ElevationExaggeration exaggerator)
	{
		super(getNameOrDefault(name, exaggerator), animation);

		Validate.notNull(exaggerator, "An exaggerator is required");
		this.exaggerator = exaggerator;

		setDefaultValue(exaggerator.getExaggeration());
	}

	protected static String getNameOrDefault(String name, ElevationExaggeration exaggerator)
	{
		return name != null ? name : getMessageOrDefault(getElevationExaggerationNameKey(), DEFAULT_NAME,
				exaggerator.getElevationBoundary());
	}

	@Override
	protected String getDefaultName()
	{
		return DEFAULT_NAME;
	}

	@Override
	public ParameterValue getCurrentValue()
	{
		return ParameterValueFactory.createParameterValue(this, exaggerator.getExaggeration(),
				animation.getCurrentFrame());
	}

	@Override
	public ElevationExaggeration getElevationExaggeration()
	{
		return exaggerator;
	}

	@Override
	public void apply()
	{
		if (!isEnabled())
		{
			return;
		}
		int frame = animation.getCurrentFrame();
		ParameterValue value = getValueAtFrame(frame);
		applyValueIfEnabled(value.getValue(), frame);
	}

	@Override
	protected void doApplyValue(double value)
	{
		exaggerator.setExaggeration(value);
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getElevationExaggerationElementName();
	}

	@Override
	protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();

		double boundary =
				WWXML.getDouble(element,
						ATTRIBUTE_PATH_PREFIX + constants.getElevationExaggerationBoundaryAttributeName(), null);
		double exaggeration =
				WWXML.getDouble(parameterElement,
						ATTRIBUTE_PATH_PREFIX + constants.getParameterAttributeDefaultValue(), null);

		ElevationExaggeration exaggerator = new ElevationExaggerationImpl(exaggeration, boundary);
		return new ElevationExaggerationParameterImpl(name, animation, exaggerator);
	}

	@Override
	protected void saveParameterToXml(Element element, Element parameterElement, AnimationFileVersion version)
	{
		super.saveParameterToXml(element, parameterElement, version);

		AnimationIOConstants constants = version.getConstants();
		WWXML.setDoubleAttribute(element, constants.getElevationExaggerationBoundaryAttributeName(),
				exaggerator.getElevationBoundary());
	}
}
