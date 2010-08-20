/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.camera;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.animator.util.message.MessageSourceAccessor;

/**
 * A default implementation of the {@link Camera} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class CameraImpl implements Camera
{
	private static final long serialVersionUID = 20100819L;

	private static final String DEFAULT_CAMERA_NAME = "Render Camera";
	
	private Parameter eyeLat;
	private Parameter eyeLon;
	private Parameter eyeElevation;
	
	private Parameter lookAtLat;
	private Parameter lookAtLon;
	private Parameter lookAtElevation;

	private Collection<Parameter> parameters;
	
	/**
	 * Constructor. Initialises the camera parameters.
	 */
	@SuppressWarnings("serial")
	public CameraImpl(Animation animation)
	{
		Validate.notNull(animation, "An animation instance is required");
		
		eyeLat = new ParameterBase(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraEyeLatNameKey()), animation)
		{
			@Override
			public ParameterValue getCurrentValue(AnimationContext context)
			{
				double value = context.getView().getEyePosition().getLatitude().getDegrees();
				return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
			}
		};
		
		eyeLon = new ParameterBase(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraEyeLonNameKey()), animation)
		{
			@Override
			public ParameterValue getCurrentValue(AnimationContext context)
			{
				double value = context.getView().getEyePosition().getLongitude().getDegrees();
				return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
			}
		};
		
		eyeElevation = new ParameterBase(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraEyeZoomNameKey()), animation)
		{
			@Override
			public ParameterValue getCurrentValue(AnimationContext context)
			{
				double value = context.getView().getEyePosition().getElevation();
				return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
			}
		};
		
		lookAtLat = new ParameterBase(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraLookatLatNameKey()), animation)
		{
			@Override
			public ParameterValue getCurrentValue(AnimationContext context)
			{
				double value = ((OrbitView)context.getView()).getCenterPosition().getLatitude().getDegrees();
				return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
			}
		};
		
		lookAtLon = new ParameterBase(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraLookatLonNameKey()), animation)
		{
			@Override
			public ParameterValue getCurrentValue(AnimationContext context)
			{
				double value = ((OrbitView)context.getView()).getCenterPosition().getLongitude().getDegrees();
				return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
			}
		};
		
		lookAtElevation = new ParameterBase(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraLookatZoomNameKey()), animation)
		{
			@Override
			public ParameterValue getCurrentValue(AnimationContext context)
			{
				double value = ((OrbitView)context.getView()).getCenterPosition().getElevation();
				return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
			}
		};
		
		parameters = new ArrayList<Parameter>(6);
		parameters.add(eyeLat);
		parameters.add(eyeLon);
		parameters.add(eyeElevation);
		parameters.add(lookAtLat);
		parameters.add(lookAtLon);
		parameters.add(lookAtElevation);
		
	}
	
	/** The name of this camera */
	private String name = MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraNameKey(), DEFAULT_CAMERA_NAME); 

	@Override
	public void apply(AnimationContext animationContext, int frame)
	{
		Position eye = Position.fromDegrees(eyeLat.getValueAtFrame(animationContext, frame).getValue(),
											eyeLon.getValueAtFrame(animationContext, frame).getValue(), 
											animationContext.unapplyZoomScaling(eyeElevation.getValueAtFrame(animationContext, frame).getValue()));
		
		Position center = Position.fromDegrees(lookAtLat.getValueAtFrame(animationContext, frame).getValue(),
											   lookAtLon.getValueAtFrame(animationContext, frame).getValue(), 
											   animationContext.unapplyZoomScaling(lookAtElevation.getValueAtFrame(animationContext, frame).getValue()));
		
		View view = animationContext.getView();
		view.stopMovement();
		view.setOrientation(eye, center);
	}

	@Override
	public String getRestorableState()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restoreState(String stateInXml)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Parameter getEyeLat()
	{
		return eyeLat;
	}

	@Override
	public Parameter getEyeLon()
	{
		return eyeLon;
	}

	@Override
	public Parameter getEyeElevation()
	{
		return eyeElevation;
	}

	@Override
	public Parameter getLookAtLat()
	{
		return lookAtLat;
	}

	@Override
	public Parameter getLookAtLon()
	{
		return lookAtLon;
	}

	@Override
	public Parameter getLookAtElevation()
	{
		return lookAtElevation;
	}
	
	@Override
	public Collection<Parameter> getParameters()
	{
		return Collections.unmodifiableCollection(parameters);
	}
	
	@Override
	public Collection<Parameter> getEnabledParameters()
	{
		Collection<Parameter> result = new ArrayList<Parameter>();
		for (Iterator<Parameter> parameterIterator = getParameters().iterator(); parameterIterator.hasNext();)
		{
			Parameter parameter = (Parameter) parameterIterator.next();
			if (parameter.isEnabled())
			{
				result.add(parameter);
			}
		}
		return result;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}
}
