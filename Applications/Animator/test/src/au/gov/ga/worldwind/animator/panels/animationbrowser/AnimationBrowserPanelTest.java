package au.gov.ga.worldwind.animator.panels.animationbrowser;

import static au.gov.ga.worldwind.test.util.TestUtils.getField;
import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.layers.CrosshairLayer;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JToolBar;
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
	
	// Flags used to inspect object removal
	private boolean confirmationRequested;
	private boolean confirmRemoveObject;
	
	@Before
	public void setup()
	{
		messageSource = new StaticMessageSource();
		MessageSourceAccessor.set(messageSource);
		
		mockContext = new Mockery();
		
		setupAnimation();
		
		confirmationRequested = false;
		denyRemovalConfirmation();
	}

	@Test
	public void testRemoveActionDisabledWhenNoSelection()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(null);
		
		assertEquals(false, getRemoveAnimationObjectAction().isEnabled());
	}

	@Test
	public void testRemoveActionDisabledWhenCameraSelected()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(0));
		
		assertEquals(false, getRemoveAnimationObjectAction().isEnabled());
	}
	
	@Test
	public void testRemoveActionEnabledWhenLayerSelected()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		
		assertEquals(true, getRemoveAnimationObjectAction().isEnabled());
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
	
	@Test
	public void testUserPromptedWhenRemoveIsFired()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		fireRemoveAnimationObjectAction();
		
		assertEquals(true, confirmationRequested);
	}
	
	@Test
	public void testNoRemovalWhenConfirmationDenied()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		denyRemovalConfirmation();
		
		expectNoObjectRemoval();
		
		fireRemoveAnimationObjectAction();
	}
	
	@Test
	public void testRemovalWhenConfirmationAccepted()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		acceptRemovalConfirmation();
		
		expectObjectRemoval(getAnimationObjects().get(1));
		
		fireRemoveAnimationObjectAction();
	}
	
	@Test
	public void testMoveUpActionDisabledWhenNoSelection()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(null);
		
		assertEquals(false, getMoveObjectUpAction().isEnabled());
	}

	@Test
	public void testMoveUpActionDisabledWhenFirstSelected()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(0));
		
		assertEquals(false, getMoveObjectUpAction().isEnabled());
	}
	
	@Test
	public void testMoveUpActionEnabledWhenSecondSelected()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		
		assertEquals(true, getMoveObjectUpAction().isEnabled());
	}
	
	@Test
	public void testMoveWhenMoveUpActionFired()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		
		expectMove(getAnimationObjects().get(1), 0);
		
		fireMoveObjectUpAction();
	}
	
	@Test
	public void testMoveDownActionDisabledWhenNoSelection()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(null);
		
		assertEquals(false, getMoveObjectDownAction().isEnabled());
	}

	@Test
	public void testMoveDownActionDisabledWhenLastSelected()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(2));
		
		assertEquals(false, getMoveObjectDownAction().isEnabled());
	}
	
	@Test
	public void testMoveDownActionEnabledWhenSecondSelected()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		
		assertEquals(true, getMoveObjectDownAction().isEnabled());
	}
	
	@Test
	public void testMoveWhenMoveDownActionFired()
	{
		addObjectsToAnimation(createCamera(), createLayerWithSingleParameter("layer1"), createLayerWithSingleParameter("layer2"));
		createAnimationBrowserPanel();
		
		select(getAnimationObjects().get(1));
		
		expectMove(getAnimationObjects().get(1), 2);

		fireMoveObjectDownAction();
	}
	
	private void expectMove(final Animatable animatable, final int newIndex)
	{
		mockContext.checking(new Expectations(){{
			exactly(1).of(animation).changeOrderOfAnimatableObject(with(animatable), with(newIndex));
		}});
	}

	private void expectNoObjectRemoval()
	{
		mockContext.checking(new Expectations(){{
			exactly(0).of(animation).removeAnimatableObject(with(any(Animatable.class)));
		}});
	}
	
	private void expectObjectRemoval(final Animatable objectExpectedToBeRemoved)
	{
		mockContext.checking(new Expectations(){{
			exactly(1).of(animation).removeAnimatableObject(with(objectExpectedToBeRemoved));
		}});
	}

	private void denyRemovalConfirmation()
	{
		confirmRemoveObject = false;
	}
	
	private void acceptRemovalConfirmation()
	{
		confirmRemoveObject = true;
	}
	
	private void fireMoveObjectDownAction()
	{
		BasicAction moveObjectDownAction = getMoveObjectDownAction();
		moveObjectDownAction.actionPerformed(new ActionEvent(moveObjectDownAction, 0, null));
	}
	
	private void fireMoveObjectUpAction()
	{
		BasicAction moveObjectUpAction = getMoveObjectUpAction();
		moveObjectUpAction.actionPerformed(new ActionEvent(moveObjectUpAction, 0, null));
	}
	
	private void fireRemoveAnimationObjectAction()
	{
		BasicAction removeAnimationObjectAction = getRemoveAnimationObjectAction();
		removeAnimationObjectAction.actionPerformed(new ActionEvent(removeAnimationObjectAction, 0, null));
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
	
	@SuppressWarnings("serial")
	private void createAnimationBrowserPanel()
	{
		classToBeTested = new AnimationBrowserPanel(animation){
			
			// Overridden to allow control over the dialog
			protected boolean promptUserForConfirmationOfRemoval(AnimationObject selectedObject) 
			{
				confirmationRequested = true;
				return confirmRemoveObject;
			};
		};
	}
	
	private BasicAction getMoveObjectDownAction()
	{
		return getField(classToBeTested, "moveObjectDownAction", BasicAction.class);
	}
	
	private BasicAction getMoveObjectUpAction()
	{
		return getField(classToBeTested, "moveObjectUpAction", BasicAction.class);
	}
	
	private BasicAction getRemoveAnimationObjectAction()
	{
		return getField(classToBeTested, "removeAnimationObjectAction", BasicAction.class);
	}
}
