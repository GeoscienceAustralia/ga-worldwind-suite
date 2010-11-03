package au.gov.ga.worldwind.animator.animation.camera;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import java.util.Collection;
import java.util.Collections;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.camera.StereoCameraParameter.EyeSeparationParameter;
import au.gov.ga.worldwind.animator.animation.camera.StereoCameraParameter.FocalLengthParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.XMLUtil;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.view.stereo.StereoView;

public class StereoCameraImpl extends CameraImpl implements StereoCamera
{
	protected Parameter focalLength;
	protected Parameter eyeSeparation;

	protected boolean dynamicStereo = true;

	public StereoCameraImpl(Animation animation)
	{
		super(MessageSourceAccessor.get().getMessage(
				AnimationMessageConstants.getStereoCameraNameKey(), DEFAULT_CAMERA_NAME), animation);
	}

	/**
	 * For deserialization
	 */
	protected StereoCameraImpl()
	{
		super();
	}

	@Override
	public Parameter getFocalLength()
	{
		return focalLength;
	}

	@Override
	public Parameter getEyeSeparation()
	{
		return eyeSeparation;
	}

	@Override
	public boolean isDynamicStereo()
	{
		return dynamicStereo;
	}

	@Override
	public void setDynamicStereo(boolean dynamicStereo)
	{
		this.dynamicStereo = dynamicStereo;
	}

	@Override
	protected void initialiseParameters(Animation animation)
	{
		focalLength = new FocalLengthParameter(animation);
		eyeSeparation = new EyeSeparationParameter(animation);

		super.initialiseParameters(animation);
	}

	@Override
	protected void doApply(AnimationContext animationContext, int frame)
	{
		super.doApply(animationContext, frame);

		View view = animationContext.getView();
		if (view instanceof StereoView)
		{
			StereoView stereo = (StereoView) view;
			double focalLength = this.focalLength.getValueAtFrame(frame).getValue();
			double eyeSeparation = this.eyeSeparation.getValueAtFrame(frame).getValue();
			stereo.getParameters().setDynamicStereo(dynamicStereo);
			stereo.getParameters().setFocalLength(focalLength);
			stereo.getParameters().setEyeSeparation(eyeSeparation);
		}
	}

	@Override
	public Collection<Parameter> getParameters()
	{
		if (parameters == null)
		{
			super.getParameters();
			parameters.add(focalLength);
			parameters.add(eyeSeparation);
		}
		return Collections.unmodifiableCollection(parameters);
	}

	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();

		Element result = super.toXml(parent, version);

		XMLUtil.setBooleanAttribute(result, constants.getCameraDynamicStereoElementName(),
				dynamicStereo);

		Element focalLengthElement =
				WWXML.appendElement(result, constants.getCameraFocalLengthElementName());
		focalLengthElement.appendChild(focalLength.toXml(focalLengthElement, version));

		Element eyeSeparationElement =
				WWXML.appendElement(result, constants.getCameraEyeSeparationElementName());
		eyeSeparationElement.appendChild(eyeSeparation.toXml(eyeSeparationElement, version));

		return result;
	}
	
	@Override
	protected String getCameraElementName(AnimationIOConstants constants)
	{
		return constants.getStereoCameraElementName();
	}

	@Override
	protected void setupFromXml(CameraImpl camera, Element element, AnimationFileVersion version,
			AVList context)
	{
		super.setupFromXml(camera, element, version, context);

		if (camera != null && camera instanceof StereoCameraImpl)
		{
			AnimationIOConstants constants = version.getConstants();
			StereoCameraImpl stereo = (StereoCameraImpl) camera;
			stereo.focalLength =
					new FocalLengthParameter().fromXml(
							WWXML.getElement(element, constants.getCameraFocalLengthElementName()
									+ "/" + constants.getParameterElementName(), null), version,
							context);
			stereo.eyeSeparation =
					new FocalLengthParameter().fromXml(
							WWXML.getElement(element, constants.getCameraEyeSeparationElementName()
									+ "/" + constants.getParameterElementName(), null), version,
							context);
			stereo.dynamicStereo =
					XMLUtil.getBoolean(element, constants.getCameraDynamicStereoElementName(), true);
		}
	}

	@Override
	protected CameraImpl createAnimatable()
	{
		return new StereoCameraImpl();
	}
}
