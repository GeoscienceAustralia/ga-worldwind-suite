package au.gov.ga.worldwind.animator.panels.objectproperties;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
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
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.panels.CollapsiblePanelBase;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.common.ui.JDoubleField;

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
	private JLabel noEditableParameterMessage = new JLabel(getMessage(getObjectPropertiesPanelNoEditableMessageKey()));
	
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
	
	private void addParameterEditor(Parameter parameterToEdit)
	{
		ParameterEditorPanel parameterEditor = new ParameterEditorPanel(parameterToEdit);
		propertiesPanel.add(parameterEditor);
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
			setLayout(new GridLayout(0, 1));
			setBorder(new LineBorder(getBackground().darker(), 1, true));
			
			addParameterName();
			addFieldEntry();
		}

		private void addParameterName()
		{
			JLabel label = new JLabel(getDisplayName(parameterToEdit));
			label.setFont(LAFConstants.getSubHeadingFont());
			add(label);
		}

		private void addFieldEntry()
		{
			JPanel fieldEntryPanel = new JPanel(new BorderLayout(2, 2));
			
			final JDoubleField propertyField = new JDoubleField(parameterToEdit.getCurrentValue(AnimationContext.Factory.createForAnimation(animation)).getValue());
			propertyField.setColumns(10);
			propertyField.setMinValue(getParameterMinValue());
			propertyField.setMaxValue(getParameterMaxValue());
			propertyField.setInputVerifier(new EditableParameterInputVerifier(parameterToEdit));
			propertyField.addFocusListener(new FocusAdapter()
			{
				@Override
				public void focusLost(FocusEvent e)
				{
					parameterToEdit.applyValue(propertyField.getValue());
				}
			});
			
			JLabel valueLabel = new JLabel(getMessage(getObjectPropertiesPanelValueCaptionKey()));
			valueLabel.setLabelFor(propertyField);
			
			fieldEntryPanel.add(valueLabel, BorderLayout.WEST);
			fieldEntryPanel.add(propertyField, BorderLayout.CENTER);
			fieldEntryPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST); //TODO: Is this the best way to add space?
			add(fieldEntryPanel);
		}
		
		private boolean isBoundParameter()
		{
			return parameterToEdit.getClass().isAnnotationPresent(EditableParameter.class) && 
					parameterToEdit.getClass().getAnnotation(EditableParameter.class).bound();
		}
		
		private Double getParameterMaxValue()
		{
			return isBoundParameter() ? parameterToEdit.getClass().getAnnotation(EditableParameter.class).maxValue() : null;
		}

		private Double getParameterMinValue()
		{
			return isBoundParameter() ? parameterToEdit.getClass().getAnnotation(EditableParameter.class).minValue() : null;
		}
		
	}
}
