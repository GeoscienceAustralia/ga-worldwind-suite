package au.gov.ga.worldwind.animator.panels.animationbrowser;

import static org.junit.Assert.assertEquals;
import static au.gov.ga.worldwind.test.util.TestUtils.*;

import gov.nasa.worldwind.layers.CrosshairLayer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.CurrentlySelectedObject;
import au.gov.ga.worldwind.animator.animation.camera.CameraImpl;
import au.gov.ga.worldwind.animator.animation.layer.AnimatableLayer;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerOpacityParameter;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.util.message.StaticMessageSource;

/**
 * Unit tests for the {@link AnimationBrowserPanel}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class AnimationBrowserPanelTest
{

	private AnimationBrowserPanel classToBeTested;
	private Animation animation;
	
	private Mockery mockContext;
	
	private StaticMessageSource messageSource;
	
	@Before
	public void setup()
	{
		messageSource = new StaticMessageSource();
		MessageSourceAccessor.set(messageSource);
		
		mockContext = new Mockery();
		
		setupAnimation();
	}

	@Test
	public void testRemoveActionDisabledWhenNoSelection()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(null);
		
		BasicAction removeAnimationObjectAction = getRemoveAnimationObjectAction();
		assertEquals(false, removeAnimationObjectAction.isEnabled());
	}

	@Test
	public void testRemoveActionDisabledWhenCameraSelected()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(0));
		
		BasicAction removeAnimationObjectAction = getRemoveAnimationObjectAction();
		assertEquals(false, removeAnimationObjectAction.isEnabled());
	}
	
	@Test
	public void testRemoveActionEnabledWhenLayerSelected()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		
		BasicAction removeAnimationObjectAction = getRemoveAnimationObjectAction();
		assertEquals(true, removeAnimationObjectAction.isEnabled());
	}
	
	@Test
	public void testCurrentlySelectedObjectUpdatedWhenSelectionChanged()
	{
		CurrentlySelectedObject.set(createLayerWithSingleParameter("layer3"));
		
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		
		assertEquals(getAnimationObjects().get(1), CurrentlySelectedObject.get());
	}
	
	private void select(AnimationObject selectedObject)
	{
		JTree tree = getField(classToBeTested, "objectTree", JTree.class);
		if (selectedObject == null)
		{
			tree.setSelectionPath(null);
		}
		else
		{
			tree.setSelectionPath(new TreePath(selectedObject));
		}
	}

	private void addObjectsToAnimation(final Animatable... objects)
	{
		mockContext.checking(new Expectations(){{
			atLeast(1).of(animation).getAnimatableObjects();will(returnValue(Arrays.asList(objects)));
		}});
	}

	private List<Animatable> getAnimationObjects()
	{
		return new ArrayList<Animatable>(animation.getAnimatableObjects());
	}
	
	private Animatable createLayerWithSingleParameter(final String name)
	{
		messageSource.addMessage(AnimationMessageConstants.getOpacityParameterNameKey(), "Opacity");
		final Collection<Parameter> parameters = Arrays.asList(new Parameter[]{new LayerOpacityParameter(animation, new CrosshairLayer())});
		final AnimatableLayer result = mockContext.mock(AnimatableLayer.class, "Layer" + name);
		mockContext.checking(new Expectations(){{
			atLeast(1).of(result).getName();will(returnValue(name));
			atLeast(1).of(result).getParameters();will(returnValue(parameters));
			atLeast(1).of(result).isEnabled();will(returnValue(true));
			atLeast(1).of(result).isAllChildrenEnabled();will(returnValue(true));
		}});
		return result;
	}

	private Animatable createCamera()
	{
		return new CameraImpl(animation);
	}

	private void setupAnimation()
	{
		animation = mockContext.mock(Animation.class);
		mockContext.checking(new Expectations(){{
			atLeast(1).of(animation).addChangeListener(with(any(AnimationTreeModel.class)));
			atLeast(1).of(animation).getName();will(returnValue("Animation"));
		}});
	}
	
	private void createAnimationBrowserPanel()
	{
		classToBeTested = new AnimationBrowserPanel(animation);
	}
	
	private BasicAction getRemoveAnimationObjectAction()
	{
		return getField(classToBeTested, "removeAnimationObjectAction", BasicAction.class);
	}
}