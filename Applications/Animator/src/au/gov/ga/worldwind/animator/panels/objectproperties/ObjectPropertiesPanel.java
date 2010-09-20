package au.gov.ga.worldwind.animator.panels.objectproperties;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelNoEditableMessageKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelNoSelectionMessageKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelSelectionTitleKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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
import au.gov.ga.worldwind.animator.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.animator.util.ChangeFrameListener;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A panel used to display and edit properties of the currently selected animation object
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class ObjectPropertiesPanel extends CollapsiblePanelBase implements CurrentlySelectedObject.ChangeListener, AnimationEventListener, ChangeFrameListener, ChangeListener
{
	private static final long serialVersionUID = 20100917L;

	private JScrollPane scrollPane;
	
	private JPanel propertiesPanel;
	
	private JLabel panelCaption;
	private JLabel noEditableParameterMessage = new JLabel(getMessage(getObjectPropertiesPanelNoEditableMessageKey()));
	
	private Map<Parameter, ParameterEditorPanel> editorMap = new HashMap<Parameter, ParameterEditorPanel>();
	private List<ParameterEditorPanel> visibleEditors = new ArrayList<ParameterEditorPanel>();
	
	Animation animation;
	
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
		panelCaption.setText(getMessage(getObjectPropertiesPanelSelectionTitleKey(), getDisplayName(currentObject)));
		
		propertiesPanel.removeAll();
		visibleEditors.clear();
		int parameterCounter = 0;
		if (isEditableParameter(currentObject))
		{
			addParameterEditor((Parameter)currentObject);
			parameterCounter++;
		}
		else if (isAnimatableObject(currentObject))
		{
			for (Parameter parameter : ((Animatable)currentObject).getEnabledParameters())
			{
				if (isEditableParameter(parameter))
				{
					addParameterEditor(parameter);
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
	
	private void updateParameterEditorsDisplay()
	{
		for (ParameterEditorPanel parameterEditor : visibleEditors)
		{
			parameterEditor.updateDisplay();
		}
		
	}
	
	private void addParameterEditor(Parameter parameterToEdit)
	{
		ParameterEditorPanel parameterEditor = editorMap.get(parameterToEdit);
		if (parameterEditor == null)
		{
			parameterEditor = new ParameterEditorPanel(animation, parameterToEdit);
			editorMap.put(parameterToEdit, parameterEditor);
		}
		
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

	/**
	 * @return A display name to use for the provided object. Uses the object's name if {@link Nameable}, 
	 * otherwise uses {@link Object#toString()}.
	 */
	private String getDisplayName(AnimationObject object)
	{
		if (object == null)
		{
			return null;
		}
		
		if (object instanceof Nameable)
		{
			return ((Nameable)object).getName();
		}
		return object.toString();
	}
	
	// Event listener methods
	
	@Override
	public void refreshView(ChangeEvent e)
	{
		if (e != null && e.getSource() instanceof Animation)
		{
			this.animation = (Animation)e.getSource();
			this.editorMap.clear();
		}
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
	public void stateChanged(ChangeEvent e)
	{
		updateParameterEditorsDisplay();		
	}
}
