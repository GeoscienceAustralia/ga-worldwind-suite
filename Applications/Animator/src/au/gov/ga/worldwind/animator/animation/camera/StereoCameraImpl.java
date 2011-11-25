package au.gov.ga.worldwind.animator.animation.camera;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getStereoCameraNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import java.util.Collection;
import java.util.Collections;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.camera.StereoCameraParameter.EyeSeparationParameter;
import au.gov.ga.worldwind.animator.animation.camera.StereoCameraParameter.FocalLengthParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.common.util.XMLUtil;
import au.gov.ga.worldwind.common.view.stereo.StereoView;

public class StereoCameraImpl extends CameraImpl implements StereoCamera
{
	private static final long serialVersionUID = 20101111L;

	protected Parameter focalLength;
	protected Parameter eyeSeparation;

	protected boolean dynamicStereo = true;
	
	public StereoCameraImpl(Animation animation)
	{
		this(null, animation);
	}

	public StereoCameraImpl(String name, Animation animation)
	{
		super(nameOrDefaultName(name, getMessage(getStereoCameraNameKey())), animation);
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
		super.initialiseParameters(animation);
		focalLength = new FocalLengthParameter(animation);
		eyeSeparation = new EyeSeparationParameter(animation);
	}

	@Override
	protected void doApply()
	{
		super.doApply();

		View view = animation.getView();
		if (view instanceof StereoView)
		{
			StereoView stereo = (StereoView) view;
			int frame = animation.getCurrentFrame();
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
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getStereoCameraElementName();
	}

	@Override
	protected CameraImpl createCamera(String name, Animation animation)
	{
		return new StereoCameraImpl(name, animation);
	}

	@Override
	protected void setupCameraFromXml(CameraImpl camera, Element element, AnimationFileVersion version, AVList context)
	{
		super.setupCameraFromXml(camera, element, version, context);

		if (camera instanceof StereoCameraImpl)
		{
			AnimationIOConstants constants = version.getConstants();
			StereoCameraImpl stereo = (StereoCameraImpl) camera;
			stereo.focalLength =
					new FocalLengthParameter().fromXml(
							WWXML.getElement(element, constants.getCameraFocalLengthElementName(), null), version,
							context);
			stereo.eyeSeparation =
					new FocalLengthParameter().fromXml(
							WWXML.getElement(element, constants.getCameraEyeSeparationElementName(), null), version,
							context);
			stereo.dynamicStereo = XMLUtil.getBoolean(element, constants.getCameraDynamicStereoElementName(), true);
		}
	}

	@Override
	protected void saveAnimatableToXml(Element element, AnimationFileVersion version)
	{
		super.saveAnimatableToXml(element, version);
		XMLUtil.setBooleanAttribute(element, version.getConstants().getCameraDynamicStereoElementName(), dynamicStereo);
	}
}
