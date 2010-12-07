package au.gov.ga.worldwind.animator.panels.layerpalette;

import static au.gov.ga.worldwind.test.util.TestUtils.getField;
import static org.junit.Assert.assertEquals;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JOptionPane;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.layers.LayerIdentifierImpl;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.ListBackedModel;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.util.message.StaticMessageSource;

/**
 * Unit tests for the {@link LayerPalettePanel} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@SuppressWarnings("serial")
public class LayerPalettePanelTest
{
	private LayerPalettePanel classToBeTested;
	private Animation animation;
	
	private Mockery mockContext;
	
	private StaticMessageSource messageSource;
	
	// Flags used to inspect prompting
	private boolean confirmationRequested;
	private boolean confirmPrompt;
	
	private static final int IDENTIFIER_NOT_IN_ANIMATION = 2;
	private static final int IDENTIFIER_IN_ANIMATION = 1;
	private static final List<LayerIdentifier> layerIdentifiers = new ArrayList<LayerIdentifier>()
	{{
		add(new LayerIdentifierImpl("Identifier0", "file://location0")); // In animation
		add(new LayerIdentifierImpl("Identifier1", "file://location1")); // In animation 
		add(new LayerIdentifierImpl("Identifier2", "file://location2")); // Not in animation
		add(new LayerIdentifierImpl("Identifier3", "file://location3")); // Not in animation
	}};
	
	@Before
	public void setup()
	{
		messageSource = new StaticMessageSource();
		MessageSourceAccessor.set(messageSource);
		
		mockContext = new Mockery();
		
		confirmationRequested = false;
		denyRemovalConfirmation();
		
		setupAnimation();
		
		createLayerPalettePanel();
	}
	
	@Test
	public void testRemoveLayerDefinitionDisabledWhenNoSelection()
	{
		select(null);
		
		assertEquals(false, getRemoveLayerDefinitionAction().isEnabled());
	}
	
	@Test
	public void testRemoveLayerDefinitionEnabledWhenIdentiferInAnimationSelected()
	{
		select(IDENTIFIER_IN_ANIMATION);
		
		assertEquals(true, getRemoveLayerDefinitionAction().isEnabled());
	}
	
	@Test
	public void testRemoveLayerDefinitionEnabledWhenIdentiferNotInAnimationSelected()
	{
		select(IDENTIFIER_NOT_IN_ANIMATION);
		
		assertEquals(true, getRemoveLayerDefinitionAction().isEnabled());
	}
	
	@Test
	public void testLoadLayerDefinitionEnabledWhenNoSelection()
	{
		select(null);
		
		assertEquals(true, getLoadLayerDefinitionAction().isEnabled());
	}
	
	@Test
	public void testLoadLayerDefinitionEnabledWhenIdentiferInAnimationSelected()
	{
		select(IDENTIFIER_IN_ANIMATION);
		
		assertEquals(true, getLoadLayerDefinitionAction().isEnabled());
	}
	
	@Test
	public void testLoadLayerDefinitionEnabledWhenIdentiferNotInAnimationSelected()
	{
		select(IDENTIFIER_NOT_IN_ANIMATION);
		
		assertEquals(true, getLoadLayerDefinitionAction().isEnabled());
	}
	
	@Test
	public void testAddLayerToAnimationDisabledWhenNoSelection()
	{
		select(null);
		
		assertEquals(false, getAddLayerToAnimationAction().isEnabled());
	}
	
	@Test
	public void testAddLayerToAnimationDisabledWhenIdentifierInAnimationSelected()
	{
		select(IDENTIFIER_IN_ANIMATION);
		
		assertEquals(false, getAddLayerToAnimationAction().isEnabled());
	}
	
	@Test
	public void testAddLayerToAnimationDisabledWhenIdentifierNotInAnimationSelected()
	{
		select(IDENTIFIER_NOT_IN_ANIMATION);
		
		assertEquals(true, getAddLayerToAnimationAction().isEnabled());
	}
	
	@Test
	public void testUserPromptedWhenSelectRemoveLayerDefinition()
	{
		select(IDENTIFIER_NOT_IN_ANIMATION);
		
		fireRemoveLayerDefinition();
		
		assertEquals(true, confirmationRequested);
	}
	
	@Test
	public void testNoActionWhenConfirmDeniedOnRemoveLayerDefinition()
	{
		select(IDENTIFIER_NOT_IN_ANIMATION);
		
		denyRemovalConfirmation();
		
		fireRemoveLayerDefinition();
		
		assertEquals(4, getListModel().size());
	}
	
	@Test
	public void testLayerRemovedWhenConfirmGrantedOnRemoveLayerDefinition()
	{
		select(IDENTIFIER_NOT_IN_ANIMATION);
		
		grantRemovalConfirmation();
		
		fireRemoveLayerDefinition();
		
		assertEquals(3, getListModel().size());
	}

	private void grantRemovalConfirmation()
	{
		confirmPrompt = true;
	}

	private void denyRemovalConfirmation()
	{
		confirmPrompt = false;
	}
	
	private void fireRemoveLayerDefinition()
	{
		BasicAction removeLayerAction = getRemoveLayerDefinitionAction();
		removeLayerAction.actionPerformed(new ActionEvent(removeLayerAction, 0, null));
	}

	private void setupAnimation()
	{
		animation = mockContext.mock(Animation.class);
		mockContext.checking(new Expectations(){{
			allowing(animation).addChangeListener(with(any(AnimationEventListener.class)));
			allowing(animation).getName();will(returnValue("Animation"));
			allowing(animation).hasLayer(with(layerIdentifiers.get(0)));will(returnValue(true));
			allowing(animation).hasLayer(with(layerIdentifiers.get(1)));will(returnValue(true));
			allowing(animation).hasLayer(with(layerIdentifiers.get(2)));will(returnValue(false));
			allowing(animation).hasLayer(with(layerIdentifiers.get(3)));will(returnValue(false));
			allowing(animation).hasLayer(with((LayerIdentifier)null));will(returnValue(false));
		}});
	}
	
	private void createLayerPalettePanel()
	{
		classToBeTested = new LayerPalettePanel(animation){
			@Override
			protected int issueConfirmationPrompt(String message, String caption)
			{
				confirmationRequested = true;
				return confirmPrompt ? JOptionPane.YES_OPTION : JOptionPane.NO_OPTION;
			}
		};
		
		getListModel().clear(); // Remove the defaults that come in from the global settings
		getListModel().addAll(layerIdentifiers);
	}
	
	private void select(Integer index)
	{
		JList list = getLayerList();
		if (index == null)
		{
			list.getSelectionModel().setSelectionInterval(-1, -1);
		}
		else
		{
			list.setSelectedIndex(index);
		}
	}

	private JList getLayerList()
	{
		return getField(classToBeTested, "layerList", JList.class);
	}
	
	@SuppressWarnings("unchecked")
	private ListBackedModel<LayerIdentifier> getListModel()
	{
		return (ListBackedModel<LayerIdentifier>)getField(classToBeTested, "layerList", JList.class).getModel();
	}
	
	private BasicAction getAddLayerToAnimationAction()
	{
		return getField(classToBeTested, "addLayerToAnimationAction", BasicAction.class);
	}
	
	private BasicAction getLoadLayerDefinitionAction()
	{
		return getField(classToBeTested, "loadLayerDefinitionAction", BasicAction.class);
	}
	
	private BasicAction getRemoveLayerDefinitionAction()
	{
		return getField(classToBeTested, "removeLayerDefinitionAction", BasicAction.class);
	}
}
