/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A panel used to edit a single parameter.
 * 
 *  @author James Navin (james.navin@ga.gov.au)
 */
final class ParameterEditorPanel extends JPanel
{
	private static final long serialVersionUID = 20100920L;

	private static final double SPINNER_INCREMENT = 0.01;
	
	private Parameter parameterToEdit;

	private SpinnerNumberModel spinnerModel;
	private JSpinner parameterSpinner;
	
	private boolean updatingDisplay = false;
	
	public ParameterEditorPanel(Parameter parameterToEdit)
	{
		Validate.notNull(parameterToEdit, "A parameter is required");
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
		JLabel label = new JLabel(parameterToEdit.getName());
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
					parameterToEdit.applyValueAnyway((Double)parameterSpinner.getValue());
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
					parameterToEdit.applyValueAnyway((Double)parameterSpinner.getValue());
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
		return parameterToEdit.getCurrentValue().getValue();
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
