package au.gov.ga.worldwind.animator.panels.objectproperties;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelNoEditableMessageKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelNoSelectionMessageKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelSelectionTitleKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.CurrentlySelectedObject;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.Changeable;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.panels.AnimatorCollapsiblePanel;
import au.gov.ga.worldwind.animator.ui.frameslider.ChangeFrameListener;
import au.gov.ga.worldwind.animator.ui.frameslider.CurrentFrameChangeListener;
import au.gov.ga.worldwind.common.ui.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A panel used to display and edit properties (parameters) of the currently selected animation object
 * <p/>
 * Can be attached as a listener to multiple event types within an animation to trigger an update on events like:
 * <ul>
 * 	<li>Change of frame
 * 	<li>Movement of the camera
 * 	<li>Selection of different animation object
 * </ul>
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class ObjectPropertiesPanel extends CollapsiblePanelBase implements AnimatorCollapsiblePanel,
																		   CurrentlySelectedObject.ChangeListener, 
																		   AnimationEventListener, 
																		   ChangeFrameListener,
																		   CurrentFrameChangeListener,
																		   ChangeListener,
																		   PropertyChangeListener
{
	private static final long serialVersionUID = 20100917L;

	/** The message to display when no editable parameters are available */
	private static final JLabel noEditableParameterMessage = new JLabel(getMessage(getObjectPropertiesPanelNoEditableMessageKey()));
	
	private JScrollPane scrollPane;
	
	/** The panel that holds the parameter editors */
	private JPanel propertiesPanel;
	
	private JLabel panelCaption;
	
	// The cache of parameter editors
	private Map<Parameter, ParameterEditorPanel> editorMap = new HashMap<Parameter, ParameterEditorPanel>();
	private List<ParameterEditorPanel> visibleEditors = new ArrayList<ParameterEditorPanel>();
	
	private Animation animation;
	
	public ObjectPropertiesPanel(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
		
		setName(getMessage(getObjectPropertiesPanelNameKey()));
		setLayout(new BorderLayout());
		
		panelCaption = new JLabel();
		add(panelCaption, BorderLayout.NORTH);
		
		propertiesPanel = new JPanel(new GridLayout(0, 1, 2, 2));
		propertiesPanel.setBorder(null);
		
		scrollPane = new JScrollPane(propertiesPanel);
		scrollPane.setBorder(null);
		
		add(scrollPane, BorderLayout.CENTER);
		
		updatePanelDisplay();
		
		CurrentlySelectedObject.addChangeListener(this);
	}
	
	private void updatePanelDisplay()
	{
		AnimationObject currentObject = CurrentlySelectedObject.get();
		if (currentObject == null)
		{
			panelCaption.setText(getMessage(getObjectPropertiesPanelNoSelectionMessageKey()));
			return;
		}
		panelCaption.setText(getMessage(getObjectPropertiesPanelSelectionTitleKey(), currentObject.getName()));
		
		propertiesPanel.removeAll();
		visibleEditors.clear();
		int parameterCounter = 0;
		if (isEditableParameter(currentObject))
		{
			addParameterEditorToPanel((Parameter)currentObject);
			parameterCounter++;
		}
		else if (isAnimatableObject(currentObject))
		{
			for (Parameter parameter : ((Animatable)currentObject).getEnabledParameters())
			{
				if (isEditableParameter(parameter))
				{
					addParameterEditorToPanel(parameter);
					parameterCounter++;
				}
			}
		}
		
		if (parameterCounter == 0)
		{
			propertiesPanel.add(noEditableParameterMessage);
		}
		
		validate();
		repaint();
	}
	
	/**
	 * Update the displays of the visible parameter editors.
	 * <p/>
	 * Should be called to notify the editors of a change in the animation state
	 */
	private void updateParameterEditorsDisplay()
	{
		for (ParameterEditorPanel parameterEditor : visibleEditors)
		{
			parameterEditor.updateDisplay();
		}
	}
	
	private void addParameterEditorToPanel(Parameter parameterToEdit)
	{
		ParameterEditorPanel parameterEditor = editorMap.get(parameterToEdit);
		if (parameterEditor == null)
		{
			parameterEditor = new ParameterEditorPanel(animation, parameterToEdit);
			editorMap.put(parameterToEdit, parameterEditor);
		}
		parameterEditor.updateDisplay();
		propertiesPanel.add(parameterEditor);
		visibleEditors.add(parameterEditor);
	}

	private boolean isAnimatableObject(AnimationObject currentObject)
	{
		return currentObject instanceof Animatable;
	}

	/**
	 * Checks for the editable parameter annotation on the provided object
	 */
	private boolean isEditableParameter(AnimationObject currentObject)
	{
		if (currentObject == null || !(currentObject instanceof Parameter))
		{
			return false;
		}
		
		return currentObject.getClass().isAnnotationPresent(EditableParameter.class);
	}

	// Event listener methods
	
	@Override
	public void updateAnimation(Animation newAnimation)
	{
		if (newAnimation == null)
		{
			return;
		}
		
		this.animation = newAnimation;
		this.editorMap.clear();
		updatePanelDisplay();
	}

	@Override
	public void selectedObjectChanged(AnimationObject currentlySelectedObject, AnimationObject previouslySelectedObject)
	{
		updatePanelDisplay();
		if (previouslySelectedObject instanceof Changeable)
		{
			((Changeable)previouslySelectedObject).removeChangeListener(this);
		}
		if (currentlySelectedObject instanceof Changeable)
		{
			((Changeable)currentlySelectedObject).addChangeListener(this);
		}
	}

	@Override
	public void receiveAnimationEvent(AnimationEvent event)
	{
		updateParameterEditorsDisplay();
	}

	@Override
	public void frameChanged(int index, int oldFrame, int newFrame)
	{
		updateParameterEditorsDisplay();
	}

	@Override
	public void currentFrameChanged(int newCurrentFrame)
	{
		updateParameterEditorsDisplay();
	}
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		updateParameterEditorsDisplay();		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		updateParameterEditorsDisplay();
	}
}
