package au.gov.ga.worldwind.animator.panels.objectproperties;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getObjectPropertiesPanelValueCaptionKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A panel used to edit a single parameter. 
 */
final class ParameterEditorPanel extends JPanel
{
	private static final long serialVersionUID = 20100920L;

	private static final double SPINNER_INCREMENT = 0.01;
	
	private Animation animation;
	
	private Parameter parameterToEdit;

	private SpinnerNumberModel spinnerModel;
	private JSpinner parameterSpinner;
	
	private boolean updatingDisplay = false;
	
	public ParameterEditorPanel(Animation animation, Parameter parameterToEdit)
	{
		Validate.notNull(animation, "An animation is required");
		Validate.notNull(parameterToEdit, "A parameter is required");
		this.animation = animation;
		this.parameterToEdit = parameterToEdit;
		
		populatePanel();
	}

	private void populatePanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new LineBorder(getBackground().darker(), 1, true));
		
		addParameterName();
		addFieldEntry();
	}
	
	private void addParameterName()
	{
		JLabel label = new JLabel(getDisplayName(parameterToEdit));
		label.setFont(LAFConstants.getSubHeadingFont());
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(label);
	}

	@SuppressWarnings("rawtypes") // Required to distinguish constructors for SpinnerNumberModel()
	private void addFieldEntry()
	{
		JPanel fieldEntryPanel = new JPanel();
		fieldEntryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldEntryPanel.setLayout(new BoxLayout(fieldEntryPanel, BoxLayout.X_AXIS));
		
		spinnerModel = new SpinnerNumberModel(getParameterCurrentValue(), (Comparable)getParameterMinValue(), getParameterMaxValue(), SPINNER_INCREMENT);
		parameterSpinner = new JSpinner(spinnerModel);
		parameterSpinner.setEditor(new JSpinner.NumberEditor(parameterSpinner));
		parameterSpinner.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (!updatingDisplay)
				{
					parameterToEdit.applyValue((Double)parameterSpinner.getValue());
				}
			}
		});
		parameterSpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (!updatingDisplay)
				{
					parameterToEdit.applyValue((Double)parameterSpinner.getValue());
				}
			}
		});
		parameterSpinner.setPreferredSize(new Dimension(80, 18));
		parameterSpinner.setMaximumSize(new Dimension(80, 18));
		
		JLabel valueLabel = new JLabel(getMessage(getObjectPropertiesPanelValueCaptionKey()));
		valueLabel.setLabelFor(parameterSpinner);
		
		fieldEntryPanel.add(valueLabel);
		fieldEntryPanel.add(parameterSpinner);
		fieldEntryPanel.add(new JLabel(getParameterUnits()));
		fieldEntryPanel.add(Box.createHorizontalGlue());
		
		// Pack the field entry into the parent 
		add(fieldEntryPanel);
		add(Box.createVerticalGlue());
	}

	public void updateDisplay()
	{
		updatingDisplay = true;
		parameterSpinner.setValue(getParameterCurrentValue());
		updatingDisplay = false;
	}
	
	private double getParameterCurrentValue()
	{
		return parameterToEdit.getCurrentValue(AnimationContext.Factory.createForAnimation(animation)).getValue();
	}
	
	/**
	 * @return A display name to use for the provided object. Uses the object's name if {@link Nameable}, 
	 * otherwise uses {@link Object#toString()}.
	 */
	// TODO: This method is duplicated in ObjectPropertiesPanel. Need to refactor.
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
	
	private String getParameterUnits()
	{
		return parameterToEdit.getClass().getAnnotation(EditableParameter.class).units();
	}

}