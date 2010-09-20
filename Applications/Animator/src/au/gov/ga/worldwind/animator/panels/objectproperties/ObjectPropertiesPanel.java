package au.gov.ga.worldwind.animator.panels.objectproperties;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelNoSelectionMessageKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelSelectionTitleKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.CurrentlySelectedObject;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.Changeable;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.animator.panels.animationbrowser.AnimationTreeModel;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A panel used to display and edit properties of the currently selected animation object
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class ObjectPropertiesPanel extends CollapsiblePanelBase implements CurrentlySelectedObject.ChangeListener, AnimationEventListener
{
	private static final long serialVersionUID = 20100917L;

	private JScrollPane scrollPane;
	
	private JPanel propertiesPanel;
	
	private JLabel panelCaption;
	
	private Animation animation;
	
	public ObjectPropertiesPanel(Animation animation)
	{
		Validate.notNull(animation, "An animation is required");
		this.animation = animation;
		
		setName(getMessage(getObjectPropertiesPanelNameKey()));
		setLayout(new BorderLayout());
		
		panelCaption = new JLabel();
		add(panelCaption, BorderLayout.NORTH);
		
		propertiesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		propertiesPanel.setBackground(Color.WHITE);
		
		scrollPane = new JScrollPane(propertiesPanel);
		
		add(scrollPane, BorderLayout.CENTER);
		
		updatePropertiesDisplay();
		
		CurrentlySelectedObject.addChangeListener(this);
	}
	
	private void updatePropertiesDisplay()
	{
		AnimationObject currentObject = CurrentlySelectedObject.get();
		if (currentObject == null)
		{
			panelCaption.setText(getMessage(getObjectPropertiesPanelNoSelectionMessageKey()));
			return;
		}
		panelCaption.setText(getMessage(getObjectPropertiesPanelSelectionTitleKey(), getDisplayName(currentObject)));
		
		propertiesPanel.removeAll();
		if (isEditableParameter(currentObject))
		{
			addParameterEditor((Parameter)currentObject);
		}
		else if (isAnimatableObject(currentObject))
		{
			for (Parameter parameter : ((Animatable)currentObject).getEnabledParameters())
			{
				if (isEditableParameter(parameter))
				{
					addParameterEditor(parameter);
				}
			}
		}
		
		
		validate();
		repaint();
	}
	
	private void addParameterEditor(Parameter parameterToEdit)
	{
		propertiesPanel.add(new ParameterEditorPanel(parameterToEdit));
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
	
	@Override
	public void refreshView(ChangeEvent e)
	{
		if (e != null && e.getSource() instanceof Animation)
		{
			this.animation = (Animation)e.getSource();
			updatePropertiesDisplay();
		}
	}

	@Override
	public void selectedObjectChanged(AnimationObject currentlySelectedObject, AnimationObject previouslySelectedObject)
	{
		updatePropertiesDisplay();
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
		updatePropertiesDisplay();
	}
	
	/**
	 * A panel used to edit a single parameter. 
	 */
	private final class ParameterEditorPanel extends JPanel
	{
		private static final long serialVersionUID = 20100920L;

		private Parameter parameterToEdit;

		public ParameterEditorPanel(Parameter parameterToEdit)
		{
			Validate.notNull(parameterToEdit, "A parameter is required");
			this.parameterToEdit = parameterToEdit;
			
			populatePanel();
		}

		private void populatePanel()
		{
			setLayout(new FlowLayout(FlowLayout.LEFT));
			setBorder(BorderFactory.createEtchedBorder());
			
			addFieldEntry();
		}

		private void addFieldEntry()
		{
			JPanel fieldEntryPanel = new JPanel(new BorderLayout());
			
			fieldEntryPanel.add(new JLabel(getDisplayName(parameterToEdit) + ": "), BorderLayout.WEST);
			
			JTextField propertyField = new JTextField(Double.toString(parameterToEdit.getCurrentValue(AnimationContext.Factory.createForAnimation(animation)).getValue()));
			propertyField.setInputVerifier(new EditableParameterInputVerifier(parameterToEdit));
			fieldEntryPanel.add(propertyField, BorderLayout.CENTER);
			
			add(fieldEntryPanel);
		}
		
	}
}
